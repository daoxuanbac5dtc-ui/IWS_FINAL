package org.example.iws_websitesneaker.constants;

import java.math.BigDecimal;

public class BanHangConstants {

    // Tráº¡ng thÃ¡i hÃ³a Ä‘Æ¡n
    public static final String TRANG_THAI_HOA_DON_CHO = "CHO";
    public static final String TRANG_THAI_HOA_DON_HOAN_THANH = "HOAN_THANH";
    public static final String TRANG_THAI_HOA_DON_HUY = "HUY";
    public static final String TRANG_THAI_HOA_DON_XAC_NHAN = "XAC_NHAN";
    public static final String TRANG_THAI_HOA_DON_DANG_GIAO = "DANG_GIAO";
    public static final String TRANG_THAI_HOA_DON_DA_GIAO = "DA_GIAO";

    // Tráº¡ng thÃ¡i sáº£n pháº©m
    public static final int TRANG_THAI_ACTIVE = 1;
    public static final int TRANG_THAI_INACTIVE = 0;

    // Loáº¡i hÃ³a Ä‘Æ¡n
    public static final String LOAI_HOA_DON_OFFLINE = "OFFLINE";
    public static final String LOAI_HOA_DON_ONLINE = "ONLINE";

    // Loáº¡i khuyáº¿n mÃ£i
    public static final String LOAI_KHUYEN_MAI_PERCENT = "PERCENT";
    public static final String LOAI_KHUYEN_MAI_FIXED = "FIXED";

    // Loáº¡i voucher
    public static final String LOAI_VOUCHER_PERCENT = "PERCENT";
    public static final String LOAI_VOUCHER_FIXED = "FIXED";

    // PhÆ°Æ¡ng thá»©c thanh toÃ¡n
    public static final String PHUONG_THUC_TIEN_MAT = "TIEN_MAT";
    public static final String PHUONG_THUC_CHUYEN_KHOAN = "CHUYEN_KHOAN";
    public static final String PHUONG_THUC_DIEM = "DIEM";
    public static final String PHUONG_THUC_KET_HOP = "KET_HOP";

    // Vai trÃ² ngÆ°á»i dÃ¹ng
    public static final boolean VAI_TRO_ADMIN = true;
    public static final boolean VAI_TRO_CUSTOMER = false;

    // Sá»‘ lÆ°á»£ng tá»‘i Ä‘a hÃ³a Ä‘Æ¡n chá»
    public static final int MAX_HOA_DON_CHO = 5;

    // Sá»‘ lÆ°á»£ng sáº£n pháº©m gá»£i Ã½
    public static final int SO_LUONG_SAN_PHAM_GOI_Y = 8;

    // Thá»i gian háº¿t háº¡n hÃ³a Ä‘Æ¡n chá» (phÃºt)
    public static final int THOI_GIAN_HET_HAN_HOA_DON_CHO = 30;

    // Sá»‘ lÆ°á»£ng tá»‘i thiá»ƒu Ä‘á»ƒ cáº£nh bÃ¡o háº¿t hÃ ng
    public static final int SO_LUONG_TOI_THIEU_CANH_BAO = 5;

    // GiÃ¡ trá»‹ Ä‘iá»ƒm quy Ä‘á»•i
    public static final int TY_LE_DIEM_TIEN = 1000; // 1000 VND = 1 Ä‘iá»ƒm
    public static final int DIEM_TOI_THIEU_SU_DUNG = 100; // Tá»‘i thiá»ƒu 100 Ä‘iá»ƒm má»›i Ä‘Æ°á»£c sá»­ dá»¥ng

    // Giá»›i háº¡n sáº£n pháº©m
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
    public static final String MSG_REQUIRED = "TrÆ°á»ng nÃ y khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng";
    public static final String MSG_EMAIL_INVALID = "Email khÃ´ng há»£p lá»‡";
    public static final String MSG_PHONE_INVALID = "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng há»£p lá»‡";
    public static final String MSG_QUANTITY_INVALID = "Sá»‘ lÆ°á»£ng pháº£i lá»›n hÆ¡n 0";
    public static final String MSG_PRICE_INVALID = "GiÃ¡ pháº£i lá»›n hÆ¡n 0";

    // Private constructor to prevent instantiation
    private BanHangConstants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Kiá»ƒm tra tráº¡ng thÃ¡i hÃ³a Ä‘Æ¡n cÃ³ há»£p lá»‡ khÃ´ng
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
     * Kiá»ƒm tra loáº¡i khuyáº¿n mÃ£i cÃ³ há»£p lá»‡ khÃ´ng
     */
    public static boolean isValidKhuyenMaiType(String loai) {
        return LOAI_KHUYEN_MAI_PERCENT.equals(loai) || LOAI_KHUYEN_MAI_FIXED.equals(loai);
    }

    /**
     * Kiá»ƒm tra loáº¡i voucher cÃ³ há»£p lá»‡ khÃ´ng
     */
    public static boolean isValidVoucherType(String loai) {
        return LOAI_VOUCHER_PERCENT.equals(loai) || LOAI_VOUCHER_FIXED.equals(loai);
    }

    /**
     * Kiá»ƒm tra sá»‘ lÆ°á»£ng cÃ³ há»£p lá»‡ khÃ´ng
     */
    public static boolean isValidQuantity(Integer soLuong) {
        return soLuong != null && soLuong >= SO_LUONG_TOI_THIEU_MUA && soLuong <= SO_LUONG_TOI_DA_MUA;
    }

    /**
     * Kiá»ƒm tra file áº£nh cÃ³ há»£p lá»‡ khÃ´ng
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
     * Táº¡o mÃ£ QR cho sáº£n pháº©m
     */
    public static String generateQRCode(String maChiTiet) {
        return QR_PREFIX + maChiTiet + "_" + System.currentTimeMillis();
    }

    /**
     * TÃ­nh Ä‘iá»ƒm tá»« tiá»n
     */
    public static int tinhDiemTuTien(BigDecimal tien) {
        if (tien == null || tien.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return tien.divide(BigDecimal.valueOf(TY_LE_DIEM_TIEN), 0, BigDecimal.ROUND_DOWN).intValue();
    }

    /**
     * TÃ­nh tiá»n tá»« Ä‘iá»ƒm
     */
    public static BigDecimal tinhTienTuDiem(int diem) {
        if (diem <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(diem * TY_LE_DIEM_TIEN);
    }

    /**
     * Kiá»ƒm tra Ä‘iá»ƒm cÃ³ Ä‘á»§ Ä‘á»ƒ sá»­ dá»¥ng khÃ´ng
     */
    public static boolean isDiemDuDeSuDung(int diem) {
        return diem >= DIEM_TOI_THIEU_SU_DUNG;
    }
}
