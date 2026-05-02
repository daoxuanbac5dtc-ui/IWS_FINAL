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
     * Lấy danh sách khách hàng với phân trang và tìm kiếm
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
                        .body(createErrorResponse("Tham số phân trang không hợp lệ", "INVALID_PAGINATION"));
            }

            List<KhachHang> allCustomers;
            if (search != null && !search.trim().isEmpty()) {
                allCustomers = khachHangService.searchByKeyword(search.trim());
            } else {
                allCustomers = khachHangService.getAllKhachHang();
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
                    .body(createErrorResponse("Lỗi khi tải danh sách khách hàng", "INTERNAL_ERROR"));
        }
    }

    /**
     * API riêng cho lấy tất cả (không phân trang) - cho export
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
     * Lấy khách hàng theo ID
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCustomerById(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID không hợp lệ", "INVALID_ID"));
            }

            Optional<KhachHang> customer = khachHangService.findByIdWithEagerLoading(id);
            if (customer.isPresent()) {
                KhachHangDto dto = convertToDto(customer.get());
                return ResponseEntity.ok(createSuccessResponse("Lấy thông tin khách hàng thành công", dto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Không tìm thấy khách hàng với ID: " + id, "NOT_FOUND"));
            }
        } catch (Exception e) {
            System.err.println("Error getting customer by ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi tải thông tin khách hàng", "INTERNAL_ERROR"));
        }
    }

    /**
     * Lấy khách hàng theo ID tài khoản
     */
    @GetMapping("/tai-khoan/{taiKhoanId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCustomerByAccountId(@PathVariable Integer taiKhoanId) {
        try {
            if (taiKhoanId == null || taiKhoanId <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID tài khoản không hợp lệ", "INVALID_ACCOUNT_ID"));
            }

            Optional<KhachHang> customer = khachHangService.findByTaiKhoanIdOptional(taiKhoanId);
            if (customer.isPresent()) {
                KhachHangDto dto = convertToDto(customer.get());
                return ResponseEntity.ok(createSuccessResponse("Lấy thông tin khách hàng thành công", dto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Không tìm thấy khách hàng với ID tài khoản: " + taiKhoanId, "NOT_FOUND"));
            }
        } catch (Exception e) {
            System.err.println("Error getting customer by account ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi tải thông tin khách hàng", "INTERNAL_ERROR"));
        }
    }

    /**
     * Lấy thông tin khách hàng hiện tại từ JWT token
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentCustomer(HttpServletRequest request) {
        try {
            // 1. Lấy JWT token từ header
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Token không tồn tại", "TOKEN_MISSING"));
            }

            // 2. Decode JWT để lấy email từ token
            String email = jwtUtils.extractEmail(token);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Token không hợp lệ", "INVALID_TOKEN"));
            }

            // 3. Tìm tài khoản theo email
            Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findByEmail(email);
            if (taiKhoanOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Không tìm thấy tài khoản", "ACCOUNT_NOT_FOUND"));
            }

            // 4. Tìm khách hàng theo tài khoản
            Optional<KhachHang> khachHangOpt = khachHangService.findByTaiKhoanIdOptional(taiKhoanOpt.get().getId());

            if (khachHangOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Không tìm thấy thông tin khách hàng", "CUSTOMER_NOT_FOUND"));
            }

            // 5. Convert sang DTO và trả về
            KhachHangDto customerDto = convertToDto(khachHangOpt.get());

            return ResponseEntity.ok(createSuccessResponse("Lấy thông tin khách hàng thành công", customerDto));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi server: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * Thêm khách hàng mới (chỉ từ admin)
     */
    @PostMapping
    public ResponseEntity<?> createKhachHang(@RequestBody @Validated KhachHangDto dto) {
        try {
            // Validate dữ liệu
            Map<String, String> errors = validateKhachHangDto(dto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Dữ liệu không hợp lệ", "VALIDATION_ERROR", errors));
            }

            // Kiểm tra tài khoản tồn tại
            if (dto.getIdTaiKhoan() != null) {
                Optional<TaiKhoan> taiKhoan = taiKhoanService.findById(dto.getIdTaiKhoan());
                if (taiKhoan.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Không tìm thấy tài khoản với ID: " + dto.getIdTaiKhoan(), "ACCOUNT_NOT_FOUND"));
                }

                // Kiểm tra tài khoản đã có khách hàng chưa
                Optional<KhachHang> existingKH = khachHangService.findByTaiKhoanIdOptional(dto.getIdTaiKhoan());
                if (existingKH.isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Tài khoản này đã được liên kết với khách hàng khác", "ACCOUNT_LINKED"));
                }
            }

            // Kiểm tra số điện thoại đã tồn tại
            if (khachHangService.existsBySdt(dto.getSdt())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Số điện thoại đã được sử dụng", "PHONE_EXISTS"));
            }

            KhachHang entity = convertToEntity(dto);
            entity.setNgayTao(new Date());
            entity.setNgayCapNhat(new Date());

            khachHangService.addKhachHang(entity);

            // Lấy lại entity vừa tạo để trả về
            Optional<KhachHang> savedOpt = khachHangService.getKhachHangById(entity.getId());
            KhachHangDto savedDto = savedOpt.map(this::convertToDto).orElse(convertToDto(entity));

            return ResponseEntity.ok(createSuccessResponse("Thêm khách hàng thành công!", savedDto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi thêm khách hàng: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * Cập nhật thông tin khách hàng
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateCustomer(@PathVariable Integer id, @RequestBody @Valid KhachHangDto dto) {
        try {
            // Validate ID
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID không hợp lệ", "INVALID_ID"));
            }

            // Check if customer exists
            Optional<KhachHang> existingOpt = khachHangService.getKhachHangById(id);
            if (existingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Không tìm thấy khách hàng với ID: " + id, "NOT_FOUND"));
            }

            // Validate DTO
            Map<String, String> validationErrors = validateCustomerDto(dto, id);
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Dữ liệu không hợp lệ", "VALIDATION_ERROR", validationErrors));
            }

            KhachHang existing = existingOpt.get();

            // Check phone number conflict
            if (dto.getSdt() != null && !dto.getSdt().equals(existing.getSdt())) {
                if (khachHangService.isPhoneNumberUsed(dto.getSdt(), id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(createErrorResponse("Số điện thoại đã được sử dụng", "PHONE_EXISTS"));
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

            return ResponseEntity.ok(createSuccessResponse("Cập nhật khách hàng thành công", updatedDto));

        } catch (Exception e) {
            System.err.println("Error updating customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi cập nhật khách hàng", "INTERNAL_ERROR"));
        }
    }

    /**
     * Hoàn thiện profile khách hàng
     */
    @PatchMapping("/{id}/complete-profile")
    @Transactional
    public ResponseEntity<?> completeProfile(@PathVariable Integer id, @RequestBody @Valid KhachHangDto profileData) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID không hợp lệ", "INVALID_ID"));
            }

            // Validate profile completion data
            List<String> validationErrors = profileData.getProfileCompletionErrors();
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Dữ liệu profile không hợp lệ", "VALIDATION_ERROR",
                                Map.of("errors", validationErrors)));
            }

            KhachHang completedCustomer = khachHangService.completeProfile(id, profileData);
            KhachHangDto dto = convertToDto(completedCustomer);

            return ResponseEntity.ok(createSuccessResponse("Hoàn thiện profile thành công", dto));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage(), "BUSINESS_RULE_VIOLATION"));
        } catch (Exception e) {
            System.err.println("Error completing profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi hoàn thiện profile", "INTERNAL_ERROR"));
        }
    }

    /**
     * Xóa khách hàng (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKhachHang(@PathVariable Integer id) {
        try {
            Optional<KhachHang> optional = khachHangService.getKhachHangById(id);
            if (optional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Không tìm thấy khách hàng với ID: " + id, "NOT_FOUND"));
            }

            // Kiểm tra xem có thể xóa không
            if (!khachHangService.canDeleteKhachHang(id)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Không thể xóa khách hàng này do còn dữ liệu liên quan", "CANNOT_DELETE"));
            }

            khachHangService.deleteKhachHang(id);
            return ResponseEntity.ok(createSuccessResponse("Đã xóa khách hàng với id: " + id, null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi xóa khách hàng: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * Xóa nhiều khách hàng
     */
    @DeleteMapping("/batch")
    public ResponseEntity<?> deleteMultipleKhachHang(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Danh sách ID không được để trống", "EMPTY_ID_LIST"));
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
                            errors.add("Không thể xóa khách hàng ID " + id + " do còn dữ liệu liên quan");
                        }
                    } else {
                        errors.add("Không tìm thấy khách hàng với ID: " + id);
                    }
                } catch (Exception e) {
                    errors.add("Lỗi khi xóa khách hàng ID " + id + ": " + e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("deletedCount", deletedIds.size());
            response.put("deletedIds", deletedIds);
            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }

            return ResponseEntity.ok(createSuccessResponse("Xóa khách hàng hoàn tất", response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi xóa khách hàng: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * Thay đổi trạng thái khách hàng
     */
    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> changeStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID không hợp lệ", "INVALID_ID"));
            }

            Integer newStatus = request.get("trangThai");
            if (newStatus == null || (newStatus != 0 && newStatus != 1)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Trạng thái không hợp lệ (0 hoặc 1)", "INVALID_STATUS"));
            }

            Optional<KhachHang> customerOpt = khachHangService.findByIdWithEagerLoading(id);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Không tìm thấy khách hàng", "NOT_FOUND"));
            }

            KhachHang customer = customerOpt.get();
            if (Objects.equals(customer.getTrangThai(), newStatus)) {
                syncLinkedAccountStatus(customer, newStatus);
                KhachHangDto sameStatusDto = customerOpt.map(this::convertToDto).orElse(null);
                return ResponseEntity.ok(createSuccessResponse("Trạng thái khách hàng đã được đồng bộ", sameStatusDto));
            }

            khachHangService.updateStatus(id, newStatus);
            syncLinkedAccountStatus(customer, newStatus);

            Optional<KhachHang> updatedOpt = khachHangService.findByIdWithEagerLoading(id);
            KhachHangDto updatedDto = updatedOpt.map(this::convertToDto).orElse(null);

            String statusText = newStatus == 1 ? "kích hoạt" : "vô hiệu hóa";
            return ResponseEntity.ok(createSuccessResponse("Đã " + statusText + " khách hàng thành công", updatedDto));

        } catch (Exception e) {
            System.err.println("Error changing status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi thay đổi trạng thái", "INTERNAL_ERROR"));
        }
    }

    private void syncLinkedAccountStatus(KhachHang customer, Integer newStatus) {
        if (customer == null || customer.getTaiKhoan() == null || newStatus == null) {
            return;
        }

        TaiKhoan linkedAccount = customer.getTaiKhoan();
        if (!Objects.equals(linkedAccount.getTrangThai(), newStatus)) {
            linkedAccount.setTrangThai(newStatus);
            taiKhoanService.save(linkedAccount);
        }
    }    // ===== SEARCH OPERATIONS =====

    /**
     * Tìm kiếm nâng cao khách hàng
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
                        .body(createErrorResponse("Tham số tìm kiếm không hợp lệ", "INVALID_SEARCH_PARAMS"));
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
                    .body(createErrorResponse("Lỗi khi tìm kiếm khách hàng", "INTERNAL_ERROR"));
        }
    }

    // ===== STATISTICS =====

    /**
     * Lấy thống kê khách hàng
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            Map<String, Object> stats = khachHangService.getStatistics();
            return ResponseEntity.ok(createSuccessResponse("Lấy thống kê thành công", stats));
        } catch (Exception e) {
            System.err.println("Error getting statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi lấy thống kê", "INTERNAL_ERROR"));
        }
    }

    /**
     * Lấy khách hàng active
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveCustomers() {
        try {
            List<KhachHang> activeCustomers = khachHangService.getActiveKhachHang();
            List<KhachHangDto> dtoList = activeCustomers.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(createSuccessResponse("Lấy khách hàng hoạt động thành công", dtoList));
        } catch (Exception e) {
            System.err.println("Error getting active customers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi lấy khách hàng hoạt động", "INTERNAL_ERROR"));
        }
    }

    /**
     * Lấy khách hàng mới gần đây
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentCustomers(@RequestParam(defaultValue = "7") int days) {
        try {
            if (days <= 0 || days > 365) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Số ngày phải trong khoảng 1-365", "INVALID_DAYS"));
            }

            Date startDate = new Date(System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L));
            Date endDate = new Date();

            List<KhachHang> recentCustomers = khachHangService.getKhachHangByDateRange(startDate, endDate);
            List<KhachHangDto> dtoList = recentCustomers.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(createSuccessResponse("Lấy khách hàng mới thành công", dtoList));
        } catch (Exception e) {
            System.err.println("Error getting recent customers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi lấy khách hàng mới", "INTERNAL_ERROR"));
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
            errors.put("hoTen", "Họ tên không được để trống");
        }

        if (dto.getSdt() == null || dto.getSdt().trim().isEmpty()) {
            errors.put("sdt", "Số điện thoại không được để trống");
        } else if (!dto.getSdt().matches("^[0-9]{10}$")) {
            errors.put("sdt", "Số điện thoại phải có 10 chữ số");
        }

        if (dto.getTrangThai() == null || (dto.getTrangThai() != 0 && dto.getTrangThai() != 1)) {
            errors.put("trangThai", "Trạng thái không hợp lệ");
        }

        return errors;
    }

    private Map<String, String> validateCustomerDto(KhachHangDto dto, Integer excludeId) {
        Map<String, String> errors = new HashMap<>();

        if (dto.getHoTen() != null && !dto.getHoTen().trim().isEmpty()) {
            if (dto.getHoTen().length() > 225) {
                errors.put("hoTen", "Họ tên không được quá 225 ký tự");
            } else if (!isValidName(dto.getHoTen())) {
                errors.put("hoTen", "Họ tên chỉ chứa chữ cái và khoảng trắng");
            }
        }

        if (dto.getSdt() != null && !dto.getSdt().trim().isEmpty()) {
            if (!isValidPhoneNumber(dto.getSdt())) {
                errors.put("sdt", "Số điện thoại không đúng định dạng (10-11 số, bắt đầu bằng 0)");
            }
        }

        if (dto.getTrangThai() != null && dto.getTrangThai() != 0 && dto.getTrangThai() != 1) {
            errors.put("trangThai", "Trạng thái phải là 0 hoặc 1");
        }

        return errors;
    }

    private boolean isValidName(String name) {
        return name != null && name.matches("^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽềếểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵýỷỹ\\s]+$");
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^0\\d{9,10}$");
    }

    private void updateRelationships(KhachHang existing, KhachHangDto dto) {
        // Cập nhật tài khoản
        if (dto.getIdTaiKhoan() != null) {
            if (existing.getTaiKhoan() == null || !dto.getIdTaiKhoan().equals(existing.getTaiKhoan().getId())) {
                Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findById(dto.getIdTaiKhoan());
                taiKhoanOpt.ifPresent(existing::setTaiKhoan);
            }
        }

        // Cập nhật ví điểm
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

            // Lấy email từ tài khoản
            if (entity.getTaiKhoan() != null) {
                dto.setEmail(entity.getTaiKhoan().getEmail());
                dto.setIdTaiKhoan(entity.getTaiKhoan().getId());

                try {
                    // Lấy danh sách địa chỉ từ tài khoản
                    List<DiaChi> diaChiList = diaChiService.findByTaiKhoanId(entity.getTaiKhoan().getId());

                    if (diaChiList != null && !diaChiList.isEmpty()) {
                        // Convert địa chỉ sang DTO
                        List<KhachHangDto.DiaChiInfo> diaChiInfoList = diaChiList.stream()
                                .filter(Objects::nonNull)
                                .map(this::convertDiaChiToInfo)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        dto.setDanhSachDiaChi(diaChiInfoList);

                        // Tìm địa chỉ mặc định
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

            // Ghép địa chỉ đầy đủ (2-level addressing - bỏ quận/huyện)
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

            String diaChiDayDu = parts.isEmpty() ? "Chưa có địa chỉ" : String.join(", ", parts);
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
            throw new RuntimeException("Lỗi khi convert DTO to entity: " + e.getMessage());
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
