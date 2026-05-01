package com.blooddonation.controller;

import com.blooddonation.dto.BloodStockDto;
import com.blooddonation.service.BloodStockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blood-stock")
public class BloodStockController {

    private final BloodStockService bloodStockService;

    public BloodStockController(BloodStockService bloodStockService) {
        this.bloodStockService = bloodStockService;
    }

    @GetMapping("/bank/{bankId}")
    public ResponseEntity<List<BloodStockDto>> getStockByBank(@PathVariable Long bankId) {
        return ResponseEntity.ok(bloodStockService.getStockByBank(bankId));
    }

    @PostMapping("/bank/{bankId}/update")
    public ResponseEntity<BloodStockDto> updateStock(@PathVariable Long bankId, @Valid @RequestBody BloodStockDto stockDto) {
        return ResponseEntity.ok(bloodStockService.updateStock(bankId, stockDto));
    }

    @GetMapping("/search")
    public ResponseEntity<List<BloodStockDto>> searchByBloodGroup(
            @RequestParam String bloodGroup,
            @RequestParam(required = false) String city) {
        return ResponseEntity.ok(bloodStockService.findBanksByBloodGroup(bloodGroup, city));
    }
}
