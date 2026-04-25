package org.example.iws_websitesneaker.Dto;

import java.math.BigDecimal;
import java.util.Date;

public class ChiTietTraHangDTO {
    private Integer id;
    private String maChiTietTraHang;
    private Integer soLuong;
    private String trangThaiHoaDon;
    private Date ngayTao;
    private Date ngayTaoTraHang;
    private Date ngayCapNhat;

    // âœ… THÃŠM: ThÃ´ng tin lÃ½ do vÃ  áº£nh
    private String lyDo;
    private String duongDanAnh;

    // ThÃ´ng tin hÃ³a Ä‘Æ¡n
    private Integer hoaDonId;
    private String maHoaDon;

    // ThÃ´ng tin chi tiáº¿t sáº£n pháº©m
    private Integer chiTietSanPhamId;
    private String maChiTiet;
    private String tenSanPham;
    private String maSanPham;
    private String mauSac;
    private String kichThuoc;
    private String thuongHieu;
    private String danhMuc;
    private String hinhAnh;
    private Double giaGoc;
    private Double giaBan;

    // ThÃ´ng tin tÃ­nh toÃ¡n
    private BigDecimal thanhTien;
    private BigDecimal tienHoan;

    public ChiTietTraHangDTO() {}

    public ChiTietTraHangDTO(Integer id, String maChiTietTraHang, Integer soLuong, String trangThaiHoaDon,
                             Date ngayTao, Date ngayTaoTraHang, Date ngayCapNhat, String lyDo, String duongDanAnh) {
        this.id = id;
        this.maChiTietTraHang = maChiTietTraHang;
        this.soLuong = soLuong;
        this.trangThaiHoaDon = trangThaiHoaDon;
        this.ngayTao = ngayTao;
        this.ngayTaoTraHang = ngayTaoTraHang;
        this.ngayCapNhat = ngayCapNhat;
        this.lyDo = lyDo;
        this.duongDanAnh = duongDanAnh;
    }

    // Method Ä‘á»ƒ tÃ­nh toÃ¡n cÃ¡c giÃ¡ trá»‹
    public void calculateValues() {
        if (giaBan != null && soLuong != null) {
            this.thanhTien = BigDecimal.valueOf(giaBan * soLuong);
            this.tienHoan = this.thanhTien; // Tiá»n hoÃ n = thÃ nh tiá»n
        } else {
            this.thanhTien = BigDecimal.ZERO;
            this.tienHoan = BigDecimal.ZERO;
        }
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

    // âœ… THÃŠM: Getters vÃ  Setters cho lÃ½ do vÃ  áº£nh
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

    public Integer getHoaDonId() {
        return hoaDonId;
    }

    public void setHoaDonId(Integer hoaDonId) {
        this.hoaDonId = hoaDonId;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public Integer getChiTietSanPhamId() {
        return chiTietSanPhamId;
    }

    public void setChiTietSanPhamId(Integer chiTietSanPhamId) {
        this.chiTietSanPhamId = chiTietSanPhamId;
    }

    public String getMaChiTiet() {
        return maChiTiet;
    }

    public void setMaChiTiet(String maChiTiet) {
        this.maChiTiet = maChiTiet;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public String getMauSac() {
        return mauSac;
    }

    public void setMauSac(String mauSac) {
        this.mauSac = mauSac;
    }

    public String getKichThuoc() {
        return kichThuoc;
    }

    public void setKichThuoc(String kichThuoc) {
        this.kichThuoc = kichThuoc;
    }

    public String getThuongHieu() {
        return thuongHieu;
    }

    public void setThuongHieu(String thuongHieu) {
        this.thuongHieu = thuongHieu;
    }

    public String getDanhMuc() {
        return danhMuc;
    }

    public void setDanhMuc(String danhMuc) {
        this.danhMuc = danhMuc;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public Double getGiaGoc() {
        return giaGoc;
    }

    public void setGiaGoc(Double giaGoc) {
        this.giaGoc = giaGoc;
    }

    public Double getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(Double giaBan) {
        this.giaBan = giaBan;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    public BigDecimal getTienHoan() {
        return tienHoan;
    }

    public void setTienHoan(BigDecimal tienHoan) {
        this.tienHoan = tienHoan;
    }
}
