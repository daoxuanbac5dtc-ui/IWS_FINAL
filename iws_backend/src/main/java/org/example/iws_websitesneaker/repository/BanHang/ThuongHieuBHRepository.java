package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThuongHieuBHRepository extends JpaRepository<ThuongHieu, Integer> {
    @Query("SELECT th FROM ThuongHieu th WHERE th.trangThai = :trangThai ORDER BY th.tenThuongHieu ASC")
    List<ThuongHieu> findByTrangThaiOrderByTenThuongHieu(@Param("trangThai") Integer trangThai);
    Optional<ThuongHieu> findByMaThuongHieu(String maThuongHieu);
    Boolean existsByMaThuongHieu(String maThuongHieu);
    List<ThuongHieu> findByTrangThaiOrderByTenThuongHieuAsc(Integer trangThai);

}

