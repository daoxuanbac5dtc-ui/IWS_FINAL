package org.example.iws_websitesneaker.controller;

import jakarta.validation.Valid;
import org.example.iws_websitesneaker.Dto.ThuongHieuDto;
import org.example.iws_websitesneaker.Service.ThuongHieuService;
import org.example.iws_websitesneaker.entity.ThuongHieu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/thuong-hieu")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")

public class ThuongHieuController {
    @Autowired
    private ThuongHieuService thuongHieuService;

    // Hiênr thị thương hiệu
    @GetMapping
    public List<ThuongHieu> getAll(){
        return thuongHieuService.getAllThuongHieu();
    }

    //Detail thương hiệu theo id
    @GetMapping("/{id}")
    public ThuongHieu getDetail(@PathVariable int id){
        return thuongHieuService.getThuongHieuById(id).orElse(null);
    }

    //Thêm thương hiệu - ngày tạo
    @PostMapping
    public String add(@Valid @RequestBody ThuongHieu thuongHieu){
        thuongHieu.setNgayTao(new Date());
        thuongHieuService.addThuongHieu(thuongHieu);
        return "Thêm thành công thương hiệu !";
    }
    // Sửa thưong hiệu - ngày cập nhật
    @PutMapping("/{id}")
    public String update(@PathVariable int id ,@Valid @RequestBody  ThuongHieu thuongHieu){
        thuongHieu.setId(id);
        thuongHieu.setNgayCapNhat(new Date());
        thuongHieuService.updateThuongHieu(thuongHieu);
        return "Sửa thành công thương hiệu với id : " + id;
    }
    // Xóa thương hiệu
    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id){
        thuongHieuService.deleteThuongHieuById(id);
        return "Đã xóa thành công thương hiệu với id : " + id;
    }

}

