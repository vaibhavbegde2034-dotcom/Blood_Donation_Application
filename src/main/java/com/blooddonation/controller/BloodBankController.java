package com.blooddonation.controller;

import com.blooddonation.dto.ApiResponseDto;
import com.blooddonation.dto.BloodBankRegistrationDto;
import com.blooddonation.service.BloodBankService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bloodbank")
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
}
