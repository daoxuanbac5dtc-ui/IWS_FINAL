package org.example.iws_websitesneaker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "voucher")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_voucher", nullable = false, length = 25, unique = true)
    private String maVoucher;

    @Column(name = "ten_voucher", nullable = false, length = 255)
    private String tenVoucher;

    @Column(name = "loai_giam_gia", nullable = false, length = 25)
    private String loaiGiamGia; // PHAN_TRAM hoáº·c TIEN_MAT

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai; // 1: Hoáº¡t Ä‘á»™ng, 0: KhÃ´ng hoáº¡t Ä‘á»™ng

    @Column(name = "duong_dan_hinh_anh", length = 500)
    private String duongDanHinhAnh;

    @Column(name = "gia_tri_giam_toi_da", nullable = false)
    private Double giaTriGiamToiDa;

    @Column(name = "gia_tri_giam", nullable = false)
    private Double giaTriGiam;

    @Column(name = "gia_tri_giam_toi_thieu", nullable = false)
    private Double giaTriGiamToiThieu; // GiÃ¡ trá»‹ Ä‘Æ¡n hÃ ng tá»‘i thiá»ƒu Ä‘á»ƒ Ã¡p dá»¥ng voucher

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "ngay_bat_dau", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayBatDau;

    @Column(name = "ngay_ket_thuc", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayKetThuc;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

//    @Column(name = "mo_ta", length = 1000)
//    private String moTa;

    // Helper methods
    public boolean isActive() {
        return trangThai != null && trangThai == 1;
    }

    public Double tinhGiaTriGiam(Double tongTien) {
        if ("PHAN_TRAM".equals(this.loaiGiamGia)) {
            Double giam = tongTien * (this.giaTriGiam / 100.0);
            return Math.min(giam, this.giaTriGiamToiDa != null ? this.giaTriGiamToiDa : giam);
        } else {
            return Math.min(this.giaTriGiam, tongTien);
        }
    }

    public boolean isValid() {
        Date now = new Date();
        return this.trangThai != null && this.trangThai == 1 &&
                this.soLuong != null && this.soLuong > 0 &&
                this.ngayBatDau != null && now.after(this.ngayBatDau) &&
                this.ngayKetThuc != null && now.before(this.ngayKetThuc);
    }
}
