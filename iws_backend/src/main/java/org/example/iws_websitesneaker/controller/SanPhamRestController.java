package org.example.iws_websitesneaker.controller;

import org.example.iws_websitesneaker.Service.SanPhamService;
import org.example.iws_websitesneaker.entity.MauSac;
import org.example.iws_websitesneaker.entity.SanPham;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/san-pham")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SanPhamRestController {
    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping("")
    public List<SanPham> getFiltered(
            @RequestParam(required = false) String tenSanPham,
            @RequestParam(required = false) Integer danhMucId,
            @RequestParam(required = false) Integer thuongHieuId,
            @RequestParam(required = false) Integer trangThai
    ) {
        return sanPhamService.getFiltered(tenSanPham, danhMucId, thuongHieuId, trangThai);
    }

    @PostMapping("save")
    public ResponseEntity<SanPham> save(@RequestBody SanPham sanPham) {
        sanPham.setNgayTao(new Date());
        sanPhamService.save(sanPham);
        return ResponseEntity.ok(sanPham);
    }
    @PutMapping("update/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody SanPham sanPham) {
        Optional<SanPham> optional = sanPhamService.getById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.ok("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m vá»›i ID: " + id);
        }
        sanPham.setId(id);
        sanPham.setNgayCapNhat(new Date());
        sanPhamService.save(sanPham);
        return ResponseEntity.ok("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m vá»›i ID: " + id);
    }
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (sanPhamService.getById(id).isPresent()) {
            sanPhamService.delete(id);
            return ResponseEntity.ok().body("XoÃ¡ thÃ nh cÃ´ng!");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

