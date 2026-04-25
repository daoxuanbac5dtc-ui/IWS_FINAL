package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaChiBHRepository extends JpaRepository<DiaChi, Integer> {
    Optional<DiaChi> findByTaiKhoanIdAndTrangThaiOrderByNgayTaoDesc(Integer taiKhoanId, Integer trangThai);
    @Query("SELECT d FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId AND d.isDefault = true AND d.trangThai = 1")
    Optional<DiaChi> findByTaiKhoanIdAndIsDefaultTrue(@Param("taiKhoanId") Integer taiKhoanId);
    @Query("SELECT d FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId AND d.trangThai = 1 ORDER BY d.isDefault DESC, d.ngayTao DESC")
    List<DiaChi> findByTaiKhoanIdAndTrangThai(@Param("taiKhoanId") Integer taiKhoanId, @Param("trangThai") Integer trangThai);
}
