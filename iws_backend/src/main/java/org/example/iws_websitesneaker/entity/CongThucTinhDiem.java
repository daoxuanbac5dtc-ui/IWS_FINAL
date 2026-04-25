package org.example.iws_websitesneaker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "cong_thuc_tinh_diem")
public class CongThucTinhDiem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tien_tich_diem", nullable = false)
    private Double tienTichDiem;

    @Column(name = "tien_tieu_diem", nullable = false)
    private Double tienTieuDiem;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    public CongThucTinhDiem(Integer id, Double tienTichDiem, Double tienTieuDiem, Integer trangThai, Date ngayTao, Date ngayCapNhat) {
        this.id = id;
        this.tienTichDiem = tienTichDiem;
        this.tienTieuDiem = tienTieuDiem;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.ngayCapNhat = ngayCapNhat;
    }

    public CongThucTinhDiem() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getTienTichDiem() {
        return tienTichDiem;
    }

    public void setTienTichDiem(Double tienTichDiem) {
        this.tienTichDiem = tienTichDiem;
    }

    public Double getTienTieuDiem() {
        return tienTieuDiem;
    }

    public void setTienTieuDiem(Double tienTieuDiem) {
        this.tienTieuDiem = tienTieuDiem;
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

