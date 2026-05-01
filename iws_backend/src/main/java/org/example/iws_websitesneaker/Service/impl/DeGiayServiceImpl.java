package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Service.DeGiayService;
import org.example.iws_websitesneaker.entity.DeGiay;
import org.example.iws_websitesneaker.repository.RepoDeGiay;
import org.example.iws_websitesneaker.repository.RepoSanPham;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeGiayServiceImpl implements DeGiayService {
    @Autowired
    private RepoDeGiay repoDeGiay;
    @Autowired
    private RepoSanPham repoSanPham;
    @Override
    public List<DeGiay> getAllDeGiay() {
        return repoDeGiay.findAll();
    }

    @Override
    public Optional<DeGiay> getDeGiayById(int id) {
        return repoDeGiay.findById(id);
    }
    //Thêm
    @Override
    public void addDeGiay(DeGiay deGiay) {
        repoDeGiay.save(deGiay);
    }
    //Sửa
    @Override
    public void updateDeGiay(DeGiay deGiay) {
        repoDeGiay.save(deGiay);
    }
    //Xóa
    @Override
    public void deleteDeGiay(int id) {
        repoSanPham.removeDeGiayReference(id);// Gỡ ràng buộc khóa ngoại từ bảng san_pham
        repoDeGiay.deleteById(id);
    }
}

