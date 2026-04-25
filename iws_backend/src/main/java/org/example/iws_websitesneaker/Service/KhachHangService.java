package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.KhachHangDto;
import org.example.iws_websitesneaker.entity.KhachHang;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface dá»‹ch vá»¥ quáº£n lÃ½ KhÃ¡ch hÃ ng
 * Äá»‹nh nghÄ©a cÃ¡c phÆ°Æ¡ng thá»©c CRUD vÃ  tÃ¬m kiáº¿m cho khÃ¡ch hÃ ng
 */
public interface KhachHangService {

    // ================== CÃC THAO TÃC CRUD CÆ  Báº¢N ==================

    /**
     * Láº¥y táº¥t cáº£ khÃ¡ch hÃ ng
     * @return Danh sÃ¡ch táº¥t cáº£ khÃ¡ch hÃ ng
     */
    List<KhachHang> getAllKhachHang();

    /**
     * Láº¥y táº¥t cáº£ khÃ¡ch hÃ ng vá»›i thÃ´ng tin Ä‘áº§y Ä‘á»§ (join fetch)
     * @return Danh sÃ¡ch táº¥t cáº£ khÃ¡ch hÃ ng vá»›i thÃ´ng tin TaiKhoan
     */
    List<KhachHang> getAllWithCompleteInfo();

    /**
     * Láº¥y khÃ¡ch hÃ ng theo ID
     * @param id ID cá»§a khÃ¡ch hÃ ng
     * @return Optional chá»©a khÃ¡ch hÃ ng náº¿u tÃ¬m tháº¥y
     */
    Optional<KhachHang> getKhachHangById(Integer id);

    /**
     * Láº¥y khÃ¡ch hÃ ng theo ID vá»›i eager loading
     * @param id ID cá»§a khÃ¡ch hÃ ng
     * @return Optional chá»©a khÃ¡ch hÃ ng vá»›i thÃ´ng tin Ä‘áº§y Ä‘á»§
     */
    Optional<KhachHang> findByIdWithEagerLoading(Integer id);

    /**
     * ThÃªm khÃ¡ch hÃ ng má»›i
     * @param khachHang ThÃ´ng tin khÃ¡ch hÃ ng cáº§n thÃªm
     */
    void addKhachHang(KhachHang khachHang);

    /**
     * Cáº­p nháº­t thÃ´ng tin khÃ¡ch hÃ ng
     * @param khachHang ThÃ´ng tin khÃ¡ch hÃ ng Ä‘Ã£ cáº­p nháº­t
     */
    void updateKhachHang(KhachHang khachHang);

    /**
     * XÃ³a khÃ¡ch hÃ ng (soft delete)
     * @param id ID cá»§a khÃ¡ch hÃ ng cáº§n xÃ³a
     */
    void deleteKhachHang(Integer id);

    /**
     * TÃ¬m khÃ¡ch hÃ ng theo ID tÃ i khoáº£n
     * @param taiKhoanId ID cá»§a tÃ i khoáº£n
     * @return KhÃ¡ch hÃ ng náº¿u tÃ¬m tháº¥y, null náº¿u khÃ´ng
     */
    KhachHang findByTaiKhoanId(Integer taiKhoanId);

    /**
     * XÃ³a khÃ¡ch hÃ ng theo ID tÃ i khoáº£n (hard delete khi xÃ³a tÃ i khoáº£n)
     * @param taiKhoanId ID cá»§a tÃ i khoáº£n
     */
    void deleteByTaiKhoanId(Integer taiKhoanId);

    // ================== CÃC PHÆ¯Æ NG THá»¨C TÃŒM KIáº¾M ==================

    /**
     * TÃ¬m khÃ¡ch hÃ ng theo ID tÃ i khoáº£n
     * @param taiKhoanId ID cá»§a tÃ i khoáº£n
     * @return Optional chá»©a khÃ¡ch hÃ ng náº¿u tÃ¬m tháº¥y
     */
    Optional<KhachHang> findByTaiKhoanIdOptional(Integer taiKhoanId);

    /**
     * Kiá»ƒm tra sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ tá»“n táº¡i chÆ°a
     * @param sdt Sá»‘ Ä‘iá»‡n thoáº¡i cáº§n kiá»ƒm tra
     * @return true náº¿u sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ tá»“n táº¡i
     */
    boolean existsBySdt(String sdt);

    /**
     * Kiá»ƒm tra mÃ£ khÃ¡ch hÃ ng Ä‘Ã£ tá»“n táº¡i chÆ°a
     * @param maKhachHang MÃ£ khÃ¡ch hÃ ng cáº§n kiá»ƒm tra
     * @return true náº¿u mÃ£ khÃ¡ch hÃ ng Ä‘Ã£ tá»“n táº¡i
     */
    boolean existsByMaKhachHang(String maKhachHang);

    /**
     * TÃ¬m khÃ¡ch hÃ ng theo mÃ£ khÃ¡ch hÃ ng
     * @param maKhachHang MÃ£ khÃ¡ch hÃ ng
     * @return Optional chá»©a khÃ¡ch hÃ ng náº¿u tÃ¬m tháº¥y
     */
    Optional<KhachHang> findByMaKhachHang(String maKhachHang);

    /**
     * TÃ¬m khÃ¡ch hÃ ng theo há» tÃªn vÃ  sá»‘ Ä‘iá»‡n thoáº¡i
     * @param hoTen Há» tÃªn khÃ¡ch hÃ ng
     * @param sdt Sá»‘ Ä‘iá»‡n thoáº¡i
     * @return KhÃ¡ch hÃ ng náº¿u tÃ¬m tháº¥y, null náº¿u khÃ´ng
     */
    KhachHang findByHoTenAndSdt(String hoTen, String sdt);

    // ================== CÃC PHÆ¯Æ NG THá»¨C TÃŒM KIáº¾M CÆ  Báº¢N ==================

    /**
     * TÃ¬m kiáº¿m khÃ¡ch hÃ ng theo tá»« khÃ³a
     * TÃ¬m trong: há» tÃªn, email, sá»‘ Ä‘iá»‡n thoáº¡i, mÃ£ khÃ¡ch hÃ ng
     * @param keyword Tá»« khÃ³a tÃ¬m kiáº¿m
     * @return Danh sÃ¡ch khÃ¡ch hÃ ng khá»›p vá»›i tá»« khÃ³a
     */
    List<KhachHang> searchByKeyword(String keyword);

    /**
     * TÃ¬m kiáº¿m nÃ¢ng cao khÃ¡ch hÃ ng
     * @param hoTen Há» tÃªn (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     * @param email Email (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     * @param sdt Sá»‘ Ä‘iá»‡n thoáº¡i (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     * @param maKhachHang MÃ£ khÃ¡ch hÃ ng (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     * @param trangThai Tráº¡ng thÃ¡i (tÃ¬m kiáº¿m chÃ­nh xÃ¡c)
     * @param gioiTinh Giá»›i tÃ­nh (tÃ¬m kiáº¿m chÃ­nh xÃ¡c) - hiá»‡n táº¡i khÃ´ng sá»­ dá»¥ng
     * @param startDate NgÃ y báº¯t Ä‘áº§u
     * @param endDate NgÃ y káº¿t thÃºc
     * @return Danh sÃ¡ch khÃ¡ch hÃ ng thá»a mÃ£n Ä‘iá»u kiá»‡n
     */
    List<KhachHang> searchAdvanced(String hoTen, String email, String sdt,
                                   String maKhachHang, Integer trangThai, String gioiTinh,
                                   Date startDate, Date endDate);

    // ================== CÃC PHÆ¯Æ NG THá»¨C TÃŒM KIáº¾M NÃ‚NG CAO ==================

    /**
     * TÃ¬m kiáº¿m nÃ¢ng cao khÃ¡ch hÃ ng vá»›i táº¥t cáº£ tiÃªu chÃ­ bao gá»“m cáº£ khoáº£ng thá»i gian
     * @param hoTen Há» tÃªn (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     * @param email Email (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     * @param sdt Sá»‘ Ä‘iá»‡n thoáº¡i (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     * @param maKhachHang MÃ£ khÃ¡ch hÃ ng (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     * @param diaChi Äá»‹a chá»‰ (tÃ¬m kiáº¿m gáº§n Ä‘Ãºng)
     * @param trangThai Tráº¡ng thÃ¡i (tÃ¬m kiáº¿m chÃ­nh xÃ¡c)
     * @param startDate NgÃ y báº¯t Ä‘áº§u (ngÃ y táº¡o >= startDate)
     * @param endDate NgÃ y káº¿t thÃºc (ngÃ y táº¡o <= endDate)
     * @return Danh sÃ¡ch khÃ¡ch hÃ ng thá»a mÃ£n táº¥t cáº£ tiÃªu chÃ­
     */
    List<KhachHang> searchAdvancedWithAllCriteria(
            String hoTen,
            String email,
            String sdt,
            String maKhachHang,
            String diaChi,
            Integer trangThai,
            Date startDate,
            Date endDate
    );

    /**
     * TÃ¬m kiáº¿m khÃ¡ch hÃ ng theo chi tiáº¿t Ä‘á»‹a chá»‰ (2-level addressing)
     * @param provinceCode MÃ£ tá»‰nh/thÃ nh phá»‘
     * @param districtCode Deprecated - khÃ´ng sá»­ dá»¥ng (Ä‘á»ƒ compatibility)
     * @param addressDetail Chi tiáº¿t Ä‘á»‹a chá»‰
     * @return Danh sÃ¡ch khÃ¡ch hÃ ng cÃ³ Ä‘á»‹a chá»‰ khá»›p
     */
    List<KhachHang> searchByAddressDetails(String provinceCode, String districtCode, String addressDetail);

    /**
     * TÃ¬m khÃ¡ch hÃ ng theo pattern sá»‘ Ä‘iá»‡n thoáº¡i
     * @param phonePattern Pattern sá»‘ Ä‘iá»‡n thoáº¡i (vÃ­ dá»¥: "098", "0123")
     * @return Danh sÃ¡ch khÃ¡ch hÃ ng cÃ³ sá»‘ Ä‘iá»‡n thoáº¡i khá»›p pattern
     */
    List<KhachHang> findByPhonePattern(String phonePattern);

    /**
     * TÃ¬m khÃ¡ch hÃ ng cÃ³ email khá»›p pattern
     * @param emailPattern Pattern email (vÃ­ dá»¥: "@gmail.com", "abc")
     * @return Danh sÃ¡ch khÃ¡ch hÃ ng cÃ³ email khá»›p pattern
     */
    List<KhachHang> findByEmailPattern(String emailPattern);

    // ================== CÃC PHÆ¯Æ NG THá»¨C VALIDATION ==================

    /**
     * Kiá»ƒm tra tÃ­nh há»£p lá»‡ cá»§a cÃ¡c tham sá»‘ tÃ¬m kiáº¿m khÃ¡ch hÃ ng
     * @param hoTen Há» tÃªn cáº§n kiá»ƒm tra
     * @param email Email cáº§n kiá»ƒm tra
     * @param sdt Sá»‘ Ä‘iá»‡n thoáº¡i cáº§n kiá»ƒm tra
     * @return true náº¿u táº¥t cáº£ tham sá»‘ há»£p lá»‡
     */
    boolean isValidKhachHangSearchParams(String hoTen, String email, String sdt);

    /**
     * Kiá»ƒm tra sá»‘ Ä‘iá»‡n thoáº¡i cÃ³ Ä‘ang Ä‘Æ°á»£c sá»­ dá»¥ng bá»Ÿi khÃ¡ch hÃ ng khÃ¡c khÃ´ng
     * @param sdt Sá»‘ Ä‘iá»‡n thoáº¡i cáº§n kiá»ƒm tra
     * @param excludeId ID khÃ¡ch hÃ ng loáº¡i trá»« (cho trÆ°á»ng há»£p update)
     * @return true náº¿u sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng
     */
    boolean isPhoneNumberUsed(String sdt, Integer excludeId);

    // ================== CÃC PHÆ¯Æ NG THá»¨C THá»NG KÃŠ ==================

    /**
     * Äáº¿m sá»‘ khÃ¡ch hÃ ng theo tráº¡ng thÃ¡i trong khoáº£ng thá»i gian
     * @param trangThai Tráº¡ng thÃ¡i cáº§n Ä‘áº¿m (null Ä‘á»ƒ Ä‘áº¿m táº¥t cáº£)
     * @param startDate NgÃ y báº¯t Ä‘áº§u (null Ä‘á»ƒ khÃ´ng giá»›i háº¡n)
     * @param endDate NgÃ y káº¿t thÃºc (null Ä‘á»ƒ khÃ´ng giá»›i háº¡n)
     * @return Sá»‘ lÆ°á»£ng khÃ¡ch hÃ ng thá»a mÃ£n Ä‘iá»u kiá»‡n
     */
    long countByStatusAndDateRange(Integer trangThai, Date startDate, Date endDate);

    /**
     * Láº¥y thá»‘ng kÃª tá»•ng quan khÃ¡ch hÃ ng
     * @return Map chá»©a cÃ¡c thá»‘ng kÃª: total, active, inactive, recent, profileCompleted, newToday
     */
    Map<String, Object> getStatistics();

    // ================== CÃC PHÆ¯Æ NG THá»¨C NGHIá»†P Vá»¤ ==================

    /**
     * HoÃ n thiá»‡n thÃ´ng tin khÃ¡ch hÃ ng (profile completion)
     * @param id ID khÃ¡ch hÃ ng
     * @param profileData Dá»¯ liá»‡u profile cáº§n hoÃ n thiá»‡n
     * @return KhÃ¡ch hÃ ng Ä‘Ã£ Ä‘Æ°á»£c cáº­p nháº­t
     */
    KhachHang completeProfile(Integer id, KhachHangDto profileData);

    /**
     * Cáº­p nháº­t tráº¡ng thÃ¡i khÃ¡ch hÃ ng
     * @param id ID khÃ¡ch hÃ ng
     * @param trangThai Tráº¡ng thÃ¡i má»›i (0: vÃ´ hiá»‡u hÃ³a, 1: kÃ­ch hoáº¡t)
     */
    void updateStatus(Integer id, Integer trangThai);

    /**
     * Convert KhachHang entity sang DTO vá»›i thÃ´ng tin Ä‘á»‹a chá»‰ Ä‘áº§y Ä‘á»§
     * @param khachHang Entity cáº§n convert
     * @return DTO Ä‘Ã£ convert vá»›i danh sÃ¡ch Ä‘á»‹a chá»‰ 2-level
     */
    KhachHangDto convertToDto(KhachHang khachHang);

    // ================== CÃC PHÆ¯Æ NG THá»¨C Bá»” SUNG ==================

    /**
     * Láº¥y danh sÃ¡ch khÃ¡ch hÃ ng hoáº¡t Ä‘á»™ng
     * @return Danh sÃ¡ch khÃ¡ch hÃ ng cÃ³ tráº¡ng thÃ¡i = 1
     */
    List<KhachHang> getActiveKhachHang();

    /**
     * Láº¥y khÃ¡ch hÃ ng theo khoáº£ng thá»i gian táº¡o
     * @param startDate NgÃ y báº¯t Ä‘áº§u
     * @param endDate NgÃ y káº¿t thÃºc
     * @return Danh sÃ¡ch khÃ¡ch hÃ ng Ä‘Æ°á»£c táº¡o trong khoáº£ng thá»i gian
     */
    List<KhachHang> getKhachHangByDateRange(Date startDate, Date endDate);

    /**
     * Láº¥y thá»‘ng kÃª chi tiáº¿t khÃ¡ch hÃ ng
     * @return Map chá»©a thá»‘ng kÃª chi tiáº¿t: total, active, inactive, recent
     */
    Map<String, Long> getKhachHangStatistics();

    /**
     * Kiá»ƒm tra cÃ³ thá»ƒ xÃ³a khÃ¡ch hÃ ng khÃ´ng
     * @param id ID khÃ¡ch hÃ ng
     * @return true náº¿u cÃ³ thá»ƒ xÃ³a
     */
    boolean canDeleteKhachHang(Integer id);

    /**
     * Chuyá»ƒn Ä‘á»•i tráº¡ng thÃ¡i khÃ¡ch hÃ ng (active/inactive)
     * @param id ID khÃ¡ch hÃ ng
     */
    void toggleTrangThai(Integer id);
}
