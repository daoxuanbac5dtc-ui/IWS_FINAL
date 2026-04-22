package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Dto.HoaDonChiTietDTO;
import org.example.iws_websitesneaker.Service.ChiTietSanPhamService;
import org.example.iws_websitesneaker.Service.HoaDonChiTietService;
import org.example.iws_websitesneaker.entity.*;
import org.example.iws_websitesneaker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HoaDonChiTietServiceImpl implements HoaDonChiTietService {

    @Autowired
    private RepoHoaDonChiTiet hoaDonChiTietRepository;

    @Autowired
    private RepoChiTietSanPham chiTietSanPhamRepository;
    @Autowired
    private ChiTietSanPhamService chiTietSanPhamService;

    @Override
    public List<HoaDonChiTietDTO> getChiTietByHoaDonId(Integer hoaDonId) {
        try {
            return hoaDonChiTietRepository.findByHoaDonIdWithFullInfo(hoaDonId).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback náº¿u query vá»›i full info khÃ´ng hoáº¡t Ä‘á»™ng
            return hoaDonChiTietRepository.findByHoaDonId(hoaDonId).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
    }
    @Override
    public HoaDonChiTiet save(HoaDonChiTiet hoaDonChiTiet) {
        try {
            // Validation cÆ¡ báº£n
            if (hoaDonChiTiet.getHoaDon() == null) {
                throw new IllegalArgumentException("HÃ³a Ä‘Æ¡n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
            }

            if (hoaDonChiTiet.getChiTietSanPham() == null) {
                throw new IllegalArgumentException("Chi tiáº¿t sáº£n pháº©m khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
            }

            if (hoaDonChiTiet.getSoLuong() == null || hoaDonChiTiet.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Sá»‘ lÆ°á»£ng pháº£i lá»›n hÆ¡n 0");
            }

            // Sá»­a validation cho Double
            if (hoaDonChiTiet.getGia() == null || hoaDonChiTiet.getGia() <= 0) {
                throw new IllegalArgumentException("GiÃ¡ pháº£i lá»›n hÆ¡n 0");
            }

            // Kiá»ƒm tra tá»“n kho
            ChiTietSanPham ctsp = hoaDonChiTiet.getChiTietSanPham();
            if (ctsp.getSoLuong() < hoaDonChiTiet.getSoLuong()) {
                throw new IllegalArgumentException("KhÃ´ng Ä‘á»§ hÃ ng trong kho. CÃ²n láº¡i: " + ctsp.getSoLuong());
            }

            // Náº¿u lÃ  táº¡o má»›i, set ngÃ y táº¡o
            if (hoaDonChiTiet.getId() == null) {
                hoaDonChiTiet.setNgayTao(new Date());
            }

            // LÆ°u chi tiáº¿t hÃ³a Ä‘Æ¡n
            HoaDonChiTiet saved = hoaDonChiTietRepository.save(hoaDonChiTiet);

            // Trá»« sá»‘ lÆ°á»£ng tá»“n kho (chá»‰ khi táº¡o má»›i)
            if (hoaDonChiTiet.getId() == null) {
                ctsp.setSoLuong(ctsp.getSoLuong() - hoaDonChiTiet.getSoLuong());
                chiTietSanPhamService.save(ctsp); // Gá»i hÃ m save á»Ÿ trÃªn
            }

            return saved;

        } catch (Exception e) {
            throw new RuntimeException("Lá»—i khi lÆ°u chi tiáº¿t hÃ³a Ä‘Æ¡n: " + e.getMessage(), e);
        }
    }
    @Override
    public HoaDonChiTietDTO getById(Integer id) {
        HoaDonChiTiet chiTiet = hoaDonChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t hÃ³a Ä‘Æ¡n vá»›i ID: " + id));
        return convertToDTO(chiTiet);
    }

    @Override
    public HoaDonChiTietDTO updateQuantity(Integer id, Integer soLuong) {
        if (soLuong == null || soLuong <= 0) {
            throw new RuntimeException("Sá»‘ lÆ°á»£ng pháº£i lá»›n hÆ¡n 0");
        }

        HoaDonChiTiet chiTiet = hoaDonChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t hÃ³a Ä‘Æ¡n"));

        // Kiá»ƒm tra tráº¡ng thÃ¡i hÃ³a Ä‘Æ¡n cÃ³ thá»ƒ chá»‰nh sá»­a khÃ´ng
        String trangThaiHoaDon = chiTiet.getHoaDon().getTrangThaiHoaDon();
        if (!canModifyInvoice(trangThaiHoaDon)) {
            throw new RuntimeException("KhÃ´ng thá»ƒ chá»‰nh sá»­a hÃ³a Ä‘Æ¡n á»Ÿ tráº¡ng thÃ¡i: " + trangThaiHoaDon);
        }

        // Kiá»ƒm tra sá»‘ lÆ°á»£ng tá»“n kho
        ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
        if (ctsp != null) {
            int soLuongHienTai = chiTiet.getSoLuong();
            int chenhLech = soLuong - soLuongHienTai;

            if (chenhLech > 0 && ctsp.getSoLuong() < chenhLech) {
                throw new RuntimeException("KhÃ´ng Ä‘á»§ sá»‘ lÆ°á»£ng trong kho. CÃ²n láº¡i: " + ctsp.getSoLuong());
            }

            // Cáº­p nháº­t sá»‘ lÆ°á»£ng tá»“n kho
            ctsp.setSoLuong(ctsp.getSoLuong() - chenhLech);
            chiTietSanPhamRepository.save(ctsp);

            // Cáº­p nháº­t tá»•ng sá»‘ lÆ°á»£ng sáº£n pháº©m
            if (ctsp.getSanPham() != null) {
                SanPham sanPham = ctsp.getSanPham();
                sanPham.setSoLuong(sanPham.getSoLuong() - chenhLech);
                // sanPhamRepository.save(sanPham); // Uncomment náº¿u cáº§n
            }
        }

        // Cáº­p nháº­t chi tiáº¿t hÃ³a Ä‘Æ¡n
        chiTiet.setSoLuong(soLuong);
        chiTiet.setNgayCapNhat(new Date());

        HoaDonChiTiet saved = hoaDonChiTietRepository.save(chiTiet);

        // Cáº­p nháº­t láº¡i tá»•ng tiá»n hÃ³a Ä‘Æ¡n
        updateInvoiceTotal(chiTiet.getHoaDon().getId());

        return convertToDTO(saved);
    }

    @Override
    public HoaDonChiTietDTO updateProduct(Integer id, HoaDonChiTietDTO dto) {
        HoaDonChiTiet chiTiet = hoaDonChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t hÃ³a Ä‘Æ¡n"));

        // Kiá»ƒm tra tráº¡ng thÃ¡i hÃ³a Ä‘Æ¡n
        String trangThaiHoaDon = chiTiet.getHoaDon().getTrangThaiHoaDon();
        if (!canModifyInvoice(trangThaiHoaDon)) {
            throw new RuntimeException("KhÃ´ng thá»ƒ chá»‰nh sá»­a hÃ³a Ä‘Æ¡n á»Ÿ tráº¡ng thÃ¡i: " + trangThaiHoaDon);
        }

        // Cáº­p nháº­t thÃ´ng tin náº¿u cÃ³
        if (dto.getSoLuong() != null) {
            return updateQuantity(id, dto.getSoLuong());
        }

        if (dto.getGiaBan() != null) {
            chiTiet.setGia(dto.getGiaBan());
        }

        if (dto.getTrangThai() != null) {
            chiTiet.setTrangThaiHoaDon(dto.getTrangThai());
        }

        chiTiet.setNgayCapNhat(new Date());
        HoaDonChiTiet saved = hoaDonChiTietRepository.save(chiTiet);

        // Cáº­p nháº­t tá»•ng tiá»n hÃ³a Ä‘Æ¡n
        updateInvoiceTotal(chiTiet.getHoaDon().getId());

        return convertToDTO(saved);
    }

    @Override
    public void removeProduct(Integer id) {
        HoaDonChiTiet chiTiet = hoaDonChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y chi tiáº¿t hÃ³a Ä‘Æ¡n"));

        // Kiá»ƒm tra tráº¡ng thÃ¡i hÃ³a Ä‘Æ¡n
        String trangThaiHoaDon = chiTiet.getHoaDon().getTrangThaiHoaDon();
        if (!canModifyInvoice(trangThaiHoaDon)) {
            throw new RuntimeException("KhÃ´ng thá»ƒ chá»‰nh sá»­a hÃ³a Ä‘Æ¡n á»Ÿ tráº¡ng thÃ¡i: " + trangThaiHoaDon);
        }

        // HoÃ n láº¡i sá»‘ lÆ°á»£ng vÃ o kho
        ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
        if (ctsp != null) {
            ctsp.setSoLuong(ctsp.getSoLuong() + chiTiet.getSoLuong());
            chiTietSanPhamRepository.save(ctsp);

            // Cáº­p nháº­t tá»•ng sá»‘ lÆ°á»£ng sáº£n pháº©m
            if (ctsp.getSanPham() != null) {
                SanPham sanPham = ctsp.getSanPham();
                sanPham.setSoLuong(sanPham.getSoLuong() + chiTiet.getSoLuong());
                // sanPhamRepository.save(sanPham); // Uncomment náº¿u cáº§n
            }
        }

        Integer hoaDonId = chiTiet.getHoaDon().getId();

        // XÃ³a chi tiáº¿t
        hoaDonChiTietRepository.delete(chiTiet);

        // Cáº­p nháº­t tá»•ng tiá»n hÃ³a Ä‘Æ¡n
        updateInvoiceTotal(hoaDonId);
    }

    // =================== PRIVATE HELPER METHODS ===================

    private HoaDonChiTietDTO convertToDTO(HoaDonChiTiet chiTiet) {
        HoaDonChiTietDTO dto = new HoaDonChiTietDTO();

        dto.setId(chiTiet.getId());
        dto.setHoaDonId(chiTiet.getHoaDon().getId());
        dto.setSoLuong(chiTiet.getSoLuong());
        dto.setGiaBan(chiTiet.getGia());
        dto.setTrangThai(chiTiet.getTrangThaiHoaDon());

        if (chiTiet.getChiTietSanPham() != null) {
            ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
            dto.setChiTietSanPhamId(ctsp.getId());
            dto.setMaChiTiet(ctsp.getMaChiTiet());
            dto.setGiaGoc(ctsp.getGiaGoc());
            dto.setHinhAnh(ctsp.getHinhAnh() != null ? ctsp.getHinhAnh().getDuongDan() : null);

            if (ctsp.getSanPham() != null) {
                dto.setTenSanPham(ctsp.getSanPham().getTenSanPham());
                dto.setMaSanPham(ctsp.getSanPham().getMaSanPham());
                dto.setThuongHieu(ctsp.getSanPham().getThuongHieu() != null ?
                        ctsp.getSanPham().getThuongHieu().getTenThuongHieu() : "N/A");
                dto.setDanhMuc(ctsp.getSanPham().getDanhMuc() != null ?
                        ctsp.getSanPham().getDanhMuc().getTenDanhMuc() : "N/A");
            }

            dto.setMauSac(ctsp.getMauSac() != null ? ctsp.getMauSac().getTenMauSac() : "N/A");
            dto.setKichThuoc(ctsp.getKichCo() != null ? ctsp.getKichCo().getTenKichCo() : "N/A");
        } else {
            // Fallback náº¿u khÃ´ng cÃ³ thÃ´ng tin chi tiáº¿t sáº£n pháº©m
            dto.setTenSanPham("Sáº£n pháº©m khÃ´ng xÃ¡c Ä‘á»‹nh");
            dto.setMaSanPham("N/A");
            dto.setMauSac("N/A");
            dto.setKichThuoc("N/A");
            dto.setThuongHieu("N/A");
            dto.setDanhMuc("N/A");
        }

        // TÃ­nh toÃ¡n cÃ¡c giÃ¡ trá»‹
        dto.calculateValues();

        return dto;
    }

    private boolean canModifyInvoice(String trangThai) {
        // Chá»‰ cho phÃ©p chá»‰nh sá»­a khi hÃ³a Ä‘Æ¡n á»Ÿ tráº¡ng thÃ¡i PENDING hoáº·c CONFIRMED
        return "PENDING".equals(trangThai) || "CONFIRMED".equals(trangThai);
    }

    private void updateInvoiceTotal(Integer hoaDonId) {
        try {
            // TÃ­nh láº¡i tá»•ng tiá»n hÃ³a Ä‘Æ¡n dá»±a trÃªn chi tiáº¿t - Sá»¬ Dá»¤NG DOUBLE
            List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

            Double tongTien = chiTietList.stream()
                    .map(ct -> {
                        Double gia = ct.getGia();
                        Integer soLuong = ct.getSoLuong();
                        return gia != null && soLuong != null ? gia * soLuong : 0.0;
                    })
                    .reduce(0.0, Double::sum);

            // Cáº­p nháº­t tá»•ng tiá»n vÃ o hÃ³a Ä‘Æ¡n
            // hoaDonRepository.updateTongTien(hoaDonId, tongTien);
            System.out.println("Tá»•ng tiá»n hÃ³a Ä‘Æ¡n " + hoaDonId + ": " + tongTien);

        } catch (Exception e) {
            System.err.println("Lá»—i cáº­p nháº­t tá»•ng tiá»n hÃ³a Ä‘Æ¡n: " + e.getMessage());
        }
    }
}
