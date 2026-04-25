package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.KichCo;

import java.util.List;
import java.util.Optional;

public interface KichCoService {
    List<KichCo> getAllKichCo();
    Optional<KichCo> getKichCoById(int id);
    // ThÃªm
    void addKichCo(KichCo kichCo);
    //Sá»­a
    void updateKichCo(KichCo kichCo);
    //XÃ³a
    void deleteKichCo(int id);
}

