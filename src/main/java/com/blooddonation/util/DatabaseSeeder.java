package com.blooddonation.util;

import com.blooddonation.dto.BloodRequestDto;
import com.blooddonation.dto.UserRegistrationDto;
import com.blooddonation.model.BloodBank;
import com.blooddonation.model.BloodStock;
import com.blooddonation.repository.BloodBankRepository;
import com.blooddonation.repository.BloodStockRepository;
import com.blooddonation.repository.UserRepository;
import com.blooddonation.service.BloodRequestService;
import com.blooddonation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private BloodRequestService bloodRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BloodBankRepository bloodBankRepository;

    @Autowired
    private BloodStockRepository bloodStockRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Backfill older seed data (when email/userType were not set)
        userRepository.findAll().forEach(existing -> {
            boolean changed = false;
            if (existing.getEmail() == null && existing.getUsername() != null && existing.getUsername().contains("@")) {
                existing.setEmail(existing.getUsername());
                changed = true;
            }
            if (existing.getUserType() == null || existing.getUserType().isBlank()) {
                existing.setUserType("DONOR");
                changed = true;
            }
            if (changed) {
                userRepository.save(existing);
            }
        });

        // Create 10 Users
        for (int i = 1; i <= 10; i++) {
            UserRegistrationDto user = new UserRegistrationDto();
            String email = "user" + i + "@test.com";
            user.setUsername(email);
            user.setEmail(email);
            user.setPassword("password123");
            user.setFullName("User " + i);
            user.setBloodGroup(Arrays.asList("A+", "B+", "O+", "AB+").get(i % 4));
            user.setCity("Pune");
            user.setContactNumber("999990000" + i);
            user.setUserType(i % 2 == 0 ? "DONOR" : "REQUESTER");
            try {
                userService.registerUser(user);
            } catch (Exception ignored) {}
        }

        // Create 3 Blood Banks + seed some stock
        for (int i = 1; i <= 3; i++) {
            String bankEmail = "bank" + i + "@test.com";
            if (bloodBankRepository.existsByEmail(bankEmail)) {
                continue;
            }

            BloodBank bank = new BloodBank();
            bank.setBankName("Test Blood Bank " + i);
            bank.setEmail(bankEmail);
            bank.setPassword(passwordEncoder.encode("password123"));
            bank.setCity("Pune");
            bank.setLocation("Pune, MH");
            bank.setContactNumber("888880000" + i);
            bloodBankRepository.save(bank);

            for (String group : Arrays.asList("A+", "B+", "O+", "AB+")) {
                BloodStock stock = new BloodStock(bank, group, 5 + i);
                bloodStockRepository.save(stock);
            }
        }

        // Create 10 Blood Requests
        for (int i = 1; i <= 10; i++) {
            BloodRequestDto req = new BloodRequestDto();
            req.setPatientName("Patient " + i);
            req.setBloodGroup(Arrays.asList("A+", "B+", "O+", "AB+").get(i % 4));
            req.setUnitsRequired(1);
            req.setCity("Pune");
            req.setHospitalName("City Hospital " + (i % 3));
            req.setContactNumber("999990000" + i);
            req.setUrgency(i % 2 == 0 ? "URGENT" : "NORMAL");
            req.setPrescriptionFilePath("uploads/dummy.pdf");
            
            try {
                bloodRequestService.createRequest(req, "user" + i + "@test.com");
            } catch (Exception ignored) {}
        }
    }
}
