package com.blooddonation.dto;

import java.time.LocalDateTime;

public class BloodRequestDto {

    private Long id;
    private String patientName;
    private String bloodGroup;
    private Integer unitsRequired;
    private String city;
    private String hospitalName;
    private String contactNumber;
    private String urgency;
    private String status;
    private String description;
    private String prescriptionFilePath;
    private LocalDateTime requestDate;
    private String requesterUsername;
    private String requesterName;
    private Long acceptedBloodBankId;
    private String acceptedBloodBankName;
    private String acceptedDonorUsername;
    private String acceptedDonorName;
    private Boolean otpVerified;
    private Boolean patientAcceptedDonorRequest;
    private LocalDateTime otpSentAt;
    private LocalDateTime otpVerifiedAt;
    private LocalDateTime donationCompletedAt;
    private LocalDateTime donorRequestSentAt;
    private LocalDateTime patientAcceptedAt;

    public BloodRequestDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public Integer getUnitsRequired() { return unitsRequired; }
    public void setUnitsRequired(Integer unitsRequired) { this.unitsRequired = unitsRequired; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPrescriptionFilePath() { return prescriptionFilePath; }
    public void setPrescriptionFilePath(String prescriptionFilePath) { this.prescriptionFilePath = prescriptionFilePath; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public String getRequesterUsername() { return requesterUsername; }
    public void setRequesterUsername(String requesterUsername) { this.requesterUsername = requesterUsername; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public Long getAcceptedBloodBankId() { return acceptedBloodBankId; }
    public void setAcceptedBloodBankId(Long acceptedBloodBankId) { this.acceptedBloodBankId = acceptedBloodBankId; }

    public String getAcceptedBloodBankName() { return acceptedBloodBankName; }
    public void setAcceptedBloodBankName(String acceptedBloodBankName) { this.acceptedBloodBankName = acceptedBloodBankName; }

    public String getAcceptedDonorUsername() { return acceptedDonorUsername; }
    public void setAcceptedDonorUsername(String acceptedDonorUsername) { this.acceptedDonorUsername = acceptedDonorUsername; }

    public String getAcceptedDonorName() { return acceptedDonorName; }
    public void setAcceptedDonorName(String acceptedDonorName) { this.acceptedDonorName = acceptedDonorName; }

    public Boolean getOtpVerified() { return otpVerified; }
    public void setOtpVerified(Boolean otpVerified) { this.otpVerified = otpVerified; }

    public Boolean getPatientAcceptedDonorRequest() { return patientAcceptedDonorRequest; }
    public void setPatientAcceptedDonorRequest(Boolean patientAcceptedDonorRequest) { this.patientAcceptedDonorRequest = patientAcceptedDonorRequest; }

    public LocalDateTime getOtpSentAt() { return otpSentAt; }
    public void setOtpSentAt(LocalDateTime otpSentAt) { this.otpSentAt = otpSentAt; }

    public LocalDateTime getOtpVerifiedAt() { return otpVerifiedAt; }
    public void setOtpVerifiedAt(LocalDateTime otpVerifiedAt) { this.otpVerifiedAt = otpVerifiedAt; }

    public LocalDateTime getDonationCompletedAt() { return donationCompletedAt; }
    public void setDonationCompletedAt(LocalDateTime donationCompletedAt) { this.donationCompletedAt = donationCompletedAt; }

    public LocalDateTime getDonorRequestSentAt() { return donorRequestSentAt; }
    public void setDonorRequestSentAt(LocalDateTime donorRequestSentAt) { this.donorRequestSentAt = donorRequestSentAt; }

    public LocalDateTime getPatientAcceptedAt() { return patientAcceptedAt; }
    public void setPatientAcceptedAt(LocalDateTime patientAcceptedAt) { this.patientAcceptedAt = patientAcceptedAt; }
}
