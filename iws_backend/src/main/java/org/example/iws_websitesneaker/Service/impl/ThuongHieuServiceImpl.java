package org.example.iws_websitesneaker.Service.impl;

import jakarta.transaction.Transactional;
import org.example.iws_websitesneaker.Service.ThuongHieuService;
import org.example.iws_websitesneaker.entity.ThuongHieu;
import org.example.iws_websitesneaker.repository.RepoSanPham;
import org.example.iws_websitesneaker.repository.RepoThuongHIeu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ThuongHieuServiceImpl implements ThuongHieuService {
    @Autowired
    private RepoThuongHIeu repoThuongHIeu;
    @Autowired
    private RepoSanPham repoSanPham;

    @Override
    public List<ThuongHieu> getAllThuongHieu() {
        return repoThuongHIeu.findAll();
    }

    @Override
    public Optional<ThuongHieu> getThuongHieuById(int id) {
        return repoThuongHIeu.findById(id);
    }

    @Override
    public void addThuongHieu(ThuongHieu thuongHieu) {
        repoThuongHIeu.save(thuongHieu);
    }

    @Override
    public void updateThuongHieu(ThuongHieu thuongHieu) {
        repoThuongHIeu.save(thuongHieu);
    }

    @Override
    public void deleteThuongHieuById(int id) {
        repoSanPham.removeThuongHieuReference(id); // Gá»¡ rÃ ng buá»™c khÃ³a ngoáº¡i tá»« báº£ng san_pham
        repoThuongHIeu.deleteById(id);// XÃ³a thÆ°Æ¡ng hiá»‡u
    }
}

