package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoThongBao extends JpaRepository<ThongBao, Integer> {
}

