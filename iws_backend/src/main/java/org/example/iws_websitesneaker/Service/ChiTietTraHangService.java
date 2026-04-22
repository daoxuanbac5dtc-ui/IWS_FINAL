package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.ChiTietTraHangDTO;

import java.util.List;
import java.util.Map;

public interface ChiTietTraHangService {

    // CÃ¡c method cÆ¡ báº£n
    List<ChiTietTraHangDTO> getChiTietTraHangByChiTietSanPham(Integer chiTietSanPhamId);

    ChiTietTraHangDTO getById(Integer id);

    List<ChiTietTraHangDTO> getChiTietTraHangByHoaDon(Integer hoaDonId);

    ChiTietTraHangDTO createChiTietTraHang(ChiTietTraHangDTO dto);

    ChiTietTraHangDTO updateChiTietTraHang(Integer id, ChiTietTraHangDTO dto);

    ChiTietTraHangDTO updateTrangThai(Integer id, String trangThai, String ghiChu);

    void deleteChiTietTraHang(Integer id);

    // CÃ¡c method thá»‘ng kÃª vÃ  kiá»ƒm tra
    Integer getTotalReturnedQuantity(Integer chiTietSanPhamId);

    Map<String, Object> getReturnStatistics(Integer hoaDonId);

    boolean canReturn(Integer chiTietSanPhamId, Integer soLuong);

    ChiTietTraHangDTO approveReturn(Integer id, String ghiChu);

    ChiTietTraHangDTO rejectReturn(Integer id, String lyDo);

    // CÃ¡c method há»— trá»£ hÃ³a Ä‘Æ¡n
    List<ChiTietTraHangDTO> getChiTietTraHangByHoaDonAndStatus(Integer hoaDonId, String trangThai);

    Integer getTotalReturnedQuantityByInvoice(Integer hoaDonId);

    Double getTotalReturnValueByInvoice(Integer hoaDonId);

    Boolean hasReturnsForInvoice(Integer hoaDonId);

    // âœ… THÃŠM: CÃ¡c method má»›i cho tÃ¬m kiáº¿m vÃ  lÃ½ do

    /**
     * TÃ¬m kiáº¿m chi tiáº¿t tráº£ hÃ ng theo nhiá»u tiÃªu chÃ­
     *
     * @param lyDo      LÃ½ do tráº£ hÃ ng (cÃ³ thá»ƒ null)
     * @param trangThai Tráº¡ng thÃ¡i (cÃ³ thá»ƒ null)
     * @param hoaDonId  ID hÃ³a Ä‘Æ¡n (cÃ³ thá»ƒ null)
     * @return Danh sÃ¡ch chi tiáº¿t tráº£ hÃ ng
     */
    List<ChiTietTraHangDTO> searchChiTietTraHang(String lyDo, String trangThai, Integer hoaDonId);

    /**
     * Láº¥y danh sÃ¡ch chi tiáº¿t tráº£ hÃ ng theo lÃ½ do
     *
     * @param lyDo LÃ½ do tráº£ hÃ ng
     * @return Danh sÃ¡ch chi tiáº¿t tráº£ hÃ ng
     */
    List<ChiTietTraHangDTO> getChiTietTraHangByLyDo(String lyDo);

    /**
     * Láº¥y danh sÃ¡ch chi tiáº¿t tráº£ hÃ ng cÃ³ áº£nh minh chá»©ng
     *
     * @return Danh sÃ¡ch chi tiáº¿t tráº£ hÃ ng cÃ³ áº£nh
     */
    List<ChiTietTraHangDTO> getChiTietTraHangWithImages();

    /**
     * Láº¥y danh sÃ¡ch chi tiáº¿t tráº£ hÃ ng khÃ´ng cÃ³ áº£nh minh chá»©ng
     *
     * @return Danh sÃ¡ch chi tiáº¿t tráº£ hÃ ng khÃ´ng cÃ³ áº£nh
     */
    List<ChiTietTraHangDTO> getChiTietTraHangWithoutImages();

    /**
     * Láº¥y thá»‘ng kÃª theo lÃ½ do tráº£ hÃ ng
     *
     * @return Map chá»©a thá»‘ng kÃª: {lyDo: soLuong}
     */
    Map<String, Long> getReturnReasonStatistics();

    /**
     * Äáº¿m sá»‘ lÆ°á»£ng tráº£ hÃ ng cÃ³ áº£nh minh chá»©ng
     *
     * @return Sá»‘ lÆ°á»£ng
     */
    Long countReturnsWithImages();

    /**
     * Äáº¿m sá»‘ lÆ°á»£ng tráº£ hÃ ng theo lÃ½ do cá»¥ thá»ƒ
     *
     * @param lyDo LÃ½ do tráº£ hÃ ng
     * @return Sá»‘ lÆ°á»£ng
     */
    Long countByLyDo(String lyDo);

    /**
     * Cáº­p nháº­t áº£nh minh chá»©ng cho chi tiáº¿t tráº£ hÃ ng
     *
     * @param id          ID chi tiáº¿t tráº£ hÃ ng
     * @param duongDanAnh ÄÆ°á»ng dáº«n áº£nh má»›i
     * @return Chi tiáº¿t tráº£ hÃ ng Ä‘Ã£ cáº­p nháº­t
     */
    ChiTietTraHangDTO updateImage(Integer id, String duongDanAnh);

    /**
     * XÃ³a áº£nh minh chá»©ng cá»§a chi tiáº¿t tráº£ hÃ ng
     *
     * @param id ID chi tiáº¿t tráº£ hÃ ng
     * @return Chi tiáº¿t tráº£ hÃ ng Ä‘Ã£ cáº­p nháº­t
     */
    ChiTietTraHangDTO removeImage(Integer id);

    ChiTietTraHangDTO approveReturnWithInventory(Integer id, String ghiChu);
    ChiTietTraHangDTO approveReturnNoInventory(Integer id, String ghiChu);
}
