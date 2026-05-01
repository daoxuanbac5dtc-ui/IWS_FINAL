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

    private Integer hoaDonId;   // chỉ lưu id, không trả về full object
    private Integer voucherId;  // chỉ lưu id, không trả về full object

    // Thông tin voucher tại thời điểm áp dụng
    private String maVoucher;
    private String tenVoucher;
    private String loaiGiamGia;
    private Double giaTriGiam;
    private Double giaTriGiamToiDa;
    private Double giaTriGiamToiThieu;

    // Thông tin tính toán
    private BigDecimal giaTriDonHang;
    private BigDecimal soTienGiam;
    private BigDecimal thanhTien;

    // Thông tin thời gian
    private Date ngayApDung;
    private Date ngayTao;
    private Date ngayCapNhat;
}

