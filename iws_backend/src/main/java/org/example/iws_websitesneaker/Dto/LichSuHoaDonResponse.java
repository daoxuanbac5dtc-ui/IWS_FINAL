package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import lombok.Builder;
import java.util.Date;

@Data
@Builder
public class LichSuHoaDonResponse {
    private Integer id;
    private String moTaHanhDong;
    private String trangThaiHoaDon;
    private Date ngayTao;
    private NhanVienResponse nhanVien;
}
