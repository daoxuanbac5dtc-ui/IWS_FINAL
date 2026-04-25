package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamInfoResponse {
    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private Integer soLuong;
    private Integer trangThai;
}

