package org.example.iws_websitesneaker.Service;


import org.example.iws_websitesneaker.entity.DanhMuc;

import java.util.List;
import java.util.Optional;

public interface DanhMucService {
    List<DanhMuc> getDanhMuc();
    Optional<DanhMuc> getDanhMucById(int id);
    void addDanhMuc(DanhMuc danhMuc);
    void updateDanhMuc(DanhMuc danhMuc);
    void deleteDanhMuc(int id);
}

