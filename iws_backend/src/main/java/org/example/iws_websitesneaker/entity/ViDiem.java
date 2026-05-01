package org.example.iws_websitesneaker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "vi_diem")
public class ViDiem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tong_diem", nullable = false)
    private Double tongDiem = 0.0;

    @Column(name = "so_diem_da_dung", nullable = false)
    private Double soDiemDaDung = 0.0;

    @Column(name = "so_diem_da_cong", nullable = false)
    private Double soDiemDaCong = 0.0;

    @Column(name = "gia_tri_diem", nullable = false)
    private Double giaTriDiem = 1000.0; // 1 điểm = 1000 VND

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    // Constructors
    public ViDiem() {
        this.tongDiem = 0.0;
        this.soDiemDaDung = 0.0;
        this.soDiemDaCong = 0.0;
        this.giaTriDiem = 1000.0;
    }

    public ViDiem(Integer id, Double tongDiem, Double soDiemDaDung, Double soDiemDaCong, Double giaTriDiem, Date ngayTao, Date ngayCapNhat) {
        this.id = id;
        this.tongDiem = tongDiem;
        this.soDiemDaDung = soDiemDaDung;
        this.soDiemDaCong = soDiemDaCong;
        this.giaTriDiem = giaTriDiem;
        this.ngayTao = ngayTao;
        this.ngayCapNhat = ngayCapNhat;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getTongDiem() {
        return tongDiem;
    }

    public void setTongDiem(Double tongDiem) {
        this.tongDiem = tongDiem;
    }

    public Double getSoDiemDaDung() {
        return soDiemDaDung;
    }

    public void setSoDiemDaDung(Double soDiemDaDung) {
        this.soDiemDaDung = soDiemDaDung;
    }

    public Double getSoDiemDaCong() {
        return soDiemDaCong;
    }

    public void setSoDiemDaCong(Double soDiemDaCong) {
        this.soDiemDaCong = soDiemDaCong;
    }

    public Double getGiaTriDiem() {
        return giaTriDiem;
    }

    public void setGiaTriDiem(Double giaTriDiem) {
        this.giaTriDiem = giaTriDiem;
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

    // Method tính điểm hiện tại có thể sử dụng
    public Double getDiemHienTai() {
        if (this.tongDiem == null) this.tongDiem = 0.0;
        if (this.soDiemDaDung == null) this.soDiemDaDung = 0.0;
        return this.tongDiem - this.soDiemDaDung;
    }

    // Method tính giá trị tiền từ điểm hiện tại
    public Double getGiaTriTienHienTai() {
        return getDiemHienTai() * (this.giaTriDiem != null ? this.giaTriDiem : 1000.0);
    }
}
