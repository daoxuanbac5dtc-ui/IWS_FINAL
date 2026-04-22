package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class HoaDonRequest {
    private String tenKhach;
    private String sdt;
    private String email;
    private String diaChi;
    private String phuongThucThanhToan;
    private Double tienNhan;
    private String ghiChu;
    private String loaiHoaDon;
    private String trangThai;
    private Integer khachHangId;
    private Integer nhanVienId;
    private Integer voucherId;
    private Integer diemSuDung;
    private Double tongTien;
    private List<HoaDonChiTietRequest> chiTiets;
}
