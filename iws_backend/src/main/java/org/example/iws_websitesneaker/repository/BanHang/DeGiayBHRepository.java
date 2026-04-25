package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.DeGiay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeGiayBHRepository extends JpaRepository<DeGiay, Integer> {
    @Query("SELECT dg FROM DeGiay dg WHERE dg.trangThai = :trangThai ORDER BY dg.tenDeGiay ASC")
    List<DeGiay> findByTrangThaiOrderByTenDeGiay(@Param("trangThai") Integer trangThai);
    Optional<DeGiay> findByMaDeGiay(String maDeGiay);
    List<DeGiay> findByTrangThaiOrderByTenDeGiayAsc(Integer trangThai);
}

