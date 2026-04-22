package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.DeGiay;

import java.util.List;
import java.util.Optional;

public interface DeGiayService {
    List<DeGiay> getAllDeGiay();
    Optional<DeGiay> getDeGiayById(int id);
    // ThÃªm
    void addDeGiay(DeGiay deGiay);
    // Sá»­a
    void updateDeGiay(DeGiay deGiay);
    //XÃ³a
    void deleteDeGiay(int id);
}

