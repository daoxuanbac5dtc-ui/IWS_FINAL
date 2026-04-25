package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import org.example.iws_websitesneaker.entity.KhuyenMai;
import org.example.iws_websitesneaker.entity.KhuyenMaiChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhuyenMaiChiTietRepository extends JpaRepository<KhuyenMaiChiTiet, Integer> {

    List<KhuyenMaiChiTiet> findByKhuyenMaiId(Integer khuyenMaiId);

    List<KhuyenMaiChiTiet> findByChiTietSanPhamId(Integer chiTietSanPhamId);

    Optional<KhuyenMaiChiTiet> findByKhuyenMaiAndChiTietSanPham(
            KhuyenMai khuyenMai, ChiTietSanPham chiTietSanPham);

    @Query("SELECT kmct FROM KhuyenMaiChiTiet kmct " +
            "WHERE kmct.chiTietSanPham.id = :chiTietSanPhamId " +
            "AND kmct.trangThai = 1")
    List<KhuyenMaiChiTiet> findActivePromotionsByProductDetail(
            @Param("chiTietSanPhamId") Integer chiTietSanPhamId);

    // Query chÃ­nh Ä‘á»ƒ láº¥y chi tiáº¿t khuyáº¿n mÃ£i vá»›i FULL thÃ´ng tin
    @Query("SELECT DISTINCT kmct FROM KhuyenMaiChiTiet kmct " +
            "LEFT JOIN FETCH kmct.khuyenMai km " +
            "LEFT JOIN FETCH kmct.chiTietSanPham ctsp " +
            "LEFT JOIN FETCH ctsp.sanPham sp " +
            "LEFT JOIN FETCH ctsp.mauSac ms " +
            "LEFT JOIN FETCH ctsp.kichCo kc " +
            "LEFT JOIN FETCH sp.thuongHieu th " +
            "LEFT JOIN FETCH sp.danhMuc dm " +
            "WHERE kmct.khuyenMai.id = :khuyenMaiId " +
            "ORDER BY kmct.ngayTao DESC")
    List<KhuyenMaiChiTiet> findDetailsByKhuyenMaiId(@Param("khuyenMaiId") Integer khuyenMaiId);

    @Modifying
    @Query("DELETE FROM KhuyenMaiChiTiet kmct WHERE kmct.khuyenMai.id = :khuyenMaiId")
    void deleteByKhuyenMaiId(@Param("khuyenMaiId") Integer khuyenMaiId);

    // Native query backup cho trÆ°á»ng há»£p cáº§n thiáº¿t
    @Query(value = "SELECT " +
            "kmct.id as kmct_id, kmct.trang_thai as kmct_trang_thai, kmct.ngay_tao as kmct_ngay_tao, " +
            "km.id as km_id, km.ma_khuyen_mai, km.ten_khuyen_mai, km.gia_tri, " +
            "ctsp.id as ctsp_id, ctsp.ma_chi_tiet, ctsp.gia_goc, ctsp.gia_ban, ctsp.so_luong, " +
            "sp.id as sp_id, sp.ten_san_pham, sp.ma_san_pham, " +
            "ms.id as ms_id, ms.ten_mau_sac, ms.ma_mau, " +
            "kc.id as kc_id, kc.ten_kich_co, " +
            "th.id as th_id, th.ten_thuong_hieu, " +
            "dm.id as dm_id, dm.ten_danh_muc " +
            "FROM khuyen_mai_chi_tiet kmct " +
            "LEFT JOIN khuyen_mai km ON kmct.khuyen_mai_id = km.id " +
            "LEFT JOIN chi_tiet_san_pham ctsp ON kmct.chi_tiet_san_pham_id = ctsp.id " +
            "LEFT JOIN san_pham sp ON ctsp.san_pham_id = sp.id " +
            "LEFT JOIN mau_sac ms ON ctsp.mau_sac_id = ms.id " +
            "LEFT JOIN kich_co kc ON ctsp.kich_co_id = kc.id " +
            "LEFT JOIN thuong_hieu th ON sp.thuong_hieu_id = th.id " +
            "LEFT JOIN danh_muc dm ON sp.danh_muc_id = dm.id " +
            "WHERE kmct.khuyen_mai_id = :khuyenMaiId " +
            "ORDER BY kmct.ngay_tao DESC",
            nativeQuery = true)
    List<Object[]> findPromotionDetailsRaw(@Param("khuyenMaiId") Integer khuyenMaiId);
}

