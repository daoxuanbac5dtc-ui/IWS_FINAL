package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangResponse {
    private Integer id;
    private String maKhachHang;
    private String hoTen;
    private String sdt;
    private String email;
    private Integer trangThai;
    private Double diemTichLuy;
    private Date ngayTao;

    // Địa chỉ
    private String tenTinh;
    private String tenHuyen;
    private String tenPhuong;
    private String diaChiChiTiet;
}

