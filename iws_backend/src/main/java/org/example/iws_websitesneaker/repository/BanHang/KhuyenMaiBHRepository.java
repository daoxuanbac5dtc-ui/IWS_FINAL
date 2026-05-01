package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface KhuyenMaiBHRepository extends JpaRepository<KhuyenMai, Integer> {

    /**
     * Tìm khuyến mãi theo mã
     */
    Optional<KhuyenMai> findByMaKhuyenMai(String maKhuyenMai);

    /**
     * Lấy khuyến mãi đang hoạt động
     */
    @Query("SELECT km FROM KhuyenMai km " +
            "WHERE km.trangThai = 1 " +
            "AND :currentDate BETWEEN km.ngayBatDau AND km.ngayKetThuc " +
            "ORDER BY km.giaTri DESC")
    List<KhuyenMai> findActiveKhuyenMai(@Param("currentDate") Date currentDate);
}

