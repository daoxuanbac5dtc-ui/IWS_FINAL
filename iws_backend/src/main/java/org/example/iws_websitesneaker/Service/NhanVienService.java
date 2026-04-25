package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for NhanVien (Employee) management - UPDATED VERSION
 *
 * QUAN TRá»ŒNG:
 * - KhÃ´ng cÃ³ chá»©c nÄƒng thÃªm nhÃ¢n viÃªn trá»±c tiáº¿p
 * - NhÃ¢n viÃªn Ä‘Æ°á»£c táº¡o tá»± Ä‘á»™ng khi táº¡o tÃ i khoáº£n
 * - Chá»‰ cho phÃ©p cáº­p nháº­t thÃ´ng tin cÆ¡ báº£n
 * - KhÃ´ng Ä‘Æ°á»£c thay Ä‘á»•i tÃ i khoáº£n liÃªn káº¿t
 * - ThÃªm há»— trá»£ pagination vÃ  search nÃ¢ng cao
 */
public interface NhanVienService {

    // ================== BASIC CRUD OPERATIONS ==================

    /**
     * Láº¥y táº¥t cáº£ nhÃ¢n viÃªn vá»›i pagination
     * @param pageable ThÃ´ng tin phÃ¢n trang vÃ  sáº¯p xáº¿p
     * @return Page chá»©a danh sÃ¡ch nhÃ¢n viÃªn
     */
    Page<NhanVien> findAllWithPagination(Pageable pageable);

    /**
     * Láº¥y táº¥t cáº£ nhÃ¢n viÃªn (khÃ´ng pagination) - Backward compatibility
     * @return Danh sÃ¡ch táº¥t cáº£ nhÃ¢n viÃªn
     */
    List<NhanVien> getAllNhanVien();

    /**
     * Láº¥y nhÃ¢n viÃªn theo ID
     * @param id ID cá»§a nhÃ¢n viÃªn
     * @return Optional chá»©a nhÃ¢n viÃªn náº¿u tÃ¬m tháº¥y
     */
    Optional<NhanVien> getNhanVienById(Integer id);

    /**
     * CHá»¨C NÄ‚NG NÃ€Y CHá»ˆ ÄÆ¯á»¢C Sá»¬ Dá»¤NG KHI Táº O Tá»ª TÃ€I KHOáº¢N
     * ThÃªm nhÃ¢n viÃªn má»›i (Ä‘Æ°á»£c gá»i tá»± Ä‘á»™ng tá»« TaiKhoanService)
     * @param nhanVien ThÃ´ng tin nhÃ¢n viÃªn cáº§n thÃªm
     */
    void addNhanVien(NhanVien nhanVien);

    /**
     * Cáº­p nháº­t thÃ´ng tin nhÃ¢n viÃªn - UPDATED
     * Chá»‰ cho phÃ©p cáº­p nháº­t: há» tÃªn, sá»‘ Ä‘iá»‡n thoáº¡i, mÃ£ nhÃ¢n viÃªn, tráº¡ng thÃ¡i
     * KHÃ”NG cho phÃ©p thay Ä‘á»•i tÃ i khoáº£n liÃªn káº¿t
     * @param nhanVien ThÃ´ng tin nhÃ¢n viÃªn cáº§n cáº­p nháº­t
     */
    void updateNhanVien(NhanVien nhanVien);

    /**
     * XÃ³a nhÃ¢n viÃªn (soft delete - chá»‰ thay Ä‘á»•i tráº¡ng thÃ¡i)
     * @param id ID cá»§a nhÃ¢n viÃªn cáº§n xÃ³a
     */
    void deleteNhanVien(Integer id);

    /**
     * XÃ³a nhiá»u nhÃ¢n viÃªn cÃ¹ng lÃºc - NEW
     * @param ids Danh sÃ¡ch ID nhÃ¢n viÃªn cáº§n xÃ³a
     */
    void batchDeleteNhanVien(List<Integer> ids);

    // ================== ADVANCED SEARCH OPERATIONS - NEW ==================

    /**
     * TÃ¬m kiáº¿m nhÃ¢n viÃªn vá»›i nhiá»u tiÃªu chÃ­ vÃ  pagination - MAIN SEARCH METHOD
     * @param globalSearch Tá»« khÃ³a tÃ¬m kiáº¿m toÃ n cá»¥c (tÃªn, email, SÄT, mÃ£ NV)
     * @param trangThai Tráº¡ng thÃ¡i nhÃ¢n viÃªn (null = táº¥t cáº£, 1 = active, 0 = inactive)
     * @param startDate NgÃ y báº¯t Ä‘áº§u (yyyy-MM-dd)
     * @param endDate NgÃ y káº¿t thÃºc (yyyy-MM-dd)
     * @param pageable ThÃ´ng tin phÃ¢n trang vÃ  sáº¯p xáº¿p
     * @return Page chá»©a káº¿t quáº£ tÃ¬m kiáº¿m
     */
    Page<NhanVien> searchWithCriteria(String globalSearch, Integer trangThai,
                                      String startDate, String endDate, Pageable pageable);

    /**
     * TÃ¬m kiáº¿m nhÃ¢n viÃªn theo tá»« khÃ³a - Simplified version
     * @param keyword Tá»« khÃ³a tÃ¬m kiáº¿m
     * @return Danh sÃ¡ch nhÃ¢n viÃªn khá»›p vá»›i tá»« khÃ³a
     */
    List<NhanVien> searchByKeyword(String keyword);

    /**
     * TÃ¬m kiáº¿m nÃ¢ng cao nhÃ¢n viÃªn vá»›i nhiá»u tiÃªu chÃ­ - Backward compatibility
     * @param hoTen Há» tÃªn (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     * @param email Email (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     * @param sdt Sá»‘ Ä‘iá»‡n thoáº¡i (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     * @param maNhanVien MÃ£ nhÃ¢n viÃªn (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     * @param diaChi Äá»‹a chá»‰ (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     * @param trangThai Tráº¡ng thÃ¡i (tÃ¬m kiáº¿m chÃ­nh xÃ¡c)
     * @param startDate NgÃ y báº¯t Ä‘áº§u (ngÃ y táº¡o >= startDate)
     * @param endDate NgÃ y káº¿t thÃºc (ngÃ y táº¡o <= endDate)
     * @return Danh sÃ¡ch nhÃ¢n viÃªn thá»a mÃ£n cÃ¡c tiÃªu chÃ­
     */
    List<NhanVien> searchAdvancedWithAllCriteria(
            String hoTen,
            String email,
            String sdt,
            String maNhanVien,
            String diaChi,
            Integer trangThai,
            Date startDate,
            Date endDate
    );

    // ================== TAI KHOAN RELATED OPERATIONS ==================

    /**
     * XÃ³a nhÃ¢n viÃªn theo ID tÃ i khoáº£n (hard delete)
     * ÄÆ°á»£c gá»i khi xÃ³a tÃ i khoáº£n
     * @param taiKhoanId ID cá»§a tÃ i khoáº£n
     */
    void deleteByTaiKhoanId(Integer taiKhoanId);

    /**
     * TÃ¬m nhÃ¢n viÃªn theo ID tÃ i khoáº£n
     * @param taiKhoanId ID cá»§a tÃ i khoáº£n
     * @return Optional chá»©a nhÃ¢n viÃªn náº¿u tÃ¬m tháº¥y
     */
    Optional<NhanVien> findByTaiKhoanId(Integer taiKhoanId);

    /**
     * TÃ¬m nhÃ¢n viÃªn theo email tÃ i khoáº£n
     * @param email Email cá»§a tÃ i khoáº£n
     * @return Optional chá»©a nhÃ¢n viÃªn náº¿u tÃ¬m tháº¥y
     */
    Optional<NhanVien> findByTaiKhoanEmail(String email);

    // ================== SIMPLE QUERY OPERATIONS ==================

    /**
     * TÃ¬m nhÃ¢n viÃªn theo mÃ£ nhÃ¢n viÃªn
     * @param maNhanVien MÃ£ nhÃ¢n viÃªn
     * @return Optional chá»©a nhÃ¢n viÃªn náº¿u tÃ¬m tháº¥y
     */
    Optional<NhanVien> findByMaNhanVien(String maNhanVien);

    /**
     * TÃ¬m nhÃ¢n viÃªn theo tráº¡ng thÃ¡i
     * @param trangThai Tráº¡ng thÃ¡i (0: nghá»‰ viá»‡c, 1: Ä‘ang lÃ m viá»‡c)
     * @return Danh sÃ¡ch nhÃ¢n viÃªn cÃ³ tráº¡ng thÃ¡i tÆ°Æ¡ng á»©ng
     */
    List<NhanVien> findByTrangThai(Integer trangThai);

    /**
     * TÃ¬m nhÃ¢n viÃªn theo tráº¡ng thÃ¡i vá»›i pagination - NEW
     * @param trangThai Tráº¡ng thÃ¡i
     * @param pageable ThÃ´ng tin phÃ¢n trang
     * @return Page chá»©a nhÃ¢n viÃªn
     */
    Page<NhanVien> findByTrangThaiWithPagination(Integer trangThai, Pageable pageable);

    // ================== VALIDATION METHODS ==================

    /**
     * Validate cÃ¡c tham sá»‘ tÃ¬m kiáº¿m nhÃ¢n viÃªn
     * @param hoTen Há» tÃªn cáº§n validate
     * @param email Email cáº§n validate
     * @param sdt Sá»‘ Ä‘iá»‡n thoáº¡i cáº§n validate
     * @param maNhanVien MÃ£ nhÃ¢n viÃªn cáº§n validate
     * @return true náº¿u táº¥t cáº£ tham sá»‘ há»£p lá»‡
     */
    boolean isValidNhanVienSearchParams(String hoTen, String email, String sdt, String maNhanVien);

    /**
     * Validate dá»¯ liá»‡u nhÃ¢n viÃªn trÆ°á»›c khi lÆ°u - NEW
     * @param nhanVien NhÃ¢n viÃªn cáº§n validate
     * @return Danh sÃ¡ch lá»—i validation (empty náº¿u há»£p lá»‡)
     */
    List<String> validateNhanVienData(NhanVien nhanVien);

    // ================== EXISTENCE CHECKS ==================

    /**
     * Kiá»ƒm tra mÃ£ nhÃ¢n viÃªn Ä‘Ã£ tá»“n táº¡i chÆ°a
     * @param maNhanVien MÃ£ nhÃ¢n viÃªn cáº§n kiá»ƒm tra
     * @return true náº¿u mÃ£ nhÃ¢n viÃªn Ä‘Ã£ tá»“n táº¡i
     */
    boolean existsByMaNhanVien(String maNhanVien);

    /**
     * Kiá»ƒm tra mÃ£ nhÃ¢n viÃªn Ä‘Ã£ tá»“n táº¡i (loáº¡i trá»« ID hiá»‡n táº¡i) - NEW
     * @param maNhanVien MÃ£ nhÃ¢n viÃªn cáº§n kiá»ƒm tra
     * @param excludeId ID nhÃ¢n viÃªn loáº¡i trá»«
     * @return true náº¿u mÃ£ nhÃ¢n viÃªn Ä‘Ã£ tá»“n táº¡i
     */
    boolean existsByMaNhanVienExcludingId(String maNhanVien, Integer excludeId);

    /**
     * Kiá»ƒm tra sá»‘ Ä‘iá»‡n thoáº¡i cÃ³ Ä‘ang Ä‘Æ°á»£c sá»­ dá»¥ng bá»Ÿi nhÃ¢n viÃªn khÃ¡c khÃ´ng
     * @param sdt Sá»‘ Ä‘iá»‡n thoáº¡i cáº§n kiá»ƒm tra
     * @param excludeId ID nhÃ¢n viÃªn loáº¡i trá»« (cho trÆ°á»ng há»£p update)
     * @return true náº¿u sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng
     */
    boolean isPhoneNumberUsed(String sdt, Integer excludeId);

    // ================== STATISTICAL METHODS ==================

    /**
     * TÃ¬m nhÃ¢n viÃªn theo phÃ²ng ban (náº¿u cÃ³)
     * @param department TÃªn phÃ²ng ban
     * @return Danh sÃ¡ch nhÃ¢n viÃªn thuá»™c phÃ²ng ban Ä‘Ã³
     */
    List<NhanVien> findByDepartment(String department);

    /**
     * Äáº¿m sá»‘ nhÃ¢n viÃªn Ä‘ang hoáº¡t Ä‘á»™ng trong khoáº£ng thá»i gian
     * @param startDate NgÃ y báº¯t Ä‘áº§u
     * @param endDate NgÃ y káº¿t thÃºc
     * @return Sá»‘ lÆ°á»£ng nhÃ¢n viÃªn active trong khoáº£ng thá»i gian
     */
    long countActiveInDateRange(Date startDate, Date endDate);

    /**
     * Láº¥y thá»‘ng kÃª nhÃ¢n viÃªn
     * @return Map chá»©a thá»‘ng kÃª: total, active, inactive, recent
     */
    Map<String, Long> getEmployeeStatistics();

    /**
     * Láº¥y thá»‘ng kÃª nhÃ¢n viÃªn vá»›i pagination context - NEW
     * @param pageable ThÃ´ng tin phÃ¢n trang hiá»‡n táº¡i
     * @return Map chá»©a thá»‘ng kÃª chi tiáº¿t
     */
    Map<String, Object> getEmployeeStatisticsWithContext(Pageable pageable);

    // ================== BUSINESS LOGIC METHODS ==================

    /**
     * Kiá»ƒm tra cÃ³ thá»ƒ xÃ³a nhÃ¢n viÃªn khÃ´ng
     * @param id ID nhÃ¢n viÃªn
     * @return true náº¿u cÃ³ thá»ƒ xÃ³a
     */
    boolean canDeleteNhanVien(Integer id);

    /**
     * Chuyá»ƒn Ä‘á»•i tráº¡ng thÃ¡i nhÃ¢n viÃªn (active/inactive)
     * @param id ID nhÃ¢n viÃªn
     */
    void toggleTrangThai(Integer id);

    /**
     * Láº¥y danh sÃ¡ch nhÃ¢n viÃªn hoáº¡t Ä‘á»™ng
     * @return Danh sÃ¡ch nhÃ¢n viÃªn cÃ³ tráº¡ng thÃ¡i = 1
     */
    List<NhanVien> getActiveNhanVien();

    /**
     * Láº¥y danh sÃ¡ch nhÃ¢n viÃªn hoáº¡t Ä‘á»™ng vá»›i pagination - NEW
     * @param pageable ThÃ´ng tin phÃ¢n trang
     * @return Page chá»©a nhÃ¢n viÃªn active
     */
    Page<NhanVien> getActiveNhanVienWithPagination(Pageable pageable);

    // ================== ADMIN DASHBOARD METHODS ==================

    /**
     * Láº¥y thá»‘ng kÃª dashboard cho ADMIN
     * @return Map chá»©a thá»‘ng kÃª dashboard
     */
    Map<String, Object> getAdminDashboardStats();

    /**
     * Láº¥y hoáº¡t Ä‘á»™ng gáº§n Ä‘Ã¢y cho ADMIN
     * @return Danh sÃ¡ch hoáº¡t Ä‘á»™ng gáº§n Ä‘Ã¢y
     */
    List<Map<String, Object>> getRecentActivities();

    /**
     * Láº¥y nhÃ¢n viÃªn má»›i trong X ngÃ y
     * @param days Sá»‘ ngÃ y
     * @return Danh sÃ¡ch nhÃ¢n viÃªn má»›i
     */
    List<NhanVien> getNewEmployees(int days);

    /**
     * Láº¥y nhÃ¢n viÃªn cáº§n ADMIN xem xÃ©t
     * @return Danh sÃ¡ch nhÃ¢n viÃªn cáº§n review
     */
    List<NhanVien> getEmployeesNeedingReview();

    /**
     * Láº¥y nhÃ¢n viÃªn cÃ³ hoáº¡t Ä‘á»™ng gáº§n Ä‘Ã¢y
     * @return Danh sÃ¡ch nhÃ¢n viÃªn active gáº§n Ä‘Ã¢y
     */
    List<NhanVien> getRecentlyActiveEmployees();

    // ================== CACHE MANAGEMENT - NEW ==================

    /**
     * XÃ³a cache liÃªn quan Ä‘áº¿n nhÃ¢n viÃªn
     */
    void clearEmployeeCache();

    /**
     * Refresh cache cho má»™t nhÃ¢n viÃªn cá»¥ thá»ƒ
     * @param employeeId ID nhÃ¢n viÃªn
     */
    void refreshEmployeeCache(Integer employeeId);

    // ================== EXPORT/IMPORT METHODS - NEW ==================

    /**
     * Xuáº¥t danh sÃ¡ch nhÃ¢n viÃªn ra Excel
     * @param searchCriteria TiÃªu chÃ­ tÃ¬m kiáº¿m Ä‘á»ƒ xuáº¥t
     * @return Byte array cá»§a file Excel
     */
    byte[] exportEmployeesToExcel(Map<String, Object> searchCriteria);

    /**
     * Láº¥y template Excel Ä‘á»ƒ import nhÃ¢n viÃªn
     * @return Byte array cá»§a template Excel
     */
    byte[] getEmployeeImportTemplate();

    // ================== PERFORMANCE MONITORING - NEW ==================

    /**
     * Láº¥y metrics hiá»‡u suáº¥t cá»§a service
     * @return Map chá»©a metrics
     */
    Map<String, Object> getPerformanceMetrics();

    boolean existsBySdt(String sdt);
}
