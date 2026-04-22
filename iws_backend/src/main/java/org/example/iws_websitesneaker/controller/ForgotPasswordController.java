package org.example.iws_websitesneaker.controller;

import org.example.iws_websitesneaker.Dto.ApiResponse;
import org.example.iws_websitesneaker.Service.TaiKhoanService;
import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth/forgot-password")
@CrossOrigin(origins = "http://localhost:5173")
public class ForgotPasswordController {

    @Autowired
    private TaiKhoanService taiKhoanService;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    // LÆ°u OTP trong memory (production nÃªn dÃ¹ng Redis)
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, Long> otpExpiry = new ConcurrentHashMap<>();

    /**
     * Gá»­i OTP vá» email
     */
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            System.out.println("=== FORGOT PASSWORD - SEND CODE ===");
            System.out.println("Email: " + email);

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Email khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng", null));
            }

            // TÃ¬m tÃ i khoáº£n trong database
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);

            if (taiKhoanOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Email khÃ´ng tá»“n táº¡i trong há»‡ thá»‘ng", null));
            }

            TaiKhoan taiKhoan = taiKhoanOpt.get();

            if (!taiKhoan.isActive()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "TÃ i khoáº£n Ä‘Ã£ bá»‹ khÃ³a", null));
            }

            // Táº¡o OTP 6 sá»‘
            String otp = String.format("%06d", new Random().nextInt(1000000));

            // LÆ°u OTP trong memory (15 phÃºt)
            String emailKey = email.toLowerCase().trim();
            otpStorage.put(emailKey, otp);
            otpExpiry.put(emailKey, System.currentTimeMillis() + 15 * 60 * 1000);

            // Gá»­i email hoáº·c in console
            boolean emailSent = sendOtpEmail(email, otp, taiKhoan);

            Map<String, Object> response = new HashMap<>();
            response.put("email", maskEmail(email));
            response.put("maTaiKhoan", taiKhoan.getMaTaiKhoan());
            response.put("expiresInMinutes", 15);

            if (emailSent) {
                return ResponseEntity.ok(new ApiResponse<>(true, "MÃ£ OTP Ä‘Ã£ Ä‘Æ°á»£c gá»­i Ä‘áº¿n email cá»§a báº¡n", response));
            } else {
                response.put("demo_otp", otp); // Äá»ƒ test khi khÃ´ng cÃ³ email config
                return ResponseEntity.ok(new ApiResponse<>(true, "MÃ£ OTP Ä‘Ã£ Ä‘Æ°á»£c táº¡o (kiá»ƒm tra console)", response));
            }

        } catch (Exception e) {
            System.err.println("Error sending OTP: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "Lá»—i gá»­i OTP", null));
        }
    }

    /**
     * XÃ¡c thá»±c OTP vÃ  Ä‘á»•i máº­t kháº©u
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String inputOtp = request.get("code");
            String newPassword = request.get("newPassword");

            System.out.println("=== RESET PASSWORD ===");
            System.out.println("Email: " + email);
            System.out.println("OTP: " + inputOtp);

            if (email == null || inputOtp == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Thiáº¿u thÃ´ng tin báº¯t buá»™c", null));
            }

            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Máº­t kháº©u pháº£i cÃ³ Ã­t nháº¥t 6 kÃ½ tá»±", null));
            }

            String emailKey = email.toLowerCase().trim();

            // Kiá»ƒm tra OTP
            String storedOtp = otpStorage.get(emailKey);
            Long expiry = otpExpiry.get(emailKey);

            if (storedOtp == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "KhÃ´ng tÃ¬m tháº¥y OTP. Vui lÃ²ng yÃªu cáº§u mÃ£ má»›i", null));
            }

            if (!storedOtp.equals(inputOtp.trim())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "MÃ£ OTP khÃ´ng Ä‘Ãºng", null));
            }

            if (expiry == null || System.currentTimeMillis() > expiry) {
                otpStorage.remove(emailKey);
                otpExpiry.remove(emailKey);
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "MÃ£ OTP Ä‘Ã£ háº¿t háº¡n", null));
            }

            // Cáº­p nháº­t máº­t kháº©u
            boolean updated = taiKhoanService.updatePassword(emailKey, newPassword);

            if (updated) {
                // XÃ³a OTP sau khi thÃ nh cÃ´ng
                otpStorage.remove(emailKey);
                otpExpiry.remove(emailKey);

                // Gá»­i email thÃ´ng bÃ¡o thÃ nh cÃ´ng (náº¿u cÃ³ config email)
                Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(emailKey);
                if (taiKhoanOpt.isPresent()) {
                    sendSuccessNotification(emailKey, taiKhoanOpt.get());
                }

                Map<String, Object> response = new HashMap<>();
                response.put("email", maskEmail(email));
                response.put("success", true);

                return ResponseEntity.ok(new ApiResponse<>(true, "Äá»•i máº­t kháº©u thÃ nh cÃ´ng!", response));
            } else {
                return ResponseEntity.status(500)
                        .body(new ApiResponse<>(false, "Lá»—i cáº­p nháº­t máº­t kháº©u", null));
            }

        } catch (Exception e) {
            System.err.println("Error reset password: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "Lá»—i há»‡ thá»‘ng", null));
        }
    }

    /**
     * Gá»­i OTP qua email hoáº·c in console
     */
    private boolean sendOtpEmail(String toEmail, String otp, TaiKhoan taiKhoan) {
        // Náº¿u cÃ³ cáº¥u hÃ¬nh email, gá»­i tháº­t
        if (mailSender != null && !mailUsername.isEmpty()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("ShoesBees - MÃ£ xÃ¡c thá»±c Ä‘áº·t láº¡i máº­t kháº©u");
                message.setText(buildOtpEmailContent(otp, taiKhoan));
                message.setFrom(mailUsername);

                mailSender.send(message);
                System.out.println("âœ… Email sent successfully to: " + toEmail);
                return true;

            } catch (Exception e) {
                System.err.println("âŒ Failed to send email: " + e.getMessage());
                // Fallback to console
                printOtpToConsole(toEmail, otp, taiKhoan);
                return false;
            }
        } else {
            // KhÃ´ng cÃ³ config email, in ra console
            printOtpToConsole(toEmail, otp, taiKhoan);
            return false;
        }
    }

    /**
     * In OTP ra console (fallback khi khÃ´ng cÃ³ email config)
     */
    private void printOtpToConsole(String email, String otp, TaiKhoan taiKhoan) {
        System.out.println("======= QUÃŠN Máº¬T KHáº¨U - OTP =======");
        System.out.println("TÃ i khoáº£n: " + taiKhoan.getMaTaiKhoan());
        System.out.println("Email: " + email);
        System.out.println("Vai trÃ²: " + taiKhoan.getRoleDisplayName());
        System.out.println("MÃƒ OTP: " + otp);
        System.out.println("Háº¿t háº¡n sau: 15 phÃºt");
        System.out.println("==================================");
    }

    /**
     * Gá»­i thÃ´ng bÃ¡o Ä‘á»•i máº­t kháº©u thÃ nh cÃ´ng
     */
    private void sendSuccessNotification(String toEmail, TaiKhoan taiKhoan) {
        if (mailSender != null && !mailUsername.isEmpty()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("ShoesBees - Máº­t kháº©u Ä‘Ã£ Ä‘Æ°á»£c Ä‘áº·t láº¡i");
                message.setText(buildSuccessEmailContent(taiKhoan));
                message.setFrom(mailUsername);

                mailSender.send(message);
                System.out.println("âœ… Success notification sent to: " + toEmail);

            } catch (Exception e) {
                System.err.println("âŒ Failed to send success notification: " + e.getMessage());
            }
        }
    }

    /**
     * Ná»™i dung email OTP
     */
    private String buildOtpEmailContent(String otp, TaiKhoan taiKhoan) {
        return "Xin chÃ o,\n\n" +
                "Báº¡n Ä‘Ã£ yÃªu cáº§u Ä‘áº·t láº¡i máº­t kháº©u cho tÃ i khoáº£n ShoesBees (" + taiKhoan.getMaTaiKhoan() + ").\n\n" +
                "MÃ£ xÃ¡c thá»±c OTP cá»§a báº¡n lÃ : " + otp + "\n\n" +
                "MÃ£ nÃ y cÃ³ hiá»‡u lá»±c trong vÃ²ng 15 phÃºt.\n\n" +
                "Náº¿u báº¡n khÃ´ng thá»±c hiá»‡n yÃªu cáº§u nÃ y, vui lÃ²ng bá» qua email nÃ y.\n\n" +
                "TrÃ¢n trá»ng,\n" +
                "Äá»™i ngÅ© ShoesBees";
    }

    /**
     * Ná»™i dung email thÃ´ng bÃ¡o thÃ nh cÃ´ng
     */
    private String buildSuccessEmailContent(TaiKhoan taiKhoan) {
        return "Xin chÃ o,\n\n" +
                "Máº­t kháº©u cho tÃ i khoáº£n ShoesBees (" + taiKhoan.getMaTaiKhoan() + ") Ä‘Ã£ Ä‘Æ°á»£c Ä‘áº·t láº¡i thÃ nh cÃ´ng.\n\n" +
                "Thá»i gian: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + "\n\n" +
                "Náº¿u báº¡n khÃ´ng thá»±c hiá»‡n thao tÃ¡c nÃ y, vui lÃ²ng liÃªn há»‡ vá»›i chÃºng tÃ´i ngay láº­p tá»©c.\n\n" +
                "TrÃ¢n trá»ng,\n" +
                "Äá»™i ngÅ© ShoesBees";
    }

    /**
     * áº¨n email Ä‘á»ƒ báº£o máº­t
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 5) return email;

        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return email;

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (username.length() <= 2) return email;

        return username.charAt(0) + "***" + username.charAt(username.length() - 1) + domain;
    }
}
