package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApDungVoucherResponse {
    private Integer voucherId;
    private String maVoucher;
    private String tenVoucher;
    private String loaiGiamGia;
    private BigDecimal giaTriGiam;
    private BigDecimal giaTriGiamToiDa;
    private BigDecimal tongTienTruocGiam;
    private BigDecimal tongTienSauGiam;
    private BigDecimal soTienTietKiem;
    private Boolean apDungThanhCong;
    private String thongBao;
}
