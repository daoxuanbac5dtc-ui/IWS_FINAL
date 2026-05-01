package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DanhMucResponse {
    private Integer id;
    private String maDanhMuc;
    private String tenDanhMuc;
    private Integer trangThai;
    private Integer soLuongSanPham;
    private Date ngayTao;           // ← THÊM field này
    private Date ngayCapNhat;
}
