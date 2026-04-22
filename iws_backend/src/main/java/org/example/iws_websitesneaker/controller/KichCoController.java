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
    //Them kich cá»¡ - ngÃ y táº¡o
    @PostMapping
    public String addKichCo(@Valid  @RequestBody KichCo kichCo){
        kichCo.setNgayTao(new Date());
        kichCoService.addKichCo(kichCo);
        return "ÄÃ£ thÃªm thÃ nh cÃ´ng kÃ­ch cá»¡!";
    }
    //Sá»­a - ngÃ y cáº­p nháº­t
    @PutMapping("/{id}")
    public String updateKichCo( @PathVariable int id , @Valid   @RequestBody  KichCo kichCo){
        Optional<KichCo> optional = kichCoService.getKichCoById(id);
        if (optional.isEmpty()) {
            return "KhÃ´ng tÃ¬m tháº¥y kÃ­ch cá»¡ vá»›i ID: " + id;
        }
        kichCo.setId(id);
        kichCo.setNgayCapNhat(new Date());
        kichCoService.updateKichCo(kichCo);
        return "ÄÃ£ sá»­a thÃ nh cÃ´ng kÃ­ch cá»¡ vá»›i id : " + id ;
    }
    //XÃ³a kÃ­ch cá»¡
    @DeleteMapping("/{id}")
    public String deleteKichCo(@PathVariable int id){
        Optional<KichCo> optional = kichCoService.getKichCoById(id);
        if (optional.isEmpty()) {
            return "KhÃ´ng tÃ¬m tháº¥y kÃ­ch cá»¡ vá»›i ID: " + id;
        }
        kichCoService.deleteKichCo(id);
        return "ÄÃ£ xÃ³a thÃ nh cÃ´ng kÃ­ch cá»¡ vá»›i id : " + id ;
    }
}

