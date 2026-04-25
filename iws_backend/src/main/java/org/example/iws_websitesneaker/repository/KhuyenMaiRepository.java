package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, Integer> {

    Optional<KhuyenMai> findByMaKhuyenMai(String maKhuyenMai);

    List<KhuyenMai> findByTrangThai(Integer trangThai);

    @Query("SELECT km FROM KhuyenMai km " +
            "WHERE km.ngayBatDau <= :currentDate " +
            "AND km.ngayKetThuc >= :currentDate " +
            "AND km.trangThai = 1")
    List<KhuyenMai> findActivePromotions(@Param("currentDate") Date currentDate);

    @Query("SELECT km FROM KhuyenMai km " +
            "WHERE km.tenKhuyenMai LIKE %:keyword% " +
            "OR km.maKhuyenMai LIKE %:keyword%")
    List<KhuyenMai> findByKeyword(@Param("keyword") String keyword);
}

