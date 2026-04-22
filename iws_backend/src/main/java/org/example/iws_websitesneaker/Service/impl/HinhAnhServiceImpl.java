package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Service.HinhAnhService;
import org.example.iws_websitesneaker.entity.HinhAnh;
import org.example.iws_websitesneaker.repository.RepoChiTietSanPham;
import org.example.iws_websitesneaker.repository.RepoHinhAnh;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HinhAnhServiceImpl implements HinhAnhService {
    @Autowired
    private RepoHinhAnh repoHinhAnh;
    @Autowired
    private RepoChiTietSanPham repoChiTietSanPham;
    @Override
    public List<HinhAnh> getAllHinhanh() {
        return repoHinhAnh.findAll();
    }

//    Detail
    @Override
    public Optional<HinhAnh> getHinhanhById(int id) {
        return repoHinhAnh.findById(id);
    }

    //ThÃªm
    @Override
    public void addHinhAnh(HinhAnh hinhAnh) {
        repoHinhAnh.save(hinhAnh);
    }
    //Sá»­a
    @Override
    public void updateHinhAnh(HinhAnh hinhAnh) {
        repoHinhAnh.save(hinhAnh);
    }
    //XÃ³a
    @Override
    public void deleteHinhAnh(int id) {
        repoChiTietSanPham.removeHinhAnhReference(id);// XÃ³a ctsp trc
        repoHinhAnh.deleteById(id);
    }

    @Override
    public Optional<HinhAnh> findByTenHinhAnh(String tenHinhAnh){
         return repoHinhAnh.findByTenHinhAnh(tenHinhAnh);
    }
//    @Override
//    public List<HinhAnh> findByChiTietSanPhamId(Integer chiTietSanPhamId) {
//        return repoHinhAnh.findByChiTietSanPhamId(chiTietSanPhamId);
//    }
}

