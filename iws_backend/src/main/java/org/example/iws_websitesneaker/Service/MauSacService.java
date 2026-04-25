package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.MauSac;

import java.util.List;
import java.util.Optional;

public interface MauSacService {
    List<MauSac> getAll();
    Optional<MauSac> getById(int id);
    void add(MauSac mauSac);
    void update(MauSac mauSac);
    void delete(int id);
}

