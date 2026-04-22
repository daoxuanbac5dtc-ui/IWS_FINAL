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

    // Láº¥y táº¥t ca cÃ¡c dá»¯ liá»‡u
    @GetMapping
    public List<DeGiay> getAllDeGiay() {
        return deGiayService.getAllDeGiay();
    }

    @GetMapping("/{id}")
    public DeGiay getDeGiayDto(@PathVariable int id) {
        return deGiayService.getDeGiayById(id).orElse(null);
    }
    //ThÃªm Ä‘Ã© giÃ y
    @PostMapping
    public String addKichCo(@Valid @RequestBody DeGiay deGiay) {
        deGiay.setNgayTao(new Date());
        deGiayService.addDeGiay(deGiay);
        return "ThÃªm kÃ­ch cá»¡ thÃ nh cÃ´ng !";
    }
    //Sá»­a
    @PutMapping("/{id}")
    public String updateKichCo(@PathVariable int id , @Valid @RequestBody DeGiay deGiay) {
        Optional<DeGiay> optional = deGiayService.getDeGiayById(id);
        if (optional.isEmpty()) {
            return "KhÃ´ng tÃ¬m tháº¥y Ä‘áº¿ giÃ y vá»›i ID: " + id;
        }
        deGiay.setId(id);
        deGiay.setNgayCapNhat(new Date());
        deGiayService.updateDeGiay(deGiay);
        return "ÄÃ£ sá»­a Ä‘áº¿ giÃ y thÃ nh cÃ´ng vá»›i id : " +id;
    }
    //XÃ³a Ä‘á» giÃ y
    @DeleteMapping("/{id}")
    public String deleteKichCo(@PathVariable int id) {
        Optional<DeGiay> optional = deGiayService.getDeGiayById(id);
        if (optional.isEmpty()) {
            return "KhÃ´ng tÃ¬m tháº¥y Ä‘áº¿ giÃ y vá»›i ID: " + id;
        }
        deGiayService.deleteDeGiay(id);
        return "ÄÃ£ xÃ³a thÃ nh cÃ´ng Ä‘áº¿ giÃ y vá»›i id : "+ id;
    }
}

