package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Dto.DiaChiDto;
import org.example.iws_websitesneaker.Service.DiaChiService;
import org.example.iws_websitesneaker.entity.DiaChi;
import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.example.iws_websitesneaker.repository.RepoDiaChi;
import org.example.iws_websitesneaker.repository.RepoKhachHang;
import org.example.iws_websitesneaker.repository.RepoNhanVien;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiaChiServiceImpl implements DiaChiService {

    @Autowired
    private RepoDiaChi repoDiaChi;
    @Autowired
    private RepoKhachHang repoKhachHang;
    @Autowired
    private RepoNhanVien repoNhanVien;

    // ====== CÁC METHOD DTO-BASED (GIỮ NGUYÊN) ======
    @Override
    public Optional<DiaChi> findDefaultByTaiKhoanId(Integer taiKhoanId) {
        try {
            return Optional.ofNullable(repoDiaChi.findByTaiKhoanIdAndIsDefault(taiKhoanId, true));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Lấy tất cả địa chỉ theo ID tài khoản
     */
    @Override
    public List<DiaChi> findByTaiKhoanId(Integer taiKhoanId) {
        try {
            return repoDiaChi.findByTaiKhoanIdOrderByIsDefaultDescNgayTaoDesc(taiKhoanId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    @Override
    public List<DiaChiDto> getAll() {
        return repoDiaChi.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public DiaChiDto getById(Integer id) {
        DiaChi diaChi = repoDiaChi.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + id));
        return convertToDto(diaChi);
    }

    @Override
    public DiaChiDto add(DiaChiDto diachiDto) {
        DiaChi diaChi = convertToEntity(diachiDto);
        diaChi.setNgayTao(new Date());
        diaChi.setNgayCapNhat(new Date());
        DiaChi savedDiaChi = repoDiaChi.save(diaChi);
        return convertToDto(savedDiaChi);
    }

    @Override
    public DiaChiDto update(Integer id, DiaChiDto diachiDto) {
        DiaChi existingDiaChi = repoDiaChi.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + id));

        // Chỉ cập nhật các trường có trong DTO (BỎ HUYỆN)
        existingDiaChi.setTenTinh(diachiDto.getTenTinh());
        existingDiaChi.setTenPhuong(diachiDto.getTenPhuong());
        existingDiaChi.setNgayCapNhat(new Date());

        DiaChi updatedDiaChi = repoDiaChi.save(existingDiaChi);
        return convertToDto(updatedDiaChi);
    }

    @Override
    public void delete(Integer id) {
        if (!repoDiaChi.existsById(id)) {
            throw new RuntimeException("Không tìm thấy địa chỉ với ID: " + id);
        }
        repoDiaChi.deleteById(id);
    }

    @Override
    public List<DiaChiDto> search(String keyword) {
        return repoDiaChi.searchByKeyword(keyword)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DiaChiDto> findByTenKhachHang(String tenKhachHang) {
        return repoDiaChi.searchDiaChiWithHoTenNguoiDung(tenKhachHang)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DiaChiDto> findByTenTinh(String tenTinh) {
        return repoDiaChi.findByTenTinhContainingIgnoreCase(tenTinh)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ====== CÁC METHOD ENTITY-BASED (GIỮ NGUYÊN) ======
    @Override
    public Optional<DiaChi> findById(Integer id) {
        return repoDiaChi.findById(id);
    }

    @Override
    public List<DiaChi> findAll() {
        return repoDiaChi.findAll();
    }

    @Override
    public DiaChi save(DiaChi diaChi) {
        return repoDiaChi.save(diaChi);
    }

    @Override
    public void deleteById(Integer id) {
        repoDiaChi.deleteById(id);
    }


    @Override
    public List<DiaChi> findByIdTaiKhoan(Integer idTaiKhoan) {
        return repoDiaChi.findByTaiKhoan_Id(idTaiKhoan);
    }

    @Override
    public boolean existsById(Integer id) {
        return repoDiaChi.existsById(id);
    }

    // ✅ THÊM: Method tìm địa chỉ mặc định
    public Optional<DiaChi> findByTaiKhoanIdAndIsDefaultTrue(Integer taiKhoanId) {
        return repoDiaChi.findByTaiKhoanIdAndIsDefaultTrue(taiKhoanId);
    }

    @Override
    @Transactional
    public void deleteByTaiKhoanId(Integer taiKhoanId) {
        try {
            System.out.println("Deleting addresses with taiKhoanId: " + taiKhoanId);

            List<DiaChi> diaChiList = findByTaiKhoanId(taiKhoanId);
            System.out.println("Found " + diaChiList.size() + " addresses to delete");

            for (DiaChi diaChi : diaChiList) {
                repoDiaChi.deleteById(diaChi.getId());
                System.out.println("Deleted address: " + diaChi.getId());
            }

            System.out.println("✅ All addresses deleted successfully");
        } catch (Exception e) {
            System.out.println("❌ Error deleting addresses: " + e.getMessage());
            throw new RuntimeException("Failed to delete addresses: " + e.getMessage(), e);
        }
    }

    // ====== HELPER METHODS (CẬP NHẬT - BỎ HUYỆN) ======
    private DiaChiDto convertToDto(DiaChi diaChi) {
        DiaChiDto dto = new DiaChiDto();
        dto.setId(diaChi.getId());
        dto.setTenTinh(diaChi.getTenTinh());
        dto.setTenPhuong(diaChi.getTenPhuong());
        dto.setMaTinh(diaChi.getMaTinh());
        dto.setMaPhuong(diaChi.getMaPhuong());
        dto.setDiaChiChiTiet(diaChi.getDiaChiChiTiet());
        dto.setTrangThai(diaChi.getTrangThai());

        if (diaChi.getTaiKhoan() != null) {
            dto.setIdTaiKhoan(diaChi.getTaiKhoan().getId());

            // Tìm KhachHang theo TaiKhoan
            var kh = repoKhachHang.findByTaiKhoanId(diaChi.getTaiKhoan().getId());
            if (kh.isPresent()) {
                dto.setTenKhachHang(kh.get().getHoTen());
            } else {
                // Nếu không phải KhachHang, kiểm tra có phải NhânViên không
                var nv = repoNhanVien.findByTaiKhoan_Id(diaChi.getTaiKhoan().getId());
                nv.ifPresent(nhanVien -> dto.setTenKhachHang(nhanVien.getHoTen()));
            }
        }

        return dto;
    }

    private DiaChi convertToEntity(DiaChiDto dto) {
        DiaChi diaChi = new DiaChi();
        diaChi.setId(dto.getId());
        diaChi.setTenTinh(dto.getTenTinh());
        diaChi.setTenPhuong(dto.getTenPhuong());
        // Set các trường khác nếu có trong DTO (BỎ HUYỆN)
        diaChi.setMaTinh(dto.getMaTinh() != null ? dto.getMaTinh() : "01");
        diaChi.setMaPhuong(dto.getMaPhuong() != null ? dto.getMaPhuong() : "00001");
        diaChi.setDiaChiChiTiet(dto.getDiaChiChiTiet() != null ? dto.getDiaChiChiTiet() : "");
        diaChi.setTrangThai(dto.getTrangThai() != null ? dto.getTrangThai() : 1);

        // Set TaiKhoan nếu có ID
        if (dto.getIdTaiKhoan() != null) {
            TaiKhoan taiKhoan = new TaiKhoan();
            taiKhoan.setId(dto.getIdTaiKhoan());
            diaChi.setTaiKhoan(taiKhoan);
        }

        return diaChi;
    }
}
