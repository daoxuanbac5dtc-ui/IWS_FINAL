package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.KhuyenMaiRequest;
import org.example.iws_websitesneaker.entity.KhuyenMai;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface KhuyenMaiService {
    List<KhuyenMai> getAll();
    KhuyenMai getById(Integer id);
    KhuyenMai create(KhuyenMaiRequest request);
    KhuyenMai update(Integer id, KhuyenMaiRequest request);
    void delete(Integer id);
    List<KhuyenMai> getByTrangThai(Integer trangThai);
    List<KhuyenMai> getActivePromotions();
    List<KhuyenMai> searchByKeyword(String keyword);
    KhuyenMai changeStatus(Integer id);
}


