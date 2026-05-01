package org.example.iws_websitesneaker.controller;

import org.example.iws_websitesneaker.Dto.ApiResponse;
import org.example.iws_websitesneaker.Service.TaiKhoanService;
import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth/forgot-password")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class ForgotPasswordController {

    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    @Autowired
    private TaiKhoanService taiKhoanService;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${forgot.password.allow-demo:false}")
    private boolean allowDemoOtp;

    @Value("${forgot.password.otp.expiry.minutes:15}")
    private long otpExpiryMinutes;

    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, Long> otpExpiry = new ConcurrentHashMap<>();

    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            System.out.println("=== FORGOT PASSWORD - SEND CODE ===");
            System.out.println("Email: " + email);

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Email is required", null));
            }

            String emailKey = email.toLowerCase().trim();
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(emailKey);
            if (taiKhoanOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Email does not exist in the system", null));
            }

            TaiKhoan taiKhoan = taiKhoanOpt.get();
            if (!taiKhoan.isActive()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Account is locked", null));
            }

            String otp = String.format("%06d", OTP_RANDOM.nextInt(1_000_000));
            otpStorage.put(emailKey, otp);
            otpExpiry.put(emailKey, System.currentTimeMillis() + otpExpiryMinutes * 60 * 1000L);

            boolean emailSent = sendOtpEmail(emailKey, otp, taiKhoan);

            Map<String, Object> response = new HashMap<>();
            response.put("email", maskEmail(emailKey));
            response.put("maTaiKhoan", taiKhoan.getMaTaiKhoan());
            response.put("expiresInMinutes", otpExpiryMinutes);

            if (emailSent) {
                return ResponseEntity.ok(new ApiResponse<>(true, "OTP sent to your email", response));
            }

            if (allowDemoOtp) {
                response.put("demo_otp", otp);
                return ResponseEntity.ok(new ApiResponse<>(true, "OTP generated for local demo", response));
            }

            otpStorage.remove(emailKey);
            otpExpiry.remove(emailKey);
            return ResponseEntity.status(503)
                    .body(new ApiResponse<>(false, "Email OTP is not configured. Please set MAIL_USERNAME and MAIL_PASSWORD.", null));

        } catch (Exception e) {
            System.err.println("Error sending OTP: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "Failed to send OTP", null));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String inputOtp = request.get("code");
            String newPassword = request.get("newPassword");

            System.out.println("=== RESET PASSWORD ===");
            System.out.println("Email: " + email);
            System.out.println("OTP received");

            if (email == null || inputOtp == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Missing required fields", null));
            }

            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Password must be at least 6 characters", null));
            }

            String emailKey = email.toLowerCase().trim();
            String storedOtp = otpStorage.get(emailKey);
            Long expiry = otpExpiry.get(emailKey);

            if (storedOtp == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "OTP not found. Please request a new code", null));
            }

            if (!storedOtp.equals(inputOtp.trim())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid OTP code", null));
            }

            if (expiry == null || System.currentTimeMillis() > expiry) {
                otpStorage.remove(emailKey);
                otpExpiry.remove(emailKey);
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "OTP code has expired", null));
            }

            boolean updated = taiKhoanService.updatePassword(emailKey, newPassword);
            if (!updated) {
                return ResponseEntity.status(500)
                        .body(new ApiResponse<>(false, "Failed to update password", null));
            }

            otpStorage.remove(emailKey);
            otpExpiry.remove(emailKey);

            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(emailKey);
            taiKhoanOpt.ifPresent(taiKhoan -> sendSuccessNotification(emailKey, taiKhoan));

            Map<String, Object> response = new HashMap<>();
            response.put("email", maskEmail(email));
            response.put("success", true);

            return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully", response));

        } catch (Exception e) {
            System.err.println("Error reset password: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "System error", null));
        }
    }

    private boolean sendOtpEmail(String toEmail, String otp, TaiKhoan taiKhoan) {
        if (isMailConfigured()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("ShoesBees - Password reset OTP");
                message.setText(buildOtpEmailContent(otp, taiKhoan));
                message.setFrom(mailUsername);

                mailSender.send(message);
                System.out.println("OTP email sent to: " + toEmail);
                return true;
            } catch (Exception e) {
                System.err.println("Failed to send OTP email: " + e.getMessage());
            }
        }

        if (allowDemoOtp) {
            printOtpToConsole(toEmail, otp, taiKhoan);
        }
        return false;
    }

    private void printOtpToConsole(String email, String otp, TaiKhoan taiKhoan) {
        System.out.println("======= FORGOT PASSWORD OTP =======");
        System.out.println("Account: " + taiKhoan.getMaTaiKhoan());
        System.out.println("Email: " + email);
        System.out.println("Role: " + taiKhoan.getRoleDisplayName());
        System.out.println("OTP: " + otp);
        System.out.println("Expires in: 15 minutes");
        System.out.println("===================================");
    }

    private void sendSuccessNotification(String toEmail, TaiKhoan taiKhoan) {
        if (isMailConfigured()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("ShoesBees - Password reset successful");
                message.setText(buildSuccessEmailContent(taiKhoan));
                message.setFrom(mailUsername);

                mailSender.send(message);
                System.out.println("Password reset confirmation sent to: " + toEmail);
            } catch (Exception e) {
                System.err.println("Failed to send success notification: " + e.getMessage());
            }
        }
    }

    private boolean isMailConfigured() {
        return mailSender != null && mailUsername != null && !mailUsername.trim().isEmpty();
    }

    private String buildOtpEmailContent(String otp, TaiKhoan taiKhoan) {
        return "Hello,\n\n"
                + "You requested a password reset for ShoesBees account " + taiKhoan.getMaTaiKhoan() + ".\n\n"
                + "Your OTP code is: " + otp + "\n\n"
                + "This code expires in 15 minutes.\n\n"
                + "If you did not request this action, you can ignore this email.\n\n"
                + "ShoesBees Team";
    }

    private String buildSuccessEmailContent(TaiKhoan taiKhoan) {
        return "Hello,\n\n"
                + "The password for ShoesBees account " + taiKhoan.getMaTaiKhoan() + " was reset successfully.\n\n"
                + "Time: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + "\n\n"
                + "If this was not you, please contact support immediately.\n\n"
                + "ShoesBees Team";
    }

    private String maskEmail(String email) {
        if (email == null || email.length() < 5) {
            return email;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (username.length() <= 2) {
            return email;
        }

        return username.charAt(0) + "***" + username.charAt(username.length() - 1) + domain;
    }
}
