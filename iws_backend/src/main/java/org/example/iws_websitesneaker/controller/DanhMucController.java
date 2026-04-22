package org.example.iws_websitesneaker.controller;

import jakarta.validation.Valid;
import org.example.iws_websitesneaker.Dto.ChatLieuDto;
import org.example.iws_websitesneaker.Dto.DanhMucDto;
import org.example.iws_websitesneaker.Service.DanhMucService;
import org.example.iws_websitesneaker.entity.ChatLieu;
import org.example.iws_websitesneaker.entity.DanhMuc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/danh-muc")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class DanhMucController {
    @Autowired
    private DanhMucService danhMucService;

    @GetMapping
    public List<DanhMuc> getAllDanhMuc(){
        return danhMucService.getDanhMuc();
    }

    @GetMapping("/{id}")
    public DanhMuc getDanhMucById(@PathVariable int id){
        return danhMucService.getDanhMucById(id)
                .orElse(null);
    }
    @PostMapping
    public String addDanhMuc(@Valid @RequestBody DanhMuc danhMuc){
        danhMuc.setNgayTao(new Date());
        danhMucService.addDanhMuc(danhMuc);
        return "ThÃªm thÃ nh cÃ´ng danh má»¥c";
    }
    //Update cháº¥t liá»‡u
    @PutMapping("/{id}")
    public String updateDanhMuc(@PathVariable int id,@Valid @RequestBody  DanhMuc danhMuc){
        Optional<DanhMuc> optional = danhMucService.getDanhMucById(id);
        if (optional.isEmpty()) {
            return "KhÃ´ng tÃ¬m tháº¥y danh má»¥c vá»›i ID: " + id;
        }
        danhMuc.setId(id);
        danhMuc.setNgayCapNhat(new Date());
        danhMucService.updateDanhMuc(danhMuc);
        return "ÄÃ£ sá»­a thÃ nh cÃ´ng danh má»¥c vá»›i id : " +id;
    }
    //Delete cháº¥t liá»‡u
    @DeleteMapping("/{id}")
    public String deleteChatLieu(@PathVariable int id){
        Optional<DanhMuc> optional = danhMucService.getDanhMucById(id);
        if (optional.isEmpty()) {
            return "KhÃ´ng tÃ¬m tháº¥y danh má»¥c vá»›i ID: " + id;
        }
        danhMucService.deleteDanhMuc(id);
        return "ÄÃ£ xÃ³a danh má»¥c vá»›i id : " +id;
    }

}

