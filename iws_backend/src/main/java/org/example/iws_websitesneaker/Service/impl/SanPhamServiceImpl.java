package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Dto.SanPhamSearchResponse;
import org.example.iws_websitesneaker.Service.SanPhamService;
import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import org.example.iws_websitesneaker.entity.SanPham;
import org.example.iws_websitesneaker.repository.ChiTietSanPhamRepository;
import org.example.iws_websitesneaker.repository.RepoChiTietSanPham;
import org.example.iws_websitesneaker.repository.RepoSanPham;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate; // Sá»­ dá»¥ng jakarta.persistence
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SanPhamServiceImpl implements SanPhamService {

    @Autowired
    private RepoSanPham sanPhamRepository;

    @Autowired
    private RepoChiTietSanPham chiTietSanPhamRepository;

    @Autowired
    private ChiTietSanPhamRepository RepoChiTietSanPhamRepository;

    @Override
    public List<SanPham> getFiltered(String tenSanPham, Integer danhMucId, Integer thuongHieuId, Integer trangThai) {
        return sanPhamRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (tenSanPham != null && !tenSanPham.isEmpty()) {
                predicates.add(cb.like(root.get("tenSanPham"), "%" + tenSanPham + "%"));
            }
            if (danhMucId != null) {
                predicates.add(cb.equal(root.get("danhMuc").get("id"), danhMucId));
            }
            if (thuongHieuId != null) {
                predicates.add(cb.equal(root.get("thuongHieu").get("id"), thuongHieuId));
            }
            if (trangThai != null) {
                predicates.add(cb.equal(root.get("trangThai"), trangThai));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    @Override
    public Optional<SanPham> getById(Integer id) {
        return sanPhamRepository.findById(id);
    }

    @Override
    public SanPham save(SanPham sanPham) {
        if (sanPham.getSoLuong() == null) {
            sanPham.setSoLuong(0);
        }
        if (sanPham.getTrangThai() == null) {
            sanPham.setTrangThai(1);
        }
        return sanPhamRepository.save(sanPham);
    }

    @Override
    public void delete(Integer id) {
        chiTietSanPhamRepository.removeSanPhamReference(id); // Gá»¡ rÃ ng buá»™c khÃ³a ngoáº¡i tá»« báº£ng ctsp
        sanPhamRepository.deleteById(id);
    }
    @Override
    public List<SanPhamSearchResponse> searchProducts(String keyword) {
        List<ChiTietSanPham> products = RepoChiTietSanPhamRepository.searchByKeyword(keyword);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<SanPhamSearchResponse> getAllAvailableProducts() {
        List<ChiTietSanPham> products = RepoChiTietSanPhamRepository.findAllAvailable();
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public SanPhamSearchResponse findByQRCode(String qrCode) {
        List<ChiTietSanPham> products = RepoChiTietSanPhamRepository.findByMaQR(qrCode);
        if (!products.isEmpty()) {
            return convertToDTO(products.get(0));
        }
        return null;
    }

    private SanPhamSearchResponse convertToDTO(ChiTietSanPham ctsp) {
        SanPhamSearchResponse dto = new SanPhamSearchResponse();
        dto.setId(ctsp.getId());
        dto.setMaSanPham(ctsp.getSanPham().getMaSanPham());
        dto.setTenSanPham(ctsp.getSanPham().getTenSanPham());
        dto.setMaChiTiet(ctsp.getMaChiTiet());
        dto.setMaQR(ctsp.getMaQR());
        dto.setMauSac(ctsp.getMauSac() != null ? ctsp.getMauSac().getTenMauSac() : "");
        dto.setKichCo(ctsp.getKichCo() != null ? ctsp.getKichCo().getTenKichCo() : "");
        dto.setThuongHieu(ctsp.getSanPham().getThuongHieu() != null ?
                ctsp.getSanPham().getThuongHieu().getTenThuongHieu() : "");
        dto.setDanhMuc(ctsp.getSanPham().getDanhMuc() != null ?
                ctsp.getSanPham().getDanhMuc().getTenDanhMuc() : "");
        dto.setGiaBan(ctsp.getGiaBan());
        dto.setSoLuong(ctsp.getSoLuong());
        return dto;
    }
}
