package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.ChiTietVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RepoChiTietVoucher extends JpaRepository<ChiTietVoucher,Integer> {
    void deleteByHoaDonId(Integer id);
    // XÃ³a voucher - sá»­a id voucher trong báº£ng ctsp vá» null
    @Modifying
    @Transactional
    @Query("UPDATE ChiTietVoucher ctv SET ctv.voucher = NULL WHERE ctv.voucher.id = :id")
    void removeVoucherReference(@Param("id") int id);

    @Query("SELECT ctv FROM ChiTietVoucher ctv " +
            "LEFT JOIN FETCH ctv.voucher v " +
            "WHERE ctv.hoaDon.id = :hoaDonId")
    List<ChiTietVoucher> findByHoaDonId(@Param("hoaDonId") Integer hoaDonId);
}

