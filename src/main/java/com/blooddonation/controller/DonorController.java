package com.blooddonation.controller;

import com.blooddonation.dto.UserProfileDto;
import com.blooddonation.model.User;
import com.blooddonation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/donors")
public class DonorController {

    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public ResponseEntity<List<UserProfileDto>> searchDonors(
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String city) {
        
        List<User> donors = userService.searchDonors(bloodGroup, city);
        
        List<UserProfileDto> dtos = donors.stream().map(user -> {
            UserProfileDto dto = new UserProfileDto();
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setFullName(user.getFullName());
            dto.setBloodGroup(user.getBloodGroup());
            dto.setCity(user.getCity());
            dto.setLastDonationDate(user.getLastDonationDate());
            dto.setAvailableToDonate(user.isAvailableToDonate());
            // Don't send contact number in search results for privacy
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<UserProfileDto> getDonorProfile(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(user -> {
                    UserProfileDto dto = new UserProfileDto();
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setFullName(user.getFullName());
                    dto.setBloodGroup(user.getBloodGroup());
                    dto.setCity(user.getCity());
                    dto.setContactNumber(user.getContactNumber());
                    dto.setLastDonationDate(user.getLastDonationDate());
                    dto.setAvailableToDonate(user.isAvailableToDonate());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
