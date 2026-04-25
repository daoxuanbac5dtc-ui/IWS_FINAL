package org.example.iws_websitesneaker.repository.BanHang;

import org.example.iws_websitesneaker.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienBHRepository extends JpaRepository<NhanVien, Integer> {

    // Method hiá»‡n táº¡i - tÃ¬m theo mÃ£ nhÃ¢n viÃªn
    Optional<NhanVien> findByMaNhanVienAndTrangThai(String maNhanVien, Integer trangThai);

    // âœ… THÃŠM: TÃ¬m theo mÃ£ tÃ i khoáº£n (vÃ¬ cÃ³ thá»ƒ Ä‘ang truyá»n nháº§m)
    Optional<NhanVien> findByTaiKhoan_MaTaiKhoanAndTrangThai(String maTaiKhoan, Integer trangThai);

    // âœ… THÃŠM: TÃ¬m linh hoáº¡t theo cáº£ mÃ£ nhÃ¢n viÃªn HOáº¶C mÃ£ tÃ i khoáº£n
    @Query("SELECT nv FROM NhanVien nv WHERE " +
            "(nv.maNhanVien = :ma OR nv.taiKhoan.maTaiKhoan = :ma) " +
            "AND nv.trangThai = :trangThai")
    Optional<NhanVien> findByMaNhanVienOrMaTaiKhoanAndTrangThai(
            @Param("ma") String ma,
            @Param("trangThai") Integer trangThai
    );

    // âœ… THÃŠM: TÃ¬m theo mÃ£ tÃ i khoáº£n chá»©a pattern
    List<NhanVien> findByTaiKhoan_MaTaiKhoanContainingIgnoreCase(String maTaiKhoan);

    List<NhanVien> findByMaNhanVienContaining(String maNhanVien);

    List<NhanVien> findByMaNhanVienContainingAndTrangThai(String maNhanVien, Integer trangThai);

    // CÃCH 3: DÃ¹ng native SQL náº¿u cáº§n
    @Query(value = "SELECT * FROM nhan_vien nv WHERE " +
            "nv.ma_nhan_vien LIKE CONCAT('%', :pattern, '%') " +
            "OR UPPER(nv.ma_nhan_vien) LIKE UPPER(CONCAT('%', :pattern, '%'))",
            nativeQuery = true)
    List<NhanVien> findByMaNhanVienPatternNative(@Param("pattern") String pattern);

    // Method kiá»ƒm tra tá»“n táº¡i
    boolean existsByMaNhanVienAndTrangThai(String maNhanVien, Integer trangThai);

    // Method tÃ¬m exact match vá»›i nhiá»u Ä‘iá»u kiá»‡n
    @Query("SELECT nv FROM NhanVien nv WHERE " +
            "(nv.maNhanVien = :maNhanVien " +
            "OR UPPER(nv.maNhanVien) = UPPER(:maNhanVien) " +
            "OR TRIM(nv.maNhanVien) = :maNhanVien) " +
            "AND nv.trangThai = :trangThai")
    List<NhanVien> findByMaNhanVienFlexible(
            @Param("maNhanVien") String maNhanVien,
            @Param("trangThai") Integer trangThai
    );
    /**
     * TÃ¬m nhÃ¢n viÃªn theo mÃ£ nhÃ¢n viÃªn
     */
    Optional<NhanVien> findByMaNhanVien(String maNhanVien);


    /**
     * TÃ¬m nhÃ¢n viÃªn theo email
     */
//    Optional<NhanVien> findByEmail(String email);

    /**
     * Láº¥y nhÃ¢n viÃªn Ä‘ang hoáº¡t Ä‘á»™ng
     */
    List<NhanVien> findByTrangThai(Integer trangThai);

    /**
     * Kiá»ƒm tra tá»“n táº¡i email
     */
//    Boolean existsByEmail(String email);

    // TÃ¬m theo mÃ£ nhÃ¢n viÃªn cÃ³ chá»©a (ignore case)
    List<NhanVien> findByMaNhanVienContainingIgnoreCase(String maNhanVien);


    // TÃ¬m kiáº¿m linh hoáº¡t vá»›i ignore case vÃ  trim
    @Query("SELECT nv FROM NhanVien nv WHERE " +
            "(UPPER(TRIM(nv.maNhanVien)) = UPPER(TRIM(:ma)) " +
            "OR UPPER(TRIM(nv.taiKhoan.maTaiKhoan)) = UPPER(TRIM(:ma))) " +
            "AND nv.trangThai = :trangThai")
    Optional<NhanVien> findByMaFlexibleAndTrangThai(
            @Param("ma") String ma,
            @Param("trangThai") Integer trangThai
    );

    // TÃ¬m theo pattern chá»©a trong mÃ£ nhÃ¢n viÃªn hoáº·c mÃ£ tÃ i khoáº£n
    @Query("SELECT nv FROM NhanVien nv WHERE " +
            "(nv.maNhanVien LIKE CONCAT('%', :pattern, '%') " +
            "OR nv.taiKhoan.maTaiKhoan LIKE CONCAT('%', :pattern, '%')) " +
            "AND nv.trangThai = :trangThai")
    List<NhanVien> findByMaPatternAndTrangThai(
            @Param("pattern") String pattern,
            @Param("trangThai") Integer trangThai
    );

    @Query("SELECT nv FROM NhanVien nv WHERE " +
            "nv.taiKhoan.vaiTro = :vaiTro " +
            "AND nv.trangThai = :trangThai")
    List<NhanVien> findByVaiTroNhanVienAndTrangThai(
            @Param("vaiTro") TaiKhoan.VaiTro vaiTro,
            @Param("trangThai") Integer trangThai
    );

    @Query("SELECT nv.maNhanVien FROM NhanVien nv WHERE nv.taiKhoan.maTaiKhoan = :maTaiKhoan")
    Optional<String> findMaNhanVienByMaTaiKhoan(@Param("maTaiKhoan") String maTaiKhoan);

    // ===== METHODS SEARCH NÃ‚NG CAO =====

    // TÃ¬m theo há» tÃªn
    List<NhanVien> findByHoTenContainingIgnoreCase(String hoTen);

    // TÃ¬m theo email
//    Optional<NhanVien> findByEmailAndTrangThai(String email, Integer trangThai);

    // TÃ¬m theo sá»‘ Ä‘iá»‡n thoáº¡i
    Optional<NhanVien> findBySdtAndTrangThai(String sdt, Integer trangThai);

    // TÃ¬m kiáº¿m toÃ n diá»‡n
    @Query("SELECT nv FROM NhanVien nv WHERE " +
            "(nv.maNhanVien LIKE CONCAT('%', :keyword, '%') " +
            "OR nv.taiKhoan.maTaiKhoan LIKE CONCAT('%', :keyword, '%') " +
            "OR nv.hoTen LIKE CONCAT('%', :keyword, '%') " +
            "OR nv.sdt LIKE CONCAT('%', :keyword, '%')) " +
            "AND nv.trangThai = :trangThai")
    List<NhanVien> searchByKeywordAndTrangThai(
            @Param("keyword") String keyword,
            @Param("trangThai") Integer trangThai
    );
}
