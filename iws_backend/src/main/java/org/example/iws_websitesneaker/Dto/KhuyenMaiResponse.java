package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import org.example.iws_websitesneaker.entity.KhuyenMai;

import java.util.Date;

@Data
public class KhuyenMaiResponse {
    private Integer id;
    private String maKhuyenMai;
    private String tenKhuyenMai;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private Integer trangThai;
    private Float giaTri;
    private Date ngayTao;
    private Date ngayCapNhat;
    private Integer soLuongSanPham;
    public KhuyenMaiResponse() {}

    public KhuyenMaiResponse(org.example.iws_websitesneaker.entity.KhuyenMai khuyenMai) {
        this.id = khuyenMai.getId();
        this.maKhuyenMai = khuyenMai.getMaKhuyenMai();
        this.tenKhuyenMai = khuyenMai.getTenKhuyenMai();
        this.ngayBatDau = khuyenMai.getNgayBatDau();
        this.ngayKetThuc = khuyenMai.getNgayKetThuc();
        this.trangThai = khuyenMai.getTrangThai();
        this.giaTri = khuyenMai.getGiaTri();
        this.ngayTao = khuyenMai.getNgayTao();
        this.ngayCapNhat = khuyenMai.getNgayCapNhat();
        this.soLuongSanPham = 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public String getTenKhuyenMai() {
        return tenKhuyenMai;
    }

    public void setTenKhuyenMai(String tenKhuyenMai) {
        this.tenKhuyenMai = tenKhuyenMai;
    }

    public Date getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(Date ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public Date getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(Date ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public Integer getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Integer trangThai) {
        this.trangThai = trangThai;
    }

    public Float getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(Float giaTri) {
        this.giaTri = giaTri;
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

    public Integer getSoLuongSanPham() {
        return soLuongSanPham;
    }

    public void setSoLuongSanPham(Integer soLuongSanPham) {
        this.soLuongSanPham = soLuongSanPham;
    }
}

