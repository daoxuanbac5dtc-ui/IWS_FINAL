package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.DiaChiDto;
import org.example.iws_websitesneaker.entity.DiaChi;

import java.util.List;
import java.util.Optional;

public interface DiaChiService {
    // CÃ¡c method hiá»‡n táº¡i (DTO-based) - GIá»® NGUYÃŠN
    List<DiaChiDto> getAll();
    DiaChiDto getById(Integer id);
    DiaChiDto add(DiaChiDto diachiDto);
    DiaChiDto update(Integer id, DiaChiDto diachiDto);
    void delete(Integer id);
    List<DiaChiDto> search(String keyword);
    List<DiaChiDto> findByTenKhachHang(String tenKhachHang);
    List<DiaChiDto> findByTenTinh(String tenTinh);
    Optional<DiaChi> findDefaultByTaiKhoanId(Integer taiKhoanId);
    // CÃ¡c method cáº§n thiáº¿t cho controller (Entity-based)
    Optional<DiaChi> findById(Integer id);
    List<DiaChi> findAll();
    DiaChi save(DiaChi diaChi);
    void deleteById(Integer id);
    List<DiaChi> findByTaiKhoanId(Integer taiKhoanId);
    boolean existsById(Integer id);
    void deleteByTaiKhoanId(Integer taiKhoanId);
    List<DiaChi> findByIdTaiKhoan(Integer idTaiKhoan);
}

