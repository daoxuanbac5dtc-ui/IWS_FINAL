<script>
import { computed, nextTick, onMounted, ref, watch } from 'vue';
const danhSachTinh = ref([]);
const danhSachXa = ref([]);
const loadingAddress = ref(false);
const voucherDaApDung = ref(null);
const stockReservations = ref(new Map()); // Map để theo dõi các reservation
const stockLocks = ref(new Map()); // Map để theo dõi các lock
const reservationTimeouts = ref(new Map()); // Map để theo dõi timeout
const RESERVATION_TIMEOUT = 5 * 60 * 1000; // 5 phút
const STOCK_CHECK_INTERVAL = 30 * 1000; // 30 giây check stock
const stockCheckInterval = ref(null);
const chiTietVoucherDaApDung = ref([]);
const loadingChiTietVoucher = ref(false);
const showVoucherDetailModal = ref(false);
// Load jsQR từ CDN
const loadJsQRFromCDN = () => {
    return new Promise((resolve, reject) => {
        if (window.jsQR && typeof window.jsQR === 'function') {
            console.log('jsQR already loaded');
            resolve(window.jsQR);
            return;
        }

        console.log('Loading jsQR from CDN...');
        const script = document.createElement('script');
        script.src = 'https://cdn.jsdelivr.net/npm/jsqr@1.4.0/dist/jsQR.js';

        script.onload = () => {
            console.log('jsQR CDN loaded successfully');
            if (window.jsQR && typeof window.jsQR === 'function') {
                resolve(window.jsQR);
            } else {
                reject(new Error('jsQR not found after loading'));
            }
        };

        script.onerror = (error) => {
            console.error('Failed to load jsQR from CDN:', error);
            reject(new Error('Failed to load jsQR library'));
        };

        document.head.appendChild(script);
    });
};

export default {
    name: 'BanHangTaiQuay',
    setup() {
        // =================== CONSTANTS ===================
        const API_BASE_URL = 'http://localhost:8080/api/ban-hang';
        // Voucher detail APIs live under /api/chi-tiet-voucher (not under /api/ban-hang)
        const API_VOUCHER_URL = 'http://localhost:8080/api/chi-tiet-voucher';

        // =================== REFS ===================
        const nhanVienInfo = ref({
            tenNhanVien: 'Nguyễn Văn A',
            maNhanVien: 'NV001'
        });

        // QR Scanner refs
        const qrStream = ref(null);
        const scanInterval = ref(null);
        const qrInput = ref(null);

        // Invoice & Products
        const hoaDonCho = ref([]);
        const hoaDonDangChon = ref(null);
        const danhSachSanPham = ref([]);
        const sanPhamDaChon = ref([]);
        const khachHang = ref(null);
        const voucher = ref(null);

        // UI States
        const loading = ref(false);
        const error = ref('');
        const searchKeyword = ref('');
        const customerSearchKeyword = ref('');
        const voucherCode = ref('');
        const soLuongChon = ref(1);
        const qrCode = ref('');

        // Pagination
        const trangHienTai = ref(0);
        const tongSoTrang = ref(0);
        const tongSoPhanTu = ref(0);

        // Modals
        const showCustomerModal = ref(false);
        const showVoucherModal = ref(false);
        const showPaymentModal = ref(false);
        const showProductDetail = ref(false);
        const showProductModal = ref(false);
        const showQrScanner = ref(false);
        const showCreateCustomerForm = ref(false);
        const showInvoicePrint = ref(false);
        const sanPhamDangXem = ref(null);

        // Loading states
        const loadingCustomers = ref(false);
        const loadingVouchers = ref(false);
        const loadingCreateCustomer = ref(false);
        const loadingVoucherCheck = ref(false);
        const loadingPayment = ref(false);
        const loadingDelete = ref(null);
        const loadingCreateInvoice = ref(false);

        // QR Scanner states
        const qrMode = ref('manual');
        const cameraError = ref('');
        const cameraStarted = ref(false);
        const loadingQR = ref(false);

        // Toast
        const showToast = ref(false);
        const toastMessage = ref('');
        const toastType = ref('success');

        // Filters
        const filters = ref({
            danhMucId: '',
            thuongHieuId: '',
            minPrice: '',
            maxPrice: ''
        });

        // Master data
        const danhSachDanhMuc = ref([]);
        const danhSachThuongHieu = ref([]);
        const danhSachKhachHang = ref([]);
        const danhSachVoucher = ref([]);

        // Forms
        const newCustomer = ref({
            hoTen: '',
            sdt: '',
            email: '',
            tinhId: '',
            xaId: '',
            diaChiChiTiet: '',
            diaChi: '' // Địa chỉ đầy đủ sẽ được tạo tự động
        });

        const newCustomerErrors = ref({});
        const layDanhSachTinh = async () => {
            try {
                loadingAddress.value = true;

                // Thử API mới trước
                const response = await fetch('https://addresskit.cas.so/latest/provinces', {
                    method: 'GET',
                    headers: {
                        Accept: 'application/json',
                        'Content-Type': 'application/json'
                    },
                    // Thêm timeout
                    signal: AbortSignal.timeout(10000) // 10 giây
                });

                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }

                const data = await response.json();

                if (data && Array.isArray(data)) {
                    danhSachTinh.value = data.map((item) => ({
                        id: item.id || item.code,
                        name: item.name || item.ten,
                        code: item.code || item.id
                    }));

                    console.log('✅ Tải danh sách tỉnh thành công (API mới):', danhSachTinh.value.length);
                    return;
                }

                throw new Error('Dữ liệu API không hợp lệ');
            } catch (error) {
                console.warn('⚠️ API mới thất bại, chuyển sang fallback:', error.message);

                // Fallback sang API cũ
                try {
                    const fallbackResponse = await fetch('https://provinces.open-api.vn/api/p/', {
                        method: 'GET',
                        headers: {
                            Accept: 'application/json'
                        },
                        signal: AbortSignal.timeout(8000)
                    });

                    if (!fallbackResponse.ok) {
                        throw new Error(`Fallback HTTP ${fallbackResponse.status}`);
                    }

                    const fallbackData = await fallbackResponse.json();

                    danhSachTinh.value = fallbackData.map((item) => ({
                        id: item.code,
                        name: item.name,
                        code: item.code
                    }));

                    console.log('✅ Fallback thành công:', danhSachTinh.value.length);
                } catch (fallbackError) {
                    console.error('❌ Tất cả API đều thất bại:', fallbackError);

                    // Sử dụng dữ liệu cứng trong trường hợp khẩn cấp
                    danhSachTinh.value = [
                        { id: '01', name: 'Hà Nội', code: '01' },
                        { id: '79', name: 'TP. Hồ Chí Minh', code: '79' },
                        { id: '48', name: 'Đà Nẵng', code: '48' },
                        { id: '92', name: 'Cần Thơ', code: '92' },
                        { id: '31', name: 'Hải Phòng', code: '31' }
                    ];

                    showToastMessage('Không thể tải danh sách tỉnh từ server. Sử dụng dữ liệu cơ bản.', 'warning');
                }
            } finally {
                loadingAddress.value = false;
            }
        };

        const layDanhSachXa = async (tinhId) => {
            if (!tinhId) {
                danhSachXa.value = [];
                newCustomer.value.xaId = '';
                return;
            }

            try {
                loadingAddress.value = true;

                // Validate tinhId
                if (typeof tinhId !== 'string' && typeof tinhId !== 'number') {
                    throw new Error('ID tỉnh không hợp lệ');
                }

                const response = await fetch(`https://addresskit.cas.so/latest/provinces/${tinhId}/communes`, {
                    method: 'GET',
                    headers: {
                        Accept: 'application/json',
                        'Content-Type': 'application/json'
                    },
                    signal: AbortSignal.timeout(10000)
                });

                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }

                const data = await response.json();

                if (data && Array.isArray(data)) {
                    danhSachXa.value = data.map((item) => ({
                        id: item.id || item.code,
                        name: item.name || item.ten,
                        code: item.code || item.id
                    }));

                    newCustomer.value.xaId = '';
                    console.log('✅ Tải danh sách xã thành công (API mới):', danhSachXa.value.length);
                    return;
                }

                throw new Error('Dữ liệu phường/xã không hợp lệ');
            } catch (error) {
                console.warn(`⚠️ Lỗi tải xã cho tỉnh ${tinhId}:`, error.message);

                // Fallback: thử API cũ
                try {
                    const fallbackResponse = await fetch(`https://provinces.open-api.vn/api/p/${tinhId}?depth=3`, {
                        signal: AbortSignal.timeout(8000)
                    });

                    if (!fallbackResponse.ok) {
                        throw new Error(`Fallback HTTP ${fallbackResponse.status}`);
                    }

                    const fallbackData = await fallbackResponse.json();

                    let allWards = [];
                    if (fallbackData.districts && Array.isArray(fallbackData.districts)) {
                        fallbackData.districts.forEach((district) => {
                            if (district.wards && Array.isArray(district.wards)) {
                                allWards = allWards.concat(
                                    district.wards.map((ward) => ({
                                        id: ward.code,
                                        name: ward.name,
                                        code: ward.code
                                    }))
                                );
                            }
                        });
                    }

                    danhSachXa.value = allWards;
                    console.log('✅ Fallback xã thành công:', danhSachXa.value.length);
                } catch (fallbackError) {
                    console.error('❌ Fallback xã cũng thất bại:', fallbackError);
                    danhSachXa.value = [];
                    showToastMessage('Không thể tải danh sách phường/xã cho tỉnh này', 'error');
                }

                newCustomer.value.xaId = '';
            } finally {
                loadingAddress.value = false;
            }
        };

        // ===== CHUYỂN ĐỔI ĐỊA CHỈ CŨ SANG MỚI =====
        const chuyenDoiDiaChiCuSangMoi = async (oldAddress) => {
            try {
                const response = await fetch('https://addresskit.cas.so/convert', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        address: oldAddress,
                        type: '3to2' // Convert from 3-level to 2-level
                    })
                });

                const data = await response.json();
                return data;
            } catch (error) {
                console.error('❌ Lỗi chuyển đổi địa chỉ:', error);
                return null;
            }
        };

        const layChiTietVoucherDaApDung = async (hoaDonId) => {
            if (!hoaDonId) return;

            loadingChiTietVoucher.value = true;
            try {
                const data = await apiCall(`${API_VOUCHER_URL}/hoa-don/${hoaDonId}`);
                if (data.success) {
                    chiTietVoucherDaApDung.value = data.data || [];
                    console.log('✅ Chi tiết voucher loaded:', chiTietVoucherDaApDung.value);
                } else {
                    chiTietVoucherDaApDung.value = [];
                }
            } catch (error) {
                console.error('❌ Lỗi lấy chi tiết voucher:', error);
                chiTietVoucherDaApDung.value = [];
                showToastMessage('Lỗi tải chi tiết voucher', 'error');
            } finally {
                loadingChiTietVoucher.value = false;
            }
        };

        // Xem chi tiết voucher đã áp dụng
        const xemChiTietVoucherDaApDung = async () => {
            if (!hoaDonDangChon.value?.id) {
                showToastMessage('Chưa chọn hóa đơn', 'warning');
                return;
            }

            await layChiTietVoucherDaApDung(hoaDonDangChon.value.id);
            showVoucherDetailModal.value = true;
        };

        // Lấy tất cả chi tiết voucher cho danh sách hóa đơn
        const layTatCaChiTietVoucher = async () => {
            try {
                const data = await apiCall(`${API_VOUCHER_URL}`);
                if (data.success) {
                    return data.data || [];
                }
                return [];
            } catch (error) {
                console.error('❌ Lỗi lấy tất cả chi tiết voucher:', error);
                return [];
            }
        };

        // Format thông tin voucher cho hiển thị
        const formatVoucherInfo = (voucherDetail) => {
            if (!voucherDetail) return {};

            return {
                tenVoucher: voucherDetail.tenVoucher || 'N/A',
                maVoucher: voucherDetail.maVoucher || 'N/A',
                loaiGiamGia: voucherDetail.loaiGiamGia,
                giaTriGiam: voucherDetail.giaTriGiam || 0,
                soTienGiam: voucherDetail.soTienGiam || 0,
                giaTriDonHang: voucherDetail.giaTriDonHang || 0,
                thanhTien: voucherDetail.thanhTien || 0,
                ngayApDung: voucherDetail.ngayApDung,
                phanTramGiam: voucherDetail.loaiGiamGia === 'PHAN_TRAM' ? voucherDetail.giaTriGiam : null,
                tienGiam: voucherDetail.loaiGiamGia === 'TIEN_MAT' ? voucherDetail.giaTriGiam : null
            };
        };

        // ===== CẬP NHẬT HÀM TẠO ĐỊA CHỈ ĐẦY ĐỦ =====
        const taoDialChiDayDu = () => {
            try {
                const tinh = danhSachTinh.value.find((t) => t.id == newCustomer.value.tinhId);
                const xa = danhSachXa.value.find((x) => x.id == newCustomer.value.xaId);

                let diaChiParts = [];

                // Thêm địa chỉ chi tiết nếu có
                const diaChiChiTiet = newCustomer.value.diaChiChiTiet || '';
                if (diaChiChiTiet.trim()) {
                    diaChiParts.push(diaChiChiTiet.trim());
                }

                // Thêm xã/phường nếu có
                if (xa && xa.name) {
                    diaChiParts.push(xa.name);
                }

                // Thêm tỉnh/thành phố
                if (tinh && tinh.name) {
                    diaChiParts.push(tinh.name);
                }

                // Tạo địa chỉ đầy đủ
                newCustomer.value.diaChi = diaChiParts.join(', ');
            } catch (error) {
                console.error('Lỗi tạo địa chỉ:', error);
                newCustomer.value.diaChi = '';
            }
        };

        // ===== CẬP NHẬT HÀM VALIDATION =====
        const validateNewCustomer = () => {
            const errors = {};

            // Kiểm tra họ tên
            const hoTen = newCustomer.value.hoTen || '';
            if (!hoTen.trim()) {
                errors.hoTen = 'Vui lòng nhập họ tên';
            } else if (hoTen.trim().length < 2) {
                errors.hoTen = 'Họ tên phải có ít nhất 2 ký tự';
            } else if (hoTen.trim().length > 100) {
                errors.hoTen = 'Họ tên không được quá 100 ký tự';
            }

            // Kiểm tra số điện thoại
            const sdt = newCustomer.value.sdt || '';
            if (!sdt.trim()) {
                errors.sdt = 'Vui lòng nhập số điện thoại';
            } else {
                const sdtClean = sdt.trim().replace(/\s+/g, '');
                if (!/^[0-9]{10,11}$/.test(sdtClean)) {
                    errors.sdt = 'Số điện thoại phải có 10-11 chữ số';
                } else if (!sdtClean.startsWith('0')) {
                    errors.sdt = 'Số điện thoại phải bắt đầu bằng số 0';
                }
            }

            // Kiểm tra email (nếu có)
            const email = newCustomer.value.email || '';
            if (email.trim()) {
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!emailRegex.test(email.trim())) {
                    errors.email = 'Email không đúng định dạng';
                } else if (email.trim().length > 255) {
                    errors.email = 'Email không được quá 255 ký tự';
                }
            }

            // Kiểm tra địa chỉ chi tiết (nếu có)
            const diaChiChiTiet = newCustomer.value.diaChiChiTiet || '';
            if (diaChiChiTiet.trim() && diaChiChiTiet.trim().length > 500) {
                errors.diaChiChiTiet = 'Địa chỉ chi tiết không được quá 500 ký tự';
            }

            newCustomerErrors.value = errors;
            return Object.keys(errors).length === 0;
        };
        // Payment
        const thongTinThanhToan = ref({
            tienMat: 0,
            tienChuyenKhoan: 0, // Đổi tên từ chuyenKhoan
            ghiChu: '',
            diemSuDung: 0,
            phuongThucThanhToan: 'TIEN_MAT' // Thêm field mới
        });
        const quickAmounts = ref([1000000, 2000000, 3000000, 5000000, 10000000]);
        const phuongThucThanhToanHienTai = computed(() => {
            const tienMat = Number(thongTinThanhToan.value.tienMat) || 0;
            const tienCK = Number(thongTinThanhToan.value.tienChuyenKhoan) || 0;

            if (tienMat > 0 && tienCK > 0) {
                return 'KET_HOP';
            } else if (tienMat > 0) {
                return 'TIEN_MAT';
            } else if (tienCK > 0) {
                return 'CHUYEN_KHOAN';
            }
            return 'TIEN_MAT';
        });

        // ===== CẬP NHẬT HÀM VALIDATE THANH TOÁN =====
        const validateThanhToan = () => {
            const errors = [];
            const tienMat = Number(thongTinThanhToan.value.tienMat) || 0;
            const tienCK = Number(thongTinThanhToan.value.tienChuyenKhoan) || 0;
            const tongCanThanhToan = tinhTongThanhToan();

            // Validate theo phương thức
            const phuongThuc = phuongThucThanhToanHienTai.value;

            switch (phuongThuc) {
                case 'TIEN_MAT':
                    if (tienMat <= 0) {
                        errors.push('Số tiền mặt phải lớn hơn 0');
                    }
                    if (tienMat < tongCanThanhToan) {
                        errors.push('Số tiền mặt không đủ để thanh toán');
                    }
                    break;

                case 'CHUYEN_KHOAN':
                    if (tienCK <= 0) {
                        errors.push('Số tiền chuyển khoản phải lớn hơn 0');
                    }
                    if (tienCK < tongCanThanhToan) {
                        errors.push('Số tiền chuyển khoản không đủ để thanh toán');
                    }
                    break;

                case 'KET_HOP':
                    if (tienMat <= 0 && tienCK <= 0) {
                        errors.push('Phải có ít nhất một phương thức thanh toán có giá trị > 0');
                    }
                    if (tienMat + tienCK < tongCanThanhToan) {
                        errors.push('Tổng tiền thanh toán không đủ');
                    }
                    break;
            }

            return errors;
        };

        // ===== CẬP NHẬT HÀM TÍNH TIỀN THỪA =====
        const tinhTienThua = () => {
            const tongNhan = (Number(thongTinThanhToan.value.tienMat) || 0) + (Number(thongTinThanhToan.value.tienChuyenKhoan) || 0);
            const tongCanThanhToan = tinhTongThanhToan();
            return tongNhan - tongCanThanhToan;
        };

        // ===== CẬP NHẬT HÀM CHỌN TIỀN NHANH =====
        const chonTienNhanh = (amount) => {
            thongTinThanhToan.value.tienMat = amount;
            thongTinThanhToan.value.tienChuyenKhoan = 0;
            thongTinThanhToan.value.phuongThucThanhToan = 'TIEN_MAT';
        };

        const chonTienVuaVua = () => {
            const tongCanThanhToan = tinhTongThanhToan();
            thongTinThanhToan.value.tienMat = tongCanThanhToan;
            thongTinThanhToan.value.tienChuyenKhoan = 0;
            thongTinThanhToan.value.phuongThucThanhToan = 'TIEN_MAT';
        };

        // ===== CẬP NHẬT HÀM CHỌN CHUYỂN KHOẢN =====
        const chonChuyenKhoanDayDu = () => {
            const tongCanThanhToan = tinhTongThanhToan();
            thongTinThanhToan.value.tienMat = 0;
            thongTinThanhToan.value.tienChuyenKhoan = tongCanThanhToan;
            thongTinThanhToan.value.phuongThucThanhToan = 'CHUYEN_KHOAN';
        };

        // ===== KIỂM TRA TỒN KHO TRƯỚC KHI THANH TOÁN =====
        const kiemTraTonKhoTruocThanhToan = async () => {
            try {
                if (!hoaDonDangChon.value?.id) return { ok: false, conflicts: [] };

                // Gọi API kiểm tra tồn kho mới nhất trước khi thanh toán
                const res = await apiCall(`${API_BASE_URL}/hoa-don-cho/${hoaDonDangChon.value.id}/kiem-tra-ton-kho`);

                if (!res || !Array.isArray(res)) {
                    // Một số backend có thể trả object; fallback đọc res.data nếu có
                    const list = Array.isArray(res?.data) ? res.data : [];
                    const conflicts = list.filter((i) => i && i.coTheban === false);
                    return { ok: conflicts.length === 0, conflicts };
                }

                const conflicts = res.filter((i) => i && i.coTheban === false);
                return { ok: conflicts.length === 0, conflicts };
            } catch (err) {
                console.warn('Không thể kiểm tra tồn kho trước thanh toán:', err);
                // Thận trọng: nếu lỗi mạng, để người dùng thử lại thay vì tiếp tục thanh toán mù
                return { ok: false, conflicts: [], error: err };
            }
        };

        // ===== CẬP NHẬT HÀM XỬ LÝ THANH TOÁN =====
        const xuLyThanhToan = async () => {
            if (loadingPayment.value) return;

            try {
                if (!hoaDonDangChon.value?.id) {
                    showToastMessage('Chưa chọn hóa đơn để thanh toán', 'error');
                    return;
                }

                // 1) Kiểm tra tồn kho lần cuối trước khi gửi thanh toán
                const check = await kiemTraTonKhoTruocThanhToan();
                if (!check.ok) {
                    // Làm tươi lại chi tiết hóa đơn để đồng bộ UI với tồn kho thực tế
                    try {
                        await layDanhSachHoaDonCho?.();
                        // Nếu có API lấy chi tiết một hóa đơn: có thể gọi lại để sync panel phải
                        // await layChiTietHoaDonCho(hoaDonDangChon.value.id);
                    } catch {}

                    if (check.conflicts && check.conflicts.length > 0) {
                        const tenSp = check.conflicts.map(c => `${c.tenSanPham || 'Sản phẩm'} (${c.kichCo || ''} ${c.mauSac || ''})`).join(', ');
                        showToastMessage(`Một số sản phẩm không đủ tồn kho: ${tenSp}. Vui lòng cập nhật hóa đơn trước khi thanh toán.`, 'error');
                    } else {
                        showToastMessage('Không thể kiểm tra tồn kho. Vui lòng thử lại.', 'error');
                    }
                    return;
                }

                // Validate trước khi gửi
                const errors = validateThanhToan();
                if (errors.length > 0) {
                    showToastMessage(errors[0], 'error');
                    return;
                }

                loadingPayment.value = true;

                // ✅ Tính tổng cần thanh toán (không dùng điểm tích lũy)
                const tongTienCanThanhToan = tinhTongThanhToan(); // Đã bao gồm voucher, KHÔNG trừ điểm

                // Lấy thông tin chi tiết để gửi lên server
                const tienGiamVoucher = tongQuan.value.tongTienVoucher;
                // Bỏ tính điểm
                const diemSuDung = 0;
                const giaTriDiem = 0;

                // Chuẩn bị dữ liệu cho API - GỬI ĐẦY ĐỦ THÔNG TIN ĐỂ BACKEND TỰ TÍNH
                const requestData = {
                    phuongThucThanhToan: phuongThucThanhToanHienTai.value,
                    loaiHoaDon: 'OFFLINE',
                    ghiChu: String(thongTinThanhToan.value.ghiChu || '').trim(),
                    // ✅ THÊM CÁC FIELD TÍNH TOÁN
                    tongTienHang: tongQuan.value.tongTienKhuyenMai, // 2.5 triệu
                    tienGiamVoucher: tienGiamVoucher, // 200k
                    tongSauVoucher: tongQuan.value.tongTienThanhToan, // 2.3 triệu
                    tongThanhToan: tongTienCanThanhToan // 2.3 triệu (sau khi trừ điểm)
                };

                // Thêm số tiền theo phương thức
                if (phuongThucThanhToanHienTai.value === 'TIEN_MAT') {
                    requestData.tienMat = Number(thongTinThanhToan.value.tienMat);
                } else if (phuongThucThanhToanHienTai.value === 'CHUYEN_KHOAN') {
                    requestData.tienChuyenKhoan = Number(thongTinThanhToan.value.tienChuyenKhoan);
                } else if (phuongThucThanhToanHienTai.value === 'KET_HOP') {
                    requestData.tienMat = Number(thongTinThanhToan.value.tienMat);
                    requestData.tienChuyenKhoan = Number(thongTinThanhToan.value.tienChuyenKhoan);
                }

                // Thêm thông tin khách hàng và voucher (bỏ điểm)
                if (khachHang.value?.id) {
                    requestData.khachHangId = Number(khachHang.value.id);
                }

                if (voucher.value?.id) {
                    requestData.voucherId = Number(voucher.value.id);
                    requestData.tienGiamVoucher = tienGiamVoucher; // ✅ Gửi số tiền giảm voucher
                }

                // Không gửi thông tin điểm

                console.log('🔍 Debug payment calculation:');
                console.log('- Tổng tiền hàng:', tongQuan.value.tongTienKhuyenMai);
                console.log('- Tiền giảm voucher:', tienGiamVoucher);
                console.log('- Tổng sau voucher:', tongQuan.value.tongTienThanhToan);
                // Bỏ log điểm sử dụng
                console.log('- Tổng cần thanh toán:', tongTienCanThanhToan);
                console.log('🔍 Request data with voucher info:', requestData);

                // Gọi API thanh toán chi tiết
                const data = await apiCall(`${API_BASE_URL}/hoa-don-cho/${hoaDonDangChon.value.id}/thanh-toan-chi-tiet`, {
                    method: 'POST',
                    body: JSON.stringify(requestData)
                });

                if (data.success) {
                    const responseData = data.data;

                    // Lưu thông tin cho in hóa đơn
                    hoaDonDaThanhToan.value = responseData;
                    sanPhamDaThanhToan.value = [...sanPhamDaChon.value];

                    // ✅ LƯU TỔNG QUAN VỚI SỐ TIỀN ĐÚNG
                    tongQuanDaThanhToan.value = {
                        ...tongQuan.value,
                        tongTienThanhToan: tongTienCanThanhToan // Số tiền đã trừ voucher
                    };

                    voucherDaThanhToan.value = voucher.value ? { ...voucher.value } : null;

                    // Lưu thông tin thanh toán chi tiết
                    thongTinThanhToanCuoi.value = {
                        phuongThucThanhToan: responseData.phuongThucThanhToan,
                        tienMat: responseData.tienMat || 0,
                        tienChuyenKhoan: responseData.tienChuyenKhoan || 0,
                        tienThua: responseData.tienThua || 0,
                        diemSuDung: 0,
                        giaTriDiem: 0,
                        voucherInfo: responseData.tenVoucher
                            ? {
                                  tenVoucher: responseData.tenVoucher,
                                  giaTriGiam: responseData.giaTriGiamVoucher || tienGiamVoucher
                              }
                            : null,
                        khachHangInfo: {
                            id: responseData.khachHangId,
                            hoTen: responseData.tenKhachHang || 'Khách lẻ',
                            sdt: responseData.sdtKhachHang || 'N/A'
                        },
                        thongBao: responseData.thongBaoThanhToan
                    };

                    showPaymentModal.value = false;

                    // Reset form
                    thongTinThanhToan.value = {
                        tienMat: 0,
                        tienChuyenKhoan: 0,
                        ghiChu: '',
                        diemSuDung: 0,
                        phuongThucThanhToan: 'TIEN_MAT'
                    };

                    voucher.value = null;
                    await layDanhSachHoaDonCho();

                    // Hiển thị thông báo từ API
                    showToastMessage(responseData.thongBaoThanhToan || 'Thanh toán thành công!');
                    showInvoicePrint.value = true;
                } else {
                    showToastMessage(data.message || 'Thanh toán thất bại', 'error');
                }
            } catch (error) {
                console.error('Lỗi xử lý thanh toán:', error);
                let errorMessage = 'Lỗi xử lý thanh toán';
                if (error.message && error.message.includes('HTTP 400')) {
                    errorMessage = 'Dữ liệu thanh toán không hợp lệ';
                } else if (error.message && error.message.includes('HTTP 404')) {
                    errorMessage = 'Không tìm thấy hóa đơn';
                } else if (error.message && error.message.includes('HTTP 500')) {
                    errorMessage = 'Lỗi hệ thống';
                }
                showToastMessage(errorMessage, 'error');
            } finally {
                loadingPayment.value = false;
            }
        };

        // ===== THÊM CÁC HÀM TIỆN ÍCH =====
        const formatPhuongThucThanhToan = (phuongThuc) => {
            const map = {
                TIEN_MAT: 'Tiền mặt',
                CHUYEN_KHOAN: 'Chuyển khoản',
                KET_HOP: 'Kết hợp'
            };
            return map[phuongThuc] || phuongThuc;
        };

        const getTienThuaDisplay = () => {
            const tienThua = tinhTienThua();
            if (tienThua > 0) {
                return `Tiền thừa: ${formatPrice(tienThua)}`;
            } else if (tienThua < 0) {
                return `Còn thiếu: ${formatPrice(Math.abs(tienThua))}`;
            }
            return 'Vừa vặn';
        };
        // Invoice print data
        const hoaDonDaThanhToan = ref(null);
        const sanPhamDaThanhToan = ref([]);
        const tongQuanDaThanhToan = ref({});
        const voucherDaThanhToan = ref(null);
        const thongTinThanhToanCuoi = ref({});

        // =================== COMPUTED ===================
        const tongQuan = computed(() => {
            if (!sanPhamDaChon.value.length) {
                return {
                    soLuongSanPham: 0,
                    tongSoLuong: 0,
                    tongTienGoc: 0,
                    tongTienKhuyenMai: 0,
                    tongTienVoucher: 0,
                    tongTienThanhToan: 0
                };
            }

            const soLuongSanPham = sanPhamDaChon.value.length;
            const tongSoLuong = sanPhamDaChon.value.reduce((sum, item) => {
                return sum + (Number(item.soLuongDaChon) || 0);
            }, 0);

            const tongTienGoc = sanPhamDaChon.value.reduce((sum, item) => {
                const soLuong = Number(item.soLuongDaChon) || 0;
                const gia = Number(item.giaGoc || item.giaBan) || 0;
                return sum + gia * soLuong;
            }, 0);

            const tongTienKhuyenMai = sanPhamDaChon.value.reduce((sum, item) => {
                const soLuong = Number(item.soLuongDaChon) || 0;
                const gia = Number(item.giaBan) || 0;
                return sum + gia * soLuong;
            }, 0);

            // ✅ Sử dụng voucher state hiện tại (bao gồm voucher đã áp dụng)
            let tongTienVoucher = 0;
            const currentVoucher = voucher.value || voucherDaApDung.value;

            if (currentVoucher) {
                if (currentVoucher.loaiGiamGia === 'PHAN_TRAM') {
                    tongTienVoucher = Math.round((tongTienKhuyenMai * (Number(currentVoucher.giaTriGiam) || 0)) / 100);
                    // Kiểm tra giới hạn tối đa
                    if (currentVoucher.giaTriGiamToiDa && tongTienVoucher > currentVoucher.giaTriGiamToiDa) {
                        tongTienVoucher = currentVoucher.giaTriGiamToiDa;
                    }
                } else {
                    tongTienVoucher = Number(currentVoucher.giaTriGiam) || 0;
                }

                // Đảm bảo voucher không vượt quá tổng tiền
                tongTienVoucher = Math.min(tongTienVoucher, tongTienKhuyenMai);
            }

            const tongTienThanhToan = Math.max(0, tongTienKhuyenMai - tongTienVoucher);

            return {
                soLuongSanPham,
                tongSoLuong,
                tongTienGoc,
                tongTienKhuyenMai,
                tongTienVoucher,
                tongTienThanhToan
            };
        });

        // =================== UTILITY FUNCTIONS ===================
        const showToastMessage = (message, type = 'success') => {
            toastMessage.value = message;
            toastType.value = type;
            showToast.value = true;
            setTimeout(() => (showToast.value = false), 3000);
        };

        const formatPrice = (price) => {
            if (!price && price !== 0) return '0₫';
            return new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND'
            }).format(price);
        };

        const getDiscountPercentage = (product) => {
            if (!product) return null;

            const giaGoc = Number(product.giaGoc) || 0;
            const giaBan = Number(product.giaBan) || 0;

            // Nếu không có giá gốc hoặc giá bán = giá gốc thì không có giảm giá
            if (!giaGoc || giaBan >= giaGoc) {
                return null;
            }

            // Tính % giảm giá
            const discountPercentage = Math.round(((giaGoc - giaBan) / giaGoc) * 100);

            // Chỉ hiển thị nếu giảm giá > 0%
            return discountPercentage > 0 ? discountPercentage : null;
        };

        const formatDate = (dateString) => {
            if (!dateString) return 'N/A';
            try {
                const date = new Date(dateString);
                return date.toLocaleDateString('vi-VN');
            } catch {
                return 'N/A';
            }
        };

        const formatDateTime = (date) => {
            return date.toLocaleString('vi-VN', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
            });
        };

        const getMauHex = (tenMau) => {
            if (!tenMau) return '#6c757d';
            const colorMap = {
                đỏ: '#dc3545',
                red: '#dc3545',
                xanh: '#0066cc',
                blue: '#0066cc',
                đen: '#000000',
                black: '#000000',
                trắng: '#ffffff',
                white: '#ffffff',
                vàng: '#ffc107',
                yellow: '#ffc107',
                'xanh lá': '#28a745',
                green: '#28a745',
                tím: '#6f42c1',
                purple: '#6f42c1',
                hồng: '#e83e8c',
                pink: '#e83e8c',
                nâu: '#8b4513',
                brown: '#8b4513',
                cam: '#fd7e14',
                orange: '#fd7e14',
                xám: '#6c757d',
                gray: '#6c757d'
            };
            return colorMap[tenMau.toLowerCase()] || '#6c757d';
        };

        const getProductImage = (product) => {
            if (product?.hinhAnhChinh && product.hinhAnhChinh !== 'null') {
                return product.hinhAnhChinh.startsWith('http') ? product.hinhAnhChinh : `http://localhost:8080${product.hinhAnhChinh}`;
            }
            return 'https://via.placeholder.com/200x200?text=No+Image';
        };

        const handleImageError = (event) => {
            event.target.src =
                'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2Y4ZjlmYSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjE0IiBmaWxsPSIjNmM3NTdkIj5LaW9uZyBjbyBhbmg8L3RleHQ+PC9zdmc+';
        };

        // =================== API FUNCTIONS ===================
        const apiCall = async (url, options = {}) => {
            try {
                const token = localStorage.getItem('auth_token') || localStorage.getItem('token');
                const headers = {
                    'Content-Type': 'application/json',
                    ...(token && { Authorization: `Bearer ${token}` })
                };

                const response = await fetch(url, {
                    method: options.method || 'GET',
                    ...options,
                    headers: {
                        ...headers,
                        ...(options.headers || {})
                    }
                });

                const contentType = response.headers.get('content-type') || '';
                let payload;
                try {
                    if (contentType.includes('application/json')) {
                        payload = await response.json();
                    } else {
                        payload = await response.text();
                    }
                } catch (_) {
                    // Nếu parse body thất bại, để payload = undefined
                }

                if (!response.ok) {
                    const detail = typeof payload === 'string' ? payload : (payload?.message || payload?.error || JSON.stringify(payload || {}));
                    const err = new Error(`HTTP ${response.status}: ${response.statusText}${detail ? ` - ${detail}` : ''}`);
                    // Gắn thêm thông tin để caller có thể dùng nếu cần
                    err.status = response.status;
                    err.statusText = response.statusText;
                    err.payload = payload;
                    throw err;
                }

                return payload;
            } catch (error) {
                console.error('API Error:', error);
                throw error;
            }
        };

        // =================== EMPLOYEE FUNCTIONS ===================
        const loadEmployeeInfo = () => {
            try {
                const userInfo = localStorage.getItem('user_info');
                if (userInfo) {
                    const user = JSON.parse(userInfo);
                    nhanVienInfo.value = {
                        tenNhanVien: user.tenNhanVien || user.hoTen || 'Nhân viên',
                        maNhanVien: user.maNhanVien || user.id || 'NV001'
                    };
                }
            } catch (error) {
                console.error('Error loading employee info:', error);
            }
        };

        // =================== INVOICE FUNCTIONS ===================
        const layDanhSachHoaDonCho = async () => {
            try {
                const data = await apiCall(`${API_BASE_URL}/hoa-don-cho`);
                if (data.success) {
                    hoaDonCho.value = data.data;
                    if (data.data.length > 0 && !hoaDonDangChon.value) {
                        chonHoaDon(data.data[0]);
                    }
                }
            } catch (error) {
                showToastMessage('Lỗi tải danh sách hóa đơn', 'error');
            }
        };

        const chonHoaDon = async (hoaDon) => {
            resetVoucherStateWhenChangeInvoice();
            hoaDonDangChon.value = hoaDon;

            // Load thông tin hóa đơn
            await layTongQuanHoaDon(hoaDon.id);

            // Load chi tiết voucher nếu có
            if (voucher.value) {
                await layChiTietVoucherDaApDung(hoaDon.id);
            }
        };
        const layTongQuanHoaDon = async (hoaDonId) => {
            try {
                console.log('🔄 Loading invoice summary for ID:', hoaDonId);

                const data = await apiCall(`${API_BASE_URL}/hoa-don-cho/${hoaDonId}/tong-quan`);

                if (data.success && data.data) {
                    const tongQuanData = data.data;

                    // Cập nhật sản phẩm
                    if (tongQuanData.danhSachSanPham && Array.isArray(tongQuanData.danhSachSanPham)) {
                        sanPhamDaChon.value = tongQuanData.danhSachSanPham.map((item, index) => ({
                            id: item.id || `temp_${Date.now()}_${index}`,
                            chiTietSanPhamId: item.chiTietSanPhamId || item.id,
                            tenSanPham: item.tenSanPham || 'Sản phẩm không xác định',
                            maSanPham: item.maSanPham || 'SP' + item.chiTietSanPhamId,
                            // số lượng đã chọn trong hóa đơn
                            soLuongDaChon: Number(item.soLuong) || 1,
                            giaBan: Number(item.giaBan) || 0,
                            giaGoc: Number(item.giaGoc) || Number(item.giaBan) || 0,
                            // tồn kho hiện tại (lấy theo nhiều tên trường khả dĩ từ backend). Nếu không có, đặt null để biểu thị 'không rõ'.
                            // Tránh mặc định 0 vì sẽ khiến nút tăng số lượng bị disable dù thực tế còn hàng.
                            soLuong: (() => {
                                const raw = item.soLuongTon ?? item.soLuongTonKho ?? item.tonKho ?? item.soLuongConLai ?? item.soLuongHienTai ?? item.stock;
                                return raw != null ? Number(raw) : null;
                            })(),
                            mauSac: {
                                tenMau: item.mauSac || 'N/A',
                                maMau: getMauHex(item.mauSac)
                            },
                            kichCo: {
                                tenKichCo: item.kichCo || 'N/A'
                            },
                            thuongHieu: {
                                tenThuongHieu: item.thuongHieu || 'N/A'
                            },
                            hinhAnhChinh: item.hinhAnhChinh || null
                        }));
                    } else {
                        sanPhamDaChon.value = [];
                    }

                    // Cập nhật khách hàng
                    khachHang.value = tongQuanData.khachHang || null;

                    // ✅ WORKAROUND: Chỉ cập nhật voucher nếu backend có trả về voucher
                    // Nếu backend không trả về voucher, giữ nguyên voucher đã áp dụng
                    if (tongQuanData.voucher) {
                        voucher.value = tongQuanData.voucher;
                        voucherDaApDung.value = tongQuanData.voucher;
                        console.log('✅ Voucher updated from API:', voucher.value);
                    } else if (voucherDaApDung.value) {
                        // Giữ voucher đã áp dụng trước đó
                        voucher.value = voucherDaApDung.value;
                        console.log('💡 Keeping previously applied voucher:', voucher.value.tenVoucher);
                    } else {
                        // Chỉ clear khi thực sự không có voucher
                        voucher.value = null;
                        voucherDaApDung.value = null;
                        console.log('🗑️ No voucher applied');
                    }
                } else {
                    sanPhamDaChon.value = [];
                }
            } catch (error) {
                console.error('❌ Error loading invoice summary:', error);
                sanPhamDaChon.value = [];
                showToastMessage('Lỗi tải thông tin hóa đơn', 'error');
            }
        };

        const kiemTraDongBoVoucher = async () => {
            if (!hoaDonDangChon.value?.id) return;

            try {
                console.log('🔄 Checking voucher sync...');
                const data = await apiCall(`${API_BASE_URL}/hoa-don-cho/${hoaDonDangChon.value.id}/voucher-status`);

                if (data.success) {
                    const voucherStatus = data.data;
                    if (voucherStatus.hasVoucher && voucherStatus.voucher) {
                        voucher.value = voucherStatus.voucher;
                        console.log('✅ Voucher synced:', voucher.value);
                    } else {
                        voucher.value = null;
                        console.log('✅ Confirmed no voucher');
                    }
                }
            } catch (error) {
                console.warn('⚠️ Could not check voucher sync:', error.message);
                // Không làm gì, giữ nguyên state hiện tại
            }
        };
        const taoHoaDonMoi = async () => {
            if (loadingCreateInvoice.value) return;
            loadingCreateInvoice.value = true;

            try {
                const nhanVienId = nhanVienInfo.value.maNhanVien || 1;
                const data = await apiCall(`${API_BASE_URL}/hoa-don-cho/tao-moi?nhanVienId=${nhanVienId}`, {
                    method: 'POST'
                });

                if (data.success) {
                    await layDanhSachHoaDonCho();
                    showToastMessage('Tạo hóa đơn mới thành công!');

                    const newInvoice = hoaDonCho.value[hoaDonCho.value.length - 1];
                    if (newInvoice) {
                        await chonHoaDon(newInvoice);
                    }
                } else {
                    showToastMessage(data.message || 'Lỗi tạo hóa đơn', 'error');
                }
            } catch (error) {
                showToastMessage(`Lỗi tạo hóa đơn: ${error.message}`, 'error');
            } finally {
                loadingCreateInvoice.value = false;
            }
        };

        const confirmDeleteHoaDon = async (hoaDon) => {
            const index = hoaDonCho.value.findIndex((hd) => hd.id === hoaDon.id) + 1;
            if (!confirm(`Bạn có chắc muốn xóa hóa đơn ${index}?`)) return;

            loadingDelete.value = hoaDon.id;
            try {
                const data = await apiCall(`${API_BASE_URL}/hoa-don-cho/${hoaDon.id}`, {
                    method: 'DELETE'
                });

                if (data.success) {
                    if (hoaDonDangChon.value?.id === hoaDon.id) {
                        const otherInvoice = hoaDonCho.value.find((hd) => hd.id !== hoaDon.id);
                        if (otherInvoice) {
                            hoaDonDangChon.value = otherInvoice;
                        }
                    }
                    await layDanhSachHoaDonCho();
                    showToastMessage('Đã xóa hóa đơn thành công!');
                } else {
                    showToastMessage(data.message || 'Lỗi xóa hóa đơn', 'error');
                }
            } catch (error) {
                showToastMessage(`Lỗi xóa hóa đơn: ${error.message}`, 'error');
                await layDanhSachHoaDonCho();
            } finally {
                loadingDelete.value = null;
            }
        };

        // =================== PRODUCT FUNCTIONS ===================
        const loadMasterData = async () => {
            try {
                const [danhMucData, thuongHieuData] = await Promise.all([
                    apiCall(`${API_BASE_URL}/master-data/danh-muc`).catch(() => ({ success: false, data: [] })),
                    apiCall(`${API_BASE_URL}/master-data/thuong-hieu`).catch(() => ({ success: false, data: [] }))
                ]);

                if (danhMucData.success) danhSachDanhMuc.value = danhMucData.data;
                if (thuongHieuData.success) danhSachThuongHieu.value = thuongHieuData.data;
            } catch (error) {
                console.error('Lỗi load master data:', error);
            }
        };

        const timKiemSanPham = async (page = 0) => {
            loading.value = true;
            error.value = '';

            try {
                const params = new URLSearchParams({
                    keyword: searchKeyword.value || '',
                    page: page.toString(),
                    size: '20',
                    sortBy: 'ngayTao',
                    sortDir: 'desc'
                });

                if (filters.value.danhMucId) params.append('danhMucId', filters.value.danhMucId);
                if (filters.value.thuongHieuId) params.append('thuongHieuId', filters.value.thuongHieuId);
                if (filters.value.minPrice) params.append('minPrice', filters.value.minPrice);
                if (filters.value.maxPrice) params.append('maxPrice', filters.value.maxPrice);

                const url = `${API_BASE_URL}/san-pham?${params}`;
                const data = await apiCall(url);

                if (data.success) {
                    danhSachSanPham.value = data.data || [];
                    trangHienTai.value = data.currentPage || 0;
                    tongSoTrang.value = data.totalPages || 0;
                    tongSoPhanTu.value = data.totalElements || 0;
                } else {
                    error.value = data.message || 'Có lỗi xảy ra khi tải sản phẩm';
                    danhSachSanPham.value = [];
                }
            } catch (err) {
                error.value = 'Không thể kết nối đến server';
                danhSachSanPham.value = [];
                showToastMessage(`Lỗi tải sản phẩm: ${err.message}`, 'error');
            } finally {
                loading.value = false;
            }
        };

        const debounceSearch = () => {
            if (window.searchTimer) clearTimeout(window.searchTimer);
            window.searchTimer = setTimeout(() => timKiemSanPham(0), 500);
        };

        const debouncePriceFilter = () => {
            if (window.priceFilterTimer) clearTimeout(window.priceFilterTimer);
            window.priceFilterTimer = setTimeout(() => timKiemSanPham(0), 800);
        };

        const applyFilters = () => {
            timKiemSanPham(0);
        };

        const chuyenTrang = (page) => {
            if (page >= 0 && page < tongSoTrang.value) {
                timKiemSanPham(page);
            }
        };

        const xemChiTietSanPham = (product) => {
            sanPhamDangXem.value = product;
            soLuongChon.value = 1;
            showProductDetail.value = true;
        };

        const themVaoHoaDon = async (product, soLuong = 1) => {
            if (!hoaDonDangChon.value) {
                showToastMessage('Vui lòng chọn hóa đơn trước', 'error');
                return;
            }

            if (!product || (!product.id && !product.chiTietSanPhamId)) {
                showToastMessage('Thông tin sản phẩm không hợp lệ', 'error');
                return;
            }

            try {
                const productId = product.chiTietSanPhamId || product.id;
                const request = {
                    chiTietSanPhamId: Number(productId),
                    soLuong: Number(soLuong),
                    donGia: Number(product.giaBan) || 0
                };

                const data = await apiCall(`${API_BASE_URL}/hoa-don-cho/${hoaDonDangChon.value.id}/them-san-pham`, {
                    method: 'POST',
                    body: JSON.stringify(request)
                });

                if (data.success) {
                    showToastMessage(`Đã thêm ${product.tenSanPham} vào hóa đơn!`);
                    setTimeout(() => {
                        layTongQuanHoaDon(hoaDonDangChon.value.id);
                    }, 500);
                } else {
                    showToastMessage(data.message || 'Lỗi thêm sản phẩm', 'error');
                }
            } catch (error) {
                showToastMessage(`Lỗi thêm sản phẩm: ${error.message}`, 'error');
            }
        };

        const themVaoHoaDonTuModal = () => {
            themVaoHoaDon(sanPhamDangXem.value, soLuongChon.value);
            showProductDetail.value = false;
        };

        const tangSoLuong = async (item) => {
            if (!item || !item.id || !hoaDonDangChon.value?.id) return;

            const soLuongHienTai = Number(item.soLuongDaChon) || 0;
            try {
                const soLuongMoi = soLuongHienTai + 1;
                const request = {
                    soLuong: soLuongMoi,
                    donGia: Number(item.giaBan) || 0
                };

                const data = await apiCall(`${API_BASE_URL}/hoa-don-cho/${hoaDonDangChon.value.id}/cap-nhat-san-pham/${item.id}`, {
                    method: 'PUT',
                    body: JSON.stringify(request)
                });

                if (data.success) {
                    const index = sanPhamDaChon.value.findIndex((sp) => sp.id === item.id);
                    if (index > -1) {
                        sanPhamDaChon.value[index].soLuongDaChon = soLuongMoi;
                    }
                    setTimeout(() => layTongQuanHoaDon(hoaDonDangChon.value.id), 300);
                } else {
                    showToastMessage(data.message || 'Lỗi cập nhật số lượng', 'error');
                }
            } catch (error) {
                showToastMessage(`Lỗi cập nhật: ${error.message}`, 'error');
            }
        };

        const giamSoLuong = async (item) => {
            if (!item || !item.id || !hoaDonDangChon.value?.id) return;

            const soLuongHienTai = Number(item.soLuongDaChon) || 0;
            if (soLuongHienTai <= 1) {
                xoaKhoiGioHang(item);
                return;
            }

            try {
                const soLuongMoi = soLuongHienTai - 1;
                const request = {
                    soLuong: soLuongMoi,
                    donGia: Number(item.giaBan) || 0
                };

                const data = await apiCall(`${API_BASE_URL}/hoa-don-cho/${hoaDonDangChon.value.id}/cap-nhat-san-pham/${item.id}`, {
                    method: 'PUT',
                    body: JSON.stringify(request)
                });

                if (data.success) {
                    const index = sanPhamDaChon.value.findIndex((sp) => sp.id === item.id);
                    if (index > -1) {
                        sanPhamDaChon.value[index].soLuongDaChon = soLuongMoi;
                    }
                    setTimeout(() => layTongQuanHoaDon(hoaDonDangChon.value.id), 300);
                } else {
                    showToastMessage(data.message || 'Lỗi cập nhật số lượng', 'error');
                }
            } catch (error) {
                showToastMessage(`Lỗi cập nhật: ${error.message}`, 'error');
            }
        };

        const xoaKhoiGioHang = async (item) => {
            if (!item || !item.id || !hoaDonDangChon.value?.id) return;

            const tenSanPham = item.tenSanPham || 'sản phẩm này';
            if (!confirm(`Bạn có chắc muốn xóa "${tenSanPham}" khỏi hóa đơn?`)) {
                return;
            }

            try {
                const data = await apiCall(`${API_BASE_URL}/hoa-don-cho/${hoaDonDangChon.value.id}/xoa-san-pham/${item.id}`, {
                    method: 'DELETE'
                });

                if (data.success) {
                    const index = sanPhamDaChon.value.findIndex((sp) => sp.id === item.id);
                    if (index > -1) {
                        sanPhamDaChon.value.splice(index, 1);
                    }
                    showToastMessage(`Đã xóa ${tenSanPham} khỏi hóa đơn`);
                    setTimeout(() => layTongQuanHoaDon(hoaDonDangChon.value.id), 300);
                } else {
                    showToastMessage(data.message || 'Lỗi xóa sản phẩm', 'error');
                }
            } catch (error) {
                showToastMessage(`Lỗi xóa sản phẩm: ${error.message}`, 'error');
            }
        };

        const tangSoLuongModal = () => {
            if (soLuongChon.value < sanPhamDangXem.value.soLuong) {
                soLuongChon.value++;
            }
        };

        const giamSoLuongModal = () => {
            if (soLuongChon.value > 1) {
                soLuongChon.value--;
            }
        };

        // =================== CUSTOMER FUNCTIONS ===================
        const loadDanhSachKhachHang = async () => {
            if (loadingCustomers.value) return;

            loadingCustomers.value = true;
            try {
                const params = new URLSearchParams({
                    keyword: customerSearchKeyword.value.trim() || '',
                    page: '0',
                    size: '50'
                });

                const data = await apiCall(`${API_BASE_URL}/khach-hang/search?${params}`);
                if (data.success) {
                    danhSachKhachHang.value = data.data || [];
                } else {
                    danhSachKhachHang.value = [];
                }
            } catch (error) {
                console.error('Lỗi load khách hàng:', error);
                danhSachKhachHang.value = [];
                showToastMessage('Lỗi tải danh sách khách hàng', 'error');
            } finally {
                loadingCustomers.value = false;
            }
        };

        const debounceCustomerSearch = () => {
            if (window.customerSearchTimer) clearTimeout(window.customerSearchTimer);
            window.customerSearchTimer = setTimeout(() => loadDanhSachKhachHang(), 500);
        };

        const taoKhachHangMoi = async () => {
            // Kiểm tra trạng thái loading
            if (loadingCreateCustomer.value) {
                console.warn('Đang tạo khách hàng, vui lòng đợi...');
                return;
            }

            // Tạo địa chỉ đầy đủ trước khi validate
            taoDialChiDayDu();

            // Validate dữ liệu
            if (!validateNewCustomer()) {
                const firstError = Object.values(newCustomerErrors.value)[0];
                showToastMessage(firstError || 'Vui lòng kiểm tra lại thông tin', 'error');
                return;
            }

            loadingCreateCustomer.value = true;

            try {
                // Chuẩn bị dữ liệu gửi API
                const customerData = {
                    hoTen: (newCustomer.value.hoTen || '').trim(),
                    sdt: (newCustomer.value.sdt || '').trim().replace(/\s+/g, ''),
                    email: (newCustomer.value.email || '').trim() || null,
                    diaChi: (newCustomer.value.diaChi || '').trim() || null,
                    tinhId: newCustomer.value.tinhId || null,
                    xaId: newCustomer.value.xaId || null,
                    diaChiChiTiet: (newCustomer.value.diaChiChiTiet || '').trim() || null,
                    // Metadata cho backend
                    addressVersion: '2025',
                    addressType: '2_LEVEL'
                };

                console.log('Đang tạo khách hàng với dữ liệu:', customerData);

                const data = await apiCall(`${API_BASE_URL}/khach-hang/tao-nhanh`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(customerData)
                });

                if (data.success && data.data) {
                    const newCustomerData = data.data;

                    // Áp dụng khách hàng vào hóa đơn hiện tại
                    await chonKhachHang(newCustomerData);

                    // Reset form
                    resetCustomerForm();

                    // Đóng modal
                    showCreateCustomerForm.value = false;
                    showCustomerModal.value = false;

                    showToastMessage(`Tạo và áp dụng khách hàng ${newCustomerData.hoTen} thành công!`);
                } else {
                    throw new Error(data.message || 'Phản hồi từ server không hợp lệ');
                }
            } catch (error) {
                console.error('Lỗi tạo khách hàng:', error);

                let errorMessage = 'Có lỗi xảy ra khi tạo khách hàng';

                if (error.message.includes('HTTP 400')) {
                    errorMessage = 'Thông tin khách hàng không hợp lệ';
                } else if (error.message.includes('HTTP 409')) {
                    errorMessage = 'Số điện thoại đã được sử dụng';
                } else if (error.message.includes('HTTP 500')) {
                    errorMessage = 'Lỗi hệ thống, vui lòng thử lại sau';
                } else if (error.message.includes('fetch')) {
                    errorMessage = 'Không thể kết nối đến server';
                }

                showToastMessage(errorMessage, 'error');
            } finally {
                loadingCreateCustomer.value = false;
            }
        };
        const resetCustomerForm = () => {
            newCustomer.value = {
                hoTen: '',
                sdt: '',
                email: '',
                tinhId: '',
                xaId: '',
                diaChiChiTiet: '',
                diaChi: ''
            };
            newCustomerErrors.value = {};
            danhSachXa.value = [];
        };

        const chonKhachHang = async (customer) => {
            try {
                const data = await apiCall(`${API_BASE_URL}/hoa-don-cho/${hoaDonDangChon.value.id}/ap-dung-khach-hang/${customer.id}`, {
                    method: 'POST'
                });

                if (data.success) {
                    khachHang.value = customer;
                    showCustomerModal.value = false;
                    customerSearchKeyword.value = '';
                    showToastMessage(`Đã áp dụng khách hàng ${customer.hoTen}`);
                } else {
                    showToastMessage(data.message || 'Lỗi áp dụng khách hàng', 'error');
                }
            } catch (error) {
                showToastMessage('Lỗi áp dụng khách hàng', 'error');
            }
        };

        const boKhachHang = async () => {
            try {
                const data = await apiCall(`${API_BASE_URL}/hoa-don-cho/${hoaDonDangChon.value.id}/bo-khach-hang`, {
                    method: 'DELETE'
                });

                if (data.success) {
                    khachHang.value = null;
                    showToastMessage('Đã bỏ khách hàng');
                } else {
                    showToastMessage(data.message || 'Lỗi bỏ khách hàng', 'error');
                }
            } catch (error) {
                showToastMessage('Lỗi bỏ khách hàng', 'error');
            }
        };

        // =================== VOUCHER FUNCTIONS ===================
        const layDanhSachVoucher = async () => {
            if (loadingVouchers.value) return;

            loadingVouchers.value = true;
            try {
                const tongTien = tongQuan.value.tongTienKhuyenMai;
                const khachHangId = khachHang.value?.id;

                const params = new URLSearchParams();
                if (khachHangId) params.append('khachHangId', khachHangId);
                if (tongTien) params.append('tongTien', tongTien);

                const data = await apiCall(`${API_BASE_URL}/voucher/kha-dung?${params}`);
                if (data.success) {
                    // ✅ THÊM: Xử lý ảnh voucher (sử dụng duongDanHinhAnh như VoucherList.vue)
                    danhSachVoucher.value = (data.data || []).map(voucher => ({
                        ...voucher,
                        hinhAnh: createVoucherImageUrl(voucher.duongDanHinhAnh)
                    }));
                } else {
                    danhSachVoucher.value = [];
                }
            } catch (error) {
                console.error('Lỗi load voucher:', error);
                danhSachVoucher.value = [];
                showToastMessage('Lỗi tải danh sách voucher', 'error');
            } finally {
                loadingVouchers.value = false;
            }
        };

        // ✅ THÊM: Helper function để tạo URL ảnh voucher (theo pattern VoucherList.vue)
        const createVoucherImageUrl = (duongDanHinhAnh) => {
            if (!duongDanHinhAnh) return null;
            if (duongDanHinhAnh.startsWith('http://') || duongDanHinhAnh.startsWith('https://')) return duongDanHinhAnh;
            return `http://localhost:8080${duongDanHinhAnh}`;
        };

        // ✅ THÊM: Helper function để xử lý lỗi ảnh voucher
        const handleVoucherImageError = (event) => {
            event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2Y4ZjlmYSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjEyIiBmaWxsPSIjNmM3NTdkIj5Wb3VjaGVyPC90ZXh0Pjwvc3ZnPg==';
        };

        const kiemTraVoucher = async () => {
            if (!voucherCode.value.trim()) {
                showToastMessage('Vui lòng nhập mã voucher', 'error');
                return;
            }

            if (!hoaDonDangChon.value || !hoaDonDangChon.value.id) {
                showToastMessage('Chưa chọn hóa đơn để áp dụng voucher', 'error');
                return;
            }

            loadingVoucherCheck.value = true;
            try {
                const request = {
                    maVoucher: voucherCode.value.trim(),
                    tongTien: tongQuan.value.tongTienKhuyenMai,
                    khachHangId: khachHang.value?.id || null
                };

                console.log('🔍 Kiểm tra voucher:', request);

                const data = await apiCall(`${API_BASE_URL}/voucher/kiem-tra`, {
                    method: 'POST',
                    body: JSON.stringify(request)
                });

                console.log('🔍 Kết quả kiểm tra voucher:', data);

                if (data.valid && data.voucher) {
                    await chonVoucher(data.voucher);
                    voucherCode.value = '';
                } else {
                    showToastMessage(data.message || 'Mã voucher không hợp lệ', 'error');
                }
            } catch (error) {
                console.error('❌ Lỗi kiểm tra voucher:', error);
                showToastMessage(`Lỗi kiểm tra voucher: ${error.message}`, 'error');
            } finally {
                loadingVoucherCheck.value = false;
            }
        };

        const chonVoucher = async (selectedVoucher) => {
            try {
                if (!selectedVoucher || !selectedVoucher.id) {
                    showToastMessage('Dữ liệu voucher không hợp lệ', 'error');
                    return;
                }

                if (!hoaDonDangChon.value || !hoaDonDangChon.value.id) {
                    showToastMessage('Chưa chọn hóa đơn để áp dụng voucher', 'error');
                    return;
                }

                const data = await apiCall(`${API_BASE_URL}/hoa-don-cho/${hoaDonDangChon.value.id}/ap-dung-voucher/${selectedVoucher.id}`, {
                    method: 'POST'
                });

                if (data.success) {
                    // Lưu voucher đã áp dụng
                    voucherDaApDung.value = selectedVoucher;
                    voucher.value = selectedVoucher;

                    showVoucherModal.value = false;
                    voucherCode.value = '';
                    showToastMessage(`Đã áp dụng voucher ${selectedVoucher.tenVoucher}`);
                } else {
                    showToastMessage(data.message || 'Lỗi áp dụng voucher', 'error');
                }
            } catch (error) {
                showToastMessage(`Lỗi áp dụng voucher: chưa đủ điều kiện`, 'error');
            }
        };

        const boVoucher = async () => {
            try {
                const data = await apiCall(`${API_BASE_URL}/hoa-don-cho/${hoaDonDangChon.value.id}/bo-voucher`, {
                    method: 'DELETE'
                });

                if (data.success) {
                    voucher.value = null;
                    voucherDaApDung.value = null;
                    chiTietVoucherDaApDung.value = []; // ✅ Clear chi tiết voucher
                    showToastMessage('Đã bỏ voucher');
                } else {
                    showToastMessage(data.message || 'Lỗi bỏ voucher', 'error');
                }
            } catch (error) {
                showToastMessage('Lỗi bỏ voucher', 'error');
            }
        };

        const voucherHienTaiInfo = computed(() => {
            if (!voucher.value) return null;

            const chiTiet = chiTietVoucherDaApDung.value.find((cv) => cv.voucherId === voucher.value.id);

            if (chiTiet) {
                return formatVoucherInfo(chiTiet);
            }

            // Fallback sử dụng thông tin voucher gốc
            return {
                tenVoucher: voucher.value.tenVoucher,
                maVoucher: voucher.value.maVoucher,
                loaiGiamGia: voucher.value.loaiGiamGia,
                giaTriGiam: voucher.value.giaTriGiam,
                phanTramGiam: voucher.value.loaiGiamGia === 'PHAN_TRAM' ? voucher.value.giaTriGiam : null,
                tienGiam: voucher.value.loaiGiamGia === 'TIEN_MAT' ? voucher.value.giaTriGiam : null
            };
        });

        // Computed để kiểm tra có chi tiết voucher hay không
        const coChiTietVoucher = computed(() => {
            return chiTietVoucherDaApDung.value.length > 0;
        });

        const resetVoucherStateWhenChangeInvoice = () => {
            voucher.value = null;
            voucherDaApDung.value = null;
        };

        // =================== PAYMENT FUNCTIONS ===================
        const tinhTongThanhToan = () => {
            // Tổng thanh toán KHÔNG trừ điểm tích lũy nữa
            const tong = tongQuan.value.tongTienThanhToan;
            return Math.max(0, tong);
        };

        const coTheThanhToan = () => {
            return tinhTienThua() >= 0 && sanPhamDaChon.value.length > 0;
        };

        const printInvoice = () => {
            const printContent = document.getElementById('invoice-print');
            const originalContent = document.body.innerHTML;

            document.body.innerHTML = printContent.outerHTML;
            window.print();
            document.body.innerHTML = originalContent;
            window.location.reload();
        };

        const switchQrMode = async (mode) => {
            if (qrMode.value === mode) return;

            if (qrMode.value === 'camera' && cameraStarted.value) {
                await stopCamera();
            }

            qrMode.value = mode;

            if (mode === 'manual') {
                await nextTick();
                qrInput.value?.focus();
            } else if (mode === 'camera') {
                await nextTick();
                setTimeout(() => {
                    startCamera();
                }, 100);
            }
        };

        const startCamera = async () => {
            try {
                cameraError.value = '';
                console.log('Loading jsQR...');
                await loadJsQRFromCDN();
                console.log('jsQR loaded, starting camera...');

                cameraStarted.value = true;
                await nextTick();
                await new Promise((resolve) => setTimeout(resolve, 200));

                const video = document.getElementById('qr-video');
                const canvas = document.getElementById('qr-canvas');

                if (!video || !canvas) {
                    throw new Error('Video hoặc Canvas element không tìm thấy');
                }

                qrStream.value = await navigator.mediaDevices.getUserMedia({
                    video: {
                        facingMode: 'environment',
                        width: { ideal: 640 },
                        height: { ideal: 480 }
                    }
                });

                video.srcObject = qrStream.value;

                await new Promise((resolve) => {
                    video.onloadedmetadata = () => {
                        video.play().then(resolve).catch(resolve);
                    };
                });

                await startQrScan(video, canvas);
                console.log('Camera and QR scanning started successfully');
            } catch (error) {
                console.error('Camera start error:', error);
                cameraStarted.value = false;

                if (error.name === 'NotAllowedError') {
                    cameraError.value = 'Bạn cần cho phép truy cập camera';
                } else if (error.name === 'NotFoundError') {
                    cameraError.value = 'Không tìm thấy camera';
                } else {
                    cameraError.value = `Lỗi: ${error.message}`;
                }
            }
        };

        const startQrScan = async (video, canvas) => {
            const ctx = canvas.getContext('2d');

            if (!window.jsQR) {
                try {
                    await loadJsQRFromCDN();
                } catch (error) {
                    cameraError.value = 'Không thể tải thư viện quét QR';
                    return;
                }
            }

            const scan = () => {
                if (!cameraStarted.value || !video.videoWidth || !video.videoHeight) {
                    return;
                }

                try {
                    canvas.width = video.videoWidth;
                    canvas.height = video.videoHeight;
                    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

                    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
                    const code = window.jsQR(imageData.data, imageData.width, imageData.height);

                    if (code && code.data) {
                        console.log('QR Code detected:', code.data);
                        qrCode.value = code.data;

                        if (scanInterval.value) {
                            clearInterval(scanInterval.value);
                            scanInterval.value = null;
                        }

                        setTimeout(() => {
                            stopCamera();
                            quetQR();
                        }, 1000);

                        return;
                    }
                } catch (error) {
                    console.warn('QR scan error:', error);
                }
            };

            scanInterval.value = setInterval(scan, 200);
            console.log('QR scanning started');
        };

        const stopCamera = async () => {
            try {
                console.log('Stopping camera...');

                if (scanInterval.value) {
                    clearInterval(scanInterval.value);
                    scanInterval.value = null;
                }

                if (qrStream.value) {
                    qrStream.value.getTracks().forEach((track) => track.stop());
                    qrStream.value = null;
                }

                const video = document.getElementById('qr-video');
                if (video) {
                    video.srcObject = null;
                }

                cameraStarted.value = false;
                cameraError.value = '';
                console.log('Camera stopped successfully');
            } catch (error) {
                console.warn('Lỗi dừng camera:', error);
                cameraStarted.value = false;
            }
        };

        const retryCamera = () => {
            cameraError.value = '';
            cameraStarted.value = false;
            startCamera();
        };

        const clearQrCode = () => {
            qrCode.value = '';
        };

        const quetQR = async () => {
            if (!qrCode.value.trim() || loadingQR.value) return;

            loadingQR.value = true;
            try {
                console.log('Scanning QR code:', qrCode.value);

                const data = await apiCall(`${API_BASE_URL}/san-pham/scan-qr`, {
                    method: 'POST',
                    body: JSON.stringify({ qrCode: qrCode.value.trim() })
                });

                if (data.success && data.data) {
                    if (cameraStarted.value) {
                        stopCamera();
                    }

                    showQrScanner.value = false;
                    qrCode.value = '';

                    await themVaoHoaDon(data.data);
                    showToastMessage(`Đã thêm ${data.data.tenSanPham} vào hóa đơn!`);
                } else {
                    showToastMessage(data.message || 'Không tìm thấy sản phẩm với mã QR này', 'error');
                }
            } catch (error) {
                console.error('QR scan error:', error);
                showToastMessage('Lỗi quét QR code: ' + error.message, 'error');
            } finally {
                loadingQR.value = false;
            }
        };

        // =================== LIFECYCLE ===================
        onMounted(async () => {
            console.log('Khởi tạo ứng dụng POS...');

            try {
                loadEmployeeInfo();
                await loadMasterData();
                await layDanhSachHoaDonCho();

                if (hoaDonCho.value.length === 0) {
                    await taoHoaDonMoi();
                }

                await timKiemSanPham(0);
                showToastMessage('Khởi tạo thành công');
            } catch (error) {
                console.error('Lỗi khởi tạo:', error);
                showToastMessage('Lỗi khởi tạo ứng dụng', 'error');
            }
        });

        // =================== WATCHERS ===================
        watch(showProductModal, (newVal) => {
            if (newVal) {
                timKiemSanPham(0);
            }
        });

        watch(showCustomerModal, async (newVal) => {
            if (newVal) {
                customerSearchKeyword.value = '';
                await loadDanhSachKhachHang();
            } else {
                danhSachKhachHang.value = [];
                customerSearchKeyword.value = '';
            }
        });
        // Thêm watch để debug khi voucher state thay đổi:
        watch(
            voucher,
            (newVoucher, oldVoucher) => {
                console.log('👀 Voucher state changed:', {
                    old: oldVoucher,
                    new: newVoucher
                });
            },
            { deep: true }
        );

        // Thêm computed để debug tongQuan:
        const tongQuanDebug = computed(() => {
            const result = tongQuan.value;
            console.log('🧮 TongQuan computed:', {
                products: sanPhamDaChon.value.length,
                voucher: voucher.value,
                tongTienVoucher: result.tongTienVoucher,
                tongTienThanhToan: result.tongTienThanhToan
            });
            return result;
        });

        watch(showVoucherModal, async (newVal) => {
            if (newVal) {
                voucherCode.value = '';
                await layDanhSachVoucher();
            } else {
                danhSachVoucher.value = [];
                voucherCode.value = '';
            }
        });

        watch(showQrScanner, (newVal) => {
            if (newVal) {
                qrCode.value = '';
                qrMode.value = 'manual';
                cameraError.value = '';
                cameraStarted.value = false;
                loadingQR.value = false;

                nextTick(() => {
                    qrInput.value?.focus();
                });

                console.log('QR Scanner modal opened');
            } else {
                stopCamera();
                qrCode.value = '';
                qrMode.value = 'manual';
                cameraError.value = '';
                loadingQR.value = false;
                console.log('QR Scanner modal closed');
            }
        });

        watch(
            () => newCustomer.value.tinhId,
            (newTinhId) => {
                if (newTinhId) {
                    layDanhSachXa(newTinhId);
                }
            }
        );
        watch(
            [() => newCustomer.value.tinhId, () => newCustomer.value.xaId, () => newCustomer.value.diaChiChiTiet],
            () => {
                taoDialChiDayDu();
            },
            { deep: true }
        );
        watch(showCreateCustomerForm, async (newVal) => {
            if (newVal) {
                // Reset form
                newCustomer.value = {
                    hoTen: '',
                    sdt: '',
                    email: '',
                    tinhId: '',
                    xaId: '',
                    diaChiChiTiet: '',
                    diaChi: ''
                };
                newCustomerErrors.value = {};

                // Load danh sách tỉnh khi mở form
                await layDanhSachTinh();
            } else {
                // Clear data khi đóng form
                danhSachTinh.value = [];
                danhSachXa.value = [];
            }
        });

        watch(showCreateCustomerForm, (newVal) => {
            if (newVal) {
                newCustomer.value = { hoTen: '', sdt: '', email: '', diaChi: '' };
                newCustomerErrors.value = {};
            }
        });

        // =================== RETURN ===================
        return {
            // Data
            nhanVienInfo,
            hoaDonCho,
            hoaDonDangChon,
            danhSachSanPham,
            sanPhamDaChon,
            khachHang,
            voucher,
            danhSachTinh,
            danhSachXa,
            loadingAddress,

            // Address methods
            layDanhSachTinh,
            layDanhSachXa,
            taoDialChiDayDu,
            chuyenDoiDiaChiCuSangMoi,

            // UI States
            loading,
            error,
            searchKeyword,
            customerSearchKeyword,
            voucherCode,
            soLuongChon,
            qrCode,
            loadingDelete,
            loadingCreateInvoice,

            // Pagination
            trangHienTai,
            tongSoTrang,
            tongSoPhanTu,

            // Modals
            showCustomerModal,
            showVoucherModal,
            showPaymentModal,
            showProductDetail,
            showProductModal,
            showQrScanner,
            showCreateCustomerForm,
            showInvoicePrint,
            sanPhamDangXem,

            // Loading states
            loadingCustomers,
            loadingVouchers,
            loadingCreateCustomer,
            loadingVoucherCheck,
            loadingPayment,

            // QR Scanner
            qrMode,
            cameraError,
            cameraStarted,
            loadingQR,
            qrInput,

            // Toast
            showToast,
            toastMessage,
            toastType,

            // Filters
            filters,
            danhSachDanhMuc,
            danhSachThuongHieu,
            danhSachKhachHang,
            danhSachVoucher,

            // Forms
            newCustomer,
            newCustomerErrors,

            // Payment
            thongTinThanhToan,
            quickAmounts,

            // Invoice print
            hoaDonDaThanhToan,
            sanPhamDaThanhToan,
            tongQuanDaThanhToan,
            voucherDaThanhToan,
            thongTinThanhToanCuoi,
            phuongThucThanhToanHienTai,
            validateThanhToan,
            chonChuyenKhoanDayDu,
            formatPhuongThucThanhToan,
            getTienThuaDisplay,

            // Computed
            tongQuan,

            // Methods
            showToastMessage,
            formatPrice,
            formatDate,
            formatDateTime,
            getMauHex,
            getProductImage,
            handleImageError,

            // Invoice methods
            layDanhSachHoaDonCho,
            chonHoaDon,
            taoHoaDonMoi,
            confirmDeleteHoaDon,

            // Product methods
            timKiemSanPham,
            debounceSearch,
            debouncePriceFilter,
            applyFilters,
            chuyenTrang,
            xemChiTietSanPham,
            themVaoHoaDon,
            themVaoHoaDonTuModal,
            tangSoLuong,
            giamSoLuong,
            xoaKhoiGioHang,
            tangSoLuongModal,
            giamSoLuongModal,

            // Customer methods
            loadDanhSachKhachHang,
            debounceCustomerSearch,
            validateNewCustomer,
            taoKhachHangMoi,
            chonKhachHang,
            boKhachHang,
            voucherDaApDung,
            resetVoucherStateWhenChangeInvoice,

            // Voucher methods
            layDanhSachVoucher,
            kiemTraVoucher,
            chonVoucher,
            boVoucher,

            // Payment methods
            tinhTongThanhToan,
            getDiscountPercentage,
            tinhTienThua,
            coTheThanhToan,
            chonTienNhanh,
            chonTienVuaVua,
            xuLyThanhToan,
            printInvoice,

            chiTietVoucherDaApDung,
            loadingChiTietVoucher,
            showVoucherDetailModal,
            layChiTietVoucherDaApDung,
            xemChiTietVoucherDaApDung,
            layTatCaChiTietVoucher,
            formatVoucherInfo,
            voucherHienTaiInfo,
            coChiTietVoucher,
            // QR Scanner methods
            switchQrMode,
            startCamera,
            stopCamera,
            retryCamera,
            clearQrCode,
            quetQR
        };
    }
};
</script>
<template>
    <div class="pos-container">
        <!-- Toast Notifications -->
        <div class="toast-container position-fixed end-0 top-0 p-3" style="z-index: 1100">
            <div
                v-if="showToast"
                class="toast show"
                :class="{
                    'text-bg-success': toastType === 'success',
                    'text-bg-danger': toastType === 'error',
                    'text-bg-warning': toastType === 'warning'
                }"
            >
                <div class="toast-body">{{ toastMessage }}</div>
            </div>
        </div>

        <!-- Main Layout -->
        <div class="main-layout">
            <!-- Header -->
            <div class="pos-header bg-primary p-3 text-white">
                <div class="container-fluid">
                    <div class="row align-items-center">
                        <div class="col">
                            <h3 class="mb-0">
                                <i class="bi bi-shop me-2"></i>
                                Bán Hàng Tại Quầy
                            </h3>
                        </div>
                        <div class="col-auto">
                            <div class="d-flex align-items-center text-white-50">
                                <i class="bi bi-person-circle me-2"></i>
                                <div>
                                    <div class="fw-semibold">{{ nhanVienInfo.tenNhanVien }}</div>
                                    <small>{{ nhanVienInfo.maNhanVien }}</small>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="invoice-tabs-section border-bottom bg-white">
                <div class="container-fluid py-2">
                    <div class="d-flex justify-content-between align-items-center">
                        <div class="d-flex flex-grow-1 gap-2 overflow-auto">
                            <button
                                v-for="(hoaDon, index) in hoaDonCho"
                                :key="hoaDon.id"
                                @click="chonHoaDon(hoaDon)"
                                class="btn btn-sm invoice-tab"
                                :class="hoaDonDangChon?.id === hoaDon.id ? 'btn-primary' : 'btn-outline-primary'"
                                :disabled="loadingDelete === hoaDon.id"
                            >
                                <span v-if="loadingDelete === hoaDon.id" class="spinner-border spinner-border-sm me-1"></span>
                                <i v-else class="bi bi-file-text me-1"></i>
                                Hóa đơn {{ index + 1 }}

                                <!-- Nút xóa - đã sửa lại vị trí -->
                                <button v-if="hoaDonCho.length > 1" @click.stop="confirmDeleteHoaDon(hoaDon)" :disabled="loadingDelete === hoaDon.id" class="btn btn-danger btn-xs-delete">
                                    <i class="bi bi-x"></i>
                                </button>
                            </button>
                        </div>

                        <button @click="taoHoaDonMoi" :disabled="loadingCreateInvoice" class="btn btn-success btn-sm ms-3">
                            <span v-if="loadingCreateInvoice" class="spinner-border spinner-border-sm me-1"></span>
                            <i v-else class="bi bi-plus-lg me-1"></i>
                            Tạo mới
                        </button>
                    </div>
                </div>
            </div>

            <!-- Main Content -->
            <div class="main-content">
                <div class="container-fluid h-100">
                    <div class="row h-100">
                        <!-- Left Panel -->
                        <div class="col-lg-8 left-panel">
                            <!-- 2. DANH SÁCH SẢN PHẨM - Bảng -->
                            <div class="card h-100">
                                <div class="card-header d-flex justify-content-between align-items-center">
                                    <h5 class="mb-0">
                                        <i class="bi bi-cart3 me-2"></i>
                                        Danh sách sản phẩm ({{ tongQuan.soLuongSanPham }} loại - {{ tongQuan.tongSoLuong }} sp)
                                    </h5>
                                    <div class="d-flex gap-2">
                                        <button @click="showQrScanner = true" class="btn btn-outline-primary btn-sm">
                                            <i class="bi bi-qr-code-scan me-1"></i>
                                            QR
                                        </button>
                                        <button @click="showProductModal = true" class="btn btn-primary btn-sm">
                                            <i class="bi bi-plus-lg me-1"></i>
                                            Thêm sản phẩm
                                        </button>
                                    </div>
                                </div>

                                <div class="card-body p-0">
                                    <!-- Empty State -->
                                    <div v-if="sanPhamDaChon.length === 0" class="text-muted py-5 text-center">
                                        <i class="bi bi-cart display-4 mb-3"></i>
                                        <h5>Chưa có sản phẩm nào</h5>
                                        <p>Nhấn "Thêm sản phẩm" để bắt đầu</p>
                                    </div>

                                    <!-- Products Table -->
                                    <div v-else class="table-responsive" style="max-height: 400px">
                                        <table class="table-hover mb-0 table">
                                            <thead class="table-light sticky-top">
                                                <tr>
                                                    <th width="60">STT</th>
                                                    <th width="80">Hình</th>
                                                    <th width="120">Mã SP</th>
                                                    <th>Tên sản phẩm</th>
                                                    <th width="80">Màu</th>
                                                    <th width="70">Size</th>
                                                    <th width="100">Đơn giá</th>
                                                    <th width="120">Số lượng</th>
                                                    <th width="120">Thành tiền</th>
                                                    <th width="80">Thao tác</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <tr v-for="(item, index) in sanPhamDaChon" :key="item.id">
                                                    <td class="text-center">{{ index + 1 }}</td>
                                                    <td>
                                                        <img :src="getProductImage(item)" :alt="item.tenSanPham" @error="handleImageError" class="rounded" style="width: 50px; height: 50px; object-fit: cover" />
                                                    </td>
                                                    <td>
                                                        <code class="small">{{ item.maSanPham || 'SP' + item.chiTietSanPhamId }}</code>
                                                    </td>
                                                    <td>
                                                        <div class="fw-semibold">{{ item.tenSanPham }}</div>
                                                        <small class="text-muted">{{ item.thuongHieu?.tenThuongHieu }}</small>
                                                    </td>
                                                    <td>
                                                        <div class="d-flex align-items-center">
                                                            <span
                                                                class="rounded-circle me-2"
                                                                :style="{
                                                                    backgroundColor: getMauHex(item.mauSac?.tenMau),
                                                                    width: '16px',
                                                                    height: '16px',
                                                                    border: '2px solid #fff',
                                                                    boxShadow: '0 0 0 1px rgba(0,0,0,0.1)'
                                                                }"
                                                            ></span>
                                                            <small>{{ item.mauSac?.tenMau || 'N/A' }}</small>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <span class="badge bg-light text-dark">{{ item.kichCo?.tenKichCo || 'N/A' }}</span>
                                                    </td>
                                                    <td>
                                                        <div class="fw-semibold text-primary">{{ formatPrice(item.giaBan) }}</div>
                                                    </td>
                                                    <td>
                                                        <div class="d-flex align-items-center justify-content-center">
                                                            <button @click="giamSoLuong(item)" class="btn btn-outline-secondary btn-sm rounded-circle" style="width: 28px; height: 28px; padding: 0">
                                                                <i class="bi bi-dash"></i>
                                                            </button>
                                                            <span class="fw-bold mx-3">{{ item.soLuongDaChon }}</span>
                                                            <button @click="tangSoLuong(item)" class="btn btn-outline-secondary btn-sm rounded-circle" style="width: 28px; height: 28px; padding: 0" :disabled="Number.isFinite(item.soLuong) && item.soLuongDaChon >= item.soLuong">
                                                                <i class="bi bi-plus"></i>
                                                            </button>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <div class="fw-bold text-success">
                                                            {{ formatPrice(item.giaBan * item.soLuongDaChon) }}
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <button @click="xoaKhoiGioHang(item)" class="btn btn-outline-danger btn-sm" title="Xóa sản phẩm">
                                                            <i class="bi bi-trash"></i>
                                                        </button>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Right Panel -->
                        <div class="col-lg-4 right-panel">
                            <div class="d-flex flex-column h-100">
                                <!-- 3. THÔNG TIN KHÁCH HÀNG -->
                                <div class="card mb-3">
                                    <div class="card-header d-flex justify-content-between align-items-center">
                                        <h6 class="mb-0">
                                            <i class="bi bi-person me-2"></i>
                                            Khách hàng
                                        </h6>
                                        <button @click="showCustomerModal = true" class="btn btn-outline-primary btn-sm">
                                            <i class="bi bi-search me-1"></i>
                                            Chọn
                                        </button>
                                    </div>
                                    <div class="card-body">
                                        <div v-if="khachHang" class="d-flex justify-content-between align-items-start">
                                            <div class="flex-grow-1">
                                                <div class="fw-bold">{{ khachHang.hoTen }}</div>
                                                <div class="text-muted small">{{ khachHang.sdt }}</div>
                                                <div v-if="false" class="small mt-1 text-primary">
                                                    <i class="bi bi-gem me-1"></i>
                                                    {{ khachHang.diemTichLuy || 0 }} điểm
                                                </div>
                                            </div>
                                            <button @click="boKhachHang" class="btn btn-outline-danger btn-sm">
                                                <i class="bi bi-x-lg"></i>
                                            </button>
                                        </div>
                                        <div v-else class="text-muted py-3 text-center">
                                            <i class="bi bi-person-circle display-6"></i>
                                            <div class="mt-2">Khách lẻ</div>
                                        </div>
                                    </div>
                                </div>

                                <!-- 4. THANH TOÁN - Voucher và tổng tiền -->
                                <div class="card flex-grow-1">
                                    <div class="card-header">
                                        <h6 class="mb-0">
                                            <i class="bi bi-credit-card me-2"></i>
                                            Thanh toán
                                        </h6>
                                    </div>
                                    <div class="card-body">
                                        <!-- Voucher Section -->
                                        <div class="mb-4">
                                            <div class="d-flex justify-content-between align-items-center mb-2">
                                                <label class="form-label mb-0">Voucher</label>
                                                <button @click="showVoucherModal = true" class="btn btn-outline-success btn-sm">
                                                    <i class="bi bi-ticket me-1"></i>
                                                    Áp dụng
                                                </button>
                                            </div>

                                            <div v-if="voucher" class="voucher-selected bg-light rounded border p-2">
                                                <div class="d-flex justify-content-between align-items-center">
                                                    <!-- ✅ THÊM: Ảnh voucher đã chọn -->
                                                    <div class="me-3" style="flex-shrink: 0;">
                                                        <img 
                                                            v-if="voucher.hinhAnh" 
                                                            :src="voucher.hinhAnh" 
                                                            :alt="voucher.tenVoucher"
                                                            @error="handleVoucherImageError"
                                                            class="rounded"
                                                            style="width: 50px; height: 50px; object-fit: cover;"
                                                        />
                                                        <div 
                                                            v-else 
                                                            class="d-flex align-items-center justify-content-center bg-white rounded border"
                                                            style="width: 50px; height: 50px;"
                                                        >
                                                            <i class="bi bi-ticket text-muted"></i>
                                                        </div>
                                                    </div>
                                                    
                                                    <div class="flex-grow-1">
                                                        <div class="fw-semibold">{{ voucher.tenVoucher }}</div>
                                                        <div class="text-success small">
                                                            <i class="bi bi-tag-fill me-1"></i>
                                                            <span v-if="voucher.loaiGiamGia === 'PHAN_TRAM'"> Giảm {{ voucher.giaTriGiam }}% </span>
                                                            <span v-else> Giảm {{ formatPrice(voucher.giaTriGiam) }} </span>
                                                        </div>

                                                        <!-- ✅ THÊM: Hiển thị chi tiết voucher nếu có -->
                                                        <div v-if="coChiTietVoucher" class="mt-2">
                                                            <small class="text-muted">
                                                                <i class="bi bi-info-circle me-1"></i>
                                                                Đã áp dụng: {{ formatPrice(voucherHienTaiInfo?.soTienGiam || 0) }}
                                                            </small>
                                                            <br />
                                                            <button @click="xemChiTietVoucherDaApDung" class="btn btn-outline-info btn-sm mt-1">
                                                                <i class="bi bi-eye me-1"></i>
                                                                Chi tiết
                                                            </button>
                                                        </div>
                                                    </div>
                                                    <button @click="boVoucher" class="btn btn-outline-danger btn-sm">
                                                        <i class="bi bi-x-lg"></i>
                                                    </button>
                                                </div>
                                            </div>

                                            <div v-else class="text-muted rounded border py-2 text-center">
                                                <i class="bi bi-ticket"></i>
                                                Chưa có voucher
                                            </div>
                                        </div>

                                        <!-- Summary -->
                                        <div class="summary-section">
                                            <div class="d-flex justify-content-between mb-2">
                                                <span>Tạm tính:</span>
                                                <span>{{ formatPrice(tongQuan.tongTienGoc) }}</span>
                                            </div>

                                            <div v-if="voucher" class="d-flex justify-content-between text-success mb-2">
                                                <span>Voucher:</span>
                                                <span>-{{ formatPrice(tongQuan.tongTienVoucher) }}</span>
                                            </div>

                                            <hr />

                                            <div class="d-flex justify-content-between fw-bold h5 mb-0">
                                                <span>Tổng thanh toán:</span>
                                                <span class="text-danger">{{ formatPrice(tongQuan.tongTienThanhToan) }}</span>
                                            </div>
                                        </div>

                                        <!-- Payment Button -->
                                        <button @click="showPaymentModal = true" :disabled="sanPhamDaChon.length === 0" class="btn w-100 btn-lg mt-3" :class="sanPhamDaChon.length === 0 ? 'btn-secondary' : 'btn-success'">
                                            <i class="bi bi-credit-card me-2"></i>
                                            Thanh toán
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- MODAL THÊM SẢN PHẨM -->
            <div v-if="showProductModal" class="modal d-block" tabindex="-1" style="background-color: rgba(0, 0, 0, 0.5)">
                <div class="modal-dialog modal-xl">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">
                                <i class="bi bi-grid-3x3-gap me-2"></i>
                                Chọn sản phẩm
                            </h5>
                            <button @click="showProductModal = false" class="btn-close"></button>
                        </div>

                        <div class="modal-body">
                            <!-- Search & Filters -->
                            <div class="row mb-3">
                                <div class="col-md-4">
                                    <input v-model="searchKeyword" @input="debounceSearch" type="text" placeholder="Tìm sản phẩm..." class="form-control" />
                                </div>
                                <div class="col-md-2">
                                    <select v-model="filters.danhMucId" @change="applyFilters" class="form-select">
                                        <option value="">Tất cả danh mục</option>
                                        <option v-for="dm in danhSachDanhMuc" :key="dm.id" :value="dm.id">
                                            {{ dm.tenDanhMuc }}
                                        </option>
                                    </select>
                                </div>
                                <div class="col-md-2">
                                    <select v-model="filters.thuongHieuId" @change="applyFilters" class="form-select">
                                        <option value="">Tất cả thương hiệu</option>
                                        <option v-for="th in danhSachThuongHieu" :key="th.id" :value="th.id">
                                            {{ th.tenThuongHieu }}
                                        </option>
                                    </select>
                                </div>
                                <div class="col-md-2">
                                    <input v-model="filters.minPrice" @input="debouncePriceFilter" type="number" placeholder="Giá từ" class="form-control" />
                                </div>
                                <div class="col-md-2">
                                    <input v-model="filters.maxPrice" @input="debouncePriceFilter" type="number" placeholder="Giá đến" class="form-control" />
                                </div>
                            </div>

                            <!-- Product Grid -->
                            <div style="max-height: 500px; overflow-y: auto">
                                <div v-if="loading" class="py-5 text-center">
                                    <div class="spinner-border text-primary"></div>
                                    <p class="mt-2">Đang tải sản phẩm...</p>
                                </div>

                                <div v-else-if="danhSachSanPham.length === 0" class="text-muted py-5 text-center">
                                    <i class="bi bi-search display-4"></i>
                                    <p class="mt-2">Không tìm thấy sản phẩm</p>
                                </div>

                                <div v-else class="row g-3">
                                    <div v-for="product in danhSachSanPham" :key="product.id" class="col-md-4 col-lg-3">
                                        <div class="card product-card h-100" @click="xemChiTietSanPham(product)">
                                            <div class="position-relative" style="height: 200px">
                                                <img :src="getProductImage(product)" :alt="product.tenSanPham" @error="handleImageError" class="card-img-top w-100 h-100" style="object-fit: cover" />

                                                <!-- Badge giảm giá trên góc -->
                                                <div v-if="getDiscountPercentage(product)" class="position-absolute start-0 top-0 m-2">
                                                    <span class="badge bg-danger discount-badge">
                                                        <i class="bi bi-percent me-1"></i>
                                                        -{{ getDiscountPercentage(product) }}%
                                                    </span>
                                                </div>

                                                <!-- Badge hết hàng -->
                                                <div v-if="product.soLuong <= 0" class="position-absolute end-0 top-0 m-2">
                                                    <span class="badge bg-secondary">Hết hàng</span>
                                                </div>
                                            </div>

                                            <div class="card-body p-3">
                                                <h6 class="card-title text-truncate mb-2">{{ product.tenSanPham }}</h6>

                                                <div class="d-flex justify-content-between align-items-center mb-2">
                                                    <small>{{ product.mauSac?.tenMau || 'N/A' }}</small>
                                                    <small class="badge bg-light text-dark">{{ product.kichCo?.tenKichCo || 'N/A' }}</small>
                                                </div>

                                                <!-- Hiển thị giá với cross-out nếu có giảm giá -->
                                                <div class="mb-3">
                                                    <div v-if="getDiscountPercentage(product)" class="price-section">
                                                        <div class="fw-bold text-danger price-current">
                                                            {{ formatPrice(product.giaBan) }}
                                                        </div>
                                                        <div class="text-muted price-original">
                                                            <s>{{ formatPrice(product.giaGoc || product.giaBan) }}</s>
                                                        </div>
                                                    </div>
                                                    <div v-else class="fw-bold text-primary">
                                                        {{ formatPrice(product.giaBan) }}
                                                    </div>
                                                </div>

                                                <button @click.stop="themVaoHoaDon(product)" :disabled="product.soLuong <= 0" class="btn btn-sm w-100" :class="product.soLuong <= 0 ? 'btn-secondary' : 'btn-primary'">
                                                    <i class="bi bi-cart-plus me-1"></i>
                                                    {{ product.soLuong <= 0 ? 'Hết hàng' : 'Thêm vào hóa đơn' }}
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Pagination -->
                                <nav v-if="tongSoTrang > 1" class="mt-4">
                                    <ul class="pagination justify-content-center">
                                        <li class="page-item" :class="{ disabled: trangHienTai === 0 }">
                                            <a class="page-link" @click.prevent="chuyenTrang(trangHienTai - 1)">
                                                <i class="bi bi-chevron-left"></i>
                                            </a>
                                        </li>
                                        <li class="page-item active">
                                            <span class="page-link">{{ trangHienTai + 1 }} / {{ tongSoTrang }}</span>
                                        </li>
                                        <li class="page-item" :class="{ disabled: trangHienTai >= tongSoTrang - 1 }">
                                            <a class="page-link" @click.prevent="chuyenTrang(trangHienTai + 1)">
                                                <i class="bi bi-chevron-right"></i>
                                            </a>
                                        </li>
                                    </ul>
                                </nav>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Customer Modal -->
        <div v-if="showCustomerModal" class="modal d-block" tabindex="-1" style="background-color: rgba(0, 0, 0, 0.5)">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">
                            <i class="bi bi-person me-2"></i>
                            Chọn khách hàng
                        </h5>
                        <button @click="showCustomerModal = false" class="btn-close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <div class="input-group">
                                <input v-model="customerSearchKeyword" @input="debounceCustomerSearch" type="text" placeholder="Tìm khách hàng..." class="form-control" />
                                <button @click="showCreateCustomerForm = true" class="btn btn-success">
                                    <i class="bi bi-person-plus me-1"></i>
                                    Tạo mới
                                </button>
                            </div>
                        </div>

                        <div style="max-height: 400px; overflow-y: auto">
                            <div v-if="loadingCustomers" class="py-4 text-center">
                                <div class="spinner-border text-primary"></div>
                                <p class="mt-2">Đang tìm kiếm...</p>
                            </div>

                            <div v-else-if="danhSachKhachHang.length === 0" class="text-muted py-4 text-center">
                                <i class="bi bi-people display-4"></i>
                                <p class="mt-2">Không tìm thấy khách hàng</p>
                            </div>

                            <div v-else class="list-group">
                                <button v-for="customer in danhSachKhachHang" :key="customer.id" @click="chonKhachHang(customer)" class="list-group-item list-group-item-action">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <h6 class="mb-1">{{ customer.hoTen }}</h6>
                                            <p class="text-muted mb-1">{{ customer.sdt }}</p>
                                            <small class="text-muted">{{ customer.email || 'Chưa có email' }}</small>
                                        </div>
                                        <span v-if="false" class="badge bg-primary">{{ customer.diemTichLuy || 0 }} điểm</span>
                                    </div>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Voucher Modal -->
        <div v-if="showVoucherModal" class="modal d-block" tabindex="-1" style="background-color: rgba(0, 0, 0, 0.5)">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">
                            <i class="bi bi-ticket me-2"></i>
                            Áp dụng voucher
                        </h5>
                        <button @click="showVoucherModal = false" class="btn-close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <div class="input-group">
                                <input v-model="voucherCode" type="text" placeholder="Nhập mã voucher" class="form-control" @keyup.enter="kiemTraVoucher" />
                                <button @click="kiemTraVoucher" :disabled="!voucherCode.trim() || loadingVoucherCheck" class="btn btn-success">
                                    <span v-if="loadingVoucherCheck" class="spinner-border spinner-border-sm me-2"></span>
                                    {{ loadingVoucherCheck ? 'Đang kiểm tra...' : 'Kiểm tra' }}
                                </button>
                            </div>
                        </div>

                        <div style="max-height: 400px; overflow-y: auto">
                            <div v-if="loadingVouchers" class="py-4 text-center">
                                <div class="spinner-border text-success"></div>
                                <p class="mt-2">Đang tải voucher...</p>
                            </div>

                            <div v-else-if="danhSachVoucher.length === 0" class="text-muted py-4 text-center">
                                <i class="bi bi-ticket display-4"></i>
                                <p class="mt-2">Không có voucher khả dụng</p>
                            </div>

                            <div v-else class="list-group">
                                <button v-for="voucher_item in danhSachVoucher" :key="voucher_item.id" @click="chonVoucher(voucher_item)" class="list-group-item list-group-item-action">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <!-- ✅ THÊM: Ảnh voucher -->
                                        <div class="me-3" style="flex-shrink: 0;">
                                            <img 
                                                v-if="voucher_item.hinhAnh" 
                                                :src="voucher_item.hinhAnh" 
                                                :alt="voucher_item.tenVoucher"
                                                @error="handleVoucherImageError"
                                                class="rounded"
                                                style="width: 60px; height: 60px; object-fit: cover;"
                                            />
                                            <div 
                                                v-else 
                                                class="d-flex align-items-center justify-content-center bg-light rounded"
                                                style="width: 60px; height: 60px;"
                                            >
                                                <i class="bi bi-ticket text-muted"></i>
                                            </div>
                                        </div>
                                        
                                        <div class="flex-grow-1">
                                            <h6 class="fw-bold mb-1">{{ voucher_item.tenVoucher }}</h6>
                                            <p class="text-muted small mb-1">
                                                <i class="bi bi-tag-fill text-success me-1"></i>
                                                <span v-if="voucher_item.loaiGiamGia === 'PHAN_TRAM'"> Giảm {{ voucher_item.giaTriGiam }}% </span>
                                                <span v-else> Giảm {{ formatPrice(voucher_item.giaTriGiam) }} </span>
                                            </p>
                                        </div>
                                        <div class="text-end">
                                            <span v-if="voucher_item.loaiGiamGia === 'PHAN_TRAM'" class="badge bg-primary"> -{{ voucher_item.giaTriGiam }}% </span>
                                            <span v-else class="badge bg-success"> -{{ formatPrice(voucher_item.giaTriGiam) }} </span>
                                        </div>
                                    </div>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Payment Modal -->
        <!-- Payment Modal - CẬP NHẬT -->
        <div v-if="showPaymentModal" class="modal d-block" tabindex="-1" style="background-color: rgba(0, 0, 0, 0.5)">
            <div class="modal-dialog modal-xl">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">
                            <i class="bi bi-credit-card me-2"></i>
                            Thanh toán
                        </h5>
                        <button @click="showPaymentModal = false" class="btn-close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <div class="col-md-6">
                                <h5 class="mb-3">Thông tin thanh toán</h5>

                                <!-- Phương thức thanh toán display -->
                                <div class="mb-3">
                                    <label class="form-label">Phương thức thanh toán</label>
                                    <div class="alert alert-info small">
                                        <i class="bi bi-info-circle me-1"></i>
                                        {{ formatPhuongThucThanhToan(phuongThucThanhToanHienTai) }}
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label">Tổng tiền</label>
                                    <input type="text" :value="formatPrice(tongQuan.tongTienThanhToan)" readonly class="form-control bg-light fw-bold text-danger" />
                                </div>

                                <!-- Tiền mặt -->
                                <div class="mb-3">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <label class="form-label mb-0">Tiền mặt</label>
                                        <div class="btn-group btn-group-sm">
                                            <button @click="chonTienVuaVua" type="button" class="btn btn-outline-primary">Vừa vặn</button>
                                            <button @click="chonChuyenKhoanDayDu" type="button" class="btn btn-outline-success">Chuyển khoản hết</button>
                                        </div>
                                    </div>
                                    <input v-model.number="thongTinThanhToan.tienMat" type="number" min="0" class="form-control mt-1" placeholder="Nhập số tiền mặt" />
                                </div>

                                <!-- Chuyển khoản -->
                                <div class="mb-3">
                                    <label class="form-label">Chuyển khoản</label>
                                    <input v-model.number="thongTinThanhToan.tienChuyenKhoan" type="number" min="0" class="form-control" placeholder="Nhập số tiền chuyển khoản" />
                                </div>

                                <!-- Điểm tích lũy -->
                                <div v-if="false" class="mb-3">
                                    <label class="form-label"> Sử dụng điểm ({{ khachHang.diemTichLuy }} điểm có sẵn) </label>
                                    <div class="input-group">
                                        <input v-model.number="thongTinThanhToan.diemSuDung" type="number" min="0" :max="khachHang.diemTichLuy" class="form-control" placeholder="Nhập số điểm sử dụng" />
                                        <span class="input-group-text">điểm</span>
                                    </div>
                                    <small class="text-muted"> 1 điểm = 1.000₫. Giá trị quy đổi: {{ formatPrice((thongTinThanhToan.diemSuDung || 0) * 1000) }} </small>
                                </div>

                                <!-- Ghi chú -->
                                <div class="mb-3">
                                    <label class="form-label">Ghi chú</label>
                                    <textarea v-model="thongTinThanhToan.ghiChu" class="form-control" rows="3" placeholder="Ghi chú thêm (tùy chọn)"></textarea>
                                </div>

                                <!-- Quick amount buttons - cập nhật để reset chuyển khoản -->
                                <div class="mb-3">
                                    <label class="form-label">Tiền mặt nhanh:</label>
                                    <div class="d-flex flex-wrap gap-2">
                                        <button v-for="amount in quickAmounts" :key="amount" @click="chonTienNhanh(amount)" class="btn btn-outline-primary btn-sm">
                                            {{ formatPrice(amount) }}
                                        </button>
                                    </div>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <h5 class="mb-3">Tóm tắt đơn hàng</h5>
                                <div class="card">
                                    <div class="card-body">
                                        <!-- Tóm tắt đơn hàng -->
                                        <div class="d-flex justify-content-between mb-2">
                                            <span>Tạm tính:</span>
                                            <span>{{ formatPrice(tongQuan.tongTienGoc) }}</span>
                                        </div>
                                        <div v-if="voucher" class="d-flex justify-content-between text-success mb-2">
                                            <span>Voucher:</span>
                                            <span>-{{ formatPrice(tongQuan.tongTienVoucher) }}</span>
                                        </div>
                                        <div v-if="false" class="d-flex justify-content-between text-info mb-2">
                                            <span>Điểm tích lũy:</span>
                                            <span>-{{ formatPrice(thongTinThanhToan.diemSuDung * 1000) }}</span>
                                        </div>
                                        <hr />
                                        <div class="d-flex justify-content-between fw-bold h5 mb-3">
                                            <span>Tổng thanh toán:</span>
                                            <span class="text-danger">{{ formatPrice(tinhTongThanhToan()) }}</span>
                                        </div>

                                        <hr />

                                        <!-- Chi tiết thanh toán -->
                                        <div class="payment-summary">
                                            <h6 class="fw-bold mb-3">Chi tiết thanh toán:</h6>

                                            <div v-if="thongTinThanhToan.tienMat > 0" class="d-flex justify-content-between mb-2">
                                                <span>Tiền mặt:</span>
                                                <span class="fw-semibold">{{ formatPrice(thongTinThanhToan.tienMat) }}</span>
                                            </div>

                                            <div v-if="thongTinThanhToan.tienChuyenKhoan > 0" class="d-flex justify-content-between mb-2">
                                                <span>Chuyển khoản:</span>
                                                <span class="fw-semibold">{{ formatPrice(thongTinThanhToan.tienChuyenKhoan) }}</span>
                                            </div>

                                            <div class="d-flex justify-content-between mb-2">
                                                <span>Tổng tiền nhận:</span>
                                                <span class="fw-bold">{{ formatPrice(thongTinThanhToan.tienMat + thongTinThanhToan.tienChuyenKhoan) }}</span>
                                            </div>

                                            <hr />

                                            <div class="d-flex justify-content-between fs-5" :class="tinhTienThua() >= 0 ? 'text-success' : 'text-danger'">
                                                <span class="fw-bold">{{ getTienThuaDisplay() }}</span>
                                                <span class="fw-bold">{{ formatPrice(Math.abs(tinhTienThua())) }}</span>
                                            </div>
                                        </div>

                                        <!-- Validation errors -->
                                        <div v-if="validateThanhToan().length > 0" class="alert alert-warning mt-3">
                                            <small>
                                                <i class="bi bi-exclamation-triangle me-1"></i>
                                                {{ validateThanhToan()[0] }}
                                            </small>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button @click="showPaymentModal = false" class="btn btn-secondary">Hủy</button>
                        <button @click="xuLyThanhToan" :disabled="validateThanhToan().length > 0 || loadingPayment" class="btn btn-success">
                            <span v-if="loadingPayment" class="spinner-border spinner-border-sm me-2"></span>
                            {{ loadingPayment ? 'Đang xử lý...' : 'Xác nhận thanh toán' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- QR Scanner Modal -->
        <div v-if="showQrScanner" class="modal d-block" tabindex="-1" style="background-color: rgba(0, 0, 0, 0.5)">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">
                            <i class="bi bi-qr-code-scan me-2"></i>
                            Quét mã QR
                        </h5>
                        <button @click="showQrScanner = false" class="btn-close"></button>
                    </div>
                    <div class="modal-body">
                        <ul class="nav nav-tabs mb-3">
                            <li class="nav-item">
                                <button class="nav-link" :class="{ active: qrMode === 'manual' }" @click="switchQrMode('manual')">
                                    <i class="bi bi-keyboard me-1"></i>
                                    Nhập thủ công
                                </button>
                            </li>
                            <li class="nav-item">
                                <button class="nav-link" :class="{ active: qrMode === 'camera' }" @click="switchQrMode('camera')">
                                    <i class="bi bi-camera me-1"></i>
                                    Quét bằng camera
                                </button>
                            </li>
                        </ul>

                        <div v-if="qrMode === 'manual'" class="mb-3">
                            <label class="form-label">Mã QR sản phẩm</label>
                            <input ref="qrInput" v-model="qrCode" type="text" placeholder="Nhập mã QR sản phẩm" class="form-control" @keyup.enter="quetQR" />
                        </div>

                        <div v-if="qrMode === 'camera'" class="mb-3">
                            <div class="rounded border p-2" style="min-height: 300px">
                                <div v-if="!cameraStarted && !cameraError" class="py-5 text-center">
                                    <i class="bi bi-camera display-4 text-muted"></i>
                                    <p class="text-muted mt-3">Nhấn "Bắt đầu quét" để khởi động camera</p>
                                    <button @click="startCamera" class="btn btn-success">
                                        <i class="bi bi-play me-1"></i>
                                        Bắt đầu quét
                                    </button>
                                </div>

                                <div v-show="cameraStarted && !cameraError" class="position-relative text-center">
                                    <video id="qr-video" autoplay playsinline muted style="width: 100%; max-width: 500px; border-radius: 8px; display: block; margin: 0 auto"></video>
                                    <canvas id="qr-canvas" style="position: absolute; top: 0; left: 50%; transform: translateX(-50%); max-width: 500px; width: 100%; pointer-events: none"></canvas>

                                    <div v-if="qrCode" class="alert alert-success mt-2">
                                        <i class="bi bi-check-circle me-2"></i>
                                        Đã phát hiện mã: {{ qrCode }}
                                    </div>

                                    <div class="mt-3">
                                        <button @click="stopCamera" class="btn btn-danger btn-sm">
                                            <i class="bi bi-stop me-1"></i>
                                            Dừng camera
                                        </button>
                                    </div>
                                </div>

                                <div v-if="cameraError" class="text-danger py-5 text-center">
                                    <i class="bi bi-exclamation-triangle display-4"></i>
                                    <p class="mt-3">{{ cameraError }}</p>
                                    <button @click="retryCamera" class="btn btn-warning btn-sm">
                                        <i class="bi bi-arrow-clockwise me-1"></i>
                                        Thử lại
                                    </button>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex gap-2">
                            <button @click="quetQR" :disabled="!qrCode.trim() || loadingQR" class="btn btn-primary flex-grow-1">
                                <span v-if="loadingQR" class="spinner-border spinner-border-sm me-2"></span>
                                <i v-else class="bi bi-search me-1"></i>
                                {{ loadingQR ? 'Đang tìm...' : 'Tìm sản phẩm' }}
                            </button>
                            <button v-if="qrCode" @click="clearQrCode" class="btn btn-outline-secondary">
                                <i class="bi bi-x-lg"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Product Detail Modal -->
        <div v-if="showProductDetail" class="modal d-block" tabindex="-1" style="background-color: rgba(0, 0, 0, 0.5)">
            <div class="modal-dialog modal-xl">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">
                            <i class="bi bi-eye me-2"></i>
                            Chi tiết sản phẩm
                        </h5>
                        <button @click="showProductDetail = false" class="btn-close"></button>
                    </div>
                    <div class="modal-body" v-if="sanPhamDangXem">
                        <div class="row">
                            <div class="col-md-6">
                                <img :src="getProductImage(sanPhamDangXem)" :alt="sanPhamDangXem.tenSanPham" class="img-fluid rounded shadow" />
                            </div>
                            <div class="col-md-6">
                                <h2 class="mb-3">{{ sanPhamDangXem.tenSanPham }}</h2>
                                <h3 class="text-danger mb-3">{{ formatPrice(sanPhamDangXem.giaBan) }}</h3>

                                <div class="row mb-4">
                                    <div class="col-sm-6">
                                        <label class="form-label text-muted">Màu sắc</label>
                                        <div class="d-flex align-items-center">
                                            <div
                                                class="rounded-circle me-2"
                                                :style="{
                                                    backgroundColor: getMauHex(sanPhamDangXem.mauSac?.tenMau),
                                                    width: '24px',
                                                    height: '24px',
                                                    border: '2px solid #fff',
                                                    boxShadow: '0 0 0 1px rgba(0,0,0,0.1)'
                                                }"
                                            ></div>
                                            <span>{{ sanPhamDangXem.mauSac?.tenMau || 'Chưa có' }}</span>
                                        </div>
                                    </div>
                                    <div class="col-sm-6">
                                        <label class="form-label text-muted">Kích cỡ</label>
                                        <div>
                                            <span class="badge bg-light text-dark fs-6">
                                                {{ sanPhamDangXem.kichCo?.tenKichCo || 'Chưa có' }}
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div class="mb-4">
                                    <label class="form-label">Số lượng</label>
                                    <div class="d-flex align-items-center gap-2" style="width: 160px">
                                        <button @click="giamSoLuongModal" :disabled="soLuongChon <= 1" class="btn btn-outline-secondary rounded-circle" style="width: 32px; height: 32px; padding: 0">
                                            <i class="bi bi-dash"></i>
                                        </button>
                                        <input v-model.number="soLuongChon" type="number" min="1" :max="sanPhamDangXem.soLuong" class="form-control text-center" />
                                        <button @click="tangSoLuongModal" :disabled="Number.isFinite(sanPhamDangXem?.soLuong) && soLuongChon >= sanPhamDangXem.soLuong" class="btn btn-outline-secondary rounded-circle" style="width: 32px; height: 32px; padding: 0">
                                            <i class="bi bi-plus"></i>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button @click="showProductDetail = false" class="btn btn-secondary">Đóng</button>
                        <button @click="themVaoHoaDonTuModal" :disabled="!sanPhamDangXem || sanPhamDangXem.soLuong <= 0" class="btn btn-primary">
                            <i class="bi bi-cart-plus me-1"></i>
                            Thêm {{ soLuongChon }} vào hóa đơn
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Create Customer Form Modal -->
        <div v-if="showCreateCustomerForm" class="modal d-block" tabindex="-1" style="background-color: rgba(0, 0, 0, 0.5)">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">
                            <i class="bi bi-person-plus me-2"></i>
                            Tạo khách hàng mới
                        </h5>
                        <button @click="showCreateCustomerForm = false" class="btn-close"></button>
                    </div>
                    <div class="modal-body">
                        <!-- Thông tin cơ bản -->
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label"> Họ tên <span class="text-danger">*</span> </label>
                                <input v-model.trim="newCustomer.hoTen" type="text" class="form-control" :class="{ 'is-invalid': newCustomerErrors.hoTen }" placeholder="Nhập họ và tên khách hàng" maxlength="100" :disabled="loadingCreateCustomer" />
                                <div v-if="newCustomerErrors.hoTen" class="invalid-feedback">
                                    {{ newCustomerErrors.hoTen }}
                                </div>
                            </div>

                            <div class="col-md-6 mb-3">
                                <label class="form-label"> Số điện thoại <span class="text-danger">*</span> </label>
                                <input
                                    v-model.trim="newCustomer.sdt"
                                    type="tel"
                                    class="form-control"
                                    :class="{ 'is-invalid': newCustomerErrors.sdt }"
                                    placeholder="Nhập số điện thoại (VD: 0901234567)"
                                    maxlength="11"
                                    :disabled="loadingCreateCustomer"
                                    @input="validatePhoneInput"
                                />
                                <div v-if="newCustomerErrors.sdt" class="invalid-feedback">
                                    {{ newCustomerErrors.sdt }}
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-12 mb-3">
                                <label class="form-label">Email</label>
                                <input v-model.trim="newCustomer.email" type="email" class="form-control" :class="{ 'is-invalid': newCustomerErrors.email }" placeholder="Nhập email (tùy chọn)" maxlength="255" :disabled="loadingCreateCustomer" />
                                <div v-if="newCustomerErrors.email" class="invalid-feedback">
                                    {{ newCustomerErrors.email }}
                                </div>
                            </div>
                        </div>

                        <!-- Trạng thái loading API -->
                        <div v-if="loadingAddress" class="alert alert-info">
                            <div class="d-flex align-items-center">
                                <div class="spinner-border spinner-border-sm me-3 text-primary"></div>
                                <div>
                                    <strong>Đang tải dữ liệu địa chỉ...</strong><br />
                                    <small class="text-muted">Sử dụng API AddressKit 2025 với fallback</small>
                                </div>
                            </div>
                        </div>

                        <!-- Debug info (có thể ẩn trong production) -->
                        <div v-if="newCustomer.tinhId" class="alert alert-light small">
                            <strong>🔧 Debug:</strong>
                            Tỉnh ID: <code>{{ newCustomer.tinhId }}</code
                            >, Xã ID: <code>{{ newCustomer.xaId || 'Chưa chọn' }}</code
                            >, API: <span class="badge bg-info">Cas AddressKit 2025</span>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button @click="showCreateCustomerForm = false" class="btn btn-secondary">Hủy</button>
                        <button @click="taoKhachHangMoi" :disabled="loadingCreateCustomer || loadingAddress" class="btn btn-primary">
                            <span v-if="loadingCreateCustomer" class="spinner-border spinner-border-sm me-2"></span>
                            <i v-else class="bi bi-person-plus me-1"></i>
                            {{ loadingCreateCustomer ? 'Đang tạo...' : 'Tạo khách hàng' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Invoice Print Modal -->
        <div v-if="showInvoicePrint" class="modal d-block" tabindex="-1" style="background-color: rgba(0, 0, 0, 0.5)">
            <div class="modal-dialog modal-fullscreen">
                <div class="modal-content">
                    <div class="modal-header no-print">
                        <h5 class="modal-title">
                            <i class="bi bi-printer me-2"></i>
                            Xem trước hóa đơn
                        </h5>
                        <button @click="showInvoicePrint = false" class="btn-close"></button>
                    </div>
                    <div class="modal-body p-0">
                        <div id="invoice-print" class="invoice-print">
                            <!-- Header chuyên nghiệp -->
                            <div class="invoice-header">
                                <table class="header-table">
                                    <tr>
                                        <td class="company-section">
                                            <div class="company-logo">
                                                <h1 class="company-name">BEE SHOES STORE</h1>
                                                <div class="company-tagline">Giày chất lượng - Phong cách hiện đại</div>
                                            </div>
                                            <div class="company-info">
                                                <div class="info-row"><strong>Địa chỉ:</strong> 123 Đường ABC, Quận XYZ, TP.HCM</div>
                                                <div class="info-row"><strong>Điện thoại:</strong> 0123-456-789</div>
                                                <div class="info-row"><strong>Email:</strong> contact@beeshoes.com</div>
                                                <div class="info-row"><strong>Website:</strong> www.beeshoes.com</div>
                                            </div>
                                        </td>
                                        <td class="invoice-title-section">
                                            <div class="invoice-title-box">
                                                <h1 class="main-title">HÓA ĐƠN BÁN HÀNG</h1>
                                                <div class="invoice-code">
                                                    {{ hoaDonDaThanhToan?.maHoaDon || 'HD' + Date.now() }}
                                                </div>
                                            </div>
                                        </td>
                                        <td class="invoice-meta-section">
                                            <table class="meta-table">
                                                <tr>
                                                    <td class="meta-label">Ngày lập:</td>
                                                    <td class="meta-value">{{ formatDate(new Date()) }}</td>
                                                </tr>
                                                <tr>
                                                    <td class="meta-label">Giờ lập:</td>
                                                    <td class="meta-value">{{ new Date().toLocaleTimeString('vi-VN') }}</td>
                                                </tr>
                                                <tr>
                                                    <td class="meta-label">Nhân viên:</td>
                                                    <td class="meta-value">{{ nhanVienInfo.tenNhanVien }}</td>
                                                </tr>
                                                <tr>
                                                    <td class="meta-label">Mã NV:</td>
                                                    <td class="meta-value">{{ nhanVienInfo.maNhanVien }}</td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                </table>
                            </div>

                            <!-- Thông tin khách hàng và thanh toán -->
                            <div class="customer-payment-section">
                                <table class="info-table">
                                    <tr>
                                        <td class="customer-info">
                                            <div class="section-header">THÔNG TIN KHÁCH HÀNG</div>
                                            <table class="detail-table">
                                                <tr>
                                                    <td class="label">Tên khách hàng:</td>
                                                    <td class="value">{{ thongTinThanhToanCuoi?.khachHangInfo?.hoTen || 'Khách lẻ' }}</td>
                                                </tr>
                                                <tr>
                                                    <td class="label">Số điện thoại:</td>
                                                    <td class="value">{{ thongTinThanhToanCuoi?.khachHangInfo?.sdt || 'N/A' }}</td>
                                                </tr>
                                                <tr>
                                                    <td class="label">Địa chỉ:</td>
                                                    <td class="value">{{ thongTinThanhToanCuoi?.khachHangInfo?.diaChi || 'N/A' }}</td>
                                                </tr>
                                            </table>
                                        </td>
                                        <td class="payment-info">
                                            <div class="section-header">PHƯƠNG THỨC THANH TOÁN</div>
                                            <table class="detail-table">
                                                <tr>
                                                    <td class="label">Phương thức:</td>
                                                    <td class="value">{{ formatPhuongThucThanhToan(thongTinThanhToanCuoi?.phuongThucThanhToan || 'TIEN_MAT') }}</td>
                                                </tr>
                                                <tr v-if="thongTinThanhToanCuoi?.tienMat > 0">
                                                    <td class="label">Tiền mặt:</td>
                                                    <td class="value amount">{{ formatPrice(thongTinThanhToanCuoi.tienMat) }}</td>
                                                </tr>
                                                <tr v-if="thongTinThanhToanCuoi?.tienChuyenKhoan > 0">
                                                    <td class="label">Chuyển khoản:</td>
                                                    <td class="value amount">{{ formatPrice(thongTinThanhToanCuoi.tienChuyenKhoan) }}</td>
                                                </tr>
                                                <tr v-if="false">
                                                    <td class="label">Điểm tích lũy:</td>
                                                    <td class="value amount discount">{{ thongTinThanhToanCuoi.diemSuDung }} điểm</td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                </table>
                            </div>

                            <!-- Bảng sản phẩm -->
                            <div class="products-section">
                                <div class="section-header">CHI TIẾT SẢN PHẨM</div>
                                <table class="products-table">
                                    <thead>
                                        <tr>
                                            <th class="stt">STT</th>
                                            <th class="ma-sp">MÃ SẢN PHẨM</th>
                                            <th class="ten-sp">TÊN SẢN PHẨM</th>
                                            <th class="mau-sac">MÀU SẮC</th>
                                            <th class="kich-co">SIZE</th>
                                            <th class="so-luong">SỐ LƯỢNG</th>
                                            <th class="don-gia">ĐƠN GIÁ</th>
                                            <th class="thanh-tien">THÀNH TIỀN</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr v-for="(item, index) in sanPhamDaThanhToan" :key="index" class="product-row">
                                            <td class="text-center">{{ index + 1 }}</td>
                                            <td class="text-center">{{ item.maSanPham || 'SP' + item.chiTietSanPhamId }}</td>
                                            <td class="product-name">{{ item.tenSanPham }}</td>
                                            <td class="text-center">{{ item.mauSac?.tenMau || 'N/A' }}</td>
                                            <td class="text-center">{{ item.kichCo?.tenKichCo || 'N/A' }}</td>
                                            <td class="text-center">{{ item.soLuongDaChon }}</td>
                                            <td class="text-right">{{ formatPrice(item.giaBan) }}</td>
                                            <td class="amount text-right">{{ formatPrice(item.giaBan * item.soLuongDaChon) }}</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>

                            <!-- Phần tổng kết thanh toán -->
                            <div class="summary-section">
                                <table class="summary-container">
                                    <tr>
                                        <td class="left-content">
                                            <div v-if="thongTinThanhToanCuoi?.thongBao" class="success-message">
                                                <div class="message-icon">✓</div>
                                                <div class="message-text">{{ thongTinThanhToanCuoi.thongBao }}</div>
                                            </div>
                                        </td>
                                        <td class="right-content">
                                            <table class="summary-table">
                                                <tr class="summary-row">
                                                    <td class="summary-label">Tạm tính:</td>
                                                    <td class="summary-value">{{ formatPrice(tongQuanDaThanhToan?.tongTienGoc || 0) }}</td>
                                                </tr>
                                                <tr v-if="voucherDaThanhToan" class="summary-row discount-row">
                                                    <td class="summary-label">Voucher ({{ voucherDaThanhToan.tenVoucher }}):</td>
                                                    <td class="summary-value discount">-{{ formatPrice(tongQuanDaThanhToan?.tongTienVoucher || 0) }}</td>
                                                </tr>
                                                <tr v-if="thongTinThanhToanCuoi?.diemSuDung > 0" class="summary-row discount-row">
                                                    <td class="summary-label">Điểm tích lũy ({{ thongTinThanhToanCuoi.diemSuDung }} điểm):</td>
                                                    <td class="summary-value discount">-{{ formatPrice(thongTinThanhToanCuoi?.giaTriDiem || thongTinThanhToanCuoi.diemSuDung * 1000) }}</td>
                                                </tr>
                                                <tr class="total-separator">
                                                    <td colspan="2"><div class="separator-line"></div></td>
                                                </tr>
                                                <tr class="total-row">
                                                    <td class="summary-label total-label">TỔNG THANH TOÁN:</td>
                                                    <td class="summary-value total-value">{{ formatPrice(tongQuanDaThanhToan?.tongTienThanhToan || 0) }}</td>
                                                </tr>
                                                <tr class="received-row">
                                                    <td class="summary-label">Tiền nhận:</td>
                                                    <td class="summary-value">{{ formatPrice((thongTinThanhToanCuoi.tienMat || 0) + (thongTinThanhToanCuoi.tienChuyenKhoan || 0)) }}</td>
                                                </tr>
                                                <tr class="change-row" :class="(thongTinThanhToanCuoi.tienThua || 0) >= 0 ? 'positive' : 'negative'">
                                                    <td class="summary-label change-label">
                                                        {{ (thongTinThanhToanCuoi.tienThua || 0) > 0 ? 'Tiền thừa:' : (thongTinThanhToanCuoi.tienThua || 0) < 0 ? 'Còn thiếu:' : 'Vừa vặn:' }}
                                                    </td>
                                                    <td class="summary-value change-value">
                                                        {{ formatPrice(Math.abs(thongTinThanhToanCuoi.tienThua || 0)) }}
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                </table>
                            </div>

                            <!-- Footer với chữ ký -->
                            <div class="footer-section">
                                <table class="signature-table">
                                    <tr>
                                        <td class="signature-cell">
                                            <div class="signature-title">Khách hàng</div>
                                            <div class="signature-line"></div>
                                            <div class="signature-note">(Ký và ghi rõ họ tên)</div>
                                        </td>
                                        <td class="center-cell">
                                            <div class="thank-message">
                                                <div class="thank-title">Cảm ơn Quý khách!</div>
                                                <div class="thank-subtitle">Hẹn gặp lại!</div>
                                                <div class="store-motto">"Mỗi bước chân đều tự tin"</div>
                                            </div>
                                        </td>
                                        <td class="signature-cell">
                                            <div class="signature-title">Nhân viên bán hàng</div>
                                            <div class="signature-line"></div>
                                            <div class="signature-note">{{ nhanVienInfo.tenNhanVien }}</div>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer no-print">
                        <button @click="printInvoice" class="btn btn-success">
                            <i class="bi bi-printer me-1"></i>
                            In hóa đơn
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Voucher Detail Modal -->
        <div v-if="showVoucherDetailModal" class="modal d-block" tabindex="-1" style="background-color: rgba(0, 0, 0, 0.5)">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">
                            <i class="bi bi-ticket-detailed me-2"></i>
                            Chi tiết voucher đã áp dụng
                        </h5>
                        <button @click="showVoucherDetailModal = false" class="btn-close"></button>
                    </div>
                    <div class="modal-body">
                        <!-- Loading State -->
                        <div v-if="loadingChiTietVoucher" class="py-4 text-center">
                            <div class="spinner-border text-primary"></div>
                            <p class="mt-2">Đang tải chi tiết voucher...</p>
                        </div>

                        <!-- Empty State -->
                        <div v-else-if="chiTietVoucherDaApDung.length === 0" class="py-4 text-center">
                            <i class="bi bi-ticket display-4 text-muted"></i>
                            <h5 class="mt-3">Chưa áp dụng voucher nào</h5>
                            <p class="text-muted">Hóa đơn này chưa có voucher được áp dụng</p>
                        </div>

                        <!-- Voucher Details -->
                        <div v-else>
                            <div v-for="(detail, index) in chiTietVoucherDaApDung" :key="detail.id || index" class="card mb-3">
                                <div class="card-body">
                                    <div class="row">
                                        <div class="col-md-6">
                                            <h6 class="card-title text-primary">
                                                <i class="bi bi-ticket-fill me-2"></i>
                                                {{ detail.tenVoucher }}
                                            </h6>
                                            <p class="mb-2">
                                                <strong>Mã voucher:</strong>
                                                <code class="bg-light rounded px-2 py-1">
                                                    {{ detail.maVoucher }}
                                                </code>
                                            </p>
                                            <p class="mb-2">
                                                <strong>Loại giảm giá:</strong>
                                                <span v-if="detail.loaiGiamGia === 'PHAN_TRAM'" class="badge bg-warning text-dark">
                                                    <i class="bi bi-percent me-1"></i>
                                                    Phần trăm ({{ detail.giaTriGiam }}%)
                                                </span>
                                                <span v-else class="badge bg-info">
                                                    <i class="bi bi-cash me-1"></i>
                                                    Tiền mặt ({{ formatPrice(detail.giaTriGiam) }})
                                                </span>
                                            </p>
                                            <p class="mb-0">
                                                <strong>Ngày áp dụng:</strong>
                                                <span class="text-muted">
                                                    {{ formatDateTime(detail.ngayApDung) }}
                                                </span>
                                            </p>
                                        </div>

                                        <div class="col-md-6">
                                            <div class="bg-light rounded p-3">
                                                <h6 class="mb-3">
                                                    <i class="bi bi-calculator me-2"></i>
                                                    Thông tin tính toán
                                                </h6>

                                                <div class="d-flex justify-content-between mb-2">
                                                    <span>Giá trị đơn hàng:</span>
                                                    <strong>{{ formatPrice(detail.giaTriDonHang) }}</strong>
                                                </div>

                                                <div class="d-flex justify-content-between text-success mb-2">
                                                    <span>Số tiền giảm:</span>
                                                    <strong>-{{ formatPrice(detail.soTienGiam) }}</strong>
                                                </div>

                                                <hr class="my-2" />

                                                <div class="d-flex justify-content-between fw-bold h6">
                                                    <span>Thành tiền:</span>
                                                    <span class="text-primary">
                                                        {{ formatPrice(detail.thanhTien) }}
                                                    </span>
                                                </div>

                                                <!-- Hiển thị % tiết kiệm -->
                                                <div class="mt-2">
                                                    <small class="text-muted">
                                                        Tiết kiệm:
                                                        <span class="text-success fw-semibold"> {{ ((detail.soTienGiam / detail.giaTriDonHang) * 100).toFixed(1) }}% </span>
                                                    </small>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Tổng kết nếu có nhiều voucher -->
                            <div v-if="chiTietVoucherDaApDung.length > 1" class="alert alert-info">
                                <h6>
                                    <i class="bi bi-info-circle me-2"></i>
                                    Tổng kết
                                </h6>
                                <div class="row">
                                    <div class="col-md-4"><strong>Tổng voucher:</strong> {{ chiTietVoucherDaApDung.length }}</div>
                                    <div class="col-md-4">
                                        <strong>Tổng tiết kiệm:</strong>
                                        <span class="text-success">
                                            {{ formatPrice(chiTietVoucherDaApDung.reduce((sum, v) => sum + (v.soTienGiam || 0), 0)) }}
                                        </span>
                                    </div>
                                    <div class="col-md-4">
                                        <strong>Thành tiền cuối:</strong>
                                        <span class="text-primary">
                                            {{ formatPrice(chiTietVoucherDaApDung.reduce((sum, v) => sum + (v.thanhTien || 0), 0)) }}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button @click="showVoucherDetailModal = false" class="btn btn-secondary">Đóng</button>
                        <button v-if="coChiTietVoucher" @click="layChiTietVoucherDaApDung(hoaDonDangChon.id)" :disabled="loadingChiTietVoucher" class="btn btn-outline-primary">
                            <span v-if="loadingChiTietVoucher" class="spinner-border spinner-border-sm me-2"></span>
                            <i v-else class="bi bi-arrow-clockwise me-1"></i>
                            {{ loadingChiTietVoucher ? 'Đang tải...' : 'Làm mới' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>
        */
    </div>
    <!-- Đóng toast-container -->
    <!-- Đóng main-layout -->
    <!-- Extra closing div removed -->
</template>
<style scoped>
/* =================== GLOBAL STYLES =================== */
.pos-container {
    height: 100vh;
    overflow: hidden;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    background: #f8fafc;
}

.main-layout {
    height: 100vh;
    display: flex;
    flex-direction: column;
}

.main-content {
    flex: 1;
    padding: 1rem 0;
    min-height: 0;
    display: flex;
}

/* =================== HEADER STYLES =================== */
.pos-header {
    flex-shrink: 0;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    background: linear-gradient(135deg, #0d6efd 0%, #0b5ed7 100%) !important;
}

.pos-header h3 {
    font-weight: 700;
    letter-spacing: -0.5px;
}

/* =================== INVOICE TABS =================== */
.invoice-tabs-section {
    flex-shrink: 0;
    border-bottom: 2px solid #e9ecef;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    background: #ffffff;
}

.invoice-tab {
    min-width: 120px;
    position: relative;
    transition: all 0.2s ease;
    white-space: nowrap;
    border-radius: 8px !important;
    font-weight: 500;
    font-size: 0.875rem;
    padding-right: 35px !important; /* Thêm padding để có chỗ cho nút xóa */
}

.invoice-tab:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.invoice-tab:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.invoice-tab.btn-primary {
    background: linear-gradient(135deg, #0d6efd 0%, #0b5ed7 100%);
    border-color: #0d6efd;
    box-shadow: 0 2px 8px rgba(13, 110, 253, 0.3);
}

.btn-xs-delete {
    font-size: 0.7rem;
    line-height: 1;
    padding: 0;
    position: absolute;
    top: 50%;
    right: 8px;
    transform: translateY(-50%);
    z-index: 10;
    background: #dc3545;
    border: 1px solid #dc3545;
    border-radius: 50%;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
}

.btn-xs-delete:hover {
    background: #c82333;
    border-color: #bd2130;
    transform: translateY(-50%) scale(1.1);
}

.btn-xs-delete i {
    font-size: 12px;
}

.btn-xs {
    font-size: 0.7rem;
    line-height: 1;
    padding: 2px;
    position: absolute;
    top: 2px;
    right: 2px;
    z-index: 10;
    background: #fff;
    border-radius: 50%;
    border: 1px solid #e53e3e;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
    width: 18px;
    height: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
}

/* =================== PANEL STYLES =================== */

.left-panel {
    border-right: 1px solid #dee2e6;
}

.right-panel {
    height: 100%;
    min-height: 0;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
}

.left-panel .card,
.right-panel .card {
    border: none;
    box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
    border-radius: 0.75rem;
}

.card-header {
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-bottom: 1px solid #dee2e6;
    font-weight: 600;
    font-size: 0.9rem;
    border-radius: 0.75rem 0.75rem 0 0 !important;
}

/* =================== PRODUCT TABLE =================== */
.table-responsive {
    border-radius: 0.75rem;
    border: 1px solid #dee2e6;
    overflow: hidden;
}

.table {
    margin-bottom: 0;
    font-size: 0.875rem;
}

.table thead th {
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-bottom: 2px solid #dee2e6;
    font-weight: 600;
    text-transform: uppercase;
    font-size: 0.75rem;
    letter-spacing: 0.05em;
    position: sticky;
    top: 0;
    z-index: 10;
    color: #495057;
}

.table tbody tr {
    transition: all 0.15s ease;
}

.table tbody tr:hover {
    background-color: rgba(13, 110, 253, 0.05);
    transform: translateX(2px);
}

.table td {
    vertical-align: middle;
    padding: 0.75rem 0.5rem;
    border-color: #f1f3f4;
}

/* =================== PRODUCT MODAL STYLES =================== */
.product-card {
    transition: all 0.3s ease;
    cursor: pointer;
    border: none;
    box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
    border-radius: 0.75rem;
    overflow: hidden;
}

.product-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 0.5rem 1.5rem rgba(0, 0, 0, 0.15);
}

.product-card .card-img-top {
    transition: transform 0.3s ease;
    border-radius: 0.75rem 0.75rem 0 0;
}

.product-card:hover .card-img-top {
    transform: scale(1.05);
}

.product-card .card-body {
    padding: 1rem;
}

.product-card .card-title {
    font-size: 0.9rem;
    font-weight: 600;
    color: #495057;
}

/* =================== RIGHT PANEL STYLES =================== */
.right-panel .card-header h6 {
    color: #495057;
    font-weight: 600;
}

.voucher-selected {
    background: linear-gradient(135deg, #e8f5e8 0%, #f0f8f0 100%) !important;
    border: 2px solid #28a745 !important;
    box-shadow: 0 0 0 3px rgba(40, 167, 69, 0.1);
    border-radius: 0.5rem;
}

.summary-section {
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    padding: 1rem;
    border-radius: 0.75rem;
    border: 1px solid #e9ecef;
    box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
}

.summary-section hr {
    border-color: #dee2e6;
    opacity: 0.5;
}

/* =================== MODAL STYLES =================== */
.modal {
    backdrop-filter: blur(4px);
}

.modal-content {
    border: none;
    box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
    border-radius: 0.75rem;
    overflow: hidden;
}

.modal-header {
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-bottom: 1px solid #dee2e6;
    border-radius: 0.75rem 0.75rem 0 0;
    padding: 1rem 1.5rem;
}

.modal-title {
    font-weight: 600;
    color: #495057;
    font-size: 1.1rem;
}

.modal-body {
    padding: 1.5rem;
}

.modal-footer {
    padding: 1rem 1.5rem;
    background: #f8f9fa;
    border-top: 1px solid #dee2e6;
}

/* =================== BUTTON STYLES =================== */
.btn {
    border-radius: 0.5rem;
    font-weight: 500;
    transition: all 0.2s ease;
    font-size: 0.875rem;
}

.btn:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.btn-primary {
    background: linear-gradient(135deg, #0d6efd 0%, #0b5ed7 100%);
    border-color: #0d6efd;
    box-shadow: 0 2px 4px rgba(13, 110, 253, 0.2);
}

.btn-success {
    background: linear-gradient(135deg, #198754 0%, #157347 100%);
    border-color: #198754;
    box-shadow: 0 2px 4px rgba(25, 135, 84, 0.2);
}

.btn-danger {
    background: linear-gradient(135deg, #dc3545 0%, #bb2d3b 100%);
    border-color: #dc3545;
    box-shadow: 0 2px 4px rgba(220, 53, 69, 0.2);
}

.btn-warning {
    background: linear-gradient(135deg, #ffc107 0%, #ffb300 100%);
    border-color: #ffc107;
    color: #212529;
    box-shadow: 0 2px 4px rgba(255, 193, 7, 0.2);
}

.btn-outline-primary {
    border: 2px solid #0d6efd;
    color: #0d6efd;
}

.btn-outline-primary:hover {
    background: linear-gradient(135deg, #0d6efd 0%, #0b5ed7 100%);
    border-color: #0d6efd;
    color: white;
}

.btn-outline-success {
    border: 2px solid #198754;
    color: #198754;
}

.btn-outline-success:hover {
    background: linear-gradient(135deg, #198754 0%, #157347 100%);
    border-color: #198754;
    color: white;
}

.btn-outline-danger {
    border: 2px solid #dc3545;
    color: #dc3545;
}

.btn-outline-danger:hover {
    background: linear-gradient(135deg, #dc3545 0%, #bb2d3b 100%);
    border-color: #dc3545;
    color: white;
}

.btn-outline-secondary {
    border: 1px solid #6c757d;
    color: #6c757d;
}

.btn-outline-secondary:hover {
    background: #6c757d;
    border-color: #6c757d;
    color: white;
}

.btn-lg {
    padding: 0.75rem 1.5rem;
    font-size: 1rem;
    font-weight: 600;
    border-radius: 0.75rem;
    border-color: #dc3545;
    color: white;
}

.btn-outline-secondary {
    border: 1px solid #6c757d;
    color: #6c757d;
}

.btn-outline-secondary:hover {
    background: #6c757d;
    border-color: #6c757d;
    color: white;
}

.btn-lg {
    padding: 0.75rem 1.5rem;
    font-size: 1rem;
    font-weight: 600;
    border-radius: 0.75rem;
}

.btn-sm {
    padding: 0.375rem 0.75rem;
    font-size: 0.8rem;
    border-radius: 0.375rem;
}

/* =================== FORM STYLES =================== */
.form-control,
.form-select {
    border-radius: 0.5rem;
    border: 1px solid #ced4da;
    transition: all 0.15s ease-in-out;
    font-size: 0.875rem;
}

.form-control:focus,
.form-select:focus {
    border-color: #86b7fe;
    box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
    transform: translateY(-1px);
}

.form-label {
    font-weight: 600;
    color: #495057;
    font-size: 0.875rem;
    margin-bottom: 0.5rem;
}

.input-group .form-control {
    border-radius: 0.5rem 0 0 0.5rem;
}

.input-group .btn {
    border-radius: 0 0.5rem 0.5rem 0;
}

.is-invalid {
    border-color: #dc3545 !important;
    box-shadow: 0 0 0 0.25rem rgba(220, 53, 69, 0.25) !important;
}

.invalid-feedback {
    display: block;
    width: 100%;
    margin-top: 0.25rem;
    font-size: 0.875em;
    color: #dc3545;
    font-weight: 500;
}

/* =================== BADGE STYLES =================== */
.badge {
    font-weight: 500;
    font-size: 0.75rem;
    padding: 0.375em 0.75em;
    border-radius: 0.375rem;
}

.badge.bg-light {
    color: #495057 !important;
    background-color: #f8f9fa !important;
    border: 1px solid #e9ecef;
}

.badge.bg-primary {
    background: linear-gradient(135deg, #0d6efd 0%, #0b5ed7 100%) !important;
}

.badge.bg-success {
    background: linear-gradient(135deg, #198754 0%, #157347 100%) !important;
}

.badge.bg-danger {
    background: linear-gradient(135deg, #dc3545 0%, #bb2d3b 100%) !important;
}

.badge.bg-warning {
    background: linear-gradient(135deg, #ffc107 0%, #ffb300 100%) !important;
    color: #212529 !important;
}

/* =================== TOAST STYLES =================== */
.toast-container {
    z-index: 1100;
}

.toast {
    border: none;
    border-radius: 0.75rem;
    box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
    min-width: 300px;
    font-weight: 500;
}

.toast.text-bg-success {
    background: linear-gradient(135deg, #198754 0%, #157347 100%) !important;
}

.toast.text-bg-danger {
    background: linear-gradient(135deg, #dc3545 0%, #bb2d3b 100%) !important;
}

.toast.text-bg-warning {
    background: linear-gradient(135deg, #ffc107 0%, #ffb300 100%) !important;
    color: #212529 !important;
}

.toast-body {
    padding: 1rem 1.25rem;
    font-size: 0.9rem;
}

/* =================== LIST GROUP STYLES =================== */
.list-group-item {
    border: 1px solid #dee2e6;
    transition: all 0.15s ease-in-out;
    border-radius: 0.5rem !important;
    margin-bottom: 0.5rem;
}

.list-group-item:last-child {
    margin-bottom: 0;
}

.list-group-item:hover {
    background-color: #f8f9fa;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.list-group-item-action {
    cursor: pointer;
    border-left: 3px solid transparent;
}

.list-group-item-action:hover {
    border-left-color: #0d6efd;
}

/* =================== PAGINATION STYLES =================== */
.pagination {
    gap: 0.5rem;
}

.page-item .page-link {
    border: none;
    border-radius: 0.5rem;
    padding: 0.5rem 0.75rem;
    color: #6c757d;
    background: #ffffff;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    transition: all 0.15s ease;
}

.page-item.active .page-link {
    background: linear-gradient(135deg, #0d6efd 0%, #0b5ed7 100%);
    color: white;
    box-shadow: 0 2px 8px rgba(13, 110, 253, 0.3);
}

.page-item:not(.disabled) .page-link:hover {
    color: #0d6efd;
    background: #e7f1ff;
    transform: translateY(-1px);
}

.page-item.disabled .page-link {
    opacity: 0.5;
    cursor: not-allowed;
}

/* =================== SPINNER STYLES =================== */
.spinner-border-sm {
    width: 1rem;
    height: 1rem;
    border-width: 0.125em;
}

.spinner-border {
    animation: spinner-border-animation 0.75s linear infinite;
}

@keyframes spinner-border-animation {
    0% {
        transform: rotate(0deg);
    }
    100% {
        transform: rotate(360deg);
    }
}

/* =================== QR SCANNER STYLES =================== */
.nav-tabs {
    border-bottom: 2px solid #dee2e6;
}

.nav-tabs .nav-link {
    border: none;
    border-radius: 0.5rem 0.5rem 0 0;
    color: #6c757d;
    font-weight: 500;
    padding: 0.75rem 1.5rem;
    margin-bottom: -2px;
}

.nav-tabs .nav-link.active {
    background: linear-gradient(135deg, #0d6efd 0%, #0b5ed7 100%);
    color: white;
    border-bottom: 2px solid #0d6efd;
}

.nav-tabs .nav-link:hover:not(.active) {
    background: #e7f1ff;
    color: #0d6efd;
}

#qr-video {
    border-radius: 0.75rem;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

/* =================== INVOICE PRINT STYLES =================== */
.invoice-print {
    background: white;
    color: black !important;
    font-family: 'Times New Roman', serif;
    padding: 25px;
    max-width: 1200px;
    margin: 0 auto;
    line-height: 1.5;
    font-size: 13px;
}

/* =================== HEADER SECTION =================== */
.invoice-header {
    border-bottom: 3px double #000;
    padding-bottom: 20px;
    margin-bottom: 20px;
}

.header-table {
    width: 100%;
    border-collapse: collapse;
}

.company-section {
    width: 35%;
    vertical-align: top;
    padding-right: 20px;
}

.invoice-title-section {
    width: 30%;
    text-align: center;
    vertical-align: middle;
    border-left: 1px solid #ccc;
    border-right: 1px solid #ccc;
    padding: 0 20px;
}

.invoice-meta-section {
    width: 35%;
    vertical-align: top;
    padding-left: 20px;
}

.company-logo {
    border-bottom: 2px solid #000;
    padding-bottom: 10px;
    margin-bottom: 15px;
}

.company-name {
    font-size: 24px;
    font-weight: bold;
    color: #000;
    margin: 0;
    letter-spacing: 2px;
    text-transform: uppercase;
}

.company-tagline {
    font-size: 11px;
    color: #666;
    font-style: italic;
    margin-top: 5px;
}

.company-info .info-row {
    margin: 6px 0;
    font-size: 12px;
    color: #000;
}

.invoice-title-box {
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border: 3px solid #000;
    border-radius: 15px;
    padding: 20px;
    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
}

.main-title {
    font-size: 22px;
    font-weight: bold;
    color: #000;
    margin: 0 0 15px 0;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.invoice-code {
    font-size: 18px;
    font-weight: bold;
    color: #d73502;
    background: white;
    border: 2px solid #d73502;
    border-radius: 8px;
    padding: 8px 15px;
    display: inline-block;
}

.meta-table {
    width: 100%;
    border-collapse: collapse;
}

.meta-table .meta-label {
    font-weight: bold;
    width: 40%;
    padding: 5px 0;
    font-size: 12px;
    color: #000;
}

.meta-table .meta-value {
    padding: 5px 0;
    font-size: 12px;
    color: #000;
}

/* =================== CUSTOMER & PAYMENT INFO =================== */
.customer-payment-section {
    margin: 20px 0;
}

.info-table {
    width: 100%;
    border-collapse: collapse;
    border: 2px solid #000;
}

.customer-info,
.payment-info {
    width: 50%;
    vertical-align: top;
    padding: 15px;
    border: 1px solid #000;
}

.section-header {
    background: #f0f0f0;
    color: #000;
    font-weight: bold;
    font-size: 14px;
    padding: 8px 12px;
    margin: -15px -15px 15px -15px;
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 2px solid #000;
}

.detail-table {
    width: 100%;
    border-collapse: collapse;
}

.detail-table .label {
    font-weight: bold;
    width: 40%;
    padding: 5px 0;
    font-size: 12px;
    color: #000;
}

.detail-table .value {
    padding: 5px 0;
    font-size: 12px;
    color: #000;
}

.detail-table .amount {
    font-weight: bold;
    color: #007bff;
}

.detail-table .discount {
    color: #28a745;
}

/* =================== PRODUCTS TABLE =================== */
.products-section {
    margin: 25px 0;
}

.products-table {
    width: 100%;
    border-collapse: collapse;
    border: 2px solid #000;
    margin-top: 10px;
}

.products-table th {
    background: linear-gradient(135deg, #343a40 0%, #495057 100%);
    color: white !important;
    font-weight: bold;
    padding: 12px 8px;
    text-align: center;
    border: 1px solid #000;
    text-transform: uppercase;
    font-size: 11px;
    letter-spacing: 0.5px;
}

.products-table td {
    padding: 10px 8px;
    border: 1px solid #000;
    color: #000;
    vertical-align: middle;
    font-size: 12px;
}

.products-table .stt {
    width: 5%;
}
.products-table .ma-sp {
    width: 12%;
}
.products-table .ten-sp {
    width: 28%;
}
.products-table .mau-sac {
    width: 10%;
}
.products-table .kich-co {
    width: 8%;
}
.products-table .so-luong {
    width: 8%;
}
.products-table .don-gia {
    width: 14%;
}
.products-table .thanh-tien {
    width: 15%;
}

.product-row:nth-child(even) {
    background: #f8f9fa;
}

.product-name {
    font-weight: 500;
    line-height: 1.3;
}

.text-center {
    text-align: center;
}
.text-right {
    text-align: right;
}
.amount {
    font-weight: bold;
    color: #007bff;
}

/* =================== SUMMARY SECTION =================== */
.summary-section {
    margin: 25px 0;
}

.summary-container {
    width: 100%;
    border-collapse: collapse;
}

.left-content {
    width: 50%;
    vertical-align: top;
    padding-right: 20px;
}

.right-content {
    width: 50%;
    vertical-align: top;
}

.success-message {
    background: #d4edda;
    border: 2px solid #c3e6cb;
    border-radius: 10px;
    padding: 15px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
}

.message-icon {
    background: #28a745;
    color: white;
    width: 30px;
    height: 30px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 18px;
    margin-right: 15px;
}

.message-text {
    color: #155724;
    font-weight: 500;
    font-size: 14px;
}

.qr-section {
    text-align: center;
}

.qr-placeholder {
    border: 2px dashed #ccc;
    border-radius: 10px;
    padding: 20px;
    background: #f8f9fa;
}

.qr-title {
    font-size: 12px;
    color: #666;
    margin-bottom: 10px;
}

.qr-box {
    width: 80px;
    height: 80px;
    border: 2px solid #000;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: center;
    background: white;
}

.qr-content {
    font-size: 24px;
    font-weight: bold;
    color: #000;
}

.summary-table {
    width: 100%;
    border-collapse: collapse;
    border: 2px solid #000;
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}

.summary-row {
    border-bottom: 1px solid #dee2e6;
}

.summary-label {
    padding: 10px 15px;
    font-weight: 500;
    color: #000;
    border-right: 1px solid #dee2e6;
    width: 60%;
    font-size: 13px;
}

.summary-value {
    padding: 10px 15px;
    text-align: right;
    font-weight: bold;
    color: #000;
    font-size: 13px;
}

.discount-row .summary-value {
    color: #28a745;
}

.total-separator {
    height: 3px;
    background: #000;
}

.separator-line {
    height: 3px;
    background: linear-gradient(90deg, #000 0%, #666 50%, #000 100%);
}

.total-row {
    background: #ffe4b5;
    border: 2px solid #000;
}

.total-label {
    font-size: 16px;
    font-weight: bold;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.total-value {
    font-size: 18px;
    font-weight: bold;
    color: #d73502;
}

.received-row {
    background: #e7f3ff;
}

.received-row .summary-value {
    color: #007bff;
}

.change-row {
    border: 2px solid #000;
}

.change-row.positive {
    background: #d4edda;
}

.change-row.positive .summary-value {
    color: #28a745;
}

.change-row.negative {
    background: #f8d7da;
}

.change-row.negative .summary-value {
    color: #dc3545;
}

.change-label,
.change-value {
    font-size: 14px;
    font-weight: bold;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

/* =================== FOOTER SECTION =================== */
.footer-section {
    margin-top: 40px;
    border-top: 2px solid #000;
    padding-top: 25px;
}

.signature-table {
    width: 100%;
    border-collapse: collapse;
}

.signature-cell {
    width: 30%;
    text-align: center;
    vertical-align: top;
    padding: 0 20px;
}

.center-cell {
    width: 40%;
    text-align: center;
    vertical-align: middle;
    border-left: 1px dashed #ccc;
    border-right: 1px dashed #ccc;
    padding: 0 20px;
}

.signature-title {
    font-weight: bold;
    font-size: 14px;
    color: #000;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 40px;
}

.signature-line {
    height: 2px;
    background: #000;
    margin: 40px 10px 15px 10px;
}

.signature-note {
    font-size: 11px;
    color: #666;
    font-style: italic;
    margin-top: 10px;
}

.thank-message {
    padding: 20px;
    background: linear-gradient(135deg, #fff9e6 0%, #ffe4b5 100%);
    border: 2px solid #ffc107;
    border-radius: 15px;
}

.thank-title {
    font-size: 18px;
    font-weight: bold;
    color: #000;
    margin-bottom: 8px;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.thank-subtitle {
    font-size: 14px;
    color: #666;
    margin-bottom: 10px;
}

.store-motto {
    font-size: 12px;
    color: #d73502;
    font-style: italic;
    font-weight: 500;
    border-top: 1px solid #ffc107;
    padding-top: 10px;
    margin-top: 10px;
}

/* =================== PRINT STYLES =================== */
@media print {
    @page {
        size: A4 landscape;
        margin: 10mm 15mm;
    }

    .no-print {
        display: none !important;
    }

    .modal-body {
        padding: 0 !important;
        margin: 0 !important;
    }

    .modal-content,
    .modal-dialog {
        border: none !important;
        box-shadow: none !important;
        margin: 0 !important;
        max-width: 100% !important;
        width: 100% !important;
        height: 100% !important;
    }

    .invoice-print {
        padding: 15mm;
        margin: 0;
        max-width: 100%;
        width: 100%;
        height: auto;
        background: white !important;
        color: black !important;
        font-size: 11px;
        -webkit-print-color-adjust: exact;
        color-adjust: exact;
    }

    .company-name {
        font-size: 20px;
    }

    .main-title {
        font-size: 18px;
    }

    .invoice-code {
        font-size: 14px;
    }

    .products-table {
        font-size: 10px;
    }

    .products-table th {
        font-size: 9px;
        padding: 8px 6px;
    }

    .products-table td {
        padding: 6px 5px;
    }

    .summary-label,
    .summary-value {
        font-size: 11px;
        padding: 8px 12px;
    }

    .total-label {
        font-size: 14px;
    }

    .total-value {
        font-size: 15px;
    }

    .change-label,
    .change-value {
        font-size: 12px;
    }

    .thank-title {
        font-size: 16px;
    }

    .thank-subtitle {
        font-size: 12px;
    }

    .store-motto {
        font-size: 10px;
    }

    .signature-title {
        font-size: 12px;
    }

    .signature-note {
        font-size: 9px;
    }

    .message-text {
        font-size: 12px;
    }

    .qr-title {
        font-size: 10px;
    }

    /* Đảm bảo không bị cắt trang */
    .invoice-header,
    .customer-payment-section,
    .products-section,
    .summary-section,
    .footer-section {
        page-break-inside: avoid;
    }

    .products-table {
        page-break-inside: auto;
    }

    .product-row {
        page-break-inside: avoid;
        page-break-after: auto;
    }

    .summary-container {
        page-break-inside: avoid;
    }

    .signature-table {
        page-break-inside: avoid;
    }
}

/* =================== SCREEN PREVIEW STYLES =================== */
@media screen {
    .invoice-print {
        border: 1px solid #ddd;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
        margin: 20px auto;
        background: white;
        transform: scale(0.9);
        transform-origin: top center;
    }

    .modal-fullscreen .modal-body {
        padding: 20px;
        background: #f5f5f5;
    }
}

/* =================== RESPONSIVE ADJUSTMENTS =================== */
@media screen and (max-width: 1200px) {
    .invoice-print {
        transform: scale(0.8);
        margin: 10px auto;
    }
}

@media screen and (max-width: 992px) {
    .invoice-print {
        transform: scale(0.7);
        margin: 5px auto;
    }

    .company-section,
    .invoice-title-section,
    .invoice-meta-section {
        display: block;
        width: 100%;
        text-align: center;
        border: none !important;
        padding: 10px 0;
    }

    .customer-info,
    .payment-info {
        display: block;
        width: 100%;
    }

    .left-content,
    .right-content {
        display: block;
        width: 100%;
        padding: 0;
    }
}

/* =================== ACCESSIBILITY =================== */
@media (prefers-reduced-motion: reduce) {
    * {
        transition: none !important;
        animation: none !important;
    }
}

@media (prefers-contrast: high) {
    .invoice-print,
    .products-table,
    .summary-table,
    .info-table {
        border: 3px solid #000 !important;
    }

    .section-header,
    .products-table th {
        background: #000 !important;
        color: #fff !important;
    }
}

.invoice-print h1,
.invoice-print h2,
.invoice-print h3,
.invoice-print h4,
.invoice-print h5,
.invoice-print h6 {
    color: black !important;
    font-weight: bold;
}

.invoice-print .table {
    color: black !important;
    border-collapse: collapse;
}

.invoice-print .table th {
    background-color: #f8f9fa !important;
    color: black !important;
    font-weight: bold;
    padding: 0.75rem;
    text-align: center;
}

.invoice-print .table td {
    color: black !important;
    padding: 0.75rem;
    border: 1px solid #dee2e6;
}

.invoice-print .table-bordered th,
.invoice-print .table-bordered td {
    border: 1px solid #000 !important;
}

.invoice-print .text-end {
    text-align: right !important;
}

/* =================== ANIMATIONS =================== */
.fade-in {
    animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.slide-in {
    animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
    from {
        transform: translateX(100%);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}

.bounce-in {
    animation: bounceIn 0.6s ease-out;
}

@keyframes bounceIn {
    0% {
        opacity: 0;
        transform: scale(0.3);
    }
    50% {
        opacity: 1;
        transform: scale(1.05);
    }
    70% {
        transform: scale(0.9);
    }
    100% {
        opacity: 1;
        transform: scale(1);
    }
}

/* =================== SCROLLBAR STYLES =================== */
::-webkit-scrollbar {
    width: 8px;
    height: 8px;
}

::-webkit-scrollbar-track {
    background: #f1f5f9;
    border-radius: 4px;
}

::-webkit-scrollbar-thumb {
    background: linear-gradient(135deg, #cbd5e0 0%, #a0aec0 100%);
    border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
    background: linear-gradient(135deg, #a0aec0 0%, #718096 100%);
}

::-webkit-scrollbar-corner {
    background: #f1f5f9;
}

/* =================== RESPONSIVE STYLES =================== */
@media (max-width: 992px) {
    .main-content .row {
        flex-direction: column;
    }

    .left-panel {
        height: auto;
        min-height: 50vh;
        border-right: none;
        border-bottom: 1px solid #dee2e6;
    }

    .right-panel {
        height: auto;
        min-height: 40vh;
    }
}

@media (max-width: 768px) {
    .pos-header h3 {
        font-size: 1.25rem;
    }

    .modal-dialog {
        margin: 0.5rem;
    }

    .modal-xl,
    .modal-lg {
        max-width: calc(100vw - 1rem);
    }

    .btn {
        font-size: 0.8rem;
        padding: 0.5rem 1rem;
    }

    .btn-sm {
        font-size: 0.75rem;
        padding: 0.25rem 0.5rem;
    }

    .table {
        font-size: 0.8rem;
    }

    .card-body {
        padding: 0.75rem;
    }
}

@media (max-width: 576px) {
    .pos-container {
        font-size: 14px;
    }

    .invoice-tab {
        min-width: 80px;
        font-size: 0.75rem;
    }

    .table th,
    .table td {
        padding: 0.5rem 0.25rem;
        font-size: 0.75rem;
    }

    .modal-body {
        padding: 1rem;
    }

    .form-control,
    .form-select {
        font-size: 0.8rem;
    }
}

/* =================== PRINT STYLES =================== */
@media print {
    .modal-header,
    .modal-footer,
    .btn,
    .no-print {
        display: none !important;
    }

    .modal-body {
        padding: 0 !important;
    }

    .invoice-print {
        margin: 0;
        padding: 20px;
        box-shadow: none !important;
    }

    .modal-content {
        border: none !important;
        box-shadow: none !important;
    }

    .modal-dialog {
        margin: 0 !important;
        max-width: 100% !important;
    }

    body {
        font-size: 12px !important;
    }

    .table {
        font-size: 11px !important;
    }
}

/* =================== UTILITY CLASSES =================== */
.text-truncate-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.text-truncate-3 {
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.cursor-pointer {
    cursor: pointer;
}

.cursor-not-allowed {
    cursor: not-allowed;
}

.user-select-none {
    user-select: none;
}

.shadow-sm-hover:hover {
    box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.1) !important;
}

.shadow-hover:hover {
    box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15) !important;
}

/* =================== ACCESSIBILITY =================== */
@media (prefers-reduced-motion: reduce) {
    *,
    *::before,
    *::after {
        animation-duration: 0.01ms !important;
        animation-iteration-count: 1 !important;
        transition-duration: 0.01ms !important;
        scroll-behavior: auto !important;
    }
}

@media (prefers-contrast: high) {
    .card,
    .modal-content,
    .btn {
        border: 2px solid #000000;
    }

    .form-control,
    .form-select {
        border: 2px solid #000000;
    }
}

/* =================== FOCUS STYLES =================== */
.btn:focus,
.form-control:focus,
.form-select:focus {
    outline: 2px solid #0d6efd;
    outline-offset: 2px;
}

/* =================== LOADING STATES =================== */
.loading-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10;
    border-radius: 0.75rem;
}

.loading-backdrop {
    backdrop-filter: blur(2px);
}

/* =================== ERROR STATES =================== */
.error-container {
    background: linear-gradient(135deg, #fff5f5 0%, #fed7d7 100%);
    border: 2px solid #feb2b2;
    border-radius: 0.75rem;
    padding: 2rem;
    text-align: center;
    color: #742a2a;
}

.error-container i {
    font-size: 3rem;
    color: #e53e3e;
    margin-bottom: 1rem;
}

/* =================== SUCCESS STATES =================== */
.success-container {
    background: linear-gradient(135deg, #f0fff4 0%, #c6f6d5 100%);
    border: 2px solid #9ae6b4;
    border-radius: 0.75rem;
    padding: 2rem;
    text-align: center;
    color: #1a202c;
}

.success-container i {
    font-size: 3rem;
    color: #38a169;
    margin-bottom: 1rem;
}
.modal-footer .btn {
    position: relative;
    z-index: 1000;
    pointer-events: auto;
}
.alert-info {
    border-left: 4px solid #0dcaf0;
    background: linear-gradient(90deg, #e7f6ff 0%, #f8f9fa 100%);
}

.alert-success {
    border-left: 4px solid #198754;
    background: linear-gradient(90deg, #e8f5e8 0%, #f8f9fa 100%);
}

.form-label small {
    font-weight: normal;
    color: #6c757d;
}

.text-warning {
    font-weight: 500;
}
.form-control:disabled,
.form-select:disabled {
    background-color: #f8f9fa;
    opacity: 0.7;
}

.position-relative .spinner-border {
    pointer-events: none;
}

/* Enhanced feedback styles */
.alert-info {
    border-left: 4px solid #0dcaf0;
    background: linear-gradient(90deg, #e7f6ff 0%, #f8f9fa 100%);
}

.alert-success {
    border-left: 4px solid #198754;
    background: linear-gradient(90deg, #e8f5e8 0%, #f8f9fa 100%);
}

.text-warning {
    font-weight: 500;
}

.badge {
    font-size: 0.75em;
}

/* Character counter */
small.text-muted {
    font-size: 0.8em;
}
.discount-badge {
    font-size: 0.8rem !important;
    font-weight: 700 !important;
    padding: 0.5rem 0.75rem !important;
    border-radius: 0.5rem !important;
    box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3) !important;
    animation: pulse-discount 2s infinite;
    background: linear-gradient(135deg, #dc3545 0%, #c82333 100%) !important;
    border: 2px solid #fff !important;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.discount-badge i {
    font-size: 0.7rem;
}

/* Pulse animation cho discount badge */
@keyframes pulse-discount {
    0% {
        box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
    }
    50% {
        box-shadow: 0 4px 16px rgba(220, 53, 69, 0.6);
        transform: scale(1.05);
    }
    100% {
        box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
    }
}

/* Price Section Styles */
.price-section {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
}

.price-current {
    font-size: 1.1rem;
    line-height: 1.2;
}

.price-original {
    font-size: 0.85rem;
    line-height: 1;
}

.price-original s {
    color: #6c757d !important;
    text-decoration: line-through !important;
    opacity: 0.8;
}

/* Product card enhancements */
.product-card {
    transition: all 0.3s ease;
    border: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.product-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.product-card:hover .discount-badge {
    animation: bounce 0.6s ease;
}

@keyframes bounce {
    0%,
    100% {
        transform: scale(1);
    }
    50% {
        transform: scale(1.1);
    }
}

/* Responsive adjustments */
@media (max-width: 768px) {
    .discount-badge {
        font-size: 0.7rem !important;
        padding: 0.375rem 0.5rem !important;
    }

    .price-current {
        font-size: 1rem;
    }

    .price-original {
        font-size: 0.8rem;
    }
}
</style>
