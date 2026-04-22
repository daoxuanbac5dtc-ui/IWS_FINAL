package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepoKhachHang extends JpaRepository<KhachHang, Integer> {

    // ===== QUERIES Má»šI Vá»šI JOIN FETCH Äá»‚ TRÃNH LazyInitializationException =====

    /**
     * Láº¥y táº¥t cáº£ khÃ¡ch hÃ ng vá»›i TaiKhoan Ä‘Æ°á»£c JOIN FETCH
     */
    @Query("SELECT DISTINCT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "WHERE kh.trangThai != 0 " +
            "ORDER BY kh.ngayTao DESC")
    List<KhachHang> findAllWithTaiKhoan();

    /**
     * TÃ¬m kiáº¿m khÃ¡ch hÃ ng vá»›i TaiKhoan Ä‘Æ°á»£c JOIN FETCH
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
     * Láº¥y má»™t khÃ¡ch hÃ ng theo ID vá»›i TaiKhoan Ä‘Æ°á»£c JOIN FETCH
     */
    @Query("SELECT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "WHERE kh.id = :id AND kh.trangThai != 0")
    Optional<KhachHang> findByIdWithTaiKhoan(@Param("id") Integer id);

    /**
     * Query phÃ¢n trang vá»›i JOIN FETCH
     */
    @Query(value = "SELECT DISTINCT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "WHERE kh.trangThai != 0",
            countQuery = "SELECT COUNT(kh) FROM KhachHang kh WHERE kh.trangThai != 0")
    Page<KhachHang> findAllWithTaiKhoan(Pageable pageable);

    /**
     * TÃ¬m kiáº¿m nÃ¢ng cao vá»›i nhiá»u Ä‘iá»u kiá»‡n
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

    // ===== QUERIES CÅ¨ GIá»® NGUYÃŠN =====

    /**
     * Kiá»ƒm tra sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ tá»“n táº¡i
     */
    boolean existsBySdt(String sdt);
    boolean existsByTaiKhoanId(Integer taiKhoanId);
    /**
     * TÃ¬m khÃ¡ch hÃ ng theo mÃ£ khÃ¡ch hÃ ng
     */
    Optional<KhachHang> findByMaKhachHang(String maKhachHang);

    /**
     * TÃ¬m khÃ¡ch hÃ ng theo ID tÃ i khoáº£n (method chÃ­nh)
     */
    Optional<KhachHang> findByTaiKhoan_Id(Integer taiKhoanId);

    /**
     * TÃ¬m khÃ¡ch hÃ ng theo ID tÃ i khoáº£n (method thay tháº¿)
     */
    @Query("SELECT k FROM KhachHang k WHERE k.taiKhoan.id = :taiKhoanId")
    Optional<KhachHang> findByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * TÃ¬m khÃ¡ch hÃ ng theo há» tÃªn vÃ  sá»‘ Ä‘iá»‡n thoáº¡i
     */
    KhachHang findByHoTenAndSdt(String hoTen, String sdt);

    /**
     * TÃ¬m kiáº¿m khÃ¡ch hÃ ng theo tá»« khÃ³a vá»›i phÃ¢n trang (khÃ´ng dÃ¹ng JOIN FETCH Ä‘á»ƒ trÃ¡nh duplicate)
     */
    @Query("SELECT k FROM KhachHang k LEFT JOIN k.taiKhoan tk WHERE " +
            "LOWER(k.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "k.sdt LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(tk.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(k.maKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<KhachHang> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * TÃ¬m kiáº¿m khÃ¡ch hÃ ng theo tá»« khÃ³a (khÃ´ng phÃ¢n trang)
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
     * Äáº¿m sá»‘ khÃ¡ch hÃ ng theo khoáº£ng thá»i gian táº¡o
     */
    Long countByNgayTaoBetween(Date startDate, Date endDate);

    /**
     * Äáº¿m sá»‘ khÃ¡ch hÃ ng theo tráº¡ng thÃ¡i
     */
    long countByTrangThai(Integer trangThai);

    /**
     * Láº¥y khÃ¡ch hÃ ng theo tráº¡ng thÃ¡i
     */
    List<KhachHang> findByTrangThai(Integer trangThai);

    /**
     * Láº¥y khÃ¡ch hÃ ng theo khoáº£ng thá»i gian táº¡o
     */
    List<KhachHang> findByNgayTaoBetween(Date startDate, Date endDate);

    /**
     * Láº¥y khÃ¡ch hÃ ng theo tráº¡ng thÃ¡i vÃ  khoáº£ng thá»i gian
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
     * Kiá»ƒm tra khÃ¡ch hÃ ng cÃ³ email cá»¥ thá»ƒ (thÃ´ng qua tÃ i khoáº£n)
     */
    @Query("SELECT COUNT(kh) > 0 FROM KhachHang kh " +
            "LEFT JOIN kh.taiKhoan tk " +
            "WHERE LOWER(tk.email) = LOWER(:email)")
    boolean existsByEmail(@Param("email") String email);

    /**
     * TÃ¬m khÃ¡ch hÃ ng theo email (thÃ´ng qua tÃ i khoáº£n)
     */
    @Query("SELECT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "WHERE LOWER(tk.email) = LOWER(:email)")
    Optional<KhachHang> findByEmail(@Param("email") String email);

    /**
     * Láº¥y danh sÃ¡ch khÃ¡ch hÃ ng cÃ³ tÃ i khoáº£n (Ä‘Ã£ Ä‘Äƒng kÃ½ online)
     */
    @Query("SELECT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "WHERE tk.id IS NOT NULL " +
            "AND kh.trangThai = 1 " +
            "ORDER BY kh.ngayTao DESC")
    List<KhachHang> findAllWithAccount();

    /**
     * Láº¥y danh sÃ¡ch khÃ¡ch hÃ ng khÃ´ng cÃ³ tÃ i khoáº£n (táº¡o tá»« admin/offline)
     */
    @Query("SELECT kh FROM KhachHang kh " +
            "WHERE kh.taiKhoan IS NULL " +
            "AND kh.trangThai = 1 " +
            "ORDER BY kh.ngayTao DESC")
    List<KhachHang> findAllWithoutAccount();
}
