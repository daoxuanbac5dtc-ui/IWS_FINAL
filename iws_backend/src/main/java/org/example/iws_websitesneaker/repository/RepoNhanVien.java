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
     * TÃ¬m táº¥t cáº£ nhÃ¢n viÃªn vá»›i EAGER loading TaiKhoan vÃ  pagination
     */
    @Query(value = "SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan ORDER BY n.id DESC",
            countQuery = "SELECT COUNT(DISTINCT n) FROM NhanVien n")
    Page<NhanVien> findAllWithTaiKhoan(Pageable pageable);

    /**
     * TÃ¬m táº¥t cáº£ nhÃ¢n viÃªn vá»›i EAGER loading TaiKhoan (khÃ´ng pagination)
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan ORDER BY n.id DESC")
    List<NhanVien> findAllWithTaiKhoan();

    /**
     * MAIN SEARCH METHOD - TÃ¬m kiáº¿m vá»›i nhiá»u tiÃªu chÃ­ vÃ  pagination
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
     * TÃ¬m nhÃ¢n viÃªn theo ID vá»›i EAGER loading TaiKhoan
     */
    @Query("SELECT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE n.id = :id")
    Optional<NhanVien> findByIdWithTaiKhoan(@Param("id") Integer id);

    /**
     * TÃ¬m nhÃ¢n viÃªn theo tráº¡ng thÃ¡i vá»›i EAGER loading TaiKhoan vÃ  pagination
     */
    @Query(value = "SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE " +
            "(:trangThai IS NULL OR n.trangThai = :trangThai) ORDER BY n.id DESC",
            countQuery = "SELECT COUNT(DISTINCT n) FROM NhanVien n WHERE " +
                    "(:trangThai IS NULL OR n.trangThai = :trangThai)")
    Page<NhanVien> findByTrangThaiWithTaiKhoan(@Param("trangThai") Integer trangThai, Pageable pageable);

    /**
     * TÃ¬m nhÃ¢n viÃªn theo tráº¡ng thÃ¡i vá»›i EAGER loading TaiKhoan (khÃ´ng pagination)
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE n.trangThai = :trangThai ORDER BY n.id DESC")
    List<NhanVien> findByTrangThaiWithTaiKhoan(@Param("trangThai") Integer trangThai);

    /**
     * TÃ¬m nhÃ¢n viÃªn theo ID tÃ i khoáº£n vá»›i EAGER loading
     */
    @Query("SELECT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE n.taiKhoan.id = :taiKhoanId")
    Optional<NhanVien> findByTaiKhoanIdWithTaiKhoan(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * TÃ¬m nhÃ¢n viÃªn theo email vá»›i EAGER loading
     */
    @Query("SELECT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan tk WHERE tk.email = :email")
    Optional<NhanVien> findByTaiKhoanEmailWithTaiKhoan(@Param("email") String email);

    /**
     * TÃ¬m nhÃ¢n viÃªn theo mÃ£ nhÃ¢n viÃªn vá»›i EAGER loading
     */
    @Query("SELECT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE n.maNhanVien = :maNhanVien")
    Optional<NhanVien> findByMaNhanVienWithTaiKhoan(@Param("maNhanVien") String maNhanVien);

    // ===== DASHBOARD METHODS WITH EAGER LOADING =====

    /**
     * Láº¥y nhÃ¢n viÃªn má»›i trong khoáº£ng thá»i gian vá»›i EAGER loading
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE " +
            "n.ngayTao >= :fromDate ORDER BY n.ngayTao DESC")
    List<NhanVien> findNewEmployeesWithTaiKhoan(@Param("fromDate") Date fromDate);

    /**
     * Láº¥y nhÃ¢n viÃªn má»›i nháº¥t vá»›i EAGER loading
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan ORDER BY n.ngayTao DESC")
    List<NhanVien> findAllOrderByNgayTaoDescWithTaiKhoan();

    /**
     * TÃ¬m nhÃ¢n viÃªn cáº§n review vá»›i EAGER loading
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE " +
            "(n.hoTen IS NULL OR n.hoTen = '' OR " +
            "n.sdt IS NULL OR n.sdt = '' OR " +
            "n.maNhanVien IS NULL OR n.maNhanVien = '') AND n.trangThai = 1")
    List<NhanVien> findEmployeesNeedingReviewWithTaiKhoan();

    /**
     * TÃ¬m nhÃ¢n viÃªn cÃ³ hoáº¡t Ä‘á»™ng gáº§n Ä‘Ã¢y vá»›i EAGER loading
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan WHERE " +
            "n.ngayCapNhat >= :fromDate ORDER BY n.ngayCapNhat DESC")
    List<NhanVien> findRecentlyActiveEmployeesWithTaiKhoan(@Param("fromDate") Date fromDate);

    // ===== VALIDATION & CHECKING METHODS =====

    /**
     * Kiá»ƒm tra mÃ£ nhÃ¢n viÃªn Ä‘Ã£ tá»“n táº¡i
     */
    boolean existsByMaNhanVien(String maNhanVien);

    /**
     * Kiá»ƒm tra mÃ£ nhÃ¢n viÃªn Ä‘Ã£ tá»“n táº¡i (loáº¡i trá»« ID hiá»‡n táº¡i)
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NhanVien n WHERE " +
            "n.maNhanVien = :maNhanVien AND (:excludeId IS NULL OR n.id != :excludeId)")
    boolean existsByMaNhanVienExcludingId(@Param("maNhanVien") String maNhanVien, @Param("excludeId") Integer excludeId);

    /**
     * Kiá»ƒm tra sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NhanVien n WHERE " +
            "n.sdt = :sdt AND (:excludeId IS NULL OR n.id != :excludeId)")
    boolean existsBySdtExcludingId(@Param("sdt") String sdt, @Param("excludeId") Integer excludeId);

    /**
     * Kiá»ƒm tra cÃ³ thá»ƒ xÃ³a nhÃ¢n viÃªn khÃ´ng
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN false ELSE true END FROM NhanVien n WHERE " +
            "n.id = :id AND n.trangThai = 1")
    boolean canDeleteNhanVien(@Param("id") Integer id);

    // ===== COUNT & STATISTICS METHODS =====

    /**
     * Äáº¿m tá»•ng sá»‘ nhÃ¢n viÃªn
     */
    @Query("SELECT COUNT(n) FROM NhanVien n")
    long countAllEmployees();

    /**
     * Äáº¿m nhÃ¢n viÃªn theo tráº¡ng thÃ¡i
     */
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE n.trangThai = :trangThai")
    long countByTrangThai(@Param("trangThai") Integer trangThai);

    /**
     * Äáº¿m nhÃ¢n viÃªn Ä‘ang hoáº¡t Ä‘á»™ng
     */
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE n.trangThai = 1")
    long countActiveEmployees();

    /**
     * Äáº¿m nhÃ¢n viÃªn nghá»‰ viá»‡c
     */
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE n.trangThai = 0")
    long countInactiveEmployees();

    /**
     * Äáº¿m nhÃ¢n viÃªn má»›i trong khoáº£ng thá»i gian
     */
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE n.ngayTao BETWEEN :startDate AND :endDate")
    long countByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Äáº¿m nhÃ¢n viÃªn má»›i hÃ´m nay (Fixed version)
     */
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE " +
            "n.ngayTao >= :startOfDay AND n.ngayTao < :endOfDay")
    long countNewToday(@Param("startOfDay") Date startOfDay, @Param("endOfDay") Date endOfDay);

    /**
     * Äáº¿m nhÃ¢n viÃªn má»›i tuáº§n nÃ y
     */
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE n.ngayTao >= :startOfWeek")
    long countNewThisWeek(@Param("startOfWeek") Date startOfWeek);

    /**
     * Äáº¿m nhÃ¢n viÃªn má»›i thÃ¡ng nÃ y
     */
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE n.ngayTao >= :startOfMonth")
    long countNewThisMonth(@Param("startOfMonth") Date startOfMonth);

    /**
     * ALTERNATIVE: Äáº¿m nhÃ¢n viÃªn má»›i hÃ´m nay báº±ng cÃ¡ch khÃ¡c (Deprecated - use countNewToday instead)
     */
    @Deprecated
    @Query("SELECT COUNT(n) FROM NhanVien n WHERE " +
            "n.ngayTao >= :startOfDay AND n.ngayTao < :endOfDay")
    long countNewTodayAlternative(@Param("startOfDay") Date startOfDay, @Param("endOfDay") Date endOfDay);

    // ===== OPTIMIZED SEARCH METHODS FOR CONTROLLER =====

    /**
     * TÃ¬m kiáº¿m vá»›i tá»« khÃ³a (cho controller search)
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
     * TÃ¬m kiáº¿m nÃ¢ng cao (cho controller advanced search)
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
     * TÃ¬m theo ID tÃ i khoáº£n (compatibility)
     */
    Optional<NhanVien> findByTaiKhoan_Id(Integer taiKhoanId);

    /**
     * TÃ¬m theo mÃ£ nhÃ¢n viÃªn (compatibility)
     */
    Optional<NhanVien> findByMaNhanVien(String maNhanVien);

    /**
     * TÃ¬m theo mÃ£ nhÃ¢n viÃªn vÃ  tráº¡ng thÃ¡i (compatibility)
     */
    Optional<NhanVien> findByMaNhanVienAndTrangThai(String maNhanVien, Integer trangThai);

    /**
     * TÃ¬m theo tráº¡ng thÃ¡i (compatibility)
     */
    List<NhanVien> findByTrangThai(Integer trangThai);

    /**
     * TÃ¬m theo email tÃ i khoáº£n (compatibility)
     */
    @Query("SELECT n FROM NhanVien n WHERE n.taiKhoan.email = :email")
    Optional<NhanVien> findByTaiKhoanEmail(@Param("email") String email);

    /**
     * TÃ¬m theo mÃ£ tÃ i khoáº£n vÃ  tráº¡ng thÃ¡i (compatibility)
     */
    @Query("SELECT n FROM NhanVien n WHERE n.taiKhoan.maTaiKhoan = :maTaiKhoan AND n.trangThai = :trangThai")
    Optional<NhanVien> findByTaiKhoan_MaTaiKhoanAndTrangThai(@Param("maTaiKhoan") String maTaiKhoan, @Param("trangThai") Integer trangThai);

    /**
     * Láº¥y mÃ£ nhÃ¢n viÃªn theo mÃ£ tÃ i khoáº£n (compatibility)
     */
    @Query("SELECT n.maNhanVien FROM NhanVien n WHERE n.taiKhoan.maTaiKhoan = :maTaiKhoan")
    Optional<String> findMaNhanVienByMaTaiKhoan(@Param("maTaiKhoan") String maTaiKhoan);

    // ===== PERFORMANCE OPTIMIZATION QUERIES =====

    /**
     * Äáº¿m nhÃ¢n viÃªn vá»›i search criteria - cho pagination
     */
    @Query("SELECT COUNT(DISTINCT n) FROM NhanVien n LEFT JOIN n.taiKhoan tk WHERE " +
            "(:globalSearch IS NULL OR :globalSearch = '' OR " +
            "LOWER(COALESCE(n.hoTen, '')) LIKE LOWER(CONCAT('%', :globalSearch, '%')) OR " +
            "COALESCE(n.sdt, '') LIKE CONCAT('%', :globalSearch, '%') OR " +
            "COALESCE(n.maNhanVien, '') LIKE CONCAT('%', :globalSearch, '%')) AND " +
            "(:trangThai IS NULL OR n.trangThai = :trangThai)")
    long countBySearchCriteria(@Param("globalSearch") String globalSearch, @Param("trangThai") Integer trangThai);

    /**
     * Láº¥y IDs cá»§a nhÃ¢n viÃªn active cho batch operations
     */
    @Query("SELECT n.id FROM NhanVien n WHERE n.trangThai = 1")
    List<Integer> findAllActiveEmployeeIds();

    /**
     * Láº¥y IDs cá»§a nhÃ¢n viÃªn theo tráº¡ng thÃ¡i
     */
    @Query("SELECT n.id FROM NhanVien n WHERE n.trangThai = :trangThai")
    List<Integer> findEmployeeIdsByStatus(@Param("trangThai") Integer trangThai);

    // ===== SPECIALIZED DASHBOARD QUERIES =====

    /**
     * Láº¥y thá»‘ng kÃª theo thÃ¡ng
     */
    @Query("SELECT EXTRACT(MONTH FROM n.ngayTao) as month, COUNT(n) " +
            "FROM NhanVien n " +
            "WHERE EXTRACT(YEAR FROM n.ngayTao) = :year " +
            "GROUP BY EXTRACT(MONTH FROM n.ngayTao) " +
            "ORDER BY EXTRACT(MONTH FROM n.ngayTao)")
    List<Object[]> getMonthlyStatistics(@Param("year") Integer year);

    /**
     * Láº¥y thá»‘ng kÃª theo tráº¡ng thÃ¡i
     */
    @Query("SELECT n.trangThai, COUNT(n) FROM NhanVien n GROUP BY n.trangThai")
    List<Object[]> getStatusStatistics();

    /**
     * Láº¥y top 10 nhÃ¢n viÃªn má»›i nháº¥t
     */
    List<NhanVien> findTop10ByOrderByIdDesc();

    /**
     * Láº¥y nhÃ¢n viÃªn cÃ³ cáº­p nháº­t gáº§n Ä‘Ã¢y
     */
    @Query("SELECT DISTINCT n FROM NhanVien n LEFT JOIN FETCH n.taiKhoan " +
            "WHERE n.ngayCapNhat >= :fromDate " +
            "ORDER BY n.ngayCapNhat DESC")
    List<NhanVien> findRecentlyUpdatedEmployees(@Param("fromDate") Date fromDate);

    // ===== BULK OPERATIONS =====

    /**
     * Cáº­p nháº­t tráº¡ng thÃ¡i hÃ ng loáº¡t
     */
    @Query("UPDATE NhanVien n SET n.trangThai = :newStatus, n.ngayCapNhat = CURRENT_TIMESTAMP " +
            "WHERE n.id IN :ids")
    int bulkUpdateStatus(@Param("ids") List<Integer> ids, @Param("newStatus") Integer newStatus);

    /**
     * XÃ³a má»m hÃ ng loáº¡t
     */
    @Query("UPDATE NhanVien n SET n.trangThai = 0, n.ngayCapNhat = CURRENT_TIMESTAMP " +
            "WHERE n.id IN :ids AND n.trangThai = 1")
    int bulkSoftDelete(@Param("ids") List<Integer> ids);

    // ===== VALIDATION QUERIES =====

    /**
     * Kiá»ƒm tra email Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng bá»Ÿi tÃ i khoáº£n khÃ¡c
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NhanVien n " +
            "JOIN n.taiKhoan tk WHERE tk.email = :email AND " +
            "(:excludeId IS NULL OR n.id != :excludeId)")
    boolean existsByEmailExcludingId(@Param("email") String email, @Param("excludeId") Integer excludeId);

    /**
     * Láº¥y nhÃ¢n viÃªn cÃ³ trÃ¹ng láº·p mÃ£ nhÃ¢n viÃªn
     */
    @Query("SELECT n.maNhanVien, COUNT(n) FROM NhanVien n " +
            "WHERE n.maNhanVien IS NOT NULL " +
            "GROUP BY n.maNhanVien HAVING COUNT(n) > 1")
    List<Object[]> findDuplicateEmployeeCodes();

    /**
     * Láº¥y nhÃ¢n viÃªn cÃ³ trÃ¹ng láº·p sá»‘ Ä‘iá»‡n thoáº¡i
     */
    @Query("SELECT n.sdt, COUNT(n) FROM NhanVien n " +
            "WHERE n.sdt IS NOT NULL " +
            "GROUP BY n.sdt HAVING COUNT(n) > 1")
    List<Object[]> findDuplicatePhoneNumbers();

    // ===== EXPORT/REPORTING QUERIES =====

    /**
     * Láº¥y dá»¯ liá»‡u Ä‘á»ƒ export Excel
     */
    @Query("SELECT n.id, n.maNhanVien, n.hoTen, n.sdt, n.trangThai, " +
            "n.ngayTao, n.ngayCapNhat, tk.email " +
            "FROM NhanVien n LEFT JOIN n.taiKhoan tk " +
            "ORDER BY n.id")
    List<Object[]> findAllForExport();

    /**
     * Láº¥y dá»¯ liá»‡u vá»›i search criteria Ä‘á»ƒ export
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
