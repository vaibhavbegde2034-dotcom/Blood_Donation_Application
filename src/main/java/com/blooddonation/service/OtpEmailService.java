package com.blooddonation.service;

import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String fromAddress;

    public OtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendDonationOtp(User requester, BloodRequest request, String otp) {
        if (requester.getEmail() == null || requester.getEmail().isBlank()) {
            throw new RuntimeException("Requester email is not available for OTP delivery");
        }
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new RuntimeException("SMTP sender email is not configured");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(requester.getEmail());
        message.setSubject("BloodCare Donation Verification OTP");
        message.setText(buildOtpMessage(requester, request, otp));
        mailSender.send(message);
    }

    private String buildOtpMessage(User requester, BloodRequest request, String otp) {
        String requesterName = requester.getFullName() != null && !requester.getFullName().isBlank()
                ? requester.getFullName()
                : requester.getUsername();

        return "Hello " + requesterName + ",\n\n"
                + "Your donor has arrived for the blood donation request.\n"
                + "Please share this OTP with the donor at the hospital to complete verification.\n\n"
                + "Patient: " + request.getPatientName() + "\n"
                + "Blood Group: " + request.getBloodGroup() + "\n"
                + "Hospital: " + request.getHospitalName() + "\n"
                + "OTP: " + otp + "\n\n"
                + "If you did not expect this message, please contact BloodCare support.\n\n"
                + "BloodCare";
    }
}
