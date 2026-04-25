package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HinhAnhResponse {
    private Integer id;
    private String maHinhAnh;
    private String tenHinhAnh;
    private String duongDan;
    private String urlHinhAnh;
    private Integer trangThai;
    private Boolean laHinhChinh;
}

