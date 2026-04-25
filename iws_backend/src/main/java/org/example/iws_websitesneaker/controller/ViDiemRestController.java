//package org.example.iws_websitesneaker.controller;
//
//import org.example.iws_websitesneaker.entity.ViDiem;
//import org.example.iws_websitesneaker.Service.ViDiemService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@RestController
//@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
//@RequestMapping("/api/vi-diem")
//public class ViDiemRestController {
//
//    @Autowired
//    private ViDiemService viDiemService;
//
//    // GET - Láº¥y danh sÃ¡ch táº¥t cáº£ vÃ­ Ä‘iá»ƒm
//    @GetMapping
//    public ResponseEntity<List<ViDiem>> getAllViDiem() {
//        try {
//            List<ViDiem> viDiems = viDiemService.findAll();
//            return ResponseEntity.ok(viDiems);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // GET - Láº¥y vÃ­ Ä‘iá»ƒm theo ID
//    @GetMapping("/{id}")
//    public ResponseEntity<ViDiem> getViDiemById(@PathVariable Integer id) {
//        try {
//            Optional<ViDiem> viDiem = viDiemService.findById(id);
//            return viDiem.map(ResponseEntity::ok)
//                    .orElse(ResponseEntity.notFound().build());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // POST - Táº¡o vÃ­ Ä‘iá»ƒm má»›i
//    @PostMapping
//    public ResponseEntity<ViDiem> createViDiem(@RequestBody ViDiem viDiem) {
//        try {
//            // Set thá»i gian táº¡o
//            viDiem.setNgayTao(new Date());
//            viDiem.setNgayCapNhat(new Date());
//
//            // Set giÃ¡ trá»‹ máº·c Ä‘á»‹nh náº¿u chÆ°a cÃ³
//            if (viDiem.getTongDiem() == null) {
//                viDiem.setTongDiem(0.0);
//            }
//            if (viDiem.getSoDiemDaDung() == null) {
//                viDiem.setSoDiemDaDung(0.0);
//            }
//            if (viDiem.getSoDiemDaCong() == null) {
//                viDiem.setSoDiemDaCong(0.0);
//            }
//            if (viDiem.getGiaTriDiem() == null) {
//                viDiem.setGiaTriDiem(1000.0);
//            }
//
//            ViDiem savedViDiem = viDiemService.save(viDiem);
//            return ResponseEntity.status(HttpStatus.CREATED).body(savedViDiem);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // PUT - Cáº­p nháº­t vÃ­ Ä‘iá»ƒm
//    @PutMapping("/{id}")
//    public ResponseEntity<ViDiem> updateViDiem(@PathVariable Integer id, @RequestBody ViDiem viDiem) {
//        try {
//            Optional<ViDiem> existingViDiemOpt = viDiemService.findById(id);
//
//            if (!existingViDiemOpt.isPresent()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            ViDiem existingViDiem = existingViDiemOpt.get();
//
//            // Cáº­p nháº­t thÃ´ng tin
//            existingViDiem.setTongDiem(viDiem.getTongDiem());
//            existingViDiem.setSoDiemDaDung(viDiem.getSoDiemDaDung());
//            existingViDiem.setSoDiemDaCong(viDiem.getSoDiemDaCong());
//            existingViDiem.setGiaTriDiem(viDiem.getGiaTriDiem());
//            existingViDiem.setNgayCapNhat(new Date());
//
//            ViDiem updatedViDiem = viDiemService.save(existingViDiem);
//            return ResponseEntity.ok(updatedViDiem);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // DELETE - XÃ³a vÃ­ Ä‘iá»ƒm
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteViDiem(@PathVariable Integer id) {
//        try {
//            Optional<ViDiem> viDiem = viDiemService.findById(id);
//
//            if (!viDiem.isPresent()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            viDiemService.deleteById(id);
//            return ResponseEntity.noContent().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // POST - Cá»™ng Ä‘iá»ƒm
//    @PostMapping("/{id}/cong-diem")
//    public ResponseEntity<ViDiem> congDiem(@PathVariable Integer id, @RequestBody Map<String, Double> request) {
//        try {
//            Double diem = request.get("diem");
//            if (diem == null || diem <= 0) {
//                return ResponseEntity.badRequest().build();
//            }
//
//            ViDiem updatedViDiem = viDiemService.congDiem(id, diem);
//            return ResponseEntity.ok(updatedViDiem);
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // POST - Trá»« Ä‘iá»ƒm (sá»­ dá»¥ng Ä‘iá»ƒm)
//    @PostMapping("/{id}/tru-diem")
//    public ResponseEntity<ViDiem> truDiem(@PathVariable Integer id, @RequestBody Map<String, Double> request) {
//        try {
//            Double diem = request.get("diem");
//            if (diem == null || diem <= 0) {
//                return ResponseEntity.badRequest().build();
//            }
//
//            ViDiem updatedViDiem = viDiemService.truDiem(id, diem);
//            return ResponseEntity.ok(updatedViDiem);
//        } catch (RuntimeException e) {
//            Map<String, String> errorResponse = new HashMap<>();
//            errorResponse.put("error", e.getMessage());
//            return ResponseEntity.badRequest().body(null);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // GET - Láº¥y Ä‘iá»ƒm hiá»‡n táº¡i
//    @GetMapping("/{id}/diem-hien-tai")
//    public ResponseEntity<Map<String, Double>> getDiemHienTai(@PathVariable Integer id) {
//        try {
//            Optional<ViDiem> viDiemOpt = viDiemService.findById(id);
//            if (!viDiemOpt.isPresent()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            ViDiem viDiem = viDiemOpt.get();
//            Double diemHienTai = viDiemService.getDiemHienTai(id);
//
//            Map<String, Double> response = new HashMap<>();
//            response.put("diemHienTai", diemHienTai);
//            response.put("tongDiem", viDiem.getTongDiem());
//            response.put("soDiemDaDung", viDiem.getSoDiemDaDung());
//            response.put("soDiemDaCong", viDiem.getSoDiemDaCong());
//            response.put("giaTriDiem", viDiem.getGiaTriDiem());
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // GET - TÃ­nh giÃ¡ trá»‹ tiá»n cá»§a Ä‘iá»ƒm hiá»‡n táº¡i
//    @GetMapping("/{id}/gia-tri-tien")
//    public ResponseEntity<Map<String, Double>> getGiaTriTien(@PathVariable Integer id) {
//        try {
//            Double diemHienTai = viDiemService.getDiemHienTai(id);
//            Double giaTriTien = viDiemService.getGiaTriTien(id);
//
//            Optional<ViDiem> viDiemOpt = viDiemService.findById(id);
//            ViDiem viDiem = viDiemOpt.get();
//
//            Map<String, Double> response = new HashMap<>();
//            response.put("diemHienTai", diemHienTai);
//            response.put("giaTriDiem", viDiem.getGiaTriDiem());
//            response.put("giaTriTien", giaTriTien);
//
//            return ResponseEntity.ok(response);
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//}viDiems);
//        } catch (Exception e) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//                }
//
//// GET - Láº¥y vÃ­ Ä‘iá»ƒm theo ID
//@GetMapping("/{id}")
//public ResponseEntity<ViDiem> getViDiemById(@PathVariable Integer id) {
//    try {
//        Optional<ViDiem> viDiem = viDiemService.findById(id);
//        return viDiem.map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    } catch (Exception e) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    }
//}
//
//// POST - Táº¡o vÃ­ Ä‘iá»ƒm má»›i
//@PostMapping
//public ResponseEntity<ViDiem> createViDiem(@RequestBody ViDiem viDiem) {
//    try {
//        // Set thá»i gian táº¡o
//        viDiem.setNgayTao(new Date());
//        viDiem.setNgayCapNhat(new Date());
//
//        // Set giÃ¡ trá»‹ máº·c Ä‘á»‹nh náº¿u chÆ°a cÃ³
//        if (viDiem.getDiemHienTai() == null) {
//            viDiem.setDiemHienTai(0);
//        }
//        if (viDiem.getTongDiemDaDung() == null) {
//            viDiem.setTongDiemDaDung(0);
//        }
//        if (viDiem.getTongDiemTichLuy() == null) {
//            viDiem.setTongDiemTichLuy(0);
//        }
//        if (viDiem.getTrangThai() == null) {
//            viDiem.setTrangThai(1);
//        }
//
//        ViDiem savedViDiem = viDiemService.save(viDiem);
//        return ResponseEntity.status(HttpStatus.CREATED).body(savedViDiem);
//    } catch (Exception e) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    }
//}
//
//// PUT - Cáº­p nháº­t vÃ­ Ä‘iá»ƒm
//@PutMapping("/{id}")
//public ResponseEntity<ViDiem> updateViDiem(@PathVariable Integer id, @RequestBody ViDiem viDiem) {
//    try {
//        Optional<ViDiem> existingViDiemOpt = viDiemService.findById(id);
//
//        if (!existingViDiemOpt.isPresent()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        ViDiem existingViDiem = existingViDiemOpt.get();
//
//        // Cáº­p nháº­t thÃ´ng tin
//        existingViDiem.setDiemHienTai(viDiem.getDiemHienTai());
//        existingViDiem.setTongDiemDaDung(viDiem.getTongDiemDaDung());
//        existingViDiem.setTongDiemTichLuy(viDiem.getTongDiemTichLuy());
//        existingViDiem.setTrangThai(viDiem.getTrangThai());
//        existingViDiem.setNgayCapNhat(new Date());
//
//        ViDiem updatedViDiem = viDiemService.save(existingViDiem);
//        return ResponseEntity.ok(updatedViDiem);
//    } catch (Exception e) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    }
//}
//
//// DELETE - XÃ³a vÃ­ Ä‘iá»ƒm
//@DeleteMapping("/{id}")
//public ResponseEntity<Void> deleteViDiem(@PathVariable Integer id) {
//    try {
//        Optional<ViDiem> viDiem = viDiemService.findById(id);
//
//        if (!viDiem.isPresent()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        viDiemService.deleteById(id);
//        return ResponseEntity.noContent().build();
//    } catch (Exception e) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    }
//}
//
//// PATCH - Cáº­p nháº­t Ä‘iá»ƒm
//@PatchMapping("/{id}/diem")
//public ResponseEntity<ViDiem> updateDiem(@PathVariable Integer id,
//                                         @RequestParam Integer diemMoi,
//                                         @RequestParam(required = false) String loaiGiaoDich) {
//    try {
//        Optional<ViDiem> viDiemOpt = viDiemService.findById(id);
//
//        if (!viDiemOpt.isPresent()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        ViDiem viDiem = viDiemOpt.get();
//
//        if ("TICH_LUY".equals(loaiGiaoDich)) {
//            // TÃ­ch lÅ©y Ä‘iá»ƒm
//            viDiem.setDiemHienTai(viDiem.getDiemHienTai() + diemMoi);
//            viDiem.setTongDiemTichLuy(viDiem.getTongDiemTichLuy() + diemMoi);
//        } else if ("SU_DUNG".equals(loaiGiaoDich)) {
//            // Sá»­ dá»¥ng Ä‘iá»ƒm
//            if (viDiem.getDiemHienTai() >= diemMoi) {
//                viDiem.setDiemHienTai(viDiem.getDiemHienTai() - diemMoi);
//                viDiem.setTongDiemDaDung(viDiem.getTongDiemDaDung() + diemMoi);
//            } else {
//                return ResponseEntity.badRequest().build();
//            }
//        }
//
//        viDiem.setNgayCapNhat(new Date());
//        ViDiem updatedViDiem = viDiemService.save(viDiem);
//        return ResponseEntity.ok(updatedViDiem);
//    } catch (Exception e) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    }
//}
//}
