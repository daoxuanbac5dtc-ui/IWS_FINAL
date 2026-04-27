SET FOREIGN_KEY_CHECKS = 0;
CREATE DATABASE IF NOT EXISTS PRO2113 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
;
USE PRO2113
;
-- Tạo các bảng (giữ nguyên mã của bạn)
CREATE TABLE mau_sac (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ma_mau_sac VARCHAR(25) NOT NULL,
    ten_mau_sac VARCHAR(225) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE thuong_hieu (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ma_thuong_hieu VARCHAR(25) NOT NULL,
    ten_thuong_hieu VARCHAR(225) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE kich_co (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ma_kich_co VARCHAR(25) NOT NULL,
    ten_kich_co VARCHAR(225) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE de_giay (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ma_de_giay VARCHAR(25) NOT NULL,
    ten_de_giay VARCHAR(225) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE danh_muc (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ma_danh_muc VARCHAR(25) NOT NULL,
    ten_danh_muc VARCHAR(225) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE chat_lieu (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ma_chat_lieu VARCHAR(25) NOT NULL,
    ten_chat_lieu VARCHAR(225) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE san_pham (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ma_san_pham VARCHAR(25) NOT NULL,
    ten_san_pham VARCHAR(225) NOT NULL,
    so_luong INT NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME,
    id_chat_lieu INT,
    id_de_giay INT,
    id_danh_muc INT,
    id_thuong_hieu INT
)
;
CREATE TABLE hinh_anh (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ma_hinh_anh VARCHAR(25) NOT NULL,
    ten_hinh_anh VARCHAR(225) NOT NULL,
    duong_dan VARCHAR(225) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE chi_tiet_san_pham (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ma_chi_tiet VARCHAR(25) NOT NULL,
    ma_QR VARCHAR(250),
    so_luong INT NOT NULL,
    gia_ban DECIMAL(10,2),
    gia_goc DECIMAL(10,2) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME,
    id_mau_sac INT,
    id_kich_co INT,
    id_san_pham INT,
    id_hinh_anh INT 
)
;
CREATE TABLE khuyen_mai (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ma_khuyen_mai VARCHAR(25) NOT NULL,
    ten_khuyen_mai VARCHAR(225) NOT NULL,
    ngay_bat_dau DATETIME NOT NULL,
    ngay_ket_thuc DATETIME NOT NULL,
    trang_thai INT NOT NULL,
    gia_tri FLOAT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE khuyen_mai_chi_tiet (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME,
    id_chi_tiet INT,
    id_khuyen_mai INT 
)
;
CREATE TABLE tai_khoan (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ma_tai_khoan VARCHAR(25) NOT NULL,
    email VARCHAR(50) NOT NULL,
    mat_khau VARCHAR(225) NOT NULL,
    vai_tro INT NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE dia_chi (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    id_tai_khoan INT NOT NULL,
    ma_tinh VARCHAR(25) NOT NULL,
   
    ma_phuong VARCHAR(25) NOT NULL,
    ten_tinh VARCHAR(225) NOT NULL,
   
    ten_phuong VARCHAR(225) NOT NULL,
    dia_chi_chi_tiet VARCHAR(250) NOT NULL,
    is_default TINYINT(1) DEFAULT 0,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE vi_diem (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    tong_diem FLOAT NOT NULL,
    so_diem_da_dung FLOAT NOT NULL,
    so_diem_da_cong FLOAT NOT NULL,
    gia_tri_diem FLOAT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE khach_hang (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    id_vi_diem INT,
    id_tai_khoan INT,
    ma_khach_hang VARCHAR(25) NOT NULL,
    -- BỎ: email VARCHAR(50) NOT NULL,  <-- Email đã có trong tai_khoan
    ho_ten VARCHAR(225) NOT NULL,
    sdt VARCHAR(10) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE nhan_vien (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    id_tai_khoan INT,
 --   id_dia_chi INT, 
    ma_nhan_vien VARCHAR(25) NOT NULL,
   -- email VARCHAR(50) NOT NULL,
    ho_ten VARCHAR(225) NOT NULL,
    sdt VARCHAR(10) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE gio_hang (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    id_tai_khoan INT,
    ma_gio_hang VARCHAR(25) NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE gio_hang_chi_tiet (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    id_gio_hang INT,
    id_ctsp INT,
    ma_gio_hang_chi_tiet VARCHAR(25) NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME,
    gia FLOAT NOT NULL,
    so_luong INT NOT NULL,
    trang_thai_hoa_don VARCHAR(250)
)
;
CREATE TABLE voucher (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ma_voucher VARCHAR(25),
    ten_voucher VARCHAR(225),
    loai_giam_gia VARCHAR(25),
    trang_thai INT NOT NULL,
    duong_dan_hinh_anh VARCHAR(250) NOT NULL,
    gia_tri_giam_toi_da FLOAT NOT NULL,
	gia_tri_giam FLOAT NOT NULL,
    gia_tri_giam_toi_thieu FLOAT NOT NULL,
    so_luong INT NOT NULL,
    ngay_bat_dau DATETIME NOT NULL,
    ngay_ket_thuc DATETIME NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE hoa_don (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    id_khach_hang INT,
    id_nhan_vien INT,
    ma_hoa_don VARCHAR(25) NOT NULL,
    dia_chi VARCHAR(250),
    email VARCHAR(25),
    ghi_chu VARCHAR(250),
    sdt VARCHAR(10),
    trang_thai_hoa_don VARCHAR(250),
    loai_hoa_don VARCHAR(25),
    ten_nguoi_dung VARCHAR(250),
	phuong_thuc_thanh_toan VARCHAR(50),
    phi_van_chuyen DECIMAL(10,2),
    diem_su_dung INT,
    tong_tien DECIMAL(10,2),
    gia_tri_diem FLOAT,
    tong_thanh_toan DECIMAL(10,2),
    ngay_hoan_thanh DATETIME,
    ngay_xac_nhan DATETIME,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME,
    ngay_giao_hang DATETIME,
    ngay_nhan_hang DATETIME,
    thoi_gian_van_chuyen DATETIME
)
;
CREATE TABLE hoa_don_chi_tiet (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    id_hoa_don INT,
    id_ctsp INT,
    gia FLOAT NOT NULL,
    so_luong INT NOT NULL,
    trang_thai_hoa_don VARCHAR(250) NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE chi_tiet_voucher (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ma_chi_tiet_voucher VARCHAR(255) NOT NULL,
    id_hoa_don INT,
    id_voucher INT, -- Vẫn giữ để tham chiếu (có thể NULL nếu voucher bị xóa)
    
    -- Thông tin voucher tại thời điểm áp dụng
    ma_voucher VARCHAR(255),
    ten_voucher VARCHAR(225),
    loai_giam_gia VARCHAR(25),
    gia_tri_giam FLOAT,
    gia_tri_giam_toi_da FLOAT,
    gia_tri_giam_toi_thieu FLOAT,
    
    -- Thông tin tính toán
    gia_tri_don_hang DECIMAL(18,2), -- Giá trị đơn hàng trước khi giảm
    so_tien_giam DECIMAL(18,2), -- Số tiền thực tế được giảm
    thanh_tien DECIMAL(18,2) NOT NULL, -- Thành tiền sau khi giảm
    
    -- Thông tin thời gian
    ngay_ap_dung DATETIME NOT NULL, -- Thời điểm áp dụng voucher
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
);
;
CREATE TABLE tai_khoan_voucher (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    id_tai_khoan INT,
    id_voucher INT,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE cong_thuc_tinh_diem (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    tien_tich_diem FLOAT NOT NULL,
    tien_tieu_diem FLOAT NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE lich_su_diem (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    id_hoa_don INT,
    id_cong_thuc_tinh_diem INT,
    id_vi_diem INT,
    loai_diem FLOAT,
    gia_tri_diem FLOAT,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
;
CREATE TABLE lich_su_hoa_don (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    id_nhan_vien INT,
    id_hoa_don INT,
    mo_ta_hanh_dong VARCHAR(250) NOT NULL,
    trang_thai_hoa_don VARCHAR(250) NOT NULL,
    ngay_tao DATETIME NOT NULL,
    ngay_cap_nhat DATETIME NOT NULL
)
;
CREATE TABLE chi_tiet_tra_hang (
    id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ma_chi_tiet_tra_hang VARCHAR(25) NOT NULL,
    so_luong INT NOT NULL,
    trang_thai_hoa_don VARCHAR(250) NOT NULL,
    ngay_tao DATETIME NOT NULL,
    ngay_tao_tra_hang DATETIME NOT NULL,
    ngay_cap_nhat DATETIME NOT NULL,
    id_ctsp INT,
	id_hoa_don int,
    ly_do VARCHAR(255),
    duong_dan_anh VARCHAR(255) 
)
;
-- Thêm các ràng buộc khóa ngoại
ALTER TABLE san_pham
ADD CONSTRAINT fk_sp_th FOREIGN KEY (id_thuong_hieu) REFERENCES thuong_hieu(id), ADD CONSTRAINT fk_sp_cl FOREIGN KEY (id_chat_lieu) REFERENCES chat_lieu(id), ADD CONSTRAINT fk_sp_dg FOREIGN KEY (id_de_giay) REFERENCES de_giay(id), ADD CONSTRAINT fk_sp_dm FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id)
;
ALTER TABLE chi_tiet_san_pham
ADD CONSTRAINT fk_ctsp_ms FOREIGN KEY (id_mau_sac) REFERENCES mau_sac(id), ADD CONSTRAINT fk_ctsp_kc FOREIGN KEY (id_kich_co) REFERENCES kich_co(id), ADD CONSTRAINT fk_ctsp_sp FOREIGN KEY (id_san_pham) REFERENCES san_pham(id), ADD CONSTRAINT fk_ctsp_ha FOREIGN KEY (id_hinh_anh) REFERENCES hinh_anh(id)
;
ALTER TABLE khuyen_mai_chi_tiet
ADD CONSTRAINT fk_kmct_ctsp FOREIGN KEY (id_chi_tiet) REFERENCES chi_tiet_san_pham(id), ADD CONSTRAINT fk_kmct_km FOREIGN KEY (id_khuyen_mai) REFERENCES khuyen_mai(id)
;
ALTER TABLE dia_chi
ADD CONSTRAINT fk_dc_tk FOREIGN KEY (id_tai_khoan) REFERENCES tai_khoan(id)
;
ALTER TABLE khach_hang
ADD CONSTRAINT fk_kh_tk FOREIGN KEY (id_tai_khoan) REFERENCES tai_khoan(id), ADD CONSTRAINT fk_kh_vd FOREIGN KEY (id_vi_diem) REFERENCES vi_diem(id)
;
ALTER TABLE nhan_vien
ADD CONSTRAINT fk_nv_tk FOREIGN KEY (id_tai_khoan) REFERENCES tai_khoan(id)
;
ALTER TABLE gio_hang
ADD CONSTRAINT fk_gh_tk FOREIGN KEY (id_tai_khoan) REFERENCES tai_khoan(id)
;
ALTER TABLE gio_hang_chi_tiet
ADD CONSTRAINT fk_ghct_gh FOREIGN KEY (id_gio_hang) REFERENCES gio_hang(id), ADD CONSTRAINT fk_ghct_ctsp FOREIGN KEY (id_ctsp) REFERENCES chi_tiet_san_pham(id)
;
ALTER TABLE hoa_don
ADD CONSTRAINT fk_hd_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id), ADD CONSTRAINT fk_hd_nv FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id)
;
ALTER TABLE hoa_don_chi_tiet
ADD CONSTRAINT fk_hdct_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id), ADD CONSTRAINT fk_hdct_ctsp FOREIGN KEY (id_ctsp) REFERENCES chi_tiet_san_pham(id)
;
ALTER TABLE chi_tiet_voucher
ADD CONSTRAINT fk_ctv_voucher FOREIGN KEY (id_voucher) REFERENCES voucher(id), ADD CONSTRAINT fk_ctv_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id)
;
ALTER TABLE tai_khoan_voucher
ADD CONSTRAINT fk_tkv_v FOREIGN KEY (id_voucher) REFERENCES voucher(id), ADD CONSTRAINT fk_tkv_tk FOREIGN KEY (id_tai_khoan) REFERENCES tai_khoan(id)
;
ALTER TABLE lich_su_diem
ADD CONSTRAINT fk_lsd_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id), ADD CONSTRAINT fk_lsd_vd FOREIGN KEY (id_vi_diem) REFERENCES vi_diem(id), ADD CONSTRAINT fk_lsd_cttd FOREIGN KEY (id_cong_thuc_tinh_diem) REFERENCES cong_thuc_tinh_diem(id)
;
ALTER TABLE lich_su_hoa_don
ADD CONSTRAINT fk_lshd_nv FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id), ADD CONSTRAINT fk_lshd_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id)
;
ALTER TABLE chi_tiet_tra_hang
ADD CONSTRAINT fk_ctth_ctsp FOREIGN KEY (id_ctsp) REFERENCES chi_tiet_san_pham(id), ADD CONSTRAINT fk_ctth_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id)
;
-- Script insert dữ liệu chuẩn cho database PRO2113
USE PRO2113
;
-- 1. Insert dữ liệu bảng mau_sac
INSERT INTO mau_sac (ma_mau_sac, ten_mau_sac, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('MS001', 'Đen', 1, NOW(), NOW()),
('MS002', 'Trắng', 1, NOW(), NOW()),
('MS003', 'Đỏ', 1, NOW(), NOW()),
('MS004', 'Xanh dương', 1, NOW(), NOW()),
('MS005', 'Xanh lá', 1, NOW(), NOW()),
('MS006', 'Vàng', 1, NOW(), NOW()),
('MS007', 'Hồng', 1, NOW(), NOW()),
('MS008', 'Nâu', 1, NOW(), NOW()),
('MS009', 'Xám', 1, NOW(), NOW()),
('MS010', 'Cam', 1, NOW(), NOW())
;
-- 2. Insert dữ liệu bảng thuong_hieu
INSERT INTO thuong_hieu (ma_thuong_hieu, ten_thuong_hieu, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('TH001', 'Nike', 1, NOW(), NOW()),
('TH002', 'Adidas', 1, NOW(), NOW()),
('TH003', 'Converse', 1, NOW(), NOW()),
('TH004', 'Vans', 1, NOW(), NOW()),
('TH005', 'Puma', 1, NOW(), NOW()),
('TH006', 'New Balance', 1, NOW(), NOW()),
('TH007', 'Reebok', 1, NOW(), NOW()),
('TH008', 'Jordan', 1, NOW(), NOW()),
('TH009', 'Under Armour', 1, NOW(), NOW()),
('TH010', 'Asics', 1, NOW(), NOW())
;
-- 3. Insert dữ liệu bảng kich_co
INSERT INTO kich_co (ma_kich_co, ten_kich_co, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('KC001', '35', 1, NOW(), NOW()),
('KC002', '36', 1, NOW(), NOW()),
('KC003', '37', 1, NOW(), NOW()),
('KC004', '38', 1, NOW(), NOW()),
('KC005', '39', 1, NOW(), NOW()),
('KC006', '40', 1, NOW(), NOW()),
('KC007', '41', 1, NOW(), NOW()),
('KC008', '42', 1, NOW(), NOW()),
('KC009', '43', 1, NOW(), NOW()),
('KC010', '44', 1, NOW(), NOW())
;
-- 4. Insert dữ liệu bảng de_giay
INSERT INTO de_giay (ma_de_giay, ten_de_giay, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('DG001', 'Đế cao su', 1, NOW(), NOW()),
('DG002', 'Đế EVA', 1, NOW(), NOW()),
('DG003', 'Đế PU', 1, NOW(), NOW()),
('DG004', 'Đế Air Max', 1, NOW(), NOW()),
('DG005', 'Đế Boost', 1, NOW(), NOW()),
('DG006', 'Đế nhựa', 1, NOW(), NOW()),
('DG007', 'Đế da', 1, NOW(), NOW()),
('DG008', 'Đế gel', 1, NOW(), NOW())
;
-- 5. Insert dữ liệu bảng danh_muc
INSERT INTO danh_muc (ma_danh_muc, ten_danh_muc, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('DM001', 'Giày thể thao', 1, NOW(), NOW()),
('DM002', 'Giày chạy bộ', 1, NOW(), NOW()),
('DM003', 'Giày bóng đá', 1, NOW(), NOW()),
('DM004', 'Giày cao gót', 1, NOW(), NOW()),
('DM005', 'Giày sandal', 1, NOW(), NOW()),
('DM006', 'Giày boot', 1, NOW(), NOW()),
('DM007', 'Giày tây', 1, NOW(), NOW()),
('DM008', 'Giày sneaker', 1, NOW(), NOW())
;
-- 6. Insert dữ liệu bảng chat_lieu
INSERT INTO chat_lieu (ma_chat_lieu, ten_chat_lieu, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('CL001', 'Da thật', 1, NOW(), NOW()),
('CL002', 'Da tổng hợp', 1, NOW(), NOW()),
('CL003', 'Canvas', 1, NOW(), NOW()),
('CL004', 'Vải dệt kim', 1, NOW(), NOW()),
('CL005', 'Mesh', 1, NOW(), NOW()),
('CL006', 'Suede', 1, NOW(), NOW()),
('CL007', 'Nylon', 1, NOW(), NOW()),
('CL008', 'Flyknit', 1, NOW(), NOW())
;
-- 7. Insert dữ liệu bảng hinh_anh
INSERT INTO hinh_anh (ma_hinh_anh, ten_hinh_anh, duong_dan, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('HA001', 'Nike Air Max 270 - Đen', '/images/nike-air-max-270-black.jpg', 1, NOW(), NOW()),
('HA002', 'Adidas Ultraboost 22 - Trắng', '/images/adidas-ultraboost-22-white.jpg', 1, NOW(), NOW()),
('HA003', 'Converse Chuck Taylor - Đỏ', '/images/converse-chuck-taylor-red.jpg', 1, NOW(), NOW()),
('HA004', 'Vans Old Skool - Đen Trắng', '/images/vans-old-skool-black-white.jpg', 1, NOW(), NOW()),
('HA005', 'Puma Suede Classic - Xanh', '/images/puma-suede-classic-blue.jpg', 1, NOW(), NOW()),
('HA006', 'New Balance 574 - Xám', '/images/new-balance-574-gray.jpg', 1, NOW(), NOW()),
('HA007', 'Jordan 1 High - Đỏ Đen', '/images/jordan-1-high-red-black.jpg', 1, NOW(), NOW()),
('HA008', 'Under Armour HOVR - Trắng', '/images/under-armour-hovr-white.jpg', 1, NOW(), NOW())
;
-- 8. Insert dữ liệu bảng san_pham
INSERT INTO san_pham (ma_san_pham, ten_san_pham, so_luong, trang_thai, ngay_tao, ngay_cap_nhat, id_chat_lieu, id_de_giay, id_danh_muc, id_thuong_hieu) VALUES
('SP001', 'Nike Air Max 270', 100, 1, NOW(), NOW(), 2, 4, 1, 1),
('SP002', 'Adidas Ultraboost 22', 80, 1, NOW(), NOW(), 4, 5, 2, 2),
('SP003', 'Converse Chuck Taylor All Star', 120, 1, NOW(), NOW(), 3, 1, 8, 3),
('SP004', 'Vans Old Skool', 90, 1, NOW(), NOW(), 6, 1, 8, 4),
('SP005', 'Puma Suede Classic', 70, 1, NOW(), NOW(), 6, 1, 1, 5),
('SP006', 'New Balance 574', 60, 1, NOW(), NOW(), 5, 2, 1, 6),
('SP007', 'Jordan 1 High OG', 50, 1, NOW(), NOW(), 1, 1, 8, 8),
('SP008', 'Under Armour HOVR Phantom', 40, 1, NOW(), NOW(), 4, 2, 2, 9)
;
-- 9. Insert dữ liệu bảng chi_tiet_san_pham
INSERT INTO chi_tiet_san_pham (ma_chi_tiet, ma_QR, so_luong, gia_ban, gia_goc, trang_thai, ngay_tao, ngay_cap_nhat, id_mau_sac, id_kich_co, id_san_pham, id_hinh_anh) VALUES
('CTSP001', 'QR001', 20, 2500000.00, 2000000.00, 1, NOW(), NOW(), 1, 6, 1, 1),
('CTSP002', 'QR002', 15, 2500000.00, 2000000.00, 1, NOW(), NOW(), 1, 7, 1, 1),
('CTSP003', 'QR003', 18, 3200000.00, 2800000.00, 1, NOW(), NOW(), 2, 6, 2, 2),
('CTSP004', 'QR004', 12, 3200000.00, 2800000.00, 1, NOW(), NOW(), 2, 7, 2, 2),
('CTSP005', 'QR005', 25, 1800000.00, 1500000.00, 1, NOW(), NOW(), 3, 5, 3, 3),
('CTSP006', 'QR006', 20, 1800000.00, 1500000.00, 1, NOW(), NOW(), 3, 6, 3, 3),
('CTSP007', 'QR007', 22, 2000000.00, 1700000.00, 1, NOW(), NOW(), 1, 6, 4, 4),
('CTSP008', 'QR008', 18, 2000000.00, 1700000.00, 1, NOW(), NOW(), 2, 7, 4, 4),
('CTSP009', 'QR009', 16, 2200000.00, 1900000.00, 1, NOW(), NOW(), 4, 6, 5, 5),
('CTSP010', 'QR010', 14, 2200000.00, 1900000.00, 1, NOW(), NOW(), 4, 7, 5, 5)
;
-- 10. Insert dữ liệu bảng khuyen_mai
INSERT INTO khuyen_mai (ma_khuyen_mai, ten_khuyen_mai, ngay_bat_dau, ngay_ket_thuc, trang_thai, gia_tri, ngay_tao, ngay_cap_nhat) VALUES
('KM001', 'Khuyến mãi mùa hè', '2024-06-01', '2024-08-31', 1, 0.15, NOW(), NOW()),
('KM002', 'Black Friday Sale', '2024-11-25', '2024-11-30', 1, 0.30, NOW(), NOW()),
('KM003', 'Tết Nguyên Đán', '2025-01-20', '2025-02-15', 1, 0.20, NOW(), NOW()),
('KM004', 'Back to School', '2024-08-15', '2024-09-15', 1, 0.10, NOW(), NOW())
;
-- 11. Insert dữ liệu bảng khuyen_mai_chi_tiet
INSERT INTO khuyen_mai_chi_tiet (trang_thai, ngay_tao, ngay_cap_nhat, id_chi_tiet, id_khuyen_mai) VALUES
(1, NOW(), NOW(), 1, 1),
(1, NOW(), NOW(), 2, 1),
(1, NOW(), NOW(), 3, 2),
(1, NOW(), NOW(), 4, 2),
(1, NOW(), NOW(), 5, 3),
(1, NOW(), NOW(), 6, 3)
;
-- 12. Insert dữ liệu bảng tai_khoan
INSERT INTO tai_khoan (ma_tai_khoan, email, mat_khau, vai_tro, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('TK001', 'admin@shoestore.com', '$2a$10$xXnh/NbED5JLjHJs0CQXietjKJ5B/73T045M6ph3ZzDGKgZBcBqeW', 2, 1, NOW(), NOW()),
('TK002', 'nhanvien1@shoestore.com', '$2a$10$FTlIlrKAoPdA0lr3y8ZxX.175LZ7DlRjSlBv5bt1dtJEoBetaMHYa', 1, 1, NOW(), NOW()),
('TK003', 'nhanvien2@shoestore.com', '$2a$10$FTlIlrKAoPdA0lr3y8ZxX.175LZ7DlRjSlBv5bt1dtJEoBetaMHYa', 1, 1, NOW(), NOW()),
('TK004', 'khachhang1@gmail.com', '$2a$10$vS7uF8ciseto6K/3Ooz0wey/HjPPlyrGUL4eAC6YkZu1Bf2tAjDSi', 0, 1, NOW(), NOW()),
('TK005', 'khachhang2@gmail.com', '$2a$10$vS7uF8ciseto6K/3Ooz0wey/HjPPlyrGUL4eAC6YkZu1Bf2tAjDSi', 0, 1, NOW(), NOW()),
('TK006', 'khachhang3@gmail.com', '$2a$10$vS7uF8ciseto6K/3Ooz0wey/HjPPlyrGUL4eAC6YkZu1Bf2tAjDSi', 0, 1, NOW(), NOW()),
('TK007', 'khachhang4@gmail.com', '$2a$10$vS7uF8ciseto6K/3Ooz0wey/HjPPlyrGUL4eAC6YkZu1Bf2tAjDSi', 0, 1, NOW(), NOW()),
('TK008', 'khachhang5@gmail.com', '$2a$10$vS7uF8ciseto6K/3Ooz0wey/HjPPlyrGUL4eAC6YkZu1Bf2tAjDSi', 0, 1, NOW(), NOW())
;
-- 13. Insert dữ liệu bảng dia_chi
INSERT INTO dia_chi (id_tai_khoan,
    ma_tinh,
    ma_phuong,
    ten_tinh,
    ten_phuong,
    dia_chi_chi_tiet,
    is_default,
    trang_thai,
    ngay_tao,
    ngay_cap_nhat
)
VALUES
(1, '01', '00001', 'Hà Nội', 'Phúc Xá', '123 Phố Huế', 1, 1, NOW(), NOW()),
(2, '79', '00001', 'TP Hồ Chí Minh', 'Bến Nghé', '456 Nguyễn Huệ', 1, 1, NOW(), NOW()),
(3, '48', '00001', 'Đà Nẵng', 'Hải Châu I', '789 Trần Phú', 1, 1, NOW(), NOW()),
(4, '01', '00010', 'Hà Nội', 'Hàng Bạc', '321 Hàng Bạc', 1, 1, NOW(), NOW()),
(5, '79', '00005', 'TP Hồ Chí Minh', 'Võ Thị Sáu', '654 Võ Thị Sáu', 1, 1, NOW(), NOW()),
(6, '01', '00015', 'Hà Nội', 'Láng Thượng', '987 Láng Hạ', 1, 1, NOW(), NOW()),
(7, '79', '00008', 'TP Hồ Chí Minh', 'Phường 5', '147 Trần Hưng Đạo', 1, 1, NOW(), NOW()),
(8, '48', '00003', 'Đà Nẵng', 'Thanh Khê Đông', '258 Lê Duẩn', 1, 1, NOW(), NOW());
;
-- 14. Insert dữ liệu bảng vi_diem
INSERT INTO vi_diem (tong_diem, so_diem_da_dung, so_diem_da_cong, gia_tri_diem, ngay_tao, ngay_cap_nhat) VALUES
(1000, 200, 1200, 1000, NOW(), NOW()),
(500, 100, 600, 1000, NOW(), NOW()),
(2000, 500, 2500, 1000, NOW(), NOW()),
(300, 50, 350, 1000, NOW(), NOW()),
(750, 150, 900, 1000, NOW(), NOW())
;
-- 15. Insert dữ liệu bảng khach_hang
INSERT INTO khach_hang (id_vi_diem, id_tai_khoan, ma_khach_hang, ho_ten, sdt, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
(1, 4, 'KH001', 'Phạm Thị Khách Hàng', '0901234567', 1, NOW(), NOW()),
(2, 5, 'KH002', 'Hoàng Văn Khách', '0901234568', 1, NOW(), NOW()),
(3, 6, 'KH003', 'Vũ Thị Minh', '0901234569', 1, NOW(), NOW()),
(4, 7, 'KH004', 'Đỗ Văn Thành', '0901234570', 1, NOW(), NOW()),
(5, 8, 'KH005', 'Bùi Thị Lan', '0901234571', 1, NOW(), NOW())
;
-- 16. Insert dữ liệu bảng nhan_vien
INSERT INTO nhan_vien (id_tai_khoan, ma_nhan_vien, ho_ten, sdt, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
(1, 'NV001', 'Nguyễn Văn Admin', '0987654321', 1, NOW(), NOW()),
(2, 'NV002', 'Trần Thị Nhân Viên', '0987654322', 1, NOW(), NOW()),
(3, 'NV003', 'Lê Văn Nhân Viên 2', '0987654323', 1, NOW(), NOW())
;
-- 17. Insert dữ liệu bảng gio_hang
INSERT INTO gio_hang (id_tai_khoan, ma_gio_hang, ngay_tao, ngay_cap_nhat) VALUES
(4, 'GH001', NOW(), NOW()),
(5, 'GH002', NOW(), NOW()),
(6, 'GH003', NOW(), NOW()),
(7, 'GH004', NOW(), NOW()),
(8, 'GH005', NOW(), NOW())
;
-- 18. Insert dữ liệu bảng gio_hang_chi_tiet
INSERT INTO gio_hang_chi_tiet (id_gio_hang, id_ctsp, ma_gio_hang_chi_tiet, ngay_tao, ngay_cap_nhat, gia, so_luong, trang_thai_hoa_don) VALUES
(1, 1, 'GHCT001', NOW(), NOW(), 2500000, 1, 'Trong giỏ hàng'),
(1, 3, 'GHCT002', NOW(), NOW(), 3200000, 1, 'Trong giỏ hàng'),
(2, 5, 'GHCT003', NOW(), NOW(), 1800000, 2, 'Trong giỏ hàng'),
(3, 7, 'GHCT004', NOW(), NOW(), 2000000, 1, 'Trong giỏ hàng'),
(4, 9, 'GHCT005', NOW(), NOW(), 2200000, 1, 'Trong giỏ hàng')
;
-- 19. Insert dữ liệu bảng voucher
INSERT INTO voucher (ma_voucher, ten_voucher, loai_giam_gia, trang_thai, duong_dan_hinh_anh, gia_tri_giam_toi_da, gia_tri_giam, gia_tri_giam_toi_thieu, so_luong, ngay_bat_dau, ngay_ket_thuc, ngay_tao, ngay_cap_nhat) VALUES
('VC001', 'Giảm 10% cho đơn hàng đầu tiên', 'PERCENT', 1, '/voucher/vc001.jpg', 500000, 0.10, 1000000, 100, '2024-01-01', '2024-12-31', NOW(), NOW()),
('VC002', 'Giảm 200K cho đơn từ 2tr', 'FIXED', 1, '/voucher/vc002.jpg', 200000, 200000, 2000000, 50, '2024-01-01', '2024-12-31', NOW(), NOW()),
('VC003', 'Freeship cho đơn từ 1tr5', 'SHIPPING', 1, '/voucher/vc003.jpg', 50000, 50000, 1500000, 200, '2024-01-01', '2024-12-31', NOW(), NOW()),
('VC004', 'Giảm 15% cho thành viên VIP', 'PERCENT', 1, '/voucher/vc004.jpg', 1000000, 0.15, 3000000, 30, '2024-06-01', '2024-12-31', NOW(), NOW())
;
-- 20. Insert dữ liệu bảng tai_khoan_voucher
INSERT INTO tai_khoan_voucher (id_tai_khoan, id_voucher, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
(4, 1, 1, NOW(), NOW()),
(4, 3, 1, NOW(), NOW()),
(5, 1, 1, NOW(), NOW()),
(5, 2, 1, NOW(), NOW()),
(6, 2, 1, NOW(), NOW()),
(6, 4, 1, NOW(), NOW()),
(7, 1, 1, NOW(), NOW()),
(8, 3, 1, NOW(), NOW())
;
-- 21. Insert dữ liệu bảng cong_thuc_tinh_diem
INSERT INTO cong_thuc_tinh_diem (tien_tich_diem, tien_tieu_diem, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
(10000, 1000, 1, NOW(), NOW())
;
-- 22. Insert dữ liệu bảng hoa_don
INSERT INTO hoa_don (id_khach_hang, id_nhan_vien, ma_hoa_don, dia_chi, email, ghi_chu, sdt, trang_thai_hoa_don, loai_hoa_don, ten_nguoi_dung, phuong_thuc_thanh_toan, phi_van_chuyen, diem_su_dung, tong_tien, gia_tri_diem, tong_thanh_toan, ngay_hoan_thanh, ngay_xac_nhan, ngay_tao, ngay_cap_nhat, ngay_giao_hang, ngay_nhan_hang, thoi_gian_van_chuyen) VALUES
(1, 2, 'HD001', '321 Hàng Bạc, Hoàn Kiếm, Hà Nội', 'khachhang1@gmail.com', 'Giao hàng giờ hành chính', '0901234567', 'Hoàn thành', 'ONLINE', 'Phạm Thị Khách Hàng', 'VNPAY', 30000, 50, 2500000, 1000, 2480000, '2024-07-20 14:30:00', '2024-07-18 09:15:00', '2024-07-18 08:30:00', NOW(), '2024-07-19 10:00:00', '2024-07-20 14:30:00', '2024-07-19 08:00:00'),
(2, 2, 'HD002', '654 Võ Thị Sáu, Quận 3, TP HCM', 'khachhang2@gmail.com', 'Gọi trước khi giao', '0901234568', 'Đang giao', 'ONLINE', 'Hoàng Văn Khách', 'COD', 30000, 0, 3600000, 1000, 3630000, NULL, '2024-07-22 11:20:00', '2024-07-22 10:45:00', NOW(), '2024-07-23 15:30:00', NULL, '2024-07-22 16:00:00'),
(3, 1, 'HD003', '987 Láng Hạ, Đống Đa, Hà Nội', 'khachhang3@gmail.com', '', '0901234569', 'Đã xác nhận', 'ONLINE', 'Vũ Thị Minh', 'MOMO', 30000, 100, 1800000, 1000, 1730000, NULL, '2024-07-23 16:45:00', '2024-07-23 15:20:00', NOW(), NULL, NULL, NULL),
(1, 3, 'HD004', '', '', 'Mua tại cửa hàng', '', 'Hoàn thành', 'OFFLINE', 'Phạm Thị Khách Hàng', 'CASH', 0, 0, 2000000, 1000, 2000000, '2024-07-21 16:00:00', '2024-07-21 16:00:00', '2024-07-21 16:00:00', NOW(), NULL, NULL, NULL)
;
-- 23. Insert dữ liệu bảng hoa_don_chi_tiet
INSERT INTO hoa_don_chi_tiet (id_hoa_don, id_ctsp, gia, so_luong, trang_thai_hoa_don, ngay_tao, ngay_cap_nhat) VALUES
(1, 1, 2500000, 1, 'Hoàn thành', NOW(), NOW()),
(2, 3, 3200000, 1, 'Đang giao', NOW(), NOW()),
(2, 6, 1800000, 1, 'Đang giao', NOW(), NOW()),
(3, 5, 1800000, 1, 'Đã xác nhận', NOW(), NOW()),
(4, 7, 2000000, 1, 'Hoàn thành', NOW(), NOW())
;
-- 25. Insert dữ liệu bảng lich_su_diem
INSERT INTO lich_su_diem (id_hoa_don, id_cong_thuc_tinh_diem, id_vi_diem, loai_diem, gia_tri_diem, ngay_tao, ngay_cap_nhat) VALUES
(1, 1, 1, 1, 250, NOW(), NOW()),  -- Tích điểm từ HD001
(1, 1, 1, -1, 50, NOW(), NOW()), -- Sử dụng điểm trong HD001
(2, 1, 2, 1, 360, NOW(), NOW()),  -- Tích điểm từ HD002
(3, 1, 3, 1, 180, NOW(), NOW()),  -- Tích điểm từ HD003
(3, 1, 3, -1, 100, NOW(), NOW()), -- Sử dụng điểm trong HD003
(4, 1, 1, 1, 200, NOW(), NOW())   -- Tích điểm từ HD004
;
-- 27. Insert dữ liệu bảng lich_su_hoa_don
INSERT INTO lich_su_hoa_don (id_nhan_vien, id_hoa_don, mo_ta_hanh_dong, trang_thai_hoa_don, ngay_tao, ngay_cap_nhat) VALUES
(2, 1, 'Tạo đơn hàng mới', 'Chờ xác nhận', '2024-07-18 08:30:00', '2024-07-18 08:30:00'),
(2, 1, 'Xác nhận đơn hàng', 'Đã xác nhận', '2024-07-18 09:15:00', '2024-07-18 09:15:00'),
(2, 1, 'Bàn giao cho đơn vị vận chuyển', 'Đang giao', '2024-07-19 10:00:00', '2024-07-19 10:00:00'),
(2, 1, 'Giao hàng thành công', 'Hoàn thành', '2024-07-20 14:30:00', '2024-07-20 14:30:00'),
(2, 2, 'Tạo đơn hàng mới', 'Chờ xác nhận', '2024-07-22 10:45:00', '2024-07-22 10:45:00'),
(2, 2, 'Xác nhận đơn hàng', 'Đã xác nhận', '2024-07-22 11:20:00', '2024-07-22 11:20:00'),
(2, 2, 'Bàn giao cho đơn vị vận chuyển', 'Đang giao', '2024-07-23 15:30:00', '2024-07-23 15:30:00'),
(1, 3, 'Tạo đơn hàng mới', 'Chờ xác nhận', '2024-07-23 15:20:00', '2024-07-23 15:20:00'),
(1, 3, 'Xác nhận đơn hàng', 'Đã xác nhận', '2024-07-23 16:45:00', '2024-07-23 16:45:00'),
(3, 4, 'Tạo đơn hàng tại cửa hàng', 'Hoàn thành', '2024-07-21 16:00:00', '2024-07-21 16:00:00')
;
-- 28. Insert dữ liệu bảng chi_tiet_tra_hang
INSERT INTO chi_tiet_tra_hang (ma_chi_tiet_tra_hang, so_luong, trang_thai_hoa_don, ngay_tao, ngay_tao_tra_hang, ngay_cap_nhat, id_ctsp) VALUES
('CTTH001', 1, 'Đã trả hàng', '2024-07-25 10:00:00', '2024-07-25 10:00:00', '2024-07-25 10:00:00', 1)
;
-- =============================================
-- CẬP NHẬT SỐ LƯỢNG TỒN KHO SAU KHI BÁN HÀNG
-- =============================================

-- Cập nhật số lượng chi tiết sản phẩm sau khi bán
UPDATE chi_tiet_san_pham SET so_luong = so_luong - 1 WHERE id = 1;  -- CTSP001 bán 1 cái
UPDATE chi_tiet_san_pham SET so_luong = so_luong - 1 WHERE id = 3;  -- CTSP003 bán 1 cái  
UPDATE chi_tiet_san_pham SET so_luong = so_luong - 1 WHERE id = 6;  -- CTSP006 bán 1 cái
UPDATE chi_tiet_san_pham SET so_luong = so_luong - 1 WHERE id = 5;  -- CTSP005 bán 1 cái
UPDATE chi_tiet_san_pham SET so_luong = so_luong - 1 WHERE id = 7;  -- CTSP007 bán 1 cái

-- Cập nhật số lượng tổng sản phẩm
UPDATE san_pham SET so_luong = so_luong - 1 WHERE id = 1;  -- SP001 bán 1 cái
UPDATE san_pham SET so_luong = so_luong - 1 WHERE id = 2;  -- SP002 bán 1 cái
UPDATE san_pham SET so_luong = so_luong - 1 WHERE id = 3;  -- SP003 bán 2 cái
UPDATE san_pham SET so_luong = so_luong - 1 WHERE id = 4;  -- SP004 bán 1 cái

-- Cập nhật số lượng sau khi trả hàng (tăng lại 1)
UPDATE chi_tiet_san_pham SET so_luong = so_luong + 1 WHERE id = 1;  -- CTTH001 trả 1 cái
UPDATE san_pham SET so_luong = so_luong + 1 WHERE id = 1;           -- SP001 trả 1 cái

-- =============================================
-- THÊM DỮ LIỆU BỔ SUNG 
-- =============================================

-- Thêm chi tiết sản phẩm với nhiều màu sắc và kích cỡ khác nhau
INSERT INTO chi_tiet_san_pham (ma_chi_tiet, ma_QR, so_luong, gia_ban, gia_goc, trang_thai, ngay_tao, ngay_cap_nhat, id_mau_sac, id_kich_co, id_san_pham, id_hinh_anh) VALUES
('CTSP011', 'QR011', 15, 2500000.00, 2000000.00, 1, NOW(), NOW(), 2, 6, 1, 1), -- Nike Air Max 270 Trắng Size 40
('CTSP012', 'QR012', 10, 2500000.00, 2000000.00, 1, NOW(), NOW(), 3, 7, 1, 1), -- Nike Air Max 270 Đỏ Size 41
('CTSP013', 'QR013', 12, 3200000.00, 2800000.00, 1, NOW(), NOW(), 1, 8, 2, 2), -- Adidas Ultraboost Đen Size 42
('CTSP014', 'QR014', 8, 1800000.00, 1500000.00, 1, NOW(), NOW(), 2, 5, 3, 3),  -- Converse Trắng Size 39
('CTSP015', 'QR015', 14, 2000000.00, 1700000.00, 1, NOW(), NOW(), 9, 8, 4, 4)  -- Vans Xám Size 42
;
-- Thêm voucher mới
INSERT INTO voucher (ma_voucher, ten_voucher, loai_giam_gia, trang_thai, duong_dan_hinh_anh, gia_tri_giam_toi_da, gia_tri_giam, gia_tri_giam_toi_thieu, so_luong, ngay_bat_dau, ngay_ket_thuc, ngay_tao, ngay_cap_nhat) VALUES
('VC005', 'Giảm 5% cho tất cả sản phẩm', 'PERCENT', 1, '/voucher/vc005.jpg', 300000, 0.05, 500000, 500, '2024-01-01', '2024-12-31', NOW(), NOW()),
('VC006', 'Voucher sinh nhật - Giảm 25%', 'PERCENT', 1, '/voucher/vc006.jpg', 1500000, 0.25, 2000000, 20, '2024-01-01', '2024-12-31', NOW(), NOW())
;
-- Phân phối voucher cho khách hàng
INSERT INTO tai_khoan_voucher (id_tai_khoan, id_voucher, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
(4, 5, 1, NOW(), NOW()),
(5, 5, 1, NOW(), NOW()),
(6, 5, 1, NOW(), NOW()),
(6, 6, 1, NOW(), NOW()), -- Khách hàng VIP có voucher sinh nhật
(7, 5, 1, NOW(), NOW()),
(8, 5, 1, NOW(), NOW())
;
SET FOREIGN_KEY_CHECKS = 1;
