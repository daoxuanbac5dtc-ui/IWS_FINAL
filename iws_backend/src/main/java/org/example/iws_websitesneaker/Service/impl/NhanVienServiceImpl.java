package org.example.iws_websitesneaker.Service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.iws_websitesneaker.Service.DiaChiService;
import org.example.iws_websitesneaker.Service.NhanVienService;
import org.example.iws_websitesneaker.entity.DiaChi;
import org.example.iws_websitesneaker.entity.NhanVien;
import org.example.iws_websitesneaker.repository.RepoNhanVien;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
            "^[a-zA-ZÃ€ÃÃ‚ÃƒÃˆÃ‰ÃŠÃŒÃÃ’Ã“Ã”Ã•Ã™ÃšÄ‚ÄÄ¨Å¨Æ Ã Ã¡Ã¢Ã£Ã¨Ã©ÃªÃ¬Ã­Ã²Ã³Ã´ÃµÃ¹ÃºÄƒÄ‘Ä©Å©Æ¡Æ¯Ä‚áº áº¢áº¤áº¦áº¨áºªáº¬áº®áº°áº²áº´áº¶áº¸áººáº¼á»€á»€á»‚Æ°Äƒáº¡áº£áº¥áº§áº©áº«áº­áº¯áº±áº³áºµáº·áº¹áº»áº½á»áº¿á»ƒá»„á»†á»ˆá»Šá»Œá»Žá»á»’á»”á»–á»˜á»šá»œá»žá» á»¢á»¤á»¦á»¨á»ªá»…á»‡á»‰á»‹á»á»á»‘á»“á»•á»—á»™á»›á»á»Ÿá»¡á»£á»¥á»§á»©á»«á»¬á»®á»°á»²á»´Ãá»¶á»¸á»­á»¯á»±á»³á»µÃ½á»·á»¹\\s]+$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    // ================== PAGINATION METHODS - NEW ==================

    @Override
    @Cacheable(value = "employees", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort")
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
    @Cacheable(value = "employees", key = "'all'")
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
    @Cacheable(value = "employee", key = "#id")
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
                throw new IllegalArgumentException("ThÃ´ng tin nhÃ¢n viÃªn khÃ´ng Ä‘Æ°á»£c null");
            }

            // Chuáº©n hÃ³a dá»¯ liá»‡u trÆ°á»›c khi validate
            normalizeNhanVienData(nhanVien);

            // Validate dá»¯ liá»‡u
            List<String> validationErrors = validateNhanVienData(nhanVien);
            if (!validationErrors.isEmpty()) {
                throw new IllegalArgumentException("Dá»¯ liá»‡u khÃ´ng há»£p lá»‡: " + String.join("; ", validationErrors));
            }

            // Kiá»ƒm tra trÃ¹ng láº·p sá»‘ Ä‘iá»‡n thoáº¡i
            if (existsByPhone(nhanVien.getSdt())) {
                throw new IllegalArgumentException("Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng: " + nhanVien.getSdt());
            }

            // Thiáº¿t láº­p thÃ´ng tin máº·c Ä‘á»‹nh
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

            // Äáº£m báº£o mÃ£ nhÃ¢n viÃªn lÃ  duy nháº¥t
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
            throw new RuntimeException("Lá»—i há»‡ thá»‘ng khi thÃªm nhÃ¢n viÃªn: " + e.getMessage(), e);
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
                throw new IllegalArgumentException("ThÃ´ng tin nhÃ¢n viÃªn hoáº·c ID khÃ´ng Ä‘Æ°á»£c null");
            }

            Optional<NhanVien> existing = getNhanVienById(nhanVien.getId());
            if (existing.isEmpty()) {
                throw new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn vá»›i ID: " + nhanVien.getId());
            }

            // Chuáº©n hÃ³a dá»¯ liá»‡u
            normalizeNhanVienData(nhanVien);

            // Validate dá»¯ liá»‡u
            List<String> validationErrors = validateNhanVienData(nhanVien);
            if (!validationErrors.isEmpty()) {
                throw new IllegalArgumentException("Dá»¯ liá»‡u khÃ´ng há»£p lá»‡: " + String.join("; ", validationErrors));
            }

            NhanVien existingNV = existing.get();

            // Check for duplicate maNhanVien
            if (!Objects.equals(nhanVien.getMaNhanVien(), existingNV.getMaNhanVien())) {
                if (existsByMaNhanVienExcludingId(nhanVien.getMaNhanVien(), nhanVien.getId())) {
                    throw new IllegalArgumentException("MÃ£ nhÃ¢n viÃªn Ä‘Ã£ tá»“n táº¡i");
                }
            }

            // Check for duplicate phone number
            if (!Objects.equals(nhanVien.getSdt(), existingNV.getSdt())) {
                if (isPhoneNumberUsed(nhanVien.getSdt(), nhanVien.getId())) {
                    throw new IllegalArgumentException("Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng bá»Ÿi nhÃ¢n viÃªn khÃ¡c");
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
            throw new RuntimeException("Lá»—i há»‡ thá»‘ng khi cáº­p nháº­t nhÃ¢n viÃªn: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public void deleteNhanVien(Integer id) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID nhÃ¢n viÃªn khÃ´ng há»£p lá»‡");
            }

            Optional<NhanVien> nhanVien = getNhanVienById(id);
            if (nhanVien.isEmpty()) {
                throw new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn vá»›i ID: " + id);
            }

            NhanVien nv = nhanVien.get();

            if (!canDeleteNhanVien(id)) {
                throw new IllegalStateException("KhÃ´ng thá»ƒ xÃ³a nhÃ¢n viÃªn nÃ y do cÃ²n dá»¯ liá»‡u liÃªn quan");
            }

            // Soft delete
            nv.setTrangThai(0);
            nv.setNgayCapNhat(new Date());
            repoNhanVien.save(nv);

            log.info("Soft deleted employee: {} (ID: {})", nv.getHoTen(), id);

        } catch (Exception e) {
            log.error("Error deleting employee: ", e);
            throw new RuntimeException("Lá»—i khi xÃ³a nhÃ¢n viÃªn: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public void batchDeleteNhanVien(List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                throw new IllegalArgumentException("Danh sÃ¡ch ID khÃ´ng Ä‘Æ°á»£c trá»‘ng");
            }

            if (ids.size() > 50) {
                throw new IllegalArgumentException("Chá»‰ cÃ³ thá»ƒ xÃ³a tá»‘i Ä‘a 50 nhÃ¢n viÃªn cÃ¹ng lÃºc");
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
            throw new RuntimeException("Lá»—i khi xÃ³a hÃ ng loáº¡t: " + e.getMessage(), e);
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
            errors.add("ThÃ´ng tin nhÃ¢n viÃªn khÃ´ng Ä‘Æ°á»£c null");
            return errors;
        }

        // Validate há» tÃªn
        if (nhanVien.getHoTen() == null || nhanVien.getHoTen().trim().isEmpty()) {
            errors.add("Há» tÃªn khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        } else {
            String hoTen = nhanVien.getHoTen().trim();
            if (hoTen.length() > 225) {
                errors.add("Há» tÃªn khÃ´ng Ä‘Æ°á»£c quÃ¡ 225 kÃ½ tá»±");
            } else if (!VIETNAMESE_NAME_PATTERN.matcher(hoTen).matches()) {
                errors.add("Há» tÃªn chá»‰ chá»©a chá»¯ cÃ¡i vÃ  khoáº£ng tráº¯ng, há»— trá»£ tiáº¿ng Viá»‡t");
            }
        }

        // Validate sá»‘ Ä‘iá»‡n thoáº¡i
        if (nhanVien.getSdt() == null || nhanVien.getSdt().trim().isEmpty()) {
            errors.add("Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        } else {
            String cleanPhone = nhanVien.getSdt().trim().replaceAll("\\s+", "");
            if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
                errors.add("Sá»‘ Ä‘iá»‡n thoáº¡i pháº£i cÃ³ 10 sá»‘ vÃ  báº¯t Ä‘áº§u báº±ng 0");
            }
        }

        // Validate email náº¿u cÃ³ tÃ i khoáº£n
        if (nhanVien.getTaiKhoan() != null &&
                nhanVien.getTaiKhoan().getEmail() != null &&
                !nhanVien.getTaiKhoan().getEmail().trim().isEmpty()) {
            String email = nhanVien.getTaiKhoan().getEmail().trim();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                errors.add("Email khÃ´ng Ä‘Ãºng Ä‘á»‹nh dáº¡ng");
            }
        }

        // Validate mÃ£ nhÃ¢n viÃªn
        if (nhanVien.getMaNhanVien() != null && !nhanVien.getMaNhanVien().trim().isEmpty()) {
            String maNV = nhanVien.getMaNhanVien().trim();
            if (maNV.length() > 25) {
                errors.add("MÃ£ nhÃ¢n viÃªn khÃ´ng Ä‘Æ°á»£c quÃ¡ 25 kÃ½ tá»±");
            } else if (!maNV.matches("^[A-Za-z0-9]+$")) {
                errors.add("MÃ£ nhÃ¢n viÃªn chá»‰ chá»©a chá»¯ cÃ¡i vÃ  sá»‘");
            }
        }

        // Validate tráº¡ng thÃ¡i
        if (nhanVien.getTrangThai() == null) {
            errors.add("Tráº¡ng thÃ¡i khÃ´ng Ä‘Æ°á»£c null");
        } else if (nhanVien.getTrangThai() != 0 && nhanVien.getTrangThai() != 1) {
            errors.add("Tráº¡ng thÃ¡i chá»‰ nháº­n giÃ¡ trá»‹ 0 hoáº·c 1");
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
     * Chuáº©n hÃ³a dá»¯ liá»‡u nhÃ¢n viÃªn trÆ°á»›c khi xá»­ lÃ½
     */
    private void normalizeNhanVienData(NhanVien nhanVien) {
        if (nhanVien == null) return;

        // Chuáº©n hÃ³a há» tÃªn
        if (nhanVien.getHoTen() != null) {
            nhanVien.setHoTen(nhanVien.getHoTen().trim().replaceAll("\\s+", " "));
        }

        // Chuáº©n hÃ³a sá»‘ Ä‘iá»‡n thoáº¡i
        if (nhanVien.getSdt() != null) {
            nhanVien.setSdt(nhanVien.getSdt().trim().replaceAll("\\s+", ""));
        }

        // Chuáº©n hÃ³a mÃ£ nhÃ¢n viÃªn
        if (nhanVien.getMaNhanVien() != null) {
            nhanVien.setMaNhanVien(nhanVien.getMaNhanVien().trim().toUpperCase());
        }

        // Chuáº©n hÃ³a email náº¿u cÃ³
        if (nhanVien.getTaiKhoan() != null && nhanVien.getTaiKhoan().getEmail() != null) {
            nhanVien.getTaiKhoan().setEmail(nhanVien.getTaiKhoan().getEmail().trim().toLowerCase());
        }
    }

    /**
     * Kiá»ƒm tra sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng chÆ°a
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
                throw new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y nhÃ¢n viÃªn vá»›i ID: " + id);
            }
        } catch (Exception e) {
            log.error("Error toggling employee status: ", e);
            throw new RuntimeException("Lá»—i khi thay Ä‘á»•i tráº¡ng thÃ¡i nhÃ¢n viÃªn: " + e.getMessage(), e);
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
                return "Táº¡o má»›i";
            }
        }
        return "Cáº­p nháº­t thÃ´ng tin";
    }

    private String generateActivityDescription(NhanVien nv) {
        StringBuilder desc = new StringBuilder();
        desc.append("NhÃ¢n viÃªn ").append(nv.getHoTen());

        if (nv.getMaNhanVien() != null) {
            desc.append(" (").append(nv.getMaNhanVien()).append(")");
        }

        String actionType = determineActivityType(nv);
        desc.append(" Ä‘Ã£ ").append(actionType.toLowerCase());

        if (needsAdminAttention(nv)) {
            desc.append(" - Cáº§n xem xÃ©t");
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
                            regionStats.merge("ChÆ°a xÃ¡c Ä‘á»‹nh", 1L, Long::sum);
                        }
                    } else {
                        regionStats.merge("ChÆ°a cÃ³ Ä‘á»‹a chá»‰", 1L, Long::sum);
                    }
                } catch (Exception e) {
                    regionStats.merge("Lá»—i dá»¯ liá»‡u", 1L, Long::sum);
                }
            } else {
                regionStats.merge("ChÆ°a cÃ³ tÃ i khoáº£n", 1L, Long::sum);
            }
        }

        return regionStats;
    }
}
