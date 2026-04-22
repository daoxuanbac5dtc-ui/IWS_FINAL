package org.example.iws_websitesneaker.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.iws_websitesneaker.Dto.KhachHangDto;
import org.example.iws_websitesneaker.Service.KhachHangService;
import org.example.iws_websitesneaker.Service.TaiKhoanService;
import org.example.iws_websitesneaker.Service.DiaChiService;
import org.example.iws_websitesneaker.entity.*;
import org.example.iws_websitesneaker.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/khach-hang")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class KhachHangRestController {

    @Autowired
    private KhachHangService khachHangService;

    @Autowired
    private TaiKhoanService taiKhoanService;

    @Autowired
    private DiaChiService diaChiService;

    @Autowired
    private JwtUtil jwtUtils;

    // ===== CRUD OPERATIONS =====

    /**
     * Láº¥y danh sÃ¡ch khÃ¡ch hÃ ng vá»›i phÃ¢n trang vÃ  tÃ¬m kiáº¿m
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer trangThai) {
        try {
            // Validate pagination parameters
            if (page < 0 || size <= 0 || size > 100) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Tham sá»‘ phÃ¢n trang khÃ´ng há»£p lá»‡", "INVALID_PAGINATION"));
            }

            List<KhachHang> allCustomers;
            if (search != null && !search.trim().isEmpty()) {
                allCustomers = khachHangService.searchByKeyword(search.trim());
            } else {
                allCustomers = khachHangService.getAllWithCompleteInfo();
            }

            // Apply status filter
            if (trangThai != null) {
                allCustomers = allCustomers.stream()
                        .filter(kh -> kh.getTrangThai().equals(trangThai))
                        .collect(Collectors.toList());
            }

            // Apply sorting
            sortCustomerList(allCustomers, sortBy, sortDir);

            // Create paginated response
            PageResponse<KhachHangDto> response = createPagedResponse(allCustomers, page, size);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error getting customers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi táº£i danh sÃ¡ch khÃ¡ch hÃ ng", "INTERNAL_ERROR"));
        }
    }

    /**
     * API riÃªng cho láº¥y táº¥t cáº£ (khÃ´ng phÃ¢n trang) - cho export
     */
    @GetMapping("/all")
    public ResponseEntity<List<KhachHangDto>> getAllKhachHangForExport() {
        try {
            List<KhachHangDto> dtoList = khachHangService.getAllKhachHang()
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtoList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Láº¥y khÃ¡ch hÃ ng theo ID
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCustomerById(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID khÃ´ng há»£p lá»‡", "INVALID_ID"));
            }

            Optional<KhachHang> customer = khachHangService.findByIdWithEagerLoading(id);
            if (customer.isPresent()) {
                KhachHangDto dto = convertToDto(customer.get());
                return ResponseEntity.ok(createSuccessResponse("Láº¥y thÃ´ng tin khÃ¡ch hÃ ng thÃ nh cÃ´ng", dto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng vá»›i ID: " + id, "NOT_FOUND"));
            }
        } catch (Exception e) {
            System.err.println("Error getting customer by ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi táº£i thÃ´ng tin khÃ¡ch hÃ ng", "INTERNAL_ERROR"));
        }
    }

    /**
     * Láº¥y khÃ¡ch hÃ ng theo ID tÃ i khoáº£n
     */
    @GetMapping("/tai-khoan/{taiKhoanId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCustomerByAccountId(@PathVariable Integer taiKhoanId) {
        try {
            if (taiKhoanId == null || taiKhoanId <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID tÃ i khoáº£n khÃ´ng há»£p lá»‡", "INVALID_ACCOUNT_ID"));
            }

            Optional<KhachHang> customer = khachHangService.findByTaiKhoanIdOptional(taiKhoanId);
            if (customer.isPresent()) {
                KhachHangDto dto = convertToDto(customer.get());
                return ResponseEntity.ok(createSuccessResponse("Láº¥y thÃ´ng tin khÃ¡ch hÃ ng thÃ nh cÃ´ng", dto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng vá»›i ID tÃ i khoáº£n: " + taiKhoanId, "NOT_FOUND"));
            }
        } catch (Exception e) {
            System.err.println("Error getting customer by account ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi táº£i thÃ´ng tin khÃ¡ch hÃ ng", "INTERNAL_ERROR"));
        }
    }

    /**
     * Láº¥y thÃ´ng tin khÃ¡ch hÃ ng hiá»‡n táº¡i tá»« JWT token
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentCustomer(HttpServletRequest request) {
        try {
            // 1. Láº¥y JWT token tá»« header
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Token khÃ´ng tá»“n táº¡i", "TOKEN_MISSING"));
            }

            // 2. Decode JWT Ä‘á»ƒ láº¥y email tá»« token
            String email = jwtUtils.extractEmail(token);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Token khÃ´ng há»£p lá»‡", "INVALID_TOKEN"));
            }

            // 3. TÃ¬m tÃ i khoáº£n theo email
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n", "ACCOUNT_NOT_FOUND"));
            }

            // 4. TÃ¬m khÃ¡ch hÃ ng theo tÃ i khoáº£n
            Optional<KhachHang> khachHangOpt = khachHangService.findByTaiKhoanIdOptional(taiKhoanOpt.get().getId());

            if (khachHangOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y thÃ´ng tin khÃ¡ch hÃ ng", "CUSTOMER_NOT_FOUND"));
            }

            // 5. Convert sang DTO vÃ  tráº£ vá»
            KhachHangDto customerDto = convertToDto(khachHangOpt.get());

            return ResponseEntity.ok(createSuccessResponse("Láº¥y thÃ´ng tin khÃ¡ch hÃ ng thÃ nh cÃ´ng", customerDto));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i server: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * ThÃªm khÃ¡ch hÃ ng má»›i (chá»‰ tá»« admin)
     */
    @PostMapping
    public ResponseEntity<?> createKhachHang(@RequestBody @Validated KhachHangDto dto) {
        try {
            // Validate dá»¯ liá»‡u
            Map<String, String> errors = validateKhachHangDto(dto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Dá»¯ liá»‡u khÃ´ng há»£p lá»‡", "VALIDATION_ERROR", errors));
            }

            // Kiá»ƒm tra tÃ i khoáº£n tá»“n táº¡i
            if (dto.getIdTaiKhoan() != null) {
                Optional<TaiKhoan> taiKhoan = taiKhoanService.findById(dto.getIdTaiKhoan());
                if (taiKhoan.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n vá»›i ID: " + dto.getIdTaiKhoan(), "ACCOUNT_NOT_FOUND"));
                }

                // Kiá»ƒm tra tÃ i khoáº£n Ä‘Ã£ cÃ³ khÃ¡ch hÃ ng chÆ°a
                Optional<KhachHang> existingKH = khachHangService.findByTaiKhoanIdOptional(dto.getIdTaiKhoan());
                if (existingKH.isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("TÃ i khoáº£n nÃ y Ä‘Ã£ Ä‘Æ°á»£c liÃªn káº¿t vá»›i khÃ¡ch hÃ ng khÃ¡c", "ACCOUNT_LINKED"));
                }
            }

            // Kiá»ƒm tra sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ tá»“n táº¡i
            if (khachHangService.existsBySdt(dto.getSdt())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng", "PHONE_EXISTS"));
            }

            KhachHang entity = convertToEntity(dto);
            entity.setNgayTao(new Date());
            entity.setNgayCapNhat(new Date());

            khachHangService.addKhachHang(entity);

            // Láº¥y láº¡i entity vá»«a táº¡o Ä‘á»ƒ tráº£ vá»
            Optional<KhachHang> savedOpt = khachHangService.getKhachHangById(entity.getId());
            KhachHangDto savedDto = savedOpt.map(this::convertToDto).orElse(convertToDto(entity));

            return ResponseEntity.ok(createSuccessResponse("ThÃªm khÃ¡ch hÃ ng thÃ nh cÃ´ng!", savedDto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi thÃªm khÃ¡ch hÃ ng: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * Cáº­p nháº­t thÃ´ng tin khÃ¡ch hÃ ng
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateCustomer(@PathVariable Integer id, @RequestBody @Valid KhachHangDto dto) {
        try {
            // Validate ID
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID khÃ´ng há»£p lá»‡", "INVALID_ID"));
            }

            // Check if customer exists
            Optional<KhachHang> existingOpt = khachHangService.getKhachHangById(id);
            if (existingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng vá»›i ID: " + id, "NOT_FOUND"));
            }

            // Validate DTO
            Map<String, String> validationErrors = validateCustomerDto(dto, id);
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Dá»¯ liá»‡u khÃ´ng há»£p lá»‡", "VALIDATION_ERROR", validationErrors));
            }

            KhachHang existing = existingOpt.get();

            // Check phone number conflict
            if (dto.getSdt() != null && !dto.getSdt().equals(existing.getSdt())) {
                if (khachHangService.isPhoneNumberUsed(dto.getSdt(), id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(createErrorResponse("Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng", "PHONE_EXISTS"));
                }
            }

            // Update basic info
            if (dto.getHoTen() != null && !dto.getHoTen().trim().isEmpty()) {
                existing.setHoTen(dto.getHoTen().trim());
            }
            if (dto.getSdt() != null && !dto.getSdt().trim().isEmpty()) {
                existing.setSdt(dto.getSdt().trim());
            }
            if (dto.getTrangThai() != null) {
                existing.setTrangThai(dto.getTrangThai());
            }

            // Update relationships
            updateRelationships(existing, dto);

            khachHangService.updateKhachHang(existing);

            // Return updated data
            Optional<KhachHang> updatedOpt = khachHangService.findByIdWithEagerLoading(id);
            KhachHangDto updatedDto = updatedOpt.map(this::convertToDto).orElse(null);

            return ResponseEntity.ok(createSuccessResponse("Cáº­p nháº­t khÃ¡ch hÃ ng thÃ nh cÃ´ng", updatedDto));

        } catch (Exception e) {
            System.err.println("Error updating customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi cáº­p nháº­t khÃ¡ch hÃ ng", "INTERNAL_ERROR"));
        }
    }

    /**
     * HoÃ n thiá»‡n profile khÃ¡ch hÃ ng
     */
    @PatchMapping("/{id}/complete-profile")
    @Transactional
    public ResponseEntity<?> completeProfile(@PathVariable Integer id, @RequestBody @Valid KhachHangDto profileData) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID khÃ´ng há»£p lá»‡", "INVALID_ID"));
            }

            // Validate profile completion data
            List<String> validationErrors = profileData.getProfileCompletionErrors();
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Dá»¯ liá»‡u profile khÃ´ng há»£p lá»‡", "VALIDATION_ERROR",
                                Map.of("errors", validationErrors)));
            }

            KhachHang completedCustomer = khachHangService.completeProfile(id, profileData);
            KhachHangDto dto = convertToDto(completedCustomer);

            return ResponseEntity.ok(createSuccessResponse("HoÃ n thiá»‡n profile thÃ nh cÃ´ng", dto));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage(), "BUSINESS_RULE_VIOLATION"));
        } catch (Exception e) {
            System.err.println("Error completing profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi hoÃ n thiá»‡n profile", "INTERNAL_ERROR"));
        }
    }

    /**
     * XÃ³a khÃ¡ch hÃ ng (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKhachHang(@PathVariable Integer id) {
        try {
            Optional<KhachHang> optional = khachHangService.getKhachHangById(id);
            if (optional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng vá»›i ID: " + id, "NOT_FOUND"));
            }

            // Kiá»ƒm tra xem cÃ³ thá»ƒ xÃ³a khÃ´ng
            if (!khachHangService.canDeleteKhachHang(id)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("KhÃ´ng thá»ƒ xÃ³a khÃ¡ch hÃ ng nÃ y do cÃ²n dá»¯ liá»‡u liÃªn quan", "CANNOT_DELETE"));
            }

            khachHangService.deleteKhachHang(id);
            return ResponseEntity.ok(createSuccessResponse("ÄÃ£ xÃ³a khÃ¡ch hÃ ng vá»›i id: " + id, null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi xÃ³a khÃ¡ch hÃ ng: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * XÃ³a nhiá»u khÃ¡ch hÃ ng
     */
    @DeleteMapping("/batch")
    public ResponseEntity<?> deleteMultipleKhachHang(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Danh sÃ¡ch ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng", "EMPTY_ID_LIST"));
            }

            List<Integer> deletedIds = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (Integer id : ids) {
                try {
                    Optional<KhachHang> optional = khachHangService.getKhachHangById(id);
                    if (optional.isPresent()) {
                        if (khachHangService.canDeleteKhachHang(id)) {
                            khachHangService.deleteKhachHang(id);
                            deletedIds.add(id);
                        } else {
                            errors.add("KhÃ´ng thá»ƒ xÃ³a khÃ¡ch hÃ ng ID " + id + " do cÃ²n dá»¯ liá»‡u liÃªn quan");
                        }
                    } else {
                        errors.add("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng vá»›i ID: " + id);
                    }
                } catch (Exception e) {
                    errors.add("Lá»—i khi xÃ³a khÃ¡ch hÃ ng ID " + id + ": " + e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("deletedCount", deletedIds.size());
            response.put("deletedIds", deletedIds);
            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }

            return ResponseEntity.ok(createSuccessResponse("XÃ³a khÃ¡ch hÃ ng hoÃ n táº¥t", response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi xÃ³a khÃ¡ch hÃ ng: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * Thay Ä‘á»•i tráº¡ng thÃ¡i khÃ¡ch hÃ ng
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID khÃ´ng há»£p lá»‡", "INVALID_ID"));
            }

            Integer newStatus = request.get("trangThai");
            if (newStatus == null || (newStatus != 0 && newStatus != 1)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Tráº¡ng thÃ¡i khÃ´ng há»£p lá»‡ (0 hoáº·c 1)", "INVALID_STATUS"));
            }

            Optional<KhachHang> customerOpt = khachHangService.getKhachHangById(id);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng", "NOT_FOUND"));
            }

            KhachHang customer = customerOpt.get();
            if (customer.getTrangThai().equals(newStatus)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("KhÃ¡ch hÃ ng Ä‘Ã£ á»Ÿ tráº¡ng thÃ¡i nÃ y", "SAME_STATUS"));
            }

            khachHangService.updateStatus(id, newStatus);

            // Get updated data
            Optional<KhachHang> updatedOpt = khachHangService.findByIdWithEagerLoading(id);
            KhachHangDto updatedDto = updatedOpt.map(this::convertToDto).orElse(null);

            String statusText = newStatus == 1 ? "kÃ­ch hoáº¡t" : "vÃ´ hiá»‡u hÃ³a";
            return ResponseEntity.ok(createSuccessResponse("ÄÃ£ " + statusText + " khÃ¡ch hÃ ng thÃ nh cÃ´ng", updatedDto));

        } catch (Exception e) {
            System.err.println("Error changing status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi thay Ä‘á»•i tráº¡ng thÃ¡i", "INTERNAL_ERROR"));
        }
    }

    // ===== SEARCH OPERATIONS =====

    /**
     * TÃ¬m kiáº¿m nÃ¢ng cao khÃ¡ch hÃ ng
     */
    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<?> searchCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String hoTen,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String sdt,
            @RequestParam(required = false) String maKhachHang,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            // Validate search parameters
            if (!khachHangService.isValidKhachHangSearchParams(hoTen, email, sdt)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Tham sá»‘ tÃ¬m kiáº¿m khÃ´ng há»£p lá»‡", "INVALID_SEARCH_PARAMS"));
            }

            List<KhachHang> results;

            if (keyword != null && !keyword.trim().isEmpty()) {
                // Simple keyword search
                results = khachHangService.searchByKeyword(keyword.trim());
            } else {
                // Advanced search
                results = khachHangService.searchAdvancedWithAllCriteria(
                        hoTen, email, sdt, maKhachHang, null, trangThai, startDate, endDate);
            }

            // Apply sorting
            sortCustomerList(results, sortBy, sortDir);

            // Create paginated response
            PageResponse<KhachHangDto> response = createPagedResponse(results, page, size);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error searching customers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi tÃ¬m kiáº¿m khÃ¡ch hÃ ng", "INTERNAL_ERROR"));
        }
    }

    // ===== STATISTICS =====

    /**
     * Láº¥y thá»‘ng kÃª khÃ¡ch hÃ ng
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            Map<String, Object> stats = khachHangService.getStatistics();
            return ResponseEntity.ok(createSuccessResponse("Láº¥y thá»‘ng kÃª thÃ nh cÃ´ng", stats));
        } catch (Exception e) {
            System.err.println("Error getting statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi láº¥y thá»‘ng kÃª", "INTERNAL_ERROR"));
        }
    }

    /**
     * Láº¥y khÃ¡ch hÃ ng active
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveCustomers() {
        try {
            List<KhachHang> activeCustomers = khachHangService.getActiveKhachHang();
            List<KhachHangDto> dtoList = activeCustomers.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(createSuccessResponse("Láº¥y khÃ¡ch hÃ ng hoáº¡t Ä‘á»™ng thÃ nh cÃ´ng", dtoList));
        } catch (Exception e) {
            System.err.println("Error getting active customers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi láº¥y khÃ¡ch hÃ ng hoáº¡t Ä‘á»™ng", "INTERNAL_ERROR"));
        }
    }

    /**
     * Láº¥y khÃ¡ch hÃ ng má»›i gáº§n Ä‘Ã¢y
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentCustomers(@RequestParam(defaultValue = "7") int days) {
        try {
            if (days <= 0 || days > 365) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Sá»‘ ngÃ y pháº£i trong khoáº£ng 1-365", "INVALID_DAYS"));
            }

            Date startDate = new Date(System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L));
            Date endDate = new Date();

            List<KhachHang> recentCustomers = khachHangService.getKhachHangByDateRange(startDate, endDate);
            List<KhachHangDto> dtoList = recentCustomers.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(createSuccessResponse("Láº¥y khÃ¡ch hÃ ng má»›i thÃ nh cÃ´ng", dtoList));
        } catch (Exception e) {
            System.err.println("Error getting recent customers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi láº¥y khÃ¡ch hÃ ng má»›i", "INTERNAL_ERROR"));
        }
    }

    // ===== UTILITY METHODS =====

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Map<String, String> validateKhachHangDto(KhachHangDto dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto.getHoTen() == null || dto.getHoTen().trim().isEmpty()) {
            errors.put("hoTen", "Há» tÃªn khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        if (dto.getSdt() == null || dto.getSdt().trim().isEmpty()) {
            errors.put("sdt", "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        } else if (!dto.getSdt().matches("^[0-9]{10}$")) {
            errors.put("sdt", "Sá»‘ Ä‘iá»‡n thoáº¡i pháº£i cÃ³ 10 chá»¯ sá»‘");
        }

        if (dto.getTrangThai() == null || (dto.getTrangThai() != 0 && dto.getTrangThai() != 1)) {
            errors.put("trangThai", "Tráº¡ng thÃ¡i khÃ´ng há»£p lá»‡");
        }

        return errors;
    }

    private Map<String, String> validateCustomerDto(KhachHangDto dto, Integer excludeId) {
        Map<String, String> errors = new HashMap<>();

        if (dto.getHoTen() != null && !dto.getHoTen().trim().isEmpty()) {
            if (dto.getHoTen().length() > 225) {
                errors.put("hoTen", "Há» tÃªn khÃ´ng Ä‘Æ°á»£c quÃ¡ 225 kÃ½ tá»±");
            } else if (!isValidName(dto.getHoTen())) {
                errors.put("hoTen", "Há» tÃªn chá»‰ chá»©a chá»¯ cÃ¡i vÃ  khoáº£ng tráº¯ng");
            }
        }

        if (dto.getSdt() != null && !dto.getSdt().trim().isEmpty()) {
            if (!isValidPhoneNumber(dto.getSdt())) {
                errors.put("sdt", "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng Ä‘Ãºng Ä‘á»‹nh dáº¡ng (10-11 sá»‘, báº¯t Ä‘áº§u báº±ng 0)");
            }
        }

        if (dto.getTrangThai() != null && dto.getTrangThai() != 0 && dto.getTrangThai() != 1) {
            errors.put("trangThai", "Tráº¡ng thÃ¡i pháº£i lÃ  0 hoáº·c 1");
        }

        return errors;
    }

    private boolean isValidName(String name) {
        return name != null && name.matches("^[a-zA-ZÃ€ÃÃ‚ÃƒÃˆÃ‰ÃŠÃŒÃÃ’Ã“Ã”Ã•Ã™ÃšÄ‚ÄÄ¨Å¨Æ Ã Ã¡Ã¢Ã£Ã¨Ã©ÃªÃ¬Ã­Ã²Ã³Ã´ÃµÃ¹ÃºÄƒÄ‘Ä©Å©Æ¡Æ¯Ä‚áº áº¢áº¤áº¦áº¨áºªáº¬áº®áº°áº²áº´áº¶áº¸áººáº¼á»€á»€á»‚Æ°Äƒáº¡áº£áº¥áº§áº©áº«áº­áº¯áº±áº³áºµáº·áº¹áº»áº½á»áº¿á»ƒá»„á»†á»ˆá»Šá»Œá»Žá»á»’á»”á»–á»˜á»šá»œá»žá» á»¢á»¤á»¦á»¨á»ªá»…á»‡á»‰á»‹á»á»á»‘á»“á»•á»—á»™á»›á»á»Ÿá»¡á»£á»¥á»§á»©á»«á»¬á»®á»°á»²á»´Ãá»¶á»¸á»­á»¯á»±á»³á»µÃ½á»·á»¹\\s]+$");
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^0\\d{9,10}$");
    }

    private void updateRelationships(KhachHang existing, KhachHangDto dto) {
        // Cáº­p nháº­t tÃ i khoáº£n
        if (dto.getIdTaiKhoan() != null) {
            if (existing.getTaiKhoan() == null || !dto.getIdTaiKhoan().equals(existing.getTaiKhoan().getId())) {
                Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findById(dto.getIdTaiKhoan());
                taiKhoanOpt.ifPresent(existing::setTaiKhoan);
            }
        }

        // Cáº­p nháº­t vÃ­ Ä‘iá»ƒm
        if (dto.getIdViDiem() != null) {
            if (existing.getViDiem() == null || !dto.getIdViDiem().equals(existing.getViDiem().getId())) {
                ViDiem viDiem = new ViDiem();
                viDiem.setId(dto.getIdViDiem());
                existing.setViDiem(viDiem);
            }
        }
    }

    private void sortCustomerList(List<KhachHang> list, String sortBy, String sortDir) {
        if (list == null || list.isEmpty() || sortBy == null) return;

        list.sort((a, b) -> {
            int result = 0;
            switch (sortBy.toLowerCase()) {
                case "id":
                    result = a.getId().compareTo(b.getId());
                    break;
                case "hoten":
                    result = Optional.ofNullable(a.getHoTen()).orElse("")
                            .compareToIgnoreCase(Optional.ofNullable(b.getHoTen()).orElse(""));
                    break;
                case "email":
                    String emailA = a.getEmailSafe();
                    String emailB = b.getEmailSafe();
                    result = emailA.compareToIgnoreCase(emailB);
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

    private PageResponse<KhachHangDto> createPagedResponse(List<KhachHang> customers, int page, int size) {
        int totalElements = customers.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        List<KhachHang> pagedList = startIndex < totalElements ?
                customers.subList(startIndex, endIndex) : new ArrayList<>();

        List<KhachHangDto> dtoList = pagedList.stream()
                .map(this::convertToDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageResponse<>(dtoList, totalElements, totalPages, page, size);
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

    private Map<String, Object> createErrorResponse(String message, String errorCode, Object details) {
        Map<String, Object> response = createErrorResponse(message, errorCode);
        response.put("details", details);
        return response;
    }

    private KhachHangDto convertToDto(KhachHang entity) {
        try {
            if (entity == null) {
                return null;
            }

            KhachHangDto dto = new KhachHangDto();
            dto.setId(entity.getId());
            dto.setMaKhachHang(entity.getMaKhachHang());
            dto.setHoTen(entity.getHoTen());
            dto.setSdt(entity.getSdt());
            dto.setTrangThai(entity.getTrangThai());
            dto.setNgayTao(entity.getNgayTao());
            dto.setNgayCapNhat(entity.getNgayCapNhat());

            // Láº¥y email tá»« tÃ i khoáº£n
            if (entity.getTaiKhoan() != null) {
                dto.setEmail(entity.getTaiKhoan().getEmail());
                dto.setIdTaiKhoan(entity.getTaiKhoan().getId());

                try {
                    // Láº¥y danh sÃ¡ch Ä‘á»‹a chá»‰ tá»« tÃ i khoáº£n
                    List<DiaChi> diaChiList = diaChiService.findByTaiKhoanId(entity.getTaiKhoan().getId());

                    if (diaChiList != null && !diaChiList.isEmpty()) {
                        // Convert Ä‘á»‹a chá»‰ sang DTO
                        List<KhachHangDto.DiaChiInfo> diaChiInfoList = diaChiList.stream()
                                .filter(Objects::nonNull)
                                .map(this::convertDiaChiToInfo)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        dto.setDanhSachDiaChi(diaChiInfoList);

                        // TÃ¬m Ä‘á»‹a chá»‰ máº·c Ä‘á»‹nh
                        KhachHangDto.DiaChiInfo defaultAddress = diaChiInfoList.stream()
                                .filter(dc -> dc.getIsDefault() != null && dc.getIsDefault())
                                .findFirst()
                                .orElse(diaChiInfoList.isEmpty() ? null : diaChiInfoList.get(0));

                        dto.setDiaChiMacDinh(defaultAddress);
                    } else {
                        dto.setDanhSachDiaChi(new ArrayList<>());
                        dto.setDiaChiMacDinh(null);
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Could not load addresses for customer " + entity.getId() + ": " + e.getMessage());
                    dto.setDanhSachDiaChi(new ArrayList<>());
                    dto.setDiaChiMacDinh(null);
                }
            } else {
                dto.setEmail(null);
                dto.setIdTaiKhoan(null);
                dto.setDanhSachDiaChi(new ArrayList<>());
                dto.setDiaChiMacDinh(null);
            }

            // Safely get related IDs
            dto.setIdViDiem(entity.getViDiem() != null ? entity.getViDiem().getId() : null);

            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error converting entity to DTO: " + e.getMessage());
            return null;
        }
    }

    private KhachHangDto.DiaChiInfo convertDiaChiToInfo(DiaChi diaChi) {
        try {
            if (diaChi == null) {
                return null;
            }

            KhachHangDto.DiaChiInfo info = new KhachHangDto.DiaChiInfo();
            info.setId(diaChi.getId());
            info.setMaTinh(diaChi.getMaTinh());
            info.setMaPhuong(diaChi.getMaPhuong());
            info.setTenTinh(diaChi.getTenTinh());
            info.setTenPhuong(diaChi.getTenPhuong());
            info.setDiaChiChiTiet(diaChi.getDiaChiChiTiet());
            info.setIsDefault(diaChi.getIsDefault());
            info.setTrangThai(diaChi.getTrangThai());

            // GhÃ©p Ä‘á»‹a chá»‰ Ä‘áº§y Ä‘á»§ (2-level addressing - bá» quáº­n/huyá»‡n)
            List<String> parts = new ArrayList<>();
            if (diaChi.getDiaChiChiTiet() != null && !diaChi.getDiaChiChiTiet().trim().isEmpty()) {
                parts.add(diaChi.getDiaChiChiTiet().trim());
            }
            if (diaChi.getTenPhuong() != null && !diaChi.getTenPhuong().trim().isEmpty()) {
                parts.add(diaChi.getTenPhuong().trim());
            }
            if (diaChi.getTenTinh() != null && !diaChi.getTenTinh().trim().isEmpty()) {
                parts.add(diaChi.getTenTinh().trim());
            }

            String diaChiDayDu = parts.isEmpty() ? "ChÆ°a cÃ³ Ä‘á»‹a chá»‰" : String.join(", ", parts);
            info.setDiaChiDayDu(diaChiDayDu);

            return info;
        } catch (Exception e) {
            System.err.println("Warning: Error converting DiaChi to DTO: " + e.getMessage());
            return null;
        }
    }

    private KhachHang convertToEntity(KhachHangDto dto) {
        try {
            KhachHang entity = new KhachHang();
            entity.setId(dto.getId());
            entity.setMaKhachHang(dto.getMaKhachHang());
            entity.setHoTen(dto.getHoTen());
            entity.setSdt(dto.getSdt());
            entity.setTrangThai(dto.getTrangThai());
            entity.setNgayTao(dto.getNgayTao());
            entity.setNgayCapNhat(dto.getNgayCapNhat());

            // Set relationships
            if (dto.getIdTaiKhoan() != null) {
                Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findById(dto.getIdTaiKhoan());
                if (taiKhoanOpt.isPresent()) {
                    entity.setTaiKhoan(taiKhoanOpt.get());
                }
            }

            if (dto.getIdViDiem() != null) {
                ViDiem viDiem = new ViDiem();
                viDiem.setId(dto.getIdViDiem());
                entity.setViDiem(viDiem);
            }

            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lá»—i khi convert DTO to entity: " + e.getMessage());
        }
    }

    // ===== INNER CLASSES =====

    public static class PageResponse<T> {
        private List<T> content;
        private long totalElements;
        private int totalPages;
        private int currentPage;
        private int size;

        public PageResponse() {
        }

        public PageResponse(List<T> content, long totalElements, int totalPages, int currentPage, int size) {
            this.content = content;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
            this.size = size;
        }

        // Getters and Setters
        public List<T> getContent() {
            return content;
        }

        public void setContent(List<T> content) {
            this.content = content;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
