package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.TaiKhoanVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RepoTaiKhoanVoucher extends JpaRepository<TaiKhoanVoucher, Integer> {
    // XÃ³a voucher - sá»­a id voucher trong báº£ng ctsp vá» null
    @Modifying
    @Transactional
    @Query("UPDATE TaiKhoanVoucher tkv SET tkv.voucher = NULL WHERE tkv.voucher.id = :id")
    void removeVoucherReference(@Param("id") int id);

    @Query("SELECT tv FROM TaiKhoanVoucher tv WHERE tv.taiKhoan.id = :taiKhoanId AND tv.trangThai = 1")
    List<TaiKhoanVoucher> findByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    @Query("SELECT tv FROM TaiKhoanVoucher tv WHERE tv.voucher.id = :voucherId AND tv.trangThai = 1")
    List<TaiKhoanVoucher> findByVoucherId(@Param("voucherId") Integer voucherId);

    boolean existsByTaiKhoanIdAndVoucherId(Integer taiKhoanId, Integer voucherId);
}

