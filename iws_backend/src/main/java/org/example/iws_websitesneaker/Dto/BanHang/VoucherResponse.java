package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherResponse {
    private Integer id;
    private String maVoucher;
    private String tenVoucher;
    // Không có field mô tả trong database
    private String loaiGiamGia;
    private Integer trangThai;
    private String duongDanHinhAnh;

    private BigDecimal giaTriGiamToiDa;
    private BigDecimal giaTriGiam;
    private BigDecimal giaTriGiamToiThieu;

    // ✅ THÊM: Field frontend expect
    private BigDecimal giaTriDonHangToiThieu; // Map từ giaTriGiamToiThieu

    private Integer soLuong;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private Date ngayTao;
    private Date ngayCapNhat;

    // Status fields
    private Boolean daHetHan;
    private Boolean daHetSoLuong;
    private Integer soLuongConLai;
    private Boolean khaDung;
}
