package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatLieuResponse {
    private Integer id;
    private String maChatLieu;
    private String tenChatLieu;
    private Integer trangThai;
    private Date ngayTao;           // â† THÃŠM field nÃ y
    private Date ngayCapNhat;       // â† THÃŠM field nÃ y
}

