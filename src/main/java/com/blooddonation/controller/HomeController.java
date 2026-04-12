package com.blooddonation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/api/status")
    public String getStatus() {
        return "Blood Donation System is running!";
    }
}
