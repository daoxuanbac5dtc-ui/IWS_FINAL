package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.ChiTietTraHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepoChiTietTraHang extends JpaRepository<ChiTietTraHang, Integer> {

    // =================== BASIC FINDER METHODS ===================

    /**
     * TÃ¬m chi tiáº¿t tráº£ hÃ ng theo ID chi tiáº¿t sáº£n pháº©m
     */
    List<ChiTietTraHang> findByChiTietSanPhamId(Integer chiTietSanPhamId);

    /**
     * TÃ¬m chi tiáº¿t tráº£ hÃ ng theo ID hÃ³a Ä‘Æ¡n
     */
    List<ChiTietTraHang> findByHoaDonId(Integer hoaDonId);

    /**
     * TÃ¬m chi tiáº¿t tráº£ hÃ ng theo tráº¡ng thÃ¡i
     */
    List<ChiTietTraHang> findByTrangThaiHoaDon(String trangThai);

    /**
     * TÃ¬m theo hÃ³a Ä‘Æ¡n vÃ  tráº¡ng thÃ¡i
     */
    List<ChiTietTraHang> findByHoaDonIdAndTrangThaiHoaDon(Integer hoaDonId, String trangThai);

    /**
     * TÃ¬m theo mÃ£ chi tiáº¿t tráº£ hÃ ng
     */
    List<ChiTietTraHang> findByMaChiTietTraHang(String maChiTietTraHang);

    /**
     * TÃ¬m theo mÃ£ chi tiáº¿t tráº£ hÃ ng - unique
     */
    Optional<ChiTietTraHang> findFirstByMaChiTietTraHang(String maChiTietTraHang);

    /**
     * Äáº¿m theo tráº¡ng thÃ¡i
     */
    Long countByTrangThaiHoaDon(String trangThai);

    /**
     * Kiá»ƒm tra tá»“n táº¡i tráº£ hÃ ng cho chi tiáº¿t sáº£n pháº©m
     */
    Boolean existsByChiTietSanPhamId(Integer chiTietSanPhamId);

    /**
     * Kiá»ƒm tra mÃ£ chi tiáº¿t tráº£ hÃ ng Ä‘Ã£ tá»“n táº¡i chÆ°a
     */
    Boolean existsByMaChiTietTraHang(String maChiTietTraHang);

    // =================== SEARCH BY REASON AND IMAGE ===================

    /**
     * TÃ¬m kiáº¿m theo lÃ½ do tráº£ hÃ ng (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.lyDo LIKE %:lyDo%")
    List<ChiTietTraHang> findByLyDoContaining(@Param("lyDo") String lyDo);

    /**
     * TÃ¬m chi tiáº¿t tráº£ hÃ ng cÃ³ áº£nh minh chá»©ng
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.duongDanAnh IS NOT NULL AND ctt.duongDanAnh != '' " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findAllWithImages();

    /**
     * TÃ¬m chi tiáº¿t tráº£ hÃ ng khÃ´ng cÃ³ áº£nh minh chá»©ng
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.duongDanAnh IS NULL OR ctt.duongDanAnh = '' " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findAllWithoutImages();

    /**
     * TÃ¬m theo lÃ½ do cá»¥ thá»ƒ (exact match)
     */
    List<ChiTietTraHang> findByLyDo(String lyDo);

    // =================== COMPLEX QUERIES WITH JOINS ===================

    /**
     * Láº¥y chi tiáº¿t tráº£ hÃ ng vá»›i Ä‘áº§y Ä‘á»§ thÃ´ng tin sáº£n pháº©m theo chi tiáº¿t sáº£n pháº©m
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "LEFT JOIN FETCH ctt.chiTietSanPham ctsp " +
            "LEFT JOIN FETCH ctt.hoaDon hd " +
            "LEFT JOIN FETCH ctsp.sanPham sp " +
            "LEFT JOIN FETCH ctsp.mauSac ms " +
            "LEFT JOIN FETCH ctsp.kichCo kc " +
            "LEFT JOIN FETCH ctsp.hinhAnh ha " +
            "LEFT JOIN FETCH sp.thuongHieu th " +
            "LEFT JOIN FETCH sp.danhMuc dm " +
            "WHERE ctt.chiTietSanPham.id = :chiTietSanPhamId " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findByChiTietSanPhamIdWithFullInfo(@Param("chiTietSanPhamId") Integer chiTietSanPhamId);

    /**
     * Láº¥y chi tiáº¿t tráº£ hÃ ng vá»›i Ä‘áº§y Ä‘á»§ thÃ´ng tin theo hÃ³a Ä‘Æ¡n
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "LEFT JOIN FETCH ctt.chiTietSanPham ctsp " +
            "LEFT JOIN FETCH ctt.hoaDon hd " +
            "LEFT JOIN FETCH ctsp.sanPham sp " +
            "LEFT JOIN FETCH ctsp.mauSac ms " +
            "LEFT JOIN FETCH ctsp.kichCo kc " +
            "LEFT JOIN FETCH ctsp.hinhAnh ha " +
            "LEFT JOIN FETCH sp.thuongHieu th " +
            "LEFT JOIN FETCH sp.danhMuc dm " +
            "WHERE ctt.hoaDon.id = :hoaDonId " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findByHoaDonIdWithFullInfo(@Param("hoaDonId") Integer hoaDonId);

    /**
     * Query fallback cho trÆ°á»ng há»£p relationship phá»©c táº¡p
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "LEFT JOIN FETCH ctt.chiTietSanPham ctsp " +
            "LEFT JOIN FETCH ctt.hoaDon hd " +
            "LEFT JOIN FETCH ctsp.sanPham sp " +
            "LEFT JOIN FETCH ctsp.mauSac ms " +
            "LEFT JOIN FETCH ctsp.kichCo kc " +
            "LEFT JOIN FETCH ctsp.hinhAnh ha " +
            "LEFT JOIN FETCH sp.thuongHieu th " +
            "LEFT JOIN FETCH sp.danhMuc dm " +
            "WHERE (ctt.hoaDon.id = :hoaDonId OR " +
            "       ctsp.id IN (SELECT hdct.chiTietSanPham.id FROM HoaDonChiTiet hdct WHERE hdct.hoaDon.id = :hoaDonId)) " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findByHoaDonIdWithFullInfoFallback(@Param("hoaDonId") Integer hoaDonId);

    /**
     * Láº¥y táº¥t cáº£ vá»›i thÃ´ng tin Ä‘áº§y Ä‘á»§
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "LEFT JOIN FETCH ctt.chiTietSanPham ctsp " +
            "LEFT JOIN FETCH ctt.hoaDon hd " +
            "LEFT JOIN FETCH ctsp.sanPham sp " +
            "LEFT JOIN FETCH ctsp.mauSac ms " +
            "LEFT JOIN FETCH ctsp.kichCo kc " +
            "LEFT JOIN FETCH ctsp.hinhAnh ha " +
            "LEFT JOIN FETCH sp.thuongHieu th " +
            "LEFT JOIN FETCH sp.danhMuc dm " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findAllWithFullInfo();

    // =================== ADVANCED SEARCH (AN TOÃ€N) ===================

    /**
     * TÃ¬m kiáº¿m nÃ¢ng cao vá»›i nhiá»u tiÃªu chÃ­ - Version Ä‘Æ¡n giáº£n vÃ  an toÃ n
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE (:lyDo IS NULL OR ctt.lyDo LIKE %:lyDo%) " +
            "AND (:trangThai IS NULL OR ctt.trangThaiHoaDon = :trangThai) " +
            "AND (:hoaDonId IS NULL OR ctt.hoaDon.id = :hoaDonId) " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> searchChiTietTraHang(@Param("lyDo") String lyDo,
                                              @Param("trangThai") String trangThai,
                                              @Param("hoaDonId") Integer hoaDonId);

    /**
     * TÃ¬m kiáº¿m theo khoáº£ng thá»i gian - AN TOÃ€N
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.ngayTaoTraHang >= :startDate AND ctt.ngayTaoTraHang < :endDate " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * TÃ¬m kiáº¿m theo khÃ¡ch hÃ ng (qua hÃ³a Ä‘Æ¡n)
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "LEFT JOIN ctt.hoaDon hd " +
            "LEFT JOIN hd.khachHang kh " +
            "WHERE kh.id = :khachHangId " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findByKhachHangId(@Param("khachHangId") Integer khachHangId);

    /**
     * TÃ¬m kiáº¿m theo nhÃ¢n viÃªn xá»­ lÃ½ (náº¿u cÃ³ trÆ°á»ng trong entity)
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "LEFT JOIN ctt.hoaDon hd " +
            "LEFT JOIN hd.nhanVien nv " +
            "WHERE nv.id = :nhanVienId " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findByNhanVienId(@Param("nhanVienId") Integer nhanVienId);

    // =================== STATISTICAL QUERIES ===================

    /**
     * Äáº¿m tá»•ng sá»‘ lÆ°á»£ng Ä‘Ã£ tráº£ theo chi tiáº¿t sáº£n pháº©m (chá»‰ tÃ­nh APPROVED)
     */
    @Query("SELECT COALESCE(SUM(ctt.soLuong), 0) FROM ChiTietTraHang ctt " +
            "WHERE ctt.chiTietSanPham.id = :chiTietSanPhamId " +
            "AND ctt.trangThaiHoaDon = 'APPROVED'")
    Integer getTotalReturnedQuantity(@Param("chiTietSanPhamId") Integer chiTietSanPhamId);

    /**
     * Äáº¿m tá»•ng sá»‘ lÆ°á»£ng Ä‘Ã£ tráº£ theo hÃ³a Ä‘Æ¡n
     */
    @Query("SELECT COALESCE(SUM(ctt.soLuong), 0) FROM ChiTietTraHang ctt " +
            "WHERE ctt.hoaDon.id = :hoaDonId " +
            "AND ctt.trangThaiHoaDon = 'APPROVED'")
    Integer getTotalReturnedQuantityByInvoice(@Param("hoaDonId") Integer hoaDonId);

    /**
     * TÃ­nh tá»•ng giÃ¡ trá»‹ tráº£ hÃ ng Ä‘Ã£ Ä‘Æ°á»£c cháº¥p nháº­n
     */
    @Query("SELECT COALESCE(SUM(ctt.soLuong * ctsp.giaGoc), 0.0) FROM ChiTietTraHang ctt " +
            "JOIN ctt.chiTietSanPham ctsp " +
            "WHERE ctt.trangThaiHoaDon = 'APPROVED'")
    Double getTotalReturnValue();

    /**
     * TÃ­nh tá»•ng giÃ¡ trá»‹ tráº£ hÃ ng theo hÃ³a Ä‘Æ¡n
     */
    @Query("SELECT COALESCE(SUM(ctt.soLuong * ctsp.giaGoc), 0.0) FROM ChiTietTraHang ctt " +
            "JOIN ctt.chiTietSanPham ctsp " +
            "WHERE ctt.hoaDon.id = :hoaDonId " +
            "AND ctt.trangThaiHoaDon = 'APPROVED'")
    Double getTotalReturnValueByInvoice(@Param("hoaDonId") Integer hoaDonId);

    /**
     * Äáº¿m sá»‘ lÆ°á»£ng theo tráº¡ng thÃ¡i vÃ  hÃ³a Ä‘Æ¡n
     */
    @Query("SELECT COUNT(ctt) FROM ChiTietTraHang ctt " +
            "WHERE ctt.hoaDon.id = :hoaDonId " +
            "AND ctt.trangThaiHoaDon = :trangThai")
    Long countByHoaDonIdAndTrangThai(@Param("hoaDonId") Integer hoaDonId, @Param("trangThai") String trangThai);

    /**
     * Kiá»ƒm tra tá»“n táº¡i tráº£ hÃ ng cho hÃ³a Ä‘Æ¡n
     */
    @Query("SELECT CASE WHEN COUNT(ctt) > 0 THEN true ELSE false END FROM ChiTietTraHang ctt " +
            "WHERE ctt.hoaDon.id = :hoaDonId")
    Boolean existsByHoaDonId(@Param("hoaDonId") Integer hoaDonId);

    // =================== REASON STATISTICS ===================

    /**
     * Thá»‘ng kÃª theo lÃ½ do tráº£ hÃ ng
     */
    @Query("SELECT ctt.lyDo, COUNT(ctt) FROM ChiTietTraHang ctt " +
            "WHERE ctt.lyDo IS NOT NULL AND ctt.lyDo != '' " +
            "GROUP BY ctt.lyDo " +
            "ORDER BY COUNT(ctt) DESC")
    List<Object[]> getReturnReasonStatistics();

    /**
     * Thá»‘ng kÃª theo lÃ½ do tráº£ hÃ ng trong khoáº£ng thá»i gian
     */
    @Query("SELECT ctt.lyDo, COUNT(ctt) FROM ChiTietTraHang ctt " +
            "WHERE ctt.lyDo IS NOT NULL AND ctt.lyDo != '' " +
            "AND ctt.ngayTaoTraHang >= :startDate AND ctt.ngayTaoTraHang < :endDate " +
            "GROUP BY ctt.lyDo " +
            "ORDER BY COUNT(ctt) DESC")
    List<Object[]> getReturnReasonStatisticsByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Äáº¿m sá»‘ lÆ°á»£ng tráº£ hÃ ng cÃ³ áº£nh minh chá»©ng
     */
    @Query("SELECT COUNT(ctt) FROM ChiTietTraHang ctt " +
            "WHERE ctt.duongDanAnh IS NOT NULL AND ctt.duongDanAnh != ''")
    Long countReturnsWithImages();

    /**
     * Äáº¿m sá»‘ lÆ°á»£ng tráº£ hÃ ng theo lÃ½ do cá»¥ thá»ƒ
     */
    @Query("SELECT COUNT(ctt) FROM ChiTietTraHang ctt " +
            "WHERE ctt.lyDo = :lyDo")
    Long countByLyDo(@Param("lyDo") String lyDo);

    /**
     * Láº¥y top N lÃ½ do tráº£ hÃ ng phá»• biáº¿n nháº¥t
     */
    @Query("SELECT ctt.lyDo, COUNT(ctt) FROM ChiTietTraHang ctt " +
            "WHERE ctt.lyDo IS NOT NULL AND ctt.lyDo != '' " +
            "GROUP BY ctt.lyDo " +
            "ORDER BY COUNT(ctt) DESC")
    List<Object[]> getTopReturnReasons();

    // =================== VALIDATION QUERIES ===================

    /**
     * Láº¥y chi tiáº¿t tráº£ hÃ ng cÃ¹ng sáº£n pháº©m trong cÃ¹ng hÃ³a Ä‘Æ¡n
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.hoaDon.id = :hoaDonId " +
            "AND ctt.chiTietSanPham.id = :chiTietSanPhamId " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findByHoaDonAndChiTietSanPham(@Param("hoaDonId") Integer hoaDonId,
                                                       @Param("chiTietSanPhamId") Integer chiTietSanPhamId);

    /**
     * Kiá»ƒm tra sá»‘ lÆ°á»£ng cÃ³ thá»ƒ tráº£ - AN TOÃ€N
     */
    @Query("SELECT COALESCE(" +
            "   (SELECT SUM(hdct.soLuong) FROM HoaDonChiTiet hdct WHERE hdct.chiTietSanPham.id = :chiTietSanPhamId), 0) - " +
            "   COALESCE(" +
            "       (SELECT SUM(ctt.soLuong) FROM ChiTietTraHang ctt " +
            "        WHERE ctt.chiTietSanPham.id = :chiTietSanPhamId AND ctt.trangThaiHoaDon = 'APPROVED'), 0)")
    Integer getAvailableReturnQuantity(@Param("chiTietSanPhamId") Integer chiTietSanPhamId);

    // =================== TIME-BASED QUERIES (AN TOÃ€N Vá»šI PARAMETERS) ===================

    /**
     * Láº¥y chi tiáº¿t tráº£ hÃ ng trong ngÃ y cá»¥ thá»ƒ
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.ngayTaoTraHang >= :startOfDay " +
            "AND ctt.ngayTaoTraHang < :endOfDay " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findReturnsByDay(@Param("startOfDay") Date startOfDay,
                                          @Param("endOfDay") Date endOfDay);

    /**
     * Láº¥y chi tiáº¿t tráº£ hÃ ng trong tuáº§n cá»¥ thá»ƒ
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.ngayTaoTraHang >= :startOfWeek " +
            "AND ctt.ngayTaoTraHang < :endOfWeek " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findReturnsByWeek(@Param("startOfWeek") Date startOfWeek,
                                           @Param("endOfWeek") Date endOfWeek);

    /**
     * Láº¥y chi tiáº¿t tráº£ hÃ ng trong thÃ¡ng cá»¥ thá»ƒ
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.ngayTaoTraHang >= :startOfMonth " +
            "AND ctt.ngayTaoTraHang < :endOfMonth " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findReturnsByMonth(@Param("startOfMonth") Date startOfMonth,
                                            @Param("endOfMonth") Date endOfMonth);

    // =================== BULK OPERATIONS (AN TOÃ€N) ===================

    /**
     * Cáº­p nháº­t tráº¡ng thÃ¡i hÃ ng loáº¡t - Sá»­ dá»¥ng @Modifying trong Service
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.id IN :ids AND ctt.trangThaiHoaDon = :currentStatus")
    List<ChiTietTraHang> findByIdsAndCurrentStatus(@Param("ids") List<Integer> ids,
                                                   @Param("currentStatus") String currentStatus);

    /**
     * TÃ¬m cÃ¡c chi tiáº¿t tráº£ hÃ ng cÃ³ áº£nh Ä‘á»ƒ xÃ³a áº£nh hÃ ng loáº¡t
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.id IN :ids AND ctt.duongDanAnh IS NOT NULL")
    List<ChiTietTraHang> findByIdsWithImages(@Param("ids") List<Integer> ids);

    @Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietTraHang c " +
            "WHERE c.chiTietSanPham.id = :chiTietSanPhamId " +
            "AND c.trangThaiHoaDon NOT IN ('REJECTED', 'CANCELLED', 'DENIED')")
    Integer getTotalReturnedQuantityExcludeRejected(@Param("chiTietSanPhamId") Integer chiTietSanPhamId);

}
