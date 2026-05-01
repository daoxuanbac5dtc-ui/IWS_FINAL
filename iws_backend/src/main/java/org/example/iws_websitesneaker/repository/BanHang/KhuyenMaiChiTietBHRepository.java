package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface KhuyenMaiChiTietBHRepository extends JpaRepository<KhuyenMaiChiTiet, Integer> {

    /**
     * Lấy khuyến mãi cho sản phẩm
     */
    @Query("SELECT kmct FROM KhuyenMaiChiTiet kmct " +
            "WHERE kmct.id = :chiTietSanPhamId " +
            "AND kmct.khuyenMai.trangThai = 1 " +
            "AND kmct.trangThai = 1 " +
            "AND :currentDate BETWEEN kmct.khuyenMai.ngayBatDau AND kmct.khuyenMai.ngayKetThuc " +
            "ORDER BY kmct.khuyenMai.giaTri DESC")
    List<KhuyenMaiChiTiet> findKhuyenMaiHienTaiByChiTietSanPham(
            @Param("chiTietSanPhamId") Integer chiTietSanPhamId,
            @Param("currentDate") Date currentDate);

    /**
     * Lấy khuyến mãi theo chi tiết sản phẩm
     */
    List<KhuyenMaiChiTiet> findByChiTietSanPham_IdAndTrangThai(Integer chiTietSanPhamId, Integer trangThai);

    /**
     * Kiểm tra sản phẩm có khuyến mãi
     */
    @Query("SELECT COUNT(kmct) > 0 FROM KhuyenMaiChiTiet kmct " +
            "WHERE kmct.id = :chiTietSanPhamId " +
            "AND kmct.khuyenMai.trangThai = 1 " +
            "AND kmct.trangThai = 1 " +
            "AND :currentDate BETWEEN kmct.khuyenMai.ngayBatDau AND kmct.khuyenMai.ngayKetThuc")
    Boolean hasActiveKhuyenMai(
            @Param("chiTietSanPhamId") Integer chiTietSanPhamId,
            @Param("currentDate") Date currentDate);
}

