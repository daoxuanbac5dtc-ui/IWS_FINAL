package org.example.iws_websitesneaker.Service;

import org.example.iws_websitesneaker.Dto.ProvinceApiDto;
import org.example.iws_websitesneaker.Dto.WardApiDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VietnamAddressService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Cache configuration
    private static final long CACHE_DURATION = 24 * 60 * 60 * 1000L; // 24 giờ

    // Cache cho provinces
    private List<ProvinceApiDto> cachedProvinces = null;
    private long lastProvincesFetch = 0;

    // Cache cho wards theo province
    private final Map<Integer, List<WardApiDto>> wardsCache = new ConcurrentHashMap<>();
    private final Map<Integer, Long> wardsCacheTime = new ConcurrentHashMap<>();

    public VietnamAddressService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Lấy tất cả tỉnh/thành phố từ API bên ngoài
     */
    public List<ProvinceApiDto> getAllProvinces() {
        // Kiểm tra cache
        if (cachedProvinces != null &&
                (System.currentTimeMillis() - lastProvincesFetch) < CACHE_DURATION) {
            return cachedProvinces;
        }

        try {
            String url = "https://provinces.open-api.vn/api/p/";
            String response = restTemplate.getForObject(url, String.class);

            JsonNode jsonNode = objectMapper.readTree(response);
            List<ProvinceApiDto> provinces = new ArrayList<>();

            for (JsonNode node : jsonNode) {
                ProvinceApiDto province = new ProvinceApiDto();
                province.setCode(node.get("code").asInt());
                province.setName(node.get("name").asText());
                province.setCodename(node.path("codename").asText());
                provinces.add(province);
            }

            // Cập nhật cache
            cachedProvinces = provinces;
            lastProvincesFetch = System.currentTimeMillis();

            return provinces;

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi API tỉnh thành: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Lấy danh sách wards theo mã tỉnh với caching
     */
    public List<WardApiDto> getWardsByProvinceCode(Integer provinceCode) {
        // Kiểm tra cache
        if (wardsCache.containsKey(provinceCode) &&
                (System.currentTimeMillis() - wardsCacheTime.getOrDefault(provinceCode, 0L)) < CACHE_DURATION) {
            return wardsCache.get(provinceCode);
        }

        try {
            String url = String.format("https://provinces.open-api.vn/api/p/%d?depth=3", provinceCode);
            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.trim().isEmpty()) {
                return new ArrayList<>();
            }

            // Xử lý response và cache kết quả
            List<WardApiDto> wards = processWardsResponse(response);
            wardsCache.put(provinceCode, wards);
            wardsCacheTime.put(provinceCode, System.currentTimeMillis());

            return wards;
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy wards cho tỉnh " + provinceCode + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Xử lý response từ API và trích xuất danh sách wards
     */
    private List<WardApiDto> processWardsResponse(String response) {
        List<WardApiDto> wards = new ArrayList<>();

        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode districts = jsonNode.get("districts");

            if (districts != null && districts.isArray()) {
                for (JsonNode district : districts) {
                    JsonNode wardsNode = district.get("wards");
                    if (wardsNode != null && wardsNode.isArray()) {
                        for (JsonNode wardNode : wardsNode) {
                            WardApiDto ward = new WardApiDto();
                            ward.setCode(wardNode.get("code").asInt());
                            ward.setName(wardNode.get("name").asText());
                            ward.setCodename(wardNode.path("codename").asText());
                            wards.add(ward);
                        }
                    }
                }
            }

            // Sắp xếp theo tên
            wards.sort((w1, w2) -> w1.getName().compareToIgnoreCase(w2.getName()));

        } catch (Exception e) {
            System.err.println("Lỗi khi xử lý response wards: " + e.getMessage());
        }

        return wards;
    }

    /**
     * Lấy tất cả xã/phường trên toàn quốc (method gốc giữ nguyên)
     */
    public List<WardApiDto> getAllWardsInVietnam() {
        List<WardApiDto> allWards = new ArrayList<>();

        try {
            // Lấy tất cả provinces
            List<ProvinceApiDto> provinces = getAllProvinces();

            for (ProvinceApiDto province : provinces) {
                try {
                    List<WardApiDto> provinceWards = getWardsByProvinceCode(province.getCode());

                    // Thêm thông tin tỉnh vào tên ward để phân biệt
                    for (WardApiDto ward : provinceWards) {
                        ward.setName(ward.getName() + " (" + province.getName() + ")");
                    }

                    allWards.addAll(provinceWards);
                } catch (Exception e) {
                    System.err.println("Lỗi khi lấy wards cho tỉnh " + province.getName() + ": " + e.getMessage());
                }
            }

            // Sắp xếp theo tên
            return allWards.stream()
                    .sorted((w1, w2) -> w1.getName().compareToIgnoreCase(w2.getName()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Lỗi khi lấy toàn bộ wards: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Clear cache - để test hoặc refresh data
     */
    public void clearCache() {
        cachedProvinces = null;
        lastProvincesFetch = 0;
        wardsCache.clear();
        wardsCacheTime.clear();
        System.out.println("Cache đã được xóa");
    }
}
