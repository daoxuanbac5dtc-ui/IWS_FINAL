package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.HinhAnh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoHinhAnh extends JpaRepository<HinhAnh, Integer> {
    Optional<HinhAnh> findByTenHinhAnh(String tenHinhAnh);
}

