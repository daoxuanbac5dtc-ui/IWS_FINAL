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

// ===== HÓA ĐƠN REPOSITORY =====

@Repository
public interface HoaDonBHRepository extends JpaRepository<HoaDon, Integer> {

    /**
     * Tìm hóa đơn theo trạng thái
     */
    List<HoaDon> findByTrangThaiHoaDon(String trangThai);

    /**
     * Tìm hóa đơn theo nhân viên
     */
    Page<HoaDon> findByNhanVienId(Integer nhanVienId, Pageable pageable);

    /**
     * Tìm hóa đơn theo loại
     */
    Page<HoaDon> findByLoaiHoaDon(String loaiHoaDon, Pageable pageable);

    /**
     * Tìm hóa đơn theo loại và khoảng thời gian
     */
    @Query("SELECT h FROM HoaDon h WHERE h.loaiHoaDon = :loaiHoaDon " +
            "AND DATE(h.ngayTao) BETWEEN :fromDate AND :toDate")
    Page<HoaDon> findByLoaiHoaDonAndDateRange(
            @Param("loaiHoaDon") String loaiHoaDon,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate,
            Pageable pageable);

    /**
     * Đếm hóa đơn theo ngày tạo
     */
    Long countByNgayTaoBetween(Date startDate, Date endDate);

    /**
     * Tính tổng doanh thu theo ngày
     */
    @Query("SELECT COALESCE(SUM(h.tongThanhToan), 0) FROM HoaDon h " +
            "WHERE h.ngayTao BETWEEN :startDate AND :endDate " +
            "AND h.trangThaiHoaDon IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')")
    Double getTongDoanhThuByNgayTao(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Tính tổng chi tiêu của khách hàng
     */
    @Query("SELECT COALESCE(SUM(h.tongThanhToan), 0) FROM HoaDon h " +
            "WHERE h.khachHang.id = :khachHangId AND h.trangThaiHoaDon IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')")
    Double getTongChiTieuByKhachHangId(@Param("khachHangId") Integer khachHangId);

    /**
     * Đếm số đơn hàng của khách hàng
     */
    Long countByKhachHangId(Integer khachHangId);

    /**
     * Tìm hóa đơn theo mã
     */
    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

}
