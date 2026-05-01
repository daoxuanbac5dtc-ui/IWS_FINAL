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
@Table(name = "khach_hang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vi_diem", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ViDiem viDiem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tai_khoan", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TaiKhoan taiKhoan;

    @Column(name = "ma_khach_hang", length = 25, nullable = false, unique = true)
    private String maKhachHang;

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

    // ===== LIFECYCLE CALLBACKS =====

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
        this.hoTen = TextEncodingGuard.normalizeAndRejectCorrupted("Họ tên khách hàng", this.hoTen);
    }

    // ===== SAFE METHODS - Không gây LazyInitializationException =====

    /**
     * Lấy email một cách an toàn từ TaiKhoan
     */
    public String getEmailSafe() {
        try {
            return this.taiKhoan != null ? this.taiKhoan.getEmail() : null;
        } catch (Exception e) {
            System.err.println("Warning: Could not get email for customer " + this.id + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Kiểm tra khách hàng có đang hoạt động không
     */
    public boolean isActive() {
        return this.trangThai != null && this.trangThai == 1;
    }

    /**
     * Kiểm tra profile đã hoàn thiện chưa
     */
    public boolean isProfileCompleted() {
        return this.hoTen != null && !this.hoTen.trim().isEmpty() &&
                this.sdt != null && !this.sdt.trim().isEmpty() &&
                getEmailSafe() != null && !getEmailSafe().trim().isEmpty();
    }

    /**
     * Lấy tên hiển thị
     */
    public String getDisplayName() {
        if (this.hoTen != null && !this.hoTen.trim().isEmpty()) {
            return this.hoTen;
        }
        String email = getEmailSafe();
        if (email != null && !email.trim().isEmpty()) {
            return email;
        }
        return this.maKhachHang != null ? this.maKhachHang : "Khách hàng";
    }

    /**
     * Get display name cho trạng thái
     */
    public String getStatusDisplayName() {
        return this.isActive() ? "Đang hoạt động" : "Ngưng hoạt động";
    }

    // ===== BUSINESS LOGIC METHODS =====

    /**
     * Cập nhật thông tin cơ bản
     */
    public void updateBasicInfo(String hoTen, String sdt) {
        if (hoTen != null && !hoTen.trim().isEmpty()) {
            this.hoTen = hoTen.trim();
        }
        if (sdt != null && !sdt.trim().isEmpty()) {
            this.sdt = sdt.trim();
        }
        this.ngayCapNhat = new Date();
    }

    /**
     * Toggle trạng thái
     */
    public void toggleStatus() {
        this.trangThai = this.trangThai == 1 ? 0 : 1;
        this.ngayCapNhat = new Date();
    }

    /**
     * Deactivate khách hàng
     */
    public void deactivate() {
        this.trangThai = 0;
        this.ngayCapNhat = new Date();
    }

    /**
     * Reactivate khách hàng
     */
    public void reactivate() {
        this.trangThai = 1;
        this.ngayCapNhat = new Date();
    }

    // ===== VALIDATION METHODS =====

    /**
     * Validate số điện thoại
     */
    public boolean hasValidPhoneNumber() {
        return this.sdt != null && this.sdt.matches("^0\\d{9,10}$");
    }

    /**
     * Validate họ tên
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
                this.maKhachHang != null && !this.maKhachHang.trim().isEmpty() &&
                this.taiKhoan != null &&
                this.trangThai != null;
    }

    // Legacy method - để compatibility
    public void setIdDiaChi(Integer diaChiId) {
        // Empty implementation for compatibility
        // Địa chỉ giờ được quản lý qua TaiKhoan
    }

    // ===== SAFE TOSTRING =====
    @Override
    public String toString() {
        return "KhachHang{" +
                "id=" + id +
                ", maKhachHang='" + maKhachHang + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", sdt='" + sdt + '\'' +
                ", trangThai=" + trangThai +
                ", ngayTao=" + ngayTao +
                ", ngayCapNhat=" + ngayCapNhat +
                ", taiKhoanId=" + (taiKhoan != null ? taiKhoan.getId() : null) +
                '}';
    }

    // ===== EQUALS & HASHCODE =====
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        KhachHang other = (KhachHang) obj;

        if (this.id != null && other.id != null) {
            return this.id.equals(other.id);
        }

        if (this.maKhachHang != null && other.maKhachHang != null) {
            return this.maKhachHang.equals(other.maKhachHang);
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (this.id != null) {
            return this.id.hashCode();
        }
        if (this.maKhachHang != null) {
            return this.maKhachHang.hashCode();
        }
        return super.hashCode();
    }
}
