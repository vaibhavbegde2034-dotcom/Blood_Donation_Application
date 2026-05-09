package com.blooddonation.dto;

import java.time.LocalDate;

public class UserProfileDto {
    private String username;
    private String fullName;
    private String bloodGroup;
    private String city;
    private String contactNumber;
    private LocalDate lastDonationDate;
    private boolean availableToDonate;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(LocalDate lastDonationDate) { this.lastDonationDate = lastDonationDate; }

    public boolean isAvailableToDonate() { return availableToDonate; }
    public void setAvailableToDonate(boolean availableToDonate) { this.availableToDonate = availableToDonate; }
}
