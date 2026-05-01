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

    // Tìm kiếm và sắp xếp cơ bản
    @Query("SELECT h FROM HoaDon h ORDER BY h.ngayTao DESC")
    List<HoaDon> findAllOrderByNgayTaoDesc();

    @Query("SELECT h FROM HoaDon h WHERE h.trangThaiHoaDon = :status ORDER BY h.ngayTao DESC")
    List<HoaDon> findByTrangThaiHoaDonOrderByNgayTaoDesc(@Param("status") String status);

    @Query("SELECT h FROM HoaDon h WHERE h.loaiHoaDon = 'OFFLINE' ORDER BY h.ngayTao DESC")
    List<HoaDon> findPOSInvoices();

    @Query("SELECT h FROM HoaDon h WHERE h.loaiHoaDon = 'ONLINE' ORDER BY h.ngayTao DESC")
    List<HoaDon> findOnlineInvoices();

    // Tìm kiếm với điều kiện
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

    // Đếm theo trạng thái
    Long countByTrangThaiHoaDon(String trangThai);

    @Query(value = """
            SELECT COUNT(*)
            FROM hoa_don h
            WHERE h.trang_thai_hoa_don IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')
               OR HEX(h.trang_thai_hoa_don) = '486FC3A06E207468C3A06E68'
            """, nativeQuery = true)
    Long countCompletedInvoices();

    // ✅ QUERY AN TOÀN - Tính tổng tiền theo trạng thái
    @Query(value = """
            SELECT GREATEST(
                COALESCE((
                    SELECT SUM(COALESCE(h.tong_thanh_toan, h.tong_tien, 0))
                    FROM hoa_don h
                    WHERE h.trang_thai_hoa_don = :status
                       OR (:status = 'COMPLETED' AND (
                            h.trang_thai_hoa_don IN ('DA_THANH_TOAN', 'HOAN_THANH')
                            OR HEX(h.trang_thai_hoa_don) = '486FC3A06E207468C3A06E68'
                       ))
                ), 0)
                -
                CASE WHEN :status = 'COMPLETED' THEN COALESCE((
                    SELECT SUM(ctt.so_luong * COALESCE(hdct.gia, ctsp.gia_goc, 0))
                    FROM chi_tiet_tra_hang ctt
                    JOIN chi_tiet_san_pham ctsp ON ctt.id_ctsp = ctsp.id
                    LEFT JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = ctt.id_hoa_don AND hdct.id_ctsp = ctt.id_ctsp
                    WHERE ctt.trang_thai_hoa_don = 'APPROVED'
                ), 0) ELSE 0 END,
            0)
            """, nativeQuery = true)
    Double sumTotalAmountByStatus(@Param("status") String status);

    // ✅ QUERY AN TOÀN - Doanh thu theo tháng với tham số
    @Query(value = """
            SELECT GREATEST(
                COALESCE((
                    SELECT SUM(COALESCE(h.tong_thanh_toan, h.tong_tien, 0))
                    FROM hoa_don h
                    WHERE (h.trang_thai_hoa_don IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')
                           OR HEX(h.trang_thai_hoa_don) = '486FC3A06E207468C3A06E68')
                      AND YEAR(h.ngay_tao) = :year
                      AND MONTH(h.ngay_tao) = :month
                ), 0)
                -
                COALESCE((
                    SELECT SUM(ctt.so_luong * COALESCE(hdct.gia, ctsp.gia_goc, 0))
                    FROM chi_tiet_tra_hang ctt
                    JOIN chi_tiet_san_pham ctsp ON ctt.id_ctsp = ctsp.id
                    LEFT JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = ctt.id_hoa_don AND hdct.id_ctsp = ctt.id_ctsp
                    WHERE ctt.trang_thai_hoa_don = 'APPROVED'
                      AND YEAR(ctt.ngay_tao_tra_hang) = :year
                      AND MONTH(ctt.ngay_tao_tra_hang) = :month
                ), 0),
            0)
            """, nativeQuery = true)
    Double sumMonthlyRevenue(@Param("year") int year, @Param("month") int month);

    // ✅ QUERY AN TOÀN - Doanh thu theo ngày với tham số
    @Query(value = """
            SELECT GREATEST(
                COALESCE((
                    SELECT SUM(COALESCE(h.tong_thanh_toan, h.tong_tien, 0))
                    FROM hoa_don h
                    WHERE (h.trang_thai_hoa_don IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')
                           OR HEX(h.trang_thai_hoa_don) = '486FC3A06E207468C3A06E68')
                      AND h.ngay_tao >= :startOfDay AND h.ngay_tao < :endOfDay
                ), 0)
                -
                COALESCE((
                    SELECT SUM(ctt.so_luong * COALESCE(hdct.gia, ctsp.gia_goc, 0))
                    FROM chi_tiet_tra_hang ctt
                    JOIN chi_tiet_san_pham ctsp ON ctt.id_ctsp = ctsp.id
                    LEFT JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = ctt.id_hoa_don AND hdct.id_ctsp = ctt.id_ctsp
                    WHERE ctt.trang_thai_hoa_don = 'APPROVED'
                      AND ctt.ngay_tao_tra_hang >= :startOfDay AND ctt.ngay_tao_tra_hang < :endOfDay
                ), 0),
            0)
            """, nativeQuery = true)
    Double sumDailyRevenue(@Param("startOfDay") Date startOfDay, @Param("endOfDay") Date endOfDay);

    // ✅ QUERY ĐƠN GIẢN NHẤT - Doanh thu theo năm
    @Query(value = """
            SELECT GREATEST(
                COALESCE((
                    SELECT SUM(COALESCE(h.tong_thanh_toan, h.tong_tien, 0))
                    FROM hoa_don h
                    WHERE (h.trang_thai_hoa_don IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')
                           OR HEX(h.trang_thai_hoa_don) = '486FC3A06E207468C3A06E68')
                      AND YEAR(h.ngay_tao) = :year
                ), 0)
                -
                COALESCE((
                    SELECT SUM(ctt.so_luong * COALESCE(hdct.gia, ctsp.gia_goc, 0))
                    FROM chi_tiet_tra_hang ctt
                    JOIN chi_tiet_san_pham ctsp ON ctt.id_ctsp = ctsp.id
                    LEFT JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = ctt.id_hoa_don AND hdct.id_ctsp = ctt.id_ctsp
                    WHERE ctt.trang_thai_hoa_don = 'APPROVED'
                      AND YEAR(ctt.ngay_tao_tra_hang) = :year
                ), 0),
            0)
            """, nativeQuery = true)
    Double sumYearlyRevenue(@Param("year") int year);

    // ✅ DOANH THU THEO KHOẢNG THỜI GIAN - AN TOÀN NHẤT
    @Query(value = """
            SELECT GREATEST(
                COALESCE((
                    SELECT SUM(COALESCE(h.tong_thanh_toan, h.tong_tien, 0))
                    FROM hoa_don h
                    WHERE (h.trang_thai_hoa_don IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')
                           OR HEX(h.trang_thai_hoa_don) = '486FC3A06E207468C3A06E68')
                      AND h.ngay_tao >= :startDate AND h.ngay_tao <= :endDate
                ), 0)
                -
                COALESCE((
                    SELECT SUM(ctt.so_luong * COALESCE(hdct.gia, ctsp.gia_goc, 0))
                    FROM chi_tiet_tra_hang ctt
                    JOIN chi_tiet_san_pham ctsp ON ctt.id_ctsp = ctsp.id
                    LEFT JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = ctt.id_hoa_don AND hdct.id_ctsp = ctt.id_ctsp
                    WHERE ctt.trang_thai_hoa_don = 'APPROVED'
                      AND ctt.ngay_tao_tra_hang >= :startDate AND ctt.ngay_tao_tra_hang <= :endDate
                ), 0),
            0)
            """, nativeQuery = true)
    Double sumRevenueByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    Optional<HoaDon> findByEmailAndMaHoaDon(String email, String maHoaDon);
    // ✅ XÓA method sumTodayRevenue() vì nó gây lỗi
    // Method này sẽ được thay thế bằng sumDailyRevenue() với startOfDay và endOfDay
    @Query("SELECT h FROM HoaDon h WHERE h.khachHang.id = :khachHangId ORDER BY h.ngayTao DESC")
    List<HoaDon> findByKhachHangId(@Param("khachHangId") Integer khachHangId);

    // Kiểm tra guest orders
    boolean existsByEmailAndKhachHangIsNull(String email);
    boolean existsBySdtAndKhachHangIsNull(String sdt);
    boolean existsByEmailAndSdtAndKhachHangIsNull(String email, String sdt);
}
