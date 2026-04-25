package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.*;
import org.example.iws_websitesneaker.entity.HoaDon;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.NoSuchElementException;

public interface HoaDonService {
    // TÃ¬m kiáº¿m vÃ  láº¥y danh sÃ¡ch
    List<HoaDonDTO> getAllHoaDons();
    List<HoaDonDTO> getHoaDonByStatus(String trangThai);
    List<HoaDonDTO> getPOSInvoices();
    List<HoaDonDTO> getOnlineInvoices();
    HoaDonDTO getHoaDonById(Integer id);
    List<HoaDonDTO> searchInvoices(String keyword, String trangThai, String loaiHoaDon);
    Optional<HoaDon> findByEmailAndMaHoaDon(String email, String maHoaDon);
    // Cáº­p nháº­t tráº¡ng thÃ¡i
    HoaDonDTO updateStatus(Integer id, InvoiceStatusUpdateRequest request);
    HoaDonDTO confirmInvoice(Integer id, Integer nhanVienId);
    HoaDonDTO cancelInvoice(Integer id, String lyDo, Integer nhanVienId);
    HoaDonDTO completeInvoice(Integer id, Integer nhanVienId);
    OrderTrackingResponse getTrackingResponse(String email, String orderCode);
    HoaDon save(HoaDon hoaDon);
    // Thá»‘ng kÃª vÃ  bÃ¡o cÃ¡o
    Map<String, Object> getInvoiceStatistics();
    List<LichSuHoaDonDTO> getInvoiceHistory(Integer hoaDonId);
    HoaDonDTO updateStatus(Integer id, StatusUpdateRequest request);
    HoaDonDTO confirmInvoice(Integer id);
    HoaDonDTO completeInvoice(Integer id);
    HoaDonDTO cancelInvoice(Integer id, String lyDo);
    HoaDonDTO createHoaDonFromCheckout(CreateHoaDonRequest request);
    List<HoaDonDTO> getHoaDonsByKhachHangId(Integer khachHangId);

    HoaDonDTO cancelInvoiceByCustomer(Integer id, String lyDo, String customerEmail)
            throws IllegalAccessException, IllegalStateException, NoSuchElementException;
}
