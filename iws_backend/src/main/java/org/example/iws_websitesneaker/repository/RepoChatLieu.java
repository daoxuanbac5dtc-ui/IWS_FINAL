package org.example.iws_websitesneaker.repository;
import org.example.iws_websitesneaker.entity.ChatLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoChatLieu extends JpaRepository<ChatLieu, Integer> {
}

