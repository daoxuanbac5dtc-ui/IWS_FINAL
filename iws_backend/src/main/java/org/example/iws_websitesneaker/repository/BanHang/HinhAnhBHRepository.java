package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import org.example.iws_websitesneaker.entity.HinhAnh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HinhAnhBHRepository extends JpaRepository<HinhAnh, Integer> {

    /**
     * Tìm hình ảnh theo ID và trạng thái
     */
    Optional<HinhAnh> findByIdAndTrangThai(Integer id, Integer trangThai);

    /**
     * Tìm tất cả hình ảnh theo trạng thái
     */
    List<HinhAnh> findByTrangThai(Integer trangThai);

    /**
     * Tìm hình ảnh theo mã hình ảnh
     */
    Optional<HinhAnh> findByMaHinhAnh(String maHinhAnh);

    /**
     * Tìm hình ảnh theo mã hình ảnh và trạng thái
     */
    Optional<HinhAnh> findByMaHinhAnhAndTrangThai(String maHinhAnh, Integer trangThai);

    /**
     * Tìm hình ảnh theo tên hình ảnh
     */
    List<HinhAnh> findByTenHinhAnhContainingIgnoreCase(String tenHinhAnh);

    /**
     * Tìm hình ảnh theo đường dẫn
     */
    Optional<HinhAnh> findByDuongDan(String duongDan);

    /**
     * Lấy hình ảnh của chi tiết sản phẩm thông qua quan hệ ngược
     * (Từ ChiTietSanPham tìm HinhAnh)
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE ha.id IN (SELECT ctsp.hinhAnh.id FROM ChiTietSanPham ctsp WHERE ctsp.id = :chiTietSanPhamId) " +
            "AND ha.trangThai = :trangThai")
    Optional<HinhAnh> findByChiTietSanPhamIdAndTrangThai(@Param("chiTietSanPhamId") Integer chiTietSanPhamId,
                                                         @Param("trangThai") Integer trangThai);

    /**
     * Lấy hình ảnh của chi tiết sản phẩm (active)
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE ha.id IN (SELECT ctsp.hinhAnh.id FROM ChiTietSanPham ctsp WHERE ctsp.id = :chiTietSanPhamId) " +
            "AND ha.trangThai = 1")
    Optional<HinhAnh> findActiveImageByChiTietSanPhamId(@Param("chiTietSanPhamId") Integer chiTietSanPhamId);

    /**
     * Lấy danh sách hình ảnh theo danh sách ID chi tiết sản phẩm
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE ha.id IN (SELECT ctsp.hinhAnh.id FROM ChiTietSanPham ctsp WHERE ctsp.id IN :chiTietSanPhamIds) " +
            "AND ha.trangThai = 1")
    List<HinhAnh> findActiveImagesByChiTietSanPhamIds(@Param("chiTietSanPhamIds") List<Integer> chiTietSanPhamIds);

    /**
     * Lấy hình ảnh theo sản phẩm (qua chi tiết sản phẩm)
     */
    @Query("SELECT DISTINCT ha FROM HinhAnh ha " +
            "WHERE ha.id IN (SELECT ctsp.hinhAnh.id FROM ChiTietSanPham ctsp WHERE ctsp.sanPham.id = :sanPhamId) " +
            "AND ha.trangThai = 1")
    List<HinhAnh> findActiveImagesBySanPhamId(@Param("sanPhamId") Integer sanPhamId);

    /**
     * Lấy hình ảnh đầu tiên của sản phẩm
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE ha.id IN (SELECT ctsp.hinhAnh.id FROM ChiTietSanPham ctsp WHERE ctsp.sanPham.id = :sanPhamId) " +
            "AND ha.trangThai = 1 " +
            "ORDER BY ha.ngayTao ASC " +
            "LIMIT 1")
    Optional<HinhAnh> findFirstActiveImageBySanPhamId(@Param("sanPhamId") Integer sanPhamId);

    /**
     * Đếm số lượng hình ảnh theo trạng thái
     */
    @Query("SELECT COUNT(ha) FROM HinhAnh ha WHERE ha.trangThai = :trangThai")
    Long countByTrangThai(@Param("trangThai") Integer trangThai);

    /**
     * Kiểm tra xem có chi tiết sản phẩm nào đang sử dụng hình ảnh này không
     */
    @Query("SELECT COUNT(ctsp) FROM ChiTietSanPham ctsp WHERE ctsp.hinhAnh.id = :hinhAnhId")
    Long countChiTietSanPhamUsingImage(@Param("hinhAnhId") Integer hinhAnhId);

    /**
     * Lấy danh sách hình ảnh chưa được sử dụng
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE ha.id NOT IN (SELECT DISTINCT ctsp.hinhAnh.id FROM ChiTietSanPham ctsp WHERE ctsp.hinhAnh.id IS NOT NULL) " +
            "AND ha.trangThai = 1")
    List<HinhAnh> findUnusedActiveImages();

    /**
     * Tìm hình ảnh theo khoảng thời gian tạo
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE ha.ngayTao >= :startDate AND ha.ngayTao <= :endDate " +
            "AND ha.trangThai = 1 " +
            "ORDER BY ha.ngayTao DESC")
    List<HinhAnh> findImagesByDateRange(@Param("startDate") java.time.LocalDateTime startDate,
                                        @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Cập nhật trạng thái hình ảnh
     */
    @Query("UPDATE HinhAnh ha SET ha.trangThai = :trangThai, ha.ngayCapNhat = CURRENT_TIMESTAMP " +
            "WHERE ha.id = :id")
    int updateTrangThaiById(@Param("id") Integer id, @Param("trangThai") Integer trangThai);

    /**
     * Cập nhật đường dẫn hình ảnh
     */
    @Query("UPDATE HinhAnh ha SET ha.duongDan = :duongDan, ha.ngayCapNhat = CURRENT_TIMESTAMP " +
            "WHERE ha.id = :id")
    int updateDuongDanById(@Param("id") Integer id, @Param("duongDan") String duongDan);

    /**
     * Tìm hình ảnh có tên tương tự
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE LOWER(ha.tenHinhAnh) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND ha.trangThai = 1")
    List<HinhAnh> searchByTenHinhAnh(@Param("keyword") String keyword);

    /**
     * Lấy hình ảnh mới nhất
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE ha.trangThai = 1 " +
            "ORDER BY ha.ngayTao DESC " +
            "LIMIT :limit")
    List<HinhAnh> findLatestImages(@Param("limit") int limit);

    /**
     * Lấy thống kê hình ảnh theo trạng thái
     */
    @Query("SELECT ha.trangThai, COUNT(ha) FROM HinhAnh ha GROUP BY ha.trangThai")
    List<Object[]> getImageStatisticsByStatus();
}
