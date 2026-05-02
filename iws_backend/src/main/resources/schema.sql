SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER DATABASE PRO2113 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE hoa_don
    MODIFY COLUMN email VARCHAR(100) NOT NULL;

ALTER TABLE mau_sac CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE de_giay CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE danh_muc CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE chat_lieu CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE hinh_anh CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE san_pham CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE khuyen_mai CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE tai_khoan CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE dia_chi CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE khach_hang CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE nhan_vien CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE gio_hang_chi_tiet CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE voucher CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE hoa_don CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE hoa_don_chi_tiet CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE lich_su_hoa_don CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE chi_tiet_tra_hang CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

UPDATE voucher
SET ngay_bat_dau = CASE
        WHEN ma_voucher = 'VC004' THEN '2024-06-01 00:00:00'
        ELSE '2024-01-01 00:00:00'
    END,
    ngay_ket_thuc = '2030-12-31 23:59:59',
    ngay_cap_nhat = NOW()
WHERE ma_voucher IN ('VC001', 'VC002', 'VC003', 'VC004', 'VC005', 'VC006');

UPDATE voucher
SET duong_dan_hinh_anh = CASE ma_voucher
    WHEN 'VC001' THEN '/voucher/images/vc001.svg'
    WHEN 'VC002' THEN '/voucher/images/vc002.svg'
    WHEN 'VC003' THEN '/voucher/images/vc003.svg'
    WHEN 'VC004' THEN '/voucher/images/vc004.svg'
    WHEN 'VC005' THEN '/voucher/images/vc005.svg'
    WHEN 'VC006' THEN '/voucher/images/vc006.svg'
    ELSE duong_dan_hinh_anh
END,
ngay_cap_nhat = NOW()
WHERE ma_voucher IN ('VC001', 'VC002', 'VC003', 'VC004', 'VC005', 'VC006')
  AND duong_dan_hinh_anh IN (
      '/voucher/vc001.jpg',
      '/voucher/vc002.jpg',
      '/voucher/vc003.jpg',
      '/voucher/vc004.jpg',
      '/voucher/vc005.jpg',
      '/voucher/vc006.jpg',
      '/voucher/images/vc001.jpg',
      '/voucher/images/vc002.jpg',
      '/voucher/images/vc003.jpg',
      '/voucher/images/vc004.jpg',
      '/voucher/images/vc005.jpg',
      '/voucher/images/vc006.jpg',
      '/voucher/images/vc001.svg',
      '/voucher/images/vc002.svg',
      '/voucher/images/vc003.svg',
      '/voucher/images/vc004.svg',
      '/voucher/images/vc005.svg',
      '/voucher/images/vc006.svg'
  );

UPDATE mau_sac
SET ten_mau_sac = CASE ma_mau_sac
    WHEN 'MS001' THEN 'Đen'
    WHEN 'MS002' THEN 'Trắng'
    WHEN 'MS003' THEN 'Đỏ'
    WHEN 'MS004' THEN 'Xanh dương'
    WHEN 'MS005' THEN 'Xanh lá'
    WHEN 'MS006' THEN 'Vàng'
    WHEN 'MS007' THEN 'Hồng'
    WHEN 'MS008' THEN 'Nâu'
    WHEN 'MS009' THEN 'Xám'
    WHEN 'MS010' THEN 'Cam'
    ELSE ten_mau_sac
END
WHERE ma_mau_sac IN ('MS001', 'MS002', 'MS003', 'MS004', 'MS005', 'MS006', 'MS007', 'MS008', 'MS009', 'MS010');

UPDATE de_giay
SET ten_de_giay = CASE ma_de_giay
    WHEN 'DG001' THEN 'Đế cao su'
    WHEN 'DG002' THEN 'Đế EVA'
    WHEN 'DG003' THEN 'Đế PU'
    WHEN 'DG004' THEN 'Đế Air Max'
    WHEN 'DG005' THEN 'Đế Boost'
    WHEN 'DG006' THEN 'Đế nhựa'
    WHEN 'DG007' THEN 'Đế da'
    WHEN 'DG008' THEN 'Đế gel'
    ELSE ten_de_giay
END
WHERE ma_de_giay IN ('DG001', 'DG002', 'DG003', 'DG004', 'DG005', 'DG006', 'DG007', 'DG008');

UPDATE danh_muc
SET ten_danh_muc = CASE ma_danh_muc
    WHEN 'DM001' THEN 'Giày thể thao'
    WHEN 'DM002' THEN 'Giày chạy bộ'
    WHEN 'DM003' THEN 'Giày bóng đá'
    WHEN 'DM004' THEN 'Giày cao gót'
    WHEN 'DM005' THEN 'Giày sandal'
    WHEN 'DM006' THEN 'Giày boot'
    WHEN 'DM007' THEN 'Giày tây'
    WHEN 'DM008' THEN 'Giày sneaker'
    ELSE ten_danh_muc
END
WHERE ma_danh_muc IN ('DM001', 'DM002', 'DM003', 'DM004', 'DM005', 'DM006', 'DM007', 'DM008');

UPDATE chat_lieu
SET ten_chat_lieu = CASE ma_chat_lieu
    WHEN 'CL001' THEN 'Da thật'
    WHEN 'CL002' THEN 'Da tổng hợp'
    WHEN 'CL003' THEN 'Canvas'
    WHEN 'CL004' THEN 'Vải dệt kim'
    WHEN 'CL005' THEN 'Mesh'
    WHEN 'CL006' THEN 'Suede'
    WHEN 'CL007' THEN 'Nylon'
    WHEN 'CL008' THEN 'Flyknit'
    ELSE ten_chat_lieu
END
WHERE ma_chat_lieu IN ('CL001', 'CL002', 'CL003', 'CL004', 'CL005', 'CL006', 'CL007', 'CL008');

UPDATE hinh_anh
SET ten_hinh_anh = CASE ma_hinh_anh
    WHEN 'HA001' THEN 'Nike Air Max 270 - Đen'
    WHEN 'HA002' THEN 'Adidas Ultraboost 22 - Trắng'
    WHEN 'HA003' THEN 'Converse Chuck Taylor - Đỏ'
    WHEN 'HA004' THEN 'Vans Old Skool - Đen Trắng'
    WHEN 'HA005' THEN 'Puma Suede Classic - Xanh'
    WHEN 'HA006' THEN 'New Balance 574 - Xám'
    WHEN 'HA007' THEN 'Jordan 1 High - Đỏ Đen'
    WHEN 'HA008' THEN 'Under Armour HOVR - Trắng'
    ELSE ten_hinh_anh
END
WHERE ma_hinh_anh IN ('HA001', 'HA002', 'HA003', 'HA004', 'HA005', 'HA006', 'HA007', 'HA008');

UPDATE khuyen_mai
SET ten_khuyen_mai = CASE ma_khuyen_mai
    WHEN 'KM001' THEN 'Khuyến mãi mùa hè'
    WHEN 'KM002' THEN 'Black Friday Sale'
    WHEN 'KM003' THEN 'Tết Nguyên Đán'
    WHEN 'KM004' THEN 'Back to School'
    ELSE ten_khuyen_mai
END
WHERE ma_khuyen_mai IN ('KM001', 'KM002', 'KM003', 'KM004');

UPDATE voucher
SET ten_voucher = CASE ma_voucher
    WHEN 'VC001' THEN 'Giảm 10% cho đơn hàng đầu tiên'
    WHEN 'VC002' THEN 'Giảm 200K cho đơn từ 2tr'
    WHEN 'VC003' THEN 'Freeship cho đơn từ 1tr5'
    WHEN 'VC004' THEN 'Giảm 15% cho thành viên VIP'
    WHEN 'VC005' THEN 'Giảm 5% cho tất cả sản phẩm'
    WHEN 'VC006' THEN 'Voucher sinh nhật - Giảm 25%'
    ELSE ten_voucher
END
WHERE ma_voucher IN ('VC001', 'VC002', 'VC003', 'VC004', 'VC005', 'VC006');

UPDATE dia_chi
SET ten_tinh = CASE id_tai_khoan
    WHEN 1 THEN 'Hà Nội'
    WHEN 2 THEN 'TP Hồ Chí Minh'
    WHEN 3 THEN 'Đà Nẵng'
    WHEN 4 THEN 'Hà Nội'
    WHEN 5 THEN 'TP Hồ Chí Minh'
    WHEN 6 THEN 'Hà Nội'
    WHEN 7 THEN 'TP Hồ Chí Minh'
    WHEN 8 THEN 'Đà Nẵng'
    ELSE ten_tinh
END,
ten_phuong = CASE id_tai_khoan
    WHEN 1 THEN 'Phúc Xá'
    WHEN 2 THEN 'Bến Nghé'
    WHEN 3 THEN 'Hải Châu I'
    WHEN 4 THEN 'Hàng Bạc'
    WHEN 5 THEN 'Võ Thị Sáu'
    WHEN 6 THEN 'Láng Thượng'
    WHEN 7 THEN 'Phường 5'
    WHEN 8 THEN 'Thanh Khê Đông'
    ELSE ten_phuong
END,
dia_chi_chi_tiet = CASE id_tai_khoan
    WHEN 1 THEN '123 Phố Huế'
    WHEN 2 THEN '456 Nguyễn Huệ'
    WHEN 3 THEN '789 Trần Phú'
    WHEN 4 THEN '321 Hàng Bạc'
    WHEN 5 THEN '654 Võ Thị Sáu'
    WHEN 6 THEN '987 Láng Hạ'
    WHEN 7 THEN '147 Trần Hưng Đạo'
    WHEN 8 THEN '258 Lê Duẩn'
    ELSE dia_chi_chi_tiet
END
WHERE id_tai_khoan IN (1, 2, 3, 4, 5, 6, 7, 8);

UPDATE khach_hang
SET ho_ten = CASE ma_khach_hang
    WHEN 'KH001' THEN 'Nguyễn Văn An'
    WHEN 'KH002' THEN 'Nguyễn Văn Bình'
    WHEN 'KH003' THEN 'Nguyễn Văn Cường'
    WHEN 'KH004' THEN 'Nguyễn Văn Đạt'
    WHEN 'KH005' THEN 'Nguyễn Văn Huy'
    ELSE ho_ten
END
WHERE ma_khach_hang IN ('KH001', 'KH002', 'KH003', 'KH004', 'KH005');

UPDATE khach_hang
SET ho_ten = CASE sdt
    WHEN '0935146213' THEN 'Nguyễn Văn Khang'
    WHEN '0357471462' THEN 'Lê Thành Đạt'
    WHEN '0993164388' THEN 'Nguyễn Văn Long'
    WHEN '0993381468' THEN 'Nguyễn Văn Minh'
    WHEN '0993644800' THEN 'Nguyễn Văn Nam'
    WHEN '0993725114' THEN 'Nguyễn Văn Phong'
    WHEN '0993837809' THEN 'Nguyễn Văn Quân'
    WHEN '0993879509' THEN 'Nguyễn Văn Sơn'
    WHEN '0947232178' THEN 'Nguyễn Văn Tú'
    WHEN '0947403171' THEN 'Nguyễn Văn Vũ'
    WHEN '0948713407' THEN 'Nguyễn Văn Bảo'
    WHEN '0949592893' THEN 'Nguyễn Văn Châu'
    WHEN '0949843045' THEN 'Nguyễn Văn Dũng'
    ELSE ho_ten
END
WHERE ho_ten LIKE '%?%'
  AND sdt IN (
      '0935146213', '0357471462', '0993164388', '0993381468', '0993644800',
      '0993725114', '0993837809', '0993879509', '0947232178', '0947403171',
      '0948713407', '0949592893', '0949843045'
  );

UPDATE nhan_vien
SET ho_ten = CASE ma_nhan_vien
    WHEN 'NV001' THEN 'Nguyễn Hoàng Minh'
    WHEN 'NV002' THEN 'Trần Quốc Huy'
    WHEN 'NV003' THEN 'Lê Minh Khang'
    ELSE ho_ten
END
WHERE ma_nhan_vien IN ('NV001', 'NV002', 'NV003');

UPDATE gio_hang_chi_tiet
SET trang_thai_hoa_don = 'Trong giỏ hàng'
WHERE ma_gio_hang_chi_tiet IN ('GHCT001', 'GHCT002', 'GHCT003', 'GHCT004', 'GHCT005');

UPDATE hoa_don
SET dia_chi = '321 Hàng Bạc, Hoàn Kiếm, Hà Nội',
    ghi_chu = 'Giao hàng giờ hành chính',
    trang_thai_hoa_don = 'Hoàn thành',
    ten_nguoi_dung = 'Nguyễn Văn An'
WHERE ma_hoa_don = 'HD001';

UPDATE hoa_don
SET dia_chi = '654 Võ Thị Sáu, Quận 3, TP HCM',
    ghi_chu = 'Gọi trước khi giao',
    trang_thai_hoa_don = 'Đang giao',
    ten_nguoi_dung = 'Nguyễn Văn Bình'
WHERE ma_hoa_don = 'HD002';

UPDATE hoa_don
SET dia_chi = '987 Láng Hạ, Đống Đa, Hà Nội',
    ghi_chu = '',
    trang_thai_hoa_don = 'Đã xác nhận',
    ten_nguoi_dung = 'Nguyễn Văn Cường'
WHERE ma_hoa_don = 'HD003';

UPDATE hoa_don
SET ghi_chu = 'Mua tại cửa hàng',
    trang_thai_hoa_don = 'Hoàn thành',
    ten_nguoi_dung = 'Nguyễn Văn An'
WHERE ma_hoa_don = 'HD004';

UPDATE hoa_don hd
JOIN khach_hang kh ON (
    hd.id_khach_hang = kh.id
    OR (hd.sdt IS NOT NULL AND hd.sdt <> '' AND hd.sdt = kh.sdt)
)
SET hd.ten_nguoi_dung = kh.ho_ten
WHERE hd.ten_nguoi_dung LIKE '%?%';

UPDATE hoa_don_chi_tiet
SET trang_thai_hoa_don = CASE id_hoa_don
    WHEN 1 THEN 'Hoàn thành'
    WHEN 2 THEN 'Đang giao'
    WHEN 3 THEN 'Đã xác nhận'
    WHEN 4 THEN 'Hoàn thành'
    ELSE trang_thai_hoa_don
END
WHERE id_hoa_don IN (1, 2, 3, 4);

UPDATE hoa_don
SET trang_thai_hoa_don = 'COMPLETED'
WHERE trang_thai_hoa_don = 'DA_THANH_TOAN';

UPDATE hoa_don_chi_tiet
SET trang_thai_hoa_don = 'COMPLETED'
WHERE trang_thai_hoa_don = 'DA_THANH_TOAN';

UPDATE lich_su_hoa_don
SET trang_thai_hoa_don = 'COMPLETED'
WHERE trang_thai_hoa_don = 'DA_THANH_TOAN';

INSERT INTO chi_tiet_san_pham (
    ma_chi_tiet, ma_QR, so_luong, gia_ban, gia_goc, trang_thai,
    ngay_tao, ngay_cap_nhat, id_mau_sac, id_kich_co, id_san_pham, id_hinh_anh
)
SELECT 'CTSP016', 'QR016', 20, 1500000.00, 1800000.00, 1,
       NOW(), NOW(), ms.id, kc.id, sp.id, ha.id
FROM san_pham sp
JOIN hinh_anh ha ON ha.ma_hinh_anh = 'HA006'
JOIN mau_sac ms ON ms.ma_mau_sac = 'MS009'
JOIN kich_co kc ON kc.ma_kich_co = 'KC006'
WHERE sp.ma_san_pham = 'SP006'
  AND NOT EXISTS (
      SELECT 1 FROM chi_tiet_san_pham ctsp WHERE ctsp.ma_chi_tiet = 'CTSP016'
  );

INSERT INTO chi_tiet_san_pham (
    ma_chi_tiet, ma_QR, so_luong, gia_ban, gia_goc, trang_thai,
    ngay_tao, ngay_cap_nhat, id_mau_sac, id_kich_co, id_san_pham, id_hinh_anh
)
SELECT 'CTSP017', 'QR017', 12, 2000000.00, 2200000.00, 1,
       NOW(), NOW(), ms.id, kc.id, sp.id, ha.id
FROM san_pham sp
JOIN hinh_anh ha ON ha.ma_hinh_anh = 'HA007'
JOIN mau_sac ms ON ms.ma_mau_sac = 'MS003'
JOIN kich_co kc ON kc.ma_kich_co = 'KC007'
WHERE sp.ma_san_pham = 'SP007'
  AND NOT EXISTS (
      SELECT 1 FROM chi_tiet_san_pham ctsp WHERE ctsp.ma_chi_tiet = 'CTSP017'
  );

INSERT INTO chi_tiet_san_pham (
    ma_chi_tiet, ma_QR, so_luong, gia_ban, gia_goc, trang_thai,
    ngay_tao, ngay_cap_nhat, id_mau_sac, id_kich_co, id_san_pham, id_hinh_anh
)
SELECT 'CTSP018', 'QR018', 16, 1800000.00, 2000000.00, 1,
       NOW(), NOW(), ms.id, kc.id, sp.id, ha.id
FROM san_pham sp
JOIN hinh_anh ha ON ha.ma_hinh_anh = 'HA008'
JOIN mau_sac ms ON ms.ma_mau_sac = 'MS002'
JOIN kich_co kc ON kc.ma_kich_co = 'KC008'
WHERE sp.ma_san_pham = 'SP008'
  AND NOT EXISTS (
      SELECT 1 FROM chi_tiet_san_pham ctsp WHERE ctsp.ma_chi_tiet = 'CTSP018'
  );

-- Seed sản phẩm cho các danh mục trước đó chưa có dữ liệu trên trang user.
INSERT INTO hinh_anh (ma_hinh_anh, ten_hinh_anh, duong_dan, trang_thai, ngay_tao, ngay_cap_nhat)
SELECT seed.ma_hinh_anh, seed.ten_hinh_anh, seed.duong_dan, 1, NOW(), NOW()
FROM (
    SELECT 'HA009' ma_hinh_anh, 'Nike Phantom Strike TF' ten_hinh_anh, '/images/football-nike-phantom-strike.jpg' duong_dan
    UNION ALL SELECT 'HA010', 'Adidas Predator Club FG', '/images/football-adidas-predator-club.png'
    UNION ALL SELECT 'HA011', 'Puma Elegance Stiletto', '/images/heel-puma-elegance-stiletto.jpg'
    UNION ALL SELECT 'HA012', 'Nike Coast Sandal', '/images/sandal-nike-coast-nikko.jpg'
    UNION ALL SELECT 'HA013', 'Adidas Breeze Sandal', '/images/sandal-adidas-breeze.jpg'
    UNION ALL SELECT 'HA014', 'Puma Chelsea Boot', '/images/boot-puma-chelsea.jpg'
    UNION ALL SELECT 'HA015', 'New Balance Trail Boot', '/images/boot-newbalance-trail.jpg'
    UNION ALL SELECT 'HA016', 'Reebok Classic Derby', '/images/dress-reebok-classic-derby.jpg'
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM hinh_anh ha WHERE ha.ma_hinh_anh = seed.ma_hinh_anh
);

INSERT INTO san_pham (ma_san_pham, ten_san_pham, so_luong, trang_thai, ngay_tao, ngay_cap_nhat, id_chat_lieu, id_de_giay, id_danh_muc, id_thuong_hieu)
SELECT seed.ma_san_pham, seed.ten_san_pham, seed.so_luong, 1, NOW(), NOW(), cl.id, dg.id, dm.id, th.id
FROM (
    SELECT 'SP009' ma_san_pham, 'Nike Phantom Strike TF' ten_san_pham, 36 so_luong, 'CL002' ma_chat_lieu, 'DG006' ma_de_giay, 'DM003' ma_danh_muc, 'TH001' ma_thuong_hieu
    UNION ALL SELECT 'SP010', 'Adidas Predator Club FG', 32, 'CL002', 'DG006', 'DM003', 'TH002'
    UNION ALL SELECT 'SP011', 'Puma Elegance Stiletto', 24, 'CL001', 'DG007', 'DM004', 'TH005'
    UNION ALL SELECT 'SP012', 'Nike Coast Sandal', 42, 'CL002', 'DG002', 'DM005', 'TH001'
    UNION ALL SELECT 'SP013', 'Adidas Breeze Sandal', 38, 'CL002', 'DG002', 'DM005', 'TH002'
    UNION ALL SELECT 'SP014', 'Puma Chelsea Boot', 28, 'CL001', 'DG007', 'DM006', 'TH005'
    UNION ALL SELECT 'SP015', 'New Balance Trail Boot', 30, 'CL005', 'DG008', 'DM006', 'TH006'
    UNION ALL SELECT 'SP016', 'Reebok Classic Derby', 22, 'CL001', 'DG007', 'DM007', 'TH007'
) seed
JOIN chat_lieu cl ON cl.ma_chat_lieu = seed.ma_chat_lieu
JOIN de_giay dg ON dg.ma_de_giay = seed.ma_de_giay
JOIN danh_muc dm ON dm.ma_danh_muc = seed.ma_danh_muc
JOIN thuong_hieu th ON th.ma_thuong_hieu = seed.ma_thuong_hieu
WHERE NOT EXISTS (
    SELECT 1 FROM san_pham sp WHERE sp.ma_san_pham = seed.ma_san_pham
);

INSERT INTO chi_tiet_san_pham (ma_chi_tiet, ma_QR, so_luong, gia_ban, gia_goc, trang_thai, ngay_tao, ngay_cap_nhat, id_mau_sac, id_kich_co, id_san_pham, id_hinh_anh)
SELECT seed.ma_chi_tiet, seed.ma_qr, seed.so_luong, seed.gia_ban, seed.gia_goc, 1, NOW(), NOW(), ms.id, kc.id, sp.id, ha.id
FROM (
    SELECT 'CTSP019' ma_chi_tiet, 'QR019' ma_qr, 36 so_luong, 1890000.00 gia_ban, 2190000.00 gia_goc, 'MS001' ma_mau_sac, 'KC007' ma_kich_co, 'SP009' ma_san_pham, 'HA009' ma_hinh_anh
    UNION ALL SELECT 'CTSP020', 'QR020', 32, 1990000.00, 2290000.00, 'MS003', 'KC008', 'SP010', 'HA010'
    UNION ALL SELECT 'CTSP021', 'QR021', 24, 1190000.00, 1490000.00, 'MS001', 'KC003', 'SP011', 'HA011'
    UNION ALL SELECT 'CTSP022', 'QR022', 42, 690000.00, 890000.00, 'MS008', 'KC006', 'SP012', 'HA012'
    UNION ALL SELECT 'CTSP023', 'QR023', 38, 590000.00, 790000.00, 'MS009', 'KC007', 'SP013', 'HA013'
    UNION ALL SELECT 'CTSP024', 'QR024', 28, 2190000.00, 2490000.00, 'MS008', 'KC008', 'SP014', 'HA014'
    UNION ALL SELECT 'CTSP025', 'QR025', 30, 2390000.00, 2790000.00, 'MS009', 'KC009', 'SP015', 'HA015'
    UNION ALL SELECT 'CTSP026', 'QR026', 22, 1890000.00, 2190000.00, 'MS001', 'KC007', 'SP016', 'HA016'
) seed
JOIN mau_sac ms ON ms.ma_mau_sac = seed.ma_mau_sac
JOIN kich_co kc ON kc.ma_kich_co = seed.ma_kich_co
JOIN san_pham sp ON sp.ma_san_pham = seed.ma_san_pham
JOIN hinh_anh ha ON ha.ma_hinh_anh = seed.ma_hinh_anh
WHERE NOT EXISTS (
    SELECT 1 FROM chi_tiet_san_pham ctsp WHERE ctsp.ma_chi_tiet = seed.ma_chi_tiet
);
