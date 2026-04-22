package org.example.iws_websitesneaker.Dto;

import lombok.Data;

@Data
public class HoaDonChiTietRequest {
    private Integer chiTietSanPhamId;
    private Integer soLuong;
    private Double donGia;
    private Double thanhTien;
}
