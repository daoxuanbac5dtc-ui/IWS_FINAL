package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.GioHang;
import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepoGioHang extends JpaRepository<GioHang, Integer> {
    Optional<GioHang> findByTaiKhoan(TaiKhoan taiKhoan);
}

