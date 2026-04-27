package com.blooddonation.service;

import com.blooddonation.dto.ApiResponseDto;
import com.blooddonation.dto.BloodBankRegistrationDto;
import com.blooddonation.model.BloodBank;
import com.blooddonation.repository.BloodBankRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BloodBankService {

    private final BloodBankRepository bloodBankRepository;
    private final PasswordEncoder passwordEncoder;

    public BloodBankService(BloodBankRepository bloodBankRepository, PasswordEncoder passwordEncoder) {
        this.bloodBankRepository = bloodBankRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ApiResponseDto registerBloodBank(BloodBankRegistrationDto dto) {
        if (bloodBankRepository.existsByEmail(dto.getEmail())) {
            return new ApiResponseDto("Email is already registered!", false);
        }

        BloodBank bloodBank = new BloodBank();
        bloodBank.setBankName(dto.getBankName());
        bloodBank.setLocation(dto.getLocation());
        bloodBank.setContactNumber(dto.getContactNumber());
        bloodBank.setEmail(dto.getEmail());
        bloodBank.setPassword(passwordEncoder.encode(dto.getPassword()));

        bloodBankRepository.save(bloodBank);
        return new ApiResponseDto("Blood Bank registered successfully", true);
    }
}
