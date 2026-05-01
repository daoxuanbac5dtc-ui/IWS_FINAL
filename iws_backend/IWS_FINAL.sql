use master 
go
CREATE DATABASE PRO2113
GO
USE PRO2113
GO

-- Tạo các bảng (giữ nguyên mã của bạn)
CREATE TABLE mau_sac (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    ma_mau_sac VARCHAR(25) NOT NULL,
    ten_mau_sac NVARCHAR(225) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE thuong_hieu (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    ma_thuong_hieu VARCHAR(25) NOT NULL,
    ten_thuong_hieu NVARCHAR(225) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE kich_co (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    ma_kich_co VARCHAR(25) NOT NULL,
    ten_kich_co NVARCHAR(225) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE de_giay (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    ma_de_giay VARCHAR(25) NOT NULL,
    ten_de_giay NVARCHAR(225) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE danh_muc (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    ma_danh_muc VARCHAR(25) NOT NULL,
    ten_danh_muc NVARCHAR(225) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE chat_lieu (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    ma_chat_lieu VARCHAR(25) NOT NULL,
    ten_chat_lieu NVARCHAR(225) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE san_pham (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    ma_san_pham VARCHAR(25) NOT NULL,
    ten_san_pham NVARCHAR(225) NOT NULL,
    so_luong INT NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME,
    id_chat_lieu INT,
    id_de_giay INT,
    id_danh_muc INT,
    id_thuong_hieu INT
)
GO

CREATE TABLE hinh_anh (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    ma_hinh_anh VARCHAR(25) NOT NULL,
    ten_hinh_anh NVARCHAR(225) NOT NULL,
    duong_dan VARCHAR(225) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE chi_tiet_san_pham (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    ma_chi_tiet VARCHAR(25) NOT NULL,
    ma_QR NVARCHAR(250),
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
GO

CREATE TABLE khuyen_mai (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    ma_khuyen_mai VARCHAR(25) NOT NULL,
    ten_khuyen_mai NVARCHAR(225) NOT NULL,
    ngay_bat_dau DATETIME NOT NULL,
    ngay_ket_thuc DATETIME NOT NULL,
    trang_thai INT NOT NULL,
    gia_tri FLOAT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE khuyen_mai_chi_tiet (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME,
    id_chi_tiet INT,
    id_khuyen_mai INT 
)
GO

CREATE TABLE tai_khoan (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    ma_tai_khoan VARCHAR(25) NOT NULL,
    email VARCHAR(50) NOT NULL,
    mat_khau VARCHAR(225) NOT NULL,
    vai_tro INT NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE dia_chi (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    id_tai_khoan INT NOT NULL,
    ma_tinh VARCHAR(25) NOT NULL,
   
    ma_phuong VARCHAR(25) NOT NULL,
    ten_tinh NVARCHAR(225) NOT NULL,
   
    ten_phuong NVARCHAR(225) NOT NULL,
    dia_chi_chi_tiet NVARCHAR(250) NOT NULL,
    is_default BIT DEFAULT 0,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO
CREATE TABLE vi_diem (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    tong_diem FLOAT NOT NULL,
    so_diem_da_dung FLOAT NOT NULL,
    so_diem_da_cong FLOAT NOT NULL,
    gia_tri_diem FLOAT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE khach_hang (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    id_vi_diem INT,
    id_tai_khoan INT,
    ma_khach_hang VARCHAR(25) NOT NULL,
    -- BỎ: email VARCHAR(50) NOT NULL,  <-- Email đã có trong tai_khoan
    ho_ten NVARCHAR(225) NOT NULL,
    sdt VARCHAR(10) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE nhan_vien (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    id_tai_khoan INT,
 --   id_dia_chi INT, 
    ma_nhan_vien VARCHAR(25) NOT NULL,
   -- email VARCHAR(50) NOT NULL,
    ho_ten NVARCHAR(225) NOT NULL,
    sdt VARCHAR(10) NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE gio_hang (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    id_tai_khoan INT,
    ma_gio_hang VARCHAR(25) NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE gio_hang_chi_tiet (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    id_gio_hang INT,
    id_ctsp INT,
    ma_gio_hang_chi_tiet VARCHAR(25) NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME,
    gia FLOAT NOT NULL,
    so_luong INT NOT NULL,
    trang_thai_hoa_don NVARCHAR(250)
)
GO

CREATE TABLE voucher (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    ma_voucher VARCHAR(25),
    ten_voucher NVARCHAR(225),
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
GO

CREATE TABLE hoa_don (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    id_khach_hang INT,
    id_nhan_vien INT,
    ma_hoa_don VARCHAR(25) NOT NULL,
    dia_chi NVARCHAR(250),
    email VARCHAR(100),
    ghi_chu NVARCHAR(250),
    sdt VARCHAR(10),
    trang_thai_hoa_don NVARCHAR(250),
    loai_hoa_don VARCHAR(25),
    ten_nguoi_dung NVARCHAR(250),
	phuong_thuc_thanh_toan NVARCHAR(50),
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
GO

CREATE TABLE hoa_don_chi_tiet (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    id_hoa_don INT,
    id_ctsp INT,
    gia FLOAT NOT NULL,
    so_luong INT NOT NULL,
    trang_thai_hoa_don NVARCHAR(250) NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE chi_tiet_voucher (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    ma_chi_tiet_voucher VARCHAR(255) NOT NULL,
    id_hoa_don INT,
    id_voucher INT, -- Vẫn giữ để tham chiếu (có thể NULL nếu voucher bị xóa)
    
    -- Thông tin voucher tại thời điểm áp dụng
    ma_voucher VARCHAR(255),
    ten_voucher NVARCHAR(225),
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
GO

CREATE TABLE tai_khoan_voucher (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    id_tai_khoan INT,
    id_voucher INT,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE cong_thuc_tinh_diem (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    tien_tich_diem FLOAT NOT NULL,
    tien_tieu_diem FLOAT NOT NULL,
    trang_thai INT NOT NULL,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO

CREATE TABLE lich_su_diem (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    id_hoa_don INT,
    id_cong_thuc_tinh_diem INT,
    id_vi_diem INT,
    loai_diem FLOAT,
    gia_tri_diem FLOAT,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME
)
GO


CREATE TABLE lich_su_hoa_don (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    id_nhan_vien INT,
    id_hoa_don INT,
    mo_ta_hanh_dong NVARCHAR(250) NOT NULL,
    trang_thai_hoa_don NVARCHAR(250) NOT NULL,
    ngay_tao DATETIME NOT NULL,
    ngay_cap_nhat DATETIME NOT NULL
)
GO

CREATE TABLE chi_tiet_tra_hang (
    id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    ma_chi_tiet_tra_hang VARCHAR(25) NOT NULL,
    so_luong INT NOT NULL,
    trang_thai_hoa_don NVARCHAR(250) NOT NULL,
    ngay_tao DATETIME NOT NULL,
    ngay_tao_tra_hang DATETIME NOT NULL,
    ngay_cap_nhat DATETIME NOT NULL,
    id_ctsp INT,
	id_hoa_don int,
    ly_do VARCHAR(255),
    duong_dan_anh VARCHAR(255) 
)
GO

-- Thêm các ràng buộc khóa ngoại
ALTER TABLE san_pham
ADD CONSTRAINT fk_sp_th FOREIGN KEY (id_thuong_hieu) REFERENCES thuong_hieu(id),
    CONSTRAINT fk_sp_cl FOREIGN KEY (id_chat_lieu) REFERENCES chat_lieu(id),
    CONSTRAINT fk_sp_dg FOREIGN KEY (id_de_giay) REFERENCES de_giay(id),
    CONSTRAINT fk_sp_dm FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id)
GO

ALTER TABLE chi_tiet_san_pham
ADD CONSTRAINT fk_ctsp_ms FOREIGN KEY (id_mau_sac) REFERENCES mau_sac(id),
    CONSTRAINT fk_ctsp_kc FOREIGN KEY (id_kich_co) REFERENCES kich_co(id),
    CONSTRAINT fk_ctsp_sp FOREIGN KEY (id_san_pham) REFERENCES san_pham(id),
    CONSTRAINT fk_ctsp_ha FOREIGN KEY (id_hinh_anh) REFERENCES hinh_anh(id)
GO

ALTER TABLE khuyen_mai_chi_tiet
ADD CONSTRAINT fk_kmct_ctsp FOREIGN KEY (id_chi_tiet) REFERENCES chi_tiet_san_pham(id),
    CONSTRAINT fk_kmct_km FOREIGN KEY (id_khuyen_mai) REFERENCES khuyen_mai(id)
GO

ALTER TABLE dia_chi
ADD CONSTRAINT fk_dc_tk FOREIGN KEY (id_tai_khoan) REFERENCES tai_khoan(id)
GO

ALTER TABLE khach_hang
ADD CONSTRAINT fk_kh_tk FOREIGN KEY (id_tai_khoan) REFERENCES tai_khoan(id),
    CONSTRAINT fk_kh_vd FOREIGN KEY (id_vi_diem) REFERENCES vi_diem(id)
GO

ALTER TABLE nhan_vien
ADD CONSTRAINT fk_nv_tk FOREIGN KEY (id_tai_khoan) REFERENCES tai_khoan(id)
GO

ALTER TABLE gio_hang
ADD CONSTRAINT fk_gh_tk FOREIGN KEY (id_tai_khoan) REFERENCES tai_khoan(id)
GO

ALTER TABLE gio_hang_chi_tiet
ADD CONSTRAINT fk_ghct_gh FOREIGN KEY (id_gio_hang) REFERENCES gio_hang(id),
    CONSTRAINT fk_ghct_ctsp FOREIGN KEY (id_ctsp) REFERENCES chi_tiet_san_pham(id)
GO

ALTER TABLE hoa_don
ADD CONSTRAINT fk_hd_kh FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    CONSTRAINT fk_hd_nv FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id)
GO

ALTER TABLE hoa_don_chi_tiet
ADD CONSTRAINT fk_hdct_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
    CONSTRAINT fk_hdct_ctsp FOREIGN KEY (id_ctsp) REFERENCES chi_tiet_san_pham(id)
GO

ALTER TABLE chi_tiet_voucher
ADD CONSTRAINT fk_ctv_voucher FOREIGN KEY (id_voucher) REFERENCES voucher(id),
    CONSTRAINT fk_ctv_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id)
GO

ALTER TABLE tai_khoan_voucher
ADD CONSTRAINT fk_tkv_v FOREIGN KEY (id_voucher) REFERENCES voucher(id),
    CONSTRAINT fk_tkv_tk FOREIGN KEY (id_tai_khoan) REFERENCES tai_khoan(id)
GO

ALTER TABLE lich_su_diem
ADD CONSTRAINT fk_lsd_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
    CONSTRAINT fk_lsd_vd FOREIGN KEY (id_vi_diem) REFERENCES vi_diem(id),
    CONSTRAINT fk_lsd_cttd FOREIGN KEY (id_cong_thuc_tinh_diem) REFERENCES cong_thuc_tinh_diem(id)
GO

ALTER TABLE lich_su_hoa_don
ADD CONSTRAINT fk_lshd_nv FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id),
    CONSTRAINT fk_lshd_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id)
GO

ALTER TABLE chi_tiet_tra_hang
ADD CONSTRAINT fk_ctth_ctsp FOREIGN KEY (id_ctsp) REFERENCES chi_tiet_san_pham(id),
	CONSTRAINT fk_ctth_hd FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id)
GO

-- Script insert dữ liệu chuẩn cho database PRO2113
USE PRO2113
GO

-- 1. Insert dữ liệu bảng mau_sac
INSERT INTO mau_sac (ma_mau_sac, ten_mau_sac, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('MS001', N'Đen', 1, GETDATE(), GETDATE()),
('MS002', N'Trắng', 1, GETDATE(), GETDATE()),
('MS003', N'Đỏ', 1, GETDATE(), GETDATE()),
('MS004', N'Xanh dương', 1, GETDATE(), GETDATE()),
('MS005', N'Xanh lá', 1, GETDATE(), GETDATE()),
('MS006', N'Vàng', 1, GETDATE(), GETDATE()),
('MS007', N'Hồng', 1, GETDATE(), GETDATE()),
('MS008', N'Nâu', 1, GETDATE(), GETDATE()),
('MS009', N'Xám', 1, GETDATE(), GETDATE()),
('MS010', N'Cam', 1, GETDATE(), GETDATE())
GO

-- 2. Insert dữ liệu bảng thuong_hieu
INSERT INTO thuong_hieu (ma_thuong_hieu, ten_thuong_hieu, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('TH001', N'Nike', 1, GETDATE(), GETDATE()),
('TH002', N'Adidas', 1, GETDATE(), GETDATE()),
('TH003', N'Converse', 1, GETDATE(), GETDATE()),
('TH004', N'Vans', 1, GETDATE(), GETDATE()),
('TH005', N'Puma', 1, GETDATE(), GETDATE()),
('TH006', N'New Balance', 1, GETDATE(), GETDATE()),
('TH007', N'Reebok', 1, GETDATE(), GETDATE()),
('TH008', N'Jordan', 1, GETDATE(), GETDATE()),
('TH009', N'Under Armour', 1, GETDATE(), GETDATE()),
('TH010', N'Asics', 1, GETDATE(), GETDATE())
GO

-- 3. Insert dữ liệu bảng kich_co
INSERT INTO kich_co (ma_kich_co, ten_kich_co, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('KC001', N'35', 1, GETDATE(), GETDATE()),
('KC002', N'36', 1, GETDATE(), GETDATE()),
('KC003', N'37', 1, GETDATE(), GETDATE()),
('KC004', N'38', 1, GETDATE(), GETDATE()),
('KC005', N'39', 1, GETDATE(), GETDATE()),
('KC006', N'40', 1, GETDATE(), GETDATE()),
('KC007', N'41', 1, GETDATE(), GETDATE()),
('KC008', N'42', 1, GETDATE(), GETDATE()),
('KC009', N'43', 1, GETDATE(), GETDATE()),
('KC010', N'44', 1, GETDATE(), GETDATE())
GO

-- 4. Insert dữ liệu bảng de_giay
INSERT INTO de_giay (ma_de_giay, ten_de_giay, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('DG001', N'Đế cao su', 1, GETDATE(), GETDATE()),
('DG002', N'Đế EVA', 1, GETDATE(), GETDATE()),
('DG003', N'Đế PU', 1, GETDATE(), GETDATE()),
('DG004', N'Đế Air Max', 1, GETDATE(), GETDATE()),
('DG005', N'Đế Boost', 1, GETDATE(), GETDATE()),
('DG006', N'Đế nhựa', 1, GETDATE(), GETDATE()),
('DG007', N'Đế da', 1, GETDATE(), GETDATE()),
('DG008', N'Đế gel', 1, GETDATE(), GETDATE())
GO

-- 5. Insert dữ liệu bảng danh_muc
INSERT INTO danh_muc (ma_danh_muc, ten_danh_muc, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('DM001', N'Giày thể thao', 1, GETDATE(), GETDATE()),
('DM002', N'Giày chạy bộ', 1, GETDATE(), GETDATE()),
('DM003', N'Giày bóng đá', 1, GETDATE(), GETDATE()),
('DM004', N'Giày cao gót', 1, GETDATE(), GETDATE()),
('DM005', N'Giày sandal', 1, GETDATE(), GETDATE()),
('DM006', N'Giày boot', 1, GETDATE(), GETDATE()),
('DM007', N'Giày tây', 1, GETDATE(), GETDATE()),
('DM008', N'Giày sneaker', 1, GETDATE(), GETDATE())
GO

-- 6. Insert dữ liệu bảng chat_lieu
INSERT INTO chat_lieu (ma_chat_lieu, ten_chat_lieu, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('CL001', N'Da thật', 1, GETDATE(), GETDATE()),
('CL002', N'Da tổng hợp', 1, GETDATE(), GETDATE()),
('CL003', N'Canvas', 1, GETDATE(), GETDATE()),
('CL004', N'Vải dệt kim', 1, GETDATE(), GETDATE()),
('CL005', N'Mesh', 1, GETDATE(), GETDATE()),
('CL006', N'Suede', 1, GETDATE(), GETDATE()),
('CL007', N'Nylon', 1, GETDATE(), GETDATE()),
('CL008', N'Flyknit', 1, GETDATE(), GETDATE())
GO

-- 7. Insert dữ liệu bảng hinh_anh
INSERT INTO hinh_anh (ma_hinh_anh, ten_hinh_anh, duong_dan, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('HA001', N'Nike Air Max 270 - Đen', '/images/nike-air-max-270-black.jpg', 1, GETDATE(), GETDATE()),
('HA002', N'Adidas Ultraboost 22 - Trắng', '/images/adidas-ultraboost-22-white.jpg', 1, GETDATE(), GETDATE()),
('HA003', N'Converse Chuck Taylor - Đỏ', '/images/converse-chuck-taylor-red.jpg', 1, GETDATE(), GETDATE()),
('HA004', N'Vans Old Skool - Đen Trắng', '/images/vans-old-skool-black-white.jpg', 1, GETDATE(), GETDATE()),
('HA005', N'Puma Suede Classic - Xanh', '/images/puma-suede-classic-blue.jpg', 1, GETDATE(), GETDATE()),
('HA006', N'New Balance 574 - Xám', '/images/new-balance-574-gray.jpg', 1, GETDATE(), GETDATE()),
('HA007', N'Jordan 1 High - Đỏ Đen', '/images/jordan-1-high-red-black.jpg', 1, GETDATE(), GETDATE()),
('HA008', N'Under Armour HOVR - Trắng', '/images/under-armour-hovr-white.jpg', 1, GETDATE(), GETDATE())
GO

-- 8. Insert dữ liệu bảng san_pham
INSERT INTO san_pham (ma_san_pham, ten_san_pham, so_luong, trang_thai, ngay_tao, ngay_cap_nhat, id_chat_lieu, id_de_giay, id_danh_muc, id_thuong_hieu) VALUES
('SP001', N'Nike Air Max 270', 100, 1, GETDATE(), GETDATE(), 2, 4, 1, 1),
('SP002', N'Adidas Ultraboost 22', 80, 1, GETDATE(), GETDATE(), 4, 5, 2, 2),
('SP003', N'Converse Chuck Taylor All Star', 120, 1, GETDATE(), GETDATE(), 3, 1, 8, 3),
('SP004', N'Vans Old Skool', 90, 1, GETDATE(), GETDATE(), 6, 1, 8, 4),
('SP005', N'Puma Suede Classic', 70, 1, GETDATE(), GETDATE(), 6, 1, 1, 5),
('SP006', N'New Balance 574', 60, 1, GETDATE(), GETDATE(), 5, 2, 1, 6),
('SP007', N'Jordan 1 High OG', 50, 1, GETDATE(), GETDATE(), 1, 1, 8, 8),
('SP008', N'Under Armour HOVR Phantom', 40, 1, GETDATE(), GETDATE(), 4, 2, 2, 9)
GO

-- 9. Insert dữ liệu bảng chi_tiet_san_pham
INSERT INTO chi_tiet_san_pham (ma_chi_tiet, ma_QR, so_luong, gia_ban, gia_goc, trang_thai, ngay_tao, ngay_cap_nhat, id_mau_sac, id_kich_co, id_san_pham, id_hinh_anh) VALUES
('CTSP001', 'QR001', 20, 2500000.00, 2000000.00, 1, GETDATE(), GETDATE(), 1, 6, 1, 1),
('CTSP002', 'QR002', 15, 2500000.00, 2000000.00, 1, GETDATE(), GETDATE(), 1, 7, 1, 1),
('CTSP003', 'QR003', 18, 3200000.00, 2800000.00, 1, GETDATE(), GETDATE(), 2, 6, 2, 2),
('CTSP004', 'QR004', 12, 3200000.00, 2800000.00, 1, GETDATE(), GETDATE(), 2, 7, 2, 2),
('CTSP005', 'QR005', 25, 1800000.00, 1500000.00, 1, GETDATE(), GETDATE(), 3, 5, 3, 3),
('CTSP006', 'QR006', 20, 1800000.00, 1500000.00, 1, GETDATE(), GETDATE(), 3, 6, 3, 3),
('CTSP007', 'QR007', 22, 2000000.00, 1700000.00, 1, GETDATE(), GETDATE(), 1, 6, 4, 4),
('CTSP008', 'QR008', 18, 2000000.00, 1700000.00, 1, GETDATE(), GETDATE(), 2, 7, 4, 4),
('CTSP009', 'QR009', 16, 2200000.00, 1900000.00, 1, GETDATE(), GETDATE(), 4, 6, 5, 5),
('CTSP010', 'QR010', 14, 2200000.00, 1900000.00, 1, GETDATE(), GETDATE(), 4, 7, 5, 5)
GO

-- 10. Insert dữ liệu bảng khuyen_mai
INSERT INTO khuyen_mai (ma_khuyen_mai, ten_khuyen_mai, ngay_bat_dau, ngay_ket_thuc, trang_thai, gia_tri, ngay_tao, ngay_cap_nhat) VALUES
('KM001', N'Khuyến mãi mùa hè', '2024-06-01', '2024-08-31', 1, 0.15, GETDATE(), GETDATE()),
('KM002', N'Black Friday Sale', '2024-11-25', '2024-11-30', 1, 0.30, GETDATE(), GETDATE()),
('KM003', N'Tết Nguyên Đán', '2025-01-20', '2025-02-15', 1, 0.20, GETDATE(), GETDATE()),
('KM004', N'Back to School', '2024-08-15', '2024-09-15', 1, 0.10, GETDATE(), GETDATE())
GO

-- 11. Insert dữ liệu bảng khuyen_mai_chi_tiet
INSERT INTO khuyen_mai_chi_tiet (trang_thai, ngay_tao, ngay_cap_nhat, id_chi_tiet, id_khuyen_mai) VALUES
(1, GETDATE(), GETDATE(), 1, 1),
(1, GETDATE(), GETDATE(), 2, 1),
(1, GETDATE(), GETDATE(), 3, 2),
(1, GETDATE(), GETDATE(), 4, 2),
(1, GETDATE(), GETDATE(), 5, 3),
(1, GETDATE(), GETDATE(), 6, 3)
GO

-- 12. Insert dữ liệu bảng tai_khoan
INSERT INTO tai_khoan (ma_tai_khoan, email, mat_khau, vai_tro, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
('TK001', 'admin@shoestore.com', '$2a$10$xXnh/NbED5JLjHJs0CQXietjKJ5B/73T045M6ph3ZzDGKgZBcBqeW', 2, 1, GETDATE(), GETDATE()),
('TK002', 'nhanvien1@shoestore.com', '$2a$10$FTlIlrKAoPdA0lr3y8ZxX.175LZ7DlRjSlBv5bt1dtJEoBetaMHYa', 1, 1, GETDATE(), GETDATE()),
('TK003', 'nhanvien2@shoestore.com', '$2a$10$FTlIlrKAoPdA0lr3y8ZxX.175LZ7DlRjSlBv5bt1dtJEoBetaMHYa', 1, 1, GETDATE(), GETDATE()),
('TK004', 'khachhang1@gmail.com', '$2a$10$vS7uF8ciseto6K/3Ooz0wey/HjPPlyrGUL4eAC6YkZu1Bf2tAjDSi', 0, 1, GETDATE(), GETDATE()),
('TK005', 'khachhang2@gmail.com', '$2a$10$vS7uF8ciseto6K/3Ooz0wey/HjPPlyrGUL4eAC6YkZu1Bf2tAjDSi', 0, 1, GETDATE(), GETDATE()),
('TK006', 'khachhang3@gmail.com', '$2a$10$vS7uF8ciseto6K/3Ooz0wey/HjPPlyrGUL4eAC6YkZu1Bf2tAjDSi', 0, 1, GETDATE(), GETDATE()),
('TK007', 'khachhang4@gmail.com', '$2a$10$vS7uF8ciseto6K/3Ooz0wey/HjPPlyrGUL4eAC6YkZu1Bf2tAjDSi', 0, 1, GETDATE(), GETDATE()),
('TK008', 'khachhang5@gmail.com', '$2a$10$vS7uF8ciseto6K/3Ooz0wey/HjPPlyrGUL4eAC6YkZu1Bf2tAjDSi', 0, 1, GETDATE(), GETDATE())
GO

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
(1, '01', '00001', N'Hà Nội', N'Phúc Xá', N'123 Phố Huế', 1, 1, GETDATE(), GETDATE()),
(2, '79', '00001', N'TP Hồ Chí Minh', N'Bến Nghé', N'456 Nguyễn Huệ', 1, 1, GETDATE(), GETDATE()),
(3, '48', '00001', N'Đà Nẵng', N'Hải Châu I', N'789 Trần Phú', 1, 1, GETDATE(), GETDATE()),
(4, '01', '00010', N'Hà Nội', N'Hàng Bạc', N'321 Hàng Bạc', 1, 1, GETDATE(), GETDATE()),
(5, '79', '00005', N'TP Hồ Chí Minh', N'Võ Thị Sáu', N'654 Võ Thị Sáu', 1, 1, GETDATE(), GETDATE()),
(6, '01', '00015', N'Hà Nội', N'Láng Thượng', N'987 Láng Hạ', 1, 1, GETDATE(), GETDATE()),
(7, '79', '00008', N'TP Hồ Chí Minh', N'Phường 5', N'147 Trần Hưng Đạo', 1, 1, GETDATE(), GETDATE()),
(8, '48', '00003', N'Đà Nẵng', N'Thanh Khê Đông', N'258 Lê Duẩn', 1, 1, GETDATE(), GETDATE());
GO

-- 14. Insert dữ liệu bảng vi_diem
INSERT INTO vi_diem (tong_diem, so_diem_da_dung, so_diem_da_cong, gia_tri_diem, ngay_tao, ngay_cap_nhat) VALUES
(1000, 200, 1200, 1000, GETDATE(), GETDATE()),
(500, 100, 600, 1000, GETDATE(), GETDATE()),
(2000, 500, 2500, 1000, GETDATE(), GETDATE()),
(300, 50, 350, 1000, GETDATE(), GETDATE()),
(750, 150, 900, 1000, GETDATE(), GETDATE())
GO

-- 15. Insert dữ liệu bảng khach_hang
INSERT INTO khach_hang (id_vi_diem, id_tai_khoan, ma_khach_hang, ho_ten, sdt, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
(1, 4, 'KH001', N'Nguyễn Văn An', '0901234567', 1, GETDATE(), GETDATE()),
(2, 5, 'KH002', N'Nguyễn Văn Bình', '0901234568', 1, GETDATE(), GETDATE()),
(3, 6, 'KH003', N'Nguyễn Văn Cường', '0901234569', 1, GETDATE(), GETDATE()),
(4, 7, 'KH004', N'Nguyễn Văn Đạt', '0901234570', 1, GETDATE(), GETDATE()),
(5, 8, 'KH005', N'Nguyễn Văn Huy', '0901234571', 1, GETDATE(), GETDATE())
GO

-- 16. Insert dữ liệu bảng nhan_vien
INSERT INTO nhan_vien (id_tai_khoan, ma_nhan_vien, ho_ten, sdt, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
(1, 'NV001', N'Nguyễn Hoàng Minh', '0987654321', 1, GETDATE(), GETDATE()),
(2, 'NV002', N'Trần Quốc Huy', '0987654322', 1, GETDATE(), GETDATE()),
(3, 'NV003', N'Lê Minh Khang', '0987654323', 1, GETDATE(), GETDATE())
GO

-- 17. Insert dữ liệu bảng gio_hang
INSERT INTO gio_hang (id_tai_khoan, ma_gio_hang, ngay_tao, ngay_cap_nhat) VALUES
(4, 'GH001', GETDATE(), GETDATE()),
(5, 'GH002', GETDATE(), GETDATE()),
(6, 'GH003', GETDATE(), GETDATE()),
(7, 'GH004', GETDATE(), GETDATE()),
(8, 'GH005', GETDATE(), GETDATE())
GO

-- 18. Insert dữ liệu bảng gio_hang_chi_tiet
INSERT INTO gio_hang_chi_tiet (id_gio_hang, id_ctsp, ma_gio_hang_chi_tiet, ngay_tao, ngay_cap_nhat, gia, so_luong, trang_thai_hoa_don) VALUES
(1, 1, 'GHCT001', GETDATE(), GETDATE(), 2500000, 1, N'Trong giỏ hàng'),
(1, 3, 'GHCT002', GETDATE(), GETDATE(), 3200000, 1, N'Trong giỏ hàng'),
(2, 5, 'GHCT003', GETDATE(), GETDATE(), 1800000, 2, N'Trong giỏ hàng'),
(3, 7, 'GHCT004', GETDATE(), GETDATE(), 2000000, 1, N'Trong giỏ hàng'),
(4, 9, 'GHCT005', GETDATE(), GETDATE(), 2200000, 1, N'Trong giỏ hàng')
GO

-- 19. Insert dữ liệu bảng voucher
INSERT INTO voucher (ma_voucher, ten_voucher, loai_giam_gia, trang_thai, duong_dan_hinh_anh, gia_tri_giam_toi_da, gia_tri_giam, gia_tri_giam_toi_thieu, so_luong, ngay_bat_dau, ngay_ket_thuc, ngay_tao, ngay_cap_nhat) VALUES
('VC001', N'Giảm 10% cho đơn hàng đầu tiên', 'PERCENT', 1, '/voucher/images/vc001.svg', 500000, 0.10, 1000000, 100, '2024-01-01', '2030-12-31', GETDATE(), GETDATE()),
('VC002', N'Giảm 200K cho đơn từ 2tr', 'FIXED', 1, '/voucher/images/vc002.svg', 200000, 200000, 2000000, 50, '2024-01-01', '2030-12-31', GETDATE(), GETDATE()),
('VC003', N'Freeship cho đơn từ 1tr5', 'SHIPPING', 1, '/voucher/images/vc003.svg', 50000, 50000, 1500000, 200, '2024-01-01', '2030-12-31', GETDATE(), GETDATE()),
('VC004', N'Giảm 15% cho thành viên VIP', 'PERCENT', 1, '/voucher/images/vc004.svg', 1000000, 0.15, 3000000, 30, '2024-06-01', '2030-12-31', GETDATE(), GETDATE())
GO

-- 20. Insert dữ liệu bảng tai_khoan_voucher
INSERT INTO tai_khoan_voucher (id_tai_khoan, id_voucher, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
(4, 1, 1, GETDATE(), GETDATE()),
(4, 3, 1, GETDATE(), GETDATE()),
(5, 1, 1, GETDATE(), GETDATE()),
(5, 2, 1, GETDATE(), GETDATE()),
(6, 2, 1, GETDATE(), GETDATE()),
(6, 4, 1, GETDATE(), GETDATE()),
(7, 1, 1, GETDATE(), GETDATE()),
(8, 3, 1, GETDATE(), GETDATE())
GO

-- 21. Insert dữ liệu bảng cong_thuc_tinh_diem
INSERT INTO cong_thuc_tinh_diem (tien_tich_diem, tien_tieu_diem, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
(10000, 1000, 1, GETDATE(), GETDATE())
GO

-- 22. Insert dữ liệu bảng hoa_don
INSERT INTO hoa_don (id_khach_hang, id_nhan_vien, ma_hoa_don, dia_chi, email, ghi_chu, sdt, trang_thai_hoa_don, loai_hoa_don, ten_nguoi_dung, phuong_thuc_thanh_toan, phi_van_chuyen, diem_su_dung, tong_tien, gia_tri_diem, tong_thanh_toan, ngay_hoan_thanh, ngay_xac_nhan, ngay_tao, ngay_cap_nhat, ngay_giao_hang, ngay_nhan_hang, thoi_gian_van_chuyen) VALUES
(1, 2, 'HD001', N'321 Hàng Bạc, Hoàn Kiếm, Hà Nội', 'khachhang1@gmail.com', N'Giao hàng giờ hành chính', '0901234567', N'Hoàn thành', 'ONLINE', N'Nguyễn Văn An', 'VNPAY', 30000, 50, 2500000, 1000, 2480000, '2024-07-20 14:30:00', '2024-07-18 09:15:00', '2024-07-18 08:30:00', GETDATE(), '2024-07-19 10:00:00', '2024-07-20 14:30:00', '2024-07-19 08:00:00'),
(2, 2, 'HD002', N'654 Võ Thị Sáu, Quận 3, TP HCM', 'khachhang2@gmail.com', N'Gọi trước khi giao', '0901234568', N'Đang giao', 'ONLINE', N'Nguyễn Văn Bình', 'COD', 30000, 0, 3600000, 1000, 3630000, NULL, '2024-07-22 11:20:00', '2024-07-22 10:45:00', GETDATE(), '2024-07-23 15:30:00', NULL, '2024-07-22 16:00:00'),
(3, 1, 'HD003', N'987 Láng Hạ, Đống Đa, Hà Nội', 'khachhang3@gmail.com', N'', '0901234569', N'Đã xác nhận', 'ONLINE', N'Nguyễn Văn Cường', 'MOMO', 30000, 100, 1800000, 1000, 1730000, NULL, '2024-07-23 16:45:00', '2024-07-23 15:20:00', GETDATE(), NULL, NULL, NULL),
(1, 3, 'HD004', N'', '', N'Mua tại cửa hàng', '', N'Hoàn thành', 'OFFLINE', N'Nguyễn Văn An', 'CASH', 0, 0, 2000000, 1000, 2000000, '2024-07-21 16:00:00', '2024-07-21 16:00:00', '2024-07-21 16:00:00', GETDATE(), NULL, NULL, NULL)
GO

-- 23. Insert dữ liệu bảng hoa_don_chi_tiet
INSERT INTO hoa_don_chi_tiet (id_hoa_don, id_ctsp, gia, so_luong, trang_thai_hoa_don, ngay_tao, ngay_cap_nhat) VALUES
(1, 1, 2500000, 1, N'Hoàn thành', GETDATE(), GETDATE()),
(2, 3, 3200000, 1, N'Đang giao', GETDATE(), GETDATE()),
(2, 6, 1800000, 1, N'Đang giao', GETDATE(), GETDATE()),
(3, 5, 1800000, 1, N'Đã xác nhận', GETDATE(), GETDATE()),
(4, 7, 2000000, 1, N'Hoàn thành', GETDATE(), GETDATE())
GO

-- 25. Insert dữ liệu bảng lich_su_diem
INSERT INTO lich_su_diem (id_hoa_don, id_cong_thuc_tinh_diem, id_vi_diem, loai_diem, gia_tri_diem, ngay_tao, ngay_cap_nhat) VALUES
(1, 1, 1, 1, 250, GETDATE(), GETDATE()),  -- Tích điểm từ HD001
(1, 1, 1, -1, 50, GETDATE(), GETDATE()), -- Sử dụng điểm trong HD001
(2, 1, 2, 1, 360, GETDATE(), GETDATE()),  -- Tích điểm từ HD002
(3, 1, 3, 1, 180, GETDATE(), GETDATE()),  -- Tích điểm từ HD003
(3, 1, 3, -1, 100, GETDATE(), GETDATE()), -- Sử dụng điểm trong HD003
(4, 1, 1, 1, 200, GETDATE(), GETDATE())   -- Tích điểm từ HD004
GO



-- 27. Insert dữ liệu bảng lich_su_hoa_don
INSERT INTO lich_su_hoa_don (id_nhan_vien, id_hoa_don, mo_ta_hanh_dong, trang_thai_hoa_don, ngay_tao, ngay_cap_nhat) VALUES
(2, 1, N'Tạo đơn hàng mới', N'Chờ xác nhận', '2024-07-18 08:30:00', '2024-07-18 08:30:00'),
(2, 1, N'Xác nhận đơn hàng', N'Đã xác nhận', '2024-07-18 09:15:00', '2024-07-18 09:15:00'),
(2, 1, N'Bàn giao cho đơn vị vận chuyển', N'Đang giao', '2024-07-19 10:00:00', '2024-07-19 10:00:00'),
(2, 1, N'Giao hàng thành công', N'Hoàn thành', '2024-07-20 14:30:00', '2024-07-20 14:30:00'),
(2, 2, N'Tạo đơn hàng mới', N'Chờ xác nhận', '2024-07-22 10:45:00', '2024-07-22 10:45:00'),
(2, 2, N'Xác nhận đơn hàng', N'Đã xác nhận', '2024-07-22 11:20:00', '2024-07-22 11:20:00'),
(2, 2, N'Bàn giao cho đơn vị vận chuyển', N'Đang giao', '2024-07-23 15:30:00', '2024-07-23 15:30:00'),
(1, 3, N'Tạo đơn hàng mới', N'Chờ xác nhận', '2024-07-23 15:20:00', '2024-07-23 15:20:00'),
(1, 3, N'Xác nhận đơn hàng', N'Đã xác nhận', '2024-07-23 16:45:00', '2024-07-23 16:45:00'),
(3, 4, N'Tạo đơn hàng tại cửa hàng', N'Hoàn thành', '2024-07-21 16:00:00', '2024-07-21 16:00:00')
GO

-- 28. Insert dữ liệu bảng chi_tiet_tra_hang
INSERT INTO chi_tiet_tra_hang (ma_chi_tiet_tra_hang, so_luong, trang_thai_hoa_don, ngay_tao, ngay_tao_tra_hang, ngay_cap_nhat, id_ctsp) VALUES
('CTTH001', 1, N'Đã trả hàng', '2024-07-25 10:00:00', '2024-07-25 10:00:00', '2024-07-25 10:00:00', 1)
GO

-- =============================================
-- CẬP NHẬT SỐ LƯỢNG TỒN KHO SAU KHI BÁN HÀNG
-- =============================================

-- Cập nhật số lượng chi tiết sản phẩm sau khi bán
UPDATE chi_tiet_san_pham SET so_luong = so_luong - 1 WHERE id = 1  -- CTSP001 bán 1 cái
UPDATE chi_tiet_san_pham SET so_luong = so_luong - 1 WHERE id = 3  -- CTSP003 bán 1 cái  
UPDATE chi_tiet_san_pham SET so_luong = so_luong - 1 WHERE id = 6  -- CTSP006 bán 1 cái
UPDATE chi_tiet_san_pham SET so_luong = so_luong - 1 WHERE id = 5  -- CTSP005 bán 1 cái
UPDATE chi_tiet_san_pham SET so_luong = so_luong - 1 WHERE id = 7  -- CTSP007 bán 1 cái

-- Cập nhật số lượng tổng sản phẩm
UPDATE san_pham SET so_luong = so_luong - 1 WHERE id = 1  -- SP001 bán 1 cái
UPDATE san_pham SET so_luong = so_luong - 1 WHERE id = 2  -- SP002 bán 1 cái
UPDATE san_pham SET so_luong = so_luong - 1 WHERE id = 3  -- SP003 bán 2 cái
UPDATE san_pham SET so_luong = so_luong - 1 WHERE id = 4  -- SP004 bán 1 cái

-- Cập nhật số lượng sau khi trả hàng (tăng lại 1)
UPDATE chi_tiet_san_pham SET so_luong = so_luong + 1 WHERE id = 1  -- CTTH001 trả 1 cái
UPDATE san_pham SET so_luong = so_luong + 1 WHERE id = 1           -- SP001 trả 1 cái

-- =============================================
-- THÊM DỮ LIỆU BỔ SUNG 
-- =============================================

-- Thêm chi tiết sản phẩm với nhiều màu sắc và kích cỡ khác nhau
INSERT INTO chi_tiet_san_pham (ma_chi_tiet, ma_QR, so_luong, gia_ban, gia_goc, trang_thai, ngay_tao, ngay_cap_nhat, id_mau_sac, id_kich_co, id_san_pham, id_hinh_anh) VALUES
('CTSP011', 'QR011', 15, 2500000.00, 2000000.00, 1, GETDATE(), GETDATE(), 2, 6, 1, 1), -- Nike Air Max 270 Trắng Size 40
('CTSP012', 'QR012', 10, 2500000.00, 2000000.00, 1, GETDATE(), GETDATE(), 3, 7, 1, 1), -- Nike Air Max 270 Đỏ Size 41
('CTSP013', 'QR013', 12, 3200000.00, 2800000.00, 1, GETDATE(), GETDATE(), 1, 8, 2, 2), -- Adidas Ultraboost Đen Size 42
('CTSP014', 'QR014', 8, 1800000.00, 1500000.00, 1, GETDATE(), GETDATE(), 2, 5, 3, 3),  -- Converse Trắng Size 39
('CTSP015', 'QR015', 14, 2000000.00, 1700000.00, 1, GETDATE(), GETDATE(), 9, 8, 4, 4),  -- Vans Xám Size 42
('CTSP016', 'QR016', 20, 1500000.00, 1800000.00, 1, GETDATE(), GETDATE(), 9, 6, 6, 6),  -- New Balance 574 Xám Size 40
('CTSP017', 'QR017', 12, 2000000.00, 2200000.00, 1, GETDATE(), GETDATE(), 3, 7, 7, 7),  -- Jordan 1 High OG Đỏ Size 41
('CTSP018', 'QR018', 16, 1800000.00, 2000000.00, 1, GETDATE(), GETDATE(), 2, 8, 8, 8)  -- Under Armour HOVR Trắng Size 42
GO

-- Thêm voucher mới
INSERT INTO voucher (ma_voucher, ten_voucher, loai_giam_gia, trang_thai, duong_dan_hinh_anh, gia_tri_giam_toi_da, gia_tri_giam, gia_tri_giam_toi_thieu, so_luong, ngay_bat_dau, ngay_ket_thuc, ngay_tao, ngay_cap_nhat) VALUES
('VC005', N'Giảm 5% cho tất cả sản phẩm', 'PERCENT', 1, '/voucher/images/vc005.svg', 300000, 0.05, 500000, 500, '2024-01-01', '2030-12-31', GETDATE(), GETDATE()),
('VC006', N'Voucher sinh nhật - Giảm 25%', 'PERCENT', 1, '/voucher/images/vc006.svg', 1500000, 0.25, 2000000, 20, '2024-01-01', '2030-12-31', GETDATE(), GETDATE())
GO

-- Phân phối voucher cho khách hàng
INSERT INTO tai_khoan_voucher (id_tai_khoan, id_voucher, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
(4, 5, 1, GETDATE(), GETDATE()),
(5, 5, 1, GETDATE(), GETDATE()),
(6, 5, 1, GETDATE(), GETDATE()),
(6, 6, 1, GETDATE(), GETDATE()), -- Khách hàng VIP có voucher sinh nhật
(7, 5, 1, GETDATE(), GETDATE()),
(8, 5, 1, GETDATE(), GETDATE())
GO
