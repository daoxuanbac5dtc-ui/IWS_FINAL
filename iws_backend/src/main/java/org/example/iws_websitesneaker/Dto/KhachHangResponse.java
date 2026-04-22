package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import lombok.Builder;
import java.util.Date;

@Data
@Builder
public class KhachHangResponse {
    private Integer id;
    private String maKhachHang;
    private String hoTen;
    private String sdt;
    private String email;
    private Integer trangThai;
    private Double diemTichLuy;
    private Date ngayTao;
    private String tenTinh;
    private String tenHuyen;
    private String tenPhuong;
    private String diaChiChiTiet;
}

