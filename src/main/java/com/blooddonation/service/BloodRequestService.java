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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BloodRequestService {

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public BloodRequest createRequest(BloodRequestDto dto, String username) {
        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BloodRequest request = new BloodRequest();
        request.setRequester(requester);
        request.setPatientName(dto.getPatientName());
        request.setBloodGroup(dto.getBloodGroup());
        request.setUnitsRequired(dto.getUnitsRequired());
        request.setCity(dto.getCity());
        request.setHospitalName(dto.getHospitalName());
        request.setContactNumber(dto.getContactNumber());
        request.setUrgency(dto.getUrgency());
        request.setDescription(dto.getDescription());
        request.setRequesterName(requester.getFullName());
        request.setStatus("PENDING");
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
        return bloodRequestRepository.findByStatusOrderByRequestDateDesc("PENDING")
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BloodRequestDto> getRequestsByCity(String city) {
        return bloodRequestRepository.findByCityIgnoreCaseAndStatusOrderByRequestDateDesc(city, "PENDING")
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
        request.setStatus("ACCEPTED");
        bloodRequestRepository.save(request);

        return new ApiResponseDto("Request accepted and inventory updated", true);
    }

    @Transactional
    public ApiResponseDto rejectRequest(Long requestId) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus("REJECTED");
        bloodRequestRepository.save(request);
        return new ApiResponseDto("Request rejected", true);
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
        return dto;
    }
}
