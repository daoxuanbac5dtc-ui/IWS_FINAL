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

    // ✅ Đếm khách hàng mới trong khoảng thời gian
    Long countByNgayTaoBetween(Date start, Date end);

    // ✅ Tìm khách hàng theo ID với fetch
    @Query("""
        SELECT kh FROM KhachHang kh 
        LEFT JOIN FETCH kh.taiKhoan tk
        LEFT JOIN FETCH kh.viDiem vd
        WHERE kh.id = :id AND kh.trangThai = 1
    """)
    Optional<KhachHang> findByIdWithDetails(@Param("id") Integer id);

    // ✅ Lấy khách hàng theo mã
    Optional<KhachHang> findByMaKhachHangAndTrangThai(String maKhachHang, Integer trangThai);

    // ✅ Lấy khách hàng theo SĐT
    Optional<KhachHang> findBySdtAndTrangThai(String sdt, Integer trangThai);

    @Query("SELECT DISTINCT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "LEFT JOIN FETCH kh.viDiem vd " +
            "WHERE kh.trangThai = 1 " +
            "ORDER BY kh.ngayTao DESC")
    Page<KhachHang> findAllActive(Pageable pageable);

    // ✅ SỬA: Query tìm kiếm an toàn với JOIN
    @Query("SELECT DISTINCT kh FROM KhachHang kh " +
            "LEFT JOIN FETCH kh.taiKhoan tk " +
            "LEFT JOIN FETCH kh.viDiem vd " +
            "WHERE kh.trangThai = 1 " +
            "AND (LOWER(kh.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR kh.sdt LIKE CONCAT('%', :keyword, '%') " +
            "OR (tk.email IS NOT NULL AND LOWER(tk.email) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
            "ORDER BY kh.ngayTao DESC")
    Page<KhachHang> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // ✅ THÊM: Query đơn giản làm fallback khi JOIN fail
    @Query("SELECT kh FROM KhachHang kh " +
            "WHERE kh.trangThai = 1 " +
            "AND (LOWER(kh.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR kh.sdt LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY kh.ngayTao DESC")
    Page<KhachHang> searchByKeywordSimple(@Param("keyword") String keyword, Pageable pageable);

    // ✅ SỬA: Kiểm tra email tồn tại qua TaiKhoan
    @Query("SELECT COUNT(kh) > 0 FROM KhachHang kh " +
            "JOIN kh.taiKhoan tk " +
            "WHERE tk.email = :email")
    boolean existsByTaiKhoanEmail(@Param("email") String email);

    // Các method khác giữ nguyên
    boolean existsBySdt(String sdt);

    // Method tìm kiếm đơn giản theo SĐT và tên (không JOIN)
    @Query("SELECT kh FROM KhachHang kh " +
            "WHERE kh.trangThai = :trangThai " +
            "AND (LOWER(kh.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR kh.sdt LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY kh.ngayTao DESC")
    Page<KhachHang> findByKeywordAndTrangThai(@Param("keyword") String keyword,
                                              @Param("trangThai") Integer trangThai,
                                              Pageable pageable);
    // ✅ THÊM: Method fallback đơn giản
    Page<KhachHang> findByTrangThai(Integer trangThai, Pageable pageable);

    Optional<KhachHang> findByIdAndTrangThai(Integer id, Integer trangThai);
}
