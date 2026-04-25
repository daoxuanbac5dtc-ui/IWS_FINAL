package org.example.iws_websitesneaker.Dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopSanPhamDTO {
    private Integer idSanPham;
    private String maSanPham;
    private String tenSanPham;
    private Long tongSoLuongBan;
    private Double tongDoanhThu;
}
