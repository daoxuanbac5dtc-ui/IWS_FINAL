package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaiKhoanBHRepository extends JpaRepository<TaiKhoan, Integer> {
    Optional<TaiKhoan> findByEmail(String email);
    Optional<TaiKhoan> findByMaTaiKhoan(String maTaiKhoan);
    Boolean existsByEmail(String email);
}

