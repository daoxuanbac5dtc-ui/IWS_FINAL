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

    //Láº¥y táº¥t cáº£ cÃ¡c dá»¯ liá»‡u cáº£u mÃ u sáº¯c
    @GetMapping
    public List<MauSac> getAllMauSac() {
        return mauSacService.getAll();
    }
    // Detail táº¥t car cÃ¡c dá»¯ liá»‡u mÃ u sáº¯c
    @GetMapping("/{id}")
    public MauSac getMauSacById(@PathVariable int id){
        return mauSacService.getById(id).orElse(null);
    }
    //ThÃªm mÃ u sáº¯c - ngÃ y táº¡o
    @PostMapping
    public String addMauSac(@Valid @RequestBody MauSac mauSac) {
        mauSac.setNgayTao(new Date());
        mauSacService.add(mauSac);
        return "ThÃªm thÃ nh cÃ´ng mÃ u sáº¯c !";
    }
    //Sá»­a mÃ u sáº¯c
    @PutMapping("/{id}")
    public String update(@PathVariable int id ,@Valid @RequestBody MauSac mauSac){
        Optional<MauSac> optional = mauSacService.getById(id);
        if (optional.isEmpty()) {
            return "KhÃ´ng tÃ¬m tháº¥y mÃ u sáº¯c vá»›i ID: " + id;
        }
        mauSac.setId(id);
        mauSac.setNgayCapNhat(new Date());
        mauSacService.update(mauSac);
        return "Sá»­a thÃ nh cÃ´ng mÃ u sáº¯c vá»›i id : " + id;
    }
    //XÃ³a mÃ u sáº¯c
    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id){
        Optional<MauSac> optional = mauSacService.getById(id);
        if (optional.isEmpty()) {
            return "KhÃ´ng tÃ¬m tháº¥y mÃ u sáº¯c vá»›i ID: " + id;
        }
        mauSacService.delete(id);
        return "ÄÃ£ xÃ³a thÃ nh cÃ´ng mÃ u sáº¯c vá»›i id :" + id ;
    }


}

