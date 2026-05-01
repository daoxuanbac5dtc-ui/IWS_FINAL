package org.example.iws_websitesneaker.controller;

import jakarta.validation.Valid;
import org.example.iws_websitesneaker.Dto.KichCoDto;
import org.example.iws_websitesneaker.Service.KichCoService;
import org.example.iws_websitesneaker.entity.KichCo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/kich-co")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")

public class KichCoController {
    @Autowired
    private KichCoService kichCoService;

    @GetMapping
    public List<KichCo> getAllKichCo() {
        return kichCoService.getAllKichCo();
    }
    // Detail
    @GetMapping("/{id}")
    public KichCo getKichCoDetail(@PathVariable int id){
        return kichCoService.getKichCoById(id).orElse(null);
    }
    //Them kich cỡ - ngày tạo
    @PostMapping
    public String addKichCo(@Valid  @RequestBody KichCo kichCo){
        kichCo.setNgayTao(new Date());
        kichCoService.addKichCo(kichCo);
        return "Đã thêm thành công kích cỡ!";
    }
    //Sửa - ngày cập nhật
    @PutMapping("/{id}")
    public String updateKichCo( @PathVariable int id , @Valid   @RequestBody  KichCo kichCo){
        Optional<KichCo> optional = kichCoService.getKichCoById(id);
        if (optional.isEmpty()) {
            return "Không tìm thấy kích cỡ với ID: " + id;
        }
        kichCo.setId(id);
        kichCo.setNgayCapNhat(new Date());
        kichCoService.updateKichCo(kichCo);
        return "Đã sửa thành công kích cỡ với id : " + id ;
    }
    //Xóa kích cỡ
    @DeleteMapping("/{id}")
    public String deleteKichCo(@PathVariable int id){
        Optional<KichCo> optional = kichCoService.getKichCoById(id);
        if (optional.isEmpty()) {
            return "Không tìm thấy kích cỡ với ID: " + id;
        }
        kichCoService.deleteKichCo(id);
        return "Đã xóa thành công kích cỡ với id : " + id ;
    }
}

