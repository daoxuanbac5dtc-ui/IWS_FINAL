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

    // Thông tin ví điểm
    private ViDiemResponse viDiem;
    private Double diemTichLuy;

    // Thông tin địa chỉ
    private DiaChiResponse diaChi;

    // Thống kê khách hàng
    private Double soLuongDonHang;
    private Double tongChiTieu;
    private String capBacKhachHang;
}
