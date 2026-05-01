package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {

    // Basic queries
    List<ChiTietSanPham> findByTrangThai(Integer trangThai);

    @Query("SELECT COUNT(ctsp) FROM ChiTietSanPham ctsp WHERE ctsp.trangThai = 1")
    Long countActiveProducts();

    // Simple active products query
    @Query("SELECT ctsp FROM ChiTietSanPham ctsp WHERE ctsp.trangThai = 1 AND ctsp.soLuong > 0")
    List<ChiTietSanPham> findActiveProducts();

    // Products with full information using LEFT JOIN FETCH
    @Query("SELECT DISTINCT ctsp FROM ChiTietSanPham ctsp " +
            "LEFT JOIN FETCH ctsp.sanPham sp " +
            "LEFT JOIN FETCH ctsp.mauSac ms " +
            "LEFT JOIN FETCH ctsp.kichCo kc " +
            "LEFT JOIN FETCH sp.thuongHieu th " +
            "LEFT JOIN FETCH sp.danhMuc dm " +
            "WHERE ctsp.trangThai = 1 AND ctsp.soLuong > 0 AND ctsp.giaGoc > 0")
    List<ChiTietSanPham> findActiveProductsWithDetails();

    // Products without active promotion - WITH FULL DETAILS
    @Query("SELECT DISTINCT ctsp FROM ChiTietSanPham ctsp " +
            "LEFT JOIN FETCH ctsp.sanPham sp " +
            "LEFT JOIN FETCH ctsp.mauSac ms " +
            "LEFT JOIN FETCH ctsp.kichCo kc " +
            "LEFT JOIN FETCH sp.thuongHieu th " +
            "LEFT JOIN FETCH sp.danhMuc dm " +
            "WHERE ctsp.trangThai = 1 AND ctsp.soLuong > 0 AND ctsp.giaGoc > 0 " +
            "AND NOT EXISTS (SELECT 1 FROM KhuyenMaiChiTiet kmct " +
            "WHERE kmct.chiTietSanPham.id = ctsp.id AND kmct.trangThai = 1)")
    List<ChiTietSanPham> findProductsWithoutActivePromotion();

    // Search products - WITH FULL DETAILS
    @Query("SELECT DISTINCT ctsp FROM ChiTietSanPham ctsp " +
            "LEFT JOIN FETCH ctsp.sanPham sp " +
            "LEFT JOIN FETCH ctsp.mauSac ms " +
            "LEFT JOIN FETCH ctsp.kichCo kc " +
            "LEFT JOIN FETCH sp.thuongHieu th " +
            "LEFT JOIN FETCH sp.danhMuc dm " +
            "WHERE ctsp.trangThai = 1 AND ctsp.soLuong > 0 AND ctsp.giaGoc > 0 " +
            "AND (LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(ctsp.maChiTiet) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(sp.maSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(th.tenThuongHieu) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(dm.tenDanhMuc) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ChiTietSanPham> searchByKeyword(@Param("keyword") String keyword);

    // Native query backup cho trường hợp JOIN FETCH không work
    @Query(value = "SELECT " +
            "ctsp.id, ctsp.ma_chi_tiet, ctsp.gia_goc, ctsp.gia_ban, ctsp.so_luong, ctsp.trang_thai, " +
            "sp.id as sp_id, sp.ten_san_pham, sp.ma_san_pham, " +
            "ms.id as ms_id, ms.ten_mau_sac, ms.ma_mau, " +
            "kc.id as kc_id, kc.ten_kich_co, " +
            "th.id as th_id, th.ten_thuong_hieu, " +
            "dm.id as dm_id, dm.ten_danh_muc " +
            "FROM chi_tiet_san_pham ctsp " +
            "LEFT JOIN san_pham sp ON ctsp.san_pham_id = sp.id " +
            "LEFT JOIN mau_sac ms ON ctsp.mau_sac_id = ms.id " +
            "LEFT JOIN kich_co kc ON ctsp.kich_co_id = kc.id " +
            "LEFT JOIN thuong_hieu th ON sp.thuong_hieu_id = th.id " +
            "LEFT JOIN danh_muc dm ON sp.danh_muc_id = dm.id " +
            "WHERE ctsp.trang_thai = 1 AND ctsp.so_luong > 0 AND ctsp.gia_goc > 0 " +
            "AND NOT EXISTS (SELECT 1 FROM khuyen_mai_chi_tiet kmct " +
            "WHERE kmct.chi_tiet_san_pham_id = ctsp.id AND kmct.trang_thai = 1)",
            nativeQuery = true)
    List<Object[]> findAvailableProductsRaw();
    // Tìm theo sản phẩm
    List<ChiTietSanPham> findBySanPhamId(Integer sanPhamId);

    // Tìm theo màu sắc
    List<ChiTietSanPham> findByMauSacId(Integer mauSacId);

    // Tìm theo kích thước
    List<ChiTietSanPham> findByKichCoId(Integer kichCoId);

    // Tìm theo sản phẩm, màu và size
    @Query("SELECT ctsp FROM ChiTietSanPham ctsp WHERE ctsp.sanPham.id = :sanPhamId AND ctsp.mauSac.id = :mauSacId AND ctsp.kichCo.id = :kichThuocId")
    Optional<ChiTietSanPham> findBySanPhamAndMauSacAndKichThuoc(@Param("sanPhamId") Integer sanPhamId,
                                                                @Param("mauSacId") Integer mauSacId,
                                                                @Param("kichThuocId") Integer kichThuocId);

    // Tìm sản phẩm còn hàng (giả sử có field soLuong thay vì soLuongTon)
    @Query("SELECT ctsp FROM ChiTietSanPham ctsp WHERE ctsp.soLuong > 0")
    List<ChiTietSanPham> findAvailableProducts();
    @Query("SELECT ctsp FROM ChiTietSanPham ctsp " +
            "JOIN ctsp.sanPham sp " +
            "WHERE ctsp.soLuong > 0 AND ctsp.trangThai = 1 AND sp.trangThai = 1 " +
            "ORDER BY sp.tenSanPham")
    List<ChiTietSanPham> findAllAvailable();

    @Query("SELECT ctsp FROM ChiTietSanPham ctsp WHERE ctsp.maQR = :maQR AND ctsp.trangThai = 1")
    List<ChiTietSanPham> findByMaQR(@Param("maQR") String maQR);
}
