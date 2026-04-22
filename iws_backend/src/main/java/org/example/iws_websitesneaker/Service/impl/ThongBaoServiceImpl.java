package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Service.ThongBaoService;
import org.example.iws_websitesneaker.entity.KhachHang;
import org.example.iws_websitesneaker.entity.MauSac;
import org.example.iws_websitesneaker.entity.ThongBao;
import org.example.iws_websitesneaker.repository.RepoHoaDon;
//import org.example.iws_websitesneaker.repository.RepoKhachHang;
import org.example.iws_websitesneaker.repository.RepoThongBao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ThongBaoServiceImpl  implements ThongBaoService {
    @Autowired
  private RepoThongBao repoThongBao;
//    @Autowired
//    private RepoKhachHang repoKhachHang;
    @Autowired
    private RepoHoaDon repoHoaDon;

    @Override
    public List<ThongBao> getAll() {
        return repoThongBao.findAll();
    }

    @Override
    public Optional<ThongBao> getById(int id) {
        return repoThongBao.findById(id);
    }

    @Override
    public void add(ThongBao thongBao) {
//        KhachHang khachHang = repoKhachHang.findById(thongBao.getTaiKhoan().getId()).orElse(null);

        repoThongBao.save(thongBao);
    }

    @Override
    public void update(ThongBao thongBao) {
        repoThongBao.save(thongBao);
    }

    @Override
    public void delete(int id) {
        repoThongBao.deleteById(id);
    }
}

