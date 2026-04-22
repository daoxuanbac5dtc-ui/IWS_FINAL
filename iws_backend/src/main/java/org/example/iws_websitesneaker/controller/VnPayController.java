package org.example.iws_websitesneaker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@RestController
@RequestMapping("/api/vnpay")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class VnPayController {

    // Cáº¥u hÃ¬nh VNPay
    private final String vnp_TmnCode = "QOXX28F3";
    private final String vnp_HashSecret = "3I6XUNL7P6LO55MCZUNRKDFO8F159T2M";
    private final String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private final String vnp_ReturnUrl = "http://localhost:5173/payment-return";

    /**
     * API táº¡o URL thanh toÃ¡n VNPay
     */
    @PostMapping("/create-payment")
    public ResponseEntity<Map<String, Object>> createPayment(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        try {
            System.out.println("=== VNPay Create Payment DEBUG ===");
            System.out.println("Request: " + request);

            // Láº¥y thÃ´ng tin tá»« request
            String orderId = (String) request.get("orderId");
            Long amount = Long.valueOf(request.get("amount").toString());
            String orderInfo = (String) request.get("orderInfo");

            System.out.println("OrderId: " + orderId);
            System.out.println("Amount: " + amount);
            System.out.println("OrderInfo: " + orderInfo);

            // Validate dá»¯ liá»‡u Ä‘áº§u vÃ o
            if (orderId == null || orderId.trim().isEmpty()) {
                throw new IllegalArgumentException("OrderId khÃ´ng Ä‘Æ°á»£c rá»—ng");
            }
            if (amount == null || amount <= 0) {
                throw new IllegalArgumentException("Amount pháº£i lá»›n hÆ¡n 0");
            }
            if (orderInfo == null || orderInfo.trim().isEmpty()) {
                orderInfo = "Payment for order " + orderId; // DÃ¹ng tiáº¿ng Anh
            }

            // Táº¡o cÃ¡c tham sá»‘ cho VNPay
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", orderId);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", getClientIP(httpRequest));

            // Táº¡o thá»i gian
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            System.out.println("=== VNPay Parameters ===");
            for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            // Sáº¯p xáº¿p tham sá»‘ vÃ  táº¡o hash
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            System.out.println("=== Hash Data ===");
            System.out.println("HashData: " + hashData.toString());

            // Táº¡o secure hash - ÄÃƒ Sá»¬A: DÃ¹ng HMAC-SHA512
            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            System.out.println("SecureHash: " + vnp_SecureHash);

            query.append("&vnp_SecureHash=").append(vnp_SecureHash);

            // Táº¡o URL Ä‘áº§y Ä‘á»§
            String paymentUrl = vnp_Url + "?" + query.toString();

            System.out.println("=== Final Payment URL ===");
            System.out.println("URL Length: " + paymentUrl.length());
            System.out.println("Payment URL: " + paymentUrl);

            // Tráº£ vá» URL cho frontend
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentUrl", paymentUrl);
            response.put("orderId", orderId);
            response.put("debug", Map.of(
                    "hashData", hashData.toString(),
                    "secureHash", vnp_SecureHash,
                    "urlLength", paymentUrl.length()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("=== ERROR ===");
            System.out.println("Error: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lá»—i táº¡o thanh toÃ¡n: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * API xá»­ lÃ½ káº¿t quáº£ tráº£ vá» tá»« VNPay
     */
    @PostMapping("/payment-return")
    public ResponseEntity<Map<String, Object>> paymentReturn(@RequestBody Map<String, String> params) {

        try {
            System.out.println("=== VNPay Payment Return ===");
            System.out.println("Params: " + params);

            // Láº¥y cÃ¡c tham sá»‘ tá»« VNPay
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef");
            String vnp_Amount = params.get("vnp_Amount");
            String vnp_BankCode = params.get("vnp_BankCode");
            String vnp_TransactionNo = params.get("vnp_TransactionNo");
            String vnp_SecureHash = params.get("vnp_SecureHash");

            // XÃ³a vnp_SecureHash Ä‘á»ƒ verify
            params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");

            // Táº¡o láº¡i hash Ä‘á»ƒ kiá»ƒm tra
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = params.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }

            String myChecksum = hmacSHA512(vnp_HashSecret, hashData.toString());

            Map<String, Object> response = new HashMap<>();

            // Kiá»ƒm tra chá»¯ kÃ½
            if (!myChecksum.equals(vnp_SecureHash)) {
                response.put("success", false);
                response.put("message", "Chá»¯ kÃ½ khÃ´ng há»£p lá»‡");
                return ResponseEntity.badRequest().body(response);
            }

            // Kiá»ƒm tra káº¿t quáº£ thanh toÃ¡n
            if ("00".equals(vnp_ResponseCode)) {
                // Thanh toÃ¡n thÃ nh cÃ´ng
                System.out.println("Thanh toÃ¡n thÃ nh cÃ´ng: " + vnp_TxnRef);

                response.put("success", true);
                response.put("message", "Thanh toÃ¡n thÃ nh cÃ´ng");
                response.put("orderCode", vnp_TxnRef);
                response.put("amount", Long.parseLong(vnp_Amount) / 100);
                response.put("transactionNo", vnp_TransactionNo);
                response.put("bankCode", vnp_BankCode);

            } else {
                // Thanh toÃ¡n tháº¥t báº¡i
                System.out.println("Thanh toÃ¡n tháº¥t báº¡i: " + vnp_TxnRef + " - " + vnp_ResponseCode);

                response.put("success", false);
                response.put("message", getErrorMessage(vnp_ResponseCode));
                response.put("orderCode", vnp_TxnRef);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lá»—i xá»­ lÃ½ káº¿t quáº£ thanh toÃ¡n: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // === HELPER METHODS ===

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            String remoteAddr = request.getRemoteAddr();
            // Chuyá»ƒn IPv6 localhost thÃ nh IPv4
            if ("0:0:0:0:0:0:0:1".equals(remoteAddr)) {
                return "127.0.0.1";
            }
            return remoteAddr;
        }
        return xfHeader.split(",")[0].trim();
    }

    // ÄÃƒ Sá»¬A: DÃ¹ng HMAC-SHA512 thay vÃ¬ SHA-512 thÃ´ng thÆ°á»ng
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA512");
            hmac.init(secretKey);
            byte[] hashBytes = hmac.doFinal(data.getBytes("UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    private String getErrorMessage(String responseCode) {
        switch (responseCode) {
            case "24": return "KhÃ¡ch hÃ ng há»§y giao dá»‹ch";
            case "51": return "TÃ i khoáº£n khÃ´ng Ä‘á»§ sá»‘ dÆ°";
            case "65": return "TÃ i khoáº£n Ä‘Ã£ vÆ°á»£t quÃ¡ háº¡n má»©c giao dá»‹ch";
            case "75": return "NgÃ¢n hÃ ng Ä‘ang báº£o trÃ¬";
            case "07": return "Giao dá»‹ch bá»‹ nghi ngá»";
            case "09": return "Tháº» chÆ°a Ä‘Äƒng kÃ½ Internet Banking";
            case "10": return "XÃ¡c thá»±c thÃ´ng tin khÃ´ng Ä‘Ãºng quÃ¡ 3 láº§n";
            case "11": return "ÄÃ£ háº¿t háº¡n chá» thanh toÃ¡n";
            case "12": return "Tháº» bá»‹ khÃ³a";
            case "13": return "Máº­t kháº©u OTP khÃ´ng Ä‘Ãºng";
            case "99": return "Lá»—i khÃ´ng xÃ¡c Ä‘á»‹nh";
            default: return "Giao dá»‹ch tháº¥t báº¡i";
        }
    }
}
