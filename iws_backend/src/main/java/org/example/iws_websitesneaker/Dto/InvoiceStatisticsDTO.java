package org.example.iws_websitesneaker.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceStatisticsDTO {
    private Long tongDonHang;
    private Long donHangHoanThanh;
    private Long donHangHuy;
    private Long donHangDangXuLy;
    private BigDecimal tongDoanhThu;
    private BigDecimal doanhThuThangNay;
    private BigDecimal doanhThuHomNay;

    // Constructors, getters và setters
}
