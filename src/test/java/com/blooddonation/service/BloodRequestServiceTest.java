package com.blooddonation.service;

import com.blooddonation.dto.BloodRequestDto;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.User;
import com.blooddonation.repository.BloodRequestRepository;
import com.blooddonation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BloodRequestServiceTest {

    @Mock
    private BloodRequestRepository bloodRequestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BloodRequestService bloodRequestService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setUsername("testuser");
    }

    @Test
    void createRequest_Success() {
        BloodRequestDto dto = new BloodRequestDto();
        dto.setBloodGroup("A+");
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

        when(bloodRequestRepository.findByStatusOrderByRequestDateDesc("PENDING"))
                .thenReturn(Collections.singletonList(request));

        List<BloodRequestDto> requests = bloodRequestService.getAllActiveRequests();

        assertFalse(requests.isEmpty());
        assertEquals(1, requests.size());
        assertEquals("O-", requests.get(0).getBloodGroup());
    }
}
