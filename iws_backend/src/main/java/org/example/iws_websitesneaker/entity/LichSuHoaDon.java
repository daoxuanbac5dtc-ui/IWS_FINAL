package org.example.iws_websitesneaker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "lich_su_hoa_don")
public class LichSuHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien", nullable = false)
    private NhanVien nhanVien;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon hoaDon;

    @Column(name = "mo_ta_hanh_dong", nullable = false, length = 250)
    private String moTaHanhDong;

    @Column(name = "trang_thai_hoa_don", nullable = false, length = 50)
    private String trangThaiHoaDon;

    @Column(name = "ngay_tao", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    public LichSuHoaDon(Integer id, NhanVien nhanVien, HoaDon hoaDon, String moTaHanhDong, String trangThaiHoaDon, Date ngayTao, Date ngayCapNhat) {
        this.id = id;
        this.nhanVien = nhanVien;
        this.hoaDon = hoaDon;
        this.moTaHanhDong = moTaHanhDong;
        this.trangThaiHoaDon = trangThaiHoaDon;
        this.ngayTao = ngayTao;
        this.ngayCapNhat = ngayCapNhat;
    }

    public LichSuHoaDon() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public String getMoTaHanhDong() {
        return moTaHanhDong;
    }

    public void setMoTaHanhDong(String moTaHanhDong) {
        this.moTaHanhDong = moTaHanhDong;
    }

    public String getTrangThaiHoaDon() {
        return trangThaiHoaDon;
    }

    public void setTrangThaiHoaDon(String trangThaiHoaDon) {
        this.trangThaiHoaDon = trangThaiHoaDon;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }

    public Date getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(Date ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }
}

