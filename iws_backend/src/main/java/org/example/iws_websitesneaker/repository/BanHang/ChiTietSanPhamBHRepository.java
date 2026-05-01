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
     * Tìm theo mã QR
     */
    Optional<ChiTietSanPham> findByMaQR(String maQR);

    /**
     * Tìm theo mã chi tiết
     */
    Optional<ChiTietSanPham> findByMaChiTiet(String maChiTiet);

    /**
     * Lấy sản phẩm tương tự
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
     * Tìm theo sản phẩm và trạng thái
     */
    List<ChiTietSanPham> findBySanPhamIdAndTrangThai(Integer sanPhamId, Integer trangThai);

    /**
     * Đếm theo trạng thái
     */
    Long countByTrangThai(Integer trangThai);

    /**
     * Lấy sản phẩm sắp hết hàng
     */
    @Query("SELECT ctsp FROM ChiTietSanPham ctsp " +
            "WHERE ctsp.soLuong <= :soLuongToiThieu " +
            "AND ctsp.trangThai = 1 " +
            "ORDER BY ctsp.soLuong ASC")
    List<ChiTietSanPham> findSanPhamSapHetHang(@Param("soLuongToiThieu") Integer soLuongToiThieu);

    /**
     * Tìm với filter nâng cao
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

