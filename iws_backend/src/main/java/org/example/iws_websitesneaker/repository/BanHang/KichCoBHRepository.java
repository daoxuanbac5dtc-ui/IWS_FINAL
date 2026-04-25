package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.KichCo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KichCoBHRepository extends JpaRepository<KichCo, Integer> {
//    List<KichCo> findByTrangThaiOrderByTenKichCo(Integer trangThai);
    Optional<KichCo> findByMaKichCo(String maKichCo);
    @Query("SELECT kc FROM KichCo kc WHERE kc.trangThai = :trangThai ORDER BY kc.tenKichCo ASC")
    List<KichCo> findByTrangThaiOrderByTenKichCo(@Param("trangThai") Integer trangThai);
    List<KichCo> findByTrangThaiOrderByTenKichCoAsc(Integer trangThai);
}

