package com.blooddonation.controller;

import com.blooddonation.dto.ApiResponseDto;
import com.blooddonation.dto.BloodBankRegistrationDto;
import com.blooddonation.service.BloodBankService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import com.blooddonation.model.BloodBank;
import com.blooddonation.dto.LoginDto;

@RestController
@RequestMapping("/api/bloodbank")
public class BloodBankController {

    private final BloodBankService bloodBankService;

    public BloodBankController(BloodBankService bloodBankService) {
        this.bloodBankService = bloodBankService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto> registerBloodBank(@Valid @RequestBody BloodBankRegistrationDto dto) {
        ApiResponseDto response = bloodBankService.registerBloodBank(dto);
        if (response.isStatus()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginBloodBank(@Valid @RequestBody LoginDto loginDto) {
        ApiResponseDto responseDto = bloodBankService.loginBloodBank(loginDto.getUsernameOrEmail(), loginDto.getPassword());
        
        if (responseDto.isStatus()) {
            BloodBank bank = bloodBankService.findByEmail(loginDto.getUsernameOrEmail()).get();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("bankName", bank.getBankName());
            response.put("email", bank.getEmail());
            response.put("location", bank.getLocation());
            response.put("city", bank.getCity());
            response.put("contactNumber", bank.getContactNumber());
            response.put("id", bank.getId().toString());
            response.put("userType", "BLOOD_BANK");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(responseDto.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto> updateBloodBank(@PathVariable Long id, @Valid @RequestBody BloodBankRegistrationDto dto) {
        ApiResponseDto response = bloodBankService.updateBloodBank(id, dto);
        if (response.isStatus()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBloodBank(@PathVariable Long id) {
        return bloodBankService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<java.util.List<BloodBank>> getAllBloodBanks(@RequestParam(required = false) String city) {
        if (city != null && !city.isEmpty()) {
            return ResponseEntity.ok(bloodBankService.getBloodBanksByCity(city));
        }
        return ResponseEntity.ok(bloodBankService.getAllBloodBanks());
    }
}
