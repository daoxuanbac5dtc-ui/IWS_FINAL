package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.KhachHangDto;
import org.example.iws_websitesneaker.entity.KhachHang;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface dịch vụ quản lý Khách hàng
 * Định nghĩa các phương thức CRUD và tìm kiếm cho khách hàng
 */
public interface KhachHangService {

    // ================== CÁC THAO TÁC CRUD CƠ BẢN ==================

    /**
     * Lấy tất cả khách hàng
     * @return Danh sách tất cả khách hàng
     */
    List<KhachHang> getAllKhachHang();

    /**
     * Lấy tất cả khách hàng với thông tin đầy đủ (join fetch)
     * @return Danh sách tất cả khách hàng với thông tin TaiKhoan
     */
    List<KhachHang> getAllWithCompleteInfo();

    /**
     * Lấy khách hàng theo ID
     * @param id ID của khách hàng
     * @return Optional chứa khách hàng nếu tìm thấy
     */
    Optional<KhachHang> getKhachHangById(Integer id);

    /**
     * Lấy khách hàng theo ID với eager loading
     * @param id ID của khách hàng
     * @return Optional chứa khách hàng với thông tin đầy đủ
     */
    Optional<KhachHang> findByIdWithEagerLoading(Integer id);

    /**
     * Thêm khách hàng mới
     * @param khachHang Thông tin khách hàng cần thêm
     */
    void addKhachHang(KhachHang khachHang);

    /**
     * Cập nhật thông tin khách hàng
     * @param khachHang Thông tin khách hàng đã cập nhật
     */
    void updateKhachHang(KhachHang khachHang);

    /**
     * Xóa khách hàng (soft delete)
     * @param id ID của khách hàng cần xóa
     */
    void deleteKhachHang(Integer id);

    /**
     * Tìm khách hàng theo ID tài khoản
     * @param taiKhoanId ID của tài khoản
     * @return Khách hàng nếu tìm thấy, null nếu không
     */
    KhachHang findByTaiKhoanId(Integer taiKhoanId);

    /**
     * Xóa khách hàng theo ID tài khoản (hard delete khi xóa tài khoản)
     * @param taiKhoanId ID của tài khoản
     */
    void deleteByTaiKhoanId(Integer taiKhoanId);

    // ================== CÁC PHƯƠNG THỨC TÌM KIẾM ==================

    /**
     * Tìm khách hàng theo ID tài khoản
     * @param taiKhoanId ID của tài khoản
     * @return Optional chứa khách hàng nếu tìm thấy
     */
    Optional<KhachHang> findByTaiKhoanIdOptional(Integer taiKhoanId);

    /**
     * Kiểm tra số điện thoại đã tồn tại chưa
     * @param sdt Số điện thoại cần kiểm tra
     * @return true nếu số điện thoại đã tồn tại
     */
    boolean existsBySdt(String sdt);

    /**
     * Kiểm tra mã khách hàng đã tồn tại chưa
     * @param maKhachHang Mã khách hàng cần kiểm tra
     * @return true nếu mã khách hàng đã tồn tại
     */
    boolean existsByMaKhachHang(String maKhachHang);

    /**
     * Tìm khách hàng theo mã khách hàng
     * @param maKhachHang Mã khách hàng
     * @return Optional chứa khách hàng nếu tìm thấy
     */
    Optional<KhachHang> findByMaKhachHang(String maKhachHang);

    /**
     * Tìm khách hàng theo họ tên và số điện thoại
     * @param hoTen Họ tên khách hàng
     * @param sdt Số điện thoại
     * @return Khách hàng nếu tìm thấy, null nếu không
     */
    KhachHang findByHoTenAndSdt(String hoTen, String sdt);

    // ================== CÁC PHƯƠNG THỨC TÌM KIẾM CƠ BẢN ==================

    /**
     * Tìm kiếm khách hàng theo từ khóa
     * Tìm trong: họ tên, email, số điện thoại, mã khách hàng
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách khách hàng khớp với từ khóa
     */
    List<KhachHang> searchByKeyword(String keyword);

    /**
     * Tìm kiếm nâng cao khách hàng
     * @param hoTen Họ tên (tìm kiếm gần đúng)
     * @param email Email (tìm kiếm gần đúng)
     * @param sdt Số điện thoại (tìm kiếm gần đúng)
     * @param maKhachHang Mã khách hàng (tìm kiếm gần đúng)
     * @param trangThai Trạng thái (tìm kiếm chính xác)
     * @param gioiTinh Giới tính (tìm kiếm chính xác) - hiện tại không sử dụng
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách khách hàng thỏa mãn điều kiện
     */
    List<KhachHang> searchAdvanced(String hoTen, String email, String sdt,
                                   String maKhachHang, Integer trangThai, String gioiTinh,
                                   Date startDate, Date endDate);

    // ================== CÁC PHƯƠNG THỨC TÌM KIẾM NÂNG CAO ==================

    /**
     * Tìm kiếm nâng cao khách hàng với tất cả tiêu chí bao gồm cả khoảng thời gian
     * @param hoTen Họ tên (tìm kiếm gần đúng)
     * @param email Email (tìm kiếm gần đúng)
     * @param sdt Số điện thoại (tìm kiếm gần đúng)
     * @param maKhachHang Mã khách hàng (tìm kiếm gần đúng)
     * @param diaChi Địa chỉ (tìm kiếm gần đúng)
     * @param trangThai Trạng thái (tìm kiếm chính xác)
     * @param startDate Ngày bắt đầu (ngày tạo >= startDate)
     * @param endDate Ngày kết thúc (ngày tạo <= endDate)
     * @return Danh sách khách hàng thỏa mãn tất cả tiêu chí
     */
    List<KhachHang> searchAdvancedWithAllCriteria(
            String hoTen,
            String email,
            String sdt,
            String maKhachHang,
            String diaChi,
            Integer trangThai,
            Date startDate,
            Date endDate
    );

    /**
     * Tìm kiếm khách hàng theo chi tiết địa chỉ (2-level addressing)
     * @param provinceCode Mã tỉnh/thành phố
     * @param districtCode Deprecated - không sử dụng (để compatibility)
     * @param addressDetail Chi tiết địa chỉ
     * @return Danh sách khách hàng có địa chỉ khớp
     */
    List<KhachHang> searchByAddressDetails(String provinceCode, String districtCode, String addressDetail);

    /**
     * Tìm khách hàng theo pattern số điện thoại
     * @param phonePattern Pattern số điện thoại (ví dụ: "098", "0123")
     * @return Danh sách khách hàng có số điện thoại khớp pattern
     */
    List<KhachHang> findByPhonePattern(String phonePattern);

    /**
     * Tìm khách hàng có email khớp pattern
     * @param emailPattern Pattern email (ví dụ: "@gmail.com", "abc")
     * @return Danh sách khách hàng có email khớp pattern
     */
    List<KhachHang> findByEmailPattern(String emailPattern);

    // ================== CÁC PHƯƠNG THỨC VALIDATION ==================

    /**
     * Kiểm tra tính hợp lệ của các tham số tìm kiếm khách hàng
     * @param hoTen Họ tên cần kiểm tra
     * @param email Email cần kiểm tra
     * @param sdt Số điện thoại cần kiểm tra
     * @return true nếu tất cả tham số hợp lệ
     */
    boolean isValidKhachHangSearchParams(String hoTen, String email, String sdt);

    /**
     * Kiểm tra số điện thoại có đang được sử dụng bởi khách hàng khác không
     * @param sdt Số điện thoại cần kiểm tra
     * @param excludeId ID khách hàng loại trừ (cho trường hợp update)
     * @return true nếu số điện thoại đã được sử dụng
     */
    boolean isPhoneNumberUsed(String sdt, Integer excludeId);

    // ================== CÁC PHƯƠNG THỨC THỐNG KÊ ==================

    /**
     * Đếm số khách hàng theo trạng thái trong khoảng thời gian
     * @param trangThai Trạng thái cần đếm (null để đếm tất cả)
     * @param startDate Ngày bắt đầu (null để không giới hạn)
     * @param endDate Ngày kết thúc (null để không giới hạn)
     * @return Số lượng khách hàng thỏa mãn điều kiện
     */
    long countByStatusAndDateRange(Integer trangThai, Date startDate, Date endDate);

    /**
     * Lấy thống kê tổng quan khách hàng
     * @return Map chứa các thống kê: total, active, inactive, recent, profileCompleted, newToday
     */
    Map<String, Object> getStatistics();

    // ================== CÁC PHƯƠNG THỨC NGHIỆP VỤ ==================

    /**
     * Hoàn thiện thông tin khách hàng (profile completion)
     * @param id ID khách hàng
     * @param profileData Dữ liệu profile cần hoàn thiện
     * @return Khách hàng đã được cập nhật
     */
    KhachHang completeProfile(Integer id, KhachHangDto profileData);

    /**
     * Cập nhật trạng thái khách hàng
     * @param id ID khách hàng
     * @param trangThai Trạng thái mới (0: vô hiệu hóa, 1: kích hoạt)
     */
    void updateStatus(Integer id, Integer trangThai);

    /**
     * Convert KhachHang entity sang DTO với thông tin địa chỉ đầy đủ
     * @param khachHang Entity cần convert
     * @return DTO đã convert với danh sách địa chỉ 2-level
     */
    KhachHangDto convertToDto(KhachHang khachHang);

    // ================== CÁC PHƯƠNG THỨC BỔ SUNG ==================

    /**
     * Lấy danh sách khách hàng hoạt động
     * @return Danh sách khách hàng có trạng thái = 1
     */
    List<KhachHang> getActiveKhachHang();

    /**
     * Lấy khách hàng theo khoảng thời gian tạo
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách khách hàng được tạo trong khoảng thời gian
     */
    List<KhachHang> getKhachHangByDateRange(Date startDate, Date endDate);

    /**
     * Lấy thống kê chi tiết khách hàng
     * @return Map chứa thống kê chi tiết: total, active, inactive, recent
     */
    Map<String, Long> getKhachHangStatistics();

    /**
     * Kiểm tra có thể xóa khách hàng không
     * @param id ID khách hàng
     * @return true nếu có thể xóa
     */
    boolean canDeleteKhachHang(Integer id);

    /**
     * Chuyển đổi trạng thái khách hàng (active/inactive)
     * @param id ID khách hàng
     */
    void toggleTrangThai(Integer id);
}
