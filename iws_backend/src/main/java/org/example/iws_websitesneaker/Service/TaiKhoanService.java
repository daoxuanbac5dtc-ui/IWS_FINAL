package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.TaiKhoanDTO;
import org.example.iws_websitesneaker.entity.TaiKhoan;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for TaiKhoan (Account) management
 * ÄÃ£ Ä‘Æ°á»£c tá»‘i Æ°u hÃ³a vÃ  sá»­a lá»—i validation, CORS, delete
 */
public interface TaiKhoanService {

    // ================== CORE CRUD OPERATIONS ==================

    /**
     * TÃ¬m táº¥t cáº£ tÃ i khoáº£n
     */
    List<TaiKhoan> findAll();

    boolean updatePassword(String email, String newPassword);
    /**
     * TÃ¬m tÃ i khoáº£n theo ID
     */
    Optional<TaiKhoan> findById(Integer id);

    /**
     * LÆ°u tÃ i khoáº£n (create hoáº·c update)
     */
    TaiKhoan save(TaiKhoan taiKhoan);

    /**
     * XÃ³a tÃ i khoáº£n theo ID (vá»›i kiá»ƒm tra rÃ ng buá»™c an toÃ n)
     */
    void deleteById(Integer id);

    // ================== ACCOUNT CREATION - MAIN METHODS ==================

    /**
     * Táº¡o tÃ i khoáº£n hoÃ n chá»‰nh tá»« DTO (MAIN METHOD)
     * Tá»± Ä‘á»™ng táº¡o related entities (KhachHang/NhanVien/DiaChi)
     */
    Map<String, Object> createCompleteAccount(TaiKhoanDTO dto);

    /**
     * Táº¡o tÃ i khoáº£n cÆ¡ báº£n tá»« DTO (chá»‰ táº¡o TaiKhoan)
     */
    TaiKhoan createTaiKhoan(TaiKhoanDTO dto);

    /**
     * Táº¡o tÃ i khoáº£n Ä‘Æ¡n giáº£n (method helper)
     */
    TaiKhoan createAccount(String email, String password, TaiKhoan.VaiTro vaiTro);

    // ================== AUTHENTICATION & SECURITY ==================

    /**
     * ÄÄƒng nháº­p báº±ng email vÃ  máº­t kháº©u
     */
    Optional<TaiKhoan> authenticate(String email, String password);

    /**
     * Thay Ä‘á»•i máº­t kháº©u vá»›i verification
     */
    boolean changePassword(Integer id, String oldPassword, String newPassword);

    /**
     * Reset máº­t kháº©u (cho admin)
     */
    boolean resetPassword(Integer id, String newPassword);

    /**
     * Kiá»ƒm tra quyá»n truy cáº­p
     */
    boolean hasPermission(Integer accountId, String permission);

    // ================== BASIC QUERIES ==================

    /**
     * TÃ¬m theo email
     */
    Optional<TaiKhoan> findByEmail(String email);

    /**
     * TÃ¬m theo mÃ£ tÃ i khoáº£n
     */
    Optional<TaiKhoan> findByMaTaiKhoan(String maTaiKhoan);

    /**
     * Kiá»ƒm tra email tá»“n táº¡i
     */
    boolean existsByEmail(String email);

    /**
     * Kiá»ƒm tra mÃ£ tÃ i khoáº£n tá»“n táº¡i
     */
    boolean existsByMaTaiKhoan(String maTaiKhoan);

    // ================== ROLE-BASED QUERIES ==================

    /**
     * TÃ¬m theo vai trÃ²
     */
    List<TaiKhoan> findByVaiTro(TaiKhoan.VaiTro vaiTro);

    /**
     * TÃ¬m tÃ i khoáº£n khÃ¡ch hÃ ng active
     */
    List<TaiKhoan> findActiveCustomers();

    /**
     * TÃ¬m tÃ i khoáº£n nhÃ¢n viÃªn active
     */
    List<TaiKhoan> findActiveEmployees();

    /**
     * TÃ¬m tÃ i khoáº£n admin active
     */
    List<TaiKhoan> findActiveAdmins();

    // ================== STATUS MANAGEMENT ==================

    /**
     * TÃ¬m theo tráº¡ng thÃ¡i
     */
    List<TaiKhoan> findByTrangThai(Integer trangThai);

    /**
     * TÃ¬m tÃ i khoáº£n Ä‘ang hoáº¡t Ä‘á»™ng
     */
    List<TaiKhoan> findActiveAccounts();

    /**
     * Chuyá»ƒn Ä‘á»•i tráº¡ng thÃ¡i tÃ i khoáº£n
     */
    boolean toggleAccountStatus(Integer id);

    /**
     * VÃ´ hiá»‡u hÃ³a tÃ i khoáº£n
     */
    boolean deactivateAccount(Integer id);

    /**
     * KÃ­ch hoáº¡t tÃ i khoáº£n
     */
    boolean activateAccount(Integer id);

    // ================== SEARCH METHODS ==================

    /**
     * TÃ¬m kiáº¿m cÆ¡ báº£n theo tá»« khÃ³a
     */
    List<TaiKhoan> searchByKeyword(String keyword);

    /**
     * TÃ¬m kiáº¿m nÃ¢ng cao vá»›i nhiá»u tiÃªu chÃ­
     */
    List<TaiKhoan> searchAdvanced(
            String email,
            String maTaiKhoan,
            TaiKhoan.VaiTro vaiTro,
            Integer trangThai,
            Date startDate,
            Date endDate
    );

    /**
     * TÃ¬m kiáº¿m vá»›i phÃ¢n trang
     */
    Map<String, Object> searchWithPagination(
            String keyword,
            TaiKhoan.VaiTro vaiTro,
            Integer trangThai,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    // ================== STATISTICS & REPORTING ==================

    /**
     * Äáº¿m tá»•ng sá»‘ tÃ i khoáº£n
     */
    long countAll();

    /**
     * Äáº¿m theo vai trÃ²
     */
    long countByVaiTro(TaiKhoan.VaiTro vaiTro);

    /**
     * Äáº¿m theo tráº¡ng thÃ¡i
     */
    long countByTrangThai(Integer trangThai);

    /**
     * Äáº¿m tÃ i khoáº£n active theo vai trÃ²
     */
    long countActiveByVaiTro(TaiKhoan.VaiTro vaiTro);

    /**
     * Thá»‘ng kÃª tÃ i khoáº£n táº¡o hÃ´m nay
     */
    long countAccountsCreatedToday();

    /**
     * Thá»‘ng kÃª tÃ i khoáº£n táº¡o thÃ¡ng nÃ y
     */
    long countAccountsCreatedThisMonth();

    /**
     * Láº¥y thá»‘ng kÃª tá»•ng quan dashboard
     */
    Map<String, Object> getDashboardStats();

    // ================== DATE RANGE QUERIES ==================

    /**
     * TÃ¬m theo khoáº£ng thá»i gian
     */
    List<TaiKhoan> findByDateRange(Date startDate, Date endDate);

    /**
     * Láº¥y tÃ i khoáº£n má»›i nháº¥t
     */
    List<TaiKhoan> findLatestAccounts(int limit);

    /**
     * TÃ¬m tÃ i khoáº£n Ä‘Æ°á»£c táº¡o trong X ngÃ y qua
     */
    List<TaiKhoan> findRecentAccounts(int days);

    // ================== VALIDATION METHODS ==================

    /**
     * Validate DTO táº¡o tÃ i khoáº£n Ä‘áº§y Ä‘á»§
     */
    boolean validateCreateAccountDto(TaiKhoanDTO dto);

    /**
     * Validate DTO cáº­p nháº­t tÃ i khoáº£n
     */
    boolean validateUpdateAccountDto(TaiKhoanDTO dto, Integer accountId);

    /**
     * Validate email format theo chuáº©n
     */
    boolean isValidEmail(String email);

    /**
     * Validate máº­t kháº©u máº¡nh
     */
    boolean isValidPassword(String password);

    /**
     * Validate sá»‘ Ä‘iá»‡n thoáº¡i Viá»‡t Nam
     */
    boolean isValidPhoneNumber(String phone);

    /**
     * Validate tham sá»‘ tÃ¬m kiáº¿m
     */
    boolean isValidSearchParams(String email, String maTaiKhoan, String vaiTro, Integer trangThai);

    // ================== BUSINESS RULES ==================

    /**
     * Kiá»ƒm tra cÃ³ thá»ƒ xÃ³a tÃ i khoáº£n khÃ´ng (business rules)
     */
    boolean canDeleteAccount(Integer id);

    /**
     * Kiá»ƒm tra cÃ³ thá»ƒ thay Ä‘á»•i vai trÃ² khÃ´ng
     */
    boolean canChangeRole(Integer accountId, TaiKhoan.VaiTro newRole);

    /**
     * Kiá»ƒm tra cÃ³ pháº£i admin cuá»‘i cÃ¹ng Ä‘ang hoáº¡t Ä‘á»™ng khÃ´ng
     */
    boolean isLastActiveAdmin(Integer accountId);

    // ================== UTILITY METHODS ==================

    /**
     * Táº¡o mÃ£ tÃ i khoáº£n unique tá»± Ä‘á»™ng
     */
    String generateMaTaiKhoan();

    /**
     * Parse vai trÃ² tá»« string (há»— trá»£ tiáº¿ng Viá»‡t vÃ  tiáº¿ng Anh)
     */
    TaiKhoan.VaiTro parseVaiTro(String vaiTroString);

    /**
     * Chuáº©n hÃ³a email (lowercase, trim)
     */
    String normalizeEmail(String email);

    /**
     * Hash máº­t kháº©u an toÃ n
     */
    String hashPassword(String password);

    /**
     * Verify máº­t kháº©u Ä‘Ã£ hash
     */
    boolean verifyPassword(String password, String hashedPassword);

    // ================== ADDRESS RELATED ==================

    /**
     * Kiá»ƒm tra cÃ³ Ä‘á»§ dá»¯ liá»‡u Ä‘á»‹a chá»‰ khÃ´ng
     */
    boolean hasRequiredAddressData(TaiKhoanDTO dto);

    /**
     * Validate dá»¯ liá»‡u Ä‘á»‹a chá»‰
     */
    void validateAddressData(TaiKhoanDTO dto);

    // ================== ACCOUNT MAINTENANCE ==================

    /**
     * Dá»n dáº¹p tÃ i khoáº£n khÃ´ng hoáº¡t Ä‘á»™ng
     */
    int cleanupInactiveAccounts(int daysInactive);

    /**
     * Export dá»¯ liá»‡u tÃ i khoáº£n
     */
    Map<String, Object> exportAccountData(Integer accountId);

    /**
     * Ghi log hoáº¡t Ä‘á»™ng tÃ i khoáº£n
     */
    void logAccountActivity(Integer accountId, String activity, String details);
}
