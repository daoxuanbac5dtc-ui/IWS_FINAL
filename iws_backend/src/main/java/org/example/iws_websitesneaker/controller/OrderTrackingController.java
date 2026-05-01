package org.example.iws_websitesneaker.controller;

import org.example.iws_websitesneaker.Dto.OrderTrackingResponse;
import org.example.iws_websitesneaker.Service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class OrderTrackingController {

    @Autowired
    private HoaDonService hoaDonService;

    @GetMapping("/track")
    public ResponseEntity<?> trackOrder(@RequestParam String email, @RequestParam String orderCode) {
        try {
            System.out.println("=== TRACK ORDER ===");
            System.out.println("Email: " + email + ", Order Code: " + orderCode);

            // Service tự xử lý tìm đơn + convert sang DTO
            OrderTrackingResponse response = hoaDonService.getTrackingResponse(email, orderCode);

            if (response == null) {
                System.out.println("Order not found");
                return ResponseEntity.notFound().build();
            }

            System.out.println("Order found: " + response.getOrderId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error tracking order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi tra cứu đơn hàng");
        }
    }
}

