package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuHoaDonBHRepository extends JpaRepository<LichSuHoaDon, Integer> {
    List<LichSuHoaDon> findByHoaDonIdOrderByNgayTaoDesc(Integer hoaDonId);
    List<LichSuHoaDon> findByHoaDonIdAndTrangThaiHoaDon(Integer hoaDonId, String trangThai);

    void deleteByHoaDonId(Integer hoaDonId);

    @Modifying
    @Query("DELETE FROM LichSuHoaDon l WHERE l.hoaDon.id = :hoaDonId")
    void deleteByHoaDonIdCustom(@Param("hoaDonId") Integer hoaDonId);
}
