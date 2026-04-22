package org.example.iws_websitesneaker.repository;
import org.example.iws_websitesneaker.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    @Query("SELECT hdct FROM HoaDonChiTiet hdct WHERE hdct.hoaDon.id = :hoaDonId")
    List<HoaDonChiTiet> findByHoaDonId(@Param("hoaDonId") Integer hoaDonId);

    Optional<HoaDonChiTiet> findByHoaDonIdAndChiTietSanPham_Id(Integer hoaDonId, Integer chiTietId);

    Optional<HoaDonChiTiet> findByHoaDonIdAndChiTietSanPhamId(Integer hoaDonId, Integer chiTietSanPhamId);

    @Modifying
    @Transactional
    @Query("DELETE FROM HoaDonChiTiet h WHERE h.hoaDon.id = :hoaDonId")
    void deleteByHoaDonId(@Param("hoaDonId") Integer hoaDonId);

    @Query("SELECT SUM(h.soLuong) FROM HoaDonChiTiet h WHERE h.hoaDon.ngayTao BETWEEN :startDate AND :endDate")
    Long getTotalQuantityByDate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    // XÃ³a Chi tiáº¿t sáº£n pháº©m  - - sá»­a id cl trong báº£ng ctsp vá» null
    @Modifying
    @Transactional
    @Query("UPDATE HoaDonChiTiet hdct SET hdct.chiTietSanPham = NULL WHERE hdct.chiTietSanPham.id = :id")
    void removeChiTietSanPhamReference(@Param("id") int id);
}

