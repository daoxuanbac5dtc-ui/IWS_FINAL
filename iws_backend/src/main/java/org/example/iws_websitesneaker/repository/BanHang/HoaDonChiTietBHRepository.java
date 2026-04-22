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
     * Láº¥y chi tiáº¿t theo hÃ³a Ä‘Æ¡n
     */
    List<HoaDonChiTiet> findByHoaDonId(Integer hoaDonId);

    /**
     * Láº¥y chi tiáº¿t theo hÃ³a Ä‘Æ¡n cÃ³ sáº¯p xáº¿p
     */
    List<HoaDonChiTiet> findByHoaDonIdOrderByNgayTao(Integer hoaDonId);

    /**
     * TÃ¬m chi tiáº¿t theo hÃ³a Ä‘Æ¡n vÃ  chi tiáº¿t sáº£n pháº©m
     */
    Optional<HoaDonChiTiet> findByHoaDonIdAndChiTietSanPham_Id(Integer hoaDonId, Integer chiTietSanPhamId);

    /**
     * XÃ³a chi tiáº¿t theo hÃ³a Ä‘Æ¡n
     */
    void deleteByHoaDonId(Integer hoaDonId);

    /**
     * TÃ­nh tá»•ng tiá»n theo hÃ³a Ä‘Æ¡n
     */
    @Query("SELECT COALESCE(SUM(hct.gia * hct.soLuong), 0) FROM HoaDonChiTiet hct " +
            "WHERE hct.hoaDon.id = :hoaDonId")
    BigDecimal calculateTotalAmountByHoaDonId(@Param("hoaDonId") Integer hoaDonId);

    /**
     * TÃ­nh tá»•ng sá»‘ lÆ°á»£ng bÃ¡n theo ngÃ y
     */
    @Query("SELECT COALESCE(SUM(hct.soLuong), 0) FROM HoaDonChiTiet hct " +
            "WHERE hct.hoaDon.ngayTao BETWEEN :startDate AND :endDate " +
            "AND hct.hoaDon.trangThaiHoaDon = 'DA_THANH_TOAN'")
    Long getTotalQuantityByDate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT ctsp FROM ChiTietSanPham ctsp " +
            "WHERE (ctsp.sanPham.danhMuc.id = :danhMucId " +         // Náº¿u dÃ¹ng relationship
            "OR ctsp.sanPham.thuongHieu.id = :thuongHieuId) " +      // Náº¿u dÃ¹ng relationship
            // "WHERE (ctsp.idSanPham IN (SELECT sp.id FROM SanPham sp WHERE sp.idDanhMuc = :danhMucId) " +  // Náº¿u dÃ¹ng ID
            // "OR ctsp.idSanPham IN (SELECT sp.id FROM SanPham sp WHERE sp.idThuongHieu = :thuongHieuId)) " + // Náº¿u dÃ¹ng ID
            "AND ctsp.id != :excludeId " +
            "AND ctsp.trangThai = 1 " +
            "AND ctsp.sanPham.trangThai = 1 " +                      // Náº¿u dÃ¹ng relationship
            // "AND ctsp.idSanPham IN (SELECT sp.id FROM SanPham sp WHERE sp.trangThai = 1) " + // Náº¿u dÃ¹ng ID
            "ORDER BY ctsp.ngayTao DESC")
    List<ChiTietSanPham> findSanPhamTuongTu(
            @Param("danhMucId") Integer danhMucId,
            @Param("thuongHieuId") Integer thuongHieuId,
            @Param("excludeId") Integer excludeId,
            Pageable pageable);
}
