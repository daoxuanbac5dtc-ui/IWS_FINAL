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
     * Tìm chi tiết trả hàng theo ID chi tiết sản phẩm
     */
    List<ChiTietTraHang> findByChiTietSanPhamId(Integer chiTietSanPhamId);

    /**
     * Tìm chi tiết trả hàng theo ID hóa đơn
     */
    List<ChiTietTraHang> findByHoaDonId(Integer hoaDonId);

    /**
     * Tìm chi tiết trả hàng theo trạng thái
     */
    List<ChiTietTraHang> findByTrangThaiHoaDon(String trangThai);

    /**
     * Tìm theo hóa đơn và trạng thái
     */
    List<ChiTietTraHang> findByHoaDonIdAndTrangThaiHoaDon(Integer hoaDonId, String trangThai);

    /**
     * Tìm theo mã chi tiết trả hàng
     */
    List<ChiTietTraHang> findByMaChiTietTraHang(String maChiTietTraHang);

    /**
     * Tìm theo mã chi tiết trả hàng - unique
     */
    Optional<ChiTietTraHang> findFirstByMaChiTietTraHang(String maChiTietTraHang);

    /**
     * Đếm theo trạng thái
     */
    Long countByTrangThaiHoaDon(String trangThai);

    /**
     * Kiểm tra tồn tại trả hàng cho chi tiết sản phẩm
     */
    Boolean existsByChiTietSanPhamId(Integer chiTietSanPhamId);

    /**
     * Kiểm tra mã chi tiết trả hàng đã tồn tại chưa
     */
    Boolean existsByMaChiTietTraHang(String maChiTietTraHang);

    // =================== SEARCH BY REASON AND IMAGE ===================

    /**
     * Tìm kiếm theo lý do trả hàng (tìm kiếm gần đúng)
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.lyDo LIKE %:lyDo%")
    List<ChiTietTraHang> findByLyDoContaining(@Param("lyDo") String lyDo);

    /**
     * Tìm chi tiết trả hàng có ảnh minh chứng
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.duongDanAnh IS NOT NULL AND ctt.duongDanAnh != '' " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findAllWithImages();

    /**
     * Tìm chi tiết trả hàng không có ảnh minh chứng
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.duongDanAnh IS NULL OR ctt.duongDanAnh = '' " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findAllWithoutImages();

    /**
     * Tìm theo lý do cụ thể (exact match)
     */
    List<ChiTietTraHang> findByLyDo(String lyDo);

    // =================== COMPLEX QUERIES WITH JOINS ===================

    /**
     * Lấy chi tiết trả hàng với đầy đủ thông tin sản phẩm theo chi tiết sản phẩm
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
     * Lấy chi tiết trả hàng với đầy đủ thông tin theo hóa đơn
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
     * Query fallback cho trường hợp relationship phức tạp
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
     * Lấy tất cả với thông tin đầy đủ
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

    // =================== ADVANCED SEARCH (AN TOÀN) ===================

    /**
     * Tìm kiếm nâng cao với nhiều tiêu chí - Version đơn giản và an toàn
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
     * Tìm kiếm theo khoảng thời gian - AN TOÀN
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.ngayTaoTraHang >= :startDate AND ctt.ngayTaoTraHang < :endDate " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Tìm kiếm theo khách hàng (qua hóa đơn)
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "LEFT JOIN ctt.hoaDon hd " +
            "LEFT JOIN hd.khachHang kh " +
            "WHERE kh.id = :khachHangId " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findByKhachHangId(@Param("khachHangId") Integer khachHangId);

    /**
     * Tìm kiếm theo nhân viên xử lý (nếu có trường trong entity)
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "LEFT JOIN ctt.hoaDon hd " +
            "LEFT JOIN hd.nhanVien nv " +
            "WHERE nv.id = :nhanVienId " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findByNhanVienId(@Param("nhanVienId") Integer nhanVienId);

    // =================== STATISTICAL QUERIES ===================

    /**
     * Đếm tổng số lượng đã trả theo chi tiết sản phẩm (chỉ tính APPROVED)
     */
    @Query("SELECT COALESCE(SUM(ctt.soLuong), 0) FROM ChiTietTraHang ctt " +
            "WHERE ctt.chiTietSanPham.id = :chiTietSanPhamId " +
            "AND ctt.trangThaiHoaDon = 'APPROVED'")
    Integer getTotalReturnedQuantity(@Param("chiTietSanPhamId") Integer chiTietSanPhamId);

    /**
     * Đếm tổng số lượng đã trả theo hóa đơn
     */
    @Query("SELECT COALESCE(SUM(ctt.soLuong), 0) FROM ChiTietTraHang ctt " +
            "WHERE ctt.hoaDon.id = :hoaDonId " +
            "AND ctt.trangThaiHoaDon = 'APPROVED'")
    Integer getTotalReturnedQuantityByInvoice(@Param("hoaDonId") Integer hoaDonId);

    /**
     * Tính tổng giá trị trả hàng đã được chấp nhận
     */
    @Query("SELECT COALESCE(SUM(ctt.soLuong * ctsp.giaGoc), 0.0) FROM ChiTietTraHang ctt " +
            "JOIN ctt.chiTietSanPham ctsp " +
            "WHERE ctt.trangThaiHoaDon = 'APPROVED'")
    Double getTotalReturnValue();

    /**
     * Tính tổng giá trị trả hàng theo hóa đơn
     */
    @Query("SELECT COALESCE(SUM(ctt.soLuong * ctsp.giaGoc), 0.0) FROM ChiTietTraHang ctt " +
            "JOIN ctt.chiTietSanPham ctsp " +
            "WHERE ctt.hoaDon.id = :hoaDonId " +
            "AND ctt.trangThaiHoaDon = 'APPROVED'")
    Double getTotalReturnValueByInvoice(@Param("hoaDonId") Integer hoaDonId);

    /**
     * Đếm số lượng theo trạng thái và hóa đơn
     */
    @Query("SELECT COUNT(ctt) FROM ChiTietTraHang ctt " +
            "WHERE ctt.hoaDon.id = :hoaDonId " +
            "AND ctt.trangThaiHoaDon = :trangThai")
    Long countByHoaDonIdAndTrangThai(@Param("hoaDonId") Integer hoaDonId, @Param("trangThai") String trangThai);

    /**
     * Kiểm tra tồn tại trả hàng cho hóa đơn
     */
    @Query("SELECT CASE WHEN COUNT(ctt) > 0 THEN true ELSE false END FROM ChiTietTraHang ctt " +
            "WHERE ctt.hoaDon.id = :hoaDonId")
    Boolean existsByHoaDonId(@Param("hoaDonId") Integer hoaDonId);

    // =================== REASON STATISTICS ===================

    /**
     * Thống kê theo lý do trả hàng
     */
    @Query("SELECT ctt.lyDo, COUNT(ctt) FROM ChiTietTraHang ctt " +
            "WHERE ctt.lyDo IS NOT NULL AND ctt.lyDo != '' " +
            "GROUP BY ctt.lyDo " +
            "ORDER BY COUNT(ctt) DESC")
    List<Object[]> getReturnReasonStatistics();

    /**
     * Thống kê theo lý do trả hàng trong khoảng thời gian
     */
    @Query("SELECT ctt.lyDo, COUNT(ctt) FROM ChiTietTraHang ctt " +
            "WHERE ctt.lyDo IS NOT NULL AND ctt.lyDo != '' " +
            "AND ctt.ngayTaoTraHang >= :startDate AND ctt.ngayTaoTraHang < :endDate " +
            "GROUP BY ctt.lyDo " +
            "ORDER BY COUNT(ctt) DESC")
    List<Object[]> getReturnReasonStatisticsByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Đếm số lượng trả hàng có ảnh minh chứng
     */
    @Query("SELECT COUNT(ctt) FROM ChiTietTraHang ctt " +
            "WHERE ctt.duongDanAnh IS NOT NULL AND ctt.duongDanAnh != ''")
    Long countReturnsWithImages();

    /**
     * Đếm số lượng trả hàng theo lý do cụ thể
     */
    @Query("SELECT COUNT(ctt) FROM ChiTietTraHang ctt " +
            "WHERE ctt.lyDo = :lyDo")
    Long countByLyDo(@Param("lyDo") String lyDo);

    /**
     * Lấy top N lý do trả hàng phổ biến nhất
     */
    @Query("SELECT ctt.lyDo, COUNT(ctt) FROM ChiTietTraHang ctt " +
            "WHERE ctt.lyDo IS NOT NULL AND ctt.lyDo != '' " +
            "GROUP BY ctt.lyDo " +
            "ORDER BY COUNT(ctt) DESC")
    List<Object[]> getTopReturnReasons();

    // =================== VALIDATION QUERIES ===================

    /**
     * Lấy chi tiết trả hàng cùng sản phẩm trong cùng hóa đơn
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.hoaDon.id = :hoaDonId " +
            "AND ctt.chiTietSanPham.id = :chiTietSanPhamId " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findByHoaDonAndChiTietSanPham(@Param("hoaDonId") Integer hoaDonId,
                                                       @Param("chiTietSanPhamId") Integer chiTietSanPhamId);

    /**
     * Kiểm tra số lượng có thể trả - AN TOÀN
     */
    @Query("SELECT COALESCE(" +
            "   (SELECT SUM(hdct.soLuong) FROM HoaDonChiTiet hdct WHERE hdct.chiTietSanPham.id = :chiTietSanPhamId), 0) - " +
            "   COALESCE(" +
            "       (SELECT SUM(ctt.soLuong) FROM ChiTietTraHang ctt " +
            "        WHERE ctt.chiTietSanPham.id = :chiTietSanPhamId AND ctt.trangThaiHoaDon = 'APPROVED'), 0)")
    Integer getAvailableReturnQuantity(@Param("chiTietSanPhamId") Integer chiTietSanPhamId);

    // =================== TIME-BASED QUERIES (AN TOÀN VỚI PARAMETERS) ===================

    /**
     * Lấy chi tiết trả hàng trong ngày cụ thể
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.ngayTaoTraHang >= :startOfDay " +
            "AND ctt.ngayTaoTraHang < :endOfDay " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findReturnsByDay(@Param("startOfDay") Date startOfDay,
                                          @Param("endOfDay") Date endOfDay);

    /**
     * Lấy chi tiết trả hàng trong tuần cụ thể
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.ngayTaoTraHang >= :startOfWeek " +
            "AND ctt.ngayTaoTraHang < :endOfWeek " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findReturnsByWeek(@Param("startOfWeek") Date startOfWeek,
                                           @Param("endOfWeek") Date endOfWeek);

    /**
     * Lấy chi tiết trả hàng trong tháng cụ thể
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.ngayTaoTraHang >= :startOfMonth " +
            "AND ctt.ngayTaoTraHang < :endOfMonth " +
            "ORDER BY ctt.ngayTaoTraHang DESC")
    List<ChiTietTraHang> findReturnsByMonth(@Param("startOfMonth") Date startOfMonth,
                                            @Param("endOfMonth") Date endOfMonth);

    // =================== BULK OPERATIONS (AN TOÀN) ===================

    /**
     * Cập nhật trạng thái hàng loạt - Sử dụng @Modifying trong Service
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.id IN :ids AND ctt.trangThaiHoaDon = :currentStatus")
    List<ChiTietTraHang> findByIdsAndCurrentStatus(@Param("ids") List<Integer> ids,
                                                   @Param("currentStatus") String currentStatus);

    /**
     * Tìm các chi tiết trả hàng có ảnh để xóa ảnh hàng loạt
     */
    @Query("SELECT ctt FROM ChiTietTraHang ctt " +
            "WHERE ctt.id IN :ids AND ctt.duongDanAnh IS NOT NULL")
    List<ChiTietTraHang> findByIdsWithImages(@Param("ids") List<Integer> ids);

    @Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietTraHang c " +
            "WHERE c.chiTietSanPham.id = :chiTietSanPhamId " +
            "AND c.trangThaiHoaDon NOT IN ('REJECTED', 'CANCELLED', 'DENIED')")
    Integer getTotalReturnedQuantityExcludeRejected(@Param("chiTietSanPhamId") Integer chiTietSanPhamId);

}
