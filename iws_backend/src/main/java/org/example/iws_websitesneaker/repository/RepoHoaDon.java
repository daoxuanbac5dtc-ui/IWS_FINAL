package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepoHoaDon extends JpaRepository<HoaDon, Integer> {

    // TÃ¬m kiáº¿m vÃ  sáº¯p xáº¿p cÆ¡ báº£n
    @Query("SELECT h FROM HoaDon h ORDER BY h.ngayTao DESC")
    List<HoaDon> findAllOrderByNgayTaoDesc();

    @Query("SELECT h FROM HoaDon h WHERE h.trangThaiHoaDon = :status ORDER BY h.ngayTao DESC")
    List<HoaDon> findByTrangThaiHoaDonOrderByNgayTaoDesc(@Param("status") String status);

    @Query("SELECT h FROM HoaDon h WHERE h.loaiHoaDon = 'OFFLINE' ORDER BY h.ngayTao DESC")
    List<HoaDon> findPOSInvoices();

    @Query("SELECT h FROM HoaDon h WHERE h.loaiHoaDon = 'ONLINE' ORDER BY h.ngayTao DESC")
    List<HoaDon> findOnlineInvoices();

    // TÃ¬m kiáº¿m vá»›i Ä‘iá»u kiá»‡n
    @Query("SELECT h FROM HoaDon h WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "h.maHoaDon LIKE %:keyword% OR " +
            "h.tenNguoiDung LIKE %:keyword% OR " +
            "h.sdt LIKE %:keyword% OR " +
            "(h.khachHang IS NOT NULL AND h.khachHang.hoTen LIKE %:keyword%)) AND " +
            "(:trangThai IS NULL OR :trangThai = '' OR h.trangThaiHoaDon = :trangThai) AND " +
            "(:loaiHoaDon IS NULL OR :loaiHoaDon = '' OR h.loaiHoaDon = :loaiHoaDon) " +
            "ORDER BY h.ngayTao DESC")
    List<HoaDon> searchInvoices(@Param("keyword") String keyword,
                                @Param("trangThai") String trangThai,
                                @Param("loaiHoaDon") String loaiHoaDon);

    // Äáº¿m theo tráº¡ng thÃ¡i
    Long countByTrangThaiHoaDon(String trangThai);

    // âœ… QUERY AN TOÃ€N - TÃ­nh tá»•ng tiá»n theo tráº¡ng thÃ¡i
    @Query("SELECT COALESCE(SUM(h.tongTien), 0.0) FROM HoaDon h WHERE h.trangThaiHoaDon = :status")
    Double sumTotalAmountByStatus(@Param("status") String status);

    // âœ… QUERY AN TOÃ€N - Doanh thu theo thÃ¡ng vá»›i tham sá»‘
    @Query("SELECT COALESCE(SUM(h.tongTien), 0.0) FROM HoaDon h WHERE " +
            "h.trangThaiHoaDon = 'COMPLETED' AND " +
            "FUNCTION('YEAR', h.ngayTao) = :year AND " +
            "FUNCTION('MONTH', h.ngayTao) = :month")
    Double sumMonthlyRevenue(@Param("year") int year, @Param("month") int month);

    // âœ… QUERY AN TOÃ€N - Doanh thu theo ngÃ y vá»›i tham sá»‘
    @Query("SELECT COALESCE(SUM(h.tongTien), 0.0) FROM HoaDon h WHERE " +
            "h.trangThaiHoaDon = 'COMPLETED' AND " +
            "h.ngayTao >= :startOfDay AND h.ngayTao < :endOfDay")
    Double sumDailyRevenue(@Param("startOfDay") Date startOfDay, @Param("endOfDay") Date endOfDay);

    // âœ… QUERY ÄÆ N GIáº¢N NHáº¤T - Doanh thu theo nÄƒm
    @Query("SELECT COALESCE(SUM(h.tongTien), 0.0) FROM HoaDon h WHERE " +
            "h.trangThaiHoaDon = 'COMPLETED' AND " +
            "FUNCTION('YEAR', h.ngayTao) = :year")
    Double sumYearlyRevenue(@Param("year") int year);

    // âœ… DOANH THU THEO KHOáº¢NG THá»œI GIAN - AN TOÃ€N NHáº¤T
    @Query("SELECT COALESCE(SUM(h.tongTien), 0.0) FROM HoaDon h WHERE " +
            "h.trangThaiHoaDon = 'COMPLETED' AND " +
            "h.ngayTao >= :startDate AND h.ngayTao <= :endDate")
    Double sumRevenueByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    Optional<HoaDon> findByEmailAndMaHoaDon(String email, String maHoaDon);
    // âœ… XÃ“A method sumTodayRevenue() vÃ¬ nÃ³ gÃ¢y lá»—i
    // Method nÃ y sáº½ Ä‘Æ°á»£c thay tháº¿ báº±ng sumDailyRevenue() vá»›i startOfDay vÃ  endOfDay
    @Query("SELECT h FROM HoaDon h WHERE h.khachHang.id = :khachHangId ORDER BY h.ngayTao DESC")
    List<HoaDon> findByKhachHangId(@Param("khachHangId") Integer khachHangId);

    // Kiá»ƒm tra guest orders
    boolean existsByEmailAndKhachHangIsNull(String email);
    boolean existsBySdtAndKhachHangIsNull(String sdt);
    boolean existsByEmailAndSdtAndKhachHangIsNull(String email, String sdt);
}
