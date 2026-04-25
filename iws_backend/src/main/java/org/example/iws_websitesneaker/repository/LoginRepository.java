package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginRepository extends JpaRepository<TaiKhoan, Integer> {
    Optional<TaiKhoan> findByEmailAndMatKhau(String email, String matKhau);
}

