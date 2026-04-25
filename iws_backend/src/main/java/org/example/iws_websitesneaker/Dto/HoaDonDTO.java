package org.example.iws_websitesneaker.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class HoaDonDTO {
    private Integer id;
    private String maHoaDon;
    private String tenKhachHang;
    private String sdt;
    private String email;
    private String diaChi;

    // ThÃ´ng tin tiá»n báº¡c chi tiáº¿t
    private BigDecimal tongTienGoc;        // Tá»•ng tiá»n trÆ°á»›c giáº£m giÃ¡
    private BigDecimal tongTienGiamGia;    // Tá»•ng sá»‘ tiá»n Ä‘Æ°á»£c giáº£m
    private BigDecimal tongTienVoucher;    // Tiá»n giáº£m tá»« voucher
    private BigDecimal tienDiem;           // Tiá»n tá»« Ä‘iá»ƒm
    private BigDecimal phiVanChuyen;       // PhÃ­ váº­n chuyá»ƒn
    private BigDecimal tongTien;           // Tá»•ng tiá»n cuá»‘i cÃ¹ng
    private BigDecimal tongThanhToan;      // Tiá»n thá»±c táº¿ pháº£i tráº£

    // ThÃ´ng tin tráº¡ng thÃ¡i vÃ  loáº¡i
    private String trangThaiHoaDon;
    private String loaiHoaDon;
    private String phuongThucThanhToan;

    // ThÃ´ng tin thá»i gian
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date ngayTao;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date ngayXacNhan;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date ngayGiaoHang;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date ngayHoanThanh;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date ngayCapNhat;

    // ThÃ´ng tin ngÆ°á»i dÃ¹ng
    private String tenNhanVien;
    private Integer khachHangId;
    private Integer nhanVienId;
    private String ghiChu;

    // ThÃ´ng tin voucher vÃ  Ä‘iá»ƒm (existing)
    private String tenVoucher;
    private String loaiVoucher;
    private BigDecimal giaTriVoucher;
    private Integer diemSuDung;
    private Double giaTriDiem;

    // âœ… THÃŠM: ThÃ´ng tin voucher chi tiáº¿t
    private List<ChiTietVoucherDTO> chiTietVoucherList;
    private BigDecimal tongTienVoucherChiTiet;      // Tá»•ng tiáº¿t kiá»‡m tá»« táº¥t cáº£ voucher
    private String maVoucherDaApDung;               // MÃ£ voucher chÃ­nh Ä‘Ã£ Ã¡p dá»¥ng
    private Integer soLuongVoucherDaApDung;         // Sá»‘ lÆ°á»£ng voucher Ä‘Ã£ Ã¡p dá»¥ng
    private Double phanTramTietKiemVoucher;         // % tiáº¿t kiá»‡m tá»« voucher
    private Map<String, Integer> thongKeVoucherTheoLoai; // Thá»‘ng kÃª voucher theo loáº¡i

    // Danh sÃ¡ch chi tiáº¿t sáº£n pháº©m
    private List<HoaDonChiTietDTO> chiTietList;

    // Thá»‘ng kÃª
    private Integer soLuongSanPham;
    private Integer tongSoLuong;

    // Tráº¡ng thÃ¡i cÃ³ thá»ƒ thay Ä‘á»•i khÃ´ng
    private Boolean coTheHuy;
    private Boolean coTheXacNhan;
    private Boolean coTheGiaoHang;
    private Boolean coTheHoanThanh;
    private List<ChiTietTraHangDTO> chiTietTraHangList;

    // Thá»‘ng kÃª tráº£ hÃ ng
    private Integer tongSoLuongTraHang;
    private BigDecimal tongTienTraHang;
    private Integer soLoaiSanPhamTraHang;
    private Integer soLuongTraHangChoXuLy;
    private Integer soLuongTraHangDaXuLy;
    private Integer soLuongTraHangTuChoi;

    // Tráº¡ng thÃ¡i cÃ³ thá»ƒ tráº£ hÃ ng
    private Boolean coTheTraHang;

    public HoaDonDTO() {}

    // ===== EXISTING GETTERS & SETTERS =====
    // (keeping all existing methods as-is)

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public BigDecimal getTongTienGoc() { return tongTienGoc; }
    public void setTongTienGoc(BigDecimal tongTienGoc) { this.tongTienGoc = tongTienGoc; }

    public BigDecimal getTongTienGiamGia() { return tongTienGiamGia; }
    public void setTongTienGiamGia(BigDecimal tongTienGiamGia) { this.tongTienGiamGia = tongTienGiamGia; }

    public BigDecimal getTongTienVoucher() { return tongTienVoucher; }
    public void setTongTienVoucher(BigDecimal tongTienVoucher) { this.tongTienVoucher = tongTienVoucher; }

    public BigDecimal getTienDiem() { return tienDiem; }
    public void setTienDiem(BigDecimal tienDiem) { this.tienDiem = tienDiem; }

    public BigDecimal getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(BigDecimal phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }

    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }

    public BigDecimal getTongThanhToan() { return tongThanhToan; }
    public void setTongThanhToan(BigDecimal tongThanhToan) { this.tongThanhToan = tongThanhToan; }

    public String getTrangThaiHoaDon() { return trangThaiHoaDon; }
    public void setTrangThaiHoaDon(String trangThaiHoaDon) { this.trangThaiHoaDon = trangThaiHoaDon; }

    public String getLoaiHoaDon() { return loaiHoaDon; }
    public void setLoaiHoaDon(String loaiHoaDon) { this.loaiHoaDon = loaiHoaDon; }

    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }

    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }

    public Date getNgayXacNhan() { return ngayXacNhan; }
    public void setNgayXacNhan(Date ngayXacNhan) { this.ngayXacNhan = ngayXacNhan; }

    public Date getNgayGiaoHang() { return ngayGiaoHang; }
    public void setNgayGiaoHang(Date ngayGiaoHang) { this.ngayGiaoHang = ngayGiaoHang; }

    public Date getNgayHoanThanh() { return ngayHoanThanh; }
    public void setNgayHoanThanh(Date ngayHoanThanh) { this.ngayHoanThanh = ngayHoanThanh; }

    public Date getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Date ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public Integer getKhachHangId() { return khachHangId; }
    public void setKhachHangId(Integer khachHangId) { this.khachHangId = khachHangId; }

    public Integer getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(Integer nhanVienId) { this.nhanVienId = nhanVienId; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getTenVoucher() { return tenVoucher; }
    public void setTenVoucher(String tenVoucher) { this.tenVoucher = tenVoucher; }

    public String getLoaiVoucher() { return loaiVoucher; }
    public void setLoaiVoucher(String loaiVoucher) { this.loaiVoucher = loaiVoucher; }

    public BigDecimal getGiaTriVoucher() { return giaTriVoucher; }
    public void setGiaTriVoucher(BigDecimal giaTriVoucher) { this.giaTriVoucher = giaTriVoucher; }

    public Integer getDiemSuDung() { return diemSuDung; }
    public void setDiemSuDung(Integer diemSuDung) { this.diemSuDung = diemSuDung; }

    public Double getGiaTriDiem() { return giaTriDiem; }
    public void setGiaTriDiem(Double giaTriDiem) { this.giaTriDiem = giaTriDiem; }

    public List<HoaDonChiTietDTO> getChiTietList() { return chiTietList; }
    public void setChiTietList(List<HoaDonChiTietDTO> chiTietList) { this.chiTietList = chiTietList; }

    public Integer getSoLuongSanPham() { return soLuongSanPham; }
    public void setSoLuongSanPham(Integer soLuongSanPham) { this.soLuongSanPham = soLuongSanPham; }

    public Integer getTongSoLuong() { return tongSoLuong; }
    public void setTongSoLuong(Integer tongSoLuong) { this.tongSoLuong = tongSoLuong; }

    public Boolean getCoTheHuy() { return coTheHuy; }
    public void setCoTheHuy(Boolean coTheHuy) { this.coTheHuy = coTheHuy; }

    public Boolean getCoTheXacNhan() { return coTheXacNhan; }
    public void setCoTheXacNhan(Boolean coTheXacNhan) { this.coTheXacNhan = coTheXacNhan; }

    public Boolean getCoTheGiaoHang() { return coTheGiaoHang; }
    public void setCoTheGiaoHang(Boolean coTheGiaoHang) { this.coTheGiaoHang = coTheGiaoHang; }

    public Boolean getCoTheHoanThanh() { return coTheHoanThanh; }
    public void setCoTheHoanThanh(Boolean coTheHoanThanh) { this.coTheHoanThanh = coTheHoanThanh; }

    public List<ChiTietTraHangDTO> getChiTietTraHangList() {
        return chiTietTraHangList;
    }

    public void setChiTietTraHangList(List<ChiTietTraHangDTO> chiTietTraHangList) {
        this.chiTietTraHangList = chiTietTraHangList;
    }

    public Integer getTongSoLuongTraHang() {
        return tongSoLuongTraHang;
    }

    public void setTongSoLuongTraHang(Integer tongSoLuongTraHang) {
        this.tongSoLuongTraHang = tongSoLuongTraHang;
    }

    public BigDecimal getTongTienTraHang() {
        return tongTienTraHang;
    }

    public void setTongTienTraHang(BigDecimal tongTienTraHang) {
        this.tongTienTraHang = tongTienTraHang;
    }

    public Integer getSoLoaiSanPhamTraHang() {
        return soLoaiSanPhamTraHang;
    }

    public void setSoLoaiSanPhamTraHang(Integer soLoaiSanPhamTraHang) {
        this.soLoaiSanPhamTraHang = soLoaiSanPhamTraHang;
    }

    public Integer getSoLuongTraHangChoXuLy() {
        return soLuongTraHangChoXuLy;
    }

    public void setSoLuongTraHangChoXuLy(Integer soLuongTraHangChoXuLy) {
        this.soLuongTraHangChoXuLy = soLuongTraHangChoXuLy;
    }

    public Integer getSoLuongTraHangDaXuLy() {
        return soLuongTraHangDaXuLy;
    }

    public void setSoLuongTraHangDaXuLy(Integer soLuongTraHangDaXuLy) {
        this.soLuongTraHangDaXuLy = soLuongTraHangDaXuLy;
    }

    public Integer getSoLuongTraHangTuChoi() {
        return soLuongTraHangTuChoi;
    }

    public void setSoLuongTraHangTuChoi(Integer soLuongTraHangTuChoi) {
        this.soLuongTraHangTuChoi = soLuongTraHangTuChoi;
    }

    public Boolean getCoTheTraHang() {
        return coTheTraHang;
    }

    public void setCoTheTraHang(Boolean coTheTraHang) {
        this.coTheTraHang = coTheTraHang;
    }

    // ===== âœ… THÃŠM: GETTERS & SETTERS CHO VOUCHER CHI TIáº¾T =====

    public List<ChiTietVoucherDTO> getChiTietVoucherList() {
        return chiTietVoucherList;
    }

    public void setChiTietVoucherList(List<ChiTietVoucherDTO> chiTietVoucherList) {
        this.chiTietVoucherList = chiTietVoucherList;
    }

    public BigDecimal getTongTienVoucherChiTiet() {
        return tongTienVoucherChiTiet;
    }

    public void setTongTienVoucherChiTiet(BigDecimal tongTienVoucherChiTiet) {
        this.tongTienVoucherChiTiet = tongTienVoucherChiTiet;
    }

    public String getMaVoucherDaApDung() {
        return maVoucherDaApDung;
    }

    public void setMaVoucherDaApDung(String maVoucherDaApDung) {
        this.maVoucherDaApDung = maVoucherDaApDung;
    }

    public Integer getSoLuongVoucherDaApDung() {
        return soLuongVoucherDaApDung;
    }

    public void setSoLuongVoucherDaApDung(Integer soLuongVoucherDaApDung) {
        this.soLuongVoucherDaApDung = soLuongVoucherDaApDung;
    }

    public Double getPhanTramTietKiemVoucher() {
        return phanTramTietKiemVoucher;
    }

    public void setPhanTramTietKiemVoucher(Double phanTramTietKiemVoucher) {
        this.phanTramTietKiemVoucher = phanTramTietKiemVoucher;
    }

    public Map<String, Integer> getThongKeVoucherTheoLoai() {
        return thongKeVoucherTheoLoai;
    }

    public void setThongKeVoucherTheoLoai(Map<String, Integer> thongKeVoucherTheoLoai) {
        this.thongKeVoucherTheoLoai = thongKeVoucherTheoLoai;
    }

    // ===== âœ… THÃŠM: HELPER METHODS =====

    /**
     * Kiá»ƒm tra cÃ³ voucher Ä‘Ã£ Ã¡p dá»¥ng khÃ´ng
     */
    public boolean hasVoucher() {
        return chiTietVoucherList != null && !chiTietVoucherList.isEmpty();
    }

    /**
     * Láº¥y tá»•ng sá»‘ voucher Ä‘Ã£ Ã¡p dá»¥ng
     */
    public int getTotalVouchersApplied() {
        return hasVoucher() ? chiTietVoucherList.size() : 0;
    }

    /**
     * Láº¥y tá»•ng tiáº¿t kiá»‡m thá»±c táº¿ tá»« voucher
     */
    public BigDecimal getActualVoucherSavings() {
        return tongTienVoucherChiTiet != null ? tongTienVoucherChiTiet : BigDecimal.ZERO;
    }

    /**
     * Kiá»ƒm tra cÃ³ voucher theo loáº¡i khÃ´ng
     */
    public boolean hasVoucherOfType(String loaiGiamGia) {
        if (!hasVoucher()) return false;
        return chiTietVoucherList.stream()
                .anyMatch(v -> loaiGiamGia.equals(v.getLoaiGiamGia()));
    }

    /**
     * Láº¥y sá»‘ lÆ°á»£ng voucher theo loáº¡i
     */
    public int getVoucherCountByType(String loaiGiamGia) {
        if (!hasVoucher()) return 0;
        return (int) chiTietVoucherList.stream()
                .filter(v -> loaiGiamGia.equals(v.getLoaiGiamGia()))
                .count();
    }

    /**
     * Láº¥y voucher cÃ³ giÃ¡ trá»‹ giáº£m lá»›n nháº¥t
     */
    public ChiTietVoucherDTO getLargestVoucher() {
        if (!hasVoucher()) return null;
        return chiTietVoucherList.stream()
                .max((v1, v2) -> {
                    BigDecimal saving1 = v1.getSoTienGiam() != null ? v1.getSoTienGiam() : BigDecimal.ZERO;
                    BigDecimal saving2 = v2.getSoTienGiam() != null ? v2.getSoTienGiam() : BigDecimal.ZERO;
                    return saving1.compareTo(saving2);
                })
                .orElse(null);
    }
}
