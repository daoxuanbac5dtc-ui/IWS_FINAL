package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoKhuyenMai extends JpaRepository<KhuyenMai, Integer> {
    @Query("SELECT km FROM KhuyenMai km WHERE km.trangThai = 1 " +
            "AND CURRENT_TIMESTAMP BETWEEN km.ngayBatDau AND km.ngayKetThuc " +
            "ORDER BY km.ngayTao DESC")
    List<KhuyenMai> findActivePromotions();

    boolean existsByMaKhuyenMai(String maKhuyenMai);
}

