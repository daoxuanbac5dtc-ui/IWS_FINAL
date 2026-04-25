package org.example.iws_websitesneaker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "hinh_anh")
public class HinhAnh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_hinh_anh", nullable = false)
    private String maHinhAnh;

    @Column(name = "ten_hinh_anh", nullable = false)
    private String tenHinhAnh;

    @Column(name = "duong_dan", nullable = false)
    private String duongDan;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

//    @ManyToOne
//    @JoinColumn(name = "id_ctsp", nullable = true)
//    private ChiTietSanPham chiTietSanPham;

    public HinhAnh(Integer id, String maHinhAnh, String tenHinhAnh, String duongDan,  Integer trangThai, Date ngayTao, Date ngayCapNhat) {
        this.id = id;
        this.maHinhAnh = maHinhAnh;
        this.tenHinhAnh = tenHinhAnh;
        this.duongDan = duongDan;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.ngayCapNhat = ngayCapNhat;
    }

    public HinhAnh() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaHinhAnh() {
        return maHinhAnh;
    }

    public void setMaHinhAnh(String maHinhAnh) {
        this.maHinhAnh = maHinhAnh;
    }

    public String getTenHinhAnh() {
        return tenHinhAnh;
    }

    public void setTenHinhAnh(String tenHinhAnh) {
        this.tenHinhAnh = tenHinhAnh;
    }

    public String getDuongDan() {
        return duongDan;
    }
    public void setDuongDan(String duongDan) {
        this.duongDan = duongDan;
    }

    public Integer getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Integer trangThai) {
        this.trangThai = trangThai;
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

