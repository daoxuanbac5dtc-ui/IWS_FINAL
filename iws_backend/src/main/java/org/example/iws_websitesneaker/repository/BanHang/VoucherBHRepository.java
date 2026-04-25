package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherBHRepository extends JpaRepository<Voucher, Integer> {

    // âœ… Sá»¬A: Query vá»›i handling null tongTien
    @Query("SELECT v FROM Voucher v " +
            "WHERE v.trangThai = 1 " +
            "AND v.soLuong > 0 " +
            "AND v.ngayBatDau <= :currentDate " +
            "AND v.ngayKetThuc >= :currentDate " +
            "AND (:tongTien IS NULL OR v.giaTriGiamToiThieu <= :tongTien) " +
            "ORDER BY v.giaTriGiam DESC")
    List<Voucher> findAvailableVouchers(@Param("currentDate") Date currentDate,
                                        @Param("tongTien") Double tongTien);

    // âœ… THÃŠM: Query láº¥y táº¥t cáº£ voucher kháº£ dá»¥ng (khÃ´ng filter theo tongTien)
    @Query("SELECT v FROM Voucher v " +
            "WHERE v.trangThai = 1 " +
            "AND v.soLuong > 0 " +
            "AND v.ngayBatDau <= :currentDate " +
            "AND v.ngayKetThuc >= :currentDate " +
            "ORDER BY v.giaTriGiam DESC")
    List<Voucher> findAllAvailableVouchers(@Param("currentDate") Date currentDate);

    // TÃ¬m voucher theo mÃ£
    Optional<Voucher> findByMaVoucher(String maVoucher);

    // âœ… Sá»¬A: Kiá»ƒm tra voucher cÃ³ thá»ƒ sá»­ dá»¥ng khÃ´ng
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Voucher v " +
            "WHERE v.maVoucher = :maVoucher " +
            "AND v.trangThai = 1 " +
            "AND v.soLuong > 0 " +
            "AND v.ngayBatDau <= :currentDate " +
            "AND v.ngayKetThuc >= :currentDate")
    boolean isVoucherUsable(@Param("maVoucher") String maVoucher,
                            @Param("currentDate") Date currentDate);

    // Láº¥y voucher hoáº¡t Ä‘á»™ng
    List<Voucher> findByTrangThaiOrderByNgayTaoDesc(Integer trangThai);
}
