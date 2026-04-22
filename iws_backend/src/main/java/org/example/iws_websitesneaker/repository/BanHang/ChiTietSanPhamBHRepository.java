package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietSanPhamBHRepository extends JpaRepository<ChiTietSanPham, Integer>, JpaSpecificationExecutor<ChiTietSanPham> {

    /**
     * TÃ¬m theo mÃ£ QR
     */
    Optional<ChiTietSanPham> findByMaQR(String maQR);

    /**
     * TÃ¬m theo mÃ£ chi tiáº¿t
     */
    Optional<ChiTietSanPham> findByMaChiTiet(String maChiTiet);

    /**
     * Láº¥y sáº£n pháº©m tÆ°Æ¡ng tá»±
     */
    @Query("SELECT ctsp FROM ChiTietSanPham ctsp " +
            "WHERE (ctsp.sanPham.danhMuc.id = :danhMucId " +
            "OR ctsp.sanPham.thuongHieu.id = :thuongHieuId) " +
            "AND ctsp.id != :excludeId " +
            "AND ctsp.trangThai = 1 " +
            "AND ctsp.sanPham.trangThai = 1 " +
            "ORDER BY ctsp.ngayTao DESC")
    List<ChiTietSanPham> findSanPhamTuongTu(
            @Param("danhMucId") Integer danhMucId,
            @Param("thuongHieuId") Integer thuongHieuId,
            @Param("excludeId") Integer excludeId,
            Pageable pageable);

    /**
     * TÃ¬m theo sáº£n pháº©m vÃ  tráº¡ng thÃ¡i
     */
    List<ChiTietSanPham> findBySanPhamIdAndTrangThai(Integer sanPhamId, Integer trangThai);

    /**
     * Äáº¿m theo tráº¡ng thÃ¡i
     */
    Long countByTrangThai(Integer trangThai);

    /**
     * Láº¥y sáº£n pháº©m sáº¯p háº¿t hÃ ng
     */
    @Query("SELECT ctsp FROM ChiTietSanPham ctsp " +
            "WHERE ctsp.soLuong <= :soLuongToiThieu " +
            "AND ctsp.trangThai = 1 " +
            "ORDER BY ctsp.soLuong ASC")
    List<ChiTietSanPham> findSanPhamSapHetHang(@Param("soLuongToiThieu") Integer soLuongToiThieu);

    /**
     * TÃ¬m vá»›i filter nÃ¢ng cao
     */
    @Query("SELECT ctsp FROM ChiTietSanPham ctsp " +
            "WHERE (:keyword IS NULL OR " +
            "LOWER(ctsp.sanPham.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ctsp.maChiTiet) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:danhMucId IS NULL OR ctsp.sanPham.danhMuc.id = :danhMucId) " +
            "AND (:thuongHieuId IS NULL OR ctsp.sanPham.thuongHieu.id = :thuongHieuId) " +
            "AND (:mauSacId IS NULL OR ctsp.mauSac.id = :mauSacId) " +
            "AND (:kichCoId IS NULL OR ctsp.kichCo.id = :kichCoId) " +
            "AND ctsp.trangThai = 1 AND ctsp.sanPham.trangThai = 1")
    Page<ChiTietSanPham> findWithFilters(
            @Param("keyword") String keyword,
            @Param("danhMucId") Integer danhMucId,
            @Param("thuongHieuId") Integer thuongHieuId,
            @Param("mauSacId") Integer mauSacId,
            @Param("kichCoId") Integer kichCoId,
            Pageable pageable);
}

