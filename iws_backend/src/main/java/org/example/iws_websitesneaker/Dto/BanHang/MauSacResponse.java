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
    private String tenMau;          // ← THÊM field này (alias cho frontend)
    private String maMau;           // ← THÊM field này (hex color)
    private Integer trangThai;
    private Date ngayTao;           // ← THÊM field này
    private Date ngayCapNhat;
}

