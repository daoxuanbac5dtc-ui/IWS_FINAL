package org.example.iws_websitesneaker.Service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.iws_websitesneaker.Service.DiaChiService;
import org.example.iws_websitesneaker.Service.NhanVienService;
import org.example.iws_websitesneaker.entity.DiaChi;
import org.example.iws_websitesneaker.entity.NhanVien;
import org.example.iws_websitesneaker.repository.RepoNhanVien;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class NhanVienServiceImpl implements NhanVienService {

    @Autowired
    private RepoNhanVien repoNhanVien;

    @Autowired
    private DiaChiService diaChiService;

    // Date format for parsing
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // Validation patterns - FIXED
    private static final Pattern VIETNAMESE_NAME_PATTERN = Pattern.compile(
            "^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽềếểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵýỷỹ\\s]+$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    // ================== PAGINATION METHODS - NEW ==================

    @Override
    public Page<NhanVien> findAllWithPagination(Pageable pageable) {
        try {
            log.debug("Finding all employees with pagination: {}", pageable);
            Page<NhanVien> result = repoNhanVien.findAllWithTaiKhoan(pageable);
            log.debug("Found {} employees in page {}", result.getContent().size(), pageable.getPageNumber());
            return result;
        } catch (Exception e) {
            log.error("Error finding employees with pagination: ", e);
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<NhanVien> searchWithCriteria(String globalSearch, Integer trangThai,
                                             String startDate, String endDate, Pageable pageable) {
        try {
            log.info("Searching employees with criteria: globalSearch='{}', trangThai={}, startDate='{}', endDate='{}'",
                    globalSearch, trangThai, startDate, endDate);

            // Parse dates
            Date parsedStartDate = parseDate(startDate);
            Date parsedEndDate = parseDate(endDate);

            // Set end date to end of day if provided
            if (parsedEndDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(parsedEndDate);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                parsedEndDate = cal.getTime();
            }

            // Call repository method
            Page<NhanVien> result = repoNhanVien.searchWithCriteriaAndPagination(
                    globalSearch, trangThai, parsedStartDate, parsedEndDate, pageable);

            log.info("Search completed: found {} employees out of {} total",
                    result.getContent().size(), result.getTotalElements());

            return result;

        } catch (Exception e) {
            log.error("Error in searchWithCriteria: ", e);
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<NhanVien> findByTrangThaiWithPagination(Integer trangThai, Pageable pageable) {
        try {
            if (trangThai == null) {
                return findAllWithPagination(pageable);
            }
            return repoNhanVien.findByTrangThaiWithTaiKhoan(trangThai, pageable);
        } catch (Exception e) {
            log.error("Error finding employees by status with pagination: ", e);
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<NhanVien> getActiveNhanVienWithPagination(Pageable pageable) {
        return findByTrangThaiWithPagination(1, pageable);
    }

    // ================== CRUD OPERATIONS - UPDATED ==================

    @Override
    public List<NhanVien> getAllNhanVien() {
        try {
            List<NhanVien> employees = repoNhanVien.findAllWithTaiKhoan();
            if (employees == null) {
                log.warn("Repository returned null for getAllNhanVien");
                return new ArrayList<>();
            }
            log.debug("Retrieved {} employees from database", employees.size());
            return employees;
        } catch (Exception e) {
            log.error("Error getting all employees: ", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<NhanVien> getNhanVienById(Integer id) {
        try {
            if (id == null || id <= 0) {
                return Optional.empty();
            }
            return repoNhanVien.findByIdWithTaiKhoan(id);
        } catch (Exception e) {
            log.error("Error getting employee by ID {}: ", id, e);
            return Optional.empty();
        }
    }

    @Override
    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public void addNhanVien(NhanVien nhanVien) {
        try {
            if (nhanVien == null) {
                throw new IllegalArgumentException("Thông tin nhân viên không được null");
            }

            // Chuẩn hóa dữ liệu trước khi validate
            normalizeNhanVienData(nhanVien);

            // Validate dữ liệu
            List<String> validationErrors = validateNhanVienData(nhanVien);
            if (!validationErrors.isEmpty()) {
                throw new IllegalArgumentException("Dữ liệu không hợp lệ: " + String.join("; ", validationErrors));
            }

            // Kiểm tra trùng lặp số điện thoại
            if (existsByPhone(nhanVien.getSdt())) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng: " + nhanVien.getSdt());
            }

            // Thiết lập thông tin mặc định
            if (nhanVien.getNgayTao() == null) {
                nhanVien.setNgayTao(new Date());
            }
            nhanVien.setNgayCapNhat(new Date());

            if (nhanVien.getMaNhanVien() == null || nhanVien.getMaNhanVien().trim().isEmpty()) {
                nhanVien.setMaNhanVien(generateMaNhanVien());
            }

            if (nhanVien.getTrangThai() == null) {
                nhanVien.setTrangThai(1);
            }

            // Đảm bảo mã nhân viên là duy nhất
            if (existsByMaNhanVien(nhanVien.getMaNhanVien())) {
                nhanVien.setMaNhanVien(generateMaNhanVien());
            }

            repoNhanVien.save(nhanVien);
            log.info("Created new employee: {} (ID: {})", nhanVien.getHoTen(), nhanVien.getId());

        } catch (IllegalArgumentException e) {
            log.error("Validation error adding employee: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error adding employee: ", e);
            throw new RuntimeException("Lỗi hệ thống khi thêm nhân viên: " + e.getMessage(), e);
        }
    }
    @Override
    public boolean existsBySdt(String sdt) {
        try {
            if (sdt == null || sdt.trim().isEmpty()) {
                return false;
            }

            String cleanPhone = sdt.trim().replaceAll("\\s+", "");
            log.debug("Checking if phone exists: {}", cleanPhone);

            boolean exists = repoNhanVien.existsBySdt(cleanPhone);
            log.debug("Phone {} exists: {}", cleanPhone, exists);

            return exists;
        } catch (Exception e) {
            log.error("Error checking phone existence: ", e);
            return false;
        }
    }

    @Override
    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public void updateNhanVien(NhanVien nhanVien) {
        try {
            if (nhanVien == null || nhanVien.getId() == null) {
                throw new IllegalArgumentException("Thông tin nhân viên hoặc ID không được null");
            }

            Optional<NhanVien> existing = getNhanVienById(nhanVien.getId());
            if (existing.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + nhanVien.getId());
            }

            // Chuẩn hóa dữ liệu
            normalizeNhanVienData(nhanVien);

            // Validate dữ liệu
            List<String> validationErrors = validateNhanVienData(nhanVien);
            if (!validationErrors.isEmpty()) {
                throw new IllegalArgumentException("Dữ liệu không hợp lệ: " + String.join("; ", validationErrors));
            }

            NhanVien existingNV = existing.get();

            // Check for duplicate maNhanVien
            if (!Objects.equals(nhanVien.getMaNhanVien(), existingNV.getMaNhanVien())) {
                if (existsByMaNhanVienExcludingId(nhanVien.getMaNhanVien(), nhanVien.getId())) {
                    throw new IllegalArgumentException("Mã nhân viên đã tồn tại");
                }
            }

            // Check for duplicate phone number
            if (!Objects.equals(nhanVien.getSdt(), existingNV.getSdt())) {
                if (isPhoneNumberUsed(nhanVien.getSdt(), nhanVien.getId())) {
                    throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi nhân viên khác");
                }
            }

            // Preserve existing data
            nhanVien.setTaiKhoan(existingNV.getTaiKhoan());
            nhanVien.setNgayTao(existingNV.getNgayTao());
            nhanVien.setNgayCapNhat(new Date());

            repoNhanVien.save(nhanVien);
            log.info("Updated employee: {} (ID: {})", nhanVien.getHoTen(), nhanVien.getId());

        } catch (IllegalArgumentException e) {
            log.error("Validation error updating employee: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating employee: ", e);
            throw new RuntimeException("Lỗi hệ thống khi cập nhật nhân viên: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public void deleteNhanVien(Integer id) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID nhân viên không hợp lệ");
            }

            Optional<NhanVien> nhanVien = getNhanVienById(id);
            if (nhanVien.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + id);
            }

            NhanVien nv = nhanVien.get();

            if (!canDeleteNhanVien(id)) {
                throw new IllegalStateException("Không thể xóa nhân viên này do còn dữ liệu liên quan");
            }

            // Soft delete
            nv.setTrangThai(0);
            nv.setNgayCapNhat(new Date());
            repoNhanVien.save(nv);

            log.info("Soft deleted employee: {} (ID: {})", nv.getHoTen(), id);

        } catch (Exception e) {
            log.error("Error deleting employee: ", e);
            throw new RuntimeException("Lỗi khi xóa nhân viên: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public void batchDeleteNhanVien(List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                throw new IllegalArgumentException("Danh sách ID không được trống");
            }

            if (ids.size() > 50) {
                throw new IllegalArgumentException("Chỉ có thể xóa tối đa 50 nhân viên cùng lúc");
            }

            log.info("Batch deleting {} employees: {}", ids.size(), ids);

            int deletedCount = 0;
            for (Integer id : ids) {
                try {
                    deleteNhanVien(id);
                    deletedCount++;
                } catch (Exception e) {
                    log.warn("Failed to delete employee {}: {}", id, e.getMessage());
                }
            }

            log.info("Batch delete completed: {}/{} employees deleted", deletedCount, ids.size());

        } catch (Exception e) {
            log.error("Error in batch delete: ", e);
            throw new RuntimeException("Lỗi khi xóa hàng loạt: " + e.getMessage(), e);
        }
    }

    // ================== TAI KHOAN RELATED ==================

    @Override
    @Transactional
    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public void deleteByTaiKhoanId(Integer taiKhoanId) {
        try {
            if (taiKhoanId == null || taiKhoanId <= 0) {
                log.warn("Invalid taiKhoanId: {}", taiKhoanId);
                return;
            }

            log.info("Deleting employee with taiKhoanId: {}", taiKhoanId);

            Optional<NhanVien> nhanVien = repoNhanVien.findByTaiKhoanIdWithTaiKhoan(taiKhoanId);
            if (nhanVien.isPresent()) {
                NhanVien nv = nhanVien.get();
                log.info("Found employee: {} (ID: {})", nv.getHoTen(), nv.getId());
                repoNhanVien.deleteById(nv.getId());
                log.info("Employee hard deleted successfully");
            } else {
                log.info("No employee found with taiKhoanId: {}", taiKhoanId);
            }
        } catch (Exception e) {
            log.error("Error deleting employee by taiKhoanId: ", e);
            throw new RuntimeException("Failed to delete employee: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<NhanVien> findByTaiKhoanId(Integer taiKhoanId) {
        try {
            if (taiKhoanId == null || taiKhoanId <= 0) {
                return Optional.empty();
            }
            return repoNhanVien.findByTaiKhoanIdWithTaiKhoan(taiKhoanId);
        } catch (Exception e) {
            log.error("Error finding employee by taiKhoanId: ", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<NhanVien> findByTaiKhoanEmail(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return Optional.empty();
            }
            return repoNhanVien.findByTaiKhoanEmailWithTaiKhoan(email.trim());
        } catch (Exception e) {
            log.error("Error finding employee by email: ", e);
            return Optional.empty();
        }
    }

    // ================== SEARCH AND QUERY ==================

    @Override
    public List<NhanVien> searchByKeyword(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllNhanVien();
            }
            return repoNhanVien.searchByKeywordWithTaiKhoan(keyword.trim());
        } catch (Exception e) {
            log.error("Error searching employees by keyword: ", e);
            return getAllNhanVien();
        }
    }

    @Override
    public Optional<NhanVien> findByMaNhanVien(String maNhanVien) {
        try {
            if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
                return Optional.empty();
            }
            return repoNhanVien.findByMaNhanVienWithTaiKhoan(maNhanVien.trim());
        } catch (Exception e) {
            log.error("Error finding employee by maNhanVien: ", e);
            return Optional.empty();
        }
    }

    @Override
    public List<NhanVien> findByTrangThai(Integer trangThai) {
        try {
            if (trangThai == null) {
                return getAllNhanVien();
            }
            return repoNhanVien.findByTrangThaiWithTaiKhoan(trangThai);
        } catch (Exception e) {
            log.error("Error finding employees by status: ", e);
            return getAllNhanVien();
        }
    }

    // ================== ADVANCED SEARCH ==================

    @Override
    public List<NhanVien> searchAdvancedWithAllCriteria(String hoTen, String email, String sdt,
                                                        String maNhanVien, String diaChi, Integer trangThai,
                                                        Date startDate, Date endDate) {
        try {
            log.debug("Advanced search with criteria: hoTen='{}', email='{}', sdt='{}', maNhanVien='{}', trangThai={}",
                    hoTen, email, sdt, maNhanVien, trangThai);

            return repoNhanVien.searchAdvancedWithTaiKhoan(hoTen, email, sdt, maNhanVien, trangThai, startDate, endDate);

        } catch (Exception e) {
            log.error("Error in advanced search: ", e);
            throw new RuntimeException("Advanced search failed: " + e.getMessage(), e);
        }
    }

    // ================== VALIDATION METHODS - FIXED ==================

    @Override
    public boolean isValidNhanVienSearchParams(String hoTen, String email, String sdt, String maNhanVien) {
        try {
            if (hoTen != null && !hoTen.trim().isEmpty()) {
                if (!VIETNAMESE_NAME_PATTERN.matcher(hoTen.trim()).matches()) {
                    return false;
                }
            }

            if (email != null && !email.trim().isEmpty()) {
                if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
                    return false;
                }
            }

            if (sdt != null && !sdt.trim().isEmpty()) {
                String cleanPhone = sdt.trim().replaceAll("\\s+", "");
                if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
                    return false;
                }
            }

            if (maNhanVien != null && !maNhanVien.trim().isEmpty()) {
                if (!maNhanVien.trim().matches("^[A-Za-z0-9]+$")) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.error("Error validating search params: ", e);
            return false;
        }
    }

    @Override
    public List<String> validateNhanVienData(NhanVien nhanVien) {
        List<String> errors = new ArrayList<>();

        if (nhanVien == null) {
            errors.add("Thông tin nhân viên không được null");
            return errors;
        }

        // Validate họ tên
        if (nhanVien.getHoTen() == null || nhanVien.getHoTen().trim().isEmpty()) {
            errors.add("Họ tên không được để trống");
        } else {
            String hoTen = nhanVien.getHoTen().trim();
            if (hoTen.length() > 225) {
                errors.add("Họ tên không được quá 225 ký tự");
            } else if (!VIETNAMESE_NAME_PATTERN.matcher(hoTen).matches()) {
                errors.add("Họ tên chỉ chứa chữ cái và khoảng trắng, hỗ trợ tiếng Việt");
            }
        }

        // Validate số điện thoại
        if (nhanVien.getSdt() == null || nhanVien.getSdt().trim().isEmpty()) {
            errors.add("Số điện thoại không được để trống");
        } else {
            String cleanPhone = nhanVien.getSdt().trim().replaceAll("\\s+", "");
            if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
                errors.add("Số điện thoại phải có 10 số và bắt đầu bằng 0");
            }
        }

        // Validate email nếu có tài khoản
        if (nhanVien.getTaiKhoan() != null &&
                nhanVien.getTaiKhoan().getEmail() != null &&
                !nhanVien.getTaiKhoan().getEmail().trim().isEmpty()) {
            String email = nhanVien.getTaiKhoan().getEmail().trim();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                errors.add("Email không đúng định dạng");
            }
        }

        // Validate mã nhân viên
        if (nhanVien.getMaNhanVien() != null && !nhanVien.getMaNhanVien().trim().isEmpty()) {
            String maNV = nhanVien.getMaNhanVien().trim();
            if (maNV.length() > 25) {
                errors.add("Mã nhân viên không được quá 25 ký tự");
            } else if (!maNV.matches("^[A-Za-z0-9]+$")) {
                errors.add("Mã nhân viên chỉ chứa chữ cái và số");
            }
        }

        // Validate trạng thái
        if (nhanVien.getTrangThai() == null) {
            errors.add("Trạng thái không được null");
        } else if (nhanVien.getTrangThai() != 0 && nhanVien.getTrangThai() != 1) {
            errors.add("Trạng thái chỉ nhận giá trị 0 hoặc 1");
        }

        return errors;
    }

    // ================== EXISTENCE CHECKS ==================

    @Override
    public boolean existsByMaNhanVien(String maNhanVien) {
        try {
            if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
                return false;
            }
            return repoNhanVien.existsByMaNhanVien(maNhanVien.trim());
        } catch (Exception e) {
            log.error("Error checking maNhanVien existence: ", e);
            return false;
        }
    }

    @Override
    public boolean existsByMaNhanVienExcludingId(String maNhanVien, Integer excludeId) {
        try {
            if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
                return false;
            }
            return repoNhanVien.existsByMaNhanVienExcludingId(maNhanVien.trim(), excludeId);
        } catch (Exception e) {
            log.error("Error checking maNhanVien existence excluding ID: ", e);
            return false;
        }
    }

    @Override
    public boolean isPhoneNumberUsed(String sdt, Integer excludeId) {
        try {
            if (sdt == null || sdt.trim().isEmpty()) {
                return false;
            }
            return repoNhanVien.existsBySdtExcludingId(sdt.trim(), excludeId);
        } catch (Exception e) {
            log.error("Error checking phone number usage: ", e);
            return false;
        }
    }

    // ================== NEW HELPER METHODS ==================

    /**
     * Chuẩn hóa dữ liệu nhân viên trước khi xử lý
     */
    private void normalizeNhanVienData(NhanVien nhanVien) {
        if (nhanVien == null) return;

        // Chuẩn hóa họ tên
        if (nhanVien.getHoTen() != null) {
            nhanVien.setHoTen(nhanVien.getHoTen().trim().replaceAll("\\s+", " "));
        }

        // Chuẩn hóa số điện thoại
        if (nhanVien.getSdt() != null) {
            nhanVien.setSdt(nhanVien.getSdt().trim().replaceAll("\\s+", ""));
        }

        // Chuẩn hóa mã nhân viên
        if (nhanVien.getMaNhanVien() != null) {
            nhanVien.setMaNhanVien(nhanVien.getMaNhanVien().trim().toUpperCase());
        }

        // Chuẩn hóa email nếu có
        if (nhanVien.getTaiKhoan() != null && nhanVien.getTaiKhoan().getEmail() != null) {
            nhanVien.getTaiKhoan().setEmail(nhanVien.getTaiKhoan().getEmail().trim().toLowerCase());
        }
    }

    /**
     * Kiểm tra số điện thoại đã được sử dụng chưa
     */
    private boolean existsByPhone(String sdt) {
        try {
            if (sdt == null || sdt.trim().isEmpty()) {
                return false;
            }
            return repoNhanVien.existsBySdt(sdt.trim());
        } catch (Exception e) {
            log.error("Error checking phone existence: ", e);
            return false;
        }
    }

    // ================== STATISTICAL METHODS ==================

    @Override
    public List<NhanVien> findByDepartment(String department) {
        try {
            // TODO: Implement department filtering when department field is added
            return getAllNhanVien();
        } catch (Exception e) {
            log.error("Error finding by department: ", e);
            return getAllNhanVien();
        }
    }

    @Override
    public long countActiveInDateRange(Date startDate, Date endDate) {
        try {
            return repoNhanVien.countByDateRange(startDate, endDate);
        } catch (Exception e) {
            log.error("Error counting active employees: ", e);
            return 0;
        }
    }

    @Override
    public Map<String, Long> getEmployeeStatistics() {
        try {
            Map<String, Long> stats = new HashMap<>();
            stats.put("total", repoNhanVien.countAllEmployees());
            stats.put("active", repoNhanVien.countActiveEmployees());
            stats.put("inactive", repoNhanVien.countInactiveEmployees());

            Date thirtyDaysAgo = new Date(System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L));
            stats.put("recent", repoNhanVien.countNewThisMonth(thirtyDaysAgo));

            return stats;
        } catch (Exception e) {
            log.error("Error getting employee statistics: ", e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getEmployeeStatisticsWithContext(Pageable pageable) {
        try {
            Map<String, Object> stats = new HashMap<>();
            Map<String, Long> basicStats = getEmployeeStatistics();

            stats.putAll(basicStats);
            stats.put("currentPage", pageable.getPageNumber());
            stats.put("pageSize", pageable.getPageSize());
            stats.put("sortBy", pageable.getSort().toString());

            return stats;
        } catch (Exception e) {
            log.error("Error getting employee statistics with context: ", e);
            return new HashMap<>();
        }
    }

    // ================== BUSINESS LOGIC METHODS ==================

    @Override
    public boolean canDeleteNhanVien(Integer id) {
        try {
            Optional<NhanVien> nhanVien = getNhanVienById(id);
            if (nhanVien.isEmpty()) {
                return false;
            }
            // TODO: Implement checks with related entities
            return true;
        } catch (Exception e) {
            log.error("Error checking if employee can be deleted: ", e);
            return false;
        }
    }

    @Override
    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public void toggleTrangThai(Integer id) {
        try {
            Optional<NhanVien> optional = getNhanVienById(id);
            if (optional.isPresent()) {
                NhanVien nv = optional.get();
                Integer newStatus = nv.getTrangThai() == 1 ? 0 : 1;
                nv.setTrangThai(newStatus);
                nv.setNgayCapNhat(new Date());
                repoNhanVien.save(nv);

                String statusText = newStatus == 1 ? "activated" : "deactivated";
                log.info("Employee {}: {} (ID: {})", statusText, nv.getHoTen(), id);
            } else {
                throw new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + id);
            }
        } catch (Exception e) {
            log.error("Error toggling employee status: ", e);
            throw new RuntimeException("Lỗi khi thay đổi trạng thái nhân viên: " + e.getMessage(), e);
        }
    }

    @Override
    public List<NhanVien> getActiveNhanVien() {
        try {
            return getAllNhanVien().stream()
                    .filter(nv -> nv.getTrangThai() == 1)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting active employees: ", e);
            return getAllNhanVien();
        }
    }

    // ================== ADMIN DASHBOARD METHODS ==================

    @Override
    public Map<String, Object> getAdminDashboardStats() {
        try {
            List<NhanVien> allEmployees = getAllNhanVien();
            Date now = new Date();
            Date sevenDaysAgo = new Date(now.getTime() - (7 * 24 * 60 * 60 * 1000L));
            Date thirtyDaysAgo = new Date(now.getTime() - (30 * 24 * 60 * 60 * 1000L));

            Map<String, Object> stats = new HashMap<>();

            stats.put("total", allEmployees.size());
            stats.put("active", allEmployees.stream().filter(nv -> nv.getTrangThai() == 1).count());
            stats.put("inactive", allEmployees.stream().filter(nv -> nv.getTrangThai() == 0).count());

            stats.put("newIn7Days", allEmployees.stream()
                    .filter(nv -> nv.getNgayTao() != null && nv.getNgayTao().after(sevenDaysAgo))
                    .count());
            stats.put("newIn30Days", allEmployees.stream()
                    .filter(nv -> nv.getNgayTao() != null && nv.getNgayTao().after(thirtyDaysAgo))
                    .count());

            stats.put("needsReview", allEmployees.stream()
                    .filter(this::needsAdminAttention)
                    .count());

            stats.put("recentlyUpdated", allEmployees.stream()
                    .filter(nv -> nv.getNgayCapNhat() != null && nv.getNgayCapNhat().after(sevenDaysAgo))
                    .count());

            Map<String, Long> regionStats = getEmployeesByRegion(allEmployees);
            stats.put("regionStats", regionStats);

            log.debug("Admin dashboard stats generated: {}", stats);
            return stats;
        } catch (Exception e) {
            log.error("Error getting admin dashboard stats: ", e);
            return new HashMap<>();
        }
    }

    @Override
    public List<Map<String, Object>> getRecentActivities() {
        try {
            List<NhanVien> allEmployees = getAllNhanVien();
            Date sevenDaysAgo = new Date(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L));

            return allEmployees.stream()
                    .filter(nv -> nv.getNgayCapNhat() != null && nv.getNgayCapNhat().after(sevenDaysAgo))
                    .sorted((a, b) -> b.getNgayCapNhat().compareTo(a.getNgayCapNhat()))
                    .limit(10)
                    .map(this::convertToActivityMap)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting recent activities: ", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<NhanVien> getNewEmployees(int days) {
        try {
            Date cutoffDate = new Date(System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L));
            return getAllNhanVien().stream()
                    .filter(nv -> nv.getNgayTao() != null && nv.getNgayTao().after(cutoffDate))
                    .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting new employees: ", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<NhanVien> getEmployeesNeedingReview() {
        try {
            return getAllNhanVien().stream()
                    .filter(this::needsAdminAttention)
                    .sorted((a, b) -> {
                        int priorityA = getAttentionPriority(a);
                        int priorityB = getAttentionPriority(b);
                        return Integer.compare(priorityB, priorityA);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting employees needing review: ", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<NhanVien> getRecentlyActiveEmployees() {
        try {
            Date sevenDaysAgo = new Date(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L));
            return getAllNhanVien().stream()
                    .filter(nv -> nv.getNgayCapNhat() != null && nv.getNgayCapNhat().after(sevenDaysAgo))
                    .sorted((a, b) -> b.getNgayCapNhat().compareTo(a.getNgayCapNhat()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting recently active employees: ", e);
            return new ArrayList<>();
        }
    }

    // ================== CACHE MANAGEMENT ==================

    @Override
    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public void clearEmployeeCache() {
        log.info("Employee cache cleared");
    }

    @Override
    @CacheEvict(value = "employee", key = "#employeeId")
    public void refreshEmployeeCache(Integer employeeId) {
        log.info("Employee cache refreshed for ID: {}", employeeId);
    }

    // ================== EXPORT/IMPORT METHODS ==================

    @Override
    public byte[] exportEmployeesToExcel(Map<String, Object> searchCriteria) {
        // TODO: Implement Excel export
        throw new UnsupportedOperationException("Excel export not yet implemented");
    }

    @Override
    public byte[] getEmployeeImportTemplate() {
        // TODO: Implement Excel template generation
        throw new UnsupportedOperationException("Excel template not yet implemented");
    }

    // ================== PERFORMANCE MONITORING ==================

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cacheEnabled", true);
        metrics.put("totalEmployees", repoNhanVien.countAllEmployees());
        metrics.put("lastUpdated", new Date());
        return metrics;
    }

    // ================== UTILITY METHODS ==================

    private String generateMaNhanVien() {
        try {
            String prefix = "NV";
            long timestamp = System.currentTimeMillis();
            int random = (int) (Math.random() * 1000);

            String maNhanVien;
            int attempts = 0;
            do {
                maNhanVien = prefix + String.format("%d%03d", (timestamp + attempts) % 100000, random);
                attempts++;
            } while (existsByMaNhanVien(maNhanVien) && attempts < 10);

            if (attempts >= 10) {
                maNhanVien = prefix + System.currentTimeMillis() + String.format("%03d", random);
            }

            log.debug("Generated employee code: {}", maNhanVien);
            return maNhanVien;
        } catch (Exception e) {
            log.error("Error generating employee code: ", e);
            return "NV" + System.currentTimeMillis();
        }
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return dateFormat.parse(dateStr.trim());
        } catch (ParseException e) {
            log.warn("Invalid date format: {}", dateStr);
            return null;
        }
    }

    // ================== HELPER METHODS FOR ADMIN ==================

    private boolean needsAdminAttention(NhanVien nv) {
        if (nv.getMaNhanVien() == null || nv.getMaNhanVien().trim().isEmpty()) {
            return true;
        }

        if (nv.getTaiKhoan() == null) {
            return true;
        }

        if (nv.getHoTen() == null || nv.getHoTen().trim().isEmpty()) {
            return true;
        }

        if (nv.getSdt() == null || nv.getSdt().trim().isEmpty()) {
            return true;
        }

        if (nv.getTaiKhoan() != null) {
            try {
                List<DiaChi> addresses = diaChiService.findByTaiKhoanId(nv.getTaiKhoan().getId());
                if (addresses.isEmpty()) {
                    return true;
                }
            } catch (Exception e) {
                return true;
            }
        }

        return false;
    }

    private int getAttentionPriority(NhanVien nv) {
        int priority = 0;

        if (nv.getTaiKhoan() == null) {
            priority += 100;
        }

        if (nv.getMaNhanVien() == null || nv.getMaNhanVien().trim().isEmpty()) {
            priority += 50;
        }

        if (nv.getHoTen() == null || nv.getHoTen().trim().isEmpty()) {
            priority += 30;
        }

        if (nv.getSdt() == null || nv.getSdt().trim().isEmpty()) {
            priority += 20;
        }

        if (nv.getTaiKhoan() != null) {
            try {
                List<DiaChi> addresses = diaChiService.findByTaiKhoanId(nv.getTaiKhoan().getId());
                if (addresses.isEmpty()) {
                    priority += 10;
                }
            } catch (Exception e) {
                priority += 5;
            }
        }

        return priority;
    }

    private Map<String, Object> convertToActivityMap(NhanVien nv) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("employeeId", nv.getId());
        activity.put("employeeName", nv.getHoTen());
        activity.put("employeeCode", nv.getMaNhanVien());
        activity.put("action", determineActivityType(nv));
        activity.put("timestamp", nv.getNgayCapNhat());
        activity.put("description", generateActivityDescription(nv));
        activity.put("priority", needsAdminAttention(nv) ? "HIGH" : "NORMAL");
        return activity;
    }

    private String determineActivityType(NhanVien nv) {
        if (nv.getNgayTao() != null && nv.getNgayCapNhat() != null) {
            long diffInHours = (nv.getNgayCapNhat().getTime() - nv.getNgayTao().getTime()) / (1000 * 60 * 60);
            if (diffInHours < 1) {
                return "Tạo mới";
            }
        }
        return "Cập nhật thông tin";
    }

    private String generateActivityDescription(NhanVien nv) {
        StringBuilder desc = new StringBuilder();
        desc.append("Nhân viên ").append(nv.getHoTen());

        if (nv.getMaNhanVien() != null) {
            desc.append(" (").append(nv.getMaNhanVien()).append(")");
        }

        String actionType = determineActivityType(nv);
        desc.append(" đã ").append(actionType.toLowerCase());

        if (needsAdminAttention(nv)) {
            desc.append(" - Cần xem xét");
        }

        return desc.toString();
    }

    private Map<String, Long> getEmployeesByRegion(List<NhanVien> employees) {
        Map<String, Long> regionStats = new HashMap<>();

        for (NhanVien nv : employees) {
            if (nv.getTaiKhoan() != null) {
                try {
                    List<DiaChi> addresses = diaChiService.findByTaiKhoanId(nv.getTaiKhoan().getId());
                    if (!addresses.isEmpty()) {
                        String region = addresses.get(0).getTenTinh();
                        if (region != null && !region.trim().isEmpty()) {
                            regionStats.merge(region, 1L, Long::sum);
                        } else {
                            regionStats.merge("Chưa xác định", 1L, Long::sum);
                        }
                    } else {
                        regionStats.merge("Chưa có địa chỉ", 1L, Long::sum);
                    }
                } catch (Exception e) {
                    regionStats.merge("Lỗi dữ liệu", 1L, Long::sum);
                }
            } else {
                regionStats.merge("Chưa có tài khoản", 1L, Long::sum);
            }
        }

        return regionStats;
    }
}
