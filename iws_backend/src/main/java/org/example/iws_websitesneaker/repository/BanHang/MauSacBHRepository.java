package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MauSacBHRepository extends JpaRepository<MauSac, Integer> {
    @Query("SELECT ms FROM MauSac ms WHERE ms.trangThai = :trangThai ORDER BY ms.tenMauSac ASC")
    List<MauSac> findByTrangThaiOrderByTenMauSac(@Param("trangThai") Integer trangThai);
    Optional<MauSac> findByMaMauSac(String maMauSac);
    List<MauSac> findByTrangThaiOrderByTenMauSacAsc(Integer trangThai);

}

