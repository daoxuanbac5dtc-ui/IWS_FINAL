package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BanHangFilterRequest {
    private Date tuNgay;
    private Date denNgay;
    private Integer nhanVienId;
    private Integer khachHangId;
    private String trangThaiHoaDon;
    private String loaiHoaDon;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
}
