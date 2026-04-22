package org.example.iws_websitesneaker.controller;

import org.example.iws_websitesneaker.Dto.LoginDTO;
import org.example.iws_websitesneaker.Dto.LoginResponse;
import org.example.iws_websitesneaker.Dto.UserDto;
import org.example.iws_websitesneaker.Dto.ApiResponse;
import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.example.iws_websitesneaker.Service.LoginService;
import org.example.iws_websitesneaker.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth") // Bá» /api
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

    @Autowired
    private LoginService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginDTO loginRequest,
            HttpServletRequest request) {

        try {
            System.out.println("=== LOGIN REQUEST ===");
            System.out.println("Email: " + loginRequest.getEmail());
            System.out.println("Password: " + loginRequest.getMatKhau());

            // Validate input
            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty() ||
                    loginRequest.getMatKhau() == null || loginRequest.getMatKhau().trim().isEmpty()) {

                System.out.println("Validation failed: Empty email or password");
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Email vÃ  password khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng", null));
            }

            // Authenticate user
            TaiKhoan user = authService.authenticate(loginRequest.getEmail(), loginRequest.getMatKhau());
            System.out.println("Authentication result: " + (user != null ? "SUCCESS" : "FAILED"));

            if (user == null) {
                System.out.println("User not found or password incorrect");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Email hoáº·c máº­t kháº©u khÃ´ng Ä‘Ãºng", null));
            }

            // Check if account is active
            if (user.getTrangThai() != 1) {
                System.out.println("Account is inactive. Status: " + user.getTrangThai());
                System.out.println("Raw database values:");
                System.out.println("- vai_tro column: " + user.getVaiTro());
                System.out.println("- trang_thai column: " + user.getTrangThai());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "TÃ i khoáº£n Ä‘Ã£ bá»‹ khÃ³a", null));
            }

            System.out.println("Login successful for user: " + user.getEmail());

            // Generate JWT token
            String token = jwtUtil.generateToken(user);

            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("token", token);
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            // Create response
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setUser(convertToUserDto(user));
            loginResponse.setExpiresIn(jwtUtil.getExpirationTime());
            System.out.println("Token usetr " + token);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "ÄÄƒng nháº­p thÃ nh cÃ´ng", loginResponse));

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "CÃ³ lá»—i xáº£y ra trong quÃ¡ trÃ¬nh Ä‘Äƒng nháº­p", null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            // Invalidate session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // Add token to blacklist if using JWT
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                jwtUtil.blacklistToken(token);
            }

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "ÄÄƒng xuáº¥t thÃ nh cÃ´ng", null));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "CÃ³ lá»—i xáº£y ra trong quÃ¡ trÃ¬nh Ä‘Äƒng xuáº¥t", null));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<UserDto>> verifyToken(
            HttpServletRequest request) {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Token khÃ´ng há»£p lá»‡", null));
            }

            String token = authHeader.substring(7);

            if (jwtUtil.isTokenExpired(token) || jwtUtil.isTokenBlacklisted(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Token Ä‘Ã£ háº¿t háº¡n", null));
            }

            String email = jwtUtil.extractEmail(token);
            TaiKhoan user = authService.findByEmail(email);

            if (user == null || user.getTrangThai() != 1) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "NgÆ°á»i dÃ¹ng khÃ´ng há»£p lá»‡", null));
            }

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Token há»£p lá»‡", convertToUserDto(user)));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Token khÃ´ng há»£p lá»‡", null));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            HttpServletRequest request) {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Token khÃ´ng há»£p lá»‡", null));
            }

            String oldToken = authHeader.substring(7);

            if (jwtUtil.isTokenBlacklisted(oldToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Token Ä‘Ã£ bá»‹ vÃ´ hiá»‡u hÃ³a", null));
            }

            String email = jwtUtil.extractEmail(oldToken);
            TaiKhoan user = authService.findByEmail(email);

            if (user == null || user.getTrangThai() != 1) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "NgÆ°á»i dÃ¹ng khÃ´ng há»£p lá»‡", null));
            }

            // Generate new token
            String newToken = jwtUtil.generateToken(user);

            // Blacklist old token
            jwtUtil.blacklistToken(oldToken);

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(newToken);
            loginResponse.setUser(convertToUserDto(user));
            loginResponse.setExpiresIn(jwtUtil.getExpirationTime());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Token Ä‘Ã£ Ä‘Æ°á»£c lÃ m má»›i", loginResponse));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "KhÃ´ng thá»ƒ lÃ m má»›i token", null));
        }
    }

    private UserDto convertToUserDto(TaiKhoan user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId().longValue());
        userDto.setEmail(user.getEmail());
        userDto.setVaiTro(user.getVaiTro().name());
        userDto.setMaTaiKhoan(user.getMaTaiKhoan()); // ThÃªm maTaiKhoan
        return userDto;
    }
}
