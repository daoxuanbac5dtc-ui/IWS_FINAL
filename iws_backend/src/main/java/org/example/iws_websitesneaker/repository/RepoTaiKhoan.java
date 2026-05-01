package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepoTaiKhoan extends JpaRepository<TaiKhoan, Integer> {

    // ================== BASIC QUERIES ==================
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM hoa_don WHERE id_tai_khoan = :accountId", nativeQuery = true)
    void deleteAccountOrders(@Param("accountId") Integer accountId);

    /**
     * Xóa chi tiết đơn hàng của tài khoản
     */
    @Modifying
    @Transactional
    @Query(value = """
    DELETE FROM hoa_don_chi_tiet 
    WHERE id_hoa_don IN (
        SELECT id FROM hoa_don WHERE id_tai_khoan = :accountId
    )
    """, nativeQuery = true)
    void deleteAccountOrderDetails(@Param("accountId") Integer accountId);

    /**
     * Kiểm tra xem tài khoản có dữ liệu liên quan không
     */
    @Query(value = """
    SELECT COUNT(*) FROM (
        SELECT 1 FROM tai_khoan_voucher WHERE id_tai_khoan = :accountId
        UNION ALL
        SELECT 1 FROM hoa_don WHERE id_tai_khoan = :accountId
        UNION ALL
        SELECT 1 FROM dia_chi WHERE id_tai_khoan = :accountId
        UNION ALL
        SELECT 1 FROM khach_hang WHERE id_tai_khoan = :accountId
        UNION ALL
        SELECT 1 FROM nhan_vien WHERE id_tai_khoan = :accountId
    ) as related_data
    """, nativeQuery = true)
    int countRelatedData(@Param("accountId") Integer accountId);

    /**
     * Lấy danh sách các bảng có dữ liệu liên quan
     */
    @Query(value = """
    SELECT table_name FROM (
        SELECT 'tai_khoan_voucher' as table_name, COUNT(*) as count 
        FROM tai_khoan_voucher WHERE id_tai_khoan = :accountId
        UNION ALL
        SELECT 'hoa_don' as table_name, COUNT(*) as count
        FROM hoa_don WHERE id_tai_khoan = :accountId
        UNION ALL
        SELECT 'dia_chi' as table_name, COUNT(*) as count
        FROM dia_chi WHERE id_tai_khoan = :accountId
        UNION ALL
        SELECT 'khach_hang' as table_name, COUNT(*) as count
        FROM khach_hang WHERE id_tai_khoan = :accountId
        UNION ALL
        SELECT 'nhan_vien' as table_name, COUNT(*) as count
        FROM nhan_vien WHERE id_tai_khoan = :accountId
    ) as tables WHERE count > 0
    """, nativeQuery = true)
    List<String> getRelatedTables(@Param("accountId") Integer accountId);

    Optional<TaiKhoan> findByEmail(String email);

    TaiKhoan findByMaTaiKhoan(String maTaiKhoan);

    TaiKhoan findByEmailAndMatKhau(String email, String matKhau);

    // ================== FIND BY ROLE (ENUM) ==================

    List<TaiKhoan> findByVaiTro(TaiKhoan.VaiTro vaiTro);

    List<TaiKhoan> findByVaiTroAndTrangThai(TaiKhoan.VaiTro vaiTro, Integer trangThai);

    List<TaiKhoan> findByVaiTroOrderByNgayTaoDesc(TaiKhoan.VaiTro vaiTro);

    // ================== FIND BY STATUS ==================

    List<TaiKhoan> findByTrangThai(Integer trangThai);

    List<TaiKhoan> findByTrangThaiOrderByNgayTaoDesc(Integer trangThai);

    // ================== EXISTENCE CHECKS ==================

    boolean existsByEmail(String email);

    boolean existsByMaTaiKhoan(String maTaiKhoan);

    // ================== SEARCH METHODS ==================

    List<TaiKhoan> findByEmailContainingIgnoreCase(String email);

    @Query("SELECT t FROM TaiKhoan t WHERE " +
            "LOWER(t.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.maTaiKhoan) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<TaiKhoan> searchByKeyword(@Param("keyword") String keyword);

    // ================== COMPLEX SEARCH METHODS ==================

    List<TaiKhoan> findByEmailContainingAndVaiTroAndTrangThai(
            String email, TaiKhoan.VaiTro vaiTro, Integer trangThai);

    List<TaiKhoan> findByEmailContainingAndTrangThai(String email, Integer trangThai);

    // ================== COUNT METHODS ==================

    long countByVaiTro(TaiKhoan.VaiTro vaiTro);

    long countByTrangThai(Integer trangThai);

    long countByVaiTroAndTrangThai(TaiKhoan.VaiTro vaiTro, Integer trangThai);

    // ================== DATE RANGE METHODS ==================

    List<TaiKhoan> findByNgayTaoBetween(Date startDate, Date endDate);

    long countByNgayTaoBetween(Date startDate, Date endDate);

    List<TaiKhoan> findTop10ByOrderByNgayTaoDesc();

    // ================== CUSTOM QUERIES FOR DATE STATISTICS ==================

    @Query("SELECT COUNT(t) FROM TaiKhoan t WHERE DATE(t.ngayTao) = DATE(:date)")
    long countAccountsCreatedToday(@Param("date") Date date);

    @Query("SELECT COUNT(t) FROM TaiKhoan t WHERE " +
            "YEAR(t.ngayTao) = YEAR(:date) AND MONTH(t.ngayTao) = MONTH(:date)")
    long countAccountsCreatedThisMonth(@Param("date") Date date);

    // Alternative date range methods (if the above don't work with your DB)
    @Query("SELECT COUNT(t) FROM TaiKhoan t WHERE t.ngayTao BETWEEN :startDate AND :endDate")
    long countAccountsCreatedTodayByRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT COUNT(t) FROM TaiKhoan t WHERE t.ngayTao BETWEEN :startDate AND :endDate")
    long countAccountsCreatedThisMonthByRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    // ================== LATEST ACCOUNTS ==================

    @Query("SELECT t FROM TaiKhoan t ORDER BY t.ngayTao DESC")
    List<TaiKhoan> findLatestAccounts();

    // ================== SEARCH WITH MULTIPLE CRITERIA ==================

    @Query("SELECT t FROM TaiKhoan t WHERE " +
            "(:email IS NULL OR LOWER(t.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:vaiTro IS NULL OR t.vaiTro = :vaiTro) AND " +
            "(:trangThai IS NULL OR t.trangThai = :trangThai)")
    List<TaiKhoan> searchWithCriteria(
            @Param("email") String email,
            @Param("vaiTro") TaiKhoan.VaiTro vaiTro,
            @Param("trangThai") Integer trangThai);

    // ================== STATISTICS METHODS ==================

    @Query("SELECT COUNT(t) FROM TaiKhoan t WHERE t.trangThai = 1")
    long countActiveAccounts();

    @Query("SELECT t FROM TaiKhoan t WHERE t.trangThai = 1 ORDER BY t.ngayTao DESC")
    List<TaiKhoan> findAllActiveOrderByNewest();

    @Query("SELECT t FROM TaiKhoan t WHERE t.vaiTro = :vaiTro ORDER BY t.ngayTao DESC")
    List<TaiKhoan> findByRoleOrderByNewest(@Param("vaiTro") TaiKhoan.VaiTro vaiTro);

    // ================== ROLE-SPECIFIC METHODS ==================

    @Query("SELECT COUNT(t) FROM TaiKhoan t WHERE t.vaiTro = :vaiTro AND t.trangThai = 1")
    long countActiveByVaiTro(@Param("vaiTro") TaiKhoan.VaiTro vaiTro);

    @Query("SELECT t FROM TaiKhoan t WHERE t.vaiTro = :vaiTro")
    List<TaiKhoan> findByVaiTroInteger(@Param("vaiTro") TaiKhoan.VaiTro vaiTro);

    @Query("SELECT COUNT(t) FROM TaiKhoan t WHERE t.vaiTro = :vaiTro")
    long countByVaiTroInteger(@Param("vaiTro") TaiKhoan.VaiTro vaiTro);

    // ================== ADMIN SPECIFIC METHODS ==================

    @Query("SELECT COUNT(t) FROM TaiKhoan t WHERE t.vaiTro = :vaiTro AND t.trangThai = 1")
    long countActiveAdmins(@Param("vaiTro") TaiKhoan.VaiTro vaiTro);

    @Query("SELECT t FROM TaiKhoan t WHERE t.vaiTro = :vaiTro AND t.trangThai = 1")
    List<TaiKhoan> findActiveAdmins(@Param("vaiTro") TaiKhoan.VaiTro vaiTro);

    // ================== EMAIL VALIDATION METHODS ==================

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TaiKhoan t WHERE LOWER(t.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT t FROM TaiKhoan t WHERE LOWER(t.email) = LOWER(:email)")
    Optional<TaiKhoan> findByEmailIgnoreCase(@Param("email") String email);

    // ================== FIXED: DELETE ACCOUNT VOUCHERS ==================
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM tai_khoan_voucher WHERE id_tai_khoan = :accountId", nativeQuery = true)
    void deleteAccountVouchers(@Param("accountId") Integer accountId);
}

