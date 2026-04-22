package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.ViDiem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoViDiem extends JpaRepository<ViDiem, Integer> {

    // TÃ¬m vÃ­ Ä‘iá»ƒm cÃ³ tá»•ng Ä‘iá»ƒm lá»›n hÆ¡n
    @Query("SELECT v FROM ViDiem v WHERE v.tongDiem > :diem")
    List<ViDiem> findByTongDiemGreaterThan(@Param("diem") Double diem);

    // TÃ¬m vÃ­ Ä‘iá»ƒm cÃ³ Ä‘iá»ƒm hiá»‡n táº¡i lá»›n hÆ¡n (tongDiem - soDiemDaDung)
    @Query("SELECT v FROM ViDiem v WHERE (v.tongDiem - v.soDiemDaDung) > :diem")
    List<ViDiem> findByDiemHienTaiGreaterThan(@Param("diem") Double diem);

    // TÃ¬m vÃ­ Ä‘iá»ƒm cÃ³ tá»•ng Ä‘iá»ƒm trong khoáº£ng
    @Query("SELECT v FROM ViDiem v WHERE v.tongDiem BETWEEN :min AND :max")
    List<ViDiem> findByTongDiemBetween(@Param("min") Double min, @Param("max") Double max);

    // Äáº¿m sá»‘ vÃ­ Ä‘iá»ƒm
    @Query("SELECT COUNT(v) FROM ViDiem v")
    Long countAllViDiem();

    // TÃ­nh tá»•ng Ä‘iá»ƒm cá»§a táº¥t cáº£ vÃ­ Ä‘iá»ƒm
    @Query("SELECT SUM(v.tongDiem) FROM ViDiem v")
    Double sumAllTongDiem();

    // TÃ¬m vÃ­ Ä‘iá»ƒm theo giÃ¡ trá»‹ Ä‘iá»ƒm
    List<ViDiem> findByGiaTriDiem(Double giaTriDiem);

    @Query("SELECT v FROM ViDiem v JOIN KhachHang k ON k.viDiem.id = v.id WHERE k.id = :khachHangId")
    Optional<ViDiem> findByKhachHangId(@Param("khachHangId") Integer khachHangId);

    @Query("SELECT SUM(v.tongDiem) FROM ViDiem v")
    Double getTotalPointsInSystem();
}

