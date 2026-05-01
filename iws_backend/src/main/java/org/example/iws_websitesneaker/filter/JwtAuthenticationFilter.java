package org.example.iws_websitesneaker.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.iws_websitesneaker.Service.LoginService;
import org.example.iws_websitesneaker.util.JwtUtil;
import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter implements Filter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LoginService authService;

    // Các endpoint không cần authentication (prefix match)
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/verify",
            "/auth/refresh",
            "/api/public/",
            "/public/",
            "/css/",
            "/js/",
            "/images/",
            "/favicon.ico",
            "/error",
            "/landing",
            "/hinh-anh/images/",
            "/hinh-anh/duong-dan/",
            "/voucher/images/",
            "/return-images/"
    );

    // Các exact paths không cần authentication
    private static final List<String> PUBLIC_EXACT_PATHS = Arrays.asList(
            "/",
            "/api/dashboard/health"
    );

    // Các endpoint POST công khai (guest có thể POST)
    private static final List<String> PUBLIC_POST_PATHS = Arrays.asList(
            "/tai-khoan",
            "/api/tai-khoan",
            "/khach-hang",
            "/api/dia-chi",
            "/auth/forgot-password/send-code",
            "/auth/forgot-password/reset-password",
            "/hoa-don",
            "/api/hoa-don/guest-order",
            "/api/gio-hang/guest/add",
            "/api/gio-hang/guest/checkout",
            "/voucher/validate-guest",
            "/api/vnpay/create-payment"
    );

    // Các endpoint GET công khai
    private static final List<String> PUBLIC_GET_PATHS = Arrays.asList(
            "/san-pham",
            "/danh-muc",
            "/thuong-hieu",
            "/api/san-pham",
            "/api/san-pham/search",
            "/api/san-pham-chi-tiet",
            "/hinh-anh",
            "/de-giay",
            "/kich-co",
            "/khuyen-mai",
            "/voucher",
            "/voucher/public",
            "/mau-sac",
            "/chat-lieu",
            "/khach-hang",
            "/api/tai-khoan/check-email",
            "/api/dia-chi",
            "/hoa-don",
            "/api/gio-hang/current",
            "/api/gio-hang/guest",
            "/api/orders/track"
    );

    // Các endpoint PUT công khai
    private static final List<String> PUBLIC_PUT_PATHS = Arrays.asList(
            "/hoa-don",
            "/api/gio-hang/guest/update"
    );

    // Admin-only (cần role ADMIN)
    private static final List<String> ADMIN_ONLY_PATHS = Arrays.asList(
            "/tai-khoan",
            "/nhan-vien",
            "/admin",
            "/users/manage",
            "/system"
    );

    // Các endpoint dành cho admin và nhân viên
    private static final List<String> ADMIN_NHANVIEN_PATHS = Arrays.asList(
            "/san-pham",
            "/khach-hang",
            "/hoa-don",
            "/thuoc-tinh",
            "/voucher",
            "/khuyen-mai",
            "/de-giay",
            "/thuong-hieu",
            "/kich-co",
            "/chat-lieu",
            "/danh-muc",
            "/mau-sac",
            "/dashboard",
            "/reports"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = Optional.ofNullable(httpRequest.getRequestURI()).orElse("");
        String method = Optional.ofNullable(httpRequest.getMethod()).orElse("GET");
        String authHeader = httpRequest.getHeader("Authorization");

        if (isDebugEnabled()) {
            System.out.println("=== JWT FILTER DEBUG ===");
            System.out.println("Request URI: " + requestURI);
            System.out.println("Method: " + method);
            System.out.println("Auth Header: " + (authHeader != null ? "Bearer ***" : "null"));
        }

        // Bỏ qua OPTIONS request (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        // Nếu path công khai (guest được truy cập)
        if (isPublicPath(requestURI, method)) {
            if (isDebugEnabled()) System.out.println("Public path - allowing: " + requestURI);

            // Nếu có JWT thì parse user và gắn vào request (để optional login)
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (jwtUtil.validateToken(token)) {
                        String email = jwtUtil.extractEmail(token);
                        TaiKhoan user = authService.findByEmail(email);
                        if (user != null && user.getTrangThai() != null && user.getTrangThai() == 1) {
                            httpRequest.setAttribute("currentUser", user);
                            httpRequest.setAttribute("currentUserId", user.getId());
                            httpRequest.setAttribute("currentUserRole",
                                    user.getVaiTro() != null ? user.getVaiTro().name() : null);
                            httpRequest.setAttribute("currentUserEmail", user.getEmail());
                            if (isDebugEnabled()) {
                                System.out.println("Public path with JWT - parsed user: " + user.getEmail());
                            }
                        }
                    }
                } catch (Exception e) {
                    if (isDebugEnabled()) {
                        System.out.println("Public path - JWT parse failed: " + e.getMessage());
                    }
                    // Không chặn, vẫn cho qua như guest
                }
            }

            chain.doFilter(request, response);
            return;
        }

        // Non-public path -> cần token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (isDebugEnabled()) System.out.println("No Authorization header for protected path: " + requestURI);
            sendUnauthorizedResponse(httpResponse, "Vui lòng đăng nhập");
            return;
        }

        String token = authHeader.substring(7);
        String email;
        try {
            if (!jwtUtil.validateToken(token)) {
                if (isDebugEnabled()) System.out.println("Token validation failed for path: " + requestURI);
                sendUnauthorizedResponse(httpResponse, "Token không hợp lệ hoặc đã hết hạn");
                return;
            }
            email = jwtUtil.extractEmail(token);
        } catch (Exception e) {
            if (isDebugEnabled()) System.out.println("Token error: " + e.getMessage());
            sendUnauthorizedResponse(httpResponse, "Token không hợp lệ");
            return;
        }

        // Kiểm tra user từ DB
        TaiKhoan user = authService.findByEmail(email);
        if (user == null) {
            if (isDebugEnabled()) System.out.println("User not found for email: " + email);
            sendUnauthorizedResponse(httpResponse, "Người dùng không tồn tại");
            return;
        }

        // Kiểm tra trạng thái tài khoản
        if (user.getTrangThai() == null || user.getTrangThai() != 1) {
            if (isDebugEnabled()) System.out.println("User inactive: " + user.getEmail());
            sendUnauthorizedResponse(httpResponse, "Tài khoản đã bị khóa hoặc không hoạt động");
            return;
        }

        if (isDebugEnabled()) {
            System.out.println("User found: " + user.getEmail() + ", role: " + user.getVaiTro());
        }

        // Kiểm tra ADMIN-only
        if (isAdminOnlyPath(requestURI) && user.getVaiTro() != TaiKhoan.VaiTro.ADMIN) {
            if (isDebugEnabled()) System.out.println("Admin-only path but user not admin: " + requestURI);
            sendForbiddenResponse(httpResponse, "Chỉ quản trị viên mới có quyền truy cập");
            return;
        }

        // Kiểm tra admin/nhanvien paths (bỏ qua nếu đường dẫn public POST/PUT)
        if (isAdminNhanVienPath(requestURI)
                && !isPublicPostPath(requestURI, method)
                && !isPublicPutPath(requestURI, method)
                && user.getVaiTro() != TaiKhoan.VaiTro.ADMIN
                && user.getVaiTro() != TaiKhoan.VaiTro.NHANVIEN) {

            if (isDebugEnabled()) System.out.println("Admin/NV path but role not allowed: " + user.getVaiTro());
            sendForbiddenResponse(httpResponse, "Không có quyền truy cập tài nguyên này");
            return;
        }

        // Đặt thông tin user vào request attributes để controller/service có thể dùng
        httpRequest.setAttribute("currentUser", user);
        httpRequest.setAttribute("currentUserId", user.getId());
        httpRequest.setAttribute("currentUserRole", user.getVaiTro() != null ? user.getVaiTro().name() : null);
        httpRequest.setAttribute("currentUserEmail", user.getEmail());

        if (isDebugEnabled()) System.out.println("Authentication successful - allowing request: " + requestURI);

        chain.doFilter(request, response);
    }

    // ========== Helpers ==========
    private boolean isPublicPath(String uri, String method) {
        // exact match
        if (PUBLIC_EXACT_PATHS.contains(uri)) return true;

        // startsWith match for static/public folders
        if (PUBLIC_PATHS.stream().anyMatch(uri::startsWith)) return true;

        // method specific lists
        if ("GET".equalsIgnoreCase(method) && PUBLIC_GET_PATHS.stream().anyMatch(uri::startsWith)) return true;
        if ("POST".equalsIgnoreCase(method)) {
            if (PUBLIC_POST_PATHS.stream().anyMatch(uri::startsWith)) return true;
            // Regex cho guest cancel order
            if (uri.matches("^/api/hoa-don/guest/\\d+/cancel$")) return true;
        }
        if ("PUT".equalsIgnoreCase(method) && PUBLIC_PUT_PATHS.stream().anyMatch(uri::startsWith)) return true;

        return false;
    }

    private boolean isPublicPostPath(String uri, String method) {
        return "POST".equalsIgnoreCase(method) && PUBLIC_POST_PATHS.stream().anyMatch(uri::startsWith);
    }

    private boolean isPublicPutPath(String uri, String method) {
        return "PUT".equalsIgnoreCase(method) && PUBLIC_PUT_PATHS.stream().anyMatch(uri::startsWith);
    }

    private boolean isAdminOnlyPath(String uri) {
        return ADMIN_ONLY_PATHS.stream().anyMatch(uri::startsWith);
    }

    private boolean isAdminNhanVienPath(String uri) {
        return ADMIN_NHANVIEN_PATHS.stream().anyMatch(uri::startsWith);
    }

    private boolean isDebugEnabled() {
        // Set false khi deploy production
        return true;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = String.format(
                "{\"success\": false, \"message\": \"%s\", \"data\": null, \"timestamp\": \"%s\"}",
                message, java.time.Instant.now().toString()
        );
        response.getWriter().write(jsonResponse);
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = String.format(
                "{\"success\": false, \"message\": \"%s\", \"data\": null, \"timestamp\": \"%s\"}",
                message, java.time.Instant.now().toString()
        );
        response.getWriter().write(jsonResponse);
    }
}

