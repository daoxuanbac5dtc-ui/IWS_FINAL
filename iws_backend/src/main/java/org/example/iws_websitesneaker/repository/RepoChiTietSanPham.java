package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoChiTietSanPham extends JpaRepository<ChiTietSanPham, Integer> {

    Optional<ChiTietSanPham> findByMaChiTiet(String maChiTiet);

    // SỬA: Thay method findBySanPhamId mặc định bằng query có JOIN FETCH
    @Query("SELECT DISTINCT ctsp FROM ChiTietSanPham ctsp " +
            "LEFT JOIN FETCH ctsp.sanPham sp " +
            "LEFT JOIN FETCH ctsp.mauSac ms " +
            "LEFT JOIN FETCH ctsp.kichCo kc " +
            "LEFT JOIN FETCH ctsp.hinhAnh ha " +
            "WHERE ctsp.sanPham.id = :sanPhamId " +
            "ORDER BY ctsp.id")
    List<ChiTietSanPham> findBySanPhamId(@Param("sanPhamId") Integer sanPhamId);

    // THÊM: Method để debug và kiểm tra dữ liệu
    @Query("SELECT DISTINCT ctsp FROM ChiTietSanPham ctsp " +
            "LEFT JOIN FETCH ctsp.sanPham sp " +
            "LEFT JOIN FETCH ctsp.mauSac ms " +
            "LEFT JOIN FETCH ctsp.kichCo kc " +
            "LEFT JOIN FETCH ctsp.hinhAnh ha " +
            "WHERE ctsp.sanPham.id = :sanPhamId " +
            "AND ctsp.mauSac IS NOT NULL " +
            "AND ctsp.kichCo IS NOT NULL")
    List<ChiTietSanPham> findBySanPhamIdWithValidData(@Param("sanPhamId") Integer sanPhamId);

    // THÊM: Method tìm theo ID với đầy đủ thông tin
    @Query("SELECT ctsp FROM ChiTietSanPham ctsp " +
            "LEFT JOIN FETCH ctsp.sanPham sp " +
            "LEFT JOIN FETCH ctsp.mauSac ms " +
            "LEFT JOIN FETCH ctsp.kichCo kc " +
            "LEFT JOIN FETCH ctsp.hinhAnh ha " +
            "WHERE ctsp.id = :id")
    Optional<ChiTietSanPham> findByIdWithDetails(@Param("id") Integer id);

    // Xóa Màu sắc - sửa id màu sắc trong bảng ctsp về null
    @Modifying
    @Transactional
    @Query("UPDATE ChiTietSanPham ctsp SET ctsp.mauSac = NULL WHERE ctsp.mauSac.id = :id")
    void removeMauSacReference(@Param("id") int id);

    // Xóa Kích cỡ - sửa id kc trong bảng ctsp về null
    @Modifying
    @Transactional
    @Query("UPDATE ChiTietSanPham ctsp SET ctsp.kichCo = NULL WHERE ctsp.kichCo.id = :id")
    void removeKichCoReference(@Param("id") int id);

    // Xóa sản phẩm - - sửa id sp trong bảng ctsp về null
    @Modifying
    @Transactional
    @Query("UPDATE ChiTietSanPham ctsp SET ctsp.sanPham = NULL WHERE ctsp.sanPham.id = :id")
    void removeSanPhamReference(@Param("id") int id);

    // Xóa hình ảnh  - - sửa id hình ảnh trong bảng ctsp về null
    @Modifying
    @Transactional
    @Query("UPDATE ChiTietSanPham ctsp SET ctsp.hinhAnh = NULL WHERE ctsp.hinhAnh.id = :id")
    void removeHinhAnhReference(@Param("id") int id);

    @Query("SELECT ctsp FROM ChiTietSanPham ctsp WHERE ctsp.soLuong > 0 AND ctsp.trangThai = 1")
    List<ChiTietSanPham> findAvailableProducts();
}
