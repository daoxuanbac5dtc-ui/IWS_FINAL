package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Service.ViDiemService;
import org.example.iws_websitesneaker.entity.ViDiem;
import org.example.iws_websitesneaker.repository.RepoViDiem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class ViDiemServiceImpl implements ViDiemService {

    @Autowired
    private RepoViDiem viDiemRepository;

    @Override
    public List<ViDiem> findAll() {
        return viDiemRepository.findAll();
    }

    @Override
    public Optional<ViDiem> findById(Integer id) {
        return viDiemRepository.findById(id);
    }

    @Override
    public ViDiem save(ViDiem viDiem) {
        return viDiemRepository.save(viDiem);
    }

    @Override
    public void deleteById(Integer id) {
        viDiemRepository.deleteById(id);
    }

    @Override
    public ViDiem createDefaultViDiem() {
        ViDiem viDiem = new ViDiem();
        viDiem.setNgayTao(new Date());
        viDiem.setNgayCapNhat(new Date());
        return save(viDiem);
    }

    @Override
    public ViDiem updateDiem(Integer id, Double diemMoi, String loaiGiaoDich) {
        Optional<ViDiem> viDiemOpt = findById(id);
        if (!viDiemOpt.isPresent()) {
            throw new RuntimeException("KhÃ´ng tÃ¬m tháº¥y vÃ­ Ä‘iá»ƒm vá»›i ID: " + id);
        }

        ViDiem viDiem = viDiemOpt.get();

        if ("CONG_DIEM".equals(loaiGiaoDich)) {
            // Cá»™ng Ä‘iá»ƒm
            viDiem.setTongDiem(viDiem.getTongDiem() + diemMoi);
            viDiem.setSoDiemDaCong(viDiem.getSoDiemDaCong() + diemMoi);
        } else if ("TRU_DIEM".equals(loaiGiaoDich)) {
            // Trá»« Ä‘iá»ƒm (sá»­ dá»¥ng Ä‘iá»ƒm)
            Double diemHienTai = calculateDiemHienTai(viDiem);
            if (diemHienTai >= diemMoi) {
                viDiem.setSoDiemDaDung(viDiem.getSoDiemDaDung() + diemMoi);
            } else {
                throw new RuntimeException("KhÃ´ng Ä‘á»§ Ä‘iá»ƒm Ä‘á»ƒ sá»­ dá»¥ng. Äiá»ƒm hiá»‡n táº¡i: " + diemHienTai);
            }
        } else {
            throw new RuntimeException("Loáº¡i giao dá»‹ch khÃ´ng há»£p lá»‡. Sá»­ dá»¥ng CONG_DIEM hoáº·c TRU_DIEM");
        }

        viDiem.setNgayCapNhat(new Date());
        return save(viDiem);
    }

    @Override
    public ViDiem congDiem(Integer id, Double diem) {
        return updateDiem(id, diem, "CONG_DIEM");
    }

    @Override
    public ViDiem truDiem(Integer id, Double diem) {
        return updateDiem(id, diem, "TRU_DIEM");
    }

    @Override
    public Double getDiemHienTai(Integer id) {
        Optional<ViDiem> viDiemOpt = findById(id);
        if (!viDiemOpt.isPresent()) {
            throw new RuntimeException("KhÃ´ng tÃ¬m tháº¥y vÃ­ Ä‘iá»ƒm vá»›i ID: " + id);
        }
        return calculateDiemHienTai(viDiemOpt.get());
    }

    @Override
    public Double getGiaTriTien(Integer id) {
        Optional<ViDiem> viDiemOpt = findById(id);
        if (!viDiemOpt.isPresent()) {
            throw new RuntimeException("KhÃ´ng tÃ¬m tháº¥y vÃ­ Ä‘iá»ƒm vá»›i ID: " + id);
        }
        ViDiem viDiem = viDiemOpt.get();
        Double diemHienTai = calculateDiemHienTai(viDiem);
        return diemHienTai * viDiem.getGiaTriDiem();
    }

    // Helper method Ä‘á»ƒ tÃ­nh Ä‘iá»ƒm hiá»‡n táº¡i
    private Double calculateDiemHienTai(ViDiem viDiem) {
        Double tongDiem = viDiem.getTongDiem() != null ? viDiem.getTongDiem() : 0.0;
        Double soDiemDaDung = viDiem.getSoDiemDaDung() != null ? viDiem.getSoDiemDaDung() : 0.0;
        return tongDiem - soDiemDaDung;
    }
}


