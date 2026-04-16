package com.blooddonation.service;

import com.blooddonation.dto.BloodRequestDto;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.User;
import com.blooddonation.repository.BloodRequestRepository;
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
        request.setBloodGroup(dto.getBloodGroup());
        request.setCity(dto.getCity());
        request.setHospitalName(dto.getHospitalName());
        request.setContactNumber(dto.getContactNumber());
        request.setUrgency(dto.getUrgency());
        request.setDescription(dto.getDescription());
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

    private BloodRequestDto convertToDto(BloodRequest request) {
        BloodRequestDto dto = new BloodRequestDto();
        dto.setId(request.getId());
        dto.setBloodGroup(request.getBloodGroup());
        dto.setCity(request.getCity());
        dto.setHospitalName(request.getHospitalName());
        dto.setContactNumber(request.getContactNumber());
        dto.setUrgency(request.getUrgency());
        dto.setStatus(request.getStatus());
        dto.setDescription(request.getDescription());
        dto.setRequestDate(request.getRequestDate());
        dto.setRequesterUsername(request.getRequester().getUsername());
        return dto;
    }
}
