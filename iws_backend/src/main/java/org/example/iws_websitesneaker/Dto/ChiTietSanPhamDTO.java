package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ChiTietSanPhamDTO {
    private Integer id;
    private String maChiTiet;
    private Double giaGoc;
    private Double giaBan;
    private Integer soLuong;
    private Integer trangThai;
    private Date ngayTao;
    private Date ngayCapNhat;

    // Nested DTOs
    private SanPhamDTO sanPham;
    private MauSacDTO mauSac;
    private KichCoDTO kichCo;

    // Constructor máº·c Ä‘á»‹nh
    public ChiTietSanPhamDTO() {}

    // Constructor tá»« entity
    public ChiTietSanPhamDTO(ChiTietSanPham entity) {
        this.id = entity.getId();
        this.maChiTiet = entity.getMaChiTiet();
        this.giaGoc = entity.getGiaGoc();
        this.giaBan = entity.getGiaBan();
        this.soLuong = entity.getSoLuong();
        this.trangThai = entity.getTrangThai();
        this.ngayTao = entity.getNgayTao();
        this.ngayCapNhat = entity.getNgayCapNhat();

        // Safe conversion cho sáº£n pháº©m
        if (entity.getSanPham() != null) {
            try {
                this.sanPham = new SanPhamDTO();
                this.sanPham.setId(entity.getSanPham().getId());
                this.sanPham.setTenSanPham(entity.getSanPham().getTenSanPham());
                this.sanPham.setMaSanPham(entity.getSanPham().getMaSanPham());
//                this.sanPham.setMoTa(entity.getSanPham().get());
                this.sanPham.setTrangThai(entity.getSanPham().getTrangThai());

                // Safe conversion cho thÆ°Æ¡ng hiá»‡u
                if (entity.getSanPham().getThuongHieu() != null) {
                    ThuongHieuDTO thuongHieu = new ThuongHieuDTO();
                    thuongHieu.setId(entity.getSanPham().getThuongHieu().getId());
                    thuongHieu.setTenThuongHieu(entity.getSanPham().getThuongHieu().getTenThuongHieu());
                    this.sanPham.setThuongHieu(thuongHieu);
                }

                // Safe conversion cho danh má»¥c
                if (entity.getSanPham().getDanhMuc() != null) {
                    DanhMucDTO danhMuc = new DanhMucDTO();
                    danhMuc.setId(entity.getSanPham().getDanhMuc().getId());
                    danhMuc.setTenDanhMuc(entity.getSanPham().getDanhMuc().getTenDanhMuc());
                    this.sanPham.setDanhMuc(danhMuc);
                }
            } catch (Exception e) {
                System.err.println("Error converting SanPham for ChiTietSanPham " + entity.getId() + ": " + e.getMessage());
                this.sanPham = null;
            }
        }

        // Safe conversion cho mÃ u sáº¯c
        if (entity.getMauSac() != null) {
            try {
                this.mauSac = new MauSacDTO();
                this.mauSac.setId(entity.getMauSac().getId());
                this.mauSac.setTenMauSac(entity.getMauSac().getTenMauSac());
                this.mauSac.setMaMau(entity.getMauSac().getMaMauSac());
                this.mauSac.setTrangThai(entity.getMauSac().getTrangThai());
            } catch (Exception e) {
                System.err.println("Error converting MauSac for ChiTietSanPham " + entity.getId() + ": " + e.getMessage());
                this.mauSac = null;
            }
        }

        // Safe conversion cho kÃ­ch cá»¡
        if (entity.getKichCo() != null) {
            try {
                this.kichCo = new KichCoDTO();
                this.kichCo.setId(entity.getKichCo().getId());
                this.kichCo.setTenKichCo(entity.getKichCo().getTenKichCo());
                this.kichCo.setTrangThai(entity.getKichCo().getTrangThai());
            } catch (Exception e) {
                System.err.println("Error converting KichCo for ChiTietSanPham " + entity.getId() + ": " + e.getMessage());
                this.kichCo = null;
            }
        }
    }

    // Getters vÃ  Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaChiTiet() {
        return maChiTiet;
    }

    public void setMaChiTiet(String maChiTiet) {
        this.maChiTiet = maChiTiet;
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

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
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

    public SanPhamDTO getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPhamDTO sanPham) {
        this.sanPham = sanPham;
    }

    public MauSacDTO getMauSac() {
        return mauSac;
    }

    public void setMauSac(MauSacDTO mauSac) {
        this.mauSac = mauSac;
    }

    public KichCoDTO getKichCo() {
        return kichCo;
    }

    public void setKichCo(KichCoDTO kichCo) {
        this.kichCo = kichCo;
    }

    // Nested DTOs
    public static class SanPhamDTO {
        private Integer id;
        private String tenSanPham;
        private String maSanPham;
//        private String moTa;
        private Integer trangThai;
        private ThuongHieuDTO thuongHieu;
        private DanhMucDTO danhMuc;

        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getTenSanPham() { return tenSanPham; }
        public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

        public String getMaSanPham() { return maSanPham; }
        public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }

//        public String getMoTa() { return moTa; }
//        public void setMoTa(String moTa) { this.moTa = moTa; }

        public Integer getTrangThai() { return trangThai; }
        public void setTrangThai(Integer trangThai) { this.trangThai = trangThai; }

        public ThuongHieuDTO getThuongHieu() { return thuongHieu; }
        public void setThuongHieu(ThuongHieuDTO thuongHieu) { this.thuongHieu = thuongHieu; }

        public DanhMucDTO getDanhMuc() { return danhMuc; }
        public void setDanhMuc(DanhMucDTO danhMuc) { this.danhMuc = danhMuc; }
    }

    public static class ThuongHieuDTO {
        private Integer id;
        private String tenThuongHieu;
        private Integer trangThai;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getTenThuongHieu() { return tenThuongHieu; }
        public void setTenThuongHieu(String tenThuongHieu) { this.tenThuongHieu = tenThuongHieu; }

        public Integer getTrangThai() { return trangThai; }
        public void setTrangThai(Integer trangThai) { this.trangThai = trangThai; }
    }

    public static class DanhMucDTO {
        private Integer id;
        private String tenDanhMuc;
        private Integer trangThai;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getTenDanhMuc() { return tenDanhMuc; }
        public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }

        public Integer getTrangThai() { return trangThai; }
        public void setTrangThai(Integer trangThai) { this.trangThai = trangThai; }
    }

    public static class MauSacDTO {
        private Integer id;
        private String tenMauSac;
        private String maMau;
        private Integer trangThai;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getTenMauSac() { return tenMauSac; }
        public void setTenMauSac(String tenMauSac) { this.tenMauSac = tenMauSac; }

        public String getMaMau() { return maMau; }
        public void setMaMau(String maMau) { this.maMau = maMau; }

        public Integer getTrangThai() { return trangThai; }
        public void setTrangThai(Integer trangThai) { this.trangThai = trangThai; }
    }

    public static class KichCoDTO {
        private Integer id;
        private String tenKichCo;
        private Integer trangThai;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getTenKichCo() { return tenKichCo; }
        public void setTenKichCo(String tenKichCo) { this.tenKichCo = tenKichCo; }

        public Integer getTrangThai() { return trangThai; }
        public void setTrangThai(Integer trangThai) { this.trangThai = trangThai; }
    }
}
