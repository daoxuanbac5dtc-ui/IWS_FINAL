package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoDanhMuc extends JpaRepository<DanhMuc, Integer> {
    @Query("SELECT dm FROM DanhMuc dm WHERE dm.trangThai = 1 ORDER BY dm.tenDanhMuc")
    List<DanhMuc> findAllActive();

    Optional<DanhMuc> findByMaDanhMuc(String maDanhMuc);

    boolean existsByMaDanhMuc(String maDanhMuc);
}

