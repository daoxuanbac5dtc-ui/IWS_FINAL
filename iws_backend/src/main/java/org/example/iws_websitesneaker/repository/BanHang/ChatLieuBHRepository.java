package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.ChatLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatLieuBHRepository extends JpaRepository<ChatLieu, Integer> {
//    List<ChatLieu> findByTrangThaiOrderByTenChatLieu(Integer trangThai);
    Optional<ChatLieu> findByMaChatLieu(String maChatLieu);
    @Query("SELECT cl FROM ChatLieu cl WHERE cl.trangThai = :trangThai ORDER BY cl.tenChatLieu ASC")
    List<ChatLieu> findByTrangThaiOrderByTenChatLieu(@Param("trangThai") Integer trangThai);
    List<ChatLieu> findByTrangThaiOrderByTenChatLieuAsc(Integer trangThai);
}

