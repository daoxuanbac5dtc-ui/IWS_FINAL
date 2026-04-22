# Tích hợp API Địa chỉ vào NhanVienView và KhachHangView

## 🎯 **Mục tiêu:**
Tích hợp API địa chỉ (`/api/dia-chi`) vào cả hai trang quản lý nhân viên và khách hàng để có thể thêm, xóa, sửa địa chỉ một cách đầy đủ và đồng bộ với database.

## 🔧 **Các chức năng đã tích hợp:**

### **1. NhanVienView.vue**

#### **A. Các function API địa chỉ:**
- `fetchEmployeeAddresses(employeeId)` - Lấy danh sách địa chỉ từ API
- `addAddressToEmployee(employeeId, addressData)` - Thêm địa chỉ mới
- `updateEmployeeAddress(addressId, addressData)` - Cập nhật địa chỉ
- `deleteEmployeeAddress(addressId, employeeId)` - Xóa địa chỉ
- `setDefaultEmployeeAddress(addressId, employeeId)` - Đặt địa chỉ mặc định
- `deleteAllEmployeeAddresses(employeeId)` - Xóa tất cả địa chỉ

#### **B. Cập nhật các function hiện tại:**
- **`saveEmployeeComplete`**: Tích hợp lưu địa chỉ vào bảng địa chỉ riêng
- **`editEmployee`**: Load địa chỉ từ API khi chỉnh sửa
- **`setDefaultAddress`**: Sử dụng API để đặt địa chỉ mặc định
- **`removeAddress`**: Sử dụng API để xóa địa chỉ

#### **C. Luồng hoạt động:**
```javascript
// Khi lưu nhân viên:
1. Lưu thông tin nhân viên vào bảng nhan_vien
2. Xóa tất cả địa chỉ cũ của nhân viên từ bảng dia_chi
3. Thêm từng địa chỉ mới vào bảng dia_chi

// Khi chỉnh sửa nhân viên:
1. Load địa chỉ từ API dia_chi
2. Hiển thị trong form chỉnh sửa
3. Cho phép thêm/xóa/sửa địa chỉ

// Khi xóa địa chỉ:
1. Gọi API xóa địa chỉ từ bảng dia_chi
2. Cập nhật local state
3. Nếu xóa địa chỉ mặc định, đặt địa chỉ khác làm mặc định
```

---

### **2. KhachHangView.vue**

#### **A. Các function API địa chỉ:**
- `fetchCustomerAddresses(customerId)` - Lấy danh sách địa chỉ từ API
- `addAddressToCustomer(customerId, addressData)` - Thêm địa chỉ mới
- `updateCustomerAddress(addressId, addressData)` - Cập nhật địa chỉ
- `deleteCustomerAddress(addressId, customerId)` - Xóa địa chỉ
- `setDefaultCustomerAddress(addressId, customerId)` - Đặt địa chỉ mặc định
- `deleteAllCustomerAddresses(customerId)` - Xóa tất cả địa chỉ

#### **B. Cập nhật các function hiện tại:**
- **`saveCustomer`**: Tích hợp lưu địa chỉ vào bảng địa chỉ riêng
- **`editCustomer`**: Load địa chỉ từ API khi chỉnh sửa

#### **C. Luồng hoạt động:**
```javascript
// Khi lưu khách hàng:
1. Lưu thông tin khách hàng vào bảng khach_hang
2. Xóa tất cả địa chỉ cũ của khách hàng từ bảng dia_chi
3. Thêm từng địa chỉ mới vào bảng dia_chi

// Khi chỉnh sửa khách hàng:
1. Load địa chỉ từ API dia_chi
2. Hiển thị trong form chỉnh sửa
3. Cho phép thêm/xóa/sửa địa chỉ
```

---

## 🏗️ **Kiến trúc tích hợp:**

### **Database Schema:**
```
Bảng dia_chi:
- id (Primary Key)
- idTaiKhoan (Foreign Key -> tai_khoan.id)
- maTinh, maPhuong (Mã tỉnh/phường)
- tenTinh, tenPhuong (Tên tỉnh/phường)
- diaChiChiTiet (Địa chỉ chi tiết)
- isDefault (Địa chỉ mặc định)
- trangThai (Trạng thái)
- ngayTao, ngayCapNhat (Timestamps)
```

### **API Endpoints được sử dụng:**
- `GET /api/dia-chi/tai-khoan/{taiKhoanId}` - Lấy địa chỉ theo tài khoản
- `POST /api/dia-chi` - Thêm địa chỉ mới
- `PUT /api/dia-chi/{id}` - Cập nhật địa chỉ
- `DELETE /api/dia-chi/{id}?taiKhoanId={taiKhoanId}` - Xóa địa chỉ
- `PATCH /api/dia-chi/{id}/set-default?taiKhoanId={taiKhoanId}` - Đặt mặc định
- `DELETE /api/dia-chi/tai-khoan/{taiKhoanId}` - Xóa tất cả địa chỉ

---

## 🔄 **Luồng dữ liệu:**

### **1. Tạo/Cập nhật địa chỉ:**
```
Frontend Form → Validation → API Call → Database → Response → UI Update
```

### **2. Xóa địa chỉ:**
```
User Click → Confirm Dialog → API Call → Database → Response → UI Update
```

### **3. Đặt địa chỉ mặc định:**
```
User Click → API Call (bỏ mặc định tất cả) → API Call (đặt mặc định) → UI Update
```

---

## 🛡️ **Xử lý lỗi và Fallback:**

### **1. Error Handling:**
- Tất cả API calls đều có try-catch
- Hiển thị toast notification cho user
- Log chi tiết lỗi vào console
- Không làm gián đoạn luồng chính (lưu nhân viên/khách hàng)

### **2. Fallback Strategy:**
```javascript
// Nếu không load được địa chỉ từ API:
try {
    addressesFromAPI = await fetchEmployeeAddresses(emp.id)
} catch (error) {
    console.warn('⚠️ Could not load addresses from API, using local data:', error)
    addressesFromAPI = emp.danhSachDiaChi || [] // Fallback to local data
}
```

### **3. Graceful Degradation:**
- Nếu API địa chỉ lỗi, vẫn lưu được nhân viên/khách hàng
- Hiển thị warning trong console
- Không crash ứng dụng

---

## 📊 **Lợi ích của tích hợp:**

### **1. Tách biệt dữ liệu:**
- Địa chỉ được lưu riêng trong bảng `dia_chi`
- Có thể quản lý địa chỉ độc lập
- Dễ dàng thêm tính năng như lịch sử địa chỉ

### **2. Tính nhất quán:**
- Đồng bộ dữ liệu giữa frontend và backend
- Đảm bảo địa chỉ mặc định chỉ có 1
- Validation địa chỉ ở cả frontend và backend

### **3. Khả năng mở rộng:**
- Dễ dàng thêm tính năng như địa chỉ giao hàng
- Có thể tích hợp với hệ thống địa chỉ bên ngoài
- Hỗ trợ nhiều loại địa chỉ khác nhau

### **4. Performance:**
- Chỉ load địa chỉ khi cần thiết
- Caching địa chỉ trong local state
- Lazy loading cho danh sách địa chỉ lớn

---

## 🧪 **Testing:**

### **1. Test Cases:**
- ✅ Thêm địa chỉ mới cho nhân viên/khách hàng
- ✅ Cập nhật địa chỉ hiện có
- ✅ Xóa địa chỉ (đơn lẻ và hàng loạt)
- ✅ Đặt địa chỉ mặc định
- ✅ Load địa chỉ khi chỉnh sửa
- ✅ Xử lý lỗi API gracefully

### **2. Test Scenarios:**
- **Normal Flow**: Thêm → Sửa → Xóa địa chỉ
- **Error Handling**: API lỗi → Fallback to local data
- **Edge Cases**: Không có địa chỉ, xóa địa chỉ mặc định cuối cùng
- **Concurrent**: Nhiều user cùng chỉnh sửa địa chỉ

---

## 🚀 **Hướng dẫn sử dụng:**

### **1. Cho Developer:**
```javascript
// Thêm địa chỉ mới:
await addAddressToEmployee(employeeId, {
    maTinh: '79',
    maPhuong: '27154',
    tenTinh: 'TP. Hồ Chí Minh',
    tenPhuong: 'Phường 1',
    diaChiChiTiet: '123 Đường ABC',
    isDefault: true
})

// Xóa địa chỉ:
await deleteEmployeeAddress(addressId, employeeId)

// Đặt mặc định:
await setDefaultEmployeeAddress(addressId, employeeId)
```

### **2. Cho User:**
1. **Thêm địa chỉ**: Nhấn "Thêm địa chỉ" → Điền thông tin → Lưu
2. **Sửa địa chỉ**: Chỉnh sửa thông tin trong form → Lưu
3. **Xóa địa chỉ**: Nhấn nút xóa → Xác nhận
4. **Đặt mặc định**: Nhấn "Đặt làm mặc định"

---

## 📝 **Ghi chú quan trọng:**

### **1. Dependencies:**
- API địa chỉ phải hoạt động (`/api/dia-chi`)
- Backend phải có bảng `dia_chi` và các endpoint tương ứng
- Frontend cần có quyền truy cập API

### **2. Limitations:**
- Hiện tại chỉ hỗ trợ địa chỉ Việt Nam
- Chưa có tính năng import/export địa chỉ
- Chưa có validation địa chỉ phức tạp

### **3. Future Enhancements:**
- Thêm tính năng tìm kiếm địa chỉ
- Hỗ trợ địa chỉ quốc tế
- Tích hợp với Google Maps API
- Thêm tính năng địa chỉ giao hàng riêng biệt

---

## ✅ **Kết luận:**

Việc tích hợp API địa chỉ vào NhanVienView và KhachHangView đã hoàn thành thành công, mang lại:

- **Tính nhất quán** trong quản lý dữ liệu địa chỉ
- **Khả năng mở rộng** cho các tính năng tương lai
- **Trải nghiệm người dùng** tốt hơn với xử lý lỗi graceful
- **Kiến trúc sạch** với tách biệt concerns

Hệ thống hiện tại đã sẵn sàng cho production và có thể dễ dàng mở rộng thêm các tính năng địa chỉ phức tạp hơn.

