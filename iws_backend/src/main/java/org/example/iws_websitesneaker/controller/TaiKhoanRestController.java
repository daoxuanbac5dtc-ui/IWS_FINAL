package org.example.iws_websitesneaker.controller;

import jakarta.validation.Valid;
import org.example.iws_websitesneaker.Service.KhachHangService;
import org.example.iws_websitesneaker.Service.NhanVienService;
import org.example.iws_websitesneaker.Dto.TaiKhoanDTO;
import org.example.iws_websitesneaker.Service.TaiKhoanService;
import org.example.iws_websitesneaker.Service.impl.TaiKhoanServiceImpl;
import org.example.iws_websitesneaker.entity.KhachHang;
import org.example.iws_websitesneaker.entity.NhanVien;
import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.example.iws_websitesneaker.repository.RepoTaiKhoan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tai-khoan")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"},
        allowCredentials = "true",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TaiKhoanRestController {

    @Autowired
    private TaiKhoanService taiKhoanService;
    @Autowired
    private KhachHangService khachHangService;
    @Autowired
    private NhanVienService nhanVienService;
    @Autowired
    private RepoTaiKhoan repoTaiKhoan;
    // ===== BASIC QUERIES =====

    /**
     * Lấy danh sách tài khoản với filter và search
     */
    @GetMapping
    public ResponseEntity<?> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String vaiTro,
            @RequestParam(required = false) Integer trangThai) {
        try {
            System.out.println("=== GET /api/tai-khoan ===");

            List<TaiKhoan> allAccounts = taiKhoanService.findAll();

            // Apply filters
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                allAccounts = allAccounts.stream()
                        .filter(acc ->
                                (acc.getEmail() != null && acc.getEmail().toLowerCase().contains(searchLower)) ||
                                        (acc.getMaTaiKhoan() != null && acc.getMaTaiKhoan().toLowerCase().contains(searchLower)) ||
                                        (acc.getVaiTro() != null && acc.getVaiTro().name().toLowerCase().contains(searchLower))
                        )
                        .collect(Collectors.toList());
            }

            if (vaiTro != null && !vaiTro.trim().isEmpty()) {
                TaiKhoan.VaiTro role = parseVaiTro(vaiTro);
                if (role != null) {
                    allAccounts = allAccounts.stream()
                            .filter(acc -> acc.getVaiTro().equals(role))
                            .collect(Collectors.toList());
                }
            }

            if (trangThai != null) {
                allAccounts = allAccounts.stream()
                        .filter(acc -> acc.getTrangThai().equals(trangThai))
                        .collect(Collectors.toList());
            }

            sortAccountList(allAccounts, sortBy, sortDir);
            return ResponseEntity.ok(createSuccessResponse("Lấy danh sách tài khoản thành công", allAccounts));

        } catch (Exception e) {
            System.err.println("❌ Error getting all accounts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi tải danh sách tài khoản", "INTERNAL_ERROR"));
        }
    }

    /**
     * Lấy tài khoản theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTaiKhoanById(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<TaiKhoan> taiKhoanOpt = repoTaiKhoan.findById(id);
            if (taiKhoanOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy tài khoản với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("success", true);
            response.put("data", taiKhoanOpt.get());
            response.put("message", "Lấy thông tin tài khoản thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi lấy thông tin tài khoản: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ===== ACCOUNT CREATION =====

    /**
     * Tạo tài khoản hoàn chỉnh với thông tin liên quan
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCompleteAccount(@RequestBody @Valid TaiKhoanDTO dto) {
        try {
            System.out.println("=== Tạo tài khoản mới ===");
            System.out.println("Dữ liệu nhận được: " + dto);

            // Validation đầy đủ
            Map<String, String> validationErrors = validateCreateAccount(dto);
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createValidationErrorResponse("Dữ liệu không hợp lệ", validationErrors));
            }

            // Kiểm tra email đã tồn tại
            if (taiKhoanService.existsByEmail(dto.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createErrorResponse("Email đã tồn tại trong hệ thống", "EMAIL_EXISTS"));
            }

            // Tạo tài khoản hoàn chỉnh thông qua service
            Map<String, Object> result = taiKhoanService.createCompleteAccount(dto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createSuccessResponse("Tài khoản được tạo thành công", result));

        } catch (IllegalArgumentException e) {
            System.err.println("Lỗi validation: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            System.err.println("Lỗi tạo tài khoản: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi hệ thống khi tạo tài khoản: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    // ===== ACCOUNT UPDATE =====

    /**
     * Cập nhật thông tin tài khoản
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTaiKhoan(
            @PathVariable Integer id,
            @RequestBody TaiKhoan taiKhoanUpdate) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<TaiKhoan> taiKhoanOpt = repoTaiKhoan.findById(id);
            if (taiKhoanOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy tài khoản với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            TaiKhoan taiKhoan = taiKhoanOpt.get();
            Integer oldTrangThai = taiKhoan.getTrangThai();

            if (taiKhoanUpdate.getEmail() != null) {
                Optional<TaiKhoan> existingEmail = repoTaiKhoan.findByEmail(taiKhoanUpdate.getEmail());
                if (existingEmail.isPresent() && !existingEmail.get().getId().equals(id)) {
                    response.put("success", false);
                    response.put("message", "Email đã tồn tại trong hệ thống");
                    return ResponseEntity.badRequest().body(response);
                }
                taiKhoan.setEmail(taiKhoanUpdate.getEmail());
            }

            if (taiKhoanUpdate.getTrangThai() != null) {
                taiKhoan.setTrangThai(taiKhoanUpdate.getTrangThai());
            }

            if (taiKhoanUpdate.getVaiTro() != null) {
                taiKhoan.setVaiTro(taiKhoanUpdate.getVaiTro());
            }

            taiKhoan.setNgayCapNhat(new Date());
            TaiKhoan updatedTaiKhoan = repoTaiKhoan.save(taiKhoan);

            if (taiKhoanUpdate.getTrangThai() != null && !Objects.equals(oldTrangThai, updatedTaiKhoan.getTrangThai())) {
                syncLinkedEntityStatus(updatedTaiKhoan, updatedTaiKhoan.getTrangThai());
            }

            response.put("success", true);
            response.put("data", updatedTaiKhoan);
            response.put("message", "Cập nhật tài khoản thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật tài khoản: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ===== STATUS CHANGE =====

    /**
     * Thay đổi trạng thái tài khoản
     */
    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<Map<String, Object>> updateTrangThai(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer trangThai,
            @RequestBody(required = false) Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();

        try {
            Integer resolvedTrangThai = resolveTrangThaiValue(trangThai, request);
            if (resolvedTrangThai == null || (resolvedTrangThai != 0 && resolvedTrangThai != 1)) {
                response.put("success", false);
                response.put("message", "Trạng thái không hợp lệ. Chỉ chấp nhận 0 hoặc 1");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<TaiKhoan> taiKhoanOpt = repoTaiKhoan.findById(id);
            if (taiKhoanOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy tài khoản với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            TaiKhoan taiKhoan = taiKhoanOpt.get();
            if (Objects.equals(taiKhoan.getTrangThai(), resolvedTrangThai)) {
                syncLinkedEntityStatus(taiKhoan, resolvedTrangThai);
                response.put("success", true);
                response.put("data", taiKhoan);
                response.put("message", "Trạng thái tài khoản đã ở giá trị yêu cầu và dữ liệu liên kết đã được đồng bộ");
                return ResponseEntity.ok(response);
            }

            taiKhoan.setTrangThai(resolvedTrangThai);
            taiKhoan.setNgayCapNhat(new Date());
            TaiKhoan updatedTaiKhoan = repoTaiKhoan.save(taiKhoan);
            syncLinkedEntityStatus(updatedTaiKhoan, resolvedTrangThai);

            response.put("success", true);
            response.put("data", updatedTaiKhoan);
            response.put("message", "Cập nhật trạng thái tài khoản thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private Integer resolveTrangThaiValue(Integer requestParamStatus, Map<String, Object> requestBody) {
        if (requestParamStatus != null) {
            return requestParamStatus;
        }

        if (requestBody == null) {
            return null;
        }

        Object rawValue = requestBody.get("trangThai");
        if (rawValue instanceof Number numberValue) {
            return numberValue.intValue();
        }

        if (rawValue instanceof String stringValue && !stringValue.trim().isEmpty()) {
            try {
                return Integer.parseInt(stringValue.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private void syncLinkedEntityStatus(TaiKhoan taiKhoan, Integer targetStatus) {
        if (taiKhoan == null || taiKhoan.getId() == null || targetStatus == null || taiKhoan.getVaiTro() == null) {
            return;
        }

        switch (taiKhoan.getVaiTro()) {
            case USER:
                Optional<KhachHang> customerOpt = khachHangService.findByTaiKhoanIdOptional(taiKhoan.getId());
                if (customerOpt.isPresent() && !Objects.equals(customerOpt.get().getTrangThai(), targetStatus)) {
                    khachHangService.toggleTrangThai(customerOpt.get().getId());
                }
                break;
            case NHANVIEN:
                Optional<NhanVien> employeeOpt = nhanVienService.findByTaiKhoanId(taiKhoan.getId());
                if (employeeOpt.isPresent() && !Objects.equals(employeeOpt.get().getTrangThai(), targetStatus)) {
                    nhanVienService.toggleTrangThai(employeeOpt.get().getId());
                }
                break;
            default:
                break;
        }
    }    // ===== DELETE ACCOUNT =====

    /**
     * Xóa tài khoản
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteAccount(@PathVariable Integer id) {
        try {
            System.out.println("=== Xóa tài khoản ===");
            System.out.println("ID tài khoản: " + id);

            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID không hợp lệ", "INVALID_ID"));
            }

            Optional<TaiKhoan> accountOpt = taiKhoanService.findById(id);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Không tìm thấy tài khoản", "NOT_FOUND"));
            }

            TaiKhoan account = accountOpt.get();

            // Kiểm tra quy tắc nghiệp vụ trước
            if (!taiKhoanService.canDeleteAccount(id)) {
                String reason = "";
                if (account.getVaiTro() == TaiKhoan.VaiTro.ADMIN && taiKhoanService.isLastActiveAdmin(id)) {
                    reason = "Không thể xóa admin cuối cùng";
                } else {
                    reason = "Tài khoản có dữ liệu quan trọng không thể xóa";
                }
                return ResponseEntity.badRequest()
                        .body(createErrorResponse(reason, "DELETE_FORBIDDEN"));
            }

            // Thực hiện xóa với try-catch chi tiết
            try {
                // Sử dụng phương thức xóa an toàn mới
                if (taiKhoanService instanceof TaiKhoanServiceImpl) {
                    ((TaiKhoanServiceImpl) taiKhoanService).safeDeleteById(id);
                } else {
                    taiKhoanService.deleteById(id);
                }

                System.out.println("✅ Xóa tài khoản thành công: " + id);

                return ResponseEntity.ok(createSuccessResponse("Xóa tài khoản thành công",
                        Map.of("deletedAccountId", id, "deletedAt", new Date())));

            } catch (Exception deleteError) {
                System.err.println("❌ Lỗi khi xóa: " + deleteError.getMessage());
                deleteError.printStackTrace();

                // Phân loại lỗi cụ thể
                String errorMessage;
                String errorCode;

                if (deleteError.getMessage().contains("constraint") ||
                        deleteError.getMessage().contains("foreign key") ||
                        deleteError.getMessage().contains("REFERENCE")) {
                    errorMessage = "Không thể xóa tài khoản do còn dữ liệu liên quan. Vui lòng kiểm tra voucher, đơn hàng, v.v.";
                    errorCode = "CONSTRAINT_VIOLATION";
                } else if (deleteError.getMessage().contains("admin cuối cùng")) {
                    errorMessage = "Không thể xóa admin cuối cùng trong hệ thống";
                    errorCode = "LAST_ADMIN_ERROR";
                } else if (deleteError.getMessage().contains("không được phép")) {
                    errorMessage = "Không có quyền xóa tài khoản này";
                    errorCode = "PERMISSION_DENIED";
                } else {
                    errorMessage = "Lỗi hệ thống khi xóa tài khoản: " + deleteError.getMessage();
                    errorCode = "DELETE_ERROR";
                }

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createErrorResponse(errorMessage, errorCode));
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi tổng quát khi xóa tài khoản: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi hệ thống: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }


    /**
     * Kiểm tra email có tồn tại không
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailExists(@RequestParam String email) {
        try {
            if (!taiKhoanService.isValidEmail(email)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Email không hợp lệ", "INVALID_EMAIL"));
            }

            boolean exists = taiKhoanService.existsByEmail(email);
            Map<String, Object> data = Map.of(
                    "email", email,
                    "exists", exists,
                    "available", !exists
            );

            return ResponseEntity.ok(createSuccessResponse("Kiểm tra email thành công", data));

        } catch (Exception e) {
            System.err.println("Error checking email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi kiểm tra email", "INTERNAL_ERROR"));
        }
    }
    @GetMapping("/{id}/can-delete")
    public ResponseEntity<?> checkCanDelete(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID không hợp lệ", "INVALID_ID"));
            }

            Optional<TaiKhoan> accountOpt = taiKhoanService.findById(id);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Không tìm thấy tài khoản", "NOT_FOUND"));
            }

            boolean canDelete = taiKhoanService.canDeleteAccount(id);
            TaiKhoan account = accountOpt.get();

            Map<String, Object> checkResult = new HashMap<>();
            checkResult.put("canDelete", canDelete);
            checkResult.put("accountId", id);
            checkResult.put("email", account.getEmail());
            checkResult.put("role", account.getVaiTro().name());

            if (!canDelete) {
                String reason = "";
                if (account.getVaiTro() == TaiKhoan.VaiTro.ADMIN && taiKhoanService.isLastActiveAdmin(id)) {
                    reason = "Admin cuối cùng trong hệ thống";
                } else {
                    reason = "Có dữ liệu liên quan quan trọng";
                }
                checkResult.put("reason", reason);
            }

            return ResponseEntity.ok(createSuccessResponse("Kiểm tra thành công", checkResult));

        } catch (Exception e) {
            System.err.println("Error checking delete permission: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi kiểm tra", "INTERNAL_ERROR"));
        }
    }
    /**
     * Lấy thống kê dashboard
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getDashboardStatistics() {
        try {
            Map<String, Object> stats = taiKhoanService.getDashboardStats();
            return ResponseEntity.ok(createSuccessResponse("Lấy thống kê thành công", stats));
        } catch (Exception e) {
            System.err.println("Error getting statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi lấy thống kê", "INTERNAL_ERROR"));
        }
    }

    /**
     * Xác thực tài khoản (đăng nhập)
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Thiếu email hoặc mật khẩu", "MISSING_CREDENTIALS"));
            }

            if (!taiKhoanService.isValidEmail(email)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Email không hợp lệ", "INVALID_EMAIL"));
            }

            Optional<TaiKhoan> account = taiKhoanService.authenticate(email, password);

            if (account.isPresent()) {
                TaiKhoan taiKhoan = account.get();
                if (taiKhoan.getTrangThai() == 0) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(createErrorResponse("Tài khoản đã bị vô hiệu hóa", "ACCOUNT_DISABLED"));
                }

                taiKhoanService.logAccountActivity(taiKhoan.getId(), "LOGIN", "Đăng nhập thành công");

                Map<String, Object> loginData = Map.of(
                        "account", taiKhoan,
                        "loginTime", new Date(),
                        "role", taiKhoan.getVaiTro().getDisplayName()
                );

                return ResponseEntity.ok(createSuccessResponse("Đăng nhập thành công", loginData));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Email hoặc mật khẩu không đúng", "INVALID_CREDENTIALS"));
            }

        } catch (Exception e) {
            System.err.println("Error authenticating: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi đăng nhập", "INTERNAL_ERROR"));
        }
    }

    // ===== VALIDATION METHODS =====

    private Map<String, String> validateCreateAccount(TaiKhoanDTO dto) {
        Map<String, String> errors = new HashMap<>();

        // Validate vai trò
        if (dto.getVaiTro() == null && (dto.getVaiTroString() == null || dto.getVaiTroString().trim().isEmpty())) {
            errors.put("vaiTro", "Vai trò là bắt buộc");
        }

        // Validate email
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            errors.put("email", "Email là bắt buộc");
        } else if (!taiKhoanService.isValidEmail(dto.getEmail())) {
            errors.put("email", "Email không hợp lệ");
        }

        // Validate mật khẩu
        if (dto.getMatKhau() == null || dto.getMatKhau().trim().isEmpty()) {
            errors.put("matKhau", "Mật khẩu là bắt buộc");
        } else if (dto.getMatKhau().length() < 6) {
            errors.put("matKhau", "Mật khẩu phải có ít nhất 6 ký tự");
        } else if (dto.getMatKhau().length() > 50) {
            errors.put("matKhau", "Mật khẩu không được quá 50 ký tự");
        }

        // Validate thông tin cá nhân cho non-admin
        if (dto.getVaiTro() != null && dto.getVaiTro() != TaiKhoan.VaiTro.ADMIN) {
            if (dto.getHoTen() == null || dto.getHoTen().trim().isEmpty()) {
                errors.put("hoTen", "Họ tên là bắt buộc");
            } else if (dto.getHoTen().trim().length() < 2) {
                errors.put("hoTen", "Họ tên phải có ít nhất 2 ký tự");
            } else if (dto.getHoTen().trim().length() > 100) {
                errors.put("hoTen", "Họ tên không được quá 100 ký tự");
            }

            if (dto.getSdt() == null || dto.getSdt().trim().isEmpty()) {
                errors.put("sdt", "Số điện thoại là bắt buộc");
            } else if (!taiKhoanService.isValidPhoneNumber(dto.getSdt())) {
                errors.put("sdt", "Số điện thoại không hợp lệ (10-11 số, bắt đầu bằng 0)");
            }
        }

        return errors;
    }

    private Map<String, String> validateUpdateAccount(Map<String, Object> updateData, TaiKhoan existing) {
        Map<String, String> errors = new HashMap<>();

        // Validate email nếu có
        if (updateData.containsKey("email")) {
            String email = (String) updateData.get("email");
            if (email != null && !email.trim().isEmpty()) {
                if (!taiKhoanService.isValidEmail(email)) {
                    errors.put("email", "Email không hợp lệ");
                }
            }
        }

        // Validate mật khẩu nếu có
        if (updateData.containsKey("matKhau")) {
            String password = (String) updateData.get("matKhau");
            if (password != null && !password.trim().isEmpty()) {
                if (password.length() < 6) {
                    errors.put("matKhau", "Mật khẩu phải có ít nhất 6 ký tự");
                } else if (password.length() > 50) {
                    errors.put("matKhau", "Mật khẩu không được quá 50 ký tự");
                }
            }
        }

        // Validate vai trò nếu có
        if (updateData.containsKey("vaiTro") || updateData.containsKey("vaiTroString")) {
            String roleStr = (String) updateData.getOrDefault("vaiTroString", updateData.get("vaiTro"));
            if (roleStr != null && !roleStr.trim().isEmpty()) {
                TaiKhoan.VaiTro role = parseVaiTro(roleStr);
                if (role == null) {
                    errors.put("vaiTro", "Vai trò không hợp lệ");
                }
            }
        }

        // Validate trạng thái nếu có
        if (updateData.containsKey("trangThai")) {
            Object statusObj = updateData.get("trangThai");
            if (statusObj != null) {
                try {
                    Integer status = null;
                    if (statusObj instanceof Integer) {
                        status = (Integer) statusObj;
                    } else if (statusObj instanceof String) {
                        status = Integer.parseInt((String) statusObj);
                    }

                    if (status == null || (status != 0 && status != 1)) {
                        errors.put("trangThai", "Trạng thái phải là 0 hoặc 1");
                    }
                } catch (NumberFormatException e) {
                    errors.put("trangThai", "Trạng thái không hợp lệ");
                }
            }
        }

        return errors;
    }

    // ===== UTILITY METHODS =====

    private TaiKhoan.VaiTro parseVaiTro(String vaiTroString) {
        if (vaiTroString == null || vaiTroString.trim().isEmpty()) {
            return null;
        }

        try {
            return taiKhoanService.parseVaiTro(vaiTroString);
        } catch (Exception e) {
            System.err.println("Error parsing role: " + vaiTroString + " - " + e.getMessage());
            return null;
        }
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", new Date());
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    private Map<String, Object> createErrorResponse(String message, String errorCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("errorCode", errorCode);
        response.put("timestamp", new Date());
        return response;
    }

    private Map<String, Object> createValidationErrorResponse(String message, Map<String, String> errors) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("errorCode", "VALIDATION_ERROR");
        response.put("errors", errors);
        response.put("timestamp", new Date());
        return response;
    }

    private void sortAccountList(List<TaiKhoan> list, String sortBy, String sortDir) {
        if (list == null || list.isEmpty() || sortBy == null) return;

        list.sort((a, b) -> {
            int result = 0;
            switch (sortBy.toLowerCase()) {
                case "id":
                    result = a.getId().compareTo(b.getId());
                    break;
                case "email":
                    result = Optional.ofNullable(a.getEmail()).orElse("")
                            .compareToIgnoreCase(Optional.ofNullable(b.getEmail()).orElse(""));
                    break;
                case "mataikhoan":
                    result = Optional.ofNullable(a.getMaTaiKhoan()).orElse("")
                            .compareToIgnoreCase(Optional.ofNullable(b.getMaTaiKhoan()).orElse(""));
                    break;
                case "vaitro":
                    result = a.getVaiTro().compareTo(b.getVaiTro());
                    break;
                case "ngaytao":
                    result = Optional.ofNullable(a.getNgayTao()).orElse(new Date(0))
                            .compareTo(Optional.ofNullable(b.getNgayTao()).orElse(new Date(0)));
                    break;
                case "trangthai":
                    result = a.getTrangThai().compareTo(b.getTrangThai());
                    break;
                default:
                    result = a.getId().compareTo(b.getId());
            }
            return "desc".equalsIgnoreCase(sortDir) ? -result : result;
        });
    }
}
