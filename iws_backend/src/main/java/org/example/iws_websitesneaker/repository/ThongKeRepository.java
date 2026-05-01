package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ThongKeRepository extends JpaRepository<HoaDon, Integer> {

    @Query(value = """
            SELECT sold.thang,
                   GREATEST(sold.doanh_thu - COALESCE(ret.tien_tra, 0), 0) AS doanh_thu
            FROM (
                SELECT MONTH(hd.ngay_tao) AS thang,
                       COALESCE(SUM(COALESCE(hd.tong_thanh_toan, hd.tong_tien, 0)), 0) AS doanh_thu
                FROM hoa_don hd
                WHERE hd.trang_thai_hoa_don IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')
                   OR HEX(hd.trang_thai_hoa_don) = '486FC3A06E207468C3A06E68'
                GROUP BY MONTH(hd.ngay_tao)
            ) sold
            LEFT JOIN (
                SELECT MONTH(ctt.ngay_tao_tra_hang) AS thang,
                       COALESCE(SUM(ctt.so_luong * COALESCE(hdct.gia, ctsp.gia_goc, 0)), 0) AS tien_tra
                FROM chi_tiet_tra_hang ctt
                JOIN chi_tiet_san_pham ctsp ON ctt.id_ctsp = ctsp.id
                LEFT JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = ctt.id_hoa_don AND hdct.id_ctsp = ctt.id_ctsp
                WHERE ctt.trang_thai_hoa_don = 'APPROVED'
                GROUP BY MONTH(ctt.ngay_tao_tra_hang)
            ) ret ON ret.thang = sold.thang
            ORDER BY sold.thang
            """, nativeQuery = true)
    List<Object[]> getDoanhThuTheoThang();

    @Query(value = """
            SELECT GREATEST(
                COALESCE((
                    SELECT SUM(COALESCE(hd.tong_thanh_toan, hd.tong_tien, 0))
                    FROM hoa_don hd
                    WHERE hd.trang_thai_hoa_don IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')
                       OR HEX(hd.trang_thai_hoa_don) = '486FC3A06E207468C3A06E68'
                ), 0)
                -
                COALESCE((
                    SELECT SUM(ctt.so_luong * COALESCE(hdct.gia, ctsp.gia_goc, 0))
                    FROM chi_tiet_tra_hang ctt
                    JOIN chi_tiet_san_pham ctsp ON ctt.id_ctsp = ctsp.id
                    LEFT JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = ctt.id_hoa_don AND hdct.id_ctsp = ctt.id_ctsp
                    WHERE ctt.trang_thai_hoa_don = 'APPROVED'
                ), 0),
            0)
            """, nativeQuery = true)
    Double getTongDoanhThu();

    @Query(value = "SELECT COUNT(*) FROM hoa_don", nativeQuery = true)
    Integer getTongDonHang();

    @Query(value = "SELECT COUNT(*) FROM khach_hang", nativeQuery = true)
    Integer getTongKhachHang();

    @Query(value = "SELECT COUNT(*) FROM san_pham", nativeQuery = true)
    Integer getTongSanPham();

    @Query(value = """
            SELECT MONTH(hd.ngay_tao) AS thang,
                   SUM(GREATEST(hdct.so_luong - COALESCE(ret.so_luong_tra, 0), 0)) AS soLuong
            FROM hoa_don_chi_tiet hdct
            JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
            LEFT JOIN (
                SELECT id_hoa_don, id_ctsp, SUM(so_luong) AS so_luong_tra
                FROM chi_tiet_tra_hang
                WHERE trang_thai_hoa_don = 'APPROVED'
                GROUP BY id_hoa_don, id_ctsp
            ) ret ON ret.id_hoa_don = hd.id AND ret.id_ctsp = hdct.id_ctsp
            WHERE hd.trang_thai_hoa_don IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')
               OR HEX(hd.trang_thai_hoa_don) = '486FC3A06E207468C3A06E68'
            GROUP BY MONTH(hd.ngay_tao)
            ORDER BY thang
            """, nativeQuery = true)
    List<Object[]> getSoLuongBanTheoThang();

    @Query(value = """
            SELECT WEEK(hd.ngay_tao, 1) AS tuan,
                   SUM(GREATEST(hdct.so_luong - COALESCE(ret.so_luong_tra, 0), 0)) AS soLuong
            FROM hoa_don_chi_tiet hdct
            JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
            LEFT JOIN (
                SELECT id_hoa_don, id_ctsp, SUM(so_luong) AS so_luong_tra
                FROM chi_tiet_tra_hang
                WHERE trang_thai_hoa_don = 'APPROVED'
                GROUP BY id_hoa_don, id_ctsp
            ) ret ON ret.id_hoa_don = hd.id AND ret.id_ctsp = hdct.id_ctsp
            WHERE hd.trang_thai_hoa_don IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')
               OR HEX(hd.trang_thai_hoa_don) = '486FC3A06E207468C3A06E68'
            GROUP BY WEEK(hd.ngay_tao, 1)
            ORDER BY tuan
            """, nativeQuery = true)
    List<Object[]> getSoLuongBanTheoTuan();

    @Query(value = """
            SELECT
                sp.id AS idSanPham,
                sp.ma_san_pham AS maSanPham,
                sp.ten_san_pham AS tenSanPham,
                SUM(GREATEST(hdct.so_luong - COALESCE(ret.so_luong_tra, 0), 0)) AS tongSoLuongBan,
                SUM(GREATEST(hdct.so_luong - COALESCE(ret.so_luong_tra, 0), 0) * hdct.gia) AS tongDoanhThu
            FROM hoa_don_chi_tiet hdct
            JOIN chi_tiet_san_pham ctsp ON hdct.id_ctsp = ctsp.id
            JOIN san_pham sp ON ctsp.id_san_pham = sp.id
            JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
            LEFT JOIN (
                SELECT id_hoa_don, id_ctsp, SUM(so_luong) AS so_luong_tra
                FROM chi_tiet_tra_hang
                WHERE trang_thai_hoa_don = 'APPROVED'
                GROUP BY id_hoa_don, id_ctsp
            ) ret ON ret.id_hoa_don = hd.id AND ret.id_ctsp = hdct.id_ctsp
            WHERE hd.trang_thai_hoa_don IN ('COMPLETED', 'DA_THANH_TOAN', 'HOAN_THANH')
               OR HEX(hd.trang_thai_hoa_don) = '486FC3A06E207468C3A06E68'
            GROUP BY sp.id, sp.ma_san_pham, sp.ten_san_pham
            HAVING tongSoLuongBan > 0
            ORDER BY tongSoLuongBan DESC
            LIMIT :top
            """, nativeQuery = true)
    List<Object[]> findTopSanPhamBanChay(@Param("top") int top);

    @Query("""
        SELECT new map(hd.trangThaiHoaDon as status, COUNT(hd) as count)
        FROM HoaDon hd
        GROUP BY hd.trangThaiHoaDon
    """)
    List<Map<String, Object>> thongKeDonHangTheoTrangThaiHoaDon();
}
