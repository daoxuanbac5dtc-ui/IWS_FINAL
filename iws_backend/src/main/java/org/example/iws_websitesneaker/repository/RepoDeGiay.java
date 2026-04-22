package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.DeGiay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoDeGiay extends JpaRepository<DeGiay, Integer> {
}

