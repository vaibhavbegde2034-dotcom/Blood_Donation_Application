package com.blooddonation.controller;

import com.blooddonation.dto.StatsDto;
import com.blooddonation.repository.BloodBankRepository;
import com.blooddonation.repository.BloodRequestRepository;
import com.blooddonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private BloodBankRepository bloodBankRepository;

    @GetMapping("/status")
    public String getStatus() {
        return "Blood Donation System is running!";
    }

    @GetMapping("/stats")
    public StatsDto getHomeStats() {
        long totalDonors = userRepository.countByUserType("DONOR");
        long livesSaved = bloodRequestRepository.countByStatus("ACCEPTED");
        long activeRequests = bloodRequestRepository.countByStatus("PENDING");
        long totalBloodBanks = bloodBankRepository.count();
        
        return new StatsDto(totalDonors, livesSaved, activeRequests, totalBloodBanks);
    }
}
