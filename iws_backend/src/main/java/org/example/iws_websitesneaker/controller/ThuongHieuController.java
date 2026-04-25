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

    // HiÃªnr thá»‹ thÆ°Æ¡ng hiá»‡u
    @GetMapping
    public List<ThuongHieu> getAll(){
        return thuongHieuService.getAllThuongHieu();
    }

    //Detail thÆ°Æ¡ng hiá»‡u theo id
    @GetMapping("/{id}")
    public ThuongHieu getDetail(@PathVariable int id){
        return thuongHieuService.getThuongHieuById(id).orElse(null);
    }

    //ThÃªm thÆ°Æ¡ng hiá»‡u - ngÃ y táº¡o
    @PostMapping
    public String add(@Valid @RequestBody ThuongHieu thuongHieu){
        thuongHieu.setNgayTao(new Date());
        thuongHieuService.addThuongHieu(thuongHieu);
        return "ThÃªm thÃ nh cÃ´ng thÆ°Æ¡ng hiá»‡u !";
    }
    // Sá»­a thÆ°ong hiá»‡u - ngÃ y cáº­p nháº­t
    @PutMapping("/{id}")
    public String update(@PathVariable int id ,@Valid @RequestBody  ThuongHieu thuongHieu){
        thuongHieu.setId(id);
        thuongHieu.setNgayCapNhat(new Date());
        thuongHieuService.updateThuongHieu(thuongHieu);
        return "Sá»­a thÃ nh cÃ´ng thÆ°Æ¡ng hiá»‡u vá»›i id : " + id;
    }
    // XÃ³a thÆ°Æ¡ng hiá»‡u
    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id){
        thuongHieuService.deleteThuongHieuById(id);
        return "ÄÃ£ xÃ³a thÃ nh cÃ´ng thÆ°Æ¡ng hiá»‡u vá»›i id : " + id;
    }

}

