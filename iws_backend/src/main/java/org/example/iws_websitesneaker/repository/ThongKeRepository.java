package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ThongKeRepository extends JpaRepository<HoaDon , Integer> {

    // Doanh thu theo thÃ¡ng
    @Query(value = """
            SELECT 
                MONTH(hd.ngay_tao) AS thang, 
                COALESCE(SUM(hd.tong_tien), 0) AS doanh_thu
            FROM hoa_don hd
            WHERE hd.trang_thai_hoa_don = N'HoÃ n thÃ nh' OR hd.trang_thai_hoa_don = 'COMPLETED'
            GROUP BY MONTH(hd.ngay_tao)
            ORDER BY thang
            """, nativeQuery = true)
    List<Object[]> getDoanhThuTheoThang();

    // Tá»•ng doanh thu (chá»‰ hÃ³a Ä‘Æ¡n Ä‘Ã£ thanh toÃ¡n)
    @Query(value = """
            SELECT COALESCE(SUM(hd.tong_tien), 0)
            FROM hoa_don hd
            WHERE hd.trang_thai_hoa_don = 'HoÃ n thÃ nh' or hd.trang_thai_hoa_don = 'COMPLETED'
            """, nativeQuery = true)
    Integer getTongDoanhThu();

    // Tá»•ng Ä‘Æ¡n hÃ ng
    @Query(value = "SELECT COUNT(*) FROM hoa_don", nativeQuery = true)
    Integer getTongDonHang();

    // Tá»•ng khÃ¡ch hÃ ng
    @Query(value = "SELECT COUNT(*) FROM khach_hang", nativeQuery = true)
    Integer getTongKhachHang();

    // Tá»•ng sáº£n pháº©m
    @Query(value = "SELECT COUNT(*) FROM san_pham", nativeQuery = true)
    Integer getTongSanPham();

    // Sá»‘ lÆ°á»£ng bÃ¡n cháº¡y theo thÃ¡ng
    @Query(value = """
    SELECT MONTH(hd.ngay_tao) AS thang,
           SUM(hdct.so_luong) AS soLuong
    FROM hoa_don_chi_tiet hdct
    JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
    WHERE hd.trang_thai_hoa_don = 'DA_THANH_TOAN'
    GROUP BY MONTH(hd.ngay_tao)
    ORDER BY thang
    """, nativeQuery = true)
    List<Object[]> getSoLuongBanTheoThang();

    // Sá»‘ lÆ°á»£ng bÃ¡n cháº¡y theo tuáº§n
    @Query(value = """
    SELECT DATEPART(WEEK, hd.ngay_tao) AS tuan,
           SUM(hdct.so_luong) AS soLuong
    FROM hoa_don_chi_tiet hdct
    JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
    WHERE hd.trang_thai_hoa_don = 'DA_THANH_TOAN'
    GROUP BY DATEPART(WEEK, hd.ngay_tao)
    ORDER BY tuan
    """, nativeQuery = true)
    List<Object[]> getSoLuongBanTheoTuan();

    // Top sáº£n pháº©m bÃ¡n cháº¡y (Top N)
    @Query(value = """
            SELECT TOP (:top) 
                sp.id AS idSanPham,
                sp.ma_san_pham AS maSanPham,
                sp.ten_san_pham AS tenSanPham,
                SUM(hdct.so_luong) AS tongSoLuongBan,
                SUM(hdct.so_luong * hdct.gia) AS tongDoanhThu
            FROM hoa_don_chi_tiet hdct
            JOIN chi_tiet_san_pham ctsp ON hdct.id_ctsp = ctsp.id
            JOIN san_pham sp ON ctsp.id_san_pham = sp.id
            JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
            WHERE hd.trang_thai_hoa_don = N'HoÃ n thÃ nh' OR hd.trang_thai_hoa_don = 'COMPLETED'
            GROUP BY sp.id, sp.ma_san_pham, sp.ten_san_pham
            ORDER BY tongSoLuongBan DESC
            """, nativeQuery = true)
    List<Object[]> findTopSanPhamBanChay(int top);

    // Tráº¡ng thÃ¡i Ä‘Æ¡n hÃ ng

    @Query("""
        SELECT new map(hd.trangThaiHoaDon as status, COUNT(hd) as count)
        FROM HoaDon hd
        GROUP BY hd.trangThaiHoaDon
    """)
    List<Map<String, Object>> thongKeDonHangTheoTrangThaiHoaDon();

}

