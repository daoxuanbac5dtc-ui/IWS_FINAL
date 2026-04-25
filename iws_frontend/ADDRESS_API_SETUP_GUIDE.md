# 🔧 Hướng dẫn thiết lập API Địa chỉ

## 🚨 **Vấn đề hiện tại:**
API endpoint `/api/dia-chi/tai-khoan/{taiKhoanId}` đang trả về lỗi 500, có thể do:
- Backend chưa có controller cho API địa chỉ
- Database chưa có bảng `dia_chi`
- Endpoint chưa được implement đúng

## ✅ **Giải pháp tạm thời đã áp dụng:**

### **1. Fallback Strategy:**
- Khi API địa chỉ không hoạt động, hệ thống sẽ sử dụng dữ liệu địa chỉ local
- Tạo ID tạm thời cho địa chỉ (`local_${timestamp}_${random}`)
- Hiển thị thông báo cảnh báo cho user

### **2. Graceful Degradation:**
- Ứng dụng vẫn hoạt động bình thường
- Chức năng địa chỉ hoạt động ở chế độ tạm thời
- Không crash ứng dụng khi API lỗi

## 🛠️ **Để khắc phục hoàn toàn, cần thiết lập backend:**

### **1. Tạo Entity DiaChi:**
```java
@Entity
@Table(name = "dia_chi")
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tai_khoan", nullable = false)
    private TaiKhoan taiKhoan;
    
    @Column(name = "ma_tinh")
    private String maTinh;
    
    @Column(name = "ma_phuong")
    private String maPhuong;
    
    @Column(name = "ten_tinh")
    private String tenTinh;
    
    @Column(name = "ten_phuong")
    private String tenPhuong;
    
    @Column(name = "dia_chi_chi_tiet")
    private String diaChiChiTiet;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(name = "trang_thai")
    private Integer trangThai = 1;
    
    @Column(name = "ngay_tao")
    private Date ngayTao;
    
    @Column(name = "ngay_cap_nhat")
    private Date ngayCapNhat;
    
    // Constructors, getters, setters...
}
```

### **2. Tạo Repository:**
```java
@Repository
public interface DiaChiRepository extends JpaRepository<DiaChi, Integer> {
    List<DiaChi> findByTaiKhoanId(Integer taiKhoanId);
    Optional<DiaChi> findByTaiKhoanIdAndIsDefaultTrue(Integer taiKhoanId);
    void deleteByTaiKhoanId(Integer taiKhoanId);
}
```

### **3. Tạo Service:**
```java
@Service
@Transactional
public class DiaChiService {
    @Autowired
    private DiaChiRepository diaChiRepository;
    
    public List<DiaChi> findByTaiKhoanId(Integer taiKhoanId) {
        return diaChiRepository.findByTaiKhoanId(taiKhoanId);
    }
    
    public Optional<DiaChi> findDefaultByTaiKhoanId(Integer taiKhoanId) {
        return diaChiRepository.findByTaiKhoanIdAndIsDefaultTrue(taiKhoanId);
    }
    
    public DiaChi save(DiaChi diaChi) {
        if (diaChi.getNgayTao() == null) {
            diaChi.setNgayTao(new Date());
        }
        diaChi.setNgayCapNhat(new Date());
        return diaChiRepository.save(diaChi);
    }
    
    public void deleteById(Integer id) {
        diaChiRepository.deleteById(id);
    }
    
    public void deleteByTaiKhoanId(Integer taiKhoanId) {
        diaChiRepository.deleteByTaiKhoanId(taiKhoanId);
    }
    
    @Transactional
    public DiaChi setDefaultAddress(Integer id, Integer taiKhoanId) {
        // Bỏ tất cả địa chỉ mặc định của tài khoản
        List<DiaChi> allAddresses = findByTaiKhoanId(taiKhoanId);
        for (DiaChi addr : allAddresses) {
            addr.setIsDefault(false);
            save(addr);
        }
        
        // Đặt địa chỉ này làm mặc định
        DiaChi address = diaChiRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
        address.setIsDefault(true);
        return save(address);
    }
}
```

### **4. Tạo Controller:**
```java
@RestController
@RequestMapping("/api/dia-chi")
@CrossOrigin(origins = "*")
public class DiaChiRestController {
    
    @Autowired
    private DiaChiService diaChiService;
    
    @Autowired
    private RepoTaiKhoan repoTaiKhoan;
    
    @GetMapping("/tai-khoan/{taiKhoanId}")
    public ResponseEntity<Map<String, Object>> getDiaChiByTaiKhoan(@PathVariable Integer taiKhoanId) {
        try {
            List<DiaChi> diaChiList = diaChiService.findByTaiKhoanId(taiKhoanId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", diaChiList);
            response.put("total", diaChiList.size());
            response.put("message", "Lấy địa chỉ theo tài khoản thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy địa chỉ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> addDiaChi(@Valid @RequestBody DiaChiDto diaChiDto) {
        try {
            // Kiểm tra tài khoản có tồn tại không
            if (!repoTaiKhoan.existsById(diaChiDto.getIdTaiKhoan())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Tài khoản không tồn tại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // Tạo entity từ DTO
            DiaChi diaChi = convertDtoToEntity(diaChiDto);
            
            // Lưu địa chỉ
            DiaChi savedDiaChi = diaChiService.save(diaChi);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", savedDiaChi);
            response.put("message", "Thêm địa chỉ thành công");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi thêm địa chỉ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDiaChi(
            @PathVariable Integer id,
            @Valid @RequestBody DiaChiDto diaChiDto) {
        try {
            // Kiểm tra địa chỉ có tồn tại không
            Optional<DiaChi> existingDiaChi = diaChiService.findById(id);
            if (existingDiaChi.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Không tìm thấy địa chỉ với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // Cập nhật thông tin
            DiaChi diaChi = existingDiaChi.get();
            updateEntityFromDto(diaChi, diaChiDto);
            
            DiaChi updatedDiaChi = diaChiService.save(diaChi);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedDiaChi);
            response.put("message", "Cập nhật địa chỉ thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi cập nhật địa chỉ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDiaChi(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer taiKhoanId) {
        try {
            Optional<DiaChi> diaChiOpt = diaChiService.findById(id);
            if (diaChiOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Không tìm thấy địa chỉ với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // Kiểm tra quyền sở hữu nếu có taiKhoanId
            if (taiKhoanId != null) {
                DiaChi diaChi = diaChiOpt.get();
                if (!diaChi.getTaiKhoan().getId().equals(taiKhoanId)) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Bạn không có quyền xóa địa chỉ này");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
                }
            }
            
            diaChiService.deleteById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa địa chỉ thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi xóa địa chỉ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PatchMapping("/{id}/set-default")
    @Transactional
    public ResponseEntity<Map<String, Object>> setDefaultAddress(
            @PathVariable Integer id,
            @RequestParam Integer taiKhoanId) {
        try {
            // Kiểm tra địa chỉ có thuộc về tài khoản không
            Optional<DiaChi> diaChiOpt = diaChiService.findById(id);
            if (diaChiOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Không tìm thấy địa chỉ");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            DiaChi diaChi = diaChiOpt.get();
            if (!diaChi.getTaiKhoan().getId().equals(taiKhoanId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Bạn không có quyền thao tác địa chỉ này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            
            DiaChi updatedDiaChi = diaChiService.setDefaultAddress(id, taiKhoanId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedDiaChi);
            response.put("message", "Đặt địa chỉ mặc định thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi đặt địa chỉ mặc định: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/tai-khoan/{taiKhoanId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteAllByTaiKhoan(@PathVariable Integer taiKhoanId) {
        try {
            List<DiaChi> addresses = diaChiService.findByTaiKhoanId(taiKhoanId);
            int deletedCount = addresses.size();
            
            diaChiService.deleteByTaiKhoanId(taiKhoanId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deletedCount", deletedCount);
            response.put("message", "Xóa tất cả địa chỉ của tài khoản thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi xóa địa chỉ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Helper methods...
    private DiaChi convertDtoToEntity(DiaChiDto dto) {
        DiaChi diaChi = new DiaChi();
        diaChi.setMaTinh(dto.getMaTinh());
        diaChi.setMaPhuong(dto.getMaPhuong());
        diaChi.setTenTinh(dto.getTenTinh());
        diaChi.setTenPhuong(dto.getTenPhuong());
        diaChi.setDiaChiChiTiet(dto.getDiaChiChiTiet());
        diaChi.setTrangThai(dto.getTrangThai() != null ? dto.getTrangThai() : 1);
        diaChi.setIsDefault(false);
        
        if (dto.getIdTaiKhoan() != null) {
            TaiKhoan taiKhoan = new TaiKhoan();
            taiKhoan.setId(dto.getIdTaiKhoan());
            diaChi.setTaiKhoan(taiKhoan);
        }
        
        return diaChi;
    }
    
    private void updateEntityFromDto(DiaChi entity, DiaChiDto dto) {
        if (dto.getMaTinh() != null) entity.setMaTinh(dto.getMaTinh());
        if (dto.getMaPhuong() != null) entity.setMaPhuong(dto.getMaPhuong());
        if (dto.getTenTinh() != null) entity.setTenTinh(dto.getTenTinh());
        if (dto.getTenPhuong() != null) entity.setTenPhuong(dto.getTenPhuong());
        if (dto.getDiaChiChiTiet() != null) entity.setDiaChiChiTiet(dto.getDiaChiChiTiet());
        if (dto.getTrangThai() != null) entity.setTrangThai(dto.getTrangThai());
        entity.setNgayCapNhat(new Date());
    }
}
```

### **5. Tạo DTO:**
```java
public class DiaChiDto {
    private Integer idTaiKhoan;
    private String maTinh;
    private String maPhuong;
    private String tenTinh;
    private String tenPhuong;
    private String diaChiChiTiet;
    private Integer trangThai;
    private Boolean isDefault;
    
    // Constructors, getters, setters...
}
```

### **6. Tạo Migration SQL:**
```sql
CREATE TABLE dia_chi (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_tai_khoan INT NOT NULL,
    ma_tinh VARCHAR(10),
    ma_phuong VARCHAR(10),
    ten_tinh VARCHAR(100),
    ten_phuong VARCHAR(100),
    dia_chi_chi_tiet TEXT,
    is_default BOOLEAN DEFAULT FALSE,
    trang_thai INT DEFAULT 1,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_tai_khoan) REFERENCES tai_khoan(id) ON DELETE CASCADE
);

CREATE INDEX idx_dia_chi_tai_khoan ON dia_chi(id_tai_khoan);
CREATE INDEX idx_dia_chi_default ON dia_chi(id_tai_khoan, is_default);
```

## 🧪 **Test API sau khi thiết lập:**

### **1. Test endpoints:**
```bash
# Lấy địa chỉ theo tài khoản
GET http://localhost:8080/api/dia-chi/tai-khoan/1

# Thêm địa chỉ mới
POST http://localhost:8080/api/dia-chi
{
    "idTaiKhoan": 1,
    "maTinh": "79",
    "maPhuong": "27154",
    "tenTinh": "TP. Hồ Chí Minh",
    "tenPhuong": "Phường 1",
    "diaChiChiTiet": "123 Đường ABC",
    "isDefault": true
}

# Cập nhật địa chỉ
PUT http://localhost:8080/api/dia-chi/1
{
    "maTinh": "79",
    "maPhuong": "27155",
    "tenTinh": "TP. Hồ Chí Minh",
    "tenPhuong": "Phường 2",
    "diaChiChiTiet": "456 Đường XYZ"
}

# Đặt địa chỉ mặc định
PATCH http://localhost:8080/api/dia-chi/1/set-default?taiKhoanId=1

# Xóa địa chỉ
DELETE http://localhost:8080/api/dia-chi/1?taiKhoanId=1

# Xóa tất cả địa chỉ của tài khoản
DELETE http://localhost:8080/api/dia-chi/tai-khoan/1
```

## 📋 **Checklist thiết lập:**

- [ ] Tạo Entity `DiaChi`
- [ ] Tạo Repository `DiaChiRepository`
- [ ] Tạo Service `DiaChiService`
- [ ] Tạo Controller `DiaChiRestController`
- [ ] Tạo DTO `DiaChiDto`
- [ ] Tạo bảng `dia_chi` trong database
- [ ] Test tất cả endpoints
- [ ] Kiểm tra frontend hoạt động với API thật

## 🎯 **Kết quả mong đợi:**

Sau khi thiết lập xong backend:
- ✅ API địa chỉ hoạt động bình thường
- ✅ Frontend tự động chuyển từ fallback sang API thật
- ✅ Dữ liệu địa chỉ được lưu trữ persistent trong database
- ✅ Có thể quản lý địa chỉ độc lập với nhân viên/khách hàng

## 🔄 **Quá trình chuyển đổi:**

1. **Hiện tại**: Frontend sử dụng fallback với dữ liệu local
2. **Sau khi thiết lập backend**: Frontend tự động detect API và chuyển sang sử dụng API thật
3. **Migration dữ liệu**: Có thể migrate dữ liệu địa chỉ từ local sang database

---

**Lưu ý**: Hiện tại ứng dụng vẫn hoạt động bình thường với fallback strategy. Việc thiết lập backend chỉ cần thiết để có tính năng địa chỉ hoàn chỉnh và persistent.

