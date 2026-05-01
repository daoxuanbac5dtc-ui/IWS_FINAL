package org.example.iws_websitesneaker.Service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadImageService {

    /**
     * Lưu ảnh vào thư mục chỉ định
     * @param file File ảnh cần lưu
     * @param folder Thư mục đích (ví dụ: "return-images", "product-images")
     * @return Đường dẫn tương đối của ảnh đã lưu
     */
    String saveImage(MultipartFile file, String folder);

    /**
     * Xóa ảnh khỏi hệ thống
     * @param imagePath Đường dẫn ảnh cần xóa
     * @return true nếu xóa thành công, false nếu thất bại
     */
    boolean deleteImage(String imagePath);

    /**
     * Kiểm tra file có phải là ảnh hợp lệ không
     * @param file File cần kiểm tra
     * @return true nếu là ảnh hợp lệ
     */
    boolean isValidImage(MultipartFile file);

    /**
     * Lấy đường dẫn đầy đủ của ảnh
     * @param relativePath Đường dẫn tương đối
     * @return Đường dẫn đầy đủ
     */
    String getFullImagePath(String relativePath);
}
