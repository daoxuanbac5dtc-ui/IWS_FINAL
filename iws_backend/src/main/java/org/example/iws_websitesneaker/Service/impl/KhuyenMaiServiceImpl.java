package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Dto.KhuyenMaiRequest;
import org.example.iws_websitesneaker.Service.KhuyenMaiService;
import org.example.iws_websitesneaker.entity.KhuyenMai;
import org.example.iws_websitesneaker.repository.KhuyenMaiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class KhuyenMaiServiceImpl implements KhuyenMaiService {
    @Autowired
    private KhuyenMaiRepository repository;

    @Override
    public List<KhuyenMai> getAll() {
        return repository.findAll();
    }

    @Override
    public KhuyenMai getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y khuyáº¿n mÃ£i vá»›i ID: " + id));
    }

    @Override
    public KhuyenMai create(KhuyenMaiRequest request) {
        if (repository.findByMaKhuyenMai(request.getMaKhuyenMai()).isPresent()) {
            throw new RuntimeException("MÃ£ khuyáº¿n mÃ£i Ä‘Ã£ tá»“n táº¡i: " + request.getMaKhuyenMai());
        }

        KhuyenMai khuyenMai = new KhuyenMai();
        khuyenMai.setMaKhuyenMai(request.getMaKhuyenMai());
        khuyenMai.setTenKhuyenMai(request.getTenKhuyenMai());
        khuyenMai.setNgayBatDau(request.getNgayBatDau());
        khuyenMai.setNgayKetThuc(request.getNgayKetThuc());
        khuyenMai.setTrangThai(request.getTrangThai());
        khuyenMai.setGiaTri(request.getGiaTri());
        khuyenMai.setNgayTao(new Date());

        return repository.save(khuyenMai);
    }

    @Override
    public KhuyenMai update(Integer id, KhuyenMaiRequest request) {
        KhuyenMai khuyenMai = getById(id);

        if (!khuyenMai.getMaKhuyenMai().equals(request.getMaKhuyenMai())) {
            if (repository.findByMaKhuyenMai(request.getMaKhuyenMai()).isPresent()) {
                throw new RuntimeException("MÃ£ khuyáº¿n mÃ£i Ä‘Ã£ tá»“n táº¡i: " + request.getMaKhuyenMai());
            }
        }

        khuyenMai.setMaKhuyenMai(request.getMaKhuyenMai());
        khuyenMai.setTenKhuyenMai(request.getTenKhuyenMai());
        khuyenMai.setNgayBatDau(request.getNgayBatDau());
        khuyenMai.setNgayKetThuc(request.getNgayKetThuc());
        khuyenMai.setTrangThai(request.getTrangThai());
        khuyenMai.setGiaTri(request.getGiaTri());
        khuyenMai.setNgayCapNhat(new Date());

        return repository.save(khuyenMai);
    }

    @Override
    public void delete(Integer id) {
        KhuyenMai khuyenMai = getById(id);
        repository.delete(khuyenMai);
    }

    @Override
    public List<KhuyenMai> getByTrangThai(Integer trangThai) {
        return repository.findByTrangThai(trangThai);
    }

    @Override
    public List<KhuyenMai> getActivePromotions() {
        return repository.findActivePromotions(new Date());
    }

    @Override
    public List<KhuyenMai> searchByKeyword(String keyword) {
        return repository.findByKeyword(keyword);
    }

    @Override
    public KhuyenMai changeStatus(Integer id) {
        KhuyenMai khuyenMai = getById(id);
        khuyenMai.setTrangThai(khuyenMai.getTrangThai() == 1 ? 0 : 1);
        khuyenMai.setNgayCapNhat(new Date());
        return repository.save(khuyenMai);
    }
}
