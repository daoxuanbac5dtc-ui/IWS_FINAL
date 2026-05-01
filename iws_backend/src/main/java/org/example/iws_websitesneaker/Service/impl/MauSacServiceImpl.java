package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Service.MauSacService;
import org.example.iws_websitesneaker.entity.MauSac;
import org.example.iws_websitesneaker.repository.RepoChiTietSanPham;
import org.example.iws_websitesneaker.repository.RepoMauSac;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MauSacServiceImpl implements MauSacService {

    @Autowired
    private RepoMauSac repoMauSac;

    @Autowired
    private RepoChiTietSanPham repoChiTietSanPham;
    @Override
    public List<MauSac> getAll() {
        return repoMauSac.findAll();
    }

    @Override
    public Optional<MauSac> getById(int id) {
        return repoMauSac.findById(id);
    }

    @Override
    public void add(MauSac mauSac) {
        repoMauSac.save(mauSac);
    }

    @Override
    public void update(MauSac mauSac) {
        repoMauSac.save(mauSac);
    }

    @Override
    public void delete(int id) {
        repoChiTietSanPham.removeMauSacReference(id);// Gỡ ràng buộc khóa ngoại từ bảng ctsp
        repoMauSac.deleteById(id);
    }
}

