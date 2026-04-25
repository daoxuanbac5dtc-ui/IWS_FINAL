package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.ThuongHieu;

import java.util.List;
import java.util.Optional;

public interface ThuongHieuService {
    //Hiá»ƒn thi
    List<ThuongHieu> getAllThuongHieu();
    Optional<ThuongHieu> getThuongHieuById(int id);
    void addThuongHieu(ThuongHieu thuongHieu);
    void updateThuongHieu(ThuongHieu thuongHieu);
    void deleteThuongHieuById(int id);
}

