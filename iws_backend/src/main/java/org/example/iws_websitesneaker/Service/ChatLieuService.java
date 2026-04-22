package org.example.iws_websitesneaker.Service;



import org.example.iws_websitesneaker.entity.ChatLieu;

import java.util.List;
import java.util.Optional;

public interface ChatLieuService {
    List<ChatLieu> getAllChatLieu();
    Optional<ChatLieu> getChatLieuById(int id);
    void addChatLieu(ChatLieu chatLieu);
    void updateChatLieu(ChatLieu chatLieu);
    void deleteChatLieu(int id);
}

