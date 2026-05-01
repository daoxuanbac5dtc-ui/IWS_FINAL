package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.BanHang.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface BanHangService {

    // ===== QUẢN LÝ HÓA ĐƠN CHỜ =====

    /**
     * Tạo hóa đơn chờ mới
     */
    HoaDonChoResponse taoHoaDonCho(Integer nhanVienId);

    /**
     * Lấy danh sách hóa đơn chờ
     */
    List<HoaDonChoResponse> layDanhSachHoaDonCho();

    /**
     * Lấy chi tiết hóa đơn chờ
     */
    HoaDonChoDetailResponse layChiTietHoaDonCho(Integer hoaDonId);

    /**
     * Lấy tổng quan hóa đơn chờ (cho sidebar)
     */
    HoaDonChoTongQuanResponse layTongQuanHoaDonCho(Integer hoaDonId);

    /**
     * Xóa hóa đơn chờ
     */
    void xoaHoaDonCho(Integer hoaDonId);

    // ===== QUẢN LÝ SẢN PHẨM =====

    /**
     * Tìm kiếm sản phẩm với filter và phân trang
     */
    Page<SanPhamChiTietBanHangResponse> timKiemSanPham(
            SanPhamChiTietFilterRequest filter,
            Pageable pageable
    );

    /**
     * Lấy chi tiết sản phẩm
     */
    SanPhamChiTietBanHangResponse layChiTietSanPham(Integer chiTietSanPhamId);

    /**
     * Scan QR code để lấy sản phẩm
     */
    ScanQRResponse scanQRSanPham(String qrCode);

    /**
     * Lấy sản phẩm tương tự
     */
    List<SanPhamChiTietBanHangResponse> laySanPhamTuongTu(Integer chiTietSanPhamId);

    // ===== QUẢN LÝ SẢN PHẨM TRONG HÓA ĐƠN =====

    /**
     * Thêm sản phẩm vào hóa đơn
     */
    HoaDonChoTongQuanResponse themSanPhamVaoHoaDon(
            Integer hoaDonId,
            ThemSanPhamRequest request
    );

    /**
     * Cập nhật sản phẩm trong hóa đơn
     */
    HoaDonChoTongQuanResponse capNhatSanPhamTrongHoaDon(
            Integer hoaDonId,
            Integer hoaDonChiTietId,
            CapNhatSanPhamRequest request
    );

    /**
     * Xóa sản phẩm khỏi hóa đơn
     */
    HoaDonChoTongQuanResponse xoaSanPhamKhoiHoaDon(
            Integer hoaDonId,
            Integer hoaDonChiTietId
    );

    /**
     * Tính giá sản phẩm (bao gồm khuyến mãi)
     */
    TinhGiaResponse tinhGiaSanPham(TinhGiaRequest request);

    // ===== QUẢN LÝ KHÁCH HÀNG =====

    /**
     * Tìm kiếm khách hàng
     */
    Page<KhachHangResponse> timKiemKhachHang(String keyword, Pageable pageable);

    /**
     * Tạo khách hàng nhanh
     */
    KhachHangResponse taoKhachHangNhanh(TaoKhachHangNhanhRequest request);

    /**
     * Lấy thông tin chi tiết khách hàng
     */
    KhachHangDetailResponse layThongTinKhachHang(Integer khachHangId);

    /**
     * Áp dụng khách hàng cho hóa đơn
     */
    HoaDonChoTongQuanResponse apDungKhachHang(Integer hoaDonId, Integer khachHangId);

    /**
     * Bỏ khách hàng khỏi hóa đơn
     */
    HoaDonChoTongQuanResponse boKhachHang(Integer hoaDonId);

    // ===== QUẢN LÝ VOUCHER =====

    /**
     * Lấy danh sách voucher khả dụng
     */
    List<VoucherResponse> layDanhSachVoucherKhaDung(Integer khachHangId, Double tongTien);

    /**
     * Kiểm tra voucher có hợp lệ không
     */
    VoucherValidationResponse kiemTraVoucher(ValidateVoucherRequest request);

    /**
     * Áp dụng voucher cho hóa đơn
     */
    HoaDonChoTongQuanResponse apDungVoucher(Integer hoaDonId, Integer voucherId);

    /**
     * Bỏ voucher khỏi hóa đơn
     */
    HoaDonChoTongQuanResponse boVoucher(Integer hoaDonId);

    // ===== THANH TOÁN =====

    /**
     * Thanh toán hóa đơn
     */
    HoaDonResponse thanhToanHoaDon(Integer hoaDonId, ThanhToanRequest request);

    /**
     * Kiểm tra tồn kho trước khi thanh toán
     */
    List<InventoryCheckResponse> kiemTraTonKho(Integer hoaDonId);

    // ===== THỐNG KÊ =====

    /**
     * Lấy thống kê bán hàng trong ngày
     */
    Map<String, Object> layThongKeBanHangTrongNgay();

    /**
     * Lấy sản phẩm bán chạy
     */
    List<Map<String, Object>> laySanPhamBanChay(int limit);

    /**
     * Lấy thống kê doanh thu theo khoảng thời gian
     */
    Map<String, Object> layThongKeDoanhThu(String tuNgay, String denNgay);

    /**
     * Lấy danh sách danh mục
     */
    List<DanhMucResponse> layDanhSachDanhMuc();

    /**
     * Lấy danh sách thương hiệu
     */
    List<ThuongHieuResponse> layDanhSachThuongHieu();

    /**
     * Lấy danh sách màu sắc
     */
    List<MauSacResponse> layDanhSachMauSac();

    /**
     * Lấy danh sách kích cỡ
     */
    List<KichCoResponse> layDanhSachKichCo();

    /**
     * Lấy danh sách chất liệu
     */
    List<ChatLieuResponse> layDanhSachChatLieu();

    /**
     * Lấy danh sách đế giày
     */
    List<DeGiayResponse> layDanhSachDeGiay();

    Integer timNhanVienIdTheoMa(String maNhanVien);

    String chuyenDoiMaTaiKhoanSangMaNhanVien(String maTaiKhoan);

    /**
     * Tìm ID nhân viên linh hoạt (mã NV hoặc mã TK)
     */
    Integer timNhanVienIdLinhHoat(String ma);

    HoaDonChoTongQuanResponse chuyenSangGiaoHang(Integer hoaDonId, GiaoHangRequest request);

    /**
     * Tính phí ship dựa trên địa chỉ và tổng tiền
     */
    TinhPhiShipResponse tinhPhiShip(TinhPhiShipRequest request);

    /**
     * Cập nhật thông tin giao hàng
     */
    HoaDonChoTongQuanResponse capNhatThongTinGiaoHang(Integer hoaDonId, CapNhatGiaoHangRequest request);

    /**
     * Xác nhận giao hàng (chuyển sang trạng thái chờ giao)
     */
    HoaDonResponse xacNhanGiaoHang(Integer hoaDonId);

    /**
     * Hủy giao hàng và chuyển về bán tại quầy
     */
    HoaDonChoTongQuanResponse huyGiaoHang(Integer hoaDonId);

    /**
     * Cập nhật trạng thái đang giao hàng
     */
    HoaDonResponse capNhatDangGiao(Integer hoaDonId);

    /**
     * Xác nhận đã giao hàng thành công
     */
    HoaDonResponse xacNhanDaGiao(Integer hoaDonId);

    ThanhToanResponse thanhToanHoaDonChiTiet(Integer hoaDonId, ThanhToanRequest request);

    List<InventoryCheckResponse> kiemTraTonKhoTruocThanhToan(Integer hoaDonId);

}
