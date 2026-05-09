package com.blooddonation.service;

import com.blooddonation.dto.ApiResponseDto;
import com.blooddonation.dto.BloodBankRegistrationDto;
import com.blooddonation.model.BloodBank;
import com.blooddonation.repository.BloodBankRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

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
        bloodBank.setCity(dto.getCity());
        bloodBank.setContactNumber(dto.getContactNumber());
        bloodBank.setEmail(dto.getEmail());
        bloodBank.setPassword(passwordEncoder.encode(dto.getPassword()));

        bloodBankRepository.save(bloodBank);
        return new ApiResponseDto("Blood Bank registered successfully", true);
    }

    public Optional<BloodBank> findByEmail(String email) {
        return bloodBankRepository.findByEmail(email);
    }

    public Optional<BloodBank> findById(Long id) {
        return bloodBankRepository.findById(id);
    }

    public ApiResponseDto loginBloodBank(String email, String password) {
        return bloodBankRepository.findByEmail(email)
                .map(bank -> {
                    if (passwordEncoder.matches(password, bank.getPassword())) {
                        return new ApiResponseDto("Login successful", true);
                    } else {
                        return new ApiResponseDto("Invalid password", false);
                    }
                })
                .orElse(new ApiResponseDto("Blood Bank not found", false));
    }

    public ApiResponseDto updateBloodBank(Long id, BloodBankRegistrationDto dto) {
        return bloodBankRepository.findById(id)
                .map(bank -> {
                    bank.setBankName(dto.getBankName());
                    bank.setLocation(dto.getLocation());
                    bank.setCity(dto.getCity());
                    bank.setContactNumber(dto.getContactNumber());
                    // Password update logic could be added here if needed, but let's keep it simple for now
                    if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                        bank.setPassword(passwordEncoder.encode(dto.getPassword()));
                    }
                    bloodBankRepository.save(bank);
                    return new ApiResponseDto("Profile updated successfully", true);
                })
                .orElse(new ApiResponseDto("Blood Bank not found", false));
    }

    public java.util.List<BloodBank> getAllBloodBanks() {
        return bloodBankRepository.findAll();
    }

    public java.util.List<BloodBank> getBloodBanksByCity(String city) {
        return bloodBankRepository.findByCityContainingIgnoreCase(city);
    }
}
