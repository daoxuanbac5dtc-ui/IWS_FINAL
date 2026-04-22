package org.example.iws_websitesneaker.Dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietVoucherDTO {
    private Integer id;

    private String maChiTietVoucher;

    private Integer hoaDonId;   // chá»‰ lÆ°u id, khÃ´ng tráº£ vá» full object
    private Integer voucherId;  // chá»‰ lÆ°u id, khÃ´ng tráº£ vá» full object

    // ThÃ´ng tin voucher táº¡i thá»i Ä‘iá»ƒm Ã¡p dá»¥ng
    private String maVoucher;
    private String tenVoucher;
    private String loaiGiamGia;
    private Double giaTriGiam;
    private Double giaTriGiamToiDa;
    private Double giaTriGiamToiThieu;

    // ThÃ´ng tin tÃ­nh toÃ¡n
    private BigDecimal giaTriDonHang;
    private BigDecimal soTienGiam;
    private BigDecimal thanhTien;

    // ThÃ´ng tin thá»i gian
    private Date ngayApDung;
    private Date ngayTao;
    private Date ngayCapNhat;
}

