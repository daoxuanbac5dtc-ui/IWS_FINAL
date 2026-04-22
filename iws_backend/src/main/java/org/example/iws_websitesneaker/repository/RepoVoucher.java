package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepoVoucher extends JpaRepository<Voucher, Integer> {
    Optional<Voucher> findByMaVoucher(String maVoucher);

    @Query("SELECT v FROM Voucher v WHERE " +
            "v.trangThai = 1 AND " +
            "v.soLuong > 0 AND " +
            "v.ngayBatDau <= :currentDate AND " +
            "v.ngayKetThuc >= :currentDate AND " +
            "v.giaTriGiamToiThieu <= :tongTien " +
            "ORDER BY v.giaTriGiamToiDa DESC")
    List<Voucher> findAvailableVouchers(@Param("currentDate") Date currentDate, @Param("tongTien") Double tongTien);

    @Query("SELECT v FROM Voucher v WHERE v.trangThai = 1 AND v.soLuong > 0")
    List<Voucher> findAllActive();

    @Query("SELECT v FROM Voucher v WHERE " +
            "v.ngayKetThuc < :currentDate OR v.soLuong = 0")
    List<Voucher> findExpiredVouchers(@Param("currentDate") Date currentDate);
}

