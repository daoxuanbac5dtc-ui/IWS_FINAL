package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Service.UploadImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UploadImageServiceImpl implements UploadImageService {

    // Cáº¥u hÃ¬nh thÆ° má»¥c upload tá»« application.properties
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // KÃ­ch thÆ°á»›c file tá»‘i Ä‘a (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // CÃ¡c Ä‘á»‹nh dáº¡ng áº£nh Ä‘Æ°á»£c phÃ©p
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif",
            "image/bmp", "image/webp"
    );

    @Override
    public String saveImage(MultipartFile file, String folder) {
        try {
            // Kiá»ƒm tra file há»£p lá»‡
            if (!isValidImage(file)) {
                throw new RuntimeException("File khÃ´ng pháº£i lÃ  áº£nh há»£p lá»‡");
            }

            // Táº¡o thÆ° má»¥c náº¿u chÆ°a tá»“n táº¡i
            String folderPath = uploadDir + File.separator + folder;
            Path uploadPath = Paths.get(folderPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Táº¡o tÃªn file unique
            String fileName = generateUniqueFileName(file.getOriginalFilename());

            // ÄÆ°á»ng dáº«n Ä‘áº§y Ä‘á»§ cá»§a file
            Path filePath = uploadPath.resolve(fileName);

            // LÆ°u file
            Files.copy(file.getInputStream(), filePath);

            // Tráº£ vá» Ä‘Æ°á»ng dáº«n tÆ°Æ¡ng Ä‘á»‘i
            return folder + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Lá»—i khi lÆ°u áº£nh: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteImage(String imagePath) {
        try {
            if (imagePath == null || imagePath.trim().isEmpty()) {
                return false;
            }

            // Táº¡o Ä‘Æ°á»ng dáº«n Ä‘áº§y Ä‘á»§
            Path fullPath = Paths.get(uploadDir, imagePath);

            // Kiá»ƒm tra file tá»“n táº¡i vÃ  xÃ³a
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                return true;
            }

            return false;
        } catch (IOException e) {
            System.err.println("Lá»—i khi xÃ³a áº£nh: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Kiá»ƒm tra kÃ­ch thÆ°á»›c file
        if (file.getSize() > MAX_FILE_SIZE) {
            return false;
        }

        // Kiá»ƒm tra content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return false;
        }

        // Kiá»ƒm tra extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }

        String extension = getFileExtension(originalFilename);
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    @Override
    public String getFullImagePath(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }

        return Paths.get(uploadDir, relativePath).toString();
    }

    /**
     * Táº¡o tÃªn file unique Ä‘á»ƒ trÃ¡nh trÃ¹ng láº·p
     */
    private String generateUniqueFileName(String originalFilename) {
        // Láº¥y extension
        String extension = getFileExtension(originalFilename);

        // Táº¡o timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());

        // Táº¡o UUID ngáº¯n
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        // Káº¿t há»£p: timestamp_uuid.extension
        return String.format("%s_%s.%s", timestamp, uuid, extension);
    }

    /**
     * Láº¥y extension tá»« tÃªn file
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }

        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Táº¡o thÆ° má»¥c theo cáº¥u trÃºc ngÃ y (optional, cÃ³ thá»ƒ dÃ¹ng cho tá»• chá»©c file tá»‘t hÆ¡n)
     */
    public String saveImageWithDateStructure(MultipartFile file, String folder) {
        try {
            // Kiá»ƒm tra file há»£p lá»‡
            if (!isValidImage(file)) {
                throw new RuntimeException("File khÃ´ng pháº£i lÃ  áº£nh há»£p lá»‡");
            }

            // Táº¡o cáº¥u trÃºc thÆ° má»¥c theo ngÃ y: uploads/folder/2024/01/15/
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
            Date now = new Date();

            String datePath = String.format("%s%s%s%s%s%s%s%s%s",
                    folder, File.separator,
                    yearFormat.format(now), File.separator,
                    monthFormat.format(now), File.separator,
                    dayFormat.format(now), File.separator
            );

            String folderPath = uploadDir + File.separator + datePath;
            Path uploadPath = Paths.get(folderPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Táº¡o tÃªn file unique
            String fileName = generateUniqueFileName(file.getOriginalFilename());

            // ÄÆ°á»ng dáº«n Ä‘áº§y Ä‘á»§ cá»§a file
            Path filePath = uploadPath.resolve(fileName);

            // LÆ°u file
            Files.copy(file.getInputStream(), filePath);

            // Tráº£ vá» Ä‘Æ°á»ng dáº«n tÆ°Æ¡ng Ä‘á»‘i
            return datePath + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Lá»—i khi lÆ°u áº£nh: " + e.getMessage());
        }
    }

    /**
     * Resize áº£nh (optional - cáº§n thÃªm dependency image processing)
     * CÃ³ thá»ƒ implement sau náº¿u cáº§n
     */
    public String saveImageWithResize(MultipartFile file, String folder, int maxWidth, int maxHeight) {
        // TODO: Implement image resizing
        // Cáº§n thÃªm dependency nhÆ° Apache Commons Imaging hoáº·c ImageIO
        return saveImage(file, folder);
    }

    /**
     * Validate vÃ  láº¥y thÃ´ng tin chi tiáº¿t cá»§a áº£nh
     */
    public ImageInfo getImageInfo(MultipartFile file) {
        if (!isValidImage(file)) {
            return null;
        }

        ImageInfo info = new ImageInfo();
        info.setFileName(file.getOriginalFilename());
        info.setFileSize(file.getSize());
        info.setContentType(file.getContentType());
        info.setExtension(getFileExtension(file.getOriginalFilename()));

        return info;
    }

    // Class Ä‘á»ƒ chá»©a thÃ´ng tin áº£nh
    public static class ImageInfo {
        private String fileName;
        private long fileSize;
        private String contentType;
        private String extension;

        // Getters and setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public String getExtension() { return extension; }
        public void setExtension(String extension) { this.extension = extension; }

        public String getFileSizeFormatted() {
            if (fileSize < 1024) return fileSize + " B";
            if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
}
