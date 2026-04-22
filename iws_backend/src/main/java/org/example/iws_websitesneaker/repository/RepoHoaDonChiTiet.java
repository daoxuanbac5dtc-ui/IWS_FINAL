package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RepoHoaDonChiTiet extends JpaRepository<HoaDonChiTiet, Integer> {

    List<HoaDonChiTiet> findByChiTietSanPhamId(Integer chiTietSanPhamId);

    @Query("SELECT hdct FROM HoaDonChiTiet hdct " +
            "LEFT JOIN FETCH hdct.hoaDon hd " +
            "LEFT JOIN FETCH hdct.chiTietSanPham ctsp " +
            "LEFT JOIN FETCH ctsp.sanPham sp " +
            "WHERE hdct.chiTietSanPham.id = :chiTietSanPhamId " +
            "ORDER BY hdct.ngayTao DESC")
    List<HoaDonChiTiet> findByChiTietSanPhamIdWithInfo(@Param("chiTietSanPhamId") Integer chiTietSanPhamId);

    // Method tÃ­nh tá»•ng sá»‘ lÆ°á»£ng Ä‘Ã£ bÃ¡n cá»§a má»™t chi tiáº¿t sáº£n pháº©m (chá»‰ Ä‘Æ¡n hÃ ng hoÃ n thÃ nh)
    @Query("SELECT COALESCE(SUM(h.soLuong), 0) FROM HoaDonChiTiet h " +
            "WHERE h.chiTietSanPham.id = :chiTietSanPhamId " +
            "AND h.hoaDon.trangThaiHoaDon IN ('COMPLETED', 'DELIVERED', 'DA_GIAO', 'HOAN_THANH')")
    Integer getTotalSoldQuantity(@Param("chiTietSanPhamId") Integer chiTietSanPhamId);

    List<HoaDonChiTiet> findByHoaDonId(Integer hoaDonId);

    @Query("SELECT hdct FROM HoaDonChiTiet hdct " +
            "LEFT JOIN FETCH hdct.chiTietSanPham ctsp " +
            "LEFT JOIN FETCH ctsp.sanPham sp " +
            "LEFT JOIN FETCH sp.thuongHieu th " +
            "LEFT JOIN FETCH sp.danhMuc dm " +
            "LEFT JOIN FETCH ctsp.mauSac ms " +
            "LEFT JOIN FETCH ctsp.kichCo kc " +
            "LEFT JOIN FETCH ctsp.hinhAnh ha " +
            "WHERE hdct.hoaDon.id = :hoaDonId")
    List<HoaDonChiTiet> findByHoaDonIdWithFullInfo(@Param("hoaDonId") Integer hoaDonId);
}

