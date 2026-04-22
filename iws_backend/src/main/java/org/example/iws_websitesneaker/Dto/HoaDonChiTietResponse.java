package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class HoaDonChiTietResponse {
    private Integer id;
    private Integer chiTietSanPhamId;
    private String tenSanPham;
    private String mauSac;
    private String kichCo;
    private String thuongHieu;
    private Double donGia;
    private Integer soLuong;
    private Double thanhTien;
    private Integer soLuongTon;
    private String hinhAnh;
    private String maChiTiet;
}
