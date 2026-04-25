package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoThuongHIeu extends JpaRepository<ThuongHieu,Integer> {
    Optional<ThuongHieu> findByMaThuongHieu(String maThuongHieu);

    boolean existsByMaThuongHieu(String maThuongHieu);

    @Query("SELECT t FROM ThuongHieu t WHERE t.trangThai = 1")
    List<ThuongHieu> findAllActive();
}

