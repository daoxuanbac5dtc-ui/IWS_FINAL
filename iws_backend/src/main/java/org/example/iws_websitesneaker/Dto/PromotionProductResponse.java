package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import org.example.iws_websitesneaker.entity.KhuyenMai;

@Data
public class PromotionProductResponse {
    private Integer id;
    private ChiTietSanPham chiTietSanPham;
    private KhuyenMai khuyenMai;
    private Double giaGoc;           // Giá gốc
    private Double giaBanSauKhuyenMai; // Giá bán sau khuyến mãi
    private Double soTienTietKiem;   // Số tiền tiết kiệm
    private Float phanTramGiam;      // Phần trăm giảm

    public PromotionProductResponse(ChiTietSanPham chiTietSanPham, KhuyenMai khuyenMai) {
        this.chiTietSanPham = chiTietSanPham;
        this.khuyenMai = khuyenMai;
        this.giaGoc = chiTietSanPham.getGiaGoc();
        this.giaBanSauKhuyenMai = chiTietSanPham.getGiaBan();

        // Tính toán tiết kiệm
        if (giaGoc != null && giaBanSauKhuyenMai != null) {
            this.soTienTietKiem = giaGoc - giaBanSauKhuyenMai;
        } else {
            this.soTienTietKiem = 0.0;
        }

        this.phanTramGiam = khuyenMai.getGiaTri() * 100;
    }

    public PromotionProductResponse() {
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ChiTietSanPham getChiTietSanPham() {
        return chiTietSanPham;
    }

    public void setChiTietSanPham(ChiTietSanPham chiTietSanPham) {
        this.chiTietSanPham = chiTietSanPham;
    }

    public KhuyenMai getKhuyenMai() {
        return khuyenMai;
    }

    public void setKhuyenMai(KhuyenMai khuyenMai) {
        this.khuyenMai = khuyenMai;
    }

    public Double getGiaGoc() {
        return giaGoc;
    }

    public void setGiaGoc(Double giaGoc) {
        this.giaGoc = giaGoc;
    }

    public Double getGiaBanSauKhuyenMai() {
        return giaBanSauKhuyenMai;
    }

    public void setGiaBanSauKhuyenMai(Double giaBanSauKhuyenMai) {
        this.giaBanSauKhuyenMai = giaBanSauKhuyenMai;
    }

    public Double getSoTienTietKiem() {
        return soTienTietKiem;
    }

    public void setSoTienTietKiem(Double soTienTietKiem) {
        this.soTienTietKiem = soTienTietKiem;
    }

    public Float getPhanTramGiam() {
        return phanTramGiam;
    }

    public void setPhanTramGiam(Float phanTramGiam) {
        this.phanTramGiam = phanTramGiam;
    }
}
