package org.example.iws_websitesneaker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "lich_su_diem")
public class LichSuDiem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "id_cong_thuc_tinh_diem", nullable = false)
    private CongThucTinhDiem congThucTinhDiem;

    @ManyToOne
    @JoinColumn(name = "id_vi_diem", nullable = false)
    private ViDiem viDiem;

    @Column(name = "loai_diem")
    private Double loaiDiem;

    @Column(name = "gia_tri_diem")
    private Double giaTriDiem;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    public LichSuDiem(Integer id, HoaDon hoaDon, CongThucTinhDiem congThucTinhDiem, ViDiem viDiem, Double loaiDiem, Double giaTriDiem, Date ngayTao, Date ngayCapNhat) {
        this.id = id;
        this.hoaDon = hoaDon;
        this.congThucTinhDiem = congThucTinhDiem;
        this.viDiem = viDiem;
        this.loaiDiem = loaiDiem;
        this.giaTriDiem = giaTriDiem;
        this.ngayTao = ngayTao;
        this.ngayCapNhat = ngayCapNhat;
    }

    public LichSuDiem() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public CongThucTinhDiem getCongThucTinhDiem() {
        return congThucTinhDiem;
    }

    public void setCongThucTinhDiem(CongThucTinhDiem congThucTinhDiem) {
        this.congThucTinhDiem = congThucTinhDiem;
    }

    public ViDiem getViDiem() {
        return viDiem;
    }

    public void setViDiem(ViDiem viDiem) {
        this.viDiem = viDiem;
    }

    public Double getLoaiDiem() {
        return loaiDiem;
    }

    public void setLoaiDiem(Double loaiDiem) {
        this.loaiDiem = loaiDiem;
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
}


