package org.example.iws_websitesneaker.Service;


import org.example.iws_websitesneaker.entity.MauSac;
import org.example.iws_websitesneaker.entity.ThongBao;

import java.util.List;
import java.util.Optional;

public interface ThongBaoService {
    List<ThongBao> getAll();
    Optional<ThongBao> getById(int id);
    void add(ThongBao thongBao);
    void update(ThongBao thongBao);
    void delete(int id);
}

