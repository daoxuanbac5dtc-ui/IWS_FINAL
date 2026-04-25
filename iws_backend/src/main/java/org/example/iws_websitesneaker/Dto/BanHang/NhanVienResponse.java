package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienResponse {
    private Integer id;
    private String maNhanVien;
    private String hoTen;
    private String email;
    private String sdt;
    private Integer trangThai;
    private String vaiTro;
    private Date ngayTao;
    private Date ngayCapNhat;
}
