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
     * TÃ¬m hÃ¬nh áº£nh theo ID vÃ  tráº¡ng thÃ¡i
     */
    Optional<HinhAnh> findByIdAndTrangThai(Integer id, Integer trangThai);

    /**
     * TÃ¬m táº¥t cáº£ hÃ¬nh áº£nh theo tráº¡ng thÃ¡i
     */
    List<HinhAnh> findByTrangThai(Integer trangThai);

    /**
     * TÃ¬m hÃ¬nh áº£nh theo mÃ£ hÃ¬nh áº£nh
     */
    Optional<HinhAnh> findByMaHinhAnh(String maHinhAnh);

    /**
     * TÃ¬m hÃ¬nh áº£nh theo mÃ£ hÃ¬nh áº£nh vÃ  tráº¡ng thÃ¡i
     */
    Optional<HinhAnh> findByMaHinhAnhAndTrangThai(String maHinhAnh, Integer trangThai);

    /**
     * TÃ¬m hÃ¬nh áº£nh theo tÃªn hÃ¬nh áº£nh
     */
    List<HinhAnh> findByTenHinhAnhContainingIgnoreCase(String tenHinhAnh);

    /**
     * TÃ¬m hÃ¬nh áº£nh theo Ä‘Æ°á»ng dáº«n
     */
    Optional<HinhAnh> findByDuongDan(String duongDan);

    /**
     * Láº¥y hÃ¬nh áº£nh cá»§a chi tiáº¿t sáº£n pháº©m thÃ´ng qua quan há»‡ ngÆ°á»£c
     * (Tá»« ChiTietSanPham tÃ¬m HinhAnh)
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE ha.id IN (SELECT ctsp.hinhAnh.id FROM ChiTietSanPham ctsp WHERE ctsp.id = :chiTietSanPhamId) " +
            "AND ha.trangThai = :trangThai")
    Optional<HinhAnh> findByChiTietSanPhamIdAndTrangThai(@Param("chiTietSanPhamId") Integer chiTietSanPhamId,
                                                         @Param("trangThai") Integer trangThai);

    /**
     * Láº¥y hÃ¬nh áº£nh cá»§a chi tiáº¿t sáº£n pháº©m (active)
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE ha.id IN (SELECT ctsp.hinhAnh.id FROM ChiTietSanPham ctsp WHERE ctsp.id = :chiTietSanPhamId) " +
            "AND ha.trangThai = 1")
    Optional<HinhAnh> findActiveImageByChiTietSanPhamId(@Param("chiTietSanPhamId") Integer chiTietSanPhamId);

    /**
     * Láº¥y danh sÃ¡ch hÃ¬nh áº£nh theo danh sÃ¡ch ID chi tiáº¿t sáº£n pháº©m
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE ha.id IN (SELECT ctsp.hinhAnh.id FROM ChiTietSanPham ctsp WHERE ctsp.id IN :chiTietSanPhamIds) " +
            "AND ha.trangThai = 1")
    List<HinhAnh> findActiveImagesByChiTietSanPhamIds(@Param("chiTietSanPhamIds") List<Integer> chiTietSanPhamIds);

    /**
     * Láº¥y hÃ¬nh áº£nh theo sáº£n pháº©m (qua chi tiáº¿t sáº£n pháº©m)
     */
    @Query("SELECT DISTINCT ha FROM HinhAnh ha " +
            "WHERE ha.id IN (SELECT ctsp.hinhAnh.id FROM ChiTietSanPham ctsp WHERE ctsp.sanPham.id = :sanPhamId) " +
            "AND ha.trangThai = 1")
    List<HinhAnh> findActiveImagesBySanPhamId(@Param("sanPhamId") Integer sanPhamId);

    /**
     * Láº¥y hÃ¬nh áº£nh Ä‘áº§u tiÃªn cá»§a sáº£n pháº©m
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE ha.id IN (SELECT ctsp.hinhAnh.id FROM ChiTietSanPham ctsp WHERE ctsp.sanPham.id = :sanPhamId) " +
            "AND ha.trangThai = 1 " +
            "ORDER BY ha.ngayTao ASC " +
            "LIMIT 1")
    Optional<HinhAnh> findFirstActiveImageBySanPhamId(@Param("sanPhamId") Integer sanPhamId);

    /**
     * Äáº¿m sá»‘ lÆ°á»£ng hÃ¬nh áº£nh theo tráº¡ng thÃ¡i
     */
    @Query("SELECT COUNT(ha) FROM HinhAnh ha WHERE ha.trangThai = :trangThai")
    Long countByTrangThai(@Param("trangThai") Integer trangThai);

    /**
     * Kiá»ƒm tra xem cÃ³ chi tiáº¿t sáº£n pháº©m nÃ o Ä‘ang sá»­ dá»¥ng hÃ¬nh áº£nh nÃ y khÃ´ng
     */
    @Query("SELECT COUNT(ctsp) FROM ChiTietSanPham ctsp WHERE ctsp.hinhAnh.id = :hinhAnhId")
    Long countChiTietSanPhamUsingImage(@Param("hinhAnhId") Integer hinhAnhId);

    /**
     * Láº¥y danh sÃ¡ch hÃ¬nh áº£nh chÆ°a Ä‘Æ°á»£c sá»­ dá»¥ng
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE ha.id NOT IN (SELECT DISTINCT ctsp.hinhAnh.id FROM ChiTietSanPham ctsp WHERE ctsp.hinhAnh.id IS NOT NULL) " +
            "AND ha.trangThai = 1")
    List<HinhAnh> findUnusedActiveImages();

    /**
     * TÃ¬m hÃ¬nh áº£nh theo khoáº£ng thá»i gian táº¡o
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE ha.ngayTao >= :startDate AND ha.ngayTao <= :endDate " +
            "AND ha.trangThai = 1 " +
            "ORDER BY ha.ngayTao DESC")
    List<HinhAnh> findImagesByDateRange(@Param("startDate") java.time.LocalDateTime startDate,
                                        @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Cáº­p nháº­t tráº¡ng thÃ¡i hÃ¬nh áº£nh
     */
    @Query("UPDATE HinhAnh ha SET ha.trangThai = :trangThai, ha.ngayCapNhat = CURRENT_TIMESTAMP " +
            "WHERE ha.id = :id")
    int updateTrangThaiById(@Param("id") Integer id, @Param("trangThai") Integer trangThai);

    /**
     * Cáº­p nháº­t Ä‘Æ°á»ng dáº«n hÃ¬nh áº£nh
     */
    @Query("UPDATE HinhAnh ha SET ha.duongDan = :duongDan, ha.ngayCapNhat = CURRENT_TIMESTAMP " +
            "WHERE ha.id = :id")
    int updateDuongDanById(@Param("id") Integer id, @Param("duongDan") String duongDan);

    /**
     * TÃ¬m hÃ¬nh áº£nh cÃ³ tÃªn tÆ°Æ¡ng tá»±
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE LOWER(ha.tenHinhAnh) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND ha.trangThai = 1")
    List<HinhAnh> searchByTenHinhAnh(@Param("keyword") String keyword);

    /**
     * Láº¥y hÃ¬nh áº£nh má»›i nháº¥t
     */
    @Query("SELECT ha FROM HinhAnh ha " +
            "WHERE ha.trangThai = 1 " +
            "ORDER BY ha.ngayTao DESC " +
            "LIMIT :limit")
    List<HinhAnh> findLatestImages(@Param("limit") int limit);

    /**
     * Láº¥y thá»‘ng kÃª hÃ¬nh áº£nh theo tráº¡ng thÃ¡i
     */
    @Query("SELECT ha.trangThai, COUNT(ha) FROM HinhAnh ha GROUP BY ha.trangThai")
    List<Object[]> getImageStatisticsByStatus();
}
