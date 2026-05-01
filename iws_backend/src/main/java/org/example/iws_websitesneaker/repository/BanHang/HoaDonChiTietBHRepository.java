package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonChiTietBHRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    /**
     * Lấy chi tiết theo hóa đơn
     */
    List<HoaDonChiTiet> findByHoaDonId(Integer hoaDonId);

    /**
     * Lấy chi tiết theo hóa đơn có sắp xếp
     */
    List<HoaDonChiTiet> findByHoaDonIdOrderByNgayTao(Integer hoaDonId);

    /**
     * Tìm chi tiết theo hóa đơn và chi tiết sản phẩm
     */
    Optional<HoaDonChiTiet> findByHoaDonIdAndChiTietSanPham_Id(Integer hoaDonId, Integer chiTietSanPhamId);

    /**
     * Xóa chi tiết theo hóa đơn
     */
    void deleteByHoaDonId(Integer hoaDonId);

    /**
     * Tính tổng tiền theo hóa đơn
     */
    @Query("SELECT COALESCE(SUM(hct.gia * hct.soLuong), 0) FROM HoaDonChiTiet hct " +
            "WHERE hct.hoaDon.id = :hoaDonId")
    BigDecimal calculateTotalAmountByHoaDonId(@Param("hoaDonId") Integer hoaDonId);

    /**
     * Tính tổng số lượng bán theo ngày
     */
    @Query("SELECT COALESCE(SUM(hct.soLuong), 0) FROM HoaDonChiTiet hct " +
            "WHERE hct.hoaDon.ngayTao BETWEEN :startDate AND :endDate " +
            "AND hct.hoaDon.trangThaiHoaDon IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')")
    Long getTotalQuantityByDate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT ctsp FROM ChiTietSanPham ctsp " +
            "WHERE (ctsp.sanPham.danhMuc.id = :danhMucId " +         // Nếu dùng relationship
            "OR ctsp.sanPham.thuongHieu.id = :thuongHieuId) " +      // Nếu dùng relationship
            // "WHERE (ctsp.idSanPham IN (SELECT sp.id FROM SanPham sp WHERE sp.idDanhMuc = :danhMucId) " +  // Nếu dùng ID
            // "OR ctsp.idSanPham IN (SELECT sp.id FROM SanPham sp WHERE sp.idThuongHieu = :thuongHieuId)) " + // Nếu dùng ID
            "AND ctsp.id != :excludeId " +
            "AND ctsp.trangThai = 1 " +
            "AND ctsp.sanPham.trangThai = 1 " +                      // Nếu dùng relationship
            // "AND ctsp.idSanPham IN (SELECT sp.id FROM SanPham sp WHERE sp.trangThai = 1) " + // Nếu dùng ID
            "ORDER BY ctsp.ngayTao DESC")
    List<ChiTietSanPham> findSanPhamTuongTu(
            @Param("danhMucId") Integer danhMucId,
            @Param("thuongHieuId") Integer thuongHieuId,
            @Param("excludeId") Integer excludeId,
            Pageable pageable);
}
