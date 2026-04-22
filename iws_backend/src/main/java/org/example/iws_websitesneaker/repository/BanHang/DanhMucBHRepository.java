package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DanhMucBHRepository extends JpaRepository<DanhMuc, Integer> {
    Optional<DanhMuc> findByMaDanhMuc(String maDanhMuc);
    Boolean existsByMaDanhMuc(String maDanhMuc);
    @Query("SELECT dm FROM DanhMuc dm WHERE dm.trangThai = :trangThai ORDER BY dm.tenDanhMuc ASC")
    List<DanhMuc> findByTrangThaiOrderByTenDanhMuc(@Param("trangThai") Integer trangThai);
    List<DanhMuc> findByTrangThaiOrderByTenDanhMucAsc(Integer trangThai);
}

