package org.example.iws_websitesneaker.Service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadImageService {

    /**
     * LÆ°u áº£nh vÃ o thÆ° má»¥c chá»‰ Ä‘á»‹nh
     * @param file File áº£nh cáº§n lÆ°u
     * @param folder ThÆ° má»¥c Ä‘Ã­ch (vÃ­ dá»¥: "return-images", "product-images")
     * @return ÄÆ°á»ng dáº«n tÆ°Æ¡ng Ä‘á»‘i cá»§a áº£nh Ä‘Ã£ lÆ°u
     */
    String saveImage(MultipartFile file, String folder);

    /**
     * XÃ³a áº£nh khá»i há»‡ thá»‘ng
     * @param imagePath ÄÆ°á»ng dáº«n áº£nh cáº§n xÃ³a
     * @return true náº¿u xÃ³a thÃ nh cÃ´ng, false náº¿u tháº¥t báº¡i
     */
    boolean deleteImage(String imagePath);

    /**
     * Kiá»ƒm tra file cÃ³ pháº£i lÃ  áº£nh há»£p lá»‡ khÃ´ng
     * @param file File cáº§n kiá»ƒm tra
     * @return true náº¿u lÃ  áº£nh há»£p lá»‡
     */
    boolean isValidImage(MultipartFile file);

    /**
     * Láº¥y Ä‘Æ°á»ng dáº«n Ä‘áº§y Ä‘á»§ cá»§a áº£nh
     * @param relativePath ÄÆ°á»ng dáº«n tÆ°Æ¡ng Ä‘á»‘i
     * @return ÄÆ°á»ng dáº«n Ä‘áº§y Ä‘á»§
     */
    String getFullImagePath(String relativePath);
}
