package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.Voucher;

import java.util.List;
import java.util.Optional;

public interface VoucherService {
    //Hiá»ƒn thi
    List<Voucher> getVouchers();
    //
    Optional<Voucher> getVoucherById(int id);
    void addVoucher(Voucher voucher);
    void updateVoucher(Voucher voucher);
    void deleteVoucher(int id);
}

