package org.example.iws_websitesneaker.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "chi_tiet_san_pham")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ChiTietSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_chi_tiet")
    private String maChiTiet;

    @Column(name = "ma_QR")
    private String maQR;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "gia_goc")
    private Double giaGoc;

    @Column(name = "gia_ban")
    private Double giaBan;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mau_sac")
    private MauSac mauSac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kich_co")
    private KichCo kichCo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hinh_anh")
    @JsonInclude(JsonInclude.Include.NON_NULL) // Chỉ serialize khi không null
    private HinhAnh hinhAnh;

    public ChiTietSanPham() {}

    public ChiTietSanPham(Integer id, String maChiTiet, String maQR, Integer soLuong,
                          Double giaGoc, Double giaBan, Integer trangThai,
                          Date ngayTao, Date ngayCapNhat, MauSac mauSac,
                          KichCo kichCo, SanPham sanPham , HinhAnh hinhAnh) {
        this.id = id;
        this.maChiTiet = maChiTiet;
        this.maQR = maQR;
        this.soLuong = soLuong;
        this.giaGoc = giaGoc;
        this.giaBan = giaBan;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.ngayCapNhat = ngayCapNhat;
        this.mauSac = mauSac;
        this.kichCo = kichCo;
        this.sanPham = sanPham;
        this.hinhAnh = hinhAnh;
    }
}
