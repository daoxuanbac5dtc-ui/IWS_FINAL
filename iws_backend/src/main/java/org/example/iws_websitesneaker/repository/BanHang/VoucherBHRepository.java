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

    // ✅ SỬA: Query với handling null tongTien
    @Query("SELECT v FROM Voucher v " +
            "WHERE v.trangThai = 1 " +
            "AND v.soLuong > 0 " +
            "AND v.ngayBatDau <= :currentDate " +
            "AND v.ngayKetThuc >= :currentDate " +
            "AND (:tongTien IS NULL OR v.giaTriGiamToiThieu <= :tongTien) " +
            "ORDER BY v.giaTriGiam DESC")
    List<Voucher> findAvailableVouchers(@Param("currentDate") Date currentDate,
                                        @Param("tongTien") Double tongTien);

    // ✅ THÊM: Query lấy tất cả voucher khả dụng (không filter theo tongTien)
    @Query("SELECT v FROM Voucher v " +
            "WHERE v.trangThai = 1 " +
            "AND v.soLuong > 0 " +
            "AND v.ngayBatDau <= :currentDate " +
            "AND v.ngayKetThuc >= :currentDate " +
            "ORDER BY v.giaTriGiam DESC")
    List<Voucher> findAllAvailableVouchers(@Param("currentDate") Date currentDate);

    // Tìm voucher theo mã
    Optional<Voucher> findByMaVoucher(String maVoucher);

    // ✅ SỬA: Kiểm tra voucher có thể sử dụng không
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Voucher v " +
            "WHERE v.maVoucher = :maVoucher " +
            "AND v.trangThai = 1 " +
            "AND v.soLuong > 0 " +
            "AND v.ngayBatDau <= :currentDate " +
            "AND v.ngayKetThuc >= :currentDate")
    boolean isVoucherUsable(@Param("maVoucher") String maVoucher,
                            @Param("currentDate") Date currentDate);

    // Lấy voucher hoạt động
    List<Voucher> findByTrangThaiOrderByNgayTaoDesc(Integer trangThai);
}
