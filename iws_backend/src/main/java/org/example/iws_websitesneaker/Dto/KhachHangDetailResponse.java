package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class KhachHangDetailResponse {
    private Integer id;
    private String maKhachHang;
    private String hoTen;
    private String sdt;
    private String email;
    private Integer trangThai;
    private Double diemTichLuy;
    private Double soLuongDonHang;
    private Double tongChiTieu;
    private String capBacKhachHang;
    private DiaChiResponse diaChi;
    private ViDiemResponse viDiem;
}
