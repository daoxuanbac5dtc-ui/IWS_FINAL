package org.example.iws_websitesneaker.Dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HoaDonChiTietDTO {
    private Integer id;
    private Integer hoaDonId;
    private Integer chiTietSanPhamId;

    // ThÃ´ng tin sáº£n pháº©m chi tiáº¿t
    private String tenSanPham;
    private String maSanPham;
    private String maChiTiet;
    private String mauSac;
    private String kichThuoc;
    private String thuongHieu;
    private String danhMuc;
    private String hinhAnh;

    // ThÃ´ng tin giÃ¡ cáº£
    private Double giaGoc;           // GiÃ¡ gá»‘c
    private Double giaBan;           // GiÃ¡ bÃ¡n (cÃ³ thá»ƒ Ä‘Ã£ giáº£m)
    private BigDecimal giaKhuyenMai;     // GiÃ¡ sau khuyáº¿n mÃ£i
    private BigDecimal tienTietKiem;     // Sá»‘ tiá»n tiáº¿t kiá»‡m Ä‘Æ°á»£c
    private Float phanTramGiam;          // % giáº£m giÃ¡

    // Sá»‘ lÆ°á»£ng vÃ  tá»•ng
    private Integer soLuong;
    private BigDecimal thanhTien;        // Tá»•ng tiá»n = giaBan * soLuong

    private String trangThai;

    public HoaDonChiTietDTO() {}

    // TÃ­nh toÃ¡n tá»± Ä‘á»™ng
    public void calculateValues() {
        if (giaGoc != null && giaBan != null && soLuong != null) {
            // TÃ­nh tiá»n tiáº¿t kiá»‡m
            this.tienTietKiem = BigDecimal.valueOf(giaGoc - giaBan).multiply(BigDecimal.valueOf(soLuong));
            // TÃ­nh pháº§n trÄƒm giáº£m giÃ¡
            if (giaGoc > 0) {
                this.phanTramGiam = (float)(((giaGoc - giaBan) / giaGoc) * 100);
            }
            // TÃ­nh thÃ nh tiá»n
            this.thanhTien = BigDecimal.valueOf(giaBan).multiply(BigDecimal.valueOf(soLuong));
        }
    }

    // Getters vÃ  Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getHoaDonId() { return hoaDonId; }
    public void setHoaDonId(Integer hoaDonId) { this.hoaDonId = hoaDonId; }

    public Integer getChiTietSanPhamId() { return chiTietSanPhamId; }
    public void setChiTietSanPhamId(Integer chiTietSanPhamId) { this.chiTietSanPhamId = chiTietSanPhamId; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }

    public String getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(String maChiTiet) { this.maChiTiet = maChiTiet; }

    public String getMauSac() { return mauSac; }
    public void setMauSac(String mauSac) { this.mauSac = mauSac; }

    public String getKichThuoc() { return kichThuoc; }
    public void setKichThuoc(String kichThuoc) { this.kichThuoc = kichThuoc; }

    public String getThuongHieu() { return thuongHieu; }
    public void setThuongHieu(String thuongHieu) { this.thuongHieu = thuongHieu; }

    public String getDanhMuc() { return danhMuc; }
    public void setDanhMuc(String danhMuc) { this.danhMuc = danhMuc; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public Double getGiaGoc() { return giaGoc; }
    public void setGiaGoc(Double giaGoc) { this.giaGoc = giaGoc; }

    public Double getGiaBan() { return giaBan; }
    public void setGiaBan(Double giaBan) { this.giaBan = giaBan; }

    public BigDecimal getGiaKhuyenMai() { return giaKhuyenMai; }
    public void setGiaKhuyenMai(BigDecimal giaKhuyenMai) { this.giaKhuyenMai = giaKhuyenMai; }

    public BigDecimal getTienTietKiem() { return tienTietKiem; }
    public void setTienTietKiem(BigDecimal tienTietKiem) { this.tienTietKiem = tienTietKiem; }

    public Float getPhanTramGiam() { return phanTramGiam; }
    public void setPhanTramGiam(Float phanTramGiam) { this.phanTramGiam = phanTramGiam; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public BigDecimal getThanhTien() { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
