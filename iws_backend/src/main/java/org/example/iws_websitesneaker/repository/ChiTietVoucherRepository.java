package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.ChiTietVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietVoucherRepository extends JpaRepository<ChiTietVoucher, Integer> {
    List<ChiTietVoucher> findByMaChiTietVoucherContainingIgnoreCase(String maChiTietVoucher);
    List<ChiTietVoucher> findByMaVoucherContainingIgnoreCase(String maVoucher);
    List<ChiTietVoucher> findByTenVoucherContainingIgnoreCase(String tenVoucher);
    List<ChiTietVoucher> findByHoaDonIdOrderByNgayApDungDesc(Integer hoaDonId);

    List<ChiTietVoucher> findByVoucherIdOrderByNgayApDungDesc(Integer voucherId);

    List<ChiTietVoucher> findByHoaDonId(Integer hoaDonId);

    List<ChiTietVoucher> findByVoucherId(Integer voucherId);

    @Query("SELECT cv FROM ChiTietVoucher cv WHERE cv.hoaDon.id = :hoaDonId AND cv.voucher.id = :voucherId")
    Optional<ChiTietVoucher> findByHoaDonIdAndVoucherId(@Param("hoaDonId") Integer hoaDonId,
                                                        @Param("voucherId") Integer voucherId);

    @Query("SELECT SUM(cv.soTienGiam) FROM ChiTietVoucher cv WHERE cv.hoaDon.id = :hoaDonId")
    BigDecimal sumSoTienGiamByHoaDonId(@Param("hoaDonId") Integer hoaDonId);

    boolean existsByHoaDonIdAndVoucherId(Integer hoaDonId, Integer voucherId);
}

