package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.ChiTietTraHangDTO;

import java.util.List;
import java.util.Map;

public interface ChiTietTraHangService {

    // Các method cơ bản
    List<ChiTietTraHangDTO> getChiTietTraHangByChiTietSanPham(Integer chiTietSanPhamId);

    ChiTietTraHangDTO getById(Integer id);

    List<ChiTietTraHangDTO> getChiTietTraHangByHoaDon(Integer hoaDonId);

    ChiTietTraHangDTO createChiTietTraHang(ChiTietTraHangDTO dto);

    ChiTietTraHangDTO updateChiTietTraHang(Integer id, ChiTietTraHangDTO dto);

    ChiTietTraHangDTO updateTrangThai(Integer id, String trangThai, String ghiChu);

    void deleteChiTietTraHang(Integer id);

    // Các method thống kê và kiểm tra
    Integer getTotalReturnedQuantity(Integer chiTietSanPhamId);

    Map<String, Object> getReturnStatistics(Integer hoaDonId);

    boolean canReturn(Integer chiTietSanPhamId, Integer soLuong);

    ChiTietTraHangDTO approveReturn(Integer id, String ghiChu);

    ChiTietTraHangDTO rejectReturn(Integer id, String lyDo);

    // Các method hỗ trợ hóa đơn
    List<ChiTietTraHangDTO> getChiTietTraHangByHoaDonAndStatus(Integer hoaDonId, String trangThai);

    Integer getTotalReturnedQuantityByInvoice(Integer hoaDonId);

    Double getTotalReturnValueByInvoice(Integer hoaDonId);

    Boolean hasReturnsForInvoice(Integer hoaDonId);

    // ✅ THÊM: Các method mới cho tìm kiếm và lý do

    /**
     * Tìm kiếm chi tiết trả hàng theo nhiều tiêu chí
     *
     * @param lyDo      Lý do trả hàng (có thể null)
     * @param trangThai Trạng thái (có thể null)
     * @param hoaDonId  ID hóa đơn (có thể null)
     * @return Danh sách chi tiết trả hàng
     */
    List<ChiTietTraHangDTO> searchChiTietTraHang(String lyDo, String trangThai, Integer hoaDonId);

    /**
     * Lấy danh sách chi tiết trả hàng theo lý do
     *
     * @param lyDo Lý do trả hàng
     * @return Danh sách chi tiết trả hàng
     */
    List<ChiTietTraHangDTO> getChiTietTraHangByLyDo(String lyDo);

    /**
     * Lấy danh sách chi tiết trả hàng có ảnh minh chứng
     *
     * @return Danh sách chi tiết trả hàng có ảnh
     */
    List<ChiTietTraHangDTO> getChiTietTraHangWithImages();

    /**
     * Lấy danh sách chi tiết trả hàng không có ảnh minh chứng
     *
     * @return Danh sách chi tiết trả hàng không có ảnh
     */
    List<ChiTietTraHangDTO> getChiTietTraHangWithoutImages();

    /**
     * Lấy thống kê theo lý do trả hàng
     *
     * @return Map chứa thống kê: {lyDo: soLuong}
     */
    Map<String, Long> getReturnReasonStatistics();

    /**
     * Đếm số lượng trả hàng có ảnh minh chứng
     *
     * @return Số lượng
     */
    Long countReturnsWithImages();

    /**
     * Đếm số lượng trả hàng theo lý do cụ thể
     *
     * @param lyDo Lý do trả hàng
     * @return Số lượng
     */
    Long countByLyDo(String lyDo);

    /**
     * Cập nhật ảnh minh chứng cho chi tiết trả hàng
     *
     * @param id          ID chi tiết trả hàng
     * @param duongDanAnh Đường dẫn ảnh mới
     * @return Chi tiết trả hàng đã cập nhật
     */
    ChiTietTraHangDTO updateImage(Integer id, String duongDanAnh);

    /**
     * Xóa ảnh minh chứng của chi tiết trả hàng
     *
     * @param id ID chi tiết trả hàng
     * @return Chi tiết trả hàng đã cập nhật
     */
    ChiTietTraHangDTO removeImage(Integer id);

    ChiTietTraHangDTO approveReturnWithInventory(Integer id, String ghiChu);
    ChiTietTraHangDTO approveReturnNoInventory(Integer id, String ghiChu);
}
