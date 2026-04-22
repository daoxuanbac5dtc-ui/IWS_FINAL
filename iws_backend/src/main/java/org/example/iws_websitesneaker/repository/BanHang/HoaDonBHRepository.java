package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

// ===== HÃ“A ÄÆ N REPOSITORY =====

@Repository
public interface HoaDonBHRepository extends JpaRepository<HoaDon, Integer> {

    /**
     * TÃ¬m hÃ³a Ä‘Æ¡n theo tráº¡ng thÃ¡i
     */
    List<HoaDon> findByTrangThaiHoaDon(String trangThai);

    /**
     * TÃ¬m hÃ³a Ä‘Æ¡n theo nhÃ¢n viÃªn
     */
    Page<HoaDon> findByNhanVienId(Integer nhanVienId, Pageable pageable);

    /**
     * TÃ¬m hÃ³a Ä‘Æ¡n theo loáº¡i
     */
    Page<HoaDon> findByLoaiHoaDon(String loaiHoaDon, Pageable pageable);

    /**
     * TÃ¬m hÃ³a Ä‘Æ¡n theo loáº¡i vÃ  khoáº£ng thá»i gian
     */
    @Query("SELECT h FROM HoaDon h WHERE h.loaiHoaDon = :loaiHoaDon " +
            "AND DATE(h.ngayTao) BETWEEN :fromDate AND :toDate")
    Page<HoaDon> findByLoaiHoaDonAndDateRange(
            @Param("loaiHoaDon") String loaiHoaDon,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate,
            Pageable pageable);

    /**
     * Äáº¿m hÃ³a Ä‘Æ¡n theo ngÃ y táº¡o
     */
    Long countByNgayTaoBetween(Date startDate, Date endDate);

    /**
     * TÃ­nh tá»•ng doanh thu theo ngÃ y
     */
    @Query("SELECT COALESCE(SUM(h.tongThanhToan), 0) FROM HoaDon h " +
            "WHERE h.ngayTao BETWEEN :startDate AND :endDate " +
            "AND h.trangThaiHoaDon = 'DA_THANH_TOAN'")
    Double getTongDoanhThuByNgayTao(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * TÃ­nh tá»•ng chi tiÃªu cá»§a khÃ¡ch hÃ ng
     */
    @Query("SELECT COALESCE(SUM(h.tongThanhToan), 0) FROM HoaDon h " +
            "WHERE h.khachHang.id = :khachHangId AND h.trangThaiHoaDon = 'DA_THANH_TOAN'")
    Double getTongChiTieuByKhachHangId(@Param("khachHangId") Integer khachHangId);

    /**
     * Äáº¿m sá»‘ Ä‘Æ¡n hÃ ng cá»§a khÃ¡ch hÃ ng
     */
    Long countByKhachHangId(Integer khachHangId);

    /**
     * TÃ¬m hÃ³a Ä‘Æ¡n theo mÃ£
     */
    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

}
