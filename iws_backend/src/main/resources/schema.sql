ALTER TABLE hoa_don
    MODIFY COLUMN email VARCHAR(100) NOT NULL;

UPDATE voucher
SET ngay_bat_dau = CASE
        WHEN ma_voucher = 'VC004' THEN '2024-06-01 00:00:00'
        ELSE '2024-01-01 00:00:00'
    END,
    ngay_ket_thuc = '2030-12-31 23:59:59',
    ngay_cap_nhat = NOW()
WHERE ma_voucher IN ('VC001', 'VC002', 'VC003', 'VC004', 'VC005', 'VC006');
