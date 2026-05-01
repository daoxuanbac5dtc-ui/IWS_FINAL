package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepoNhanVien extends JpaRepository<NhanVien, Integer> {

    // ===== MAIN QUERIES WITH EAGER LOADING & PAGINATION =====

    /**
     * Tìm tất cả nhân viên với EAGER loading TaiKhoan và pagination
     */
    @Query(value = "SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan ORDER BY n.id DESC",
            countQuery = "SELECT COUNT(DISTINCT n) FROM NhanVien n")
    Page<NhanVien> findAllWithTaiKhoan(Pageable pageable);

    /**
     * Tìm tất cả nhân viên với EAGER loading TaiKhoan (không pagination)
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan ORDER BY n.id DESC")
    List<NhanVien> findAllWithTaiKhoan();

    /**
     * MAIN SEARCH METHOD - Tìm kiếm với nhiều tiêu chí và pagination
     */
    @Query(value = "SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan tk WHERE " +
            "(:globalSearch IS NULL OR :globalSearch = '' OR " +
            "LOWER(COALESCE(n.hoTen, '')) LIKE LOWER(CONCAT('%', :globalSearch, '%')) OR " +
            "COALESCE(n.sdt, '') LIKE CONCAT('%', :globalSearch, '%') OR " +
            "COALESCE(n.maNhanVien, '') LIKE CONCAT('%', :globalSearch, '%') OR " +
            "LOWER(COALESCE(tk.email, '')) LIKE LOWER(CONCAT('%', :globalSearch, '%')) OR " +
            "CAST(n.id AS string) LIKE CONCAT('%', :globalSearch, '%')) AND " +
            "(:trangThai IS NULL OR n.trangThai = :trangThai) AND " +
            "(:startDate IS NULL OR n.ngayTao >= :startDate) AND " +
            "(:endDate IS NULL OR n.ngayTao <= :endDate) " +
            "ORDER BY n.id DESC",
            countQuery = "SELECT COUNT(DISTINCT n) FROM NhanVien n LEFT JOIN n.taiKhoan tk WHERE " +
                    "(:globalSearch IS NULL OR :globalSearch = '' OR " +
                    "LOWER(COALESCE(n.hoTen, '')) LIKE LOWER(CONCAT('%', :globalSearch, '%')) OR " +
                    "COALESCE(n.sdt, '') LIKE CONCAT('%', :globalSearch, '%') OR " +
                    "COALESCE(n.maNhanVien, '') LIKE CONCAT('%', :globalSearch, '%') OR " +
                    "LOWER(COALESCE(tk.email, '')) LIKE LOWER(CONCAT('%', :globalSearch, '%')) OR " +
                    "CAST(n.id AS string) LIKE CONCAT('%', :globalSearch, '%')) AND " +
                    "(:trangThai IS NULL OR n.trangThai = :trangThai) AND " +
                    "(:startDate IS NULL OR n.ngayTao >= :startDate) AND " +
                    "(:endDate IS NULL OR n.ngayTao <= :endDate)")
    Page<NhanVien> searchWithCriteriaAndPagination(
            @Param("globalSearch") String globalSearch,
            @Param("trangThai") Integer trangThai,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            Pageable pageable);

    /**
     * Tìm nhân viên theo ID với EAGER loading TaiKhoan
     */
    @Query("SELECT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE n.id = :id")
    Optional<NhanVien> findByIdWithTaiKhoan(@Param("id") Integer id);

    /**
     * Tìm nhân viên theo trạng thái với EAGER loading TaiKhoan và pagination
     */
    @Query(value = "SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE " +
            "(:trangThai IS NULL OR n.trangThai = :trangThai) ORDER BY n.id DESC",
            countQuery = "SELECT COUNT(DISTINCT n) FROM NhanVien n WHERE " +
                    "(:trangThai IS NULL OR n.trangThai = :trangThai)")
    Page<NhanVien> findByTrangThaiWithTaiKhoan(@Param("trangThai") Integer trangThai, Pageable pageable);

    /**
     * Tìm nhân viên theo trạng thái với EAGER loading TaiKhoan (không pagination)
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE n.trangThai = :trangThai ORDER BY n.id DESC")
    List<NhanVien> findByTrangThaiWithTaiKhoan(@Param("trangThai") Integer trangThai);

    /**
     * Tìm nhân viên theo ID tài khoản với EAGER loading
     */
    @Query("SELECT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE n.taiKhoan.id = :taiKhoanId")
    Optional<NhanVien> findByTaiKhoanIdWithTaiKhoan(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * Tìm nhân viên theo email với EAGER loading
     */
    @Query("SELECT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan tk WHERE tk.email = :email")
    Optional<NhanVien> findByTaiKhoanEmailWithTaiKhoan(@Param("email") String email);

    /**
     * Tìm nhân viên theo mã nhân viên với EAGER loading
     */
    @Query("SELECT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE n.maNhanVien = :maNhanVien")
    Optional<NhanVien> findByMaNhanVienWithTaiKhoan(@Param("maNhanVien") String maNhanVien);

    // ===== DASHBOARD METHODS WITH EAGER LOADING =====

    /**
     * Lấy nhân viên mới trong khoảng thời gian với EAGER loading
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE " +
            "n.ngayTao >= :fromDate ORDER BY n.ngayTao DESC")
    List<NhanVien> findNewEmployeesWithTaiKhoan(@Param("fromDate") Date fromDate);

    /**
     * Lấy nhân viên mới nhất với EAGER loading
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan ORDER BY n.ngayTao DESC")
    List<NhanVien> findAllOrderByNgayTaoDescWithTaiKhoan();

    /**
     * Tìm nhân viên cần review với EAGER loading
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE " +
            "(n.hoTen IS NULL OR n.hoTen = '' OR " +
            "n.sdt IS NULL OR n.sdt = '' OR " +
            "n.maNhanVien IS NULL OR n.maNhanVien = '') AND n.trangThai = 1")
    List<NhanVien> findEmployeesNeedingReviewWithTaiKhoan();

    /**
     * Tìm nhân viên có hoạt động gần đây với EAGER loading
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE " +
            "n.ngayCapNhat >= :fromDate ORDER BY n.ngayCapNhat DESC")
    List<NhanVien> findRecentlyActiveEmployeesWithTaiKhoan(@Param("fromDate") Date fromDate);

    // ===== VALIDATION & CHECKING METHODS =====

    /**
     * Kiểm tra mã nhân viên đã tồn tại
     */
    boolean existsByMaNhanVien(String maNhanVien);

    /**
     * Kiểm tra mã nhân viên đã tồn tại (loại trừ ID hiện tại)
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NhanVien n WHERE " +
            "n.maNhanVien = :maNhanVien AND (:excludeId IS NULL OR n.id != :excludeId)")
    boolean existsByMaNhanVienExcludingId(@Param("maNhanVien") String maNhanVien, @Param("excludeId") Integer excludeId);

    /**
     * Kiểm tra số điện thoại đã được sử dụng
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NhanVien n WHERE " +
            "n.sdt = :sdt AND (:excludeId IS NULL OR n.id != :excludeId)")
    boolean existsBySdtExcludingId(@Param("sdt") String sdt, @Param("excludeId") Integer excludeId);

    /**
     * Kiểm tra có thể xóa nhân viên không
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN false ELSE true END FROM NhanVien n WHERE " +
            "n.id = :id AND n.trangThai = 1")
    boolean canDeleteNhanVien(@Param("id") Integer id);

    // ===== COUNT & STATISTICS METHODS =====

    /**
     * Đếm tổng số nhân viên
     */
    @Query("SELECT COUNT(n) FROM NhanVien n")
    long countAllEmployees();

    /**
     * Đếm nhân viên theo trạng thái
     */
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE n.trangThai = :trangThai")
    long countByTrangThai(@Param("trangThai") Integer trangThai);

    /**
     * Đếm nhân viên đang hoạt động
     */
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE n.trangThai = 1")
    long countActiveEmployees();

    /**
     * Đếm nhân viên nghỉ việc
     */
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE n.trangThai = 0")
    long countInactiveEmployees();

    /**
     * Đếm nhân viên mới trong khoảng thời gian
     */
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE n.ngayTao BETWEEN :startDate AND :endDate")
    long countByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Đếm nhân viên mới hôm nay (Fixed version)
     */
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE " +
            "n.ngayTao >= :startOfDay AND n.ngayTao < :endOfDay")
    long countNewToday(@Param("startOfDay") Date startOfDay, @Param("endOfDay") Date endOfDay);

    /**
     * Đếm nhân viên mới tuần này
     */
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE n.ngayTao >= :startOfWeek")
    long countNewThisWeek(@Param("startOfWeek") Date startOfWeek);

    /**
     * Đếm nhân viên mới tháng này
     */
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE n.ngayTao >= :startOfMonth")
    long countNewThisMonth(@Param("startOfMonth") Date startOfMonth);

    /**
     * ALTERNATIVE: Đếm nhân viên mới hôm nay bằng cách khác (Deprecated - use countNewToday instead)
     */
    @Deprecated
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE " +
            "n.ngayTao >= :startOfDay AND n.ngayTao < :endOfDay")
    long countNewTodayAlternative(@Param("startOfDay") Date startOfDay, @Param("endOfDay") Date endOfDay);

    // ===== OPTIMIZED SEARCH METHODS FOR CONTROLLER =====

    /**
     * Tìm kiếm với từ khóa (cho controller search)
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan tk WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(COALESCE(n.hoTen, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "COALESCE(n.sdt, '') LIKE CONCAT('%', :keyword, '%') OR " +
            "COALESCE(n.maNhanVien, '') LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(COALESCE(tk.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(n.id AS string) LIKE CONCAT('%', :keyword, '%'))")
    List<NhanVien> searchByKeywordWithTaiKhoan(@Param("keyword") String keyword);

    /**
     * Tìm kiếm nâng cao (cho controller advanced search)
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan tk WHERE " +
            "(:hoTen IS NULL OR :hoTen = '' OR LOWER(COALESCE(n.hoTen, '')) LIKE LOWER(CONCAT('%', :hoTen, '%'))) AND " +
            "(:email IS NULL OR :email = '' OR LOWER(COALESCE(tk.email, '')) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:sdt IS NULL OR :sdt = '' OR COALESCE(n.sdt, '') LIKE CONCAT('%', :sdt, '%')) AND " +
            "(:maNhanVien IS NULL OR :maNhanVien = '' OR LOWER(COALESCE(n.maNhanVien, '')) LIKE LOWER(CONCAT('%', :maNhanVien, '%'))) AND " +
            "(:trangThai IS NULL OR n.trangThai = :trangThai) AND " +
            "(:startDate IS NULL OR n.ngayTao >= :startDate) AND " +
            "(:endDate IS NULL OR n.ngayTao <= :endDate)")
    List<NhanVien> searchAdvancedWithTaiKhoan(
            @Param("hoTen") String hoTen,
            @Param("email") String email,
            @Param("sdt") String sdt,
            @Param("maNhanVien") String maNhanVien,
            @Param("trangThai") Integer trangThai,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    // ===== BACKWARD COMPATIBILITY METHODS =====

    /**
     * Tìm theo ID tài khoản (compatibility)
     */
    Optional<NhanVien> findByTaiKhoan_Id(Integer taiKhoanId);

    /**
     * Tìm theo mã nhân viên (compatibility)
     */
    Optional<NhanVien> findByMaNhanVien(String maNhanVien);

    /**
     * Tìm theo mã nhân viên và trạng thái (compatibility)
     */
    Optional<NhanVien> findByMaNhanVienAndTrangThai(String maNhanVien, Integer trangThai);

    /**
     * Tìm theo trạng thái (compatibility)
     */
    List<NhanVien> findByTrangThai(Integer trangThai);

    /**
     * Tìm theo email tài khoản (compatibility)
     */
    @Query("SELECT n FROM NhanVien n WHERE n.taiKhoan.email = :email")
    Optional<NhanVien> findByTaiKhoanEmail(@Param("email") String email);

    /**
     * Tìm theo mã tài khoản và trạng thái (compatibility)
     */
    @Query("SELECT n FROM NhanVien n WHERE n.taiKhoan.maTaiKhoan = :maTaiKhoan AND n.trangThai = :trangThai")
    Optional<NhanVien> findByTaiKhoan_MaTaiKhoanAndTrangThai(@Param("maTaiKhoan") String maTaiKhoan, @Param("trangThai") Integer trangThai);

    /**
     * Lấy mã nhân viên theo mã tài khoản (compatibility)
     */
    @Query("SELECT n.maNhanVien FROM NhanVien n WHERE n.taiKhoan.maTaiKhoan = :maTaiKhoan")
    Optional<String> findMaNhanVienByMaTaiKhoan(@Param("maTaiKhoan") String maTaiKhoan);

    // ===== PERFORMANCE OPTIMIZATION QUERIES =====

    /**
     * Đếm nhân viên với search criteria - cho pagination
     */
    @Query("SELECT COUNT(DISTINCT n) FROM NhanVien n LEFT JOIN n.taiKhoan tk WHERE " +
            "(:globalSearch IS NULL OR :globalSearch = '' OR " +
            "LOWER(COALESCE(n.hoTen, '')) LIKE LOWER(CONCAT('%', :globalSearch, '%')) OR " +
            "COALESCE(n.sdt, '') LIKE CONCAT('%', :globalSearch, '%') OR " +
            "COALESCE(n.maNhanVien, '') LIKE CONCAT('%', :globalSearch, '%')) AND " +
            "(:trangThai IS NULL OR n.trangThai = :trangThai)")
    long countBySearchCriteria(@Param("globalSearch") String globalSearch, @Param("trangThai") Integer trangThai);

    /**
     * Lấy IDs của nhân viên active cho batch operations
     */
    @Query("SELECT n.id FROM NhanVien n WHERE n.trangThai = 1")
    List<Integer> findAllActiveEmployeeIds();

    /**
     * Lấy IDs của nhân viên theo trạng thái
     */
    @Query("SELECT n.id FROM NhanVien n WHERE n.trangThai = :trangThai")
    List<Integer> findEmployeeIdsByStatus(@Param("trangThai") Integer trangThai);

    // ===== SPECIALIZED DASHBOARD QUERIES =====

    /**
     * Lấy thống kê theo tháng
     */
    @Query("SELECT EXTRACT(MONTH FROM n.ngayTao) as month, COUNT(n) " +
            "FROM NhanVien n " +
            "WHERE EXTRACT(YEAR FROM n.ngayTao) = :year " +
            "GROUP BY EXTRACT(MONTH FROM n.ngayTao) " +
            "ORDER BY EXTRACT(MONTH FROM n.ngayTao)")
    List<Object[]> getMonthlyStatistics(@Param("year") Integer year);

    /**
     * Lấy thống kê theo trạng thái
     */
    @Query("SELECT n.trangThai, COUNT(n) FROM NhanVien n GROUP BY n.trangThai")
    List<Object[]> getStatusStatistics();

    /**
     * Lấy top 10 nhân viên mới nhất
     */
    List<NhanVien> findTop10ByOrderByIdDesc();

    /**
     * Lấy nhân viên có cập nhật gần đây
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan " +
            "WHERE n.ngayCapNhat >= :fromDate " +
            "ORDER BY n.ngayCapNhat DESC")
    List<NhanVien> findRecentlyUpdatedEmployees(@Param("fromDate") Date fromDate);

    // ===== BULK OPERATIONS =====

    /**
     * Cập nhật trạng thái hàng loạt
     */
    @Query("UPDATE NhanVien n SET n.trangThai = :newStatus, n.ngayCapNhat = CURRENT_TIMESTAMP " +
            "WHERE n.id IN :ids")
    int bulkUpdateStatus(@Param("ids") List<Integer> ids, @Param("newStatus") Integer newStatus);

    /**
     * Xóa mềm hàng loạt
     */
    @Query("UPDATE NhanVien n SET n.trangThai = 0, n.ngayCapNhat = CURRENT_TIMESTAMP " +
            "WHERE n.id IN :ids AND n.trangThai = 1")
    int bulkSoftDelete(@Param("ids") List<Integer> ids);

    // ===== VALIDATION QUERIES =====

    /**
     * Kiểm tra email đã được sử dụng bởi tài khoản khác
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NhanVien n " +
            "JOIN n.taiKhoan tk WHERE tk.email = :email AND " +
            "(:excludeId IS NULL OR n.id != :excludeId)")
    boolean existsByEmailExcludingId(@Param("email") String email, @Param("excludeId") Integer excludeId);

    /**
     * Lấy nhân viên có trùng lặp mã nhân viên
     */
    @Query("SELECT n.maNhanVien, COUNT(n) FROM NhanVien n " +
            "WHERE n.maNhanVien IS NOT NULL " +
            "GROUP BY n.maNhanVien HAVING COUNT(n) > 1")
    List<Object[]> findDuplicateEmployeeCodes();

    /**
     * Lấy nhân viên có trùng lặp số điện thoại
     */
    @Query("SELECT n.sdt, COUNT(n) FROM NhanVien n " +
            "WHERE n.sdt IS NOT NULL " +
            "GROUP BY n.sdt HAVING COUNT(n) > 1")
    List<Object[]> findDuplicatePhoneNumbers();

    // ===== EXPORT/REPORTING QUERIES =====

    /**
     * Lấy dữ liệu để export Excel
     */
    @Query("SELECT n.id, n.maNhanVien, n.hoTen, n.sdt, n.trangThai, " +
            "n.ngayTao, n.ngayCapNhat, tk.email " +
            "FROM NhanVien n LEFT JOIN n.taiKhoan tk " +
            "ORDER BY n.id")
    List<Object[]> findAllForExport();

    /**
     * Lấy dữ liệu với search criteria để export
     */
    @Query("SELECT n.id, n.maNhanVien, n.hoTen, n.sdt, n.trangThai, " +
            "n.ngayTao, n.ngayCapNhat, tk.email " +
            "FROM NhanVien n LEFT JOIN n.taiKhoan tk WHERE " +
            "(:globalSearch IS NULL OR :globalSearch = '' OR " +
            "LOWER(COALESCE(n.hoTen, '')) LIKE LOWER(CONCAT('%', :globalSearch, '%'))) AND " +
            "(:trangThai IS NULL OR n.trangThai = :trangThai) " +
            "ORDER BY n.id")
    List<Object[]> findBySearchCriteriaForExport(
            @Param("globalSearch") String globalSearch,
            @Param("trangThai") Integer trangThai);

    boolean existsBySdt(String sdt);
}
