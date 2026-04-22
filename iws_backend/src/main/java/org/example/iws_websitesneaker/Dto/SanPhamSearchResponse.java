package org.example.iws_websitesneaker.Dto;

import lombok.Data;

@Data
public class SanPhamSearchResponse {
    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private String maChiTiet;
    private String maQR;
    private String mauSac;
    private String kichCo;
    private String thuongHieu;
    private String danhMuc;
    private Double giaBan;
    private Integer soLuong;
    private String hinhAnh;
}
