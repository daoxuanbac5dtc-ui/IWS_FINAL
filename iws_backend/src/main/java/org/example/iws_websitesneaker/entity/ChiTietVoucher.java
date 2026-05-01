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

    // Thông tin voucher tại thời điểm áp dụng
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

    // Thông tin tính toán
    @Column(name = "gia_tri_don_hang", precision = 18, scale = 2)
    private BigDecimal giaTriDonHang;

    @Column(name = "so_tien_giam", precision = 18, scale = 2)
    private BigDecimal soTienGiam;

    @Column(name = "thanh_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal thanhTien;

    // Thông tin thời gian
    @Column(name = "ngay_ap_dung", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayApDung;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    // Constructor để tạo chi tiết voucher từ voucher gốc
    public ChiTietVoucher(String maChiTietVoucher, HoaDon hoaDon, Voucher voucher,
                          BigDecimal giaTriDonHang, BigDecimal soTienGiam, BigDecimal thanhTien) {
        this.maChiTietVoucher = maChiTietVoucher;
        this.hoaDon = hoaDon;
        this.voucher = voucher;

        // Sao chép thông tin voucher tại thời điểm áp dụng
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
