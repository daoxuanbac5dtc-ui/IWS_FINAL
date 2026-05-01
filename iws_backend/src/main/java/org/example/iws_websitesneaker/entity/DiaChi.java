package org.example.iws_websitesneaker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.iws_websitesneaker.util.TextEncodingGuard;

import java.util.Date;

@Entity
@Table(name = "dia_chi")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tai_khoan", referencedColumnName = "id")
    private TaiKhoan taiKhoan;

    @Column(name = "ma_tinh", length = 10)
    private String maTinh;

    @Column(name = "ma_phuong", length = 25, nullable = false)
    private String maPhuong;

    @Column(name = "ten_tinh", length = 100)
    private String tenTinh;

    @Column(name = "ten_phuong", length = 225, nullable = false)
    private String tenPhuong;

    @Column(name = "dia_chi_chi_tiet", length = 255)
    private String diaChiChiTiet;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "trang_thai")
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
            this.trangThai = 1; // Active by default
        }
        if (this.isDefault == null) {
            this.isDefault = false; // Not default by default
        }
    }

    @PreUpdate
    protected void onUpdate() {
        normalizeAndValidateText();
        this.ngayCapNhat = new Date();
    }

    private void normalizeAndValidateText() {
        this.tenTinh = TextEncodingGuard.normalizeAndRejectCorrupted("Tỉnh/TP", this.tenTinh);
        this.tenPhuong = TextEncodingGuard.normalizeAndRejectCorrupted("Phường/Xã", this.tenPhuong);
        this.diaChiChiTiet = TextEncodingGuard.normalizeAndRejectCorrupted("Địa chỉ chi tiết", this.diaChiChiTiet);
    }

    // ===== HELPER METHODS =====

    /**
     * Get full address string
     */
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();

        if (diaChiChiTiet != null && !diaChiChiTiet.trim().isEmpty()) {
            fullAddress.append(diaChiChiTiet).append(", ");
        }
        if (tenPhuong != null && !tenPhuong.trim().isEmpty()) {
            fullAddress.append(tenPhuong).append(", ");
        }
        if (tenTinh != null && !tenTinh.trim().isEmpty()) {
            fullAddress.append(tenTinh);
        }

        String result = fullAddress.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }

        return result.isEmpty() ? "Chưa có địa chỉ" : result;
    }

    /**
     * Check if address is active
     */
    public boolean isActive() {
        return this.trangThai != null && this.trangThai == 1;
    }

    /**
     * Check if this is default address
     */
    public boolean isDefaultAddress() {
        return this.isDefault != null && this.isDefault;
    }

    /**
     * Get display name for status
     */
    public String getStatusDisplayName() {
        return isActive() ? "Hoạt động" : "Không hoạt động";
    }

    /**
     * Safe toString method
     */
    @Override
    public String toString() {
        return "DiaChi{" +
                "id=" + id +
                ", maTinh='" + maTinh + '\'' +
                ", tenTinh='" + tenTinh + '\'' +
                ", maPhuong='" + maPhuong + '\'' +
                ", tenPhuong='" + tenPhuong + '\'' +
                ", diaChiChiTiet='" + diaChiChiTiet + '\'' +
                ", isDefault=" + isDefault +
                ", trangThai=" + trangThai +
                ", taiKhoanId=" + (taiKhoan != null ? taiKhoan.getId() : null) +
                '}';
    }
}
