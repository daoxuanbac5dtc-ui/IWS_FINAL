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

    // Tìm ví điểm có tổng điểm lớn hơn
    @Query("SELECT v FROM ViDiem v WHERE v.tongDiem > :diem")
    List<ViDiem> findByTongDiemGreaterThan(@Param("diem") Double diem);

    // Tìm ví điểm có điểm hiện tại lớn hơn (tongDiem - soDiemDaDung)
    @Query("SELECT v FROM ViDiem v WHERE (v.tongDiem - v.soDiemDaDung) > :diem")
    List<ViDiem> findByDiemHienTaiGreaterThan(@Param("diem") Double diem);

    // Tìm ví điểm có tổng điểm trong khoảng
    @Query("SELECT v FROM ViDiem v WHERE v.tongDiem BETWEEN :min AND :max")
    List<ViDiem> findByTongDiemBetween(@Param("min") Double min, @Param("max") Double max);

    // Đếm số ví điểm
    @Query("SELECT COUNT(v) FROM ViDiem v")
    Long countAllViDiem();

    // Tính tổng điểm của tất cả ví điểm
    @Query("SELECT SUM(v.tongDiem) FROM ViDiem v")
    Double sumAllTongDiem();

    // Tìm ví điểm theo giá trị điểm
    List<ViDiem> findByGiaTriDiem(Double giaTriDiem);

    @Query("SELECT v FROM ViDiem v JOIN KhachHang k ON k.viDiem.id = v.id WHERE k.id = :khachHangId")
    Optional<ViDiem> findByKhachHangId(@Param("khachHangId") Integer khachHangId);

    @Query("SELECT SUM(v.tongDiem) FROM ViDiem v")
    Double getTotalPointsInSystem();
}

