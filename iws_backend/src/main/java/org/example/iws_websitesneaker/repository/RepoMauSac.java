package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoMauSac extends JpaRepository<MauSac, Integer> {
    Optional<MauSac> findByMaMauSac(String maMauSac);

    boolean existsByMaMauSac(String maMauSac);

    @Query("SELECT m FROM MauSac m WHERE m.trangThai = 1")
    List<MauSac> findAllActive();
}

