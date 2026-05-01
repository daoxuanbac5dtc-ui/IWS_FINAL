package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoDiaChi extends JpaRepository<DiaChi, Integer> {

    // ===== BASIC FIND METHODS =====

    /**
     * Tìm địa chỉ theo ID tài khoản (Spring Data JPA method)
     */
    List<DiaChi> findByTaiKhoan_Id(Integer taiKhoanId);

    /**
     * Tìm địa chỉ theo ID tài khoản với sắp xếp
     */
    @Query("SELECT d FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId ORDER BY d.isDefault DESC, d.ngayTao DESC")
    List<DiaChi> findByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * Tìm địa chỉ theo ID tài khoản với sắp xếp (JPA method name)
     */
    List<DiaChi> findByTaiKhoanIdOrderByIsDefaultDescNgayTaoDesc(Integer taiKhoanId);

    /**
     * Tìm địa chỉ active theo ID tài khoản
     */
    @Query("SELECT d FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId AND d.trangThai = 1 ORDER BY d.isDefault DESC, d.ngayTao DESC")
    List<DiaChi> findActiveByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * Tìm địa chỉ đầu tiên (có thể làm mặc định)
     */
    @Query("SELECT d FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId AND d.trangThai = 1 ORDER BY d.ngayTao ASC")
    Optional<DiaChi> findFirstByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * Tìm địa chỉ mặc định theo ID tài khoản
     */
    @Query("SELECT d FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId AND d.isDefault = true AND d.trangThai = 1")
    Optional<DiaChi> findByTaiKhoanIdAndIsDefaultTrue(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * Tìm địa chỉ mặc định theo ID tài khoản (JPA method name)
     */
    DiaChi findByTaiKhoanIdAndIsDefault(Integer taiKhoanId, Boolean isDefault);

    /**
     * Đếm số địa chỉ mặc định của 1 tài khoản
     */
    int countByTaiKhoanIdAndIsDefault(Integer taiKhoanId, Boolean isDefault);

    /**
     * Tìm địa chỉ mặc định (method tiện lợi)
     */
    default DiaChi findDefaultByTaiKhoanId(Integer taiKhoanId) {
        return findByTaiKhoanIdAndIsDefault(taiKhoanId, true);
    }

    // ===== SEARCH METHODS (2-level addressing: Tỉnh -> Phường) =====

    /**
     * Tìm kiếm địa chỉ kèm thông tin người dùng (2-level: Tỉnh -> Phường)
     */
    @Query("""
    SELECT d FROM DiaChi d
    LEFT JOIN d.taiKhoan tk
    LEFT JOIN KhachHang kh ON kh.taiKhoan.id = tk.id
    LEFT JOIN NhanVien nv ON nv.taiKhoan.id = tk.id
    WHERE
        (:keyword IS NULL OR :keyword = '' OR
        LOWER(d.tenTinh) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(d.tenPhuong) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(d.diaChiChiTiet) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(COALESCE(kh.hoTen, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(COALESCE(nv.hoTen, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
    ORDER BY d.ngayTao DESC
    """)
    List<DiaChi> searchDiaChiWithHoTenNguoiDung(@Param("keyword") String keyword);

    /**
     * Tìm kiếm đơn giản theo từ khóa
     */
    @Query("SELECT d FROM DiaChi d WHERE " +
            "LOWER(d.tenPhuong) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.tenTinh) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.diaChiChiTiet) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DiaChi> searchByKeyword(@Param("keyword") String keyword);

    // ===== FIND BY LOCATION (2-level: Tỉnh/Phường only) =====

    /**
     * Tìm theo tên tỉnh
     */
    List<DiaChi> findByTenTinhContainingIgnoreCase(String tenTinh);

    /**
     * Tìm theo mã tỉnh
     */
    @Query("SELECT d FROM DiaChi d WHERE d.maTinh = :maTinh")
    List<DiaChi> findByMaTinh(@Param("maTinh") String maTinh);

    /**
     * Tìm theo tên phường
     */
    @Query("SELECT d FROM DiaChi d WHERE LOWER(d.tenPhuong) LIKE LOWER(CONCAT('%', :tenPhuong, '%'))")
    List<DiaChi> findByTenPhuongContainingIgnoreCase(@Param("tenPhuong") String tenPhuong);

    /**
     * Tìm theo mã phường
     */
    @Query("SELECT d FROM DiaChi d WHERE d.maPhuong = :maPhuong")
    List<DiaChi> findByMaPhuong(@Param("maPhuong") String maPhuong);

    // ===== ADVANCED SEARCH =====

    /**
     * Tìm kiếm nâng cao theo tỉnh và phường
     */
    @Query("SELECT d FROM DiaChi d WHERE " +
            "(:maTinh IS NULL OR d.maTinh = :maTinh) AND " +
            "(:maPhuong IS NULL OR d.maPhuong = :maPhuong) AND " +
            "(:trangThai IS NULL OR d.trangThai = :trangThai)")
    List<DiaChi> findByAdvancedCriteria(@Param("maTinh") String maTinh,
                                        @Param("maPhuong") String maPhuong,
                                        @Param("trangThai") Integer trangThai);

    // ===== STATUS MANAGEMENT =====

    /**
     * Tìm địa chỉ theo trạng thái
     */
    List<DiaChi> findByTrangThai(Integer trangThai);

    /**
     * Đếm địa chỉ theo trạng thái
     */
    long countByTrangThai(Integer trangThai);

    /**
     * Đếm địa chỉ theo tài khoản và trạng thái
     */
    @Query("SELECT COUNT(d) FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId AND d.trangThai = :trangThai")
    long countByTaiKhoanIdAndTrangThai(@Param("taiKhoanId") Integer taiKhoanId, @Param("trangThai") Integer trangThai);

    // ===== DEFAULT ADDRESS MANAGEMENT =====

    /**
     * Bỏ tất cả địa chỉ mặc định của một tài khoản
     */
    @Modifying
    @Transactional
    @Query("UPDATE DiaChi d SET d.isDefault = false WHERE d.taiKhoan.id = :taiKhoanId")
    void clearDefaultAddressByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * Đặt địa chỉ làm mặc định
     */
    @Modifying
    @Transactional
    @Query("UPDATE DiaChi d SET d.isDefault = true WHERE d.id = :diaChiId")
    void setAsDefaultAddress(@Param("diaChiId") Integer diaChiId);

    // ===== DELETE OPERATIONS =====

    /**
     * Xóa địa chỉ theo ID tài khoản (hard delete)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId")
    void deleteByTaiKhoanIdQuery(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * Xóa địa chỉ theo ID tài khoản (JPA method)
     */
    @Modifying
    @Transactional
    void deleteByTaiKhoan_Id(Integer taiKhoanId);

    /**
     * Soft delete - Vô hiệu hóa địa chỉ theo ID tài khoản
     */
    @Modifying
    @Transactional
    @Query("UPDATE DiaChi d SET d.trangThai = 0 WHERE d.taiKhoan.id = :taiKhoanId")
    void deactivateByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    // ===== STATISTICS =====

    /**
     * Thống kê địa chỉ theo tỉnh
     */
    @Query("SELECT d.tenTinh, COUNT(d) FROM DiaChi d WHERE d.trangThai = 1 GROUP BY d.tenTinh ORDER BY COUNT(d) DESC")
    List<Object[]> getAddressStatisticsByProvince();

    /**
     * Kiểm tra tài khoản có địa chỉ không
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId AND d.trangThai = 1")
    boolean hasActiveAddressByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    // ===== VALIDATION =====

    /**
     * Kiểm tra địa chỉ đã tồn tại chưa (tránh trùng lặp)
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DiaChi d WHERE " +
            "d.taiKhoan.id = :taiKhoanId AND " +
            "d.maTinh = :maTinh AND " +
            "d.maPhuong = :maPhuong AND " +
            "LOWER(TRIM(d.diaChiChiTiet)) = LOWER(TRIM(:diaChiChiTiet)) AND " +
            "d.trangThai = 1 AND " +
            "(:excludeId IS NULL OR d.id != :excludeId)")
    boolean existsDuplicateAddress(@Param("taiKhoanId") Integer taiKhoanId,
                                   @Param("maTinh") String maTinh,
                                   @Param("maPhuong") String maPhuong,
                                   @Param("diaChiChiTiet") String diaChiChiTiet,
                                   @Param("excludeId") Integer excludeId);
}
