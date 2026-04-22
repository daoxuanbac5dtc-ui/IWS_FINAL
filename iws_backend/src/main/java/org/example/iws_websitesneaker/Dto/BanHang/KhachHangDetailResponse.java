package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangDetailResponse {
    private Integer id;
    private String maKhachHang;
    private String hoTen;
    private String email;
    private String sdt;
    private Integer trangThai;
    private Date ngayTao;
    private Date ngayCapNhat;

    // ThÃ´ng tin vÃ­ Ä‘iá»ƒm
    private ViDiemResponse viDiem;
    private Double diemTichLuy;

    // ThÃ´ng tin Ä‘á»‹a chá»‰
    private DiaChiResponse diaChi;

    // Thá»‘ng kÃª khÃ¡ch hÃ ng
    private Double soLuongDonHang;
    private Double tongChiTieu;
    private String capBacKhachHang;
}
