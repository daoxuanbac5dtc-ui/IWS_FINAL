package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeGiayResponse {
    private Integer id;
    private String maDeGiay;
    private String tenDeGiay;
    private Integer trangThai;
    private Date ngayTao;           // â† THÃŠM field nÃ y
    private Date ngayCapNhat;
}

