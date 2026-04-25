package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.KichCo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoKichCo extends JpaRepository<KichCo, Integer> {
    Optional<KichCo> findByMaKichCo(String maKichCo);

    boolean existsByMaKichCo(String maKichCo);

    @Query("SELECT k FROM KichCo k WHERE k.trangThai = 1")
    List<KichCo> findAllActive();
}

