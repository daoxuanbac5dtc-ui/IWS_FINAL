package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.DeGiay;

import java.util.List;
import java.util.Optional;

public interface DeGiayService {
    List<DeGiay> getAllDeGiay();
    Optional<DeGiay> getDeGiayById(int id);
    // Thêm
    void addDeGiay(DeGiay deGiay);
    // Sửa
    void updateDeGiay(DeGiay deGiay);
    //Xóa
    void deleteDeGiay(int id);
}

