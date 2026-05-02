package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepoKhachHang extends JpaRepository<KhachHang, Integer> {

    // ===== QUERIES MỚI VỚI JOIN FETCH ĐỂ TRÁNH LazyInitializationException =====

    /**
     * Lấy tất cả khách hàng với TaiKhoan được JOIN FETCH
     */
    @Query("SELECT DISTINCT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "WHERE kh.trangThai != 0 " +
            "ORDER BY kh.ngayTao DESC")
    List<KhachHang> findAllWithTaiKhoan();

    /**
     * Tìm kiếm khách hàng với TaiKhoan được JOIN FETCH
     */
    @Query("SELECT DISTINCT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(kh.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(kh.sdt) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(tk.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(kh.maKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:trangThai IS NULL OR kh.trangThai = :trangThai) " +
            "AND kh.trangThai != 0 " +
            "ORDER BY kh.ngayTao DESC")
    List<KhachHang> searchWithTaiKhoan(
            @Param("keyword") String keyword,
            @Param("trangThai") Integer trangThai
    );

    /**
     * Lấy một khách hàng theo ID với TaiKhoan được JOIN FETCH
     */
    @Query("SELECT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "WHERE kh.id = :id")
    Optional<KhachHang> findByIdWithTaiKhoan(@Param("id") Integer id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE KhachHang kh SET kh.trangThai = :trangThai, kh.ngayCapNhat = :ngayCapNhat WHERE kh.id = :id")
    int updateTrangThaiById(@Param("id") Integer id,
                            @Param("trangThai") Integer trangThai,
                            @Param("ngayCapNhat") Date ngayCapNhat);

    /**
     * Query phân trang với JOIN FETCH
     */
    @Query(value = "SELECT DISTINCT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "WHERE kh.trangThai != 0",
            countQuery = "SELECT COUNT(kh) FROM KhachHang kh WHERE kh.trangThai != 0")
    Page<KhachHang> findAllWithTaiKhoan(Pageable pageable);

    /**
     * Tìm kiếm nâng cao với nhiều điều kiện
     */
    @Query("SELECT DISTINCT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "WHERE (:hoTen IS NULL OR :hoTen = '' OR LOWER(kh.hoTen) LIKE LOWER(CONCAT('%', :hoTen, '%'))) " +
            "AND (:email IS NULL OR :email = '' OR LOWER(tk.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (:sdt IS NULL OR :sdt = '' OR kh.sdt LIKE CONCAT('%', :sdt, '%')) " +
            "AND (:maKhachHang IS NULL OR :maKhachHang = '' OR LOWER(kh.maKhachHang) LIKE LOWER(CONCAT('%', :maKhachHang, '%'))) " +
            "AND (:trangThai IS NULL OR kh.trangThai = :trangThai) " +
            "AND kh.trangThai != 0 " +
            "ORDER BY kh.ngayTao DESC")
    List<KhachHang> searchAdvancedWithTaiKhoan(
            @Param("hoTen") String hoTen,
            @Param("email") String email,
            @Param("sdt") String sdt,
            @Param("maKhachHang") String maKhachHang,
            @Param("trangThai") Integer trangThai
    );

    // ===== QUERIES CŨ GIỮ NGUYÊN =====

    /**
     * Kiểm tra số điện thoại đã tồn tại
     */
    boolean existsBySdt(String sdt);
    boolean existsByTaiKhoanId(Integer taiKhoanId);
    /**
     * Tìm khách hàng theo mã khách hàng
     */
    Optional<KhachHang> findByMaKhachHang(String maKhachHang);

    /**
     * Tìm khách hàng theo ID tài khoản (method chính)
     */
    Optional<KhachHang> findByTaiKhoan_Id(Integer taiKhoanId);

    /**
     * Tìm khách hàng theo ID tài khoản (method thay thế)
     */
    @Query("SELECT k FROM KhachHang k WHERE k.taiKhoan.id = :taiKhoanId")
    Optional<KhachHang> findByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * Tìm khách hàng theo họ tên và số điện thoại
     */
    KhachHang findByHoTenAndSdt(String hoTen, String sdt);

    /**
     * Tìm kiếm khách hàng theo từ khóa với phân trang (không dùng JOIN FETCH để tránh duplicate)
     */
    @Query("SELECT k FROM KhachHang k LEFT JOIN k.taiKhoan tk WHERE " +
            "LOWER(k.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "k.sdt LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(tk.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(k.maKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<KhachHang> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Tìm kiếm khách hàng theo từ khóa (không phân trang)
     */
    @Query("SELECT kh FROM KhachHang kh " +
            "LEFT JOIN kh.taiKhoan tk " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(kh.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(kh.sdt) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(tk.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(kh.maKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<KhachHang> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Đếm số khách hàng theo khoảng thời gian tạo
     */
    Long countByNgayTaoBetween(Date startDate, Date endDate);

    /**
     * Đếm số khách hàng theo trạng thái
     */
    long countByTrangThai(Integer trangThai);

    /**
     * Lấy khách hàng theo trạng thái
     */
    List<KhachHang> findByTrangThai(Integer trangThai);

    /**
     * Lấy khách hàng theo khoảng thời gian tạo
     */
    List<KhachHang> findByNgayTaoBetween(Date startDate, Date endDate);

    /**
     * Lấy khách hàng theo trạng thái và khoảng thời gian
     */
    @Query("SELECT kh FROM KhachHang kh WHERE " +
            "(:trangThai IS NULL OR kh.trangThai = :trangThai) " +
            "AND (:startDate IS NULL OR kh.ngayTao >= :startDate) " +
            "AND (:endDate IS NULL OR kh.ngayTao <= :endDate) " +
            "ORDER BY kh.ngayTao DESC")
    List<KhachHang> findByTrangThaiAndDateRange(
            @Param("trangThai") Integer trangThai,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * Kiểm tra khách hàng có email cụ thể (thông qua tài khoản)
     */
    @Query("SELECT COUNT(kh) > 0 FROM KhachHang kh " +
            "LEFT JOIN kh.taiKhoan tk " +
            "WHERE LOWER(tk.email) = LOWER(:email)")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Tìm khách hàng theo email (thông qua tài khoản)
     */
    @Query("SELECT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "WHERE LOWER(tk.email) = LOWER(:email)")
    Optional<KhachHang> findByEmail(@Param("email") String email);

    /**
     * Lấy danh sách khách hàng có tài khoản (đã đăng ký online)
     */
    @Query("SELECT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "WHERE tk.id IS NOT NULL " +
            "AND kh.trangThai = 1 " +
            "ORDER BY kh.ngayTao DESC")
    List<KhachHang> findAllWithAccount();

    /**
     * Lấy danh sách khách hàng không có tài khoản (tạo từ admin/offline)
     */
    @Query("SELECT kh FROM KhachHang kh " +
            "WHERE kh.taiKhoan IS NULL " +
            "AND kh.trangThai = 1 " +
            "ORDER BY kh.ngayTao DESC")
    List<KhachHang> findAllWithoutAccount();
}
