package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaChiResponse {
    private Integer id;
    private String maTinh;
    private String maHuyen;
    private String maPhuong;
    private String tenTinh;
    private String tenHuyen;
    private String tenPhuong;
    private String hoTen;
    private String diaChiChiTiet;
    private Integer trangThai;
    private Date ngayTao;
    private Date ngayCapNhat;

    // ThÃ´ng tin bá»• sung
    private String diaChiDayDu;
}

