package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for NhanVien (Employee) management - UPDATED VERSION
 *
 * QUAN TRỌNG:
 * - Không có chức năng thêm nhân viên trực tiếp
 * - Nhân viên được tạo tự động khi tạo tài khoản
 * - Chỉ cho phép cập nhật thông tin cơ bản
 * - Không được thay đổi tài khoản liên kết
 * - Thêm hỗ trợ pagination và search nâng cao
 */
public interface NhanVienService {

    // ================== BASIC CRUD OPERATIONS ==================

    /**
     * Lấy tất cả nhân viên với pagination
     * @param pageable Thông tin phân trang và sắp xếp
     * @return Page chứa danh sách nhân viên
     */
    Page<NhanVien> findAllWithPagination(Pageable pageable);

    /**
     * Lấy tất cả nhân viên (không pagination) - Backward compatibility
     * @return Danh sách tất cả nhân viên
     */
    List<NhanVien> getAllNhanVien();

    /**
     * Lấy nhân viên theo ID
     * @param id ID của nhân viên
     * @return Optional chứa nhân viên nếu tìm thấy
     */
    Optional<NhanVien> getNhanVienById(Integer id);

    /**
     * CHỨC NĂNG NÀY CHỈ ĐƯỢC SỬ DỤNG KHI TẠO TỪ TÀI KHOẢN
     * Thêm nhân viên mới (được gọi tự động từ TaiKhoanService)
     * @param nhanVien Thông tin nhân viên cần thêm
     */
    void addNhanVien(NhanVien nhanVien);

    /**
     * Cập nhật thông tin nhân viên - UPDATED
     * Chỉ cho phép cập nhật: họ tên, số điện thoại, mã nhân viên, trạng thái
     * KHÔNG cho phép thay đổi tài khoản liên kết
     * @param nhanVien Thông tin nhân viên cần cập nhật
     */
    void updateNhanVien(NhanVien nhanVien);

    /**
     * Xóa nhân viên (soft delete - chỉ thay đổi trạng thái)
     * @param id ID của nhân viên cần xóa
     */
    void deleteNhanVien(Integer id);

    /**
     * Xóa nhiều nhân viên cùng lúc - NEW
     * @param ids Danh sách ID nhân viên cần xóa
     */
    void batchDeleteNhanVien(List<Integer> ids);

    // ================== ADVANCED SEARCH OPERATIONS - NEW ==================

    /**
     * Tìm kiếm nhân viên với nhiều tiêu chí và pagination - MAIN SEARCH METHOD
     * @param globalSearch Từ khóa tìm kiếm toàn cục (tên, email, SĐT, mã NV)
     * @param trangThai Trạng thái nhân viên (null = tất cả, 1 = active, 0 = inactive)
     * @param startDate Ngày bắt đầu (yyyy-MM-dd)
     * @param endDate Ngày kết thúc (yyyy-MM-dd)
     * @param pageable Thông tin phân trang và sắp xếp
     * @return Page chứa kết quả tìm kiếm
     */
    Page<NhanVien> searchWithCriteria(String globalSearch, Integer trangThai,
                                      String startDate, String endDate, Pageable pageable);

    /**
     * Tìm kiếm nhân viên theo từ khóa - Simplified version
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách nhân viên khớp với từ khóa
     */
    List<NhanVien> searchByKeyword(String keyword);

    /**
     * Tìm kiếm nâng cao nhân viên với nhiều tiêu chí - Backward compatibility
     * @param hoTen Họ tên (tìm kiếm gần đúng)
     * @param email Email (tìm kiếm gần đúng)
     * @param sdt Số điện thoại (tìm kiếm gần đúng)
     * @param maNhanVien Mã nhân viên (tìm kiếm gần đúng)
     * @param diaChi Địa chỉ (tìm kiếm gần đúng)
     * @param trangThai Trạng thái (tìm kiếm chính xác)
     * @param startDate Ngày bắt đầu (ngày tạo >= startDate)
     * @param endDate Ngày kết thúc (ngày tạo <= endDate)
     * @return Danh sách nhân viên thỏa mãn các tiêu chí
     */
    List<NhanVien> searchAdvancedWithAllCriteria(
            String hoTen,
            String email,
            String sdt,
            String maNhanVien,
            String diaChi,
            Integer trangThai,
            Date startDate,
            Date endDate
    );

    // ================== TAI KHOAN RELATED OPERATIONS ==================

    /**
     * Xóa nhân viên theo ID tài khoản (hard delete)
     * Được gọi khi xóa tài khoản
     * @param taiKhoanId ID của tài khoản
     */
    void deleteByTaiKhoanId(Integer taiKhoanId);

    /**
     * Tìm nhân viên theo ID tài khoản
     * @param taiKhoanId ID của tài khoản
     * @return Optional chứa nhân viên nếu tìm thấy
     */
    Optional<NhanVien> findByTaiKhoanId(Integer taiKhoanId);

    /**
     * Tìm nhân viên theo email tài khoản
     * @param email Email của tài khoản
     * @return Optional chứa nhân viên nếu tìm thấy
     */
    Optional<NhanVien> findByTaiKhoanEmail(String email);

    // ================== SIMPLE QUERY OPERATIONS ==================

    /**
     * Tìm nhân viên theo mã nhân viên
     * @param maNhanVien Mã nhân viên
     * @return Optional chứa nhân viên nếu tìm thấy
     */
    Optional<NhanVien> findByMaNhanVien(String maNhanVien);

    /**
     * Tìm nhân viên theo trạng thái
     * @param trangThai Trạng thái (0: nghỉ việc, 1: đang làm việc)
     * @return Danh sách nhân viên có trạng thái tương ứng
     */
    List<NhanVien> findByTrangThai(Integer trangThai);

    /**
     * Tìm nhân viên theo trạng thái với pagination - NEW
     * @param trangThai Trạng thái
     * @param pageable Thông tin phân trang
     * @return Page chứa nhân viên
     */
    Page<NhanVien> findByTrangThaiWithPagination(Integer trangThai, Pageable pageable);

    // ================== VALIDATION METHODS ==================

    /**
     * Validate các tham số tìm kiếm nhân viên
     * @param hoTen Họ tên cần validate
     * @param email Email cần validate
     * @param sdt Số điện thoại cần validate
     * @param maNhanVien Mã nhân viên cần validate
     * @return true nếu tất cả tham số hợp lệ
     */
    boolean isValidNhanVienSearchParams(String hoTen, String email, String sdt, String maNhanVien);

    /**
     * Validate dữ liệu nhân viên trước khi lưu - NEW
     * @param nhanVien Nhân viên cần validate
     * @return Danh sách lỗi validation (empty nếu hợp lệ)
     */
    List<String> validateNhanVienData(NhanVien nhanVien);

    // ================== EXISTENCE CHECKS ==================

    /**
     * Kiểm tra mã nhân viên đã tồn tại chưa
     * @param maNhanVien Mã nhân viên cần kiểm tra
     * @return true nếu mã nhân viên đã tồn tại
     */
    boolean existsByMaNhanVien(String maNhanVien);

    /**
     * Kiểm tra mã nhân viên đã tồn tại (loại trừ ID hiện tại) - NEW
     * @param maNhanVien Mã nhân viên cần kiểm tra
     * @param excludeId ID nhân viên loại trừ
     * @return true nếu mã nhân viên đã tồn tại
     */
    boolean existsByMaNhanVienExcludingId(String maNhanVien, Integer excludeId);

    /**
     * Kiểm tra số điện thoại có đang được sử dụng bởi nhân viên khác không
     * @param sdt Số điện thoại cần kiểm tra
     * @param excludeId ID nhân viên loại trừ (cho trường hợp update)
     * @return true nếu số điện thoại đã được sử dụng
     */
    boolean isPhoneNumberUsed(String sdt, Integer excludeId);

    // ================== STATISTICAL METHODS ==================

    /**
     * Tìm nhân viên theo phòng ban (nếu có)
     * @param department Tên phòng ban
     * @return Danh sách nhân viên thuộc phòng ban đó
     */
    List<NhanVien> findByDepartment(String department);

    /**
     * Đếm số nhân viên đang hoạt động trong khoảng thời gian
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Số lượng nhân viên active trong khoảng thời gian
     */
    long countActiveInDateRange(Date startDate, Date endDate);

    /**
     * Lấy thống kê nhân viên
     * @return Map chứa thống kê: total, active, inactive, recent
     */
    Map<String, Long> getEmployeeStatistics();

    /**
     * Lấy thống kê nhân viên với pagination context - NEW
     * @param pageable Thông tin phân trang hiện tại
     * @return Map chứa thống kê chi tiết
     */
    Map<String, Object> getEmployeeStatisticsWithContext(Pageable pageable);

    // ================== BUSINESS LOGIC METHODS ==================

    /**
     * Kiểm tra có thể xóa nhân viên không
     * @param id ID nhân viên
     * @return true nếu có thể xóa
     */
    boolean canDeleteNhanVien(Integer id);

    /**
     * Chuyển đổi trạng thái nhân viên (active/inactive)
     * @param id ID nhân viên
     */
    void toggleTrangThai(Integer id);

    /**
     * Lấy danh sách nhân viên hoạt động
     * @return Danh sách nhân viên có trạng thái = 1
     */
    List<NhanVien> getActiveNhanVien();

    /**
     * Lấy danh sách nhân viên hoạt động với pagination - NEW
     * @param pageable Thông tin phân trang
     * @return Page chứa nhân viên active
     */
    Page<NhanVien> getActiveNhanVienWithPagination(Pageable pageable);

    // ================== ADMIN DASHBOARD METHODS ==================

    /**
     * Lấy thống kê dashboard cho ADMIN
     * @return Map chứa thống kê dashboard
     */
    Map<String, Object> getAdminDashboardStats();

    /**
     * Lấy hoạt động gần đây cho ADMIN
     * @return Danh sách hoạt động gần đây
     */
    List<Map<String, Object>> getRecentActivities();

    /**
     * Lấy nhân viên mới trong X ngày
     * @param days Số ngày
     * @return Danh sách nhân viên mới
     */
    List<NhanVien> getNewEmployees(int days);

    /**
     * Lấy nhân viên cần ADMIN xem xét
     * @return Danh sách nhân viên cần review
     */
    List<NhanVien> getEmployeesNeedingReview();

    /**
     * Lấy nhân viên có hoạt động gần đây
     * @return Danh sách nhân viên active gần đây
     */
    List<NhanVien> getRecentlyActiveEmployees();

    // ================== CACHE MANAGEMENT - NEW ==================

    /**
     * Xóa cache liên quan đến nhân viên
     */
    void clearEmployeeCache();

    /**
     * Refresh cache cho một nhân viên cụ thể
     * @param employeeId ID nhân viên
     */
    void refreshEmployeeCache(Integer employeeId);

    // ================== EXPORT/IMPORT METHODS - NEW ==================

    /**
     * Xuất danh sách nhân viên ra Excel
     * @param searchCriteria Tiêu chí tìm kiếm để xuất
     * @return Byte array của file Excel
     */
    byte[] exportEmployeesToExcel(Map<String, Object> searchCriteria);

    /**
     * Lấy template Excel để import nhân viên
     * @return Byte array của template Excel
     */
    byte[] getEmployeeImportTemplate();

    // ================== PERFORMANCE MONITORING - NEW ==================

    /**
     * Lấy metrics hiệu suất của service
     * @return Map chứa metrics
     */
    Map<String, Object> getPerformanceMetrics();

    boolean existsBySdt(String sdt);
}
