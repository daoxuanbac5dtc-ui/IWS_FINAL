package org.example.iws_websitesneaker.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaChiThanhToanDTO {
    private Integer id;
    private Integer idTaiKhoan;
    private String maTinh;
    private String maPhuong;
    private String tenTinh;
    private String tenPhuong;
    private String diaChiChiTiet;
    private Boolean isDefault;
    private Integer trangThai;
    private Date ngayTao;
    private Date ngayCapNhat;
}

