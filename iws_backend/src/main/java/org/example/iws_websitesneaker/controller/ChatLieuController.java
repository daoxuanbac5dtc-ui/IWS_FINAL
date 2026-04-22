package org.example.iws_websitesneaker.controller;


import jakarta.validation.Valid;
import org.example.iws_websitesneaker.Dto.ChatLieuDto;
import org.example.iws_websitesneaker.Service.ChatLieuService;
import org.example.iws_websitesneaker.entity.ChatLieu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat-lieu")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")


public class ChatLieuController {
    @Autowired
    private ChatLieuService chatLieuService;

    //Hiá»ƒn thá»‹ táº¥t cáº£ cÃ¡c dá»¯ liá»‡u trong báº£ng cháº¥t liá»‡u
    @GetMapping
    public List<ChatLieu> getAll(){
        return chatLieuService.getAllChatLieu();
    }

    // Hien thi
    @GetMapping("/{id}")
    public ChatLieu getById(@PathVariable int id){
        return chatLieuService.getChatLieuById(id).orElse(null);
    }
    //ThÃªm cháº¥t liá»‡u theo dto
    @PostMapping
    public String addChatLieu(@Valid @RequestBody ChatLieu chatLieu){
        chatLieu.setNgayTao(new Date());
        chatLieuService.addChatLieu(chatLieu);
        return "ThÃªm cháº¥t liá»‡u thÃ nh cÃ´ng ";
    }

    //Update cháº¥t liá»‡u theo dto
    @PutMapping("/{id}")
    public String updateChatLieu(@PathVariable int id,@Valid @RequestBody  ChatLieu chatLieu){
        Optional<ChatLieu> optional = chatLieuService.getChatLieuById(id);
        if (optional.isEmpty()) {
            return "KhÃ´ng tÃ¬m tháº¥y cháº¥t liá»‡u vá»›i ID: " + id;
        }
        chatLieu.setId(id);
        chatLieu.setNgayCapNhat(new Date());
        chatLieuService.updateChatLieu(chatLieu);
        return "ÄÃ£ sá»­a thÃ nh cÃ´ng cháº¥t liá»‡u vá»›i id : " +id;
    }
    //Delete cl
    @DeleteMapping("/{id}")
    public String deleteChatLieu(@PathVariable int id ){
        Optional<ChatLieu> optional = chatLieuService.getChatLieuById(id);
        if (optional.isEmpty()) {
            return "KhÃ´ng tÃ¬m tháº¥y cháº¥t liá»‡u vá»›i ID: " + id;
        }
        chatLieuService.deleteChatLieu(id);
        return "ÄÃ£ xÃ³a cháº¥t liÃªu cháº¥t liá»‡u vá»›i id : " +id;
    }


}

