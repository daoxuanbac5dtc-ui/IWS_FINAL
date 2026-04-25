package org.example.iws_websitesneaker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chi_tiet_voucher")
public class ChiTietVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_chi_tiet_voucher", nullable = false, length = 25)
    private String maChiTietVoucher;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don", nullable = true)
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "id_voucher", nullable = true)
    private Voucher voucher;

    // ThÃ´ng tin voucher táº¡i thá»i Ä‘iá»ƒm Ã¡p dá»¥ng
    @Column(name = "ma_voucher", length = 25)
    private String maVoucher;

    @Column(name = "ten_voucher", length = 225)
    private String tenVoucher;

    @Column(name = "loai_giam_gia", length = 25)
    private String loaiGiamGia;

    @Column(name = "gia_tri_giam")
    private Double giaTriGiam;

    @Column(name = "gia_tri_giam_toi_da")
    private Double giaTriGiamToiDa;

    @Column(name = "gia_tri_giam_toi_thieu")
    private Double giaTriGiamToiThieu;

    // ThÃ´ng tin tÃ­nh toÃ¡n
    @Column(name = "gia_tri_don_hang", precision = 18, scale = 2)
    private BigDecimal giaTriDonHang;

    @Column(name = "so_tien_giam", precision = 18, scale = 2)
    private BigDecimal soTienGiam;

    @Column(name = "thanh_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal thanhTien;

    // ThÃ´ng tin thá»i gian
    @Column(name = "ngay_ap_dung", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayApDung;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    // Constructor Ä‘á»ƒ táº¡o chi tiáº¿t voucher tá»« voucher gá»‘c
    public ChiTietVoucher(String maChiTietVoucher, HoaDon hoaDon, Voucher voucher,
                          BigDecimal giaTriDonHang, BigDecimal soTienGiam, BigDecimal thanhTien) {
        this.maChiTietVoucher = maChiTietVoucher;
        this.hoaDon = hoaDon;
        this.voucher = voucher;

        // Sao chÃ©p thÃ´ng tin voucher táº¡i thá»i Ä‘iá»ƒm Ã¡p dá»¥ng
        if (voucher != null) {
            this.maVoucher = voucher.getMaVoucher();
            this.tenVoucher = voucher.getTenVoucher();
            this.loaiGiamGia = voucher.getLoaiGiamGia();
            this.giaTriGiam = voucher.getGiaTriGiam();
            this.giaTriGiamToiDa = voucher.getGiaTriGiamToiDa();
            this.giaTriGiamToiThieu = voucher.getGiaTriGiamToiThieu();
        }

        this.giaTriDonHang = giaTriDonHang;
        this.soTienGiam = soTienGiam;
        this.thanhTien = thanhTien;
        this.ngayApDung = new Date();
        this.ngayTao = new Date();
    }

    @PrePersist
    protected void onCreate() {
        ngayTao = new Date();
        if (ngayApDung == null) {
            ngayApDung = new Date();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = new Date();
    }
}
