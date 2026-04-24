package com.blooddonation.controller;

import com.blooddonation.dto.UserProfileDto;
import com.blooddonation.model.User;
import com.blooddonation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile/{username}")
    public ResponseEntity<?> getProfile(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(user -> {
                    UserProfileDto dto = new UserProfileDto();
                    dto.setFullName(user.getFullName());
                    dto.setBloodGroup(user.getBloodGroup());
                    dto.setCity(user.getCity());
                    dto.setLastDonationDate(user.getLastDonationDate());
                    dto.setAvailableToDonate(user.isAvailableToDonate());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile/{username}")
    public ResponseEntity<?> updateProfile(@PathVariable String username, @RequestBody UserProfileDto profileDto) {
        try {
            User updatedUser = userService.updateProfile(username, profileDto);
            return ResponseEntity.ok("Profile updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/availability/{username}")
    public ResponseEntity<?> updateAvailability(@PathVariable String username, @RequestBody java.util.Map<String, Boolean> payload) {
        try {
            boolean isAvailable = payload.get("isAvailable");
            User user = userService.updateAvailability(username, isAvailable);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
