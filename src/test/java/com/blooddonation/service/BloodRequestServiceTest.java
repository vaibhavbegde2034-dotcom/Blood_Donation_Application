package com.blooddonation.service;

import com.blooddonation.dto.BloodRequestDto;
import com.blooddonation.dto.ApiResponseDto;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.User;
import com.blooddonation.repository.BloodRequestRepository;
import com.blooddonation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BloodRequestServiceTest {

    private BloodRequestRepository bloodRequestRepository;

    private UserRepository userRepository;

    private BloodRequestService bloodRequestService;

    private JavaMailSender mailSender;
    private OtpEmailService otpEmailService;
    private User testUser;
    private User donorUser;

    @BeforeEach
    void setUp() {
        bloodRequestRepository = mock(BloodRequestRepository.class);
        userRepository = mock(UserRepository.class);
        mailSender = mock(JavaMailSender.class);
        otpEmailService = new OtpEmailService(mailSender);
        ReflectionTestUtils.setField(otpEmailService, "fromAddress", "noreply@bloodcare.test");

        bloodRequestService = new BloodRequestService();
        ReflectionTestUtils.setField(bloodRequestService, "bloodRequestRepository", bloodRequestRepository);
        ReflectionTestUtils.setField(bloodRequestService, "userRepository", userRepository);
        ReflectionTestUtils.setField(bloodRequestService, "otpEmailService", otpEmailService);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("patient@example.com");
        testUser.setFullName("Patient User");

        donorUser = new User();
        donorUser.setUsername("donor1");
        donorUser.setFullName("Donor One");
    }

    @Test
    void createRequest_Success() {
        BloodRequestDto dto = new BloodRequestDto();
        dto.setPatientName("Patient A");
        dto.setBloodGroup("A+");
        dto.setUnitsRequired(2);
        dto.setCity("New York");
        dto.setHospitalName("City Hospital");
        dto.setContactNumber("1234567890");
        dto.setUrgency("URGENT");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bloodRequestRepository.save(any(BloodRequest.class))).thenAnswer(i -> i.getArguments()[0]);

        BloodRequest request = bloodRequestService.createRequest(dto, "testuser");

        assertNotNull(request);
        assertEquals("A+", request.getBloodGroup());
        assertEquals("PENDING", request.getStatus());
        assertEquals(testUser, request.getRequester());
        verify(bloodRequestRepository, times(1)).save(any(BloodRequest.class));
    }

    @Test
    void createRequest_Autofill_Success() {
        testUser.setCity("Old City");
        testUser.setContactNumber("9876543210");
        testUser.setFullName("Test User Full Name");

        BloodRequestDto dto = new BloodRequestDto();
        dto.setPatientName("Patient B");
        dto.setBloodGroup("O+");
        dto.setUnitsRequired(1);
        // Leave city and contactNumber empty to trigger autofill
        dto.setHospitalName("Main Hospital");
        dto.setUrgency("NORMAL");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bloodRequestRepository.save(any(BloodRequest.class))).thenAnswer(i -> i.getArguments()[0]);

        BloodRequest request = bloodRequestService.createRequest(dto, "testuser");

        assertNotNull(request);
        assertEquals("O+", request.getBloodGroup());
        assertEquals("Old City", request.getCity()); // Autofilled
        assertEquals("9876543210", request.getContactNumber()); // Autofilled
        assertEquals("Test User Full Name", request.getRequesterName()); // Autofilled
        verify(bloodRequestRepository, times(1)).save(any(BloodRequest.class));
    }

    @Test
    void getMyRequests_Success() {
        BloodRequest request = new BloodRequest();
        request.setBloodGroup("B+");
        request.setRequester(testUser);
        request.setStatus("PENDING");
        request.setRequestDate(LocalDateTime.now());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bloodRequestRepository.findByRequesterOrderByRequestDateDesc(testUser))
                .thenReturn(Collections.singletonList(request));

        List<BloodRequestDto> requests = bloodRequestService.getMyRequests("testuser");

        assertFalse(requests.isEmpty());
        assertEquals(1, requests.size());
        assertEquals("B+", requests.get(0).getBloodGroup());
    }

    @Test
    void getAllActiveRequests_Success() {
        BloodRequest request = new BloodRequest();
        request.setBloodGroup("O-");
        request.setStatus("PENDING");
        request.setRequester(testUser);
        request.setRequestDate(LocalDateTime.now());

        when(bloodRequestRepository.findByStatusInOrderByRequestDateDesc(Arrays.asList("PENDING", "DONOR_REQUEST_SENT", "OTP_VERIFICATION")))
                .thenReturn(Collections.singletonList(request));

        List<BloodRequestDto> requests = bloodRequestService.getAllActiveRequests();

        assertFalse(requests.isEmpty());
        assertEquals(1, requests.size());
        assertEquals("O-", requests.get(0).getBloodGroup());
    }

    @Test
    void acceptByDonor_Success() {
        BloodRequest request = new BloodRequest();
        request.setId(10L);
        request.setRequester(testUser);
        request.setStatus("PENDING");

        when(bloodRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(userRepository.findByUsername("donor1")).thenReturn(Optional.of(donorUser));

        ApiResponseDto response = bloodRequestService.acceptByDonor(10L, "donor1");

        assertTrue(response.isStatus());
        assertEquals("DONOR_REQUEST_SENT", request.getStatus());
        assertEquals("donor1", request.getAcceptedDonorUsername());
        assertEquals("Donor One", request.getAcceptedDonorName());
        assertNotNull(request.getDonorRequestSentAt());
        verify(bloodRequestRepository).save(request);
    }

    @Test
    void patientAcceptDonorRequest_SendsOtpAndMovesToVerification() {
        BloodRequest request = new BloodRequest();
        request.setId(11L);
        request.setRequester(testUser);
        request.setStatus("DONOR_REQUEST_SENT");
        request.setAcceptedDonorUsername("donor1");
        request.setAcceptedDonorName("Donor One");

        when(bloodRequestRepository.findById(11L)).thenReturn(Optional.of(request));

        ApiResponseDto response = bloodRequestService.patientAcceptDonorRequest(11L, "testuser");

        assertTrue(response.isStatus());
        assertEquals("OTP_VERIFICATION", request.getStatus());
        assertTrue(request.getPatientAcceptedDonorRequest());
        assertNotNull(request.getDonationOtp());
        assertEquals(6, request.getDonationOtp().length());
        assertNotNull(request.getOtpSentAt());
        assertNotNull(request.getPatientAcceptedAt());
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
        verify(bloodRequestRepository).save(request);
    }

    @Test
    void patientAcceptDonorRequest_StillSucceedsWhenEmailIsNotConfigured() {
        ReflectionTestUtils.setField(otpEmailService, "fromAddress", "");

        BloodRequest request = new BloodRequest();
        request.setId(13L);
        request.setRequester(testUser);
        request.setStatus("DONOR_REQUEST_SENT");
        request.setAcceptedDonorUsername("donor1");
        request.setAcceptedDonorName("Donor One");

        when(bloodRequestRepository.findById(13L)).thenReturn(Optional.of(request));

        ApiResponseDto response = bloodRequestService.patientAcceptDonorRequest(13L, "testuser");

        assertTrue(response.isStatus());
        assertTrue(response.getMessage().contains("OTP generated, but email could not be sent"));
        assertEquals("OTP_VERIFICATION", request.getStatus());
        assertTrue(request.getPatientAcceptedDonorRequest());
        assertNotNull(request.getDonationOtp());
        verify(mailSender, never()).send(any(org.springframework.mail.SimpleMailMessage.class));
        verify(bloodRequestRepository).save(request);
    }

    @Test
    void verifyOtp_CompletesDonation() {
        BloodRequest request = new BloodRequest();
        request.setId(12L);
        request.setRequester(testUser);
        request.setStatus("OTP_VERIFICATION");
        request.setAcceptedDonorUsername("donor1");
        request.setDonationOtp("123456");
        request.setOtpVerified(Boolean.FALSE);

        when(bloodRequestRepository.findById(12L)).thenReturn(Optional.of(request));

        ApiResponseDto response = bloodRequestService.verifyOtp(12L, "donor1", "123456");

        assertTrue(response.isStatus());
        assertEquals("COMPLETED", request.getStatus());
        assertTrue(request.getOtpVerified());
        assertNull(request.getDonationOtp());
        assertNotNull(request.getOtpVerifiedAt());
        assertNotNull(request.getDonationCompletedAt());
        verify(bloodRequestRepository).save(request);
    }
}
