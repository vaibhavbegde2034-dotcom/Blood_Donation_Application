package com.blooddonation.service;

import com.blooddonation.dto.UserRegistrationDto;
import com.blooddonation.dto.UserProfileDto;
import com.blooddonation.model.User;
import com.blooddonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        System.out.println("DEBUG: Checking if user exists: " + registrationDto.getUsername());
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        System.out.println("DEBUG: Encoding password for user: " + registrationDto.getUsername());
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRole("USER");
        
        System.out.println("DEBUG: Saving user to database...");
        User savedUser = userRepository.save(user);
        System.out.println("DEBUG: User saved successfully with ID: " + savedUser.getId());
        return savedUser;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User updateProfile(String username, UserProfileDto profileDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (profileDto.isAvailableToDonate() && !isEligibleToDonate(profileDto.getLastDonationDate())) {
            throw new RuntimeException("You are not eligible to donate. 90 days must pass since your last donation.");
        }

        user.setFullName(profileDto.getFullName());
        user.setBloodGroup(profileDto.getBloodGroup());
        user.setCity(profileDto.getCity());
        user.setLastDonationDate(profileDto.getLastDonationDate());
        user.setAvailableToDonate(profileDto.isAvailableToDonate());

        return userRepository.save(user);
    }

    public boolean isEligibleToDonate(LocalDate lastDonationDate) {
        if (lastDonationDate == null) return true;
        return ChronoUnit.DAYS.between(lastDonationDate, LocalDate.now()) >= 90;
    }

    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        Optional<User> user = userRepository.findByUsername(usernameOrEmail);
        if (user.isEmpty()) {
            user = userRepository.findByEmail(usernameOrEmail);
        }
        return user;
    }
}
