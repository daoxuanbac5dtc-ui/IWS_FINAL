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
    private static final long CACHE_DURATION = 24 * 60 * 60 * 1000L; // 24 giá»

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
     * Láº¥y táº¥t cáº£ tá»‰nh/thÃ nh phá»‘ tá»« API bÃªn ngoÃ i
     */
    public List<ProvinceApiDto> getAllProvinces() {
        // Kiá»ƒm tra cache
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

            // Cáº­p nháº­t cache
            cachedProvinces = provinces;
            lastProvincesFetch = System.currentTimeMillis();

            return provinces;

        } catch (Exception e) {
            System.err.println("Lá»—i khi gá»i API tá»‰nh thÃ nh: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Láº¥y danh sÃ¡ch wards theo mÃ£ tá»‰nh vá»›i caching
     */
    public List<WardApiDto> getWardsByProvinceCode(Integer provinceCode) {
        // Kiá»ƒm tra cache
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

            // Xá»­ lÃ½ response vÃ  cache káº¿t quáº£
            List<WardApiDto> wards = processWardsResponse(response);
            wardsCache.put(provinceCode, wards);
            wardsCacheTime.put(provinceCode, System.currentTimeMillis());

            return wards;
        } catch (Exception e) {
            System.err.println("Lá»—i khi láº¥y wards cho tá»‰nh " + provinceCode + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Xá»­ lÃ½ response tá»« API vÃ  trÃ­ch xuáº¥t danh sÃ¡ch wards
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

            // Sáº¯p xáº¿p theo tÃªn
            wards.sort((w1, w2) -> w1.getName().compareToIgnoreCase(w2.getName()));

        } catch (Exception e) {
            System.err.println("Lá»—i khi xá»­ lÃ½ response wards: " + e.getMessage());
        }

        return wards;
    }

    /**
     * Láº¥y táº¥t cáº£ xÃ£/phÆ°á»ng trÃªn toÃ n quá»‘c (method gá»‘c giá»¯ nguyÃªn)
     */
    public List<WardApiDto> getAllWardsInVietnam() {
        List<WardApiDto> allWards = new ArrayList<>();

        try {
            // Láº¥y táº¥t cáº£ provinces
            List<ProvinceApiDto> provinces = getAllProvinces();

            for (ProvinceApiDto province : provinces) {
                try {
                    List<WardApiDto> provinceWards = getWardsByProvinceCode(province.getCode());

                    // ThÃªm thÃ´ng tin tá»‰nh vÃ o tÃªn ward Ä‘á»ƒ phÃ¢n biá»‡t
                    for (WardApiDto ward : provinceWards) {
                        ward.setName(ward.getName() + " (" + province.getName() + ")");
                    }

                    allWards.addAll(provinceWards);
                } catch (Exception e) {
                    System.err.println("Lá»—i khi láº¥y wards cho tá»‰nh " + province.getName() + ": " + e.getMessage());
                }
            }

            // Sáº¯p xáº¿p theo tÃªn
            return allWards.stream()
                    .sorted((w1, w2) -> w1.getName().compareToIgnoreCase(w2.getName()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Lá»—i khi láº¥y toÃ n bá»™ wards: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Clear cache - Ä‘á»ƒ test hoáº·c refresh data
     */
    public void clearCache() {
        cachedProvinces = null;
        lastProvincesFetch = 0;
        wardsCache.clear();
        wardsCacheTime.clear();
        System.out.println("Cache Ä‘Ã£ Ä‘Æ°á»£c xÃ³a");
    }
}
