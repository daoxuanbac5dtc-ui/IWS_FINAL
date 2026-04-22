package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.BanHang.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface BanHangService {

    // ===== QUáº¢N LÃ HÃ“A ÄÆ N CHá»œ =====

    /**
     * Táº¡o hÃ³a Ä‘Æ¡n chá» má»›i
     */
    HoaDonChoResponse taoHoaDonCho(Integer nhanVienId);

    /**
     * Láº¥y danh sÃ¡ch hÃ³a Ä‘Æ¡n chá»
     */
    List<HoaDonChoResponse> layDanhSachHoaDonCho();

    /**
     * Láº¥y chi tiáº¿t hÃ³a Ä‘Æ¡n chá»
     */
    HoaDonChoDetailResponse layChiTietHoaDonCho(Integer hoaDonId);

    /**
     * Láº¥y tá»•ng quan hÃ³a Ä‘Æ¡n chá» (cho sidebar)
     */
    HoaDonChoTongQuanResponse layTongQuanHoaDonCho(Integer hoaDonId);

    /**
     * XÃ³a hÃ³a Ä‘Æ¡n chá»
     */
    void xoaHoaDonCho(Integer hoaDonId);

    // ===== QUáº¢N LÃ Sáº¢N PHáº¨M =====

    /**
     * TÃ¬m kiáº¿m sáº£n pháº©m vá»›i filter vÃ  phÃ¢n trang
     */
    Page<SanPhamChiTietBanHangResponse> timKiemSanPham(
            SanPhamChiTietFilterRequest filter,
            Pageable pageable
    );

    /**
     * Láº¥y chi tiáº¿t sáº£n pháº©m
     */
    SanPhamChiTietBanHangResponse layChiTietSanPham(Integer chiTietSanPhamId);

    /**
     * Scan QR code Ä‘á»ƒ láº¥y sáº£n pháº©m
     */
    ScanQRResponse scanQRSanPham(String qrCode);

    /**
     * Láº¥y sáº£n pháº©m tÆ°Æ¡ng tá»±
     */
    List<SanPhamChiTietBanHangResponse> laySanPhamTuongTu(Integer chiTietSanPhamId);

    // ===== QUáº¢N LÃ Sáº¢N PHáº¨M TRONG HÃ“A ÄÆ N =====

    /**
     * ThÃªm sáº£n pháº©m vÃ o hÃ³a Ä‘Æ¡n
     */
    HoaDonChoTongQuanResponse themSanPhamVaoHoaDon(
            Integer hoaDonId,
            ThemSanPhamRequest request
    );

    /**
     * Cáº­p nháº­t sáº£n pháº©m trong hÃ³a Ä‘Æ¡n
     */
    HoaDonChoTongQuanResponse capNhatSanPhamTrongHoaDon(
            Integer hoaDonId,
            Integer hoaDonChiTietId,
            CapNhatSanPhamRequest request
    );

    /**
     * XÃ³a sáº£n pháº©m khá»i hÃ³a Ä‘Æ¡n
     */
    HoaDonChoTongQuanResponse xoaSanPhamKhoiHoaDon(
            Integer hoaDonId,
            Integer hoaDonChiTietId
    );

    /**
     * TÃ­nh giÃ¡ sáº£n pháº©m (bao gá»“m khuyáº¿n mÃ£i)
     */
    TinhGiaResponse tinhGiaSanPham(TinhGiaRequest request);

    // ===== QUáº¢N LÃ KHÃCH HÃ€NG =====

    /**
     * TÃ¬m kiáº¿m khÃ¡ch hÃ ng
     */
    Page<KhachHangResponse> timKiemKhachHang(String keyword, Pageable pageable);

    /**
     * Táº¡o khÃ¡ch hÃ ng nhanh
     */
    KhachHangResponse taoKhachHangNhanh(TaoKhachHangNhanhRequest request);

    /**
     * Láº¥y thÃ´ng tin chi tiáº¿t khÃ¡ch hÃ ng
     */
    KhachHangDetailResponse layThongTinKhachHang(Integer khachHangId);

    /**
     * Ãp dá»¥ng khÃ¡ch hÃ ng cho hÃ³a Ä‘Æ¡n
     */
    HoaDonChoTongQuanResponse apDungKhachHang(Integer hoaDonId, Integer khachHangId);

    /**
     * Bá» khÃ¡ch hÃ ng khá»i hÃ³a Ä‘Æ¡n
     */
    HoaDonChoTongQuanResponse boKhachHang(Integer hoaDonId);

    // ===== QUáº¢N LÃ VOUCHER =====

    /**
     * Láº¥y danh sÃ¡ch voucher kháº£ dá»¥ng
     */
    List<VoucherResponse> layDanhSachVoucherKhaDung(Integer khachHangId, Double tongTien);

    /**
     * Kiá»ƒm tra voucher cÃ³ há»£p lá»‡ khÃ´ng
     */
    VoucherValidationResponse kiemTraVoucher(ValidateVoucherRequest request);

    /**
     * Ãp dá»¥ng voucher cho hÃ³a Ä‘Æ¡n
     */
    HoaDonChoTongQuanResponse apDungVoucher(Integer hoaDonId, Integer voucherId);

    /**
     * Bá» voucher khá»i hÃ³a Ä‘Æ¡n
     */
    HoaDonChoTongQuanResponse boVoucher(Integer hoaDonId);

    // ===== THANH TOÃN =====

    /**
     * Thanh toÃ¡n hÃ³a Ä‘Æ¡n
     */
    HoaDonResponse thanhToanHoaDon(Integer hoaDonId, ThanhToanRequest request);

    /**
     * Kiá»ƒm tra tá»“n kho trÆ°á»›c khi thanh toÃ¡n
     */
    List<InventoryCheckResponse> kiemTraTonKho(Integer hoaDonId);

    // ===== THá»NG KÃŠ =====

    /**
     * Láº¥y thá»‘ng kÃª bÃ¡n hÃ ng trong ngÃ y
     */
    Map<String, Object> layThongKeBanHangTrongNgay();

    /**
     * Láº¥y sáº£n pháº©m bÃ¡n cháº¡y
     */
    List<Map<String, Object>> laySanPhamBanChay(int limit);

    /**
     * Láº¥y thá»‘ng kÃª doanh thu theo khoáº£ng thá»i gian
     */
    Map<String, Object> layThongKeDoanhThu(String tuNgay, String denNgay);

    /**
     * Láº¥y danh sÃ¡ch danh má»¥c
     */
    List<DanhMucResponse> layDanhSachDanhMuc();

    /**
     * Láº¥y danh sÃ¡ch thÆ°Æ¡ng hiá»‡u
     */
    List<ThuongHieuResponse> layDanhSachThuongHieu();

    /**
     * Láº¥y danh sÃ¡ch mÃ u sáº¯c
     */
    List<MauSacResponse> layDanhSachMauSac();

    /**
     * Láº¥y danh sÃ¡ch kÃ­ch cá»¡
     */
    List<KichCoResponse> layDanhSachKichCo();

    /**
     * Láº¥y danh sÃ¡ch cháº¥t liá»‡u
     */
    List<ChatLieuResponse> layDanhSachChatLieu();

    /**
     * Láº¥y danh sÃ¡ch Ä‘áº¿ giÃ y
     */
    List<DeGiayResponse> layDanhSachDeGiay();

    Integer timNhanVienIdTheoMa(String maNhanVien);

    String chuyenDoiMaTaiKhoanSangMaNhanVien(String maTaiKhoan);

    /**
     * TÃ¬m ID nhÃ¢n viÃªn linh hoáº¡t (mÃ£ NV hoáº·c mÃ£ TK)
     */
    Integer timNhanVienIdLinhHoat(String ma);

    HoaDonChoTongQuanResponse chuyenSangGiaoHang(Integer hoaDonId, GiaoHangRequest request);

    /**
     * TÃ­nh phÃ­ ship dá»±a trÃªn Ä‘á»‹a chá»‰ vÃ  tá»•ng tiá»n
     */
    TinhPhiShipResponse tinhPhiShip(TinhPhiShipRequest request);

    /**
     * Cáº­p nháº­t thÃ´ng tin giao hÃ ng
     */
    HoaDonChoTongQuanResponse capNhatThongTinGiaoHang(Integer hoaDonId, CapNhatGiaoHangRequest request);

    /**
     * XÃ¡c nháº­n giao hÃ ng (chuyá»ƒn sang tráº¡ng thÃ¡i chá» giao)
     */
    HoaDonResponse xacNhanGiaoHang(Integer hoaDonId);

    /**
     * Há»§y giao hÃ ng vÃ  chuyá»ƒn vá» bÃ¡n táº¡i quáº§y
     */
    HoaDonChoTongQuanResponse huyGiaoHang(Integer hoaDonId);

    /**
     * Cáº­p nháº­t tráº¡ng thÃ¡i Ä‘ang giao hÃ ng
     */
    HoaDonResponse capNhatDangGiao(Integer hoaDonId);

    /**
     * XÃ¡c nháº­n Ä‘Ã£ giao hÃ ng thÃ nh cÃ´ng
     */
    HoaDonResponse xacNhanDaGiao(Integer hoaDonId);

    ThanhToanResponse thanhToanHoaDonChiTiet(Integer hoaDonId, ThanhToanRequest request);

    List<InventoryCheckResponse> kiemTraTonKhoTruocThanhToan(Integer hoaDonId);

}
