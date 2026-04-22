package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThuongHieuResponse {
    private Integer id;
    private String maThuongHieu;
    private String tenThuongHieu;
    private Integer trangThai;
    private Integer soLuongSanPham;
    private Date ngayTao;           // â† THÃŠM field nÃ y
    private Date ngayCapNhat;
}

