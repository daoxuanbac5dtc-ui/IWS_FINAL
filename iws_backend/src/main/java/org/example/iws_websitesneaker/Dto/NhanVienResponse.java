package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class NhanVienResponse {
    private Integer id;
    private String maNhanVien;
    private String hoTen;
    private String email;
    private String sdt;
    private Integer trangThai;
    private String vaiTro;
}
