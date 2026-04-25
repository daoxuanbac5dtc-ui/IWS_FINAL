package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface KhachHangBHRepository extends JpaRepository<KhachHang, Integer> {

    // âœ… Äáº¿m khÃ¡ch hÃ ng má»›i trong khoáº£ng thá»i gian
    Long countByNgayTaoBetween(Date start, Date end);

    // âœ… TÃ¬m khÃ¡ch hÃ ng theo ID vá»›i fetch
    @Query("""
        SELECT kh FROM KhachHang kh 
        LEFT JOIN FETCH kh.taiKhoan tk
        LEFT JOIN FETCH kh.viDiem vd
        WHERE kh.id = :id AND kh.trangThai = 1
    """)
    Optional<KhachHang> findByIdWithDetails(@Param("id") Integer id);

    // âœ… Láº¥y khÃ¡ch hÃ ng theo mÃ£
    Optional<KhachHang> findByMaKhachHangAndTrangThai(String maKhachHang, Integer trangThai);

    // âœ… Láº¥y khÃ¡ch hÃ ng theo SÄT
    Optional<KhachHang> findBySdtAndTrangThai(String sdt, Integer trangThai);

    @Query("SELECT DISTINCT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "LEFT JOIN FETCH kh.viDiem vd " +
            "WHERE kh.trangThai = 1 " +
            "ORDER BY kh.ngayTao DESC")
    Page<KhachHang> findAllActive(Pageable pageable);

    // âœ… Sá»¬A: Query tÃ¬m kiáº¿m an toÃ n vá»›i JOIN
    @Query("SELECT DISTINCT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "LEFT JOIN FETCH kh.viDiem vd " +
            "WHERE kh.trangThai = 1 " +
            "AND (LOWER(kh.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR kh.sdt LIKE CONCAT('%', :keyword, '%') " +
            "OR (tk.email IS NOT NULL AND LOWER(tk.email) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
            "ORDER BY kh.ngayTao DESC")
    Page<KhachHang> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // âœ… THÃŠM: Query Ä‘Æ¡n giáº£n lÃ m fallback khi JOIN fail
    @Query("SELECT kh FROM KhachHang kh " +
            "WHERE kh.trangThai = 1 " +
            "AND (LOWER(kh.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR kh.sdt LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY kh.ngayTao DESC")
    Page<KhachHang> searchByKeywordSimple(@Param("keyword") String keyword, Pageable pageable);

    // âœ… Sá»¬A: Kiá»ƒm tra email tá»“n táº¡i qua TaiKhoan
    @Query("SELECT COUNT(kh) > 0 FROM KhachHang kh " +
            "JOIN kh.taiKhoan tk " +
            "WHERE tk.email = :email")
    boolean existsByTaiKhoanEmail(@Param("email") String email);

    // CÃ¡c method khÃ¡c giá»¯ nguyÃªn
    boolean existsBySdt(String sdt);

    // Method tÃ¬m kiáº¿m Ä‘Æ¡n giáº£n theo SÄT vÃ  tÃªn (khÃ´ng JOIN)
    @Query("SELECT kh FROM KhachHang kh " +
            "WHERE kh.trangThai = :trangThai " +
            "AND (LOWER(kh.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR kh.sdt LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY kh.ngayTao DESC")
    Page<KhachHang> findByKeywordAndTrangThai(@Param("keyword") String keyword,
                                              @Param("trangThai") Integer trangThai,
                                              Pageable pageable);
    // âœ… THÃŠM: Method fallback Ä‘Æ¡n giáº£n
    Page<KhachHang> findByTrangThai(Integer trangThai, Pageable pageable);

    Optional<KhachHang> findByIdAndTrangThai(Integer id, Integer trangThai);
}
