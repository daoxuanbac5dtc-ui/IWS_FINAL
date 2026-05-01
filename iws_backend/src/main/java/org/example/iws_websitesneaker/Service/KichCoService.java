package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.KichCo;

import java.util.List;
import java.util.Optional;

public interface KichCoService {
    List<KichCo> getAllKichCo();
    Optional<KichCo> getKichCoById(int id);
    // Thêm
    void addKichCo(KichCo kichCo);
    //Sửa
    void updateKichCo(KichCo kichCo);
    //Xóa
    void deleteKichCo(int id);
}

