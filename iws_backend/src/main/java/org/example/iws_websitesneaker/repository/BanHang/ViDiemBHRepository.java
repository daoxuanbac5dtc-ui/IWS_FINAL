package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.ViDiem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ViDiemBHRepository extends JpaRepository<ViDiem, Integer> {

    // Query thÃ´ng qua báº£ng khach_hang
    @Query("SELECT kh.viDiem FROM KhachHang kh WHERE kh.id = :khachHangId")
    Optional<ViDiem> findByKhachHangId(@Param("khachHangId") Integer khachHangId);

    // Láº¥y Ä‘iá»ƒm hiá»‡n táº¡i cá»§a khÃ¡ch hÃ ng
    @Query("SELECT (vd.tongDiem - vd.soDiemDaDung) FROM KhachHang kh JOIN kh.viDiem vd WHERE kh.id = :khachHangId")
    Optional<Double> getDiemHienTaiByKhachHangId(@Param("khachHangId") Integer khachHangId);

    // Cáº­p nháº­t Ä‘iá»ƒm cho khÃ¡ch hÃ ng
    @Query("UPDATE ViDiem vd SET vd.tongDiem = :tongDiem, vd.soDiemDaCong = :soDiemDaCong WHERE vd.id = (SELECT kh.viDiem.id FROM KhachHang kh WHERE kh.id = :khachHangId)")
    void updateDiemByKhachHangId(@Param("khachHangId") Integer khachHangId,
                                 @Param("tongDiem") Double tongDiem,
                                 @Param("soDiemDaCong") Double soDiemDaCong);
}
