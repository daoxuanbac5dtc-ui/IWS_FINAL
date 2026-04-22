package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.CongThucTinhDiem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoCongThucTinhDiem extends JpaRepository<CongThucTinhDiem, Integer> {
}

