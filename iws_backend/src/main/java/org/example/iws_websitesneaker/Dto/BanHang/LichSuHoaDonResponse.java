package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LichSuHoaDonResponse {
    private Integer id;
    private String moTaHanhDong;
    private String trangThaiHoaDon;
    private Date ngayTao;
    private NhanVienResponse nhanVien;
}
