package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KichCoResponse {
    private Integer id;
    private String maKichCo;
    private String tenKichCo;
    private Integer trangThai;
    private Integer thuTu;          // â† THÃŠM field nÃ y (thá»© tá»± sáº¯p xáº¿p)
    private Date ngayTao;           // â† THÃŠM field nÃ y
    private Date ngayCapNhat;
}
