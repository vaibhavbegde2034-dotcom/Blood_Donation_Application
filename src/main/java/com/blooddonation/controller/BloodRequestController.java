package com.blooddonation.controller;

import com.blooddonation.dto.ApiResponseDto;
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

    @PostMapping(value = "/create/{username}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createRequest(@PathVariable String username, 
                                          @RequestParam("patientName") String patientName,
                                          @RequestParam("bloodGroup") String bloodGroup,
                                          @RequestParam("unitsRequired") Integer unitsRequired,
                                          @RequestParam("city") String city,
                                          @RequestParam("hospitalName") String hospitalName,
                                          @RequestParam("contactNumber") String contactNumber,
                                          @RequestParam("urgency") String urgency,
                                          @RequestParam(value = "description", required = false) String description,
                                          @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            BloodRequestDto dto = new BloodRequestDto();
            dto.setPatientName(patientName);
            dto.setBloodGroup(bloodGroup);
            dto.setUnitsRequired(unitsRequired);
            dto.setCity(city);
            dto.setHospitalName(hospitalName);
            dto.setContactNumber(contactNumber);
            dto.setUrgency(urgency);
            dto.setDescription(description);
            
            // NOTE: Here you would typically save the file and set the path
            // dto.setPrescriptionFilePath(fileService.save(file));
            dto.setPrescriptionFilePath("uploads/" + file.getOriginalFilename());

            bloodRequestService.createRequest(dto, username);
            return ResponseEntity.ok("Blood request created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
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

    @GetMapping("/city/{city}")
    public ResponseEntity<List<BloodRequestDto>> getRequestsByCity(@PathVariable String city) {
        return ResponseEntity.ok(bloodRequestService.getRequestsByCity(city));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BloodRequestDto> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(bloodRequestService.getRequestById(id));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponseDto> acceptRequest(@PathVariable Long id, @RequestParam Long bloodBankId) {
        return ResponseEntity.ok(bloodRequestService.acceptRequest(id, bloodBankId));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponseDto> rejectRequest(@PathVariable Long id) {
        return ResponseEntity.ok(bloodRequestService.rejectRequest(id));
    }

    @PutMapping("/{id}/verify-otp")
    public ResponseEntity<ApiResponseDto> verifyOtp(@PathVariable Long id,
                                                    @RequestParam String donorUsername,
                                                    @RequestParam String otp) {
        return ResponseEntity.ok(bloodRequestService.verifyOtp(id, donorUsername, otp));
    }

    @PutMapping("/{id}/confirm-donation")
    public ResponseEntity<ApiResponseDto> confirmDonation(@PathVariable Long id, @RequestParam String donorUsername) {
        return ResponseEntity.ok(bloodRequestService.confirmDonation(id, donorUsername));
    }

    @PutMapping("/{id}/accept-donor")
    public ResponseEntity<ApiResponseDto> acceptByDonor(@PathVariable Long id, @RequestParam String donorUsername) {
        return ResponseEntity.ok(bloodRequestService.acceptByDonor(id, donorUsername));
    }

    @PutMapping("/{id}/send-donor-request")
    public ResponseEntity<ApiResponseDto> sendDonorRequest(@PathVariable Long id, @RequestParam String donorUsername) {
        return ResponseEntity.ok(bloodRequestService.sendDonorRequest(id, donorUsername));
    }

    @PutMapping("/{id}/patient-accept")
    public ResponseEntity<ApiResponseDto> patientAcceptDonorRequest(@PathVariable Long id, @RequestParam String requesterUsername) {
        return ResponseEntity.ok(bloodRequestService.patientAcceptDonorRequest(id, requesterUsername));
    }
}
