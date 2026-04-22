package org.example.iws_websitesneaker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "chi_tiet_tra_hang")
public class ChiTietTraHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_chi_tiet_tra_hang", nullable = false, length = 25)
    private String maChiTietTraHang;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "trang_thai_hoa_don", nullable = false, length = 50)
    private String trangThaiHoaDon;

    @Column(name = "ngay_tao", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_tao_tra_hang", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTaoTraHang;

    @Column(name = "ngay_cap_nhat", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    // âœ… THÃŠM: TrÆ°á»ng lÃ½ do tráº£ hÃ ng
    @Column(name = "ly_do", length = 255)
    private String lyDo;

    // âœ… THÃŠM: TrÆ°á»ng Ä‘Æ°á»ng dáº«n áº£nh
    @Column(name = "duong_dan_anh", length = 255)
    private String duongDanAnh;

    @ManyToOne
    @JoinColumn(name = "id_ctsp", nullable = false)
    private ChiTietSanPham chiTietSanPham;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don", nullable = true)
    private HoaDon hoaDon;

    public ChiTietTraHang() {
    }

    public ChiTietTraHang(Integer id, String maChiTietTraHang, Integer soLuong, String trangThaiHoaDon,
                          Date ngayTao, Date ngayTaoTraHang, Date ngayCapNhat,
                          ChiTietSanPham chiTietSanPham, HoaDon hoaDon, String lyDo, String duongDanAnh) {
        this.id = id;
        this.maChiTietTraHang = maChiTietTraHang;
        this.soLuong = soLuong;
        this.trangThaiHoaDon = trangThaiHoaDon;
        this.ngayTao = ngayTao;
        this.ngayTaoTraHang = ngayTaoTraHang;
        this.ngayCapNhat = ngayCapNhat;
        this.chiTietSanPham = chiTietSanPham;
        this.hoaDon = hoaDon;
        this.lyDo = lyDo;
        this.duongDanAnh = duongDanAnh;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaChiTietTraHang() {
        return maChiTietTraHang;
    }

    public void setMaChiTietTraHang(String maChiTietTraHang) {
        this.maChiTietTraHang = maChiTietTraHang;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
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

    public Date getNgayTaoTraHang() {
        return ngayTaoTraHang;
    }

    public void setNgayTaoTraHang(Date ngayTaoTraHang) {
        this.ngayTaoTraHang = ngayTaoTraHang;
    }

    public Date getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(Date ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getDuongDanAnh() {
        return duongDanAnh;
    }

    public void setDuongDanAnh(String duongDanAnh) {
        this.duongDanAnh = duongDanAnh;
    }

    public ChiTietSanPham getChiTietSanPham() {
        return chiTietSanPham;
    }

    public void setChiTietSanPham(ChiTietSanPham chiTietSanPham) {
        this.chiTietSanPham = chiTietSanPham;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }
}
