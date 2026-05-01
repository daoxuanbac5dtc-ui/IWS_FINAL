package org.example.iws_websitesneaker.constants;

import java.math.BigDecimal;

public class BanHangConstants {

    // Trạng thái hóa đơn
    public static final String TRANG_THAI_HOA_DON_CHO = "CHO";
    public static final String TRANG_THAI_HOA_DON_HOAN_THANH = "HOAN_THANH";
    public static final String TRANG_THAI_HOA_DON_HUY = "HUY";
    public static final String TRANG_THAI_HOA_DON_XAC_NHAN = "XAC_NHAN";
    public static final String TRANG_THAI_HOA_DON_DANG_GIAO = "DANG_GIAO";
    public static final String TRANG_THAI_HOA_DON_DA_GIAO = "DA_GIAO";

    // Trạng thái sản phẩm
    public static final int TRANG_THAI_ACTIVE = 1;
    public static final int TRANG_THAI_INACTIVE = 0;

    // Loại hóa đơn
    public static final String LOAI_HOA_DON_OFFLINE = "OFFLINE";
    public static final String LOAI_HOA_DON_ONLINE = "ONLINE";

    // Loại khuyến mãi
    public static final String LOAI_KHUYEN_MAI_PERCENT = "PERCENT";
    public static final String LOAI_KHUYEN_MAI_FIXED = "FIXED";

    // Loại voucher
    public static final String LOAI_VOUCHER_PERCENT = "PERCENT";
    public static final String LOAI_VOUCHER_FIXED = "FIXED";

    // Phương thức thanh toán
    public static final String PHUONG_THUC_TIEN_MAT = "TIEN_MAT";
    public static final String PHUONG_THUC_CHUYEN_KHOAN = "CHUYEN_KHOAN";
    public static final String PHUONG_THUC_DIEM = "DIEM";
    public static final String PHUONG_THUC_KET_HOP = "KET_HOP";

    // Vai trò người dùng
    public static final boolean VAI_TRO_ADMIN = true;
    public static final boolean VAI_TRO_CUSTOMER = false;

    // Số lượng tối đa hóa đơn chờ
    public static final int MAX_HOA_DON_CHO = 5;

    // Số lượng sản phẩm gợi ý
    public static final int SO_LUONG_SAN_PHAM_GOI_Y = 8;

    // Thời gian hết hạn hóa đơn chờ (phút)
    public static final int THOI_GIAN_HET_HAN_HOA_DON_CHO = 30;

    // Số lượng tối thiểu để cảnh báo hết hàng
    public static final int SO_LUONG_TOI_THIEU_CANH_BAO = 5;

    // Giá trị điểm quy đổi
    public static final int TY_LE_DIEM_TIEN = 1000; // 1000 VND = 1 điểm
    public static final int DIEM_TOI_THIEU_SU_DUNG = 100; // Tối thiểu 100 điểm mới được sử dụng

    // Giới hạn sản phẩm
    public static final int SO_LUONG_TOI_DA_MUA = 999;
    public static final int SO_LUONG_TOI_THIEU_MUA = 1;

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_DIRECTION = "desc";
    public static final String DEFAULT_SORT_BY = "ngayTao";

    // File upload
    public static final String UPLOAD_PATH = "/uploads/";
    public static final String IMAGE_PATH = "/images/";
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif", "webp"};

    // QR Code
    public static final String QR_PREFIX = "SP_";
    public static final int QR_CODE_SIZE = 200;

    // Validation messages
    public static final String MSG_REQUIRED = "Trường này không được để trống";
    public static final String MSG_EMAIL_INVALID = "Email không hợp lệ";
    public static final String MSG_PHONE_INVALID = "Số điện thoại không hợp lệ";
    public static final String MSG_QUANTITY_INVALID = "Số lượng phải lớn hơn 0";
    public static final String MSG_PRICE_INVALID = "Giá phải lớn hơn 0";

    // Private constructor to prevent instantiation
    private BanHangConstants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Kiểm tra trạng thái hóa đơn có hợp lệ không
     */
    public static boolean isValidHoaDonStatus(String trangThai) {
        return TRANG_THAI_HOA_DON_CHO.equals(trangThai) ||
                TRANG_THAI_HOA_DON_HOAN_THANH.equals(trangThai) ||
                TRANG_THAI_HOA_DON_HUY.equals(trangThai) ||
                TRANG_THAI_HOA_DON_XAC_NHAN.equals(trangThai) ||
                TRANG_THAI_HOA_DON_DANG_GIAO.equals(trangThai) ||
                TRANG_THAI_HOA_DON_DA_GIAO.equals(trangThai);
    }

    /**
     * Kiểm tra loại khuyến mãi có hợp lệ không
     */
    public static boolean isValidKhuyenMaiType(String loai) {
        return LOAI_KHUYEN_MAI_PERCENT.equals(loai) || LOAI_KHUYEN_MAI_FIXED.equals(loai);
    }

    /**
     * Kiểm tra loại voucher có hợp lệ không
     */
    public static boolean isValidVoucherType(String loai) {
        return LOAI_VOUCHER_PERCENT.equals(loai) || LOAI_VOUCHER_FIXED.equals(loai);
    }

    /**
     * Kiểm tra số lượng có hợp lệ không
     */
    public static boolean isValidQuantity(Integer soLuong) {
        return soLuong != null && soLuong >= SO_LUONG_TOI_THIEU_MUA && soLuong <= SO_LUONG_TOI_DA_MUA;
    }

    /**
     * Kiểm tra file ảnh có hợp lệ không
     */
    public static boolean isValidImageType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tạo mã QR cho sản phẩm
     */
    public static String generateQRCode(String maChiTiet) {
        return QR_PREFIX + maChiTiet + "_" + System.currentTimeMillis();
    }

    /**
     * Tính điểm từ tiền
     */
    public static int tinhDiemTuTien(BigDecimal tien) {
        if (tien == null || tien.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return tien.divide(BigDecimal.valueOf(TY_LE_DIEM_TIEN), 0, BigDecimal.ROUND_DOWN).intValue();
    }

    /**
     * Tính tiền từ điểm
     */
    public static BigDecimal tinhTienTuDiem(int diem) {
        if (diem <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(diem * TY_LE_DIEM_TIEN);
    }

    /**
     * Kiểm tra điểm có đủ để sử dụng không
     */
    public static boolean isDiemDuDeSuDung(int diem) {
        return diem >= DIEM_TOI_THIEU_SU_DUNG;
    }
}
