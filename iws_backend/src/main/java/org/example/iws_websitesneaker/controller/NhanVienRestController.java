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
     * Lấy danh sách nhân viên với phân trang và tìm kiếm
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
                        .body(createErrorResponse("Tham số phân trang không hợp lệ", "INVALID_PAGINATION"));
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
                    .body(createErrorResponse("Lỗi khi tải danh sách nhân viên", "INTERNAL_ERROR"));
        }
    }

    /**
     * API riêng cho lấy tất cả (không phân trang) - cho export
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
     * Lấy nhân viên theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID không hợp lệ", "INVALID_ID"));
            }

            Optional<NhanVien> employee = nhanVienService.getNhanVienById(id);
            if (employee.isPresent()) {
                NhanVienDto dto = convertToDto(employee.get());
                return ResponseEntity.ok(createSuccessResponse("Lấy thông tin nhân viên thành công", dto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Không tìm thấy nhân viên với ID: " + id, "NOT_FOUND"));
            }
        } catch (Exception e) {
            System.err.println("Error getting employee by ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi tải thông tin nhân viên", "INTERNAL_ERROR"));
        }
    }

    /**
     * Lấy nhân viên theo ID tài khoản
     */
    @GetMapping("/tai-khoan/{taiKhoanId}")
    public ResponseEntity<?> getEmployeeByAccountId(@PathVariable Integer taiKhoanId) {
        try {
            if (taiKhoanId == null || taiKhoanId <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID tài khoản không hợp lệ", "INVALID_ACCOUNT_ID"));
            }

            Optional<NhanVien> employee = nhanVienService.findByTaiKhoanId(taiKhoanId);
            if (employee.isPresent()) {
                NhanVienDto dto = convertToDto(employee.get());
                return ResponseEntity.ok(createSuccessResponse("Lấy thông tin nhân viên thành công", dto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Không tìm thấy nhân viên với ID tài khoản: " + taiKhoanId, "NOT_FOUND"));
            }
        } catch (Exception e) {
            System.err.println("Error getting employee by account ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi tải thông tin nhân viên", "INTERNAL_ERROR"));
        }
    }

    /**
     * Thêm nhân viên mới (chỉ từ admin)
     */
    @PostMapping
    public ResponseEntity<?> createNhanVien(@RequestBody @Validated NhanVienDto dto) {
        try {
            // Log admin action
            logAdminAction("CREATE_EMPLOYEE", Map.of("data", dto));

            // Validate dữ liệu
            Map<String, String> errors = validateNhanVienDto(dto);
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

                // Kiểm tra tài khoản đã có nhân viên chưa
                Optional<NhanVien> existingNV = nhanVienService.findByTaiKhoanId(dto.getIdTaiKhoan());
                if (existingNV.isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Tài khoản này đã được liên kết với nhân viên khác", "ACCOUNT_LINKED"));
                }
            }

            // Kiểm tra mã nhân viên đã tồn tại
            if (dto.getMaNhanVien() != null && nhanVienService.existsByMaNhanVien(dto.getMaNhanVien())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Mã nhân viên đã tồn tại", "EMPLOYEE_CODE_EXISTS"));
            }

            NhanVien entity = convertToEntity(dto);
            entity.setNgayTao(new Date());
            entity.setNgayCapNhat(new Date());

            nhanVienService.addNhanVien(entity);

            // Lấy lại entity vừa tạo để trả về
            Optional<NhanVien> savedOpt = nhanVienService.getNhanVienById(entity.getId());
            NhanVienDto savedDto = savedOpt.map(this::convertToDto).orElse(convertToDto(entity));

            return ResponseEntity.ok(createSuccessResponse("Thêm nhân viên thành công!", savedDto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi thêm nhân viên: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * Cập nhật thông tin nhân viên (chỉ cho ADMIN hoặc chính nhân viên đó)
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
                        .body(createErrorResponse("ID không hợp lệ", "INVALID_ID"));
            }

            // Check if employee exists
            Optional<NhanVien> existingOpt = nhanVienService.getNhanVienById(id);
            if (existingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Không tìm thấy nhân viên với ID: " + id, "NOT_FOUND"));
            }

            // Validate DTO
            Map<String, String> validationErrors = validateEmployeeDto(dto, id);
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Dữ liệu không hợp lệ", "VALIDATION_ERROR", validationErrors));
            }

            NhanVien existing = existingOpt.get();

            // SECURITY: Don't allow changing account relationship
            if (dto.getIdTaiKhoan() != null && existing.getTaiKhoan() != null &&
                    !dto.getIdTaiKhoan().equals(existing.getTaiKhoan().getId())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Không được phép thay đổi tài khoản liên kết", "ACCOUNT_CHANGE_FORBIDDEN"));
            }

            // Check employee code conflict
            if (dto.getMaNhanVien() != null && !dto.getMaNhanVien().equals(existing.getMaNhanVien())) {
                if (nhanVienService.existsByMaNhanVien(dto.getMaNhanVien())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(createErrorResponse("Mã nhân viên đã tồn tại", "EMPLOYEE_CODE_EXISTS"));
                }
            }

            // Check phone number conflict
            if (dto.getSdt() != null && !dto.getSdt().equals(existing.getSdt())) {
                if (nhanVienService.isPhoneNumberUsed(dto.getSdt(), id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(createErrorResponse("Số điện thoại đã được sử dụng", "PHONE_EXISTS"));
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

            return ResponseEntity.ok(createSuccessResponse("Cập nhật nhân viên thành công", updatedDto));

        } catch (Exception e) {
            System.err.println("Error updating employee: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi cập nhật nhân viên", "INTERNAL_ERROR"));
        }
    }

    /**
     * Thay đổi trạng thái nhân viên (chỉ ADMIN)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        try {
            logAdminAction("CHANGE_EMPLOYEE_STATUS", Map.of("id", id, "newStatus", request.get("trangThai")));

            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID không hợp lệ", "INVALID_ID"));
            }

            Integer newStatus = request.get("trangThai");
            if (newStatus == null || (newStatus != 0 && newStatus != 1)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Trạng thái không hợp lệ (0 hoặc 1)", "INVALID_STATUS"));
            }

            Optional<NhanVien> employeeOpt = nhanVienService.getNhanVienById(id);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Không tìm thấy nhân viên", "NOT_FOUND"));
            }

            NhanVien employee = employeeOpt.get();
            if (Objects.equals(employee.getTrangThai(), newStatus)) {
                syncLinkedAccountStatus(employee, newStatus);
                return ResponseEntity.ok(createSuccessResponse("Trạng thái nhân viên đã được đồng bộ", convertToDto(employee)));
            }

            nhanVienService.toggleTrangThai(id);

            Optional<NhanVien> updatedOpt = nhanVienService.getNhanVienById(id);
            NhanVien updated = updatedOpt.orElse(employee);
            syncLinkedAccountStatus(updated, newStatus);
            NhanVienDto updatedDto = convertToDto(updated);

            String statusText = newStatus == 1 ? "kích hoạt" : "cho nghỉ việc";
            return ResponseEntity.ok(createSuccessResponse("Đã " + statusText + " nhân viên thành công", updatedDto));

        } catch (Exception e) {
            System.err.println("Error changing employee status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi thay đổi trạng thái", "INTERNAL_ERROR"));
        }
    }

    private void syncLinkedAccountStatus(NhanVien employee, Integer newStatus) {
        if (employee == null || employee.getTaiKhoan() == null || newStatus == null) {
            return;
        }

        TaiKhoan linkedAccount = employee.getTaiKhoan();
        if (!Objects.equals(linkedAccount.getTrangThai(), newStatus)) {
            linkedAccount.setTrangThai(newStatus);
            taiKhoanService.save(linkedAccount);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Integer id) {
        try {
            // Log admin action
            logAdminAction("DELETE_EMPLOYEE", Map.of("id", id));

            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("ID không hợp lệ", "INVALID_ID"));
            }

            Optional<NhanVien> employeeOpt = nhanVienService.getNhanVienById(id);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Không tìm thấy nhân viên", "NOT_FOUND"));
            }

            NhanVien employee = employeeOpt.get();
            if (employee.getTrangThai() == 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Nhân viên đã nghỉ việc", "ALREADY_INACTIVE"));
            }

            if (!nhanVienService.canDeleteNhanVien(id)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Không thể cho nghỉ việc nhân viên này do còn dữ liệu liên quan", "DELETE_FORBIDDEN"));
            }

            nhanVienService.deleteNhanVien(id); // Soft delete

            return ResponseEntity.ok(createSuccessResponse("Đã cho nhân viên nghỉ việc thành công", null));

        } catch (Exception e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi xóa nhân viên", "INTERNAL_ERROR"));
        }
    }

    /**
     * Xóa nhiều nhân viên (batch delete - chỉ ADMIN)
     */
    @DeleteMapping("/batch")
    public ResponseEntity<?> deleteMultipleEmployees(@RequestBody List<Integer> ids) {
        try {
            // Log admin action
            logAdminAction("BATCH_DELETE_EMPLOYEES", Map.of("ids", ids));

            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Danh sách ID không được để trống", "EMPTY_ID_LIST"));
            }

            List<Integer> successIds = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (Integer id : ids) {
                try {
                    Optional<NhanVien> employeeOpt = nhanVienService.getNhanVienById(id);
                    if (employeeOpt.isPresent()) {
                        NhanVien employee = employeeOpt.get();
                        if (employee.getTrangThai() == 0) {
                            errors.add("Nhân viên ID " + id + " đã nghỉ việc");
                            continue;
                        }

                        if (nhanVienService.canDeleteNhanVien(id)) {
                            nhanVienService.deleteNhanVien(id);
                            successIds.add(id);
                        } else {
                            errors.add("Không thể cho nghỉ việc nhân viên ID " + id + " do còn dữ liệu liên quan");
                        }
                    } else {
                        errors.add("Không tìm thấy nhân viên với ID: " + id);
                    }
                } catch (Exception e) {
                    errors.add("Lỗi khi xử lý nhân viên ID " + id + ": " + e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successIds.size());
            result.put("successIds", successIds);
            result.put("message", "Đã cho " + successIds.size() + " nhân viên nghỉ việc");
            if (!errors.isEmpty()) {
                result.put("errors", errors);
            }

            return ResponseEntity.ok(createSuccessResponse("Xóa nhân viên hoàn tất", result));

        } catch (Exception e) {
            System.err.println("Error batch deleting employees: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi xóa nhân viên", "INTERNAL_ERROR"));
        }
    }

    // ===== SEARCH OPERATIONS =====

    /**
     * Tìm kiếm nâng cao nhân viên
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
                        .body(createErrorResponse("Tham số tìm kiếm không hợp lệ", "INVALID_SEARCH_PARAMS"));
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
                    .body(createErrorResponse("Lỗi khi tìm kiếm nhân viên", "INTERNAL_ERROR"));
        }
    }

    // ===== ADMIN DASHBOARD OPERATIONS =====

    /**
     * Lấy thống kê dashboard cho ADMIN
     */
    @GetMapping("/admin/dashboard-stats")
    public ResponseEntity<?> getAdminDashboardStats() {
        try {
            Map<String, Object> stats = nhanVienService.getAdminDashboardStats();
            return ResponseEntity.ok(createSuccessResponse("Lấy thống kê dashboard thành công", stats));
        } catch (Exception e) {
            System.err.println("Error getting admin dashboard stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi lấy thống kê dashboard", "INTERNAL_ERROR"));
        }
    }

    /**
     * Lấy hoạt động gần đây cho ADMIN
     */
    @GetMapping("/admin/recent-activities")
    public ResponseEntity<?> getRecentActivities() {
        try {
            List<Map<String, Object>> activities = nhanVienService.getRecentActivities();
            return ResponseEntity.ok(createSuccessResponse("Lấy hoạt động gần đây thành công", activities));
        } catch (Exception e) {
            System.err.println("Error getting recent activities: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi lấy hoạt động gần đây", "INTERNAL_ERROR"));
        }
    }

    /**
     * Lấy nhân viên theo bộ lọc nhanh
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
                            .body(createErrorResponse("Loại filter không hợp lệ", "INVALID_FILTER_TYPE"));
            }

            PageResponse<NhanVienDto> response = createPagedResponse(filteredEmployees, page, size);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error in quick filters: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi tải bộ lọc nhanh", "INTERNAL_ERROR"));
        }
    }

    // ===== STATISTICS =====

    /**
     * Lấy thống kê nhân viên
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            Map<String, Long> stats = nhanVienService.getEmployeeStatistics();
            return ResponseEntity.ok(createSuccessResponse("Lấy thống kê thành công", stats));
        } catch (Exception e) {
            System.err.println("Error getting statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi lấy thống kê", "INTERNAL_ERROR"));
        }
    }

    /**
     * Lấy nhân viên active
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveEmployees() {
        try {
            List<NhanVien> activeEmployees = nhanVienService.getActiveNhanVien();
            List<NhanVienDto> activeEmployeeDtos = activeEmployees.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(createSuccessResponse("Lấy nhân viên hoạt động thành công", activeEmployeeDtos));
        } catch (Exception e) {
            System.err.println("Error getting active employees: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi lấy nhân viên hoạt động", "INTERNAL_ERROR"));
        }
    }

    /**
     * Lấy nhân viên mới gần đây
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentEmployees(@RequestParam(defaultValue = "7") int days) {
        try {
            if (days <= 0 || days > 365) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Số ngày phải trong khoảng 1-365", "INVALID_DAYS"));
            }

            List<NhanVien> recentEmployees = nhanVienService.getNewEmployees(days);
            List<NhanVienDto> recentEmployeeDtos = recentEmployees.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(createSuccessResponse("Lấy nhân viên mới thành công", recentEmployeeDtos));
        } catch (Exception e) {
            System.err.println("Error getting recent employees: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi lấy nhân viên mới", "INTERNAL_ERROR"));
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

    private Map<String, String> validateEmployeeDto(NhanVienDto dto, Integer excludeId) {
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

        if (dto.getMaNhanVien() != null && !dto.getMaNhanVien().trim().isEmpty()) {
            if (dto.getMaNhanVien().length() > 25) {
                errors.put("maNhanVien", "Mã nhân viên không được quá 25 ký tự");
            } else if (!dto.getMaNhanVien().matches("^[A-Za-z0-9]+$")) {
                errors.put("maNhanVien", "Mã nhân viên chỉ chứa chữ cái và số");
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

    private void updateRelationships(NhanVien existing, NhanVienDto dto) {
        // Cập nhật tài khoản
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
                    // Lấy thông tin địa chỉ
                    List<DiaChi> addresses = diaChiService.findByTaiKhoanId(entity.getTaiKhoan().getId());
                    if (!addresses.isEmpty()) {
                        // Convert địa chỉ sang DTO format
                        List<NhanVienDto.DiaChiInfo> diaChiInfos = addresses.stream()
                                .filter(Objects::nonNull)
                                .map(this::convertAddressToInfo)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        dto.setDanhSachDiaChi(diaChiInfos);

                        // Set địa chỉ mặc định
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

            // Tạo địa chỉ đầy đủ (2-level addressing - bỏ quận/huyện)
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

            String fullAddressStr = parts.isEmpty() ? "Chưa có địa chỉ" : String.join(", ", parts);
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

            // Set tài khoản
            if (dto.getIdTaiKhoan() != null) {
                Optional<TaiKhoan> taiKhoanOpt = taiKhoanService.findById(dto.getIdTaiKhoan());
                if (taiKhoanOpt.isPresent()) {
                    entity.setTaiKhoan(taiKhoanOpt.get());
                } else {
                    // Tạo reference object nếu không tìm thấy
                    TaiKhoan taiKhoan = new TaiKhoan();
                    taiKhoan.setId(dto.getIdTaiKhoan());
                    entity.setTaiKhoan(taiKhoan);
                }
            }

            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi convert DTO to entity: " + e.getMessage());
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
