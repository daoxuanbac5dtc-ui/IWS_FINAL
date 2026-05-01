package org.example.iws_websitesneaker.controller;

import jakarta.validation.Valid;
import org.example.iws_websitesneaker.Dto.MauSacDto;
import org.example.iws_websitesneaker.Service.MauSacService;
import org.example.iws_websitesneaker.entity.MauSac;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mau-sac")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")


public class MauSacController {
    @Autowired
    private MauSacService mauSacService;

    //Lấy tất cả các dữ liệu cảu màu sắc
    @GetMapping
    public List<MauSac> getAllMauSac() {
        return mauSacService.getAll();
    }
    // Detail tất car các dữ liệu màu sắc
    @GetMapping("/{id}")
    public MauSac getMauSacById(@PathVariable int id){
        return mauSacService.getById(id).orElse(null);
    }
    //Thêm màu sắc - ngày tạo
    @PostMapping
    public String addMauSac(@Valid @RequestBody MauSac mauSac) {
        mauSac.setNgayTao(new Date());
        mauSacService.add(mauSac);
        return "Thêm thành công màu sắc !";
    }
    //Sửa màu sắc
    @PutMapping("/{id}")
    public String update(@PathVariable int id ,@Valid @RequestBody MauSac mauSac){
        Optional<MauSac> optional = mauSacService.getById(id);
        if (optional.isEmpty()) {
            return "Không tìm thấy màu sắc với ID: " + id;
        }
        mauSac.setId(id);
        mauSac.setNgayCapNhat(new Date());
        mauSacService.update(mauSac);
        return "Sửa thành công màu sắc với id : " + id;
    }
    //Xóa màu sắc
    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id){
        Optional<MauSac> optional = mauSacService.getById(id);
        if (optional.isEmpty()) {
            return "Không tìm thấy màu sắc với ID: " + id;
        }
        mauSacService.delete(id);
        return "Đã xóa thành công màu sắc với id :" + id ;
    }


}

