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
     * TÃ¬m Ä‘á»‹a chá»‰ theo ID tÃ i khoáº£n (Spring Data JPA method)
     */
    List<DiaChi> findByTaiKhoan_Id(Integer taiKhoanId);

    /**
     * TÃ¬m Ä‘á»‹a chá»‰ theo ID tÃ i khoáº£n vá»›i sáº¯p xáº¿p
     */
    @Query("SELECT d FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId ORDER BY d.isDefault DESC, d.ngayTao DESC")
    List<DiaChi> findByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * TÃ¬m Ä‘á»‹a chá»‰ theo ID tÃ i khoáº£n vá»›i sáº¯p xáº¿p (JPA method name)
     */
    List<DiaChi> findByTaiKhoanIdOrderByIsDefaultDescNgayTaoDesc(Integer taiKhoanId);

    /**
     * TÃ¬m Ä‘á»‹a chá»‰ active theo ID tÃ i khoáº£n
     */
    @Query("SELECT d FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId AND d.trangThai = 1 ORDER BY d.isDefault DESC, d.ngayTao DESC")
    List<DiaChi> findActiveByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * TÃ¬m Ä‘á»‹a chá»‰ Ä‘áº§u tiÃªn (cÃ³ thá»ƒ lÃ m máº·c Ä‘á»‹nh)
     */
    @Query("SELECT d FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId AND d.trangThai = 1 ORDER BY d.ngayTao ASC")
    Optional<DiaChi> findFirstByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * TÃ¬m Ä‘á»‹a chá»‰ máº·c Ä‘á»‹nh theo ID tÃ i khoáº£n
     */
    @Query("SELECT d FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId AND d.isDefault = true AND d.trangThai = 1")
    Optional<DiaChi> findByTaiKhoanIdAndIsDefaultTrue(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * TÃ¬m Ä‘á»‹a chá»‰ máº·c Ä‘á»‹nh theo ID tÃ i khoáº£n (JPA method name)
     */
    DiaChi findByTaiKhoanIdAndIsDefault(Integer taiKhoanId, Boolean isDefault);

    /**
     * Äáº¿m sá»‘ Ä‘á»‹a chá»‰ máº·c Ä‘á»‹nh cá»§a 1 tÃ i khoáº£n
     */
    int countByTaiKhoanIdAndIsDefault(Integer taiKhoanId, Boolean isDefault);

    /**
     * TÃ¬m Ä‘á»‹a chá»‰ máº·c Ä‘á»‹nh (method tiá»‡n lá»£i)
     */
    default DiaChi findDefaultByTaiKhoanId(Integer taiKhoanId) {
        return findByTaiKhoanIdAndIsDefault(taiKhoanId, true);
    }

    // ===== SEARCH METHODS (2-level addressing: Tá»‰nh -> PhÆ°á»ng) =====

    /**
     * TÃ¬m kiáº¿m Ä‘á»‹a chá»‰ kÃ¨m thÃ´ng tin ngÆ°á»i dÃ¹ng (2-level: Tá»‰nh -> PhÆ°á»ng)
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
     * TÃ¬m kiáº¿m Ä‘Æ¡n giáº£n theo tá»« khÃ³a
     */
    @Query("SELECT d FROM DiaChi d WHERE " +
            "LOWER(d.tenPhuong) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.tenTinh) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.diaChiChiTiet) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DiaChi> searchByKeyword(@Param("keyword") String keyword);

    // ===== FIND BY LOCATION (2-level: Tá»‰nh/PhÆ°á»ng only) =====

    /**
     * TÃ¬m theo tÃªn tá»‰nh
     */
    List<DiaChi> findByTenTinhContainingIgnoreCase(String tenTinh);

    /**
     * TÃ¬m theo mÃ£ tá»‰nh
     */
    @Query("SELECT d FROM DiaChi d WHERE d.maTinh = :maTinh")
    List<DiaChi> findByMaTinh(@Param("maTinh") String maTinh);

    /**
     * TÃ¬m theo tÃªn phÆ°á»ng
     */
    @Query("SELECT d FROM DiaChi d WHERE LOWER(d.tenPhuong) LIKE LOWER(CONCAT('%', :tenPhuong, '%'))")
    List<DiaChi> findByTenPhuongContainingIgnoreCase(@Param("tenPhuong") String tenPhuong);

    /**
     * TÃ¬m theo mÃ£ phÆ°á»ng
     */
    @Query("SELECT d FROM DiaChi d WHERE d.maPhuong = :maPhuong")
    List<DiaChi> findByMaPhuong(@Param("maPhuong") String maPhuong);

    // ===== ADVANCED SEARCH =====

    /**
     * TÃ¬m kiáº¿m nÃ¢ng cao theo tá»‰nh vÃ  phÆ°á»ng
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
     * TÃ¬m Ä‘á»‹a chá»‰ theo tráº¡ng thÃ¡i
     */
    List<DiaChi> findByTrangThai(Integer trangThai);

    /**
     * Äáº¿m Ä‘á»‹a chá»‰ theo tráº¡ng thÃ¡i
     */
    long countByTrangThai(Integer trangThai);

    /**
     * Äáº¿m Ä‘á»‹a chá»‰ theo tÃ i khoáº£n vÃ  tráº¡ng thÃ¡i
     */
    @Query("SELECT COUNT(d) FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId AND d.trangThai = :trangThai")
    long countByTaiKhoanIdAndTrangThai(@Param("taiKhoanId") Integer taiKhoanId, @Param("trangThai") Integer trangThai);

    // ===== DEFAULT ADDRESS MANAGEMENT =====

    /**
     * Bá» táº¥t cáº£ Ä‘á»‹a chá»‰ máº·c Ä‘á»‹nh cá»§a má»™t tÃ i khoáº£n
     */
    @Modifying
    @Transactional
    @Query("UPDATE DiaChi d SET d.isDefault = false WHERE d.taiKhoan.id = :taiKhoanId")
    void clearDefaultAddressByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * Äáº·t Ä‘á»‹a chá»‰ lÃ m máº·c Ä‘á»‹nh
     */
    @Modifying
    @Transactional
    @Query("UPDATE DiaChi d SET d.isDefault = true WHERE d.id = :diaChiId")
    void setAsDefaultAddress(@Param("diaChiId") Integer diaChiId);

    // ===== DELETE OPERATIONS =====

    /**
     * XÃ³a Ä‘á»‹a chá»‰ theo ID tÃ i khoáº£n (hard delete)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId")
    void deleteByTaiKhoanIdQuery(@Param("taiKhoanId") Integer taiKhoanId);

    /**
     * XÃ³a Ä‘á»‹a chá»‰ theo ID tÃ i khoáº£n (JPA method)
     */
    @Modifying
    @Transactional
    void deleteByTaiKhoan_Id(Integer taiKhoanId);

    /**
     * Soft delete - VÃ´ hiá»‡u hÃ³a Ä‘á»‹a chá»‰ theo ID tÃ i khoáº£n
     */
    @Modifying
    @Transactional
    @Query("UPDATE DiaChi d SET d.trangThai = 0 WHERE d.taiKhoan.id = :taiKhoanId")
    void deactivateByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    // ===== STATISTICS =====

    /**
     * Thá»‘ng kÃª Ä‘á»‹a chá»‰ theo tá»‰nh
     */
    @Query("SELECT d.tenTinh, COUNT(d) FROM DiaChi d WHERE d.trangThai = 1 GROUP BY d.tenTinh ORDER BY COUNT(d) DESC")
    List<Object[]> getAddressStatisticsByProvince();

    /**
     * Kiá»ƒm tra tÃ i khoáº£n cÃ³ Ä‘á»‹a chá»‰ khÃ´ng
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DiaChi d WHERE d.taiKhoan.id = :taiKhoanId AND d.trangThai = 1")
    boolean hasActiveAddressByTaiKhoanId(@Param("taiKhoanId") Integer taiKhoanId);

    // ===== VALIDATION =====

    /**
     * Kiá»ƒm tra Ä‘á»‹a chá»‰ Ä‘Ã£ tá»“n táº¡i chÆ°a (trÃ¡nh trÃ¹ng láº·p)
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
