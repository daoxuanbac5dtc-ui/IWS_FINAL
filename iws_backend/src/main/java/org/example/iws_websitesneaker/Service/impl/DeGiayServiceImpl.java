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
    //ThÃªm
    @Override
    public void addDeGiay(DeGiay deGiay) {
        repoDeGiay.save(deGiay);
    }
    //Sá»­a
    @Override
    public void updateDeGiay(DeGiay deGiay) {
        repoDeGiay.save(deGiay);
    }
    //XÃ³a
    @Override
    public void deleteDeGiay(int id) {
        repoSanPham.removeDeGiayReference(id);// Gá»¡ rÃ ng buá»™c khÃ³a ngoáº¡i tá»« báº£ng san_pham
        repoDeGiay.deleteById(id);
    }
}

