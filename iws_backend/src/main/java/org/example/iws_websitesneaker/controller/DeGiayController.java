package org.example.iws_websitesneaker.controller;

import jakarta.validation.Valid;
import org.example.iws_websitesneaker.Dto.DeGiayDto;
import org.example.iws_websitesneaker.Service.DeGiayService;
import org.example.iws_websitesneaker.entity.DeGiay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/de-giay")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")


public class DeGiayController {
    @Autowired
    private DeGiayService deGiayService;

    // Lấy tất ca các dữ liệu
    @GetMapping
    public List<DeGiay> getAllDeGiay() {
        return deGiayService.getAllDeGiay();
    }

    @GetMapping("/{id}")
    public DeGiay getDeGiayDto(@PathVariable int id) {
        return deGiayService.getDeGiayById(id).orElse(null);
    }
    //Thêm đé giày
    @PostMapping
    public String addKichCo(@Valid @RequestBody DeGiay deGiay) {
        deGiay.setNgayTao(new Date());
        deGiayService.addDeGiay(deGiay);
        return "Thêm kích cỡ thành công !";
    }
    //Sửa
    @PutMapping("/{id}")
    public String updateKichCo(@PathVariable int id , @Valid @RequestBody DeGiay deGiay) {
        Optional<DeGiay> optional = deGiayService.getDeGiayById(id);
        if (optional.isEmpty()) {
            return "Không tìm thấy đế giày với ID: " + id;
        }
        deGiay.setId(id);
        deGiay.setNgayCapNhat(new Date());
        deGiayService.updateDeGiay(deGiay);
        return "Đã sửa đế giày thành công với id : " +id;
    }
    //Xóa đề giày
    @DeleteMapping("/{id}")
    public String deleteKichCo(@PathVariable int id) {
        Optional<DeGiay> optional = deGiayService.getDeGiayById(id);
        if (optional.isEmpty()) {
            return "Không tìm thấy đế giày với ID: " + id;
        }
        deGiayService.deleteDeGiay(id);
        return "Đã xóa thành công đế giày với id : "+ id;
    }
}

