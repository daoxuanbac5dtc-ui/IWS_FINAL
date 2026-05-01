package org.example.iws_websitesneaker.repository;
import org.example.iws_websitesneaker.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    @Query("SELECT hd FROM HoaDon hd WHERE hd.loaiHoaDon = 'OFFLINE' ORDER BY hd.ngayTao DESC")
    List<HoaDon> findOfflineOrders();

    @Query("SELECT hd FROM HoaDon hd WHERE hd.nhanVien.id = :nhanVienId AND hd.loaiHoaDon = 'OFFLINE' " +
            "AND DATE(hd.ngayTao) = DATE(:ngay) ORDER BY hd.ngayTao DESC")
    List<HoaDon> findByNhanVienAndDate(@Param("nhanVienId") Integer nhanVienId, @Param("ngay") Date ngay);

    @Query("SELECT COUNT(hd) FROM HoaDon hd WHERE hd.maHoaDon LIKE :prefix%")
    Long countByMaHoaDonPrefix(@Param("prefix") String prefix);


    Page<HoaDon> findByNhanVienId(Integer nhanVienId, Pageable pageable);

    Page<HoaDon> findByLoaiHoaDon(String loaiHoaDon, Pageable pageable);

    @Query("SELECT h FROM HoaDon h WHERE h.loaiHoaDon = :loaiHoaDon AND h.ngayTao BETWEEN :fromDate AND :toDate")
    Page<HoaDon> findByLoaiHoaDonAndDateRange(@Param("loaiHoaDon") String loaiHoaDon,
                                              @Param("fromDate") String fromDate,
                                              @Param("toDate") String toDate,
                                              Pageable pageable);

    Long countByNgayTaoBetween(Date startDate, Date endDate);

    @Query("SELECT COALESCE(SUM(h.tongThanhToan), 0) FROM HoaDon h WHERE h.ngayTao BETWEEN :startDate AND :endDate AND h.trangThaiHoaDon IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')")
    Double getTongDoanhThuByNgayTao(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT COALESCE(SUM(h.tongThanhToan), 0) FROM HoaDon h WHERE h.khachHang.id = :khachHangId AND h.trangThaiHoaDon IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')")
    Double getTongChiTieuByKhachHangId(@Param("khachHangId") Integer khachHangId);

    Long countByKhachHangId(Integer khachHangId);

    List<HoaDon> findByTrangThaiHoaDon(String trangThaiHoaDon);
}

