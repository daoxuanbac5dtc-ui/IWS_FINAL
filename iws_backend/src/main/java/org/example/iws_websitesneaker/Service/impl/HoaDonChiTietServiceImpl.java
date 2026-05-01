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
            // Fallback nếu query với full info không hoạt động
            return hoaDonChiTietRepository.findByHoaDonId(hoaDonId).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
    }
    @Override
    public HoaDonChiTiet save(HoaDonChiTiet hoaDonChiTiet) {
        try {
            // Validation cơ bản
            if (hoaDonChiTiet.getHoaDon() == null) {
                throw new IllegalArgumentException("Hóa đơn không được để trống");
            }

            if (hoaDonChiTiet.getChiTietSanPham() == null) {
                throw new IllegalArgumentException("Chi tiết sản phẩm không được để trống");
            }

            if (hoaDonChiTiet.getSoLuong() == null || hoaDonChiTiet.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
            }

            // Sửa validation cho Double
            if (hoaDonChiTiet.getGia() == null || hoaDonChiTiet.getGia() <= 0) {
                throw new IllegalArgumentException("Giá phải lớn hơn 0");
            }

            // Kiểm tra tồn kho
            ChiTietSanPham ctsp = hoaDonChiTiet.getChiTietSanPham();
            if (ctsp.getSoLuong() < hoaDonChiTiet.getSoLuong()) {
                throw new IllegalArgumentException("Không đủ hàng trong kho. Còn lại: " + ctsp.getSoLuong());
            }

            // Nếu là tạo mới, set ngày tạo
            if (hoaDonChiTiet.getId() == null) {
                hoaDonChiTiet.setNgayTao(new Date());
            }

            // Lưu chi tiết hóa đơn
            HoaDonChiTiet saved = hoaDonChiTietRepository.save(hoaDonChiTiet);

            // Trừ số lượng tồn kho (chỉ khi tạo mới)
            if (hoaDonChiTiet.getId() == null) {
                ctsp.setSoLuong(ctsp.getSoLuong() - hoaDonChiTiet.getSoLuong());
                chiTietSanPhamService.save(ctsp); // Gọi hàm save ở trên
            }

            return saved;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu chi tiết hóa đơn: " + e.getMessage(), e);
        }
    }
    @Override
    public HoaDonChiTietDTO getById(Integer id) {
        HoaDonChiTiet chiTiet = hoaDonChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn với ID: " + id));
        return convertToDTO(chiTiet);
    }

    @Override
    public HoaDonChiTietDTO updateQuantity(Integer id, Integer soLuong) {
        if (soLuong == null || soLuong <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        HoaDonChiTiet chiTiet = hoaDonChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn"));

        // Kiểm tra trạng thái hóa đơn có thể chỉnh sửa không
        String trangThaiHoaDon = chiTiet.getHoaDon().getTrangThaiHoaDon();
        if (!canModifyInvoice(trangThaiHoaDon)) {
            throw new RuntimeException("Không thể chỉnh sửa hóa đơn ở trạng thái: " + trangThaiHoaDon);
        }

        // Kiểm tra số lượng tồn kho
        ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
        if (ctsp != null) {
            int soLuongHienTai = chiTiet.getSoLuong();
            int chenhLech = soLuong - soLuongHienTai;

            if (chenhLech > 0 && ctsp.getSoLuong() < chenhLech) {
                throw new RuntimeException("Không đủ số lượng trong kho. Còn lại: " + ctsp.getSoLuong());
            }

            // Cập nhật số lượng tồn kho
            ctsp.setSoLuong(ctsp.getSoLuong() - chenhLech);
            chiTietSanPhamRepository.save(ctsp);

            // Cập nhật tổng số lượng sản phẩm
            if (ctsp.getSanPham() != null) {
                SanPham sanPham = ctsp.getSanPham();
                sanPham.setSoLuong(sanPham.getSoLuong() - chenhLech);
                // sanPhamRepository.save(sanPham); // Uncomment nếu cần
            }
        }

        // Cập nhật chi tiết hóa đơn
        chiTiet.setSoLuong(soLuong);
        chiTiet.setNgayCapNhat(new Date());

        HoaDonChiTiet saved = hoaDonChiTietRepository.save(chiTiet);

        // Cập nhật lại tổng tiền hóa đơn
        updateInvoiceTotal(chiTiet.getHoaDon().getId());

        return convertToDTO(saved);
    }

    @Override
    public HoaDonChiTietDTO updateProduct(Integer id, HoaDonChiTietDTO dto) {
        HoaDonChiTiet chiTiet = hoaDonChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn"));

        // Kiểm tra trạng thái hóa đơn
        String trangThaiHoaDon = chiTiet.getHoaDon().getTrangThaiHoaDon();
        if (!canModifyInvoice(trangThaiHoaDon)) {
            throw new RuntimeException("Không thể chỉnh sửa hóa đơn ở trạng thái: " + trangThaiHoaDon);
        }

        // Cập nhật thông tin nếu có
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

        // Cập nhật tổng tiền hóa đơn
        updateInvoiceTotal(chiTiet.getHoaDon().getId());

        return convertToDTO(saved);
    }

    @Override
    public void removeProduct(Integer id) {
        HoaDonChiTiet chiTiet = hoaDonChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn"));

        // Kiểm tra trạng thái hóa đơn
        String trangThaiHoaDon = chiTiet.getHoaDon().getTrangThaiHoaDon();
        if (!canModifyInvoice(trangThaiHoaDon)) {
            throw new RuntimeException("Không thể chỉnh sửa hóa đơn ở trạng thái: " + trangThaiHoaDon);
        }

        // Hoàn lại số lượng vào kho
        ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
        if (ctsp != null) {
            ctsp.setSoLuong(ctsp.getSoLuong() + chiTiet.getSoLuong());
            chiTietSanPhamRepository.save(ctsp);

            // Cập nhật tổng số lượng sản phẩm
            if (ctsp.getSanPham() != null) {
                SanPham sanPham = ctsp.getSanPham();
                sanPham.setSoLuong(sanPham.getSoLuong() + chiTiet.getSoLuong());
                // sanPhamRepository.save(sanPham); // Uncomment nếu cần
            }
        }

        Integer hoaDonId = chiTiet.getHoaDon().getId();

        // Xóa chi tiết
        hoaDonChiTietRepository.delete(chiTiet);

        // Cập nhật tổng tiền hóa đơn
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
            // Fallback nếu không có thông tin chi tiết sản phẩm
            dto.setTenSanPham("Sản phẩm không xác định");
            dto.setMaSanPham("N/A");
            dto.setMauSac("N/A");
            dto.setKichThuoc("N/A");
            dto.setThuongHieu("N/A");
            dto.setDanhMuc("N/A");
        }

        // Tính toán các giá trị
        dto.calculateValues();

        return dto;
    }

    private boolean canModifyInvoice(String trangThai) {
        // Chỉ cho phép chỉnh sửa khi hóa đơn ở trạng thái PENDING hoặc CONFIRMED
        return "PENDING".equals(trangThai) || "CONFIRMED".equals(trangThai);
    }

    private void updateInvoiceTotal(Integer hoaDonId) {
        try {
            // Tính lại tổng tiền hóa đơn dựa trên chi tiết - SỬ DỤNG DOUBLE
            List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

            Double tongTien = chiTietList.stream()
                    .map(ct -> {
                        Double gia = ct.getGia();
                        Integer soLuong = ct.getSoLuong();
                        return gia != null && soLuong != null ? gia * soLuong : 0.0;
                    })
                    .reduce(0.0, Double::sum);

            // Cập nhật tổng tiền vào hóa đơn
            // hoaDonRepository.updateTongTien(hoaDonId, tongTien);
            System.out.println("Tổng tiền hóa đơn " + hoaDonId + ": " + tongTien);

        } catch (Exception e) {
            System.err.println("Lỗi cập nhật tổng tiền hóa đơn: " + e.getMessage());
        }
    }
}
