package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.LichSuDiem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoLichSuDiem extends JpaRepository<LichSuDiem, Integer> {
}

