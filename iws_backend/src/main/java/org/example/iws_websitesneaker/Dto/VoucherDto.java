package org.example.iws_websitesneaker.Dto;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDto {
    private Integer id;
    private String maVoucher;
    private String tenVoucher;
    private String duongDanHinhAnh;
    private String loaiGiamGia;
    private Integer trangThai;
    private Double giaTriGiamToiDa;
    private Double giaTriGiamToiThieu;
    private int soLuong;
    private Date ngayBatDau;
    private Date ngayKetThuc;
}

