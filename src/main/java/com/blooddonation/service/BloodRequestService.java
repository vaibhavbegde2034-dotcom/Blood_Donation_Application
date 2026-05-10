package com.blooddonation.service;

import com.blooddonation.dto.ApiResponseDto;
import com.blooddonation.dto.BloodRequestDto;
import com.blooddonation.model.BloodBank;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.BloodStock;
import com.blooddonation.model.User;
import com.blooddonation.repository.BloodBankRepository;
import com.blooddonation.repository.BloodRequestRepository;
import com.blooddonation.repository.BloodStockRepository;
import com.blooddonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BloodRequestService {

    private static final SecureRandom OTP_RANDOM = new SecureRandom();
    private static final List<String> ACTIVE_REQUEST_STATUSES = Arrays.asList(
            "PENDING",
            "DONOR_REQUEST_SENT",
            "OTP_VERIFICATION"
    );

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpEmailService otpEmailService;

    @Transactional
    public BloodRequest createRequest(BloodRequestDto dto, String username) {
        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BloodRequest request = new BloodRequest();
        request.setRequester(requester);
        request.setPatientName(dto.getPatientName());
        request.setBloodGroup(dto.getBloodGroup());
        request.setUnitsRequired(dto.getUnitsRequired());
        request.setCity(dto.getCity() != null && !dto.getCity().isEmpty() ? dto.getCity() : requester.getCity());
        request.setHospitalName(dto.getHospitalName());
        request.setContactNumber(dto.getContactNumber() != null && !dto.getContactNumber().isEmpty() ? dto.getContactNumber() : requester.getContactNumber());
        request.setUrgency(dto.getUrgency());
        request.setDescription(dto.getDescription());
        request.setRequesterName(requester.getFullName());
        request.setStatus("PENDING");
        request.setOtpVerified(Boolean.FALSE);
        request.setPatientAcceptedDonorRequest(Boolean.FALSE);
        request.setRequestDate(LocalDateTime.now());

        return bloodRequestRepository.save(request);
    }

    public List<BloodRequestDto> getMyRequests(String username) {
        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return bloodRequestRepository.findByRequesterOrderByRequestDateDesc(requester)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BloodRequestDto> getAllActiveRequests() {
        return bloodRequestRepository.findByStatusInOrderByRequestDateDesc(ACTIVE_REQUEST_STATUSES)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BloodRequestDto> getRequestsByCity(String city) {
        return bloodRequestRepository.findByCityIgnoreCaseAndStatusInOrderByRequestDateDesc(city, ACTIVE_REQUEST_STATUSES)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BloodRequestDto getRequestById(Long id) {
        return bloodRequestRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("Request not found"));
    }

    @Autowired
    private BloodStockRepository bloodStockRepository;

    @Autowired
    private BloodBankRepository bloodBankRepository;

    @Transactional
    public ApiResponseDto acceptRequest(Long requestId, Long bloodBankId) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        BloodBank bank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new RuntimeException("Blood Bank not found"));

        // 1. Find the stock for the requested blood group in this bank
        BloodStock stock = bloodStockRepository.findByBloodBankAndBloodGroup(bank, request.getBloodGroup())
                .orElseThrow(() -> new RuntimeException("Blood group not found in inventory"));

        // 2. Check if sufficient units are available
        if (stock.getUnits() < request.getUnitsRequired()) {
            return new ApiResponseDto("Insufficient blood units in inventory", false);
        }

        // 3. Deduct units and save
        stock.setUnits(stock.getUnits() - request.getUnitsRequired());
        bloodStockRepository.save(stock);

        // 4. Update request status
        request.setAcceptedBloodBankId(bank.getId());
        request.setAcceptedBloodBankName(bank.getBankName());
        request.setOtpVerified(Boolean.FALSE);
        bloodRequestRepository.save(request);

        return new ApiResponseDto("Request accepted. Donor can now proceed for OTP confirmation.", true);
    }

    @Transactional
    public ApiResponseDto rejectRequest(Long requestId) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus("REJECTED");
        bloodRequestRepository.save(request);
        return new ApiResponseDto("Request rejected", true);
    }

    @Transactional
    public ApiResponseDto acceptByDonor(Long requestId, String donorUsername) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        User donor = userRepository.findByUsername(donorUsername)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        if (!"PENDING".equals(request.getStatus())) {
            return new ApiResponseDto("Only pending requests can be accepted by a donor", false);
        }

        request.setAcceptedDonorUsername(donor.getUsername());
        request.setAcceptedDonorName(donor.getFullName() != null && !donor.getFullName().isBlank() ? donor.getFullName() : donor.getUsername());
        request.setPatientAcceptedDonorRequest(Boolean.FALSE);
        request.setDonorRequestSentAt(LocalDateTime.now());
        request.setStatus("DONOR_REQUEST_SENT");
        bloodRequestRepository.save(request);

        return new ApiResponseDto("Donor is ready to donate. Patient can now accept the donor request.", true);
    }

    @Transactional
    public ApiResponseDto sendDonorRequest(Long requestId, String donorUsername) {
        BloodRequest request = getRequestForDonor(requestId, donorUsername);

        if (Boolean.TRUE.equals(request.getPatientAcceptedDonorRequest())) {
            return new ApiResponseDto("Patient has already accepted the donor request", false);
        }
        if ("COMPLETED".equals(request.getStatus())) {
            return new ApiResponseDto("Donation is already completed", false);
        }

        request.setStatus("DONOR_REQUEST_SENT");
        request.setDonorRequestSentAt(LocalDateTime.now());
        bloodRequestRepository.save(request);

        return new ApiResponseDto("Donor request sent to patient. Patient can now accept this donor.", true);
    }

    @Transactional
    public ApiResponseDto patientAcceptDonorRequest(Long requestId, String requesterUsername) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getRequester().getUsername().equalsIgnoreCase(requesterUsername)) {
            return new ApiResponseDto("Only the requester can accept this donor request", false);
        }
        if (request.getAcceptedDonorUsername() == null || request.getAcceptedDonorUsername().isBlank()) {
            return new ApiResponseDto("No donor has accepted this request yet", false);
        }
        if (!"DONOR_REQUEST_SENT".equals(request.getStatus())) {
            return new ApiResponseDto("Donor request must be sent before patient acceptance", false);
        }

        User requester = request.getRequester();
        String otp = String.format("%06d", OTP_RANDOM.nextInt(1_000_000));
        request.setPatientAcceptedDonorRequest(Boolean.TRUE);
        request.setPatientAcceptedAt(LocalDateTime.now());
        request.setDonationOtp(otp);
        request.setOtpVerified(Boolean.FALSE);
        request.setOtpSentAt(LocalDateTime.now());
        request.setOtpVerifiedAt(null);
        request.setDonationCompletedAt(null);
        request.setStatus("OTP_VERIFICATION");
        bloodRequestRepository.save(request);

        try {
            otpEmailService.sendDonationOtp(requester, request, otp);
            return new ApiResponseDto("Patient accepted donor request. OTP sent to patient email.", true);
        } catch (RuntimeException ex) {
            return new ApiResponseDto(
                    "Patient accepted donor request. OTP generated, but email could not be sent: " + ex.getMessage(),
                    true
            );
        }
    }

    @Transactional
    public ApiResponseDto verifyOtp(Long requestId, String donorUsername, String otp) {
        BloodRequest request = getRequestForDonor(requestId, donorUsername);

        if (!"OTP_VERIFICATION".equals(request.getStatus())) {
            return new ApiResponseDto("Send OTP before verifying it", false);
        }
        if (request.getDonationOtp() == null || request.getDonationOtp().isBlank()) {
            return new ApiResponseDto("No OTP has been generated for this request", false);
        }
        if (!request.getDonationOtp().equals(otp)) {
            return new ApiResponseDto("Invalid OTP. Ask the requester to confirm the latest code.", false);
        }

        request.setOtpVerified(Boolean.TRUE);
        request.setOtpVerifiedAt(LocalDateTime.now());
        request.setStatus("COMPLETED");
        request.setDonationCompletedAt(LocalDateTime.now());
        request.setDonationOtp(null);
        bloodRequestRepository.save(request);

        return new ApiResponseDto("OTP verified successfully. Donation Successfully Completed.", true);
    }

    @Transactional
    public ApiResponseDto confirmDonation(Long requestId, String donorUsername) {
        BloodRequest request = getRequestForDonor(requestId, donorUsername);

        if (!Boolean.TRUE.equals(request.getOtpVerified())) {
            return new ApiResponseDto("Verify OTP before confirming donation", false);
        }
        if ("COMPLETED".equals(request.getStatus())) {
            return new ApiResponseDto("Donation is already completed", false);
        }

        request.setStatus("COMPLETED");
        request.setDonationCompletedAt(LocalDateTime.now());
        request.setDonationOtp(null);
        bloodRequestRepository.save(request);

        return new ApiResponseDto("Donation confirmed successfully", true);
    }

    private BloodRequestDto convertToDto(BloodRequest request) {
        BloodRequestDto dto = new BloodRequestDto();
        dto.setId(request.getId());
        dto.setPatientName(request.getPatientName());
        dto.setBloodGroup(request.getBloodGroup());
        dto.setUnitsRequired(request.getUnitsRequired());
        dto.setCity(request.getCity());
        dto.setHospitalName(request.getHospitalName());
        dto.setContactNumber(request.getContactNumber());
        dto.setUrgency(request.getUrgency());
        dto.setStatus(request.getStatus());
        dto.setDescription(request.getDescription());
        dto.setRequestDate(request.getRequestDate());
        dto.setRequesterUsername(request.getRequester().getUsername());
        dto.setRequesterName(request.getRequesterName());
        dto.setPrescriptionFilePath(request.getPrescriptionFilePath());
        dto.setAcceptedBloodBankId(request.getAcceptedBloodBankId());
        dto.setAcceptedBloodBankName(request.getAcceptedBloodBankName());
        dto.setAcceptedDonorUsername(request.getAcceptedDonorUsername());
        dto.setAcceptedDonorName(request.getAcceptedDonorName());
        dto.setOtpVerified(request.getOtpVerified());
        dto.setPatientAcceptedDonorRequest(request.getPatientAcceptedDonorRequest());
        dto.setOtpSentAt(request.getOtpSentAt());
        dto.setOtpVerifiedAt(request.getOtpVerifiedAt());
        dto.setDonationCompletedAt(request.getDonationCompletedAt());
        dto.setDonorRequestSentAt(request.getDonorRequestSentAt());
        dto.setPatientAcceptedAt(request.getPatientAcceptedAt());
        return dto;
    }

    private BloodRequest getRequestForDonor(Long requestId, String donorUsername) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getAcceptedDonorUsername() == null || request.getAcceptedDonorUsername().isBlank()) {
            throw new RuntimeException("Donor must accept the request before continuing");
        }

        if (!request.getAcceptedDonorUsername().equalsIgnoreCase(donorUsername)) {
            throw new RuntimeException("This request is assigned to another donor");
        }

        return request;
    }
}
