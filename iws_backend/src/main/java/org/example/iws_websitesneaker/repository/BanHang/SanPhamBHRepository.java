package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamBHRepository extends JpaRepository<SanPham, Integer> {
    Integer countByDanhMucIdAndTrangThai(Integer danhMucId, Integer trangThai);
    Integer countByThuongHieuIdAndTrangThai(Integer thuongHieuId, Integer trangThai);
    Optional<SanPham> findByMaSanPham(String maSanPham);
    List<SanPham> findByDanhMucIdAndTrangThai(Integer danhMucId, Integer trangThai);
}

