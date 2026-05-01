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

    // Cấu hình thư mục upload từ application.properties
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // Kích thước file tối đa (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // Các định dạng ảnh được phép
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
            // Kiểm tra file hợp lệ
            if (!isValidImage(file)) {
                throw new RuntimeException("File không phải là ảnh hợp lệ");
            }

            // Tạo thư mục nếu chưa tồn tại
            String folderPath = uploadDir + File.separator + folder;
            Path uploadPath = Paths.get(folderPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file unique
            String fileName = generateUniqueFileName(file.getOriginalFilename());

            // Đường dẫn đầy đủ của file
            Path filePath = uploadPath.resolve(fileName);

            // Lưu file
            Files.copy(file.getInputStream(), filePath);

            // Trả về đường dẫn tương đối
            return folder + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteImage(String imagePath) {
        try {
            if (imagePath == null || imagePath.trim().isEmpty()) {
                return false;
            }

            // Tạo đường dẫn đầy đủ
            Path fullPath = Paths.get(uploadDir, imagePath);

            // Kiểm tra file tồn tại và xóa
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                return true;
            }

            return false;
        } catch (IOException e) {
            System.err.println("Lỗi khi xóa ảnh: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Kiểm tra kích thước file
        if (file.getSize() > MAX_FILE_SIZE) {
            return false;
        }

        // Kiểm tra content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return false;
        }

        // Kiểm tra extension
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
     * Tạo tên file unique để tránh trùng lặp
     */
    private String generateUniqueFileName(String originalFilename) {
        // Lấy extension
        String extension = getFileExtension(originalFilename);

        // Tạo timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());

        // Tạo UUID ngắn
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        // Kết hợp: timestamp_uuid.extension
        return String.format("%s_%s.%s", timestamp, uuid, extension);
    }

    /**
     * Lấy extension từ tên file
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }

        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Tạo thư mục theo cấu trúc ngày (optional, có thể dùng cho tổ chức file tốt hơn)
     */
    public String saveImageWithDateStructure(MultipartFile file, String folder) {
        try {
            // Kiểm tra file hợp lệ
            if (!isValidImage(file)) {
                throw new RuntimeException("File không phải là ảnh hợp lệ");
            }

            // Tạo cấu trúc thư mục theo ngày: uploads/folder/2024/01/15/
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

            // Tạo tên file unique
            String fileName = generateUniqueFileName(file.getOriginalFilename());

            // Đường dẫn đầy đủ của file
            Path filePath = uploadPath.resolve(fileName);

            // Lưu file
            Files.copy(file.getInputStream(), filePath);

            // Trả về đường dẫn tương đối
            return datePath + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage());
        }
    }

    /**
     * Resize ảnh (optional - cần thêm dependency image processing)
     * Có thể implement sau nếu cần
     */
    public String saveImageWithResize(MultipartFile file, String folder, int maxWidth, int maxHeight) {
        // TODO: Implement image resizing
        // Cần thêm dependency như Apache Commons Imaging hoặc ImageIO
        return saveImage(file, folder);
    }

    /**
     * Validate và lấy thông tin chi tiết của ảnh
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

    // Class để chứa thông tin ảnh
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
