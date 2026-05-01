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
    private Date ngayTao;           // ← THÊM field này
    private Date ngayCapNhat;       // ← THÊM field này
}

