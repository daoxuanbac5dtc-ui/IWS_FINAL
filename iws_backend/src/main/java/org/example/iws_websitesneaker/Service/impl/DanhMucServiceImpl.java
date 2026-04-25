package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Service.DanhMucService;
import org.example.iws_websitesneaker.entity.DanhMuc;
import org.example.iws_websitesneaker.repository.RepoDanhMuc;
import org.example.iws_websitesneaker.repository.RepoSanPham;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DanhMucServiceImpl implements DanhMucService {
    @Autowired
    private RepoDanhMuc repoDanhMuc;
    @Autowired
    private RepoSanPham repoSanPham;
    @Override
    public List<DanhMuc> getDanhMuc() {
        return repoDanhMuc.findAll();
    }

    @Override
    public Optional<DanhMuc> getDanhMucById(int id) {
        return repoDanhMuc.findById(id);
    }

    @Override
    public void addDanhMuc(DanhMuc danhMuc) {
        repoDanhMuc.save(danhMuc);
    }

    @Override
    public void updateDanhMuc(DanhMuc danhMuc) {
        repoDanhMuc.save(danhMuc);
    }

    @Override
    public void deleteDanhMuc(int id) {
        repoSanPham.removeDanhMucReference(id);// Gá»¡ rÃ ng buá»™c khÃ³a ngoáº¡i tá»« báº£ng san_pham
        repoDanhMuc.deleteById(id);
    }
}

