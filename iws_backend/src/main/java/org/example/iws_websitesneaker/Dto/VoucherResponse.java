package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import lombok.Builder;
import java.util.Date;

@Data
@Builder
public class VoucherResponse {
    private Integer id;
    private String maVoucher;
    private String tenVoucher;
    private String loaiGiamGia; // PHAN_TRAM, TIEN_MAT
    private Double giaTriGiam;
    private Double giaTriGiamToiDa;
    private Double giaTriDonHangToiThieu;
    private Integer soLuong;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private Boolean khaDung;
    private String moTa;
}
