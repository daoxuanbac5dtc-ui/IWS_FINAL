package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.TaiKhoanDTO;
import org.example.iws_websitesneaker.entity.TaiKhoan;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for TaiKhoan (Account) management
 * Đã được tối ưu hóa và sửa lỗi validation, CORS, delete
 */
public interface TaiKhoanService {

    // ================== CORE CRUD OPERATIONS ==================

    /**
     * Tìm tất cả tài khoản
     */
    List<TaiKhoan> findAll();

    boolean updatePassword(String email, String newPassword);
    /**
     * Tìm tài khoản theo ID
     */
    Optional<TaiKhoan> findById(Integer id);

    /**
     * Lưu tài khoản (create hoặc update)
     */
    TaiKhoan save(TaiKhoan taiKhoan);

    /**
     * Xóa tài khoản theo ID (với kiểm tra ràng buộc an toàn)
     */
    void deleteById(Integer id);

    // ================== ACCOUNT CREATION - MAIN METHODS ==================

    /**
     * Tạo tài khoản hoàn chỉnh từ DTO (MAIN METHOD)
     * Tự động tạo related entities (KhachHang/NhanVien/DiaChi)
     */
    Map<String, Object> createCompleteAccount(TaiKhoanDTO dto);

    /**
     * Tạo tài khoản cơ bản từ DTO (chỉ tạo TaiKhoan)
     */
    TaiKhoan createTaiKhoan(TaiKhoanDTO dto);

    /**
     * Tạo tài khoản đơn giản (method helper)
     */
    TaiKhoan createAccount(String email, String password, TaiKhoan.VaiTro vaiTro);

    // ================== AUTHENTICATION & SECURITY ==================

    /**
     * Đăng nhập bằng email và mật khẩu
     */
    Optional<TaiKhoan> authenticate(String email, String password);

    /**
     * Thay đổi mật khẩu với verification
     */
    boolean changePassword(Integer id, String oldPassword, String newPassword);

    /**
     * Reset mật khẩu (cho admin)
     */
    boolean resetPassword(Integer id, String newPassword);

    /**
     * Kiểm tra quyền truy cập
     */
    boolean hasPermission(Integer accountId, String permission);

    // ================== BASIC QUERIES ==================

    /**
     * Tìm theo email
     */
    Optional<TaiKhoan> findByEmail(String email);

    /**
     * Tìm theo mã tài khoản
     */
    Optional<TaiKhoan> findByMaTaiKhoan(String maTaiKhoan);

    /**
     * Kiểm tra email tồn tại
     */
    boolean existsByEmail(String email);

    /**
     * Kiểm tra mã tài khoản tồn tại
     */
    boolean existsByMaTaiKhoan(String maTaiKhoan);

    // ================== ROLE-BASED QUERIES ==================

    /**
     * Tìm theo vai trò
     */
    List<TaiKhoan> findByVaiTro(TaiKhoan.VaiTro vaiTro);

    /**
     * Tìm tài khoản khách hàng active
     */
    List<TaiKhoan> findActiveCustomers();

    /**
     * Tìm tài khoản nhân viên active
     */
    List<TaiKhoan> findActiveEmployees();

    /**
     * Tìm tài khoản admin active
     */
    List<TaiKhoan> findActiveAdmins();

    // ================== STATUS MANAGEMENT ==================

    /**
     * Tìm theo trạng thái
     */
    List<TaiKhoan> findByTrangThai(Integer trangThai);

    /**
     * Tìm tài khoản đang hoạt động
     */
    List<TaiKhoan> findActiveAccounts();

    /**
     * Chuyển đổi trạng thái tài khoản
     */
    boolean toggleAccountStatus(Integer id);

    /**
     * Vô hiệu hóa tài khoản
     */
    boolean deactivateAccount(Integer id);

    /**
     * Kích hoạt tài khoản
     */
    boolean activateAccount(Integer id);

    // ================== SEARCH METHODS ==================

    /**
     * Tìm kiếm cơ bản theo từ khóa
     */
    List<TaiKhoan> searchByKeyword(String keyword);

    /**
     * Tìm kiếm nâng cao với nhiều tiêu chí
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
     * Tìm kiếm với phân trang
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
     * Đếm tổng số tài khoản
     */
    long countAll();

    /**
     * Đếm theo vai trò
     */
    long countByVaiTro(TaiKhoan.VaiTro vaiTro);

    /**
     * Đếm theo trạng thái
     */
    long countByTrangThai(Integer trangThai);

    /**
     * Đếm tài khoản active theo vai trò
     */
    long countActiveByVaiTro(TaiKhoan.VaiTro vaiTro);

    /**
     * Thống kê tài khoản tạo hôm nay
     */
    long countAccountsCreatedToday();

    /**
     * Thống kê tài khoản tạo tháng này
     */
    long countAccountsCreatedThisMonth();

    /**
     * Lấy thống kê tổng quan dashboard
     */
    Map<String, Object> getDashboardStats();

    // ================== DATE RANGE QUERIES ==================

    /**
     * Tìm theo khoảng thời gian
     */
    List<TaiKhoan> findByDateRange(Date startDate, Date endDate);

    /**
     * Lấy tài khoản mới nhất
     */
    List<TaiKhoan> findLatestAccounts(int limit);

    /**
     * Tìm tài khoản được tạo trong X ngày qua
     */
    List<TaiKhoan> findRecentAccounts(int days);

    // ================== VALIDATION METHODS ==================

    /**
     * Validate DTO tạo tài khoản đầy đủ
     */
    boolean validateCreateAccountDto(TaiKhoanDTO dto);

    /**
     * Validate DTO cập nhật tài khoản
     */
    boolean validateUpdateAccountDto(TaiKhoanDTO dto, Integer accountId);

    /**
     * Validate email format theo chuẩn
     */
    boolean isValidEmail(String email);

    /**
     * Validate mật khẩu mạnh
     */
    boolean isValidPassword(String password);

    /**
     * Validate số điện thoại Việt Nam
     */
    boolean isValidPhoneNumber(String phone);

    /**
     * Validate tham số tìm kiếm
     */
    boolean isValidSearchParams(String email, String maTaiKhoan, String vaiTro, Integer trangThai);

    // ================== BUSINESS RULES ==================

    /**
     * Kiểm tra có thể xóa tài khoản không (business rules)
     */
    boolean canDeleteAccount(Integer id);

    /**
     * Kiểm tra có thể thay đổi vai trò không
     */
    boolean canChangeRole(Integer accountId, TaiKhoan.VaiTro newRole);

    /**
     * Kiểm tra có phải admin cuối cùng đang hoạt động không
     */
    boolean isLastActiveAdmin(Integer accountId);

    // ================== UTILITY METHODS ==================

    /**
     * Tạo mã tài khoản unique tự động
     */
    String generateMaTaiKhoan();

    /**
     * Parse vai trò từ string (hỗ trợ tiếng Việt và tiếng Anh)
     */
    TaiKhoan.VaiTro parseVaiTro(String vaiTroString);

    /**
     * Chuẩn hóa email (lowercase, trim)
     */
    String normalizeEmail(String email);

    /**
     * Hash mật khẩu an toàn
     */
    String hashPassword(String password);

    /**
     * Verify mật khẩu đã hash
     */
    boolean verifyPassword(String password, String hashedPassword);

    // ================== ADDRESS RELATED ==================

    /**
     * Kiểm tra có đủ dữ liệu địa chỉ không
     */
    boolean hasRequiredAddressData(TaiKhoanDTO dto);

    /**
     * Validate dữ liệu địa chỉ
     */
    void validateAddressData(TaiKhoanDTO dto);

    // ================== ACCOUNT MAINTENANCE ==================

    /**
     * Dọn dẹp tài khoản không hoạt động
     */
    int cleanupInactiveAccounts(int daysInactive);

    /**
     * Export dữ liệu tài khoản
     */
    Map<String, Object> exportAccountData(Integer accountId);

    /**
     * Ghi log hoạt động tài khoản
     */
    void logAccountActivity(Integer accountId, String activity, String details);
}
