package org.example.iws_websitesneaker.Dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KhuyenMaiChiTietDto {
    private Integer id ;
    private String tenSanPham;
    private String tenMauSac;
    private String tenKichCo ;
    private String tenChatLieu;
    private String tenDeGiay;
    private String tenDanhMuc;
    private String tenThuongHieu;
    private String tenKhuyenMai;
    private Integer trangThai;
}

