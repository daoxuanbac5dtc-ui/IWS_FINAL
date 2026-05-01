package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Service.KichCoService;
import org.example.iws_websitesneaker.entity.KichCo;
import org.example.iws_websitesneaker.repository.RepoChiTietSanPham;
import org.example.iws_websitesneaker.repository.RepoKichCo;
import org.example.iws_websitesneaker.repository.RepoSanPham;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KichCoServiceImpl implements KichCoService {
    @Autowired
    private RepoKichCo repoKichCo;

    @Autowired
    private RepoChiTietSanPham repoChiTietSanPham;
    @Override
    public List<KichCo> getAllKichCo() {
        return repoKichCo.findAll();
    }

    @Override
    public Optional<KichCo> getKichCoById(int id) {
        return repoKichCo.findById(id);
    }
    //Thêm
    @Override
    public void addKichCo(KichCo kichCo) {
        repoKichCo.save(kichCo);
    }
    //Sửa
    @Override
    public void updateKichCo(KichCo kichCo) {
        repoKichCo.save(kichCo);
    }
    //Xóa
    @Override
    public void deleteKichCo(int id) {
        repoChiTietSanPham.removeKichCoReference(id);// Gỡ ràng buộc khóa ngoại từ bảng ctsp
        repoKichCo.deleteById(id);
    }
}

