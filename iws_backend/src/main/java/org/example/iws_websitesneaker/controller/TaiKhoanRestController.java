package org.example.iws_websitesneaker.controller;

import jakarta.validation.Valid;
import org.example.iws_websitesneaker.Dto.TaiKhoanDTO;
import org.example.iws_websitesneaker.Service.TaiKhoanService;
import org.example.iws_websitesneaker.Service.impl.TaiKhoanServiceImpl;
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
    private RepoTaiKhoan repoTaiKhoan;
    // ===== BASIC QUERIES =====

    /**
     * Láº¥y danh sÃ¡ch tÃ i khoáº£n vá»›i filter vÃ  search
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
            return ResponseEntity.ok(createSuccessResponse("Láº¥y danh sÃ¡ch tÃ i khoáº£n thÃ nh cÃ´ng", allAccounts));

        } catch (Exception e) {
            System.err.println("âŒ Error getting all accounts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi táº£i danh sÃ¡ch tÃ i khoáº£n", "INTERNAL_ERROR"));
        }
    }

    /**
     * Láº¥y tÃ i khoáº£n theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTaiKhoanById(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<TaiKhoan> taiKhoanOpt = repoTaiKhoan.findById(id);
            if (taiKhoanOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n vá»›i ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("success", true);
            response.put("data", taiKhoanOpt.get());
            response.put("message", "Láº¥y thÃ´ng tin tÃ i khoáº£n thÃ nh cÃ´ng");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lá»—i khi láº¥y thÃ´ng tin tÃ i khoáº£n: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ===== ACCOUNT CREATION =====

    /**
     * Táº¡o tÃ i khoáº£n hoÃ n chá»‰nh vá»›i thÃ´ng tin liÃªn quan
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCompleteAccount(@RequestBody @Valid TaiKhoanDTO dto) {
        try {
            System.out.println("=== Táº¡o tÃ i khoáº£n má»›i ===");
            System.out.println("Dá»¯ liá»‡u nháº­n Ä‘Æ°á»£c: " + dto);

            // Validation Ä‘áº§y Ä‘á»§
            Map<String, String> validationErrors = validateCreateAccount(dto);
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createValidationErrorResponse("Dá»¯ liá»‡u khÃ´ng há»£p lá»‡", validationErrors));
            }

            // Kiá»ƒm tra email Ä‘Ã£ tá»“n táº¡i
            if (taiKhoanService.existsByEmail(dto.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createErrorResponse("Email Ä‘Ã£ tá»“n táº¡i trong há»‡ thá»‘ng", "EMAIL_EXISTS"));
            }

            // Táº¡o tÃ i khoáº£n hoÃ n chá»‰nh thÃ´ng qua service
            Map<String, Object> result = taiKhoanService.createCompleteAccount(dto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createSuccessResponse("TÃ i khoáº£n Ä‘Æ°á»£c táº¡o thÃ nh cÃ´ng", result));

        } catch (IllegalArgumentException e) {
            System.err.println("Lá»—i validation: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            System.err.println("Lá»—i táº¡o tÃ i khoáº£n: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i há»‡ thá»‘ng khi táº¡o tÃ i khoáº£n: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    // ===== ACCOUNT UPDATE =====

    /**
     * Cáº­p nháº­t thÃ´ng tin tÃ i khoáº£n
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
                response.put("message", "KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n vá»›i ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            TaiKhoan taiKhoan = taiKhoanOpt.get();

            // Cáº­p nháº­t cÃ¡c trÆ°á»ng cÃ³ thá»ƒ thay Ä‘á»•i
            if (taiKhoanUpdate.getEmail() != null) {
                // Kiá»ƒm tra email Ä‘Ã£ tá»“n táº¡i chÆ°a
                Optional<TaiKhoan> existingEmail = repoTaiKhoan.findByEmail(taiKhoanUpdate.getEmail());
                if (existingEmail.isPresent() && !existingEmail.get().getId().equals(id)) {
                    response.put("success", false);
                    response.put("message", "Email Ä‘Ã£ tá»“n táº¡i trong há»‡ thá»‘ng");
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

            // KhÃ´ng cho phÃ©p cáº­p nháº­t máº­t kháº©u qua API nÃ y
            taiKhoan.setNgayCapNhat(new java.util.Date());

            TaiKhoan updatedTaiKhoan = repoTaiKhoan.save(taiKhoan);

            response.put("success", true);
            response.put("data", updatedTaiKhoan);
            response.put("message", "Cáº­p nháº­t tÃ i khoáº£n thÃ nh cÃ´ng");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lá»—i khi cáº­p nháº­t tÃ i khoáº£n: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ===== STATUS CHANGE =====

    /**
     * Thay Ä‘á»•i tráº¡ng thÃ¡i tÃ i khoáº£n
     */
    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<Map<String, Object>> updateTrangThai(
            @PathVariable Integer id,
            @RequestParam Integer trangThai) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate tráº¡ng thÃ¡i
            if (trangThai == null || (trangThai != 0 && trangThai != 1)) {
                response.put("success", false);
                response.put("message", "Tráº¡ng thÃ¡i khÃ´ng há»£p lá»‡. Chá»‰ cháº¥p nháº­n 0 (vÃ´ hiá»‡u hÃ³a) hoáº·c 1 (kÃ­ch hoáº¡t)");
                return ResponseEntity.badRequest().body(response);
            }

            // TÃ¬m tÃ i khoáº£n
            Optional<TaiKhoan> taiKhoanOpt = repoTaiKhoan.findById(id);
            if (taiKhoanOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n vá»›i ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            TaiKhoan taiKhoan = taiKhoanOpt.get();

            // Kiá»ƒm tra tráº¡ng thÃ¡i hiá»‡n táº¡i
            if (taiKhoan.getTrangThai().equals(trangThai)) {
                String statusText = trangThai == 1 ? "Ä‘Ã£ Ä‘Æ°á»£c kÃ­ch hoáº¡t" : "Ä‘Ã£ bá»‹ vÃ´ hiá»‡u hÃ³a";
                response.put("success", false);
                response.put("message", "TÃ i khoáº£n " + statusText + " rá»“i");
                return ResponseEntity.badRequest().body(response);
            }

            // Cáº­p nháº­t tráº¡ng thÃ¡i
            taiKhoan.setTrangThai(trangThai);
            taiKhoan.setNgayCapNhat(new java.util.Date());
            TaiKhoan updatedTaiKhoan = repoTaiKhoan.save(taiKhoan);

            // Tráº£ vá» káº¿t quáº£
            String statusText = trangThai == 1 ? "kÃ­ch hoáº¡t" : "vÃ´ hiá»‡u hÃ³a";
            response.put("success", true);
            response.put("data", updatedTaiKhoan);
            response.put("message", "Cáº­p nháº­t tráº¡ng thÃ¡i tÃ i khoáº£n thÃ nh cÃ´ng (" + statusText + ")");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lá»—i khi cáº­p nháº­t tráº¡ng thÃ¡i: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // ===== DELETE ACCOUNT =====

    /**
     * XÃ³a tÃ i khoáº£n
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteAccount(@PathVariable Integer id) {
        try {
            System.out.println("=== XÃ³a tÃ i khoáº£n ===");
            System.out.println("ID tÃ i khoáº£n: " + id);

            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID khÃ´ng há»£p lá»‡", "INVALID_ID"));
            }

            Optional<TaiKhoan> accountOpt = taiKhoanService.findById(id);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n", "NOT_FOUND"));
            }

            TaiKhoan account = accountOpt.get();

            // Kiá»ƒm tra quy táº¯c nghiá»‡p vá»¥ trÆ°á»›c
            if (!taiKhoanService.canDeleteAccount(id)) {
                String reason = "";
                if (account.getVaiTro() == TaiKhoan.VaiTro.ADMIN && taiKhoanService.isLastActiveAdmin(id)) {
                    reason = "KhÃ´ng thá»ƒ xÃ³a admin cuá»‘i cÃ¹ng";
                } else {
                    reason = "TÃ i khoáº£n cÃ³ dá»¯ liá»‡u quan trá»ng khÃ´ng thá»ƒ xÃ³a";
                }
                return ResponseEntity.badRequest()
                        .body(createErrorResponse(reason, "DELETE_FORBIDDEN"));
            }

            // Thá»±c hiá»‡n xÃ³a vá»›i try-catch chi tiáº¿t
            try {
                // Sá»­ dá»¥ng phÆ°Æ¡ng thá»©c xÃ³a an toÃ n má»›i
                if (taiKhoanService instanceof TaiKhoanServiceImpl) {
                    ((TaiKhoanServiceImpl) taiKhoanService).safeDeleteById(id);
                } else {
                    taiKhoanService.deleteById(id);
                }

                System.out.println("âœ… XÃ³a tÃ i khoáº£n thÃ nh cÃ´ng: " + id);

                return ResponseEntity.ok(createSuccessResponse("XÃ³a tÃ i khoáº£n thÃ nh cÃ´ng",
                        Map.of("deletedAccountId", id, "deletedAt", new Date())));

            } catch (Exception deleteError) {
                System.err.println("âŒ Lá»—i khi xÃ³a: " + deleteError.getMessage());
                deleteError.printStackTrace();

                // PhÃ¢n loáº¡i lá»—i cá»¥ thá»ƒ
                String errorMessage;
                String errorCode;

                if (deleteError.getMessage().contains("constraint") ||
                        deleteError.getMessage().contains("foreign key") ||
                        deleteError.getMessage().contains("REFERENCE")) {
                    errorMessage = "KhÃ´ng thá»ƒ xÃ³a tÃ i khoáº£n do cÃ²n dá»¯ liá»‡u liÃªn quan. Vui lÃ²ng kiá»ƒm tra voucher, Ä‘Æ¡n hÃ ng, v.v.";
                    errorCode = "CONSTRAINT_VIOLATION";
                } else if (deleteError.getMessage().contains("admin cuá»‘i cÃ¹ng")) {
                    errorMessage = "KhÃ´ng thá»ƒ xÃ³a admin cuá»‘i cÃ¹ng trong há»‡ thá»‘ng";
                    errorCode = "LAST_ADMIN_ERROR";
                } else if (deleteError.getMessage().contains("khÃ´ng Ä‘Æ°á»£c phÃ©p")) {
                    errorMessage = "KhÃ´ng cÃ³ quyá»n xÃ³a tÃ i khoáº£n nÃ y";
                    errorCode = "PERMISSION_DENIED";
                } else {
                    errorMessage = "Lá»—i há»‡ thá»‘ng khi xÃ³a tÃ i khoáº£n: " + deleteError.getMessage();
                    errorCode = "DELETE_ERROR";
                }

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createErrorResponse(errorMessage, errorCode));
            }

        } catch (Exception e) {
            System.err.println("âŒ Lá»—i tá»•ng quÃ¡t khi xÃ³a tÃ i khoáº£n: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i há»‡ thá»‘ng: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }


    /**
     * Kiá»ƒm tra email cÃ³ tá»“n táº¡i khÃ´ng
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailExists(@RequestParam String email) {
        try {
            if (!taiKhoanService.isValidEmail(email)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Email khÃ´ng há»£p lá»‡", "INVALID_EMAIL"));
            }

            boolean exists = taiKhoanService.existsByEmail(email);
            Map<String, Object> data = Map.of(
                    "email", email,
                    "exists", exists,
                    "available", !exists
            );

            return ResponseEntity.ok(createSuccessResponse("Kiá»ƒm tra email thÃ nh cÃ´ng", data));

        } catch (Exception e) {
            System.err.println("Error checking email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi kiá»ƒm tra email", "INTERNAL_ERROR"));
        }
    }
    @GetMapping("/{id}/can-delete")
    public ResponseEntity<?> checkCanDelete(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID khÃ´ng há»£p lá»‡", "INVALID_ID"));
            }

            Optional<TaiKhoan> accountOpt = taiKhoanService.findById(id);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n", "NOT_FOUND"));
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
                    reason = "Admin cuá»‘i cÃ¹ng trong há»‡ thá»‘ng";
                } else {
                    reason = "CÃ³ dá»¯ liá»‡u liÃªn quan quan trá»ng";
                }
                checkResult.put("reason", reason);
            }

            return ResponseEntity.ok(createSuccessResponse("Kiá»ƒm tra thÃ nh cÃ´ng", checkResult));

        } catch (Exception e) {
            System.err.println("Error checking delete permission: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi kiá»ƒm tra", "INTERNAL_ERROR"));
        }
    }
    /**
     * Láº¥y thá»‘ng kÃª dashboard
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getDashboardStatistics() {
        try {
            Map<String, Object> stats = taiKhoanService.getDashboardStats();
            return ResponseEntity.ok(createSuccessResponse("Láº¥y thá»‘ng kÃª thÃ nh cÃ´ng", stats));
        } catch (Exception e) {
            System.err.println("Error getting statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi láº¥y thá»‘ng kÃª", "INTERNAL_ERROR"));
        }
    }

    /**
     * XÃ¡c thá»±c tÃ i khoáº£n (Ä‘Äƒng nháº­p)
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Thiáº¿u email hoáº·c máº­t kháº©u", "MISSING_CREDENTIALS"));
            }

            if (!taiKhoanService.isValidEmail(email)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Email khÃ´ng há»£p lá»‡", "INVALID_EMAIL"));
            }

            Optional<TaiKhoan> account = taiKhoanService.authenticate(email, password);

            if (account.isPresent()) {
                TaiKhoan taiKhoan = account.get();
                if (taiKhoan.getTrangThai() == 0) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(createErrorResponse("TÃ i khoáº£n Ä‘Ã£ bá»‹ vÃ´ hiá»‡u hÃ³a", "ACCOUNT_DISABLED"));
                }

                taiKhoanService.logAccountActivity(taiKhoan.getId(), "LOGIN", "ÄÄƒng nháº­p thÃ nh cÃ´ng");

                Map<String, Object> loginData = Map.of(
                        "account", taiKhoan,
                        "loginTime", new Date(),
                        "role", taiKhoan.getVaiTro().getDisplayName()
                );

                return ResponseEntity.ok(createSuccessResponse("ÄÄƒng nháº­p thÃ nh cÃ´ng", loginData));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Email hoáº·c máº­t kháº©u khÃ´ng Ä‘Ãºng", "INVALID_CREDENTIALS"));
            }

        } catch (Exception e) {
            System.err.println("Error authenticating: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi Ä‘Äƒng nháº­p", "INTERNAL_ERROR"));
        }
    }

    // ===== VALIDATION METHODS =====

    private Map<String, String> validateCreateAccount(TaiKhoanDTO dto) {
        Map<String, String> errors = new HashMap<>();

        // Validate vai trÃ²
        if (dto.getVaiTro() == null && (dto.getVaiTroString() == null || dto.getVaiTroString().trim().isEmpty())) {
            errors.put("vaiTro", "Vai trÃ² lÃ  báº¯t buá»™c");
        }

        // Validate email
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            errors.put("email", "Email lÃ  báº¯t buá»™c");
        } else if (!taiKhoanService.isValidEmail(dto.getEmail())) {
            errors.put("email", "Email khÃ´ng há»£p lá»‡");
        }

        // Validate máº­t kháº©u
        if (dto.getMatKhau() == null || dto.getMatKhau().trim().isEmpty()) {
            errors.put("matKhau", "Máº­t kháº©u lÃ  báº¯t buá»™c");
        } else if (dto.getMatKhau().length() < 6) {
            errors.put("matKhau", "Máº­t kháº©u pháº£i cÃ³ Ã­t nháº¥t 6 kÃ½ tá»±");
        } else if (dto.getMatKhau().length() > 50) {
            errors.put("matKhau", "Máº­t kháº©u khÃ´ng Ä‘Æ°á»£c quÃ¡ 50 kÃ½ tá»±");
        }

        // Validate thÃ´ng tin cÃ¡ nhÃ¢n cho non-admin
        if (dto.getVaiTro() != null && dto.getVaiTro() != TaiKhoan.VaiTro.ADMIN) {
            if (dto.getHoTen() == null || dto.getHoTen().trim().isEmpty()) {
                errors.put("hoTen", "Há» tÃªn lÃ  báº¯t buá»™c");
            } else if (dto.getHoTen().trim().length() < 2) {
                errors.put("hoTen", "Há» tÃªn pháº£i cÃ³ Ã­t nháº¥t 2 kÃ½ tá»±");
            } else if (dto.getHoTen().trim().length() > 100) {
                errors.put("hoTen", "Há» tÃªn khÃ´ng Ä‘Æ°á»£c quÃ¡ 100 kÃ½ tá»±");
            }

            if (dto.getSdt() == null || dto.getSdt().trim().isEmpty()) {
                errors.put("sdt", "Sá»‘ Ä‘iá»‡n thoáº¡i lÃ  báº¯t buá»™c");
            } else if (!taiKhoanService.isValidPhoneNumber(dto.getSdt())) {
                errors.put("sdt", "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng há»£p lá»‡ (10-11 sá»‘, báº¯t Ä‘áº§u báº±ng 0)");
            }
        }

        return errors;
    }

    private Map<String, String> validateUpdateAccount(Map<String, Object> updateData, TaiKhoan existing) {
        Map<String, String> errors = new HashMap<>();

        // Validate email náº¿u cÃ³
        if (updateData.containsKey("email")) {
            String email = (String) updateData.get("email");
            if (email != null && !email.trim().isEmpty()) {
                if (!taiKhoanService.isValidEmail(email)) {
                    errors.put("email", "Email khÃ´ng há»£p lá»‡");
                }
            }
        }

        // Validate máº­t kháº©u náº¿u cÃ³
        if (updateData.containsKey("matKhau")) {
            String password = (String) updateData.get("matKhau");
            if (password != null && !password.trim().isEmpty()) {
                if (password.length() < 6) {
                    errors.put("matKhau", "Máº­t kháº©u pháº£i cÃ³ Ã­t nháº¥t 6 kÃ½ tá»±");
                } else if (password.length() > 50) {
                    errors.put("matKhau", "Máº­t kháº©u khÃ´ng Ä‘Æ°á»£c quÃ¡ 50 kÃ½ tá»±");
                }
            }
        }

        // Validate vai trÃ² náº¿u cÃ³
        if (updateData.containsKey("vaiTro") || updateData.containsKey("vaiTroString")) {
            String roleStr = (String) updateData.getOrDefault("vaiTroString", updateData.get("vaiTro"));
            if (roleStr != null && !roleStr.trim().isEmpty()) {
                TaiKhoan.VaiTro role = parseVaiTro(roleStr);
                if (role == null) {
                    errors.put("vaiTro", "Vai trÃ² khÃ´ng há»£p lá»‡");
                }
            }
        }

        // Validate tráº¡ng thÃ¡i náº¿u cÃ³
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
                        errors.put("trangThai", "Tráº¡ng thÃ¡i pháº£i lÃ  0 hoáº·c 1");
                    }
                } catch (NumberFormatException e) {
                    errors.put("trangThai", "Tráº¡ng thÃ¡i khÃ´ng há»£p lá»‡");
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
