package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Service.VoucherService;
import org.example.iws_websitesneaker.entity.Voucher;
import org.example.iws_websitesneaker.repository.RepoChiTietVoucher;
import org.example.iws_websitesneaker.repository.RepoTaiKhoanVoucher;
import org.example.iws_websitesneaker.repository.RepoVoucher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VoucherServiceImpl implements VoucherService {
    @Autowired
    private RepoVoucher repoVoucher;
    @Autowired
    private RepoChiTietVoucher repoChiTietVoucher;
    @Autowired
    private RepoTaiKhoanVoucher repoTaiKhoanVoucher;
    //Hiá»ƒn thi
    @Override
    public List<Voucher> getVouchers() {
        return repoVoucher.findAll();
    }

    @Override
    public Optional<Voucher> getVoucherById(int id) {
        return repoVoucher.findById(id);
    }

    @Override
    public void addVoucher(Voucher voucher) {
        repoVoucher.save(voucher);
    }

    @Override
    public void updateVoucher(Voucher voucher) {
        repoVoucher.save(voucher);
    }

    @Override
    public void deleteVoucher(int id) {
        repoTaiKhoanVoucher.removeVoucherReference(id);// Gá»¡ rÃ ng buá»™c khÃ³a ngoáº¡i tá»« báº£ng tÃ i kháº£on voucher
        repoChiTietVoucher.removeVoucherReference(id);// Gá»¡ rÃ ng buá»™c khÃ³a ngoáº¡i tá»« báº£ng chitietvoucher
        repoVoucher.deleteById(id);
    }
}

