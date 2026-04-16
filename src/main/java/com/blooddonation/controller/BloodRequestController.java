package com.blooddonation.controller;

import com.blooddonation.dto.BloodRequestDto;
import com.blooddonation.service.BloodRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blood-requests")
public class BloodRequestController {

    @Autowired
    private BloodRequestService bloodRequestService;

    @PostMapping("/create/{username}")
    public ResponseEntity<?> createRequest(@PathVariable String username, @RequestBody BloodRequestDto dto) {
        try {
            bloodRequestService.createRequest(dto, username);
            return ResponseEntity.ok("Blood request created successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-requests/{username}")
    public ResponseEntity<List<BloodRequestDto>> getMyRequests(@PathVariable String username) {
        return ResponseEntity.ok(bloodRequestService.getMyRequests(username));
    }

    @GetMapping("/active")
    public ResponseEntity<List<BloodRequestDto>> getAllActiveRequests() {
        return ResponseEntity.ok(bloodRequestService.getAllActiveRequests());
    }
}
