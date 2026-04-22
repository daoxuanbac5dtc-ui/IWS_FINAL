package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.BanHang.InventoryCheckResponse;
import org.example.iws_websitesneaker.Dto.ChiTietVoucherDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChiTietVoucherService {
    List<ChiTietVoucherDTO> getAll();
    ChiTietVoucherDTO getById(Integer id);
    ChiTietVoucherDTO create(ChiTietVoucherDTO dto);
    ChiTietVoucherDTO update(Integer id, ChiTietVoucherDTO dto);
    void delete(Integer id);

    // TÃ¬m kiáº¿m
    List<ChiTietVoucherDTO> searchByMaChiTietVoucher(String keyword);
    List<ChiTietVoucherDTO> searchByMaVoucher(String keyword);
    List<ChiTietVoucherDTO> searchByTenVoucher(String keyword);

    List<ChiTietVoucherDTO> findByHoaDonId(Integer hoaDonId);

    Page<ChiTietVoucherDTO> findAll(Pageable pageable);

    List<ChiTietVoucherDTO> findByVoucherId(Integer voucherId);

    boolean existsByHoaDonIdAndVoucherId(Integer hoaDonId, Integer voucherId);
}

