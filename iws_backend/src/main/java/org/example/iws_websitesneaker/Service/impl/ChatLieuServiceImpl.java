package org.example.iws_websitesneaker.Service.impl;


import org.example.iws_websitesneaker.Service.ChatLieuService;
import org.example.iws_websitesneaker.entity.ChatLieu;
import org.example.iws_websitesneaker.repository.RepoChatLieu;
import org.example.iws_websitesneaker.repository.RepoSanPham;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatLieuServiceImpl implements ChatLieuService {
    @Autowired
    RepoChatLieu chatLieuRepository;

    @Autowired
    RepoSanPham sanPhamRepository;

    @Override
    public List<ChatLieu> getAllChatLieu() {
        return chatLieuRepository.findAll();
    }

    @Override
    public Optional<ChatLieu> getChatLieuById(int id) {
        return chatLieuRepository.findById(id);
    }

    @Override
    public void addChatLieu(ChatLieu chatLieu) {
        chatLieuRepository.save(chatLieu);
    }

    @Override
    public void updateChatLieu(ChatLieu chatLieu) {
        chatLieuRepository.save(chatLieu);
    }

    @Override
    public void deleteChatLieu(int id) {
        sanPhamRepository.removeChatLieuReference(id);// Gá»¡ rÃ ng buá»™c khÃ³a ngoáº¡i tá»« báº£ng san_pham
        chatLieuRepository.deleteById(id);
    }
}

