package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MauSacResponse {
    private Integer id;
    private String maMauSac;
    private String tenMauSac;
    private String tenMau;          // â† THÃŠM field nÃ y (alias cho frontend)
    private String maMau;           // â† THÃŠM field nÃ y (hex color)
    private Integer trangThai;
    private Date ngayTao;           // â† THÃŠM field nÃ y
    private Date ngayCapNhat;
}

