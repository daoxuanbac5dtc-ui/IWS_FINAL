package org.example.iws_websitesneaker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Table(name = "hoa_don")
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang", nullable = true)
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien", nullable = true)
    private NhanVien nhanVien;

    @Column(name = "ma_hoa_don", nullable = false, length = 25)
    private String maHoaDon;

    @Column(name = "dia_chi", nullable = false, length = 250)
    private String diaChi;

    @Column(name = "email", nullable = false, length = 25)
    private String email;

    @Column(name = "ghi_chu", length = 250)
    private String ghiChu;

    @Column(name = "sdt", nullable = false, length = 10)
    private String sdt;

    @Column(name = "trang_thai_hoa_don", nullable = false, length = 50)
    private String trangThaiHoaDon;

    @Column(name = "loai_hoa_don", nullable = false, length = 25)
    private String loaiHoaDon;

    @Column(name = "ten_nguoi_dung", nullable = false, length = 50)
    private String tenNguoiDung;

    @Column(name = "phi_van_chuyen", nullable = false, precision = 10, scale = 2)
    private BigDecimal phiVanChuyen;

    @Column(name = "diem_su_dung")
    private Integer diemSuDung;

    @Column(name = "tong_tien", nullable = false, precision = 10, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "gia_tri_diem")
    private Double giaTriDiem;

    @Column(name = "tong_thanh_toan", precision = 10, scale = 2)
    private BigDecimal tongThanhToan;

    @Column(name = "ngay_hoan_thanh", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayHoanThanh;

    @Column(name = "ngay_xac_nhan", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayXacNhan;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    @Column(name = "ngay_giao_hang", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayGiaoHang;

    @Column(name = "ngay_nhan_hang", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayNhanHang;

    @Column(name = "thoi_gian_van_chuyen", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date thoiGianVanChuyen;

    @Column(name = "phuong_thuc_thanh_toan")
    private String phuongThucThanhToan;

    public HoaDon(Integer id, KhachHang khachHang, NhanVien nhanVien, String maHoaDon, String diaChi, String email, String ghiChu, String sdt, String trangThaiHoaDon, String loaiHoaDon, String tenNguoiDung, BigDecimal phiVanChuyen, Integer diemSuDung, BigDecimal tongTien, Double giaTriDiem, BigDecimal tongThanhToan, Date ngayHoanThanh, Date ngayXacNhan, Date ngayTao, Date ngayCapNhat, Date ngayGiaoHang, Date ngayNhanHang, Date thoiGianVanChuyen, String phuongThucThanhToan) {
        this.id = id;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
        this.maHoaDon = maHoaDon;
        this.diaChi = diaChi;
        this.email = email;
        this.ghiChu = ghiChu;
        this.sdt = sdt;
        this.trangThaiHoaDon = trangThaiHoaDon;
        this.loaiHoaDon = loaiHoaDon;
        this.tenNguoiDung = tenNguoiDung;
        this.phiVanChuyen = phiVanChuyen;
        this.diemSuDung = diemSuDung;
        this.tongTien = tongTien;
        this.giaTriDiem = giaTriDiem;
        this.tongThanhToan = tongThanhToan;
        this.ngayHoanThanh = ngayHoanThanh;
        this.ngayXacNhan = ngayXacNhan;
        this.ngayTao = ngayTao;
        this.ngayCapNhat = ngayCapNhat;
        this.ngayGiaoHang = ngayGiaoHang;
        this.ngayNhanHang = ngayNhanHang;
        this.thoiGianVanChuyen = thoiGianVanChuyen;
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public HoaDon() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getTrangThaiHoaDon() {
        return trangThaiHoaDon;
    }

    public void setTrangThaiHoaDon(String trangThaiHoaDon) {
        this.trangThaiHoaDon = trangThaiHoaDon;
    }

    public String getLoaiHoaDon() {
        return loaiHoaDon;
    }

    public void setLoaiHoaDon(String loaiHoaDon) {
        this.loaiHoaDon = loaiHoaDon;
    }

    public String getTenNguoiDung() {
        return tenNguoiDung;
    }

    public void setTenNguoiDung(String tenNguoiDung) {
        this.tenNguoiDung = tenNguoiDung;
    }

    public BigDecimal getPhiVanChuyen() {
        return phiVanChuyen;
    }

    public void setPhiVanChuyen(BigDecimal phiVanChuyen) {
        this.phiVanChuyen = phiVanChuyen;
    }

    public Integer getDiemSuDung() {
        return diemSuDung;
    }

    public void setDiemSuDung(Integer diemSuDung) {
        this.diemSuDung = diemSuDung;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public Double getGiaTriDiem() {
        return giaTriDiem;
    }

    public void setGiaTriDiem(Double giaTriDiem) {
        this.giaTriDiem = giaTriDiem;
    }

    public BigDecimal getTongThanhToan() {
        return tongThanhToan;
    }

    public void setTongThanhToan(BigDecimal tongThanhToan) {
        this.tongThanhToan = tongThanhToan;
    }

    public Date getNgayHoanThanh() {
        return ngayHoanThanh;
    }

    public void setNgayHoanThanh(Date ngayHoanThanh) {
        this.ngayHoanThanh = ngayHoanThanh;
    }

    public Date getNgayXacNhan() {
        return ngayXacNhan;
    }

    public void setNgayXacNhan(Date ngayXacNhan) {
        this.ngayXacNhan = ngayXacNhan;
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

    public Date getNgayGiaoHang() {
        return ngayGiaoHang;
    }

    public void setNgayGiaoHang(Date ngayGiaoHang) {
        this.ngayGiaoHang = ngayGiaoHang;
    }

    public Date getNgayNhanHang() {
        return ngayNhanHang;
    }

    public void setNgayNhanHang(Date ngayNhanHang) {
        this.ngayNhanHang = ngayNhanHang;
    }

    public Date getThoiGianVanChuyen() {
        return thoiGianVanChuyen;
    }

    public void setThoiGianVanChuyen(Date thoiGianVanChuyen) {
        this.thoiGianVanChuyen = thoiGianVanChuyen;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }
}

