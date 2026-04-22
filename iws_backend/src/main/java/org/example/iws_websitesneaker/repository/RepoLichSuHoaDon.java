package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoLichSuHoaDon extends JpaRepository<LichSuHoaDon,Integer> {
    List<LichSuHoaDon> findByNhanVienIdOrderByNgayTaoDesc(Integer nhanVienId);
    @Query("SELECT l FROM LichSuHoaDon l WHERE l.hoaDon.id = :hoaDonId AND l.trangThaiHoaDon = :trangThai ORDER BY l.ngayTao DESC")
    List<LichSuHoaDon> findByHoaDonIdAndTrangThai(@Param("hoaDonId") Integer hoaDonId, @Param("trangThai") String trangThai);
    List<LichSuHoaDon> findByHoaDonIdOrderByNgayTaoDesc(Integer hoaDonId);
}

