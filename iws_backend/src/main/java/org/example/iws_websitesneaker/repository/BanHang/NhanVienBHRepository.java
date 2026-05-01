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

    // Method hiện tại - tìm theo mã nhân viên
    Optional<NhanVien> findByMaNhanVienAndTrangThai(String maNhanVien, Integer trangThai);

    // ✅ THÊM: Tìm theo mã tài khoản (vì có thể đang truyền nhầm)
    Optional<NhanVien> findByTaiKhoan_MaTaiKhoanAndTrangThai(String maTaiKhoan, Integer trangThai);

    // ✅ THÊM: Tìm linh hoạt theo cả mã nhân viên HOẶC mã tài khoản
    @Query("SELECT nv FROM NhanVien nv WHERE " +
            "(nv.maNhanVien = :ma OR nv.taiKhoan.maTaiKhoan = :ma) " +
            "AND nv.trangThai = :trangThai")
    Optional<NhanVien> findByMaNhanVienOrMaTaiKhoanAndTrangThai(
            @Param("ma") String ma,
            @Param("trangThai") Integer trangThai
    );

    // ✅ THÊM: Tìm theo mã tài khoản chứa pattern
    List<NhanVien> findByTaiKhoan_MaTaiKhoanContainingIgnoreCase(String maTaiKhoan);

    List<NhanVien> findByMaNhanVienContaining(String maNhanVien);

    List<NhanVien> findByMaNhanVienContainingAndTrangThai(String maNhanVien, Integer trangThai);

    // CÁCH 3: Dùng native SQL nếu cần
    @Query(value = "SELECT * FROM nhan_vien nv WHERE " +
            "nv.ma_nhan_vien LIKE CONCAT('%', :pattern, '%') " +
            "OR UPPER(nv.ma_nhan_vien) LIKE UPPER(CONCAT('%', :pattern, '%'))",
            nativeQuery = true)
    List<NhanVien> findByMaNhanVienPatternNative(@Param("pattern") String pattern);

    // Method kiểm tra tồn tại
    boolean existsByMaNhanVienAndTrangThai(String maNhanVien, Integer trangThai);

    // Method tìm exact match với nhiều điều kiện
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
     * Tìm nhân viên theo mã nhân viên
     */
    Optional<NhanVien> findByMaNhanVien(String maNhanVien);


    /**
     * Tìm nhân viên theo email
     */
//    Optional<NhanVien> findByEmail(String email);

    /**
     * Lấy nhân viên đang hoạt động
     */
    List<NhanVien> findByTrangThai(Integer trangThai);

    /**
     * Kiểm tra tồn tại email
     */
//    Boolean existsByEmail(String email);

    // Tìm theo mã nhân viên có chứa (ignore case)
    List<NhanVien> findByMaNhanVienContainingIgnoreCase(String maNhanVien);


    // Tìm kiếm linh hoạt với ignore case và trim
    @Query("SELECT nv FROM NhanVien nv WHERE " +
            "(UPPER(TRIM(nv.maNhanVien)) = UPPER(TRIM(:ma)) " +
            "OR UPPER(TRIM(nv.taiKhoan.maTaiKhoan)) = UPPER(TRIM(:ma))) " +
            "AND nv.trangThai = :trangThai")
    Optional<NhanVien> findByMaFlexibleAndTrangThai(
            @Param("ma") String ma,
            @Param("trangThai") Integer trangThai
    );

    // Tìm theo pattern chứa trong mã nhân viên hoặc mã tài khoản
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

    // ===== METHODS SEARCH NÂNG CAO =====

    // Tìm theo họ tên
    List<NhanVien> findByHoTenContainingIgnoreCase(String hoTen);

    // Tìm theo email
//    Optional<NhanVien> findByEmailAndTrangThai(String email, Integer trangThai);

    // Tìm theo số điện thoại
    Optional<NhanVien> findBySdtAndTrangThai(String sdt, Integer trangThai);

    // Tìm kiếm toàn diện
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
