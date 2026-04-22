package org.example.iws_websitesneaker.controller;

import jakarta.validation.Valid;
import org.example.iws_websitesneaker.Dto.NhanVienDto;
import org.example.iws_websitesneaker.Service.NhanVienService;
import org.example.iws_websitesneaker.Service.TaiKhoanService;
import org.example.iws_websitesneaker.Service.DiaChiService;
import org.example.iws_websitesneaker.entity.NhanVien;
import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.example.iws_websitesneaker.entity.DiaChi;
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
@RequestMapping("/api/nhan-vien")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class NhanVienRestController {

    @Autowired
    private NhanVienService nhanVienService;

    @Autowired
    private TaiKhoanService taiKhoanService;

    @Autowired
    private DiaChiService diaChiService;

    // ===== CRUD OPERATIONS =====

    /**
     * Láº¥y danh sÃ¡ch nhÃ¢n viÃªn vá»›i phÃ¢n trang vÃ  tÃ¬m kiáº¿m
     */
    @GetMapping
    public ResponseEntity<?> getAllEmployees(
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

            List<NhanVien> allEmployees;
            if (search != null && !search.trim().isEmpty()) {
                allEmployees = nhanVienService.searchByKeyword(search.trim());
            } else {
                allEmployees = nhanVienService.getAllNhanVien();
            }

            // Apply status filter
            if (trangThai != null) {
                allEmployees = allEmployees.stream()
                        .filter(nv -> nv.getTrangThai().equals(trangThai))
                        .collect(Collectors.toList());
            }

            // Apply sorting
            sortEmployeeList(allEmployees, sortBy, sortDir);

            // Create paginated response
            PageResponse<NhanVienDto> response = createPagedResponse(allEmployees, page, size);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error getting employees: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi táº£i danh sÃ¡ch nhÃ¢n viÃªn", "INTERNAL_ERROR"));
        }
    }

    /**
     * API riÃªng cho láº¥y táº¥t cáº£ (khÃ´ng phÃ¢n trang) - cho export
     */
    @GetMapping("/all")
    public ResponseEntity<List<NhanVienDto>> getAllNhanVienForExport() {
        try {
            List<NhanVienDto> dtoList = nhanVienService.getAllNhanVien()
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
     * Láº¥y nhÃ¢n viÃªn theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID khÃ´ng há»£p lá»‡", "INVALID_ID"));
            }

            Optional<NhanVien> employee = nhanVienService.getNhanVienById(id);
            if (employee.isPresent()) {
                NhanVienDto dto = convertToDto(employee.get());
                return ResponseEntity.ok(createSuccessResponse("Láº¥y thÃ´ng tin nhÃ¢n viÃªn thÃ nh cÃ´ng", dto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn vá»›i ID: " + id, "NOT_FOUND"));
            }
        } catch (Exception e) {
            System.err.println("Error getting employee by ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi táº£i thÃ´ng tin nhÃ¢n viÃªn", "INTERNAL_ERROR"));
        }
    }

    /**
     * Láº¥y nhÃ¢n viÃªn theo ID tÃ i khoáº£n
     */
    @GetMapping("/tai-khoan/{taiKhoanId}")
    public ResponseEntity<?> getEmployeeByAccountId(@PathVariable Integer taiKhoanId) {
        try {
            if (taiKhoanId == null || taiKhoanId <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID tÃ i khoáº£n khÃ´ng há»£p lá»‡", "INVALID_ACCOUNT_ID"));
            }

            Optional<NhanVien> employee = nhanVienService.findByTaiKhoanId(taiKhoanId);
            if (employee.isPresent()) {
                NhanVienDto dto = convertToDto(employee.get());
                return ResponseEntity.ok(createSuccessResponse("Láº¥y thÃ´ng tin nhÃ¢n viÃªn thÃ nh cÃ´ng", dto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn vá»›i ID tÃ i khoáº£n: " + taiKhoanId, "NOT_FOUND"));
            }
        } catch (Exception e) {
            System.err.println("Error getting employee by account ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi táº£i thÃ´ng tin nhÃ¢n viÃªn", "INTERNAL_ERROR"));
        }
    }

    /**
     * ThÃªm nhÃ¢n viÃªn má»›i (chá»‰ tá»« admin)
     */
    @PostMapping
    public ResponseEntity<?> createNhanVien(@RequestBody @Validated NhanVienDto dto) {
        try {
            // Log admin action
            logAdminAction("CREATE_EMPLOYEE", Map.of("data", dto));

            // Validate dá»¯ liá»‡u
            Map<String, String> errors = validateNhanVienDto(dto);
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

                // Kiá»ƒm tra tÃ i khoáº£n Ä‘Ã£ cÃ³ nhÃ¢n viÃªn chÆ°a
                Optional<NhanVien> existingNV = nhanVienService.findByTaiKhoanId(dto.getIdTaiKhoan());
                if (existingNV.isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("TÃ i khoáº£n nÃ y Ä‘Ã£ Ä‘Æ°á»£c liÃªn káº¿t vá»›i nhÃ¢n viÃªn khÃ¡c", "ACCOUNT_LINKED"));
                }
            }

            // Kiá»ƒm tra mÃ£ nhÃ¢n viÃªn Ä‘Ã£ tá»“n táº¡i
            if (dto.getMaNhanVien() != null && nhanVienService.existsByMaNhanVien(dto.getMaNhanVien())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("MÃ£ nhÃ¢n viÃªn Ä‘Ã£ tá»“n táº¡i", "EMPLOYEE_CODE_EXISTS"));
            }

            NhanVien entity = convertToEntity(dto);
            entity.setNgayTao(new Date());
            entity.setNgayCapNhat(new Date());

            nhanVienService.addNhanVien(entity);

            // Láº¥y láº¡i entity vá»«a táº¡o Ä‘á»ƒ tráº£ vá»
            Optional<NhanVien> savedOpt = nhanVienService.getNhanVienById(entity.getId());
            NhanVienDto savedDto = savedOpt.map(this::convertToDto).orElse(convertToDto(entity));

            return ResponseEntity.ok(createSuccessResponse("ThÃªm nhÃ¢n viÃªn thÃ nh cÃ´ng!", savedDto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi thÃªm nhÃ¢n viÃªn: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * Cáº­p nháº­t thÃ´ng tin nhÃ¢n viÃªn (chá»‰ cho ADMIN hoáº·c chÃ­nh nhÃ¢n viÃªn Ä‘Ã³)
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateEmployee(@PathVariable Integer id, @RequestBody @Valid NhanVienDto dto) {
        try {
            // Log admin action
            logAdminAction("UPDATE_EMPLOYEE", Map.of("id", id, "data", dto));

            // Validate ID
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID khÃ´ng há»£p lá»‡", "INVALID_ID"));
            }

            // Check if employee exists
            Optional<NhanVien> existingOpt = nhanVienService.getNhanVienById(id);
            if (existingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn vá»›i ID: " + id, "NOT_FOUND"));
            }

            // Validate DTO
            Map<String, String> validationErrors = validateEmployeeDto(dto, id);
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Dá»¯ liá»‡u khÃ´ng há»£p lá»‡", "VALIDATION_ERROR", validationErrors));
            }

            NhanVien existing = existingOpt.get();

            // SECURITY: Don't allow changing account relationship
            if (dto.getIdTaiKhoan() != null && existing.getTaiKhoan() != null &&
                    !dto.getIdTaiKhoan().equals(existing.getTaiKhoan().getId())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("KhÃ´ng Ä‘Æ°á»£c phÃ©p thay Ä‘á»•i tÃ i khoáº£n liÃªn káº¿t", "ACCOUNT_CHANGE_FORBIDDEN"));
            }

            // Check employee code conflict
            if (dto.getMaNhanVien() != null && !dto.getMaNhanVien().equals(existing.getMaNhanVien())) {
                if (nhanVienService.existsByMaNhanVien(dto.getMaNhanVien())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(createErrorResponse("MÃ£ nhÃ¢n viÃªn Ä‘Ã£ tá»“n táº¡i", "EMPLOYEE_CODE_EXISTS"));
                }
            }

            // Check phone number conflict
            if (dto.getSdt() != null && !dto.getSdt().equals(existing.getSdt())) {
                if (nhanVienService.isPhoneNumberUsed(dto.getSdt(), id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(createErrorResponse("Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng", "PHONE_EXISTS"));
                }
            }

            // Update fields
            if (dto.getMaNhanVien() != null && !dto.getMaNhanVien().trim().isEmpty()) {
                existing.setMaNhanVien(dto.getMaNhanVien().trim());
            }
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

            nhanVienService.updateNhanVien(existing);

            // Return updated data with address
            Optional<NhanVien> updatedOpt = nhanVienService.getNhanVienById(id);
            NhanVien updated = updatedOpt.orElse(existing);
            NhanVienDto updatedDto = convertToDto(updated);

            return ResponseEntity.ok(createSuccessResponse("Cáº­p nháº­t nhÃ¢n viÃªn thÃ nh cÃ´ng", updatedDto));

        } catch (Exception e) {
            System.err.println("Error updating employee: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi cáº­p nháº­t nhÃ¢n viÃªn", "INTERNAL_ERROR"));
        }
    }

    /**
     * Thay Ä‘á»•i tráº¡ng thÃ¡i nhÃ¢n viÃªn (chá»‰ ADMIN)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        try {
            // Log admin action
            logAdminAction("CHANGE_EMPLOYEE_STATUS", Map.of("id", id, "newStatus", request.get("trangThai")));

            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID khÃ´ng há»£p lá»‡", "INVALID_ID"));
            }

            Integer newStatus = request.get("trangThai");
            if (newStatus == null || (newStatus != 0 && newStatus != 1)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Tráº¡ng thÃ¡i khÃ´ng há»£p lá»‡ (0 hoáº·c 1)", "INVALID_STATUS"));
            }

            Optional<NhanVien> employeeOpt = nhanVienService.getNhanVienById(id);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn", "NOT_FOUND"));
            }

            NhanVien employee = employeeOpt.get();
            if (employee.getTrangThai().equals(newStatus)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("NhÃ¢n viÃªn Ä‘Ã£ á»Ÿ tráº¡ng thÃ¡i nÃ y", "SAME_STATUS"));
            }

            nhanVienService.toggleTrangThai(id);

            // Get updated data with address
            Optional<NhanVien> updatedOpt = nhanVienService.getNhanVienById(id);
            NhanVien updated = updatedOpt.orElse(employee);
            NhanVienDto updatedDto = convertToDto(updated);

            String statusText = newStatus == 1 ? "kÃ­ch hoáº¡t" : "cho nghá»‰ viá»‡c";
            return ResponseEntity.ok(createSuccessResponse("ÄÃ£ " + statusText + " nhÃ¢n viÃªn thÃ nh cÃ´ng", updatedDto));

        } catch (Exception e) {
            System.err.println("Error changing employee status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi thay Ä‘á»•i tráº¡ng thÃ¡i", "INTERNAL_ERROR"));
        }
    }

    /**
     * XÃ³a nhÃ¢n viÃªn (soft delete - chá»‰ ADMIN)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Integer id) {
        try {
            // Log admin action
            logAdminAction("DELETE_EMPLOYEE", Map.of("id", id));

            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID khÃ´ng há»£p lá»‡", "INVALID_ID"));
            }

            Optional<NhanVien> employeeOpt = nhanVienService.getNhanVienById(id);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn", "NOT_FOUND"));
            }

            NhanVien employee = employeeOpt.get();
            if (employee.getTrangThai() == 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("NhÃ¢n viÃªn Ä‘Ã£ nghá»‰ viá»‡c", "ALREADY_INACTIVE"));
            }

            if (!nhanVienService.canDeleteNhanVien(id)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("KhÃ´ng thá»ƒ cho nghá»‰ viá»‡c nhÃ¢n viÃªn nÃ y do cÃ²n dá»¯ liá»‡u liÃªn quan", "DELETE_FORBIDDEN"));
            }

            nhanVienService.deleteNhanVien(id); // Soft delete

            return ResponseEntity.ok(createSuccessResponse("ÄÃ£ cho nhÃ¢n viÃªn nghá»‰ viá»‡c thÃ nh cÃ´ng", null));

        } catch (Exception e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi xÃ³a nhÃ¢n viÃªn", "INTERNAL_ERROR"));
        }
    }

    /**
     * XÃ³a nhiá»u nhÃ¢n viÃªn (batch delete - chá»‰ ADMIN)
     */
    @DeleteMapping("/batch")
    public ResponseEntity<?> deleteMultipleEmployees(@RequestBody List<Integer> ids) {
        try {
            // Log admin action
            logAdminAction("BATCH_DELETE_EMPLOYEES", Map.of("ids", ids));

            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Danh sÃ¡ch ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng", "EMPTY_ID_LIST"));
            }

            List<Integer> successIds = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (Integer id : ids) {
                try {
                    Optional<NhanVien> employeeOpt = nhanVienService.getNhanVienById(id);
                    if (employeeOpt.isPresent()) {
                        NhanVien employee = employeeOpt.get();
                        if (employee.getTrangThai() == 0) {
                            errors.add("NhÃ¢n viÃªn ID " + id + " Ä‘Ã£ nghá»‰ viá»‡c");
                            continue;
                        }

                        if (nhanVienService.canDeleteNhanVien(id)) {
                            nhanVienService.deleteNhanVien(id);
                            successIds.add(id);
                        } else {
                            errors.add("KhÃ´ng thá»ƒ cho nghá»‰ viá»‡c nhÃ¢n viÃªn ID " + id + " do cÃ²n dá»¯ liá»‡u liÃªn quan");
                        }
                    } else {
                        errors.add("KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn vá»›i ID: " + id);
                    }
                } catch (Exception e) {
                    errors.add("Lá»—i khi xá»­ lÃ½ nhÃ¢n viÃªn ID " + id + ": " + e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successIds.size());
            result.put("successIds", successIds);
            result.put("message", "ÄÃ£ cho " + successIds.size() + " nhÃ¢n viÃªn nghá»‰ viá»‡c");
            if (!errors.isEmpty()) {
                result.put("errors", errors);
            }

            return ResponseEntity.ok(createSuccessResponse("XÃ³a nhÃ¢n viÃªn hoÃ n táº¥t", result));

        } catch (Exception e) {
            System.err.println("Error batch deleting employees: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi xÃ³a nhÃ¢n viÃªn", "INTERNAL_ERROR"));
        }
    }

    // ===== SEARCH OPERATIONS =====

    /**
     * TÃ¬m kiáº¿m nÃ¢ng cao nhÃ¢n viÃªn
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String hoTen,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String sdt,
            @RequestParam(required = false) String maNhanVien,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            // Validate search parameters
            if (!nhanVienService.isValidNhanVienSearchParams(hoTen, email, sdt, maNhanVien)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Tham sá»‘ tÃ¬m kiáº¿m khÃ´ng há»£p lá»‡", "INVALID_SEARCH_PARAMS"));
            }

            List<NhanVien> results;

            if (keyword != null && !keyword.trim().isEmpty()) {
                // Simple keyword search
                results = nhanVienService.searchByKeyword(keyword.trim());
            } else {
                // Advanced search
                results = nhanVienService.searchAdvancedWithAllCriteria(
                        hoTen, email, sdt, maNhanVien, null, trangThai, startDate, endDate);
            }

            // Apply sorting
            sortEmployeeList(results, sortBy, sortDir);

            // Create paginated response
            PageResponse<NhanVienDto> response = createPagedResponse(results, page, size);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error searching employees: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi tÃ¬m kiáº¿m nhÃ¢n viÃªn", "INTERNAL_ERROR"));
        }
    }

    // ===== ADMIN DASHBOARD OPERATIONS =====

    /**
     * Láº¥y thá»‘ng kÃª dashboard cho ADMIN
     */
    @GetMapping("/admin/dashboard-stats")
    public ResponseEntity<?> getAdminDashboardStats() {
        try {
            Map<String, Object> stats = nhanVienService.getAdminDashboardStats();
            return ResponseEntity.ok(createSuccessResponse("Láº¥y thá»‘ng kÃª dashboard thÃ nh cÃ´ng", stats));
        } catch (Exception e) {
            System.err.println("Error getting admin dashboard stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi láº¥y thá»‘ng kÃª dashboard", "INTERNAL_ERROR"));
        }
    }

    /**
     * Láº¥y hoáº¡t Ä‘á»™ng gáº§n Ä‘Ã¢y cho ADMIN
     */
    @GetMapping("/admin/recent-activities")
    public ResponseEntity<?> getRecentActivities() {
        try {
            List<Map<String, Object>> activities = nhanVienService.getRecentActivities();
            return ResponseEntity.ok(createSuccessResponse("Láº¥y hoáº¡t Ä‘á»™ng gáº§n Ä‘Ã¢y thÃ nh cÃ´ng", activities));
        } catch (Exception e) {
            System.err.println("Error getting recent activities: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi láº¥y hoáº¡t Ä‘á»™ng gáº§n Ä‘Ã¢y", "INTERNAL_ERROR"));
        }
    }

    /**
     * Láº¥y nhÃ¢n viÃªn theo bá»™ lá»c nhanh
     */
    @GetMapping("/admin/quick-filters")
    public ResponseEntity<?> getQuickFilters(
            @RequestParam String filterType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<NhanVien> filteredEmployees = new ArrayList<>();

            switch (filterType.toLowerCase()) {
                case "new7days":
                    filteredEmployees = nhanVienService.getNewEmployees(7);
                    break;
                case "needsreview":
                    filteredEmployees = nhanVienService.getEmployeesNeedingReview();
                    break;
                case "recentactivity":
                    filteredEmployees = nhanVienService.getRecentlyActiveEmployees();
                    break;
                case "active":
                    filteredEmployees = nhanVienService.getActiveNhanVien();
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Loáº¡i filter khÃ´ng há»£p lá»‡", "INVALID_FILTER_TYPE"));
            }

            PageResponse<NhanVienDto> response = createPagedResponse(filteredEmployees, page, size);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error in quick filters: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi táº£i bá»™ lá»c nhanh", "INTERNAL_ERROR"));
        }
    }

    // ===== STATISTICS =====

    /**
     * Láº¥y thá»‘ng kÃª nhÃ¢n viÃªn
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            Map<String, Long> stats = nhanVienService.getEmployeeStatistics();
            return ResponseEntity.ok(createSuccessResponse("Láº¥y thá»‘ng kÃª thÃ nh cÃ´ng", stats));
        } catch (Exception e) {
            System.err.println("Error getting statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi láº¥y thá»‘ng kÃª", "INTERNAL_ERROR"));
        }
    }

    /**
     * Láº¥y nhÃ¢n viÃªn active
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveEmployees() {
        try {
            List<NhanVien> activeEmployees = nhanVienService.getActiveNhanVien();
            List<NhanVienDto> activeEmployeeDtos = activeEmployees.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(createSuccessResponse("Láº¥y nhÃ¢n viÃªn hoáº¡t Ä‘á»™ng thÃ nh cÃ´ng", activeEmployeeDtos));
        } catch (Exception e) {
            System.err.println("Error getting active employees: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi láº¥y nhÃ¢n viÃªn hoáº¡t Ä‘á»™ng", "INTERNAL_ERROR"));
        }
    }

    /**
     * Láº¥y nhÃ¢n viÃªn má»›i gáº§n Ä‘Ã¢y
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentEmployees(@RequestParam(defaultValue = "7") int days) {
        try {
            if (days <= 0 || days > 365) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Sá»‘ ngÃ y pháº£i trong khoáº£ng 1-365", "INVALID_DAYS"));
            }

            List<NhanVien> recentEmployees = nhanVienService.getNewEmployees(days);
            List<NhanVienDto> recentEmployeeDtos = recentEmployees.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(createSuccessResponse("Láº¥y nhÃ¢n viÃªn má»›i thÃ nh cÃ´ng", recentEmployeeDtos));
        } catch (Exception e) {
            System.err.println("Error getting recent employees: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lá»—i khi láº¥y nhÃ¢n viÃªn má»›i", "INTERNAL_ERROR"));
        }
    }

    // ===== UTILITY METHODS =====

    private void logAdminAction(String action, Object data) {
        System.out.println("ADMIN ACTION: " + action);
        System.out.println("Time: " + new Date());
        System.out.println("Data: " + data);
        // TODO: Save to database or log file for audit trail
    }

    private Map<String, String> validateNhanVienDto(NhanVienDto dto) {
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

    private Map<String, String> validateEmployeeDto(NhanVienDto dto, Integer excludeId) {
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

        if (dto.getMaNhanVien() != null && !dto.getMaNhanVien().trim().isEmpty()) {
            if (dto.getMaNhanVien().length() > 25) {
                errors.put("maNhanVien", "MÃ£ nhÃ¢n viÃªn khÃ´ng Ä‘Æ°á»£c quÃ¡ 25 kÃ½ tá»±");
            } else if (!dto.getMaNhanVien().matches("^[A-Za-z0-9]+$")) {
                errors.put("maNhanVien", "MÃ£ nhÃ¢n viÃªn chá»‰ chá»©a chá»¯ cÃ¡i vÃ  sá»‘");
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

    private void updateRelationships(NhanVien existing, NhanVienDto dto) {
        // Cáº­p nháº­t tÃ i khoáº£n
        if (dto.getIdTaiKhoan() != null) {
            if (existing.getTaiKhoan() == null || !dto.getIdTaiKhoan().equals(existing.getTaiKhoan().getId())) {
                Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findById(dto.getIdTaiKhoan());
                taiKhoanOpt.ifPresent(existing::setTaiKhoan);
            }
        }
    }

    private void sortEmployeeList(List<NhanVien> list, String sortBy, String sortDir) {
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
                case "manhanvien":
                    result = Optional.ofNullable(a.getMaNhanVien()).orElse("")
                            .compareToIgnoreCase(Optional.ofNullable(b.getMaNhanVien()).orElse(""));
                    break;
                default:
                    result = a.getId().compareTo(b.getId());
            }
            return "desc".equalsIgnoreCase(sortDir) ? -result : result;
        });
    }

    private PageResponse<NhanVienDto> createPagedResponse(List<NhanVien> employees, int page, int size) {
        int totalElements = employees.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        List<NhanVien> pagedList = startIndex < totalElements ?
                employees.subList(startIndex, endIndex) : new ArrayList<>();

        List<NhanVienDto> dtoList = pagedList.stream()
                .map(this::convertToDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageResponse<>(dtoList, totalElements, totalPages, page, size);
    }

    private NhanVienDto convertToDto(NhanVien entity) {
        if (entity == null) return null;

        try {
            NhanVienDto dto = new NhanVienDto();
            dto.setId(entity.getId());
            dto.setMaNhanVien(entity.getMaNhanVien());
            dto.setHoTen(entity.getHoTen());
            dto.setSdt(entity.getSdt());
            dto.setTrangThai(entity.getTrangThai());
            dto.setNgayTao(entity.getNgayTao());
            dto.setNgayCapNhat(entity.getNgayCapNhat());

            if (entity.getTaiKhoan() != null) {
                dto.setEmail(entity.getEmailSafe());
                dto.setIdTaiKhoan(entity.getTaiKhoan().getId());

                try {
                    // Láº¥y thÃ´ng tin Ä‘á»‹a chá»‰
                    List<DiaChi> addresses = diaChiService.findByTaiKhoanId(entity.getTaiKhoan().getId());
                    if (!addresses.isEmpty()) {
                        // Convert Ä‘á»‹a chá»‰ sang DTO format
                        List<NhanVienDto.DiaChiInfo> diaChiInfos = addresses.stream()
                                .filter(Objects::nonNull)
                                .map(this::convertAddressToInfo)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        dto.setDanhSachDiaChi(diaChiInfos);

                        // Set Ä‘á»‹a chá»‰ máº·c Ä‘á»‹nh
                        NhanVienDto.DiaChiInfo defaultAddr = diaChiInfos.stream()
                                .filter(addr -> addr.getIsDefault() != null && addr.getIsDefault())
                                .findFirst()
                                .orElse(diaChiInfos.isEmpty() ? null : diaChiInfos.get(0));
                        dto.setDiaChiMacDinh(defaultAddr);
                    } else {
                        dto.setDanhSachDiaChi(new ArrayList<>());
                        dto.setDiaChiMacDinh(null);
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Could not load addresses for employee " + entity.getId() + ": " + e.getMessage());
                    dto.setDanhSachDiaChi(new ArrayList<>());
                    dto.setDiaChiMacDinh(null);
                }
            } else {
                dto.setEmail(null);
                dto.setIdTaiKhoan(null);
                dto.setDanhSachDiaChi(new ArrayList<>());
                dto.setDiaChiMacDinh(null);
            }

            return dto;
        } catch (Exception e) {
            System.err.println("Error converting employee to DTO: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private NhanVienDto.DiaChiInfo convertAddressToInfo(DiaChi diaChi) {
        if (diaChi == null) return null;

        try {
            NhanVienDto.DiaChiInfo info = new NhanVienDto.DiaChiInfo();
            info.setId(diaChi.getId());
            info.setMaTinh(diaChi.getMaTinh());
            info.setMaPhuong(diaChi.getMaPhuong());
            info.setTenTinh(diaChi.getTenTinh());
            info.setTenPhuong(diaChi.getTenPhuong());
            info.setDiaChiChiTiet(diaChi.getDiaChiChiTiet());
            info.setIsDefault(diaChi.getIsDefault());
            info.setTrangThai(diaChi.getTrangThai());

            // Táº¡o Ä‘á»‹a chá»‰ Ä‘áº§y Ä‘á»§ (2-level addressing - bá» quáº­n/huyá»‡n)
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

            String fullAddressStr = parts.isEmpty() ? "ChÆ°a cÃ³ Ä‘á»‹a chá»‰" : String.join(", ", parts);
            info.setDiaChiDayDu(fullAddressStr);

            return info;
        } catch (Exception e) {
            System.err.println("Warning: Error converting address to DTO: " + e.getMessage());
            return null;
        }
    }

    private NhanVien convertToEntity(NhanVienDto dto) {
        try {
            NhanVien entity = new NhanVien();
            entity.setId(dto.getId());
            entity.setMaNhanVien(dto.getMaNhanVien());
            entity.setHoTen(dto.getHoTen());
            entity.setSdt(dto.getSdt());
            entity.setTrangThai(dto.getTrangThai());
            entity.setNgayTao(dto.getNgayTao());
            entity.setNgayCapNhat(dto.getNgayCapNhat());

            // Set tÃ i khoáº£n
            if (dto.getIdTaiKhoan() != null) {
                Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findById(dto.getIdTaiKhoan());
                if (taiKhoanOpt.isPresent()) {
                    entity.setTaiKhoan(taiKhoanOpt.get());
                } else {
                    // Táº¡o reference object náº¿u khÃ´ng tÃ¬m tháº¥y
                    TaiKhoan taiKhoan = new TaiKhoan();
                    taiKhoan.setId(dto.getIdTaiKhoan());
                    entity.setTaiKhoan(taiKhoan);
                }
            }

            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lá»—i khi convert DTO to entity: " + e.getMessage());
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

    private Map<String, Object> createErrorResponse(String message, String errorCode, Object details) {
        Map<String, Object> response = createErrorResponse(message, errorCode);
        response.put("details", details);
        return response;
    }

    // ===== INNER CLASSES =====

    public static class PageResponse<T> {
        private List<T> content;
        private long totalElements;
        private int totalPages;
        private int currentPage;
        private int size;

        public PageResponse() {}

        public PageResponse(List<T> content, long totalElements, int totalPages, int currentPage, int size) {
            this.content = content;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
            this.size = size;
        }

        // Getters and Setters
        public List<T> getContent() { return content; }
        public void setContent(List<T> content) { this.content = content; }
        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }
}
