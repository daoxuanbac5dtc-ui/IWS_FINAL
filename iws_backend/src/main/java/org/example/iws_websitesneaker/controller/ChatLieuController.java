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

    //Hiển thị tất cả các dữ liệu trong bảng chất liệu
    @GetMapping
    public List<ChatLieu> getAll(){
        return chatLieuService.getAllChatLieu();
    }

    // Hien thi
    @GetMapping("/{id}")
    public ChatLieu getById(@PathVariable int id){
        return chatLieuService.getChatLieuById(id).orElse(null);
    }
    //Thêm chất liệu theo dto
    @PostMapping
    public String addChatLieu(@Valid @RequestBody ChatLieu chatLieu){
        chatLieu.setNgayTao(new Date());
        chatLieuService.addChatLieu(chatLieu);
        return "Thêm chất liệu thành công ";
    }

    //Update chất liệu theo dto
    @PutMapping("/{id}")
    public String updateChatLieu(@PathVariable int id,@Valid @RequestBody  ChatLieu chatLieu){
        Optional<ChatLieu> optional = chatLieuService.getChatLieuById(id);
        if (optional.isEmpty()) {
            return "Không tìm thấy chất liệu với ID: " + id;
        }
        chatLieu.setId(id);
        chatLieu.setNgayCapNhat(new Date());
        chatLieuService.updateChatLieu(chatLieu);
        return "Đã sửa thành công chất liệu với id : " +id;
    }
    //Delete cl
    @DeleteMapping("/{id}")
    public String deleteChatLieu(@PathVariable int id ){
        Optional<ChatLieu> optional = chatLieuService.getChatLieuById(id);
        if (optional.isEmpty()) {
            return "Không tìm thấy chất liệu với ID: " + id;
        }
        chatLieuService.deleteChatLieu(id);
        return "Đã xóa chất liêu chất liệu với id : " +id;
    }


}

