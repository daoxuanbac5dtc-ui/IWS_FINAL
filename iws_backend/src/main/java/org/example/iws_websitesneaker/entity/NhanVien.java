package org.example.iws_websitesneaker.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.iws_websitesneaker.util.TextEncodingGuard;

import java.util.Date;

@Entity
@Table(name = "nhan_vien")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// THÊM: Annotation để tránh lỗi lazy loading khi serialize JSON
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // GIỮ NGUYÊN: FetchType.LAZY vì sẽ dùng JOIN FETCH trong repository
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tai_khoan", referencedColumnName = "id")
    // THÊM: Annotation để tránh lỗi JSON serialization
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TaiKhoan taiKhoan;

    @Column(name = "ma_nhan_vien", length = 25, nullable = false, unique = true)
    private String maNhanVien;

    @Column(name = "ho_ten", length = 225, nullable = false)
    private String hoTen;

    @Column(name = "sdt", length = 10, nullable = false)
    private String sdt;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    // GIỮ NGUYÊN: Các method này
    public void setDiaChi(DiaChi diaChi) {
        // Implementation for setting address
    }

    public void setIdDiaChi(Integer diaChiId) {
        // Implementation for setting address ID
    }

    // ===== THÊM: LIFECYCLE CALLBACKS =====

    @PrePersist
    protected void onCreate() {
        normalizeAndValidateText();
        Date now = new Date();
        if (this.ngayTao == null) {
            this.ngayTao = now;
        }
        if (this.ngayCapNhat == null) {
            this.ngayCapNhat = now;
        }
        if (this.trangThai == null) {
            this.trangThai = 1; // Mặc định active
        }
    }

    @PreUpdate
    protected void onUpdate() {
        normalizeAndValidateText();
        this.ngayCapNhat = new Date();
    }

    private void normalizeAndValidateText() {
        this.hoTen = TextEncodingGuard.normalizeAndRejectCorrupted("Họ tên nhân viên", this.hoTen);
    }

    // ===== THÊM: SAFE METHODS - Không gây LazyInitializationException =====

    /**
     * Lấy email một cách an toàn từ TaiKhoan
     * QUAN TRỌNG: Method này không gây LazyInitializationException
     */
    public String getEmailSafe() {
        try {
            return this.taiKhoan != null ? this.taiKhoan.getEmail() : null;
        } catch (Exception e) {
            // Log warning nhưng không throw exception để tránh crash
            System.err.println("Warning: Could not get email for employee " + this.id + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Kiểm tra nhân viên có đang hoạt động không
     */
    public boolean isActive() {
        return this.trangThai != null && this.trangThai == 1;
    }

    /**
     * Kiểm tra có phải admin không (thông qua tài khoản)
     * SAFE METHOD - không gây LazyInitializationException
     */
    public boolean isAdminSafe() {
        try {
            return this.taiKhoan != null && this.taiKhoan.isAdmin();
        } catch (Exception e) {
            System.err.println("Warning: Could not check admin status for employee " + this.id + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy tên vai trò một cách an toàn
     */
    public String getRoleDisplayNameSafe() {
        try {
            return this.taiKhoan != null ? this.taiKhoan.getRoleDisplayName() : "Không xác định";
        } catch (Exception e) {
            System.err.println("Warning: Could not get role for employee " + this.id + ": " + e.getMessage());
            return "Không xác định";
        }
    }

    /**
     * Get display name cho trạng thái
     */
    public String getStatusDisplayName() {
        return this.isActive() ? "Đang làm việc" : "Nghỉ việc";
    }

    // ===== THÊM: BUSINESS LOGIC METHODS =====

    /**
     * Cập nhật thông tin cơ bản (không thay đổi tài khoản)
     */
    public void updateBasicInfo(String hoTen, String sdt, String maNhanVien) {
        if (hoTen != null && !hoTen.trim().isEmpty()) {
            this.hoTen = hoTen.trim();
        }
        if (sdt != null && !sdt.trim().isEmpty()) {
            this.sdt = sdt.trim();
        }
        if (maNhanVien != null && !maNhanVien.trim().isEmpty()) {
            this.maNhanVien = maNhanVien.trim();
        }
        this.ngayCapNhat = new Date();
    }

    /**
     * Toggle trạng thái hoạt động
     */
    public void toggleStatus() {
        this.trangThai = this.trangThai == 1 ? 0 : 1;
        this.ngayCapNhat = new Date();
    }

    /**
     * Deactivate nhân viên (soft delete)
     */
    public void deactivate() {
        this.trangThai = 0;
        this.ngayCapNhat = new Date();
    }

    /**
     * Reactivate nhân viên
     */
    public void reactivate() {
        this.trangThai = 1;
        this.ngayCapNhat = new Date();
    }

    // ===== THÊM: VALIDATION METHODS =====

    /**
     * Validate số điện thoại Việt Nam (10-11 số, bắt đầu bằng 0)
     */
    public boolean hasValidPhoneNumber() {
        return this.sdt != null && this.sdt.matches("^0\\d{9,10}$");
    }

    /**
     * Validate họ tên (chỉ chứa chữ cái và khoảng trắng, hỗ trợ tiếng Việt)
     */
    public boolean hasValidName() {
        return this.hoTen != null &&
                this.hoTen.matches("^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽềếểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵýỷỹ\\s]+$");
    }

    /**
     * Validate dữ liệu cơ bản
     */
    public boolean isValidForSave() {
        return this.hoTen != null && !this.hoTen.trim().isEmpty() &&
                this.sdt != null && !this.sdt.trim().isEmpty() &&
                this.maNhanVien != null && !this.maNhanVien.trim().isEmpty() &&
                this.taiKhoan != null &&
                this.trangThai != null;
    }

    // ===== THÊM: SAFE TOSTRING =====

    /**
     * toString an toàn - không truy cập lazy-loaded fields
     */
    @Override
    public String toString() {
        return "NhanVien{" +
                "id=" + id +
                ", maNhanVien='" + maNhanVien + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", sdt='" + sdt + '\'' +
                ", trangThai=" + trangThai +
                ", ngayTao=" + ngayTao +
                ", ngayCapNhat=" + ngayCapNhat +
                ", taiKhoanId=" + (taiKhoan != null ? taiKhoan.getId() : null) +
                '}';
    }

    // ===== THÊM: STATIC FACTORY METHODS =====

    /**
     * Tạo nhân viên mới từ tài khoản
     */
    public static NhanVien createFromTaiKhoan(TaiKhoan taiKhoan, String maNhanVien,
                                              String hoTen, String sdt) {
        if (taiKhoan == null || hoTen == null || sdt == null || maNhanVien == null) {
            throw new IllegalArgumentException("Các tham số bắt buộc không được null");
        }

        return NhanVien.builder()
                .taiKhoan(taiKhoan)
                .maNhanVien(maNhanVien)
                .hoTen(hoTen)
                .sdt(sdt)
                .trangThai(1)
                .ngayTao(new Date())
                .ngayCapNhat(new Date())
                .build();
    }

    // ===== THÊM: EQUALS & HASHCODE =====

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        NhanVien other = (NhanVien) obj;

        // So sánh theo ID nếu có
        if (this.id != null && other.id != null) {
            return this.id.equals(other.id);
        }

        // So sánh theo mã nhân viên
        if (this.maNhanVien != null && other.maNhanVien != null) {
            return this.maNhanVien.equals(other.maNhanVien);
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (this.id != null) {
            return this.id.hashCode();
        }
        if (this.maNhanVien != null) {
            return this.maNhanVien.hashCode();
        }
        return super.hashCode();
    }
}
