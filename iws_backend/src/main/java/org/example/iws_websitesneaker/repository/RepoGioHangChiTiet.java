package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.GioHang;
import org.example.iws_websitesneaker.entity.GioHangChiTIet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoGioHangChiTiet extends JpaRepository<GioHangChiTIet,Integer> {
    List<GioHangChiTIet> findByGioHang(GioHang gioHang);

    Optional<GioHangChiTIet> findByGioHangAndChiTietSanPham_Id(GioHang gioHang, Integer chiTietSanPhamId);

    void deleteByGioHang(GioHang gioHang);
}

