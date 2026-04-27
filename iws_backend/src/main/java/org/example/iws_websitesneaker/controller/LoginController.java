package org.example.iws_websitesneaker.controller;

import org.example.iws_websitesneaker.Dto.ApiResponse;
import org.example.iws_websitesneaker.Dto.LoginDTO;
import org.example.iws_websitesneaker.Dto.LoginResponse;
import org.example.iws_websitesneaker.Dto.UserDto;
import org.example.iws_websitesneaker.Service.LoginService;
import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.example.iws_websitesneaker.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
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
            System.out.println("Login request received");

            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()
                    || loginRequest.getMatKhau() == null || loginRequest.getMatKhau().trim().isEmpty()) {

                System.out.println("Login request rejected: empty credentials");
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Email and password are required", null));
            }

            TaiKhoan user = authService.authenticate(loginRequest.getEmail(), loginRequest.getMatKhau());

            if (user == null) {
                System.out.println("Login failed: invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Invalid email or password", null));
            }

            if (user.getTrangThai() != 1) {
                System.out.println("Login blocked: inactive account id=" + user.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "Account is locked", null));
            }

            System.out.println("Login successful for account id=" + user.getId());

            String token = jwtUtil.generateToken(user);

            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("token", token);
            session.setMaxInactiveInterval(30 * 60);

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setUser(convertToUserDto(user));
            loginResponse.setExpiresIn(jwtUtil.getExpirationTime());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Login successful", loginResponse));

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred during login", null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                jwtUtil.blacklistToken(token);
            }

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Logout successful", null));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred during logout", null));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<UserDto>> verifyToken(
            HttpServletRequest request) {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Invalid token", null));
            }

            String token = authHeader.substring(7);

            if (jwtUtil.isTokenExpired(token) || jwtUtil.isTokenBlacklisted(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Token has expired", null));
            }

            String email = jwtUtil.extractEmail(token);
            TaiKhoan user = authService.findByEmail(email);

            if (user == null || user.getTrangThai() != 1) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Invalid user", null));
            }

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Token is valid", convertToUserDto(user)));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid token", null));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            HttpServletRequest request) {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Invalid token", null));
            }

            String oldToken = authHeader.substring(7);

            if (jwtUtil.isTokenBlacklisted(oldToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Token has been invalidated", null));
            }

            String email = jwtUtil.extractEmail(oldToken);
            TaiKhoan user = authService.findByEmail(email);

            if (user == null || user.getTrangThai() != 1) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Invalid user", null));
            }

            String newToken = jwtUtil.generateToken(user);
            jwtUtil.blacklistToken(oldToken);

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(newToken);
            loginResponse.setUser(convertToUserDto(user));
            loginResponse.setExpiresIn(jwtUtil.getExpirationTime());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Token refreshed", loginResponse));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Could not refresh token", null));
        }
    }

    private UserDto convertToUserDto(TaiKhoan user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId().longValue());
        userDto.setEmail(user.getEmail());
        userDto.setVaiTro(user.getVaiTro().name());
        userDto.setMaTaiKhoan(user.getMaTaiKhoan());
        return userDto;
    }
}
