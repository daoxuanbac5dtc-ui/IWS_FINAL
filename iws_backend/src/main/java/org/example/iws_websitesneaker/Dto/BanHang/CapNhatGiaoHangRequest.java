package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapNhatGiaoHangRequest {
    private String tenNguoiNhan;
    private String sdt;
    private String email;
    private String diaChi;
    private BigDecimal phiVanChuyen;
    private String ghiChu;
}

