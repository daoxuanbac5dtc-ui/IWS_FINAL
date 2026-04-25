package org.example.iws_websitesneaker.Service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.iws_websitesneaker.Service.ChiTietVoucherService;
import org.example.iws_websitesneaker.Dto.ChiTietVoucherDTO;
import org.example.iws_websitesneaker.entity.ChiTietVoucher;
import org.example.iws_websitesneaker.entity.HoaDon;
import org.example.iws_websitesneaker.entity.Voucher;
import org.example.iws_websitesneaker.repository.BanHang.VoucherBHRepository;
import org.example.iws_websitesneaker.repository.ChiTietVoucherRepository;
import org.example.iws_websitesneaker.repository.HoaDonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChiTietVoucherServiceImpl implements ChiTietVoucherService {

    private final ChiTietVoucherRepository repository;
    private final HoaDonRepository hoaDonRepository;
    private final VoucherBHRepository voucherRepository;


    @Override
    @Transactional
    public ChiTietVoucherDTO create(ChiTietVoucherDTO dto) {
        try {
            log.info("Creating ChiTietVoucher with DTO: {}", dto.getMaChiTietVoucher());

            ChiTietVoucher entity = new ChiTietVoucher();
            updateEntityFromDTO(entity, dto);

            // SET RELATIONSHIPS
            if (dto.getHoaDonId() != null) {
                log.info("Finding HoaDon with ID: {}", dto.getHoaDonId());
                HoaDon hoaDon = hoaDonRepository.findById(dto.getHoaDonId())
                        .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n ID: " + dto.getHoaDonId()));
                entity.setHoaDon(hoaDon);
                log.info("HoaDon set successfully");
            }

            if (dto.getVoucherId() != null) {
                log.info("Finding Voucher with ID: {}", dto.getVoucherId());
                Voucher voucher = voucherRepository.findById(dto.getVoucherId())
                        .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y voucher ID: " + dto.getVoucherId()));
                entity.setVoucher(voucher);
                log.info("Voucher set successfully");
            }

            // Set thá»i gian táº¡o
            entity.setNgayTao(new Date());

            log.info("Saving entity: {}", entity.getMaChiTietVoucher());
            ChiTietVoucher saved = repository.save(entity);
            log.info("Entity saved successfully with ID: {}", saved.getId());

            return convertToDTO(saved);

        } catch (Exception e) {
            log.error("Error creating ChiTietVoucher: ", e);
            throw new RuntimeException("Lá»—i táº¡o chi tiáº¿t voucher: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ChiTietVoucherDTO getById(Integer id) {
        try {
            log.info("Finding ChiTietVoucher by ID: {}", id);

            ChiTietVoucher entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t voucher ID: " + id));

            return convertToDTO(entity);

        } catch (Exception e) {
            log.error("Error finding ChiTietVoucher by ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Lá»—i tÃ¬m chi tiáº¿t voucher: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChiTietVoucherDTO> findByHoaDonId(Integer hoaDonId) {
        try {
            log.info("Finding ChiTietVoucher by HoaDon ID: {}", hoaDonId);

            List<ChiTietVoucher> entities = repository.findByHoaDonIdOrderByNgayApDungDesc(hoaDonId);

            List<ChiTietVoucherDTO> dtos = entities.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            log.info("Found {} ChiTietVoucher for HoaDon {}", dtos.size(), hoaDonId);

            return dtos;

        } catch (Exception e) {
            log.error("Error finding ChiTietVoucher by HoaDon {}: {}", hoaDonId, e.getMessage());
            throw new RuntimeException("Lá»—i tÃ¬m chi tiáº¿t voucher: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChiTietVoucherDTO> findAll(Pageable pageable) {
        try {
            log.info("Finding all ChiTietVoucher with pagination");

            Page<ChiTietVoucher> entities = repository.findAll(pageable);

            return entities.map(this::convertToDTO);

        } catch (Exception e) {
            log.error("Error finding all ChiTietVoucher: {}", e.getMessage());
            throw new RuntimeException("Lá»—i láº¥y danh sÃ¡ch: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChiTietVoucherDTO> getAll() {
        try {
            log.info("Getting all ChiTietVoucher without pagination");

            List<ChiTietVoucher> entities = repository.findAll();

            return entities.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting all ChiTietVoucher: {}", e.getMessage());
            throw new RuntimeException("Lá»—i láº¥y táº¥t cáº£ chi tiáº¿t voucher: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChiTietVoucherDTO> findByVoucherId(Integer voucherId) {
        try {
            log.info("Finding ChiTietVoucher by Voucher ID: {}", voucherId);

            List<ChiTietVoucher> entities = repository.findByVoucherIdOrderByNgayApDungDesc(voucherId);

            return entities.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error finding ChiTietVoucher by Voucher {}: {}", voucherId, e.getMessage());
            throw new RuntimeException("Lá»—i tÃ¬m chi tiáº¿t voucher: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ChiTietVoucherDTO update(Integer id, ChiTietVoucherDTO dto) {
        try {
            log.info("Updating ChiTietVoucher ID: {}", id);

            ChiTietVoucher entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t voucher ID: " + id));

            updateEntityFromDTO(entity, dto);

            // Update relationships if needed
            if (dto.getHoaDonId() != null) {
                HoaDon hoaDon = hoaDonRepository.findById(dto.getHoaDonId())
                        .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n ID: " + dto.getHoaDonId()));
                entity.setHoaDon(hoaDon);
            }

            if (dto.getVoucherId() != null) {
                Voucher voucher = voucherRepository.findById(dto.getVoucherId())
                        .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y voucher ID: " + dto.getVoucherId()));
                entity.setVoucher(voucher);
            }

            entity.setNgayCapNhat(new Date());

            ChiTietVoucher updated = repository.save(entity);

            return convertToDTO(updated);

        } catch (Exception e) {
            log.error("Error updating ChiTietVoucher ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Lá»—i cáº­p nháº­t chi tiáº¿t voucher: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChiTietVoucherDTO> searchByMaChiTietVoucher(String keyword) {
        try {
            log.info("Searching ChiTietVoucher by MaChiTietVoucher: {}", keyword);

            List<ChiTietVoucher> entities = repository.findByMaChiTietVoucherContainingIgnoreCase(keyword);

            return entities.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error searching ChiTietVoucher by MaChiTietVoucher {}: {}", keyword, e.getMessage());
            throw new RuntimeException("Lá»—i tÃ¬m kiáº¿m chi tiáº¿t voucher: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChiTietVoucherDTO> searchByMaVoucher(String keyword) {
        try {
            log.info("Searching ChiTietVoucher by MaVoucher: {}", keyword);

            List<ChiTietVoucher> entities = repository.findByMaVoucherContainingIgnoreCase(keyword);

            return entities.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error searching ChiTietVoucher by MaVoucher {}: {}", keyword, e.getMessage());
            throw new RuntimeException("Lá»—i tÃ¬m kiáº¿m chi tiáº¿t voucher: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChiTietVoucherDTO> searchByTenVoucher(String keyword) {
        try {
            log.info("Searching ChiTietVoucher by TenVoucher: {}", keyword);

            List<ChiTietVoucher> entities = repository.findByTenVoucherContainingIgnoreCase(keyword);

            return entities.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error searching ChiTietVoucher by TenVoucher {}: {}", keyword, e.getMessage());
            throw new RuntimeException("Lá»—i tÃ¬m kiáº¿m chi tiáº¿t voucher: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        try {
            log.info("Deleting ChiTietVoucher ID: {}", id);

            if (!repository.existsById(id)) {
                throw new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t voucher ID: " + id);
            }

            repository.deleteById(id);

            log.info("ChiTietVoucher ID {} deleted successfully", id);

        } catch (Exception e) {
            log.error("Error deleting ChiTietVoucher ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Lá»—i xÃ³a chi tiáº¿t voucher: " + e.getMessage());
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Convert Entity to DTO
     */
    private ChiTietVoucherDTO convertToDTO(ChiTietVoucher entity) {
        if (entity == null) return null;

        ChiTietVoucherDTO dto = new ChiTietVoucherDTO();

        // Basic fields
        dto.setId(entity.getId());
        dto.setMaChiTietVoucher(entity.getMaChiTietVoucher());

        // Foreign key IDs
        dto.setHoaDonId(entity.getHoaDon() != null ? entity.getHoaDon().getId() : null);
        dto.setVoucherId(entity.getVoucher() != null ? entity.getVoucher().getId() : null);

        // Voucher snapshot information
        dto.setMaVoucher(entity.getMaVoucher());
        dto.setTenVoucher(entity.getTenVoucher());
        dto.setLoaiGiamGia(entity.getLoaiGiamGia());
        dto.setGiaTriGiam(entity.getGiaTriGiam());
        dto.setGiaTriGiamToiDa(entity.getGiaTriGiamToiDa());
        dto.setGiaTriGiamToiThieu(entity.getGiaTriGiamToiThieu());

        // Calculation information
        dto.setGiaTriDonHang(entity.getGiaTriDonHang());
        dto.setSoTienGiam(entity.getSoTienGiam());
        dto.setThanhTien(entity.getThanhTien());

        // Timestamps
        dto.setNgayApDung(entity.getNgayApDung());
        dto.setNgayTao(entity.getNgayTao());
        dto.setNgayCapNhat(entity.getNgayCapNhat());

        return dto;
    }

    /**
     * Update Entity from DTO
     */
    private void updateEntityFromDTO(ChiTietVoucher entity, ChiTietVoucherDTO dto) {
        if (dto == null) return;

        // Basic fields
        entity.setMaChiTietVoucher(dto.getMaChiTietVoucher());

        // Voucher snapshot information
        entity.setMaVoucher(dto.getMaVoucher());
        entity.setTenVoucher(dto.getTenVoucher());
        entity.setLoaiGiamGia(dto.getLoaiGiamGia());
        entity.setGiaTriGiam(dto.getGiaTriGiam());
        entity.setGiaTriGiamToiDa(dto.getGiaTriGiamToiDa());
        entity.setGiaTriGiamToiThieu(dto.getGiaTriGiamToiThieu());

        // Calculation information
        entity.setGiaTriDonHang(dto.getGiaTriDonHang());
        entity.setSoTienGiam(dto.getSoTienGiam());
        entity.setThanhTien(dto.getThanhTien());
        entity.setNgayApDung(dto.getNgayApDung());
    }
    @Override
    public boolean existsByHoaDonIdAndVoucherId(Integer hoaDonId, Integer voucherId) {
        return repository.existsByHoaDonIdAndVoucherId(hoaDonId, voucherId);
    }

}
