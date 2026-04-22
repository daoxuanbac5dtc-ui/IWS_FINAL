package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.ViDiem;
import java.util.List;
import java.util.Optional;

public interface ViDiemService {
    List<ViDiem> findAll();
    Optional<ViDiem> findById(Integer id);
    ViDiem save(ViDiem viDiem);
    void deleteById(Integer id);
    ViDiem createDefaultViDiem();
    ViDiem updateDiem(Integer id, Double diemMoi, String loaiGiaoDich);
    ViDiem congDiem(Integer id, Double diem);
    ViDiem truDiem(Integer id, Double diem);
    Double getDiemHienTai(Integer id);
    Double getGiaTriTien(Integer id);
}

