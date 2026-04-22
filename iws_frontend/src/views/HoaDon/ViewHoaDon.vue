<script setup>
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';

// Modal trạng thái trả hàng cần xử lý
const showImageViewModal = ref(false);
const showReasonModal = ref(false);
const selectedImageItem = ref(null);
const selectedReasonItem = ref(null);
const selectedVoucherDetail = ref(null);
const showVoucherDetailDialog = ref(false);
const voucherDetailsForInvoice = ref([]);
const isLoadingVoucherDetails = ref(false);

// ===== AUTHENTICATION & API SETUP =====
const API_BASE_URL = 'http://localhost:8080';
const API_ENDPOINTS = {
    hoaDon: `${API_BASE_URL}/hoa-don`,
    hoaDonPOS: `${API_BASE_URL}/hoa-don/pos`,
    hoaDonOnline: `${API_BASE_URL}/hoa-don/online`,
    hoaDonChiTiet: `${API_BASE_URL}/api/hoa-don-chi-tiet`,
    voucherDetails: `${API_BASE_URL}/hoa-don`,
    chiTietVoucher: `${API_BASE_URL}/api/chi-tiet-voucher`
};

function showImageModal(returnItem) {
    if (returnItem.anhMinhChung) {
        selectedImageItem.value = returnItem;
        showImageViewModal.value = true;
    }
}

function closeImageModal() {
    showImageViewModal.value = false;
    selectedImageItem.value = null;
}

function showReasonDetail(returnItem) {
    if (returnItem.lyDo) {
        selectedReasonItem.value = returnItem;
        showReasonModal.value = true;
    }
}

function closeReasonModal() {
    showReasonModal.value = false;
    selectedReasonItem.value = null;
}

function handleImageError(event) {
    event.target.src =
        'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2Y4ZjlmYSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjE0IiBmaWxsPSIjNmM3NTdkIj5LaW9uZyBjbyBhbmg8L3RleHQ+PC9zdmc+';
}

function formatReasonPreview(reason, maxLength = 50) {
    if (!reason) return 'Không có';
    if (reason.length <= maxLength) return reason;
    return reason.substring(0, maxLength) + '...';
}

function viewReturnDetail(returnItem) {
    const details = {
        sanPham: returnItem.tenSanPham,
        maYeuCau: returnItem.maChiTietTraHang,
        soLuong: returnItem.soLuong,
        tienHoan: formatCurrency(returnItem.tienHoan),
        ngayTao: formatDate(returnItem.ngayTaoTraHang),
        trangThai: getReturnStatusLabel(returnItem.trangThaiHoaDon),
        lyDo: returnItem.lyDo || 'Không có',
        anhMinhChung: returnItem.anhMinhChung ? 'Có' : 'Không'
    };

    toast.add({
        severity: 'info',
        summary: 'Chi tiết yêu cầu trả hàng',
        detail: `${details.sanPham} - SL: ${details.soLuong} - ${details.tienHoan}`,
        life: 5000
    });
}
function getAuthHeaders() {
    // Try multiple possible token storage keys
    const token = localStorage.getItem('auth_token') || 
                  localStorage.getItem('token') || 
                  localStorage.getItem('accessToken') ||
                  sessionStorage.getItem('auth_token') ||
                  sessionStorage.getItem('token');
    
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };
    
    if (token) {
        // Remove any 'Bearer ' prefix if it already exists
        const cleanToken = token.replace(/^Bearer\s+/i, '');
        headers.Authorization = `Bearer ${cleanToken}`;
        
        // Also add X-Auth-Token header as some APIs might expect it
        headers['X-Auth-Token'] = cleanToken;
    }
    
    return headers;
}

async function fetchWithErrorHandling(url, options = {}) {
    try {
        const authHeaders = getAuthHeaders();
        const response = await fetch(url, {
            headers: {
                ...authHeaders,
                ...options.headers
            },
            ...options
        });

        if (!response.ok) {
            if (response.status === 401) {
                // Clear invalid token and redirect to login
                localStorage.removeItem('auth_token');
                localStorage.removeItem('user_info');
                throw new Error('Authentication failed - Token không hợp lệ hoặc đã hết hạn');
            }
            if (response.status === 403) {
                throw new Error('Access denied - Không có quyền truy cập tài nguyên này');
            }
            if (response.status === 404) {
                throw new Error('Resource not found - Không tìm thấy tài nguyên');
            }
            
            // Try to get error message from response
            let errorData = {};
            try {
                errorData = await response.json();
            } catch (e) {
                // If response is not JSON, use status text
                errorData = { message: response.statusText };
            }
            
            throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
        }

        // Handle empty responses
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        } else {
            // If response is not JSON, return a success object
            return { success: true, message: 'Operation completed successfully' };
        }
    } catch (error) {
        console.error(`API Error for ${url}:`, error);
        throw error;
    }
}

// ===== REACTIVE DATA =====
const toast = useToast();

// Main data
const hoaDons = ref([]);
const isLoading = ref(false);
const hasError = ref(false);
const errorMessage = ref('');
const loadingMessage = ref('');

// Filter & Search
const searchKeyword = ref('');
const statusFilter = ref('');
const typeFilter = ref('');
const dateFilter = ref(null);
const showAdvancedFilter = ref(false);
const minAmount = ref(null);
const maxAmount = ref(null);
const paymentMethodFilter = ref('');
const returnStatusFilter = ref('');

// Tab management
const activeTabIndex = ref(0);
const itemsPerPage = ref(10);

// Modal refs
const showChiTietDialog = ref(false);
const showStatusUpdateDialog = ref(false);
const showDetailedBreakdown = ref(false); // THÊM DÒNG NÀY

// Chi tiết hóa đơn
const selectedHoaDon = ref(null);
const hoaDonChiTiets = ref([]);
const isLoadingChiTiet = ref(false);
const searchChiTietKeyword = ref('');
const editingItemId = ref(null);
const editQuantity = ref(1);

// Status update
const selectedInvoiceForUpdate = ref(null);
const newStatus = ref('');
const statusNote = ref('');

// ===== API FUNCTIONS =====
async function fetchAllData() {
    isLoading.value = true;
    hasError.value = false;
    loadingMessage.value = 'Đang tải danh sách hóa đơn...';

    try {
        const response = await fetchWithErrorHandling(API_ENDPOINTS.hoaDon);
        let data = [];

        if (Array.isArray(response)) {
            data = response;
        } else if (response.success && response.data) {
            data = Array.isArray(response.data) ? response.data : [];
        }

        hoaDons.value = data.map(normalizeHoaDon);

        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: `Đã tải ${hoaDons.value.length} hóa đơn`,
            life: 3000
        });
    } catch (error) {
        console.error('Error in fetchAllData:', error);
        hasError.value = true;

        if (error.message.includes('Authentication failed')) {
            errorMessage.value = 'Phiên đăng nhập đã hết hạn';
            toast.add({
                severity: 'error',
                summary: 'Lỗi xác thực',
                detail: 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
                life: 5000
            });
        } else {
            errorMessage.value = `Không thể kết nối đến API: ${error.message}`;
            toast.add({
                severity: 'error',
                summary: 'Lỗi kết nối',
                detail: 'Không thể tải dữ liệu từ server.',
                life: 3000
            });
            hoaDons.value = createSampleData();
        }
    } finally {
        isLoading.value = false;
        loadingMessage.value = '';
    }
}

async function fetchChiTietHoaDon(hoaDonId) {
    isLoadingChiTiet.value = true;
    try {
        // Lấy chi tiết sản phẩm
        const endpoint = `${API_ENDPOINTS.hoaDonChiTiet}/by-hoa-don/${hoaDonId}`;
        const response = await fetchWithErrorHandling(endpoint);

        let data = [];
        if (response.success && response.data) {
            data = Array.isArray(response.data) ? response.data : [];
        } else if (Array.isArray(response)) {
            data = response;
        }

        hoaDonChiTiets.value = data.map((item) => ({
            ...item,
            hinhAnh: createImageUrl(item.hinhAnh || item.duongDan || item.imageUrl)
        }));

        // ✅ THÊM: Lấy chi tiết voucher song song
        await fetchVoucherDetails(hoaDonId);

        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: `Đã tải ${hoaDonChiTiets.value.length} chi tiết sản phẩm`,
            life: 3000
        });
    } catch (error) {
        console.error('Error fetching chi tiet:', error);
        hoaDonChiTiets.value = createSampleChiTietData(hoaDonId).map((item) => ({
            ...item,
            hinhAnh: createImageUrl(item.hinhAnh || item.duongDan || item.imageUrl)
        }));

        toast.add({
            severity: 'warn',
            summary: 'Cảnh báo',
            detail: 'Không thể tải chi tiết từ API, hiển thị dữ liệu mẫu',
            life: 3000
        });
    } finally {
        isLoadingChiTiet.value = false;
    }

    // Hàm xử lý đường dẫn ảnh sản phẩm
    function createImageUrl(duongDan) {
        if (!duongDan) return null;
        if (duongDan.startsWith('http://') || duongDan.startsWith('https://')) return duongDan;
        return API_BASE_URL + '/' + duongDan.replace(/^\//, '');
    }
}

// ===== STATUS MANAGEMENT FUNCTIONS =====
async function processNextStepWithRefresh(hoaDon) {
    try {
        const currentStep = normalizeStatus(hoaDon.trangThaiHoaDon);
        const steps = getWorkflowSteps(hoaDon.loaiHoaDon);
        const currentIndex = steps.indexOf(currentStep);

        if (currentIndex >= 0 && currentIndex < steps.length - 1) {
            const nextStep = steps[currentIndex + 1];
            const endpoint = `${API_ENDPOINTS.hoaDon}/${hoaDon.id}/trang-thai`;

            const requestData = {
                trangThai: nextStep,
                ghiChu: `Cập nhật trạng thái từ ${getStatusLabel(currentStep)} sang ${getStatusLabel(nextStep)}`
            };

            const response = await fetchWithErrorHandling(endpoint, {
                method: 'PUT',
                body: JSON.stringify(requestData)
            });

            let updatedHoaDon = null;
            if (response && response.data) {
                updatedHoaDon = normalizeHoaDon(response.data);
            } else if (response && response.id) {
                updatedHoaDon = normalizeHoaDon(response);
            }

            if (updatedHoaDon) {
                const index = hoaDons.value.findIndex((hd) => hd.id === hoaDon.id);
                if (index !== -1) {
                    hoaDons.value[index] = updatedHoaDon;
                }

                if (selectedHoaDon.value && selectedHoaDon.value.id === hoaDon.id) {
                    selectedHoaDon.value = updatedHoaDon;
                }

                toast.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: `Đã cập nhật trạng thái sang ${getStatusLabel(nextStep)}`,
                    life: 3000
                });
            }
        }
    } catch (error) {
        console.error('Error processing next step:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: `Không thể cập nhật trạng thái: ${error.message}`,
            life: 3000
        });
    }
}

async function cancelInvoice(hoaDon) {
    const reason = prompt('Nhập lý do hủy đơn hàng:');
    if (!reason || reason.trim() === '') {
        toast.add({
            severity: 'warn',
            summary: 'Cảnh báo',
            detail: 'Vui lòng nhập lý do hủy đơn',
            life: 3000
        });
        return;
    }

    try {
        const endpoint = `${API_ENDPOINTS.hoaDon}/${hoaDon.id}/huy`;
        const response = await fetchWithErrorHandling(endpoint, {
            method: 'PUT',
            body: JSON.stringify({ lyDo: reason })
        });

        let updatedHoaDon = null;
        if (response && response.data) {
            updatedHoaDon = normalizeHoaDon(response.data);
        } else if (response && response.id) {
            updatedHoaDon = normalizeHoaDon(response);
        }

        if (updatedHoaDon) {
            const index = hoaDons.value.findIndex((hd) => hd.id === hoaDon.id);
            if (index !== -1) {
                hoaDons.value[index] = updatedHoaDon;
            }

            if (selectedHoaDon.value && selectedHoaDon.value.id === hoaDon.id) {
                selectedHoaDon.value = updatedHoaDon;
            }

            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: 'Đã hủy đơn hàng thành công',
                life: 3000
            });
        }
    } catch (error) {
        console.error('Error cancelling invoice:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: `Không thể hủy đơn hàng: ${error.message}`,
            life: 3000
        });
    }
}

async function confirmStatusUpdate() {
    if (!newStatus.value) {
        toast.add({
            severity: 'warn',
            summary: 'Cảnh báo',
            detail: 'Vui lòng chọn trạng thái mới',
            life: 3000
        });
        return;
    }

    try {
        const endpoint = `${API_ENDPOINTS.hoaDon}/${selectedInvoiceForUpdate.value.id}/trang-thai`;
        const requestData = {
            trangThai: newStatus.value,
            ghiChu: statusNote.value || `Cập nhật trạng thái sang ${getStatusLabel(newStatus.value)}`
        };

        const response = await fetchWithErrorHandling(endpoint, {
            method: 'PUT',
            body: JSON.stringify(requestData)
        });

        let updatedHoaDon = null;
        if (response && response.data) {
            updatedHoaDon = normalizeHoaDon(response.data);
        } else if (response && response.id) {
            updatedHoaDon = normalizeHoaDon(response);
        }

        if (updatedHoaDon) {
            const index = hoaDons.value.findIndex((hd) => hd.id === selectedInvoiceForUpdate.value.id);
            if (index !== -1) {
                hoaDons.value[index] = updatedHoaDon;
            }

            if (selectedHoaDon.value && selectedHoaDon.value.id === selectedInvoiceForUpdate.value.id) {
                selectedHoaDon.value = updatedHoaDon;
            }

            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Cập nhật trạng thái thành công sang ${getStatusLabel(newStatus.value)}`,
                life: 3000
            });

            closeStatusUpdateDialog();
        }
    } catch (error) {
        console.error('Error updating status:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: `Không thể cập nhật trạng thái: ${error.message}`,
            life: 3000
        });
    }
}

// ===== ITEM EDITING FUNCTIONS =====
async function saveQuantity(itemId) {
    try {
        const endpoint = `${API_ENDPOINTS.hoaDonChiTiet}/${itemId}/quantity`;
        const response = await fetchWithErrorHandling(endpoint, {
            method: 'PUT',
            body: JSON.stringify({ soLuong: editQuantity.value })
        });

        if (response.success) {
            const index = hoaDonChiTiets.value.findIndex((item) => item.id === itemId);
            if (index !== -1) {
                if (response.data) {
                    hoaDonChiTiets.value[index] = response.data;
                } else {
                    hoaDonChiTiets.value[index].soLuong = editQuantity.value;
                    hoaDonChiTiets.value[index].thanhTien = hoaDonChiTiets.value[index].giaBan * editQuantity.value;
                }
            }

            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: response.message || 'Cập nhật số lượng thành công',
                life: 3000
            });
            cancelEdit();
        }
    } catch (error) {
        console.error('Error updating quantity:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể cập nhật số lượng',
            life: 3000
        });
    }
}

async function removeItem(itemId) {
    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) return;

    try {
        const endpoint = `${API_ENDPOINTS.hoaDonChiTiet}/remove-product/${itemId}`;
        const response = await fetchWithErrorHandling(endpoint, { method: 'DELETE' });

        if (response.success) {
            const index = hoaDonChiTiets.value.findIndex((item) => item.id === itemId);
            if (index !== -1) {
                hoaDonChiTiets.value.splice(index, 1);
            }

            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: response.message || 'Đã xóa sản phẩm',
                life: 3000
            });
        }
    } catch (error) {
        console.error('Error removing item:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể xóa sản phẩm',
            life: 3000
        });
    }
}

// ===== MODAL FUNCTIONS =====
async function viewChiTiet(hoaDon) {
    selectedHoaDon.value = { ...hoaDon };
    searchChiTietKeyword.value = '';
    showChiTietDialog.value = true;
    await fetchChiTietHoaDon(hoaDon.id);
}

function closeChiTietDialog() {
    showChiTietDialog.value = false;
    selectedHoaDon.value = null;
    hoaDonChiTiets.value = [];
    searchChiTietKeyword.value = '';
    editingItemId.value = null;
    showDetailedBreakdown.value = false; // THÊM DÒNG NÀY
}

function updateInvoiceStatus(hoaDon) {
    selectedInvoiceForUpdate.value = { ...hoaDon };
    newStatus.value = '';
    statusNote.value = '';
    showStatusUpdateDialog.value = true;
}

function getTotalBeforeVoucher() {
    if (!selectedHoaDon.value) return 0;

    // Tổng tiền hàng + phí vận chuyển
    const tongTienHang = getTotalAmount();
    const phiVanChuyen = selectedHoaDon.value.phiVanChuyen || 0;

    return tongTienHang + phiVanChuyen;
}

// Tính tổng tiền sau khi áp voucher (nhưng chưa trừ điểm)
function getTotalAfterVoucher() {
    if (!selectedHoaDon.value) return 0;

    const totalBeforeVoucher = getTotalBeforeVoucher();
    const voucherDiscount = selectedHoaDon.value.chiTietVoucherList ? calculateTotalVoucherSavings(selectedHoaDon.value.chiTietVoucherList) : 0;

    return totalBeforeVoucher - voucherDiscount;
}

// Tính tổng tiền cuối cùng (sau voucher và điểm)
function getFinalTotalAfterAll() {
    if (!selectedHoaDon.value) return 0;

    const totalAfterVoucher = getTotalAfterVoucher();
    const pointDiscount = selectedHoaDon.value.tienDiem || 0;

    return totalAfterVoucher - pointDiscount;
}

function closeStatusUpdateDialog() {
    showStatusUpdateDialog.value = false;
    selectedInvoiceForUpdate.value = null;
    newStatus.value = '';
    statusNote.value = '';
}

// ===== UTILITY FUNCTIONS =====
function normalizeHoaDon(hd) {
    let ngayTaoDate = null;
    if (hd.ngayTao) {
        ngayTaoDate = new Date(hd.ngayTao);
        if (isNaN(ngayTaoDate.getTime())) {
            ngayTaoDate = new Date();
        }
    } else {
        ngayTaoDate = new Date();
    }

    return {
        ...hd,
        ngayTao: ngayTaoDate,
        ngayXacNhan: hd.ngayXacNhan ? new Date(hd.ngayXacNhan) : null
    };
}

function formatDate(date) {
    if (!date) return 'N/A';
    const parsedDate = new Date(date);
    if (isNaN(parsedDate.getTime())) return 'N/A';
    return parsedDate.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatCurrency(amount) {
    if (!amount && amount !== 0) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function getInitials(name) {
    if (!name) return 'KH';
    return name
        .split(' ')
        .map((word) => word.charAt(0))
        .join('')
        .toUpperCase()
        .slice(0, 2);
}

function getCustomerName(hoaDon) {
    return hoaDon.tenKhachHang || hoaDon.tenNguoiDung || hoaDon.customerName || hoaDon.userName || (hoaDon.khachHang && hoaDon.khachHang.hoTen) || 'Khách lẻ';
}

function getStatusLabel(status) {
    if (!status) return 'Không xác định';
    const statusMap = {
        PENDING: 'Chờ xác nhận',
        CONFIRMED: 'Đã xác nhận',
        SHIPPING: 'Đang giao',
        DELIVERED: 'Đã giao',
        COMPLETED: 'Hoàn thành',
        CANCELLED: 'Đã hủy',
        RETURNED: 'Hoàn trả'
    };
    return statusMap[status.toUpperCase()] || statusMap[status] || status;
}

function getStatusSeverity(status) {
    if (!status) return 'secondary';
    const statusLower = status.toString().toLowerCase();

    if (statusLower.includes('pending') || statusLower.includes('chờ')) {
        return 'warning';
    } else if (statusLower.includes('confirmed') || statusLower.includes('xác nhận')) {
        return 'info';
    } else if (statusLower.includes('shipping') || statusLower.includes('giao')) {
        return null;
    } else if (statusLower.includes('completed') || statusLower.includes('hoàn thành')) {
        return 'success';
    } else if (statusLower.includes('cancelled') || statusLower.includes('hủy')) {
        return 'danger';
    } else if (statusLower.includes('delivered')) {
        return 'info';
    } else {
        return 'secondary';
    }
}

function getStatusIcon(status) {
    if (!status) return 'pi pi-question-circle';
    const statusLower = status.toString().toLowerCase();

    if (statusLower.includes('pending') || statusLower.includes('chờ')) {
        return 'pi pi-clock';
    } else if (statusLower.includes('confirmed') || statusLower.includes('xác nhận')) {
        return 'pi pi-check-circle';
    } else if (statusLower.includes('shipping') || statusLower.includes('giao')) {
        return 'pi pi-truck';
    } else if (statusLower.includes('completed') || statusLower.includes('hoàn thành')) {
        return 'pi pi-verified';
    } else if (statusLower.includes('cancelled') || statusLower.includes('hủy')) {
        return 'pi pi-times-circle';
    } else if (statusLower.includes('delivered')) {
        return 'pi pi-home';
    } else {
        return 'pi pi-circle';
    }
}

// ===== WORKFLOW FUNCTIONS =====
function getWorkflowSteps(loaiHoaDon) {
    const normalizedType = loaiHoaDon ? loaiHoaDon.toUpperCase() : '';
    if (normalizedType === 'OFFLINE') {
        return ['PENDING', 'COMPLETED'];
    } else {
        return ['PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'COMPLETED'];
    }
}

function getWorkflowStepClass(hoaDon, step) {
    const normalizedCurrentStep = normalizeStatus(hoaDon.trangThaiHoaDon);
    const steps = getWorkflowSteps(hoaDon.loaiHoaDon);
    const currentIndex = steps.indexOf(normalizedCurrentStep);
    const stepIndex = steps.indexOf(step);

    const baseClass = 'workflow-step d-flex align-items-center justify-content-center';

    if (stepIndex < currentIndex) {
        return `${baseClass} bg-success text-white`;
    } else if (stepIndex === currentIndex) {
        return `${baseClass} bg-primary text-white`;
    } else {
        return `${baseClass} bg-light text-muted border`;
    }
}

function getWorkflowConnectorClass(hoaDon, step) {
    const normalizedCurrentStep = normalizeStatus(hoaDon.trangThaiHoaDon);
    const steps = getWorkflowSteps(hoaDon.loaiHoaDon);
    const currentIndex = steps.indexOf(normalizedCurrentStep);
    const stepIndex = steps.indexOf(step);

    const baseClass = 'workflow-connector';

    if (stepIndex < currentIndex) {
        return `${baseClass} completed`;
    } else {
        return `${baseClass} pending`;
    }
}

function isStepCompleted(hoaDon, step) {
    const normalizedCurrentStep = normalizeStatus(hoaDon.trangThaiHoaDon);
    const steps = getWorkflowSteps(hoaDon.loaiHoaDon);
    const currentIndex = steps.indexOf(normalizedCurrentStep);
    const stepIndex = steps.indexOf(step);
    return stepIndex < currentIndex;
}

function isStepActive(hoaDon, step) {
    const normalizedCurrentStep = normalizeStatus(hoaDon.trangThaiHoaDon);
    return normalizedCurrentStep === step;
}

function getStepIcon(step) {
    const iconMap = {
        PENDING: 'pi pi-clock',
        CONFIRMED: 'pi pi-check',
        SHIPPING: 'pi pi-truck',
        DELIVERED: 'pi pi-home',
        COMPLETED: 'pi pi-check-circle'
    };
    return iconMap[step] || 'pi pi-circle';
}

function getStepLabel(step) {
    const labelMap = {
        PENDING: 'Chờ xác nhận',
        CONFIRMED: 'Đã xác nhận',
        SHIPPING: 'Đang giao',
        DELIVERED: 'Đã giao',
        COMPLETED: 'Hoàn thành'
    };
    return labelMap[step] || step;
}

function normalizeStatus(status) {
    if (!status) return '';
    const statusStr = status.toString().trim().toUpperCase();

    const vietnameseToEnglish = {
        CHO_XAC_NHAN: 'PENDING',
        DA_XAC_NHAN: 'CONFIRMED',
        DANG_GIAO: 'SHIPPING',
        DA_GIAO: 'DELIVERED',
        HOAN_THANH: 'COMPLETED',
        DA_HUY: 'CANCELLED',
        HOAN_TRA: 'RETURNED'
    };

    return vietnameseToEnglish[statusStr] || statusStr;
}

//Voucher detail
async function fetchVoucherDetails(hoaDonId) {
    isLoadingVoucherDetails.value = true;
    try {
        // First check if voucher data is already available in the selected invoice
        if (selectedVoucherDetail.value?.chiTietVoucherList && selectedVoucherDetail.value.chiTietVoucherList.length > 0) {
            voucherDetailsForInvoice.value = selectedVoucherDetail.value.chiTietVoucherList;
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã tải ${voucherDetailsForInvoice.value.length} voucher từ dữ liệu hóa đơn`,
                life: 3000
            });
            return;
        }

        // Try multiple possible endpoints for voucher details
        let response = null;
        let endpoint = '';
        let lastError = null;
        
        const endpoints = [
            // Try different possible API endpoints
            `${API_BASE_URL}/api/chi-tiet-voucher/by-hoa-don/${hoaDonId}`,
            `${API_BASE_URL}/api/hoa-don/${hoaDonId}/voucher-details`,
            `${API_BASE_URL}/api/voucher/hoa-don/${hoaDonId}`,
            `${API_BASE_URL}/hoa-don/${hoaDonId}/voucher-details`,
            `${API_BASE_URL}/api/hoa-don/${hoaDonId}/vouchers`,
            `${API_BASE_URL}/voucher/hoa-don/${hoaDonId}`,
            `${API_BASE_URL}/api/voucher-details/${hoaDonId}`,
            // Try with chi-tiet-voucher endpoint from API_ENDPOINTS
            `${API_ENDPOINTS.chiTietVoucher}/by-hoa-don/${hoaDonId}`,
            `${API_ENDPOINTS.chiTietVoucher}/${hoaDonId}`,
        ];

        for (const testEndpoint of endpoints) {
            try {
                console.log(`Trying endpoint: ${testEndpoint}`);
                endpoint = testEndpoint;
                response = await fetchWithErrorHandling(endpoint);
                console.log(`Success with endpoint: ${testEndpoint}`, response);
                break; // If successful, break out of the loop
            } catch (error) {
                console.warn(`Endpoint ${testEndpoint} failed:`, error.message);
                lastError = error;
                continue; // Try next endpoint
            }
        }

        // Handle different response formats
        if (response) {
            let voucherData = [];
            
            if (response.success && response.data) {
                // Handle nested response structure
                if (response.data.voucherInfo?.chiTietVoucherList) {
                    voucherData = response.data.voucherInfo.chiTietVoucherList;
                } else if (response.data.chiTietVoucherList) {
                    voucherData = response.data.chiTietVoucherList;
                } else if (Array.isArray(response.data)) {
                    voucherData = response.data;
                } else if (response.data.vouchers) {
                    voucherData = response.data.vouchers;
                }
            } else if (Array.isArray(response)) {
                voucherData = response;
            } else if (response.chiTietVoucherList) {
                voucherData = response.chiTietVoucherList;
            } else if (response.vouchers) {
                voucherData = response.vouchers;
            } else if (response.data && Array.isArray(response.data)) {
                voucherData = response.data;
            }
            
            voucherDetailsForInvoice.value = voucherData;

            if (voucherData.length > 0) {
                toast.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: `Đã tải ${voucherData.length} voucher`,
                    life: 3000
                });
            } else {
                // Don't show warning if no vouchers found, just info
                toast.add({
                    severity: 'info',
                    summary: 'Thông tin',
                    detail: 'Hóa đơn này không có voucher nào',
                    life: 3000
                });
            }
        } else {
            // If all endpoints failed, check if it's because there are no vouchers (not an error)
            voucherDetailsForInvoice.value = [];
            
            // Only show error if it's a real error (not 404 which means no vouchers)
            if (lastError && !lastError.message.includes('404')) {
                let errorMessage = 'Không thể tải thông tin voucher';
                if (lastError.message.includes('403')) {
                    errorMessage = 'Không có quyền truy cập thông tin voucher';
                } else if (lastError.message.includes('401')) {
                    errorMessage = 'Phiên đăng nhập đã hết hạn';
                }

                toast.add({
                    severity: 'warn',
                    summary: 'Cảnh báo',
                    detail: errorMessage,
                    life: 3000
                });
            } else {
                // If it's 404 or no specific error, just show info
                toast.add({
                    severity: 'info',
                    summary: 'Thông tin',
                    detail: 'Hóa đơn này không có voucher nào',
                    life: 3000
                });
            }
        }
    } catch (error) {
        console.error('Error fetching voucher details:', error);
        voucherDetailsForInvoice.value = [];

        // Only show error if it's not a 404 (which means no vouchers exist)
        if (!error.message.includes('404')) {
            let errorMessage = 'Không thể tải thông tin voucher';
            if (error.message.includes('403')) {
                errorMessage = 'Không có quyền truy cập thông tin voucher';
            } else if (error.message.includes('401')) {
                errorMessage = 'Phiên đăng nhập đã hết hạn';
            }

            toast.add({
                severity: 'warn',
                summary: 'Cảnh báo',
                detail: errorMessage,
                life: 3000
            });
        } else {
            toast.add({
                severity: 'info',
                summary: 'Thông tin',
                detail: 'Hóa đơn này không có voucher nào',
                life: 3000
            });
        }
    } finally {
        isLoadingVoucherDetails.value = false;
    }
}
async function showVoucherDetail(hoaDon) {
    selectedVoucherDetail.value = { ...hoaDon };
    showVoucherDetailDialog.value = true;
    await fetchVoucherDetails(hoaDon.id);
}

function closeVoucherDetailDialog() {
    showVoucherDetailDialog.value = false;
    selectedVoucherDetail.value = null;
    voucherDetailsForInvoice.value = [];
}
function getVoucherTypeLabel(loaiGiamGia) {
    const typeMap = {
        PHAN_TRAM: 'Phần trăm',
        TIEN_MAT: 'Tiền mặt',
        FIXED_AMOUNT: 'Số tiền cố định'
    };
    return typeMap[loaiGiamGia] || loaiGiamGia;
}

function getVoucherTypeSeverity(loaiGiamGia) {
    const severityMap = {
        PHAN_TRAM: 'info',
        TIEN_MAT: 'success',
        FIXED_AMOUNT: 'warning'
    };
    return severityMap[loaiGiamGia] || 'secondary';
}
function calculateTotalVoucherSavings(voucherList) {
    if (!voucherList || voucherList.length === 0) return 0;

    return voucherList.reduce((total, voucher) => {
        return total + (voucher.soTienGiam || 0);
    }, 0);
}
function hasVoucherDiscount(hoaDon) {
    return hoaDon.chiTietVoucherList && hoaDon.chiTietVoucherList.length > 0;
}
function getPrimaryVoucher(voucherList) {
    if (!voucherList || voucherList.length === 0) return null;

    return voucherList.reduce((primary, current) => {
        const primaryAmount = primary.soTienGiam || 0;
        const currentAmount = current.soTienGiam || 0;
        return currentAmount > primaryAmount ? current : primary;
    });
}

// ===== STATUS CHECK FUNCTIONS =====
function canUpdateStatus(hoaDon) {
    if (!hoaDon) return false;
    const normalizedStatus = normalizeStatus(hoaDon.trangThaiHoaDon);
    const terminalStates = ['COMPLETED', 'CANCELLED', 'RETURNED'];
    return !terminalStates.includes(normalizedStatus);
}

function canProcessNextStep(hoaDon) {
    const steps = getWorkflowSteps(hoaDon.loaiHoaDon);
    const normalizedStatus = normalizeStatus(hoaDon.trangThaiHoaDon);
    const currentIndex = steps.indexOf(normalizedStatus);
    return currentIndex >= 0 && currentIndex < steps.length - 1;
}

function getNextStepAction(hoaDon) {
    const normalizedStatus = normalizeStatus(hoaDon.trangThaiHoaDon);
    const actionMap = {
        PENDING: 'Xác nhận',
        CONFIRMED: 'Giao hàng',
        SHIPPING: 'Đã giao',
        DELIVERED: 'Hoàn thành'
    };
    return actionMap[normalizedStatus] || 'Tiếp theo';
}

function canCancelInvoice(hoaDon) {
    const normalizedStatus = normalizeStatus(hoaDon.trangThaiHoaDon);
    const cancelableStates = ['PENDING', 'CONFIRMED'];
    return cancelableStates.includes(normalizedStatus);
}

function canEditItems(hoaDon) {
    const normalizedStatus = normalizeStatus(hoaDon.trangThaiHoaDon);
    return normalizedStatus === 'PENDING';
}

function getAvailableStatuses(hoaDon) {
    if (!hoaDon) return [];
    const currentStatus = normalizeStatus(hoaDon.trangThaiHoaDon);
    const availableSteps = [];

    switch (currentStatus) {
        case 'PENDING':
            availableSteps.push({ value: 'CONFIRMED', label: 'Đã xác nhận' });
            break;
        case 'CONFIRMED':
            availableSteps.push({ value: 'SHIPPING', label: 'Đang giao' });
            break;
        case 'SHIPPING':
            availableSteps.push({ value: 'DELIVERED', label: 'Đã giao' });
            break;
        case 'DELIVERED':
            availableSteps.push({ value: 'COMPLETED', label: 'Hoàn thành' });
            break;
    }

    const terminalStates = ['COMPLETED', 'CANCELLED', 'RETURNED'];
    if (!terminalStates.includes(currentStatus)) {
        availableSteps.push({ value: 'CANCELLED', label: 'Hủy đơn' });
    }

    return availableSteps;
}

function needsNote(status) {
    return status === 'CANCELLED';
}

// ===== ITEM EDITING FUNCTIONS =====
function isEditingItem(itemId) {
    return editingItemId.value === itemId;
}

function editItem(item) {
    editingItemId.value = item.id;
    editQuantity.value = item.soLuong;
}

function cancelEdit() {
    editingItemId.value = null;
    editQuantity.value = 1;
}

// ===== CALCULATION FUNCTIONS =====
function getTotalQuantity() {
    return filteredChiTiets.value.reduce((sum, item) => sum + (item.soLuong || 0), 0);
}

function getTotalAmount() {
    return filteredChiTiets.value.reduce((sum, item) => {
        const thanhTien = item.thanhTien || (item.giaBan || 0) * (item.soLuong || 0);
        return sum + thanhTien;
    }, 0);
}

function getTotalDiscount() {
    // Tính tổng tiền giảm từ sản phẩm
    const productDiscount = filteredChiTiets.value.reduce((sum, item) => {
        const giaGoc = item.giaGoc || item.giaBan || 0;
        const giaBan = item.giaBan || 0;
        const soLuong = item.soLuong || 0;
        const tienGiam = (giaGoc - giaBan) * soLuong;
        return sum + (tienGiam > 0 ? tienGiam : 0);
    }, 0);

    // Tính tổng tiền giảm từ voucher
    const voucherDiscount = selectedHoaDon.value?.tongTienVoucherChiTiet || selectedHoaDon.value?.tongTienVoucher || (selectedHoaDon.value?.chiTietVoucherList ? calculateTotalVoucherSavings(selectedHoaDon.value.chiTietVoucherList) : 0);

    // Tính tiền giảm từ điểm (nếu có)
    const pointDiscount = selectedHoaDon.value?.tienDiem || (selectedHoaDon.value?.giaTriDiem ? selectedHoaDon.value.giaTriDiem : 0);

    return productDiscount + voucherDiscount + pointDiscount;
}

function getTotalOriginalAmount() {
    return filteredChiTiets.value.reduce((sum, item) => {
        const giaGoc = item.giaGoc || item.giaBan || 0;
        const soLuong = item.soLuong || 0;
        return sum + giaGoc * soLuong;
    }, 0);
}

// ===== SAMPLE DATA FUNCTIONS =====
function createSampleData() {
    return [
        {
            id: 1,
            maHoaDon: 'HD001',
            tenKhachHang: 'Nguyễn Văn A',
            sdt: '0912345671',
            email: 'user1@example.com',
            tongTien: 2400000.0,
            trangThaiHoaDon: 'COMPLETED',
            loaiHoaDon: 'ONLINE',
            ngayTao: new Date('2025-01-01T10:00:00.000Z'),
            phuongThucThanhToan: 'BANK_TRANSFER',
            chiTietTraHangList: []
        },
        {
            id: 2,
            maHoaDon: 'HD002',
            tenKhachHang: 'Trần Văn B',
            sdt: '0912345672',
            email: 'user2@example.com',
            tongTien: 900000.0,
            trangThaiHoaDon: 'PENDING',
            loaiHoaDon: 'ONLINE',
            ngayTao: new Date('2025-01-01T11:00:00.000Z'),
            phuongThucThanhToan: 'COD',
            chiTietTraHangList: []
        },
        {
            id: 3,
            maHoaDon: 'HD003',
            tenKhachHang: 'Lê Văn C',
            sdt: '0912345673',
            tongTien: 1200000.0,
            trangThaiHoaDon: 'COMPLETED',
            loaiHoaDon: 'OFFLINE',
            ngayTao: new Date('2025-01-01T12:00:00.000Z'),
            phuongThucThanhToan: 'CASH',
            chiTietTraHangList: []
        }
    ];
}

function createSampleChiTietData(hoaDonId) {
    return [
        {
            id: 1,
            hoaDonId: hoaDonId,
            tenSanPham: 'Giày Nike Air Max',
            maSanPham: 'SP001',
            mauSac: 'Đen',
            kichThuoc: '42',
            giaBan: 1200000.0,
            giaGoc: 1500000.0,
            soLuong: 1,
            thanhTien: 1200000.0,
            tienTietKiem: 300000.0,
            chiTietSanPhamId: 1
        },
        {
            id: 2,
            hoaDonId: hoaDonId,
            tenSanPham: 'Áo thun Adidas',
            maSanPham: 'SP002',
            mauSac: 'Trắng',
            kichThuoc: 'L',
            giaBan: 600000.0,
            giaGoc: 800000.0,
            soLuong: 2,
            thanhTien: 1200000.0,
            tienTietKiem: 400000.0,
            chiTietSanPhamId: 2
        }
    ];
}

// ===== EVENT HANDLERS =====
async function refreshAllData() {
    await fetchAllData();
}

async function retryConnection() {
    hasError.value = false;
    await fetchAllData();
}

function goToLogin() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_info');
    localStorage.removeItem('token');
    sessionStorage.clear();
    window.location.href = '/login';
}

let searchTimeout;
function onSearch() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        // Trigger reactive update
    }, 300);
}

function applyFilters() {
    // Triggers reactive computed properties
}

function applyAdvancedFilters() {
    toast.add({
        severity: 'info',
        summary: 'Bộ lọc',
        detail: 'Đã áp dụng bộ lọc nâng cao',
        life: 3000
    });
}

function clearAllFilters() {
    searchKeyword.value = '';
    typeFilter.value = '';
    statusFilter.value = '';
    dateFilter.value = null;
    minAmount.value = null;
    maxAmount.value = null;
    paymentMethodFilter.value = '';
    returnStatusFilter.value = '';
    toast.add({
        severity: 'info',
        summary: 'Bộ lọc',
        detail: 'Đã xóa tất cả bộ lọc',
        life: 3000
    });
}

function onSort(event) {
    // Handle sorting
}

function onTabChange(event) {
    activeTabIndex.value = event.index;
}

function exportData() {
    try {
        const headers = ['ID', 'Mã hóa đơn', 'Loại', 'Ngày tạo', 'Tên khách hàng', 'Số điện thoại', 'Email', 'Tổng tiền', 'Trạng thái', 'Phương thức thanh toán', 'Số yêu cầu trả hàng'];

        const csvData = filteredHoaDons.value.map((hd) => [
            hd.id,
            hd.maHoaDon || '',
            hd.loaiHoaDon && hd.loaiHoaDon.toUpperCase() === 'OFFLINE' ? 'POS' : 'Online',
            formatDate(hd.ngayTao),
            getCustomerName(hd),
            hd.sdt || '',
            hd.email || '',
            hd.tongTien || 0,
            getStatusLabel(hd.trangThaiHoaDon),
            hd.phuongThucThanhToan || '',
            hd.chiTietTraHangList ? hd.chiTietTraHangList.length : 0
        ]);

        const csvContent = [
            headers.join(','),
            ...csvData.map((row) =>
                row
                    .map((field) => {
                        const stringField = String(field);
                        if (stringField.includes(',') || stringField.includes('"') || stringField.includes('\n')) {
                            return `"${stringField.replace(/"/g, '""')}"`;
                        }
                        return stringField;
                    })
                    .join(',')
            )
        ].join('\n');

        const BOM = '\uFEFF';
        const csvWithBOM = BOM + csvContent;
        const blob = new Blob([csvWithBOM], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);

        const now = new Date();
        const dateStr = now.toISOString().split('T')[0];
        const filename = `HoaDon_TraHang_${dateStr}.csv`;

        link.setAttribute('href', url);
        link.setAttribute('download', filename);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);

        toast.add({
            severity: 'success',
            summary: 'Xuất dữ liệu thành công',
            detail: `Đã xuất ${filteredHoaDons.value.length} hóa đơn ra file ${filename}`,
            life: 3000
        });
    } catch (error) {
        console.error('Error exporting data:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi xuất dữ liệu',
            detail: 'Có lỗi xảy ra khi xuất file',
            life: 3000
        });
    }
}

function editPOSItems(hoaDon) {
    toast.add({
        severity: 'info',
        summary: 'Chỉnh sửa',
        detail: `Chỉnh sửa sản phẩm cho ${hoaDon.maHoaDon}`,
        life: 3000
    });
}

// ===== COMPUTED PROPERTIES =====
const filteredHoaDons = computed(() => {
    let filtered = [...hoaDons.value];

    // Tab filtering
    if (activeTabIndex.value === 1) {
        // POS tab
        filtered = filtered.filter((hd) => hd.loaiHoaDon && hd.loaiHoaDon.toUpperCase() === 'OFFLINE');
    } else if (activeTabIndex.value === 2) {
        // Online tab
        filtered = filtered.filter((hd) => hd.loaiHoaDon && hd.loaiHoaDon.toUpperCase() === 'ONLINE');
    } else if (activeTabIndex.value === 3) {
        // Pending tab
        filtered = filtered.filter((hd) => {
            const status = normalizeStatus(hd.trangThaiHoaDon);
            return ['PENDING', 'CONFIRMED', 'SHIPPING'].includes(status);
        });
    }

    // Search filtering
    if (searchKeyword.value.trim()) {
        const keyword = searchKeyword.value.toLowerCase();
        filtered = filtered.filter(
            (hd) => (hd.maHoaDon && hd.maHoaDon.toLowerCase().includes(keyword)) || getCustomerName(hd).toLowerCase().includes(keyword) || (hd.sdt && hd.sdt.includes(keyword)) || (hd.email && hd.email.toLowerCase().includes(keyword))
        );
    }

    // Type filtering
    if (typeFilter.value) {
        filtered = filtered.filter((hd) => hd.loaiHoaDon && hd.loaiHoaDon.toUpperCase() === typeFilter.value);
    }

    // Status filtering
    if (statusFilter.value) {
        filtered = filtered.filter((hd) => {
            const status = normalizeStatus(hd.trangThaiHoaDon);
            return status === statusFilter.value;
        });
    }

    // Date filtering
    if (dateFilter.value) {
        const filterDate = new Date(dateFilter.value).toDateString();
        filtered = filtered.filter((hd) => {
            const hoaDonDate = new Date(hd.ngayTao).toDateString();
            return hoaDonDate === filterDate;
        });
    }

    // Amount filtering
    if (minAmount.value || maxAmount.value) {
        filtered = filtered.filter((hd) => {
            const amount = parseFloat(hd.tongTien) || 0;
            const min = minAmount.value ? parseFloat(minAmount.value) : 0;
            const max = maxAmount.value ? parseFloat(maxAmount.value) : Infinity;
            return amount >= min && amount <= max;
        });
    }

    // Payment method filtering
    if (paymentMethodFilter.value) {
        filtered = filtered.filter((hd) => hd.phuongThucThanhToan === paymentMethodFilter.value);
    }

    // Return status filtering
    if (returnStatusFilter.value) {
        filtered = filtered.filter((hd) => {
            const hasReturns = hd.chiTietTraHangList && hd.chiTietTraHangList.length > 0;
            const hasPendingReturns = hasReturns && hd.chiTietTraHangList.some((r) => r.trangThaiHoaDon === 'PENDING');

            switch (returnStatusFilter.value) {
                case 'HAS_RETURNS':
                    return hasReturns;
                case 'NO_RETURNS':
                    return !hasReturns;
                case 'PENDING_RETURNS':
                    return hasPendingReturns;
                default:
                    return true;
            }
        });
    }

    return filtered;
});

const filteredChiTiets = computed(() => {
    if (!searchChiTietKeyword.value.trim()) {
        return hoaDonChiTiets.value;
    }

    const keyword = searchChiTietKeyword.value.toLowerCase();
    return hoaDonChiTiets.value.filter((item) => (item.tenSanPham && item.tenSanPham.toLowerCase().includes(keyword)) || (item.maSanPham && item.maSanPham.toLowerCase().includes(keyword)));
});

// Statistics
const posInvoices = computed(() => {
    return hoaDons.value.filter((hd) => {
        const type = hd.loaiHoaDon ? hd.loaiHoaDon.toUpperCase() : '';
        return type === 'OFFLINE';
    });
});

const onlineInvoices = computed(() => {
    return hoaDons.value.filter((hd) => {
        const type = hd.loaiHoaDon ? hd.loaiHoaDon.toUpperCase() : '';
        return type === 'ONLINE';
    });
});

const totalRevenue = computed(() => {
    return hoaDons.value
        .filter((hd) => {
            const status = normalizeStatus(hd.trangThaiHoaDon);
            return status === 'COMPLETED';
        })
        .reduce((sum, hd) => sum + (parseFloat(hd.tongTien) || 0), 0);
});

const posRevenue = computed(() => {
    return posInvoices.value
        .filter((hd) => {
            const status = normalizeStatus(hd.trangThaiHoaDon);
            return status === 'COMPLETED';
        })
        .reduce((sum, hd) => sum + (parseFloat(hd.tongTien) || 0), 0);
});

const onlineRevenue = computed(() => {
    return onlineInvoices.value
        .filter((hd) => {
            const status = normalizeStatus(hd.trangThaiHoaDon);
            return status === 'COMPLETED';
        })
        .reduce((sum, hd) => sum + (parseFloat(hd.tongTien) || 0), 0);
});

const completedInvoices = computed(() => {
    return hoaDons.value.filter((hd) => {
        const status = normalizeStatus(hd.trangThaiHoaDon);
        return status === 'COMPLETED';
    }).length;
});

const pendingInvoices = computed(() => {
    return hoaDons.value.filter((hd) => {
        const status = normalizeStatus(hd.trangThaiHoaDon);
        return ['PENDING', 'CONFIRMED', 'SHIPPING'].includes(status);
    }).length;
});

const completionRate = computed(() => {
    if (hoaDons.value.length === 0) return 0;
    return Math.round((completedInvoices.value / hoaDons.value.length) * 100);
});

const totalVoucherSavings = computed(() => {
    return hoaDons.value.reduce((total, hd) => {
        if (hd.chiTietVoucherList && hd.chiTietVoucherList.length > 0) {
            return total + calculateTotalVoucherSavings(hd.chiTietVoucherList);
        }
        return total;
    }, 0);
});

const invoicesWithVouchers = computed(() => {
    return hoaDons.value.filter((hd) => hasVoucherDiscount(hd)).length;
});

// ===== LIFECYCLE HOOKS =====
onMounted(() => {
    fetchAllData();
});
</script>

<template>
    <div class="invoice-management">
        <!-- Toast Component -->
        <Toast />

        <!-- Loading Overlay -->
        <div v-if="isLoading" class="loading-overlay">
            <div class="text-center">
                <ProgressSpinner style="width: 60px; height: 60px" strokeWidth="6" />
                <h5 class="mt-3 text-primary">{{ loadingMessage }}</h5>
            </div>
        </div>

        <!-- Header Section -->
        <div class="gradient-header mb-4 p-4">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                    <h2 class="h3 fw-bold mb-2">Quản Lý Hóa Đơn</h2>
                    <p class="text-white-50 mb-0">Hệ thống quản lý hóa đơn & trả hàng</p>
                </div>
                <div class="d-flex gap-2">
                    <Button @click="refreshAllData" :disabled="isLoading" class="btn btn-outline-light" size="small">
                        <i :class="isLoading ? 'pi pi-spinner pi-spin' : 'pi pi-refresh'" class="me-1"></i>
                        {{ isLoading ? 'Đang tải...' : 'Làm mới' }}
                    </Button>
                    <Button @click="exportData" class="btn btn-success" size="small">
                        <i class="pi pi-download me-1"></i>
                        Xuất dữ liệu
                    </Button>
                </div>
            </div>

            <!-- Tab Navigation -->
            <TabView v-model:activeIndex="activeTabIndex" @tab-change="onTabChange">
                <TabPanel leftIcon="pi pi-list">
                    <template #header>
                        <i class="pi pi-list me-2"></i>
                        Tất cả
                        <Badge :value="hoaDons.length" class="ms-2" />
                    </template>
                </TabPanel>
                <TabPanel leftIcon="pi pi-desktop">
                    <template #header>
                        <i class="pi pi-desktop me-2"></i>
                        POS
                        <Badge :value="posInvoices.length" class="ms-2" />
                    </template>
                </TabPanel>
                <TabPanel leftIcon="pi pi-globe">
                    <template #header>
                        <i class="pi pi-globe me-2"></i>
                        Online
                        <Badge :value="onlineInvoices.length" class="ms-2" />
                    </template>
                </TabPanel>
                <TabPanel leftIcon="pi pi-clock">
                    <template #header>
                        <i class="pi pi-clock me-2"></i>
                        Cần xử lý
                        <Badge :value="pendingInvoices" severity="warning" class="ms-2" />
                    </template>
                </TabPanel>
            </TabView>
        </div>

        <!-- Statistics Cards -->
        <div class="row g-3 mb-4">
            <div class="col-md-6 col-lg-3">
                <div class="stat-card card border-0">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="mb-1 text-primary">Tổng hóa đơn</h6>
                                <h3 class="mb-2 text-primary">{{ hoaDons.length }}</h3>
                                <div class="d-flex gap-2">
                                    <span class="badge bg-info">POS: {{ posInvoices.length }}</span>
                                    <span class="badge bg-success">Online: {{ onlineInvoices.length }}</span>
                                </div>
                            </div>
                            <i class="pi pi-file-text text-primary" style="font-size: 2.5rem"></i>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6 col-lg-3">
                <div class="stat-card card border-0">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-success mb-1">Tổng doanh thu</h6>
                                <h3 class="text-success mb-2">{{ formatCurrency(totalRevenue) }}</h3>
                                <div class="d-flex gap-2">
                                    <span class="badge bg-warning text-dark">POS: {{ formatCurrency(posRevenue) }}</span>
                                    <span class="badge bg-info">Online: {{ formatCurrency(onlineRevenue) }}</span>
                                </div>
                            </div>
                            <i class="pi pi-money-bill text-success" style="font-size: 2.5rem"></i>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-6 col-lg-3">
                <div class="stat-card card border-0">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-info mb-1">Voucher</h6>
                                <h3 class="text-info mb-2">{{ formatCurrency(totalVoucherSavings) }}</h3>
                                <div class="d-flex gap-2">
                                    <span class="badge bg-primary">{{ invoicesWithVouchers }} HĐ</span>
                                    <span class="badge bg-success">Tiết kiệm</span>
                                </div>
                            </div>
                            <i class="pi pi-ticket text-info" style="font-size: 2.5rem"></i>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-6 col-lg-3">
                <div class="stat-card card border-0">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-info mb-1">Hoàn thành</h6>
                                <h3 class="text-info mb-2">{{ completedInvoices }}</h3>
                                <span class="badge bg-secondary">Tỷ lệ: {{ completionRate }}%</span>
                            </div>
                            <i class="pi pi-check-circle text-info" style="font-size: 2.5rem"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Search and Filter -->
        <div class="card mb-4 border-0 shadow-sm">
            <div class="card-body">
                <div class="row g-3 align-items-end">
                    <div class="col-md-4">
                        <label class="form-label fw-semibold">Tìm kiếm</label>
                        <div class="input-group">
                            <span class="input-group-text">
                                <i class="pi pi-search"></i>
                            </span>
                            <input v-model="searchKeyword" @input="onSearch" class="form-control" placeholder="Tìm kiếm hóa đơn, khách hàng..." />
                        </div>
                    </div>

                    <div class="col-md-2">
                        <label class="form-label fw-semibold">Loại hóa đơn</label>
                        <select v-model="typeFilter" @change="applyFilters" class="form-select">
                            <option value="">Tất cả loại</option>
                            <option value="OFFLINE">POS (Tại quầy)</option>
                            <option value="ONLINE">Online</option>
                        </select>
                    </div>

                    <div class="col-md-2">
                        <label class="form-label fw-semibold">Trạng thái</label>
                        <select v-model="statusFilter" @change="applyFilters" class="form-select">
                            <option value="">Tất cả trạng thái</option>
                            <option value="PENDING">Chờ xác nhận</option>
                            <option value="CONFIRMED">Đã xác nhận</option>
                            <option value="SHIPPING">Đang giao</option>
                            <option value="COMPLETED">Hoàn thành</option>
                            <option value="CANCELLED">Đã hủy</option>
                        </select>
                    </div>

                    <div class="col-md-2">
                        <label class="form-label fw-semibold">Ngày tạo</label>
                        <Calendar v-model="dateFilter" @date-select="applyFilters" placeholder="Chọn ngày" dateFormat="dd/mm/yy" class="w-100" />
                    </div>

                    <div class="col-md-2">
                        <Button @click="showAdvancedFilter = !showAdvancedFilter" class="btn btn-outline-secondary w-100" outlined>
                            <i class="pi pi-filter me-1"></i>
                            Lọc nâng cao
                        </Button>
                    </div>
                </div>

                <!-- Advanced Filters -->
                <div v-if="showAdvancedFilter" class="border-top mt-3 pt-3">
                    <h6 class="fw-semibold mb-3">Bộ lọc nâng cao</h6>
                    <div class="row g-3">
                        <div class="col-md-4">
                            <label class="form-label">Khoảng tiền</label>
                            <div class="row g-2">
                                <div class="col">
                                    <InputNumber v-model="minAmount" placeholder="Từ" mode="currency" currency="VND" locale="vi-VN" />
                                </div>
                                <div class="col">
                                    <InputNumber v-model="maxAmount" placeholder="Đến" mode="currency" currency="VND" locale="vi-VN" />
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Phương thức thanh toán</label>
                            <select v-model="paymentMethodFilter" class="form-select">
                                <option value="">Tất cả</option>
                                <option value="CASH">Tiền mặt</option>
                                <option value="CARD">Thẻ</option>
                                <option value="BANK_TRANSFER">Chuyển khoản</option>
                                <option value="E_WALLET">Ví điện tử</option>
                                <option value="COD">COD</option>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Trạng thái trả hàng</label>
                            <select v-model="returnStatusFilter" class="form-select">
                                <option value="">Tất cả</option>
                                <option value="HAS_RETURNS">Có trả hàng</option>
                                <option value="NO_RETURNS">Không trả hàng</option>
                                <option value="PENDING_RETURNS">Chờ xử lý trả</option>
                            </select>
                        </div>
                    </div>
                    <div class="mt-3">
                        <Button @click="applyAdvancedFilters" class="me-2">Áp dụng</Button>
                        <Button @click="clearAllFilters" outlined>Xóa bộ lọc</Button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Error State -->
        <div v-if="hasError" class="py-5 text-center">
            <i class="pi pi-exclamation-triangle text-danger display-1 mb-3"></i>
            <h4 class="text-danger mb-2">Lỗi kết nối API</h4>
            <p class="text-muted mb-3">{{ errorMessage }}</p>
            <div class="d-flex justify-content-center gap-2">
                <Button @click="retryConnection" severity="danger">
                    <i class="pi pi-refresh me-1"></i>
                    Thử lại
                </Button>
                <Button @click="goToLogin" outlined v-if="errorMessage.includes('xác thực')">
                    <i class="pi pi-sign-in me-1"></i>
                    Đăng nhập lại
                </Button>
            </div>
        </div>

        <!-- Data Table -->
        <div v-else class="table-container">
            <DataTable
                :value="filteredHoaDons"
                dataKey="id"
                :paginator="true"
                :rows="itemsPerPage"
                :totalRecords="filteredHoaDons.length"
                :loading="isLoading"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                :rowsPerPageOptions="[10, 25, 50, 100]"
                currentPageReportTemplate="Hiển thị {first} đến {last} trong tổng số {totalRecords} hóa đơn"
                tableStyle="min-width: 60rem"
                sortMode="single"
                @sort="onSort"
                class="p-datatable-lg"
                responsiveLayout="scroll"
            >
                <Column field="id" header="ID" sortable style="min-width: 8rem">
                    <template #body="slotProps">
                        <span class="fw-bold text-primary">#{{ slotProps.data.id }}</span>
                    </template>
                </Column>

                <Column field="maHoaDon" header="Mã HĐ" sortable style="min-width: 10rem">
                    <template #body="slotProps">
                        <Tag :value="slotProps.data.maHoaDon" severity="secondary" />
                    </template>
                </Column>

                <Column field="loaiHoaDon" header="Loại" style="min-width: 8rem">
                    <template #body="slotProps">
                        <Tag
                            :value="slotProps.data.loaiHoaDon === 'OFFLINE' ? 'POS' : 'Online'"
                            :severity="slotProps.data.loaiHoaDon === 'OFFLINE' ? 'warning' : 'success'"
                            :icon="slotProps.data.loaiHoaDon === 'OFFLINE' ? 'pi pi-desktop' : 'pi pi-globe'"
                        />
                    </template>
                </Column>

                <Column field="ngayTao" header="Ngày tạo" sortable style="min-width: 12rem">
                    <template #body="slotProps">
                        <div class="d-flex align-items-center">
                            <i class="pi pi-calendar text-muted me-2"></i>
                            <small>{{ formatDate(slotProps.data.ngayTao) }}</small>
                        </div>
                    </template>
                </Column>

                <Column field="tenKhachHang" header="Khách hàng" style="min-width: 16rem">
                    <template #body="slotProps">
                        <div class="d-flex align-items-center">
                            <div class="avatar rounded-circle d-flex align-items-center justify-content-center me-2 bg-primary text-white" style="width: 32px; height: 32px; font-size: 12px">
                                {{ getInitials(getCustomerName(slotProps.data)) }}
                            </div>
                            <div>
                                <div class="fw-semibold">{{ getCustomerName(slotProps.data) }}</div>
                                <small v-if="slotProps.data.sdt" class="text-muted"> <i class="pi pi-phone me-1"></i>{{ slotProps.data.sdt }} </small>
                            </div>
                        </div>
                    </template>
                </Column>

                <Column field="tongTien" header="Tổng tiền" sortable style="min-width: 12rem">
                    <template #body="slotProps">
                        <div class="d-flex align-items-center">
                            <i class="pi pi-money-bill text-success me-2"></i>
                            <span class="fw-bold text-success">{{ formatCurrency(slotProps.data.tongTien) }}</span>
                        </div>
                    </template>
                </Column>

                <Column header="Voucher" style="min-width: 12rem">
                    <template #body="slotProps">
                        <div v-if="hasVoucherDiscount(slotProps.data)" class="d-flex align-items-center">
                            <i class="pi pi-ticket text-success me-2"></i>
                            <div class="voucher-stats">
                                <Tag :value="slotProps.data.soLuongVoucherDaApDung || slotProps.data.chiTietVoucherList?.length || 0" severity="success" icon="pi pi-ticket" class="voucher-count me-1" />
                                <div class="voucher-savings">
                                    {{ formatCurrency(slotProps.data.tongTienVoucherChiTiet || calculateTotalVoucherSavings(slotProps.data.chiTietVoucherList)) }}
                                </div>
                            </div>
                        </div>
                        <small v-else class="text-muted">Không có</small>
                    </template>
                </Column>

                <Column field="trangThaiHoaDon" header="Trạng thái" style="min-width: 12rem">
                    <template #body="slotProps">
                        <Tag :value="getStatusLabel(slotProps.data.trangThaiHoaDon)" :severity="getStatusSeverity(slotProps.data.trangThaiHoaDon)" :icon="getStatusIcon(slotProps.data.trangThaiHoaDon)" />
                    </template>
                </Column>

                <Column header="Thao tác" style="min-width: 14rem">
                    <template #body="slotProps">
                        <div class="d-flex gap-1">
                            <Button @click="viewChiTiet(slotProps.data)" size="small" outlined title="Xem chi tiết">
                                <i class="pi pi-eye"></i>
                            </Button>
                            <Button v-if="hasVoucherDiscount(slotProps.data)" @click="showVoucherDetail(slotProps.data)" size="small" severity="info" outlined title="Xem voucher">
                                <i class="pi pi-ticket"></i>
                            </Button>
                            <Button v-if="canUpdateStatus(slotProps.data)" @click="updateInvoiceStatus(slotProps.data)" size="small" severity="success" outlined title="Cập nhật trạng thái">
                                <i class="pi pi-arrow-right"></i>
                            </Button>
                        </div>
                    </template>
                </Column>

                <!-- Empty State -->
                <template #empty>
                    <div class="py-5 text-center">
                        <i class="pi pi-file-o text-muted display-1 mb-3"></i>
                        <h5 class="text-muted mb-2">Không tìm thấy hóa đơn</h5>
                        <p class="text-muted">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm.</p>
                    </div>
                </template>
            </DataTable>
        </div>

        <!-- Modal chi tiết hóa đơn -->
        <Dialog v-model:visible="showChiTietDialog" modal :style="{ width: '95vw' }" :maximizable="true" header="Chi tiết hóa đơn" class="p-fluid">
            <template #header>
                <div class="d-flex align-items-center">
                    <i class="pi pi-file-text me-2 text-primary"></i>
                    <span>Chi tiết hóa đơn {{ selectedHoaDon?.maHoaDon }}</span>
                </div>
            </template>

            <div v-if="selectedHoaDon" class="invoice-detail-content">
                <!-- Workflow Progress -->
                <div class="card mb-4 border-0 shadow-sm">
                    <div class="card-header bg-light">
                        <h6 class="card-title fw-semibold mb-0">
                            <i class="pi pi-sitemap me-2"></i>
                            Tiến trình xử lý
                        </h6>
                    </div>
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between align-items-center workflow-container">
                            <div v-for="(step, index) in getWorkflowSteps(selectedHoaDon.loaiHoaDon)" :key="step" class="d-flex flex-fill align-items-center">
                                <div class="d-flex flex-column align-items-center flex-fill">
                                    <div :class="getWorkflowStepClass(selectedHoaDon, step)">
                                        <i :class="getStepIcon(step)"></i>
                                    </div>
                                    <small class="fw-medium mt-2 text-center">{{ getStepLabel(step) }}</small>
                                    <small v-if="isStepActive(selectedHoaDon, step)" class="text-primary">Đang thực hiện</small>
                                    <small v-else-if="isStepCompleted(selectedHoaDon, step)" class="text-success">Hoàn thành</small>
                                </div>
                                <div v-if="index < getWorkflowSteps(selectedHoaDon.loaiHoaDon).length - 1" :class="getWorkflowConnectorClass(selectedHoaDon, step)"></div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Action Buttons -->
                <div class="d-flex justify-content-end bg-light mb-4 gap-2 rounded p-3">
                    <Button v-if="canProcessNextStep(selectedHoaDon)" @click="processNextStepWithRefresh(selectedHoaDon)" severity="success" size="small">
                        <i class="pi pi-arrow-right me-1"></i>
                        {{ getNextStepAction(selectedHoaDon) }}
                    </Button>

                    <Button v-if="canUpdateStatus(selectedHoaDon)" @click="updateInvoiceStatus(selectedHoaDon)" severity="warning" size="small" outlined>
                        <i class="pi pi-edit me-1"></i>
                        Cập nhật trạng thái
                    </Button>

                    <Button v-if="canCancelInvoice(selectedHoaDon)" @click="cancelInvoice(selectedHoaDon)" severity="danger" size="small" outlined>
                        <i class="pi pi-times me-1"></i>
                        Hủy đơn
                    </Button>
                </div>

                <!-- Thông tin chi tiết -->
                <div class="row g-3 mb-4">
                    <!-- Thông tin hóa đơn -->
                    <div class="col-lg-4">
                        <div class="card h-100 border-0 shadow-sm">
                            <div class="card-header bg-primary text-white">
                                <h6 class="card-title mb-0 text-white">
                                    <i class="pi pi-file me-2"></i>
                                    Thông tin hóa đơn
                                </h6>
                            </div>
                            <div class="card-body">
                                <div class="invoice-info">
                                    <div class="info-row">
                                        <strong>Mã HĐ:</strong>
                                        <Tag :value="selectedHoaDon.maHoaDon" severity="secondary" />
                                    </div>
                                    <div class="info-row">
                                        <strong>Loại:</strong>
                                        <Tag :value="selectedHoaDon.loaiHoaDon === 'OFFLINE' ? 'POS' : 'Online'" :severity="selectedHoaDon.loaiHoaDon === 'OFFLINE' ? 'warning' : 'success'" />
                                    </div>
                                    <div class="info-row">
                                        <strong>Ngày tạo:</strong>
                                        <span>{{ formatDate(selectedHoaDon.ngayTao) }}</span>
                                    </div>
                                    <div class="info-row">
                                        <strong>Tổng tiền:</strong>
                                        <span class="fw-bold text-success">{{ formatCurrency(selectedHoaDon.tongTien) }}</span>
                                    </div>
                                    <div class="info-row">
                                        <strong>Trạng thái:</strong>
                                        <Tag :value="getStatusLabel(selectedHoaDon.trangThaiHoaDon)" :severity="getStatusSeverity(selectedHoaDon.trangThaiHoaDon)" />
                                    </div>
                                    <div v-if="selectedHoaDon.tenNhanVien" class="info-row">
                                        <strong>Nhân viên:</strong>
                                        <span>{{ selectedHoaDon.tenNhanVien }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Thông tin khách hàng -->
                    <div class="col-lg-4">
                        <div class="card h-100 border-0 shadow-sm">
                            <div class="card-header bg-info text-white">
                                <h6 class="card-title mb-0 text-white">
                                    <i class="pi pi-user me-2"></i>
                                    Thông tin khách hàng
                                </h6>
                            </div>
                            <div class="card-body">
                                <div class="invoice-info">
                                    <div class="info-row">
                                        <strong>Tên:</strong>
                                        <span>{{ getCustomerName(selectedHoaDon) }}</span>
                                    </div>
                                    <div v-if="selectedHoaDon.sdt" class="info-row">
                                        <strong>SĐT:</strong>
                                        <span>{{ selectedHoaDon.sdt }}</span>
                                    </div>
                                    <div v-if="selectedHoaDon.email" class="info-row">
                                        <strong>Email:</strong>
                                        <span>{{ selectedHoaDon.email }}</span>
                                    </div>
                                    <div v-if="selectedHoaDon.diaChi" class="info-row">
                                        <strong>Địa chỉ:</strong>
                                        <span>{{ selectedHoaDon.diaChi }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-4">
                        <div class="card h-100 border-0 shadow-sm">
                            <div class="card-header bg-success text-white">
                                <h6 class="card-title mb-0 text-white">
                                    <i class="pi pi-ticket me-2"></i>
                                    Thông tin voucher
                                </h6>
                            </div>
                            <div class="card-body">
                                <div v-if="selectedHoaDon.chiTietVoucherList && selectedHoaDon.chiTietVoucherList.length > 0" class="invoice-info">
                                    <div class="info-row">
                                        <strong>Số voucher:</strong>
                                        <Tag :value="selectedHoaDon.soLuongVoucherDaApDung || selectedHoaDon.chiTietVoucherList.length" severity="success" />
                                    </div>
                                    <div class="info-row">
                                        <strong>Tổng tiết kiệm:</strong>
                                        <span class="fw-bold text-success">
                                            {{ formatCurrency(selectedHoaDon.tongTienVoucherChiTiet || calculateTotalVoucherSavings(selectedHoaDon.chiTietVoucherList)) }}
                                        </span>
                                    </div>
                                    <div class="info-row">
                                        <strong>% tiết kiệm:</strong>
                                        <span class="fw-bold text-info"> {{ selectedHoaDon.phanTramTietKiemVoucher?.toFixed(1) || '0.0' }}% </span>
                                    </div>
                                    <div v-if="getPrimaryVoucher(selectedHoaDon.chiTietVoucherList)" class="info-row">
                                        <strong>Voucher chính:</strong>
                                        <div>
                                            <Tag :value="getPrimaryVoucher(selectedHoaDon.chiTietVoucherList).tenVoucher" severity="primary" />
                                            <div class="mt-1">
                                                <small class="text-success fw-bold">
                                                    {{ formatCurrency(getPrimaryVoucher(selectedHoaDon.chiTietVoucherList).soTienGiam) }}
                                                </small>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div v-else class="text-muted text-center">
                                    <i class="pi pi-info-circle display-4 mb-2"></i>
                                    <p>Không sử dụng voucher</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Chi tiết sản phẩm -->
                <div class="card mb-4 border-0 shadow-sm">
                    <div class="card-header bg-light">
                        <div class="d-flex justify-content-between align-items-center">
                            <h6 class="card-title fw-semibold mb-0">
                                <i class="pi pi-list me-2"></i>
                                Chi tiết sản phẩm ({{ hoaDonChiTiets.length }} mặt hàng)
                            </h6>
                            <div class="d-flex gap-2">
                                <div class="input-group input-group-sm" style="width: 250px">
                                    <span class="input-group-text">
                                        <i class="pi pi-search"></i>
                                    </span>
                                    <input v-model="searchChiTietKeyword" class="form-control" placeholder="Tìm kiếm sản phẩm..." />
                                </div>
                                <Button v-if="canEditItems(selectedHoaDon)" @click="editPOSItems(selectedHoaDon)" size="small" outlined>
                                    <i class="pi pi-pencil me-1"></i>
                                    Sửa
                                </Button>
                            </div>
                        </div>
                    </div>
                    <div class="card-body">
                        <!-- Loading Chi Tiết -->
                        <div v-if="isLoadingChiTiet" class="py-4 text-center">
                            <ProgressSpinner style="width: 40px; height: 40px" strokeWidth="6" />
                            <p class="text-muted mt-2">Đang tải chi tiết hóa đơn...</p>
                        </div>

                        <!-- Chi Tiết Table -->
                        <div v-else>
                            <DataTable :value="filteredChiTiets" class="p-datatable-sm" responsiveLayout="scroll" :paginator="hoaDonChiTiets.length > 10" :rows="10">
                                <Column field="stt" header="STT" style="width: 60px">
                                    <template #body="slotProps">
                                        <Tag :value="slotProps.index + 1" severity="secondary" />
                                    </template>
                                </Column>

                                <Column field="tenSanPham" header="Sản phẩm" style="min-width: 200px">
                                    <template #body="slotProps">
                                        <div class="d-flex align-items-center">
                                            <div v-if="slotProps.data.hinhAnh" class="product-image me-3">
                                                <img :src="slotProps.data.hinhAnh" alt="Product" class="rounded" style="width: 40px; height: 40px; object-fit: cover" />
                                            </div>
                                            <div>
                                                <div class="fw-medium">{{ slotProps.data.tenSanPham || 'N/A' }}</div>
                                                <small v-if="slotProps.data.maSanPham" class="text-muted"> Mã: {{ slotProps.data.maSanPham }} </small>
                                            </div>
                                        </div>
                                    </template>
                                </Column>

                                <Column field="thuocTinh" header="Thuộc tính" style="min-width: 120px">
                                    <template #body="slotProps">
                                        <div class="d-flex flex-column gap-1">
                                            <Tag v-if="slotProps.data.mauSac && slotProps.data.mauSac !== 'N/A'" :value="slotProps.data.mauSac" severity="info" size="small" />
                                            <Tag v-if="slotProps.data.kichThuoc && slotProps.data.kichThuoc !== 'N/A'" :value="slotProps.data.kichThuoc" severity="success" size="small" />
                                        </div>
                                    </template>
                                </Column>

                                <Column field="giaBan" header="Giá bán" style="min-width: 120px">
                                    <template #body="slotProps">
                                        <div>
                                            <span class="fw-bold text-success">{{ formatCurrency(slotProps.data.giaBan) }}</span>
                                            <div v-if="slotProps.data.giaGoc && slotProps.data.giaGoc !== slotProps.data.giaBan">
                                                <small class="text-muted text-decoration-line-through">
                                                    {{ formatCurrency(slotProps.data.giaGoc) }}
                                                </small>
                                            </div>
                                        </div>
                                    </template>
                                </Column>

                                <Column field="soLuong" header="Số lượng" style="min-width: 100px">
                                    <template #body="slotProps">
                                        <div v-if="isEditingItem(slotProps.data.id)" class="d-flex align-items-center gap-1">
                                            <InputNumber v-model="editQuantity" :min="1" size="small" style="width: 80px" />
                                            <Button @click="saveQuantity(slotProps.data.id)" severity="success" size="small">
                                                <i class="pi pi-check"></i>
                                            </Button>
                                            <Button @click="cancelEdit()" severity="secondary" size="small">
                                                <i class="pi pi-times"></i>
                                            </Button>
                                        </div>
                                        <Tag v-else :value="slotProps.data.soLuong" />
                                    </template>
                                </Column>

                                <Column field="thanhTien" header="Thành tiền" style="min-width: 120px">
                                    <template #body="slotProps">
                                        <div>
                                            <span class="fw-bold text-primary">
                                                {{ formatCurrency(slotProps.data.thanhTien || slotProps.data.giaBan * slotProps.data.soLuong) }}
                                            </span>
                                            <div v-if="slotProps.data.tienTietKiem && slotProps.data.tienTietKiem > 0">
                                                <small class="text-success">
                                                    <i class="pi pi-tag me-1"></i>
                                                    Tiết kiệm: {{ formatCurrency(slotProps.data.tienTietKiem) }}
                                                </small>
                                            </div>
                                        </div>
                                    </template>
                                </Column>

                                <!-- <Column v-if="canEditItems(selectedHoaDon)" header="Thao tác" style="min-width: 100px">
                                    <template #body="slotProps">
                                        <div class="d-flex gap-1">
                                            <Button v-if="!isEditingItem(slotProps.data.id)" @click="editItem(slotProps.data)" size="small" outlined title="Sửa số lượng">
                                                <i class="pi pi-pencil"></i>
                                            </Button>
                                            <Button @click="removeItem(slotProps.data.id)" severity="danger" size="small" outlined title="Xóa sản phẩm">
                                                <i class="pi pi-trash"></i>
                                            </Button>
                                        </div>
                                    </template>
                                </Column> -->

                                <!-- Empty State for Chi Tiet -->
                                <template #empty>
                                    <div class="py-4 text-center">
                                        <i class="pi pi-info-circle text-muted display-4 mb-3"></i>
                                        <h6 class="text-muted mb-2">Không có chi tiết nào</h6>
                                        <p class="text-muted">Hóa đơn này chưa có chi tiết sản phẩm.</p>
                                    </div>
                                </template>
                            </DataTable>

                            <!-- Tổng kết -->
                            <div class="row g-3 border-top mt-3 pt-3">
                                <!-- Số mặt hàng -->
                                <div class="col-md-2 text-center">
                                    <div class="statistic-item">
                                        <div class="h4 mb-1 text-primary">{{ hoaDonChiTiets.length }}</div>
                                        <div class="text-muted small">Số mặt hàng</div>
                                    </div>
                                </div>

                                <!-- Tổng số lượng -->
                                <div class="col-md-2 text-center">
                                    <div class="statistic-item">
                                        <div class="h4 text-info mb-1">{{ getTotalQuantity() }}</div>
                                        <div class="text-muted small">Tổng số lượng</div>
                                    </div>
                                </div>

                                <!-- Tổng tiền hàng -->
                                <div class="col-md-2 text-center">
                                    <div class="statistic-item">
                                        <div class="h4 text-success mb-1">{{ formatCurrency(getTotalAmount()) }}</div>
                                        <div class="text-muted small">Tổng tiền hàng</div>
                                    </div>
                                </div>

                                <!-- THÊM MỚI: Tổng tiền giảm giá -->
                                <div class="col-md-2 text-center">
                                    <div class="statistic-item">
                                        <div class="h4 text-danger mb-1">-{{ formatCurrency(getTotalDiscount()) }}</div>
                                        <div class="text-muted small">Tổng giảm giá</div>
                                    </div>
                                </div>

                                <!-- THÊM MỚI: Phí vận chuyển -->
                                <div class="col-md-2 text-center">
                                    <div class="statistic-item">
                                        <div class="h4 text-warning mb-1">{{ formatCurrency(selectedHoaDon.phiVanChuyen || 0) }}</div>
                                        <div class="text-muted small">Phí vận chuyển</div>
                                    </div>
                                </div>

                                <!-- Thành tiền cuối cùng -->
                                <div class="col-md-2 text-center">
                                    <div class="statistic-item">
                                        <div class="h4 fw-bold mb-1 text-primary">{{ formatCurrency(selectedHoaDon.tongThanhToan || selectedHoaDon.tongTien) }}</div>
                                        <div class="text-muted small">Thành tiền</div>
                                    </div>
                                </div>
                            </div>

                            <!-- THÊM MỚI: Button hiển thị breakdown -->
                            <div class="mt-3">
                                <Button @click="showDetailedBreakdown = !showDetailedBreakdown" size="small" outlined class="w-100">
                                    <i :class="showDetailedBreakdown ? 'pi pi-chevron-up' : 'pi pi-chevron-down'" class="me-1"></i>
                                    {{ showDetailedBreakdown ? 'Ẩn' : 'Hiện' }} chi tiết tính toán
                                </Button>
                            </div>

                            <!-- THÊM MỚI: Chi tiết breakdown -->
                            <div v-if="showDetailedBreakdown" class="bg-light mt-3 rounded border p-3">
                                <h6 class="fw-semibold mb-3">
                                    <i class="pi pi-calculator me-2 text-primary"></i>
                                    Chi tiết tính toán
                                </h6>

                                <div class="calculation-breakdown">
                                    <!-- Tiền gốc sản phẩm -->
                                    <div class="d-flex justify-content-between align-items-center border-bottom py-2">
                                        <span>Tổng tiền gốc sản phẩm:</span>
                                        <span class="text-muted">{{ formatCurrency(getTotalOriginalAmount()) }}</span>
                                    </div>

                                    <!-- Giảm giá sản phẩm -->
                                    <div v-if="getTotalOriginalAmount() > getTotalAmount()" class="d-flex justify-content-between align-items-center border-bottom py-2">
                                        <span>Giảm giá sản phẩm:</span>
                                        <span class="text-danger">-{{ formatCurrency(getTotalOriginalAmount() - getTotalAmount()) }}</span>
                                    </div>

                                    <!-- Tiền hàng sau giảm -->
                                    <div class="d-flex justify-content-between align-items-center border-bottom py-2">
                                        <span class="fw-medium">Tiền hàng (sau giảm):</span>
                                        <span class="fw-medium text-success">{{ formatCurrency(getTotalAmount()) }}</span>
                                    </div>

                                    <!-- Phí vận chuyển -->
                                    <div v-if="selectedHoaDon.phiVanChuyen && selectedHoaDon.phiVanChuyen > 0" class="d-flex justify-content-between align-items-center border-bottom py-2">
                                        <span>Phí vận chuyển:</span>
                                        <span class="text-warning">+{{ formatCurrency(selectedHoaDon.phiVanChuyen) }}</span>
                                    </div>

                                    <!-- Tổng trước voucher/điểm -->
                                    <div class="d-flex justify-content-between align-items-center border-bottom py-2">
                                        <span class="fw-medium">Tổng tiền:</span>
                                        <span class="fw-medium">{{ formatCurrency(selectedHoaDon.tongTien || getTotalAmount() + (selectedHoaDon.phiVanChuyen || 0)) }}</span>
                                    </div>

                                    <!-- Voucher -->
                                    <div v-if="selectedHoaDon.tongTienVoucherChiTiet && selectedHoaDon.tongTienVoucherChiTiet > 0" class="d-flex justify-content-between align-items-center border-bottom py-2">
                                        <span>
                                            <i class="pi pi-ticket text-success me-1"></i>
                                            Voucher ({{ selectedHoaDon.soLuongVoucherDaApDung || selectedHoaDon.chiTietVoucherList?.length || 0 }} mã):
                                        </span>
                                        <span class="text-success">-{{ formatCurrency(selectedHoaDon.tongTienVoucherChiTiet) }}</span>
                                    </div>

                                    <!-- Điểm -->
                                    <div v-if="selectedHoaDon.tienDiem && selectedHoaDon.tienDiem > 0" class="d-flex justify-content-between align-items-center border-bottom py-2">
                                        <span>
                                            <i class="pi pi-star text-warning me-1"></i>
                                            Điểm ({{ selectedHoaDon.diemSuDung || 0 }} điểm):
                                        </span>
                                        <span class="text-warning">-{{ formatCurrency(selectedHoaDon.tienDiem) }}</span>
                                    </div>

                                    <!-- Tổng thanh toán cuối cùng -->
                                    <div class="calculation-final mt-3 rounded bg-primary bg-opacity-10 p-3">
                                        <div class="d-flex justify-content-between align-items-center mb-2">
                                            <span class="fw-medium">Tổng (hàng + vận chuyển):</span>
                                            <span class="fw-medium">{{ formatCurrency(getTotalBeforeVoucher()) }}</span>
                                        </div>
                                        <div v-if="selectedHoaDon.tongTienVoucherChiTiet && selectedHoaDon.tongTienVoucherChiTiet > 0" class="d-flex justify-content-between align-items-center mb-2">
                                            <span class="text-success">Voucher giảm:</span>
                                            <span class="text-success fw-medium">-{{ formatCurrency(selectedHoaDon.tongTienVoucherChiTiet) }}</span>
                                        </div>
                                        <div v-if="selectedHoaDon.tienDiem && selectedHoaDon.tienDiem > 0" class="d-flex justify-content-between align-items-center mb-2">
                                            <span class="text-warning">Điểm giảm:</span>
                                            <span class="text-warning fw-medium">-{{ formatCurrency(selectedHoaDon.tienDiem) }}</span>
                                        </div>
                                        <div class="border-top pt-2">
                                            <div class="d-flex justify-content-between align-items-center">
                                                <span class="fw-bold h6 mb-0">THANH TOÁN:</span>
                                                <span class="fw-bold h5 mb-0 text-primary">{{ formatCurrency(selectedHoaDon.tongThanhToan || selectedHoaDon.tongTien) }}</span>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Thống kê tiết kiệm -->
                                    <div v-if="getTotalDiscount() > 0" class="bg-success mt-3 rounded bg-opacity-10 p-2 text-center">
                                        <small class="text-success fw-medium">
                                            <i class="pi pi-check-circle me-1"></i>
                                            Tiết kiệm: {{ formatCurrency(getTotalDiscount()) }}
                                        </small>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="selectedHoaDon.chiTietVoucherList && selectedHoaDon.chiTietVoucherList.length > 0" class="card mb-4 border-0 shadow-sm">
                    <div class="card-header bg-success text-white">
                        <h6 class="card-title fw-semibold mb-0 text-white">
                            <i class="pi pi-ticket me-2"></i>
                            Chi tiết voucher áp dụng ({{ selectedHoaDon.chiTietVoucherList.length }} voucher)
                        </h6>
                    </div>
                    <div class="card-body">
                        <div class="row g-3">
                            <div v-for="voucher in selectedHoaDon.chiTietVoucherList" :key="voucher.id" class="col-md-6">
                                <div class="voucher-item p-3">
                                    <div class="d-flex justify-content-between align-items-start mb-2">
                                        <div>
                                            <h6 class="mb-1 text-primary">{{ voucher.tenVoucher }}</h6>
                                            <small class="text-muted">{{ voucher.maVoucher }}</small>
                                        </div>
                                        <Tag :value="getVoucherTypeLabel(voucher.loaiGiamGia)" :severity="getVoucherTypeSeverity(voucher.loaiGiamGia)" />
                                    </div>

                                    <div class="voucher-details">
                                        <div class="d-flex justify-content-between mb-2">
                                            <span>Giá trị giảm:</span>
                                            <div>
                                                <strong v-if="voucher.loaiGiamGia === 'PHAN_TRAM'"> {{ voucher.giaTriGiam }}% </strong>
                                                <strong v-else>
                                                    {{ formatCurrency(voucher.giaTriGiam) }}
                                                </strong>
                                            </div>
                                        </div>
                                        <div class="d-flex justify-content-between mb-2">
                                            <span>Số tiền giảm:</span>
                                            <strong class="text-success">{{ formatCurrency(voucher.soTienGiam) }}</strong>
                                        </div>
                                        <div class="d-flex justify-content-between mb-2">
                                            <span>Giá trị đơn hàng:</span>
                                            <span>{{ formatCurrency(voucher.giaTriDonHang) }}</span>
                                        </div>
                                        <div class="d-flex justify-content-between">
                                            <span>Ngày áp dụng:</span>
                                            <span>{{ formatDate(voucher.ngayApDung) }}</span>
                                        </div>

                                        <!-- Hiển thị giới hạn nếu có -->
                                        <div v-if="voucher.giaTriGiamToiDa" class="border-top mt-2 pt-2">
                                            <small class="text-muted">
                                                <i class="pi pi-info-circle me-1"></i>
                                                Giảm tối đa: {{ formatCurrency(voucher.giaTriGiamToiDa) }}
                                            </small>
                                        </div>
                                        <div v-if="voucher.giaTriGiamToiThieu" class="mt-1">
                                            <small class="text-muted">
                                                <i class="pi pi-info-circle me-1"></i>
                                                Đơn tối thiểu: {{ formatCurrency(voucher.giaTriGiamToiThieu) }}
                                            </small>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Tổng kết voucher -->
                        <div class="voucher-summary mt-4 rounded p-3">
                            <h6 class="mb-3 text-white">Tổng quan voucher</h6>

                            <!-- Thông tin voucher -->
                            <div class="row mb-3 text-center text-white">
                                <div class="col-6">
                                    <div class="h5 mb-1">{{ selectedHoaDon.chiTietVoucherList.length }}</div>
                                    <small>Số voucher áp dụng</small>
                                </div>
                                <div class="col-6">
                                    <div class="h5 mb-1">{{ formatCurrency(calculateTotalVoucherSavings(selectedHoaDon.chiTietVoucherList)) }}</div>
                                    <small>Tổng tiền giảm</small>
                                </div>
                            </div>

                            <!-- Chi tiết tính toán voucher -->
                            <div class="voucher-calculation bg-dark rounded bg-opacity-25 p-3">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <span class="text-white">Tổng tiền trước voucher:</span>
                                    <span class="fw-bold text-white">{{ formatCurrency(getTotalBeforeVoucher()) }}</span>
                                </div>
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <span class="text-white">Voucher giảm:</span>
                                    <span class="fw-bold text-warning">-{{ formatCurrency(calculateTotalVoucherSavings(selectedHoaDon.chiTietVoucherList)) }}</span>
                                </div>
                                <div class="border-top border-white border-opacity-50 pt-2">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <span class="fw-bold text-white">Sau khi áp voucher:</span>
                                        <span class="fw-bold h5 mb-0 text-white">{{ formatCurrency(getTotalAfterVoucher()) }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </Dialog>

        <!-- Modal cập nhật trạng thái -->
        <Dialog v-model:visible="showStatusUpdateDialog" modal :style="{ width: '450px' }" header="Cập nhật trạng thái">
            <div class="status-update-content">
                <div class="mb-3" v-if="selectedInvoiceForUpdate">
                    <label class="form-label fw-semibold">Hóa đơn:</label>
                    <div>
                        <strong>{{ selectedInvoiceForUpdate.maHoaDon }}</strong> -
                        {{ getCustomerName(selectedInvoiceForUpdate) }}
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label fw-semibold">Trạng thái hiện tại:</label>
                    <div>
                        <Tag :value="getStatusLabel(selectedInvoiceForUpdate?.trangThaiHoaDon)" :severity="getStatusSeverity(selectedInvoiceForUpdate?.trangThaiHoaDon)" />
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label fw-semibold">Trạng thái mới:</label>
                    <select v-model="newStatus" class="form-select">
                        <option value="">Chọn trạng thái mới</option>
                        <option v-for="status in getAvailableStatuses(selectedInvoiceForUpdate)" :key="status.value" :value="status.value">
                            {{ status.label }}
                        </option>
                    </select>
                </div>

                <div v-if="needsNote(newStatus)" class="mb-3">
                    <label class="form-label fw-semibold">Ghi chú:</label>
                    <textarea v-model="statusNote" class="form-control" rows="3" placeholder="Nhập ghi chú..."></textarea>
                </div>
            </div>

            <template #footer>
                <div class="d-flex gap-2">
                    <Button @click="confirmStatusUpdate" :disabled="!newStatus">
                        <i class="pi pi-check me-1"></i>
                        Cập nhật
                    </Button>
                    <Button @click="closeStatusUpdateDialog" outlined>
                        <i class="pi pi-times me-1"></i>
                        Hủy
                    </Button>
                </div>
            </template>
        </Dialog>

        <Dialog v-model:visible="showImageViewModal" modal :style="{ width: '600px' }" header="Ảnh minh chứng">
            <div v-if="selectedImageItem" class="image-view-content">
                <div class="mb-3">
                    <h6 class="mb-2">{{ selectedImageItem.tenSanPham }}</h6>
                    <div class="d-flex justify-content-between text-muted">
                        <span>Mã: {{ selectedImageItem.maChiTietTraHang }}</span>
                        <span>{{ formatDate(selectedImageItem.ngayTaoTraHang) }}</span>
                    </div>
                </div>

                <div class="mb-3 text-center">
                    <img :src="createImageUrl(selectedImageItem.anhMinhChung)" :alt="'Ảnh minh chứng cho ' + selectedImageItem.tenSanPham" class="full-evidence-image rounded" @error="handleImageError" />
                </div>

                <div v-if="selectedImageItem.lyDo" class="reason-detail mb-3">
                    <strong>Lý do trả hàng:</strong>
                    <div class="bg-light mt-1 rounded p-2">
                        {{ selectedImageItem.lyDo }}
                    </div>
                </div>
            </div>

            <template #footer>
                <Button @click="closeImageModal" outlined>
                    <i class="pi pi-times me-1"></i>
                    Đóng
                </Button>
            </template>
        </Dialog>

        <!-- Modal xem chi tiết lý do -->
        <Dialog v-model:visible="showReasonModal" modal :style="{ width: '500px' }" header="Chi tiết lý do trả hàng">
            <div v-if="selectedReasonItem" class="reason-detail-content">
                <div class="mb-3">
                    <h6 class="mb-2">{{ selectedReasonItem.tenSanPham }}</h6>
                    <div class="d-flex justify-content-between text-muted">
                        <span>Mã: {{ selectedReasonItem.maChiTietTraHang }}</span>
                        <span>{{ formatDate(selectedReasonItem.ngayTaoTraHang) }}</span>
                    </div>
                </div>

                <div class="reason-full-text bg-light rounded p-3">
                    {{ selectedReasonItem.lyDo }}
                </div>
            </div>

            <template #footer>
                <Button @click="closeReasonModal" outlined>
                    <i class="pi pi-times me-1"></i>
                    Đóng
                </Button>
            </template>
        </Dialog>
        <Dialog v-model:visible="showVoucherDetailDialog" modal :style="{ width: '80vw' }" :maximizable="true" header="Chi tiết voucher">
            <template #header>
                <div class="d-flex align-items-center">
                    <i class="pi pi-ticket text-success me-2"></i>
                    <span>Chi tiết voucher - {{ selectedVoucherDetail?.maHoaDon }}</span>
                </div>
            </template>

            <div v-if="selectedVoucherDetail" class="voucher-detail-content">
                <!-- Thông tin hóa đơn -->
                <div class="card mb-4 border-0 shadow-sm">
                    <div class="card-header bg-primary text-white">
                        <h6 class="mb-0 text-white">
                            <i class="pi pi-file me-2"></i>
                            Thông tin hóa đơn
                        </h6>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-3"><strong>Mã hóa đơn:</strong> {{ selectedVoucherDetail.maHoaDon }}</div>
                            <div class="col-md-3"><strong>Khách hàng:</strong> {{ getCustomerName(selectedVoucherDetail) }}</div>
                            <div class="col-md-3"><strong>Tổng tiền:</strong> {{ formatCurrency(selectedVoucherDetail.tongTien) }}</div>
                            <div class="col-md-3"><strong>Ngày tạo:</strong> {{ formatDate(selectedVoucherDetail.ngayTao) }}</div>
                        </div>
                    </div>
                </div>

                <!-- Loading voucher details -->
                <div v-if="isLoadingVoucherDetails" class="py-4 text-center">
                    <ProgressSpinner style="width: 40px; height: 40px" strokeWidth="6" />
                    <p class="text-muted mt-2">Đang tải chi tiết voucher...</p>
                </div>

                <!-- Danh sách voucher -->
                <div v-else class="card border-0 shadow-sm">
                    <div class="card-header bg-success text-white">
                        <h6 class="mb-0 text-white">
                            <i class="pi pi-list me-2"></i>
                            Danh sách voucher đã áp dụng
                        </h6>
                    </div>
                    <div class="card-body">
                        <DataTable :value="voucherDetailsForInvoice" class="p-datatable-sm" responsiveLayout="scroll">
                            <Column field="tenVoucher" header="Tên voucher" style="min-width: 200px">
                                <template #body="slotProps">
                                    <div>
                                        <div class="fw-medium">{{ slotProps.data.tenVoucher }}</div>
                                        <small class="text-muted">{{ slotProps.data.maVoucher }}</small>
                                    </div>
                                </template>
                            </Column>

                            <Column field="loaiGiamGia" header="Loại" style="min-width: 100px">
                                <template #body="slotProps">
                                    <Tag :value="getVoucherTypeLabel(slotProps.data.loaiGiamGia)" :severity="getVoucherTypeSeverity(slotProps.data.loaiGiamGia)" />
                                </template>
                            </Column>

                            <Column field="giaTriGiam" header="Giá trị" style="min-width: 120px">
                                <template #body="slotProps">
                                    <div v-if="slotProps.data.loaiGiamGia === 'PHAN_TRAM'" class="fw-bold text-info">{{ slotProps.data.giaTriGiam }}%</div>
                                    <div v-else class="fw-bold text-success">
                                        {{ formatCurrency(slotProps.data.giaTriGiam) }}
                                    </div>
                                </template>
                            </Column>

                            <Column field="soTienGiam" header="Tiền giảm" style="min-width: 120px">
                                <template #body="slotProps">
                                    <span class="fw-bold text-success">{{ formatCurrency(slotProps.data.soTienGiam) }}</span>
                                </template>
                            </Column>

                            <Column field="giaTriDonHang" header="Giá trị ĐH" style="min-width: 120px">
                                <template #body="slotProps">
                                    <span>{{ formatCurrency(slotProps.data.giaTriDonHang) }}</span>
                                </template>
                            </Column>

                            <Column field="ngayApDung" header="Ngày áp dụng" style="min-width: 150px">
                                <template #body="slotProps">
                                    <small>{{ formatDate(slotProps.data.ngayApDung) }}</small>
                                </template>
                            </Column>

                            <template #empty>
                                <div class="py-4 text-center">
                                    <i class="pi pi-ticket text-muted display-4 mb-3"></i>
                                    <p class="text-muted">Không có voucher nào được áp dụng</p>
                                </div>
                            </template>
                        </DataTable>

                        <!-- Tổng kết -->
                        <div v-if="voucherDetailsForInvoice.length > 0" class="voucher-summary mt-4 rounded p-3">
                            <div class="row text-center text-white">
                                <div class="col-4">
                                    <div class="h4 mb-1">{{ voucherDetailsForInvoice.length }}</div>
                                    <div class="text-white-50">Voucher đã dùng</div>
                                </div>
                                <div class="col-4">
                                    <div class="h4 mb-1">{{ formatCurrency(calculateTotalVoucherSavings(voucherDetailsForInvoice)) }}</div>
                                    <div class="text-white-50">Tổng tiết kiệm</div>
                                </div>
                                <div class="col-4">
                                    <div class="h4 mb-1">{{ selectedVoucherDetail.tongTien ? ((calculateTotalVoucherSavings(voucherDetailsForInvoice) / selectedVoucherDetail.tongTien) * 100).toFixed(1) : '0.0' }}%</div>
                                    <div class="text-white-50">Phần trăm giảm</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <template #footer>
                <Button @click="closeVoucherDetailDialog" outlined>
                    <i class="pi pi-times me-1"></i>
                    Đóng
                </Button>
            </template>
        </Dialog>
    </div>
</template>

<style scoped>
/* Base styles */
.invoice-management {
    padding: 1rem;
    min-height: 100vh;
    background-color: #f8f9fa;
}

.gradient-header {
    background: linear-gradient(135deg, #007bff, #6610f2);
    color: white;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 123, 255, 0.3);
}

.stat-card {
    border: none;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
    overflow: hidden;
}

.stat-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

/* Workflow styles */
.workflow-step {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    font-weight: 600;
    transition: all 0.3s ease;
    position: relative;
}

.workflow-connector {
    flex: 1;
    height: 3px;
    margin: 0 15px;
    border-radius: 2px;
    transition: all 0.3s ease;
}

.workflow-connector.completed {
    background: linear-gradient(90deg, #28a745, #20c997);
}

.workflow-connector.pending {
    background: #e9ecef;
}

/* Return styles */
.return-section {
    background: linear-gradient(135deg, #fff3cd, #ffeaa7);
    border-left: 4px solid #ffc107;
}

.return-item {
    background: white;
    border-radius: 8px;
    border: 1px solid #e9ecef;
    transition: all 0.3s ease;
}

.return-item:hover {
    border-color: #007bff;
    box-shadow: 0 2px 8px rgba(0, 123, 255, 0.1);
}

.return-statistics {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-radius: 12px;
}

.return-details {
    font-size: 0.9rem;
}

/* Table styles */
.table-container {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    background: white;
}

/* Info row styles */
.invoice-info .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.5rem 0;
    border-bottom: 1px solid #f1f3f5;
}

.invoice-info .info-row:last-child {
    border-bottom: none;
}

/* Product image */
.product-image img {
    border: 2px solid #e9ecef;
}

/* Statistic items */
.statistic-item {
    padding: 1rem;
    border-radius: 8px;
    background: rgba(0, 123, 255, 0.05);
    border: 1px solid rgba(0, 123, 255, 0.1);
}

/* Loading overlay */
.loading-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.95);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
    backdrop-filter: blur(2px);
}

/* Dialog styles */
.invoice-detail-content .card {
    margin-bottom: 1.5rem;
}

.status-update-content .form-label {
    margin-bottom: 0.5rem;
    font-weight: 600;
}

.return-management-content .card {
    margin-bottom: 1.5rem;
}
/* Create return dialog */
.create-return-content .product-info {
    font-size: 0.9rem;
}

.create-return-content .product-info .d-flex {
    align-items: center;
    padding: 0.3rem 0;
    border-bottom: 1px solid #f1f3f5;
}

.create-return-content .product-info .d-flex:last-child {
    border-bottom: none;
}

/* Return product table styles */
.returnable-products-table .p-datatable-tbody > tr {
    transition: all 0.3s ease;
}

.returnable-products-table .p-datatable-tbody > tr:hover {
    background-color: rgba(0, 123, 255, 0.05);
}

/* Return statistics enhanced */
.return-statistics-content {
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-radius: 8px;
    padding: 1rem;
}

.return-statistics-content .info-row {
    padding: 0.4rem 0;
    border-bottom: 1px dotted #dee2e6;
}

.return-statistics-content .info-row:last-child {
    border-bottom: none;
}

/* Enhanced return item cards */
.return-item {
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
    border: 1px solid #e9ecef;
    border-radius: 12px;
    padding: 1rem;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
}

.return-item::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: linear-gradient(180deg, #ffc107, #fd7e14);
}

.return-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    border-color: #007bff;
}

/* Return status indicators */
.return-status-pending {
    background: linear-gradient(135deg, #fff3cd, #ffeaa7);
    border-left: 4px solid #ffc107;
}

.return-status-approved {
    background: linear-gradient(135deg, #d4edda, #c3e6cb);
    border-left: 4px solid #28a745;
}

.return-status-rejected {
    background: linear-gradient(135deg, #f8d7da, #f5c6cb);
    border-left: 4px solid #dc3545;
}

/* Enhanced statistics cards */
.return-statistics {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
    overflow: hidden;
    position: relative;
}

.return-statistics::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.5), transparent);
}

/* Product info card in create return dialog */
.product-info {
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    border-radius: 8px;
    padding: 1rem;
    border: 1px solid #e9ecef;
}

/* Enhanced buttons */
.btn-create-return {
    background: linear-gradient(135deg, #ffc107, #fd7e14);
    border: none;
    color: white;
    font-weight: 600;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(255, 193, 7, 0.3);
}

.btn-create-return:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(255, 193, 7, 0.4);
}

.btn-approve-return {
    background: linear-gradient(135deg, #28a745, #20c997);
    border: none;
    color: white;
    font-weight: 600;
}

.btn-reject-return {
    background: linear-gradient(135deg, #dc3545, #e83e8c);
    border: none;
    color: white;
    font-weight: 600;
}

/* Loading states */
.loading-return-data {
    text-align: center;
    padding: 2rem;
    color: #6c757d;
}

.loading-return-data .pi-spin {
    font-size: 2rem;
    margin-bottom: 1rem;
}

/* Responsive adjustments */
@media (max-width: 768px) {
    .return-item {
        margin-bottom: 1rem;
    }

    .return-statistics .col-3 {
        text-align: center;
        margin-bottom: 1rem;
    }

    .product-info .d-flex {
        flex-direction: column;
        align-items: flex-start !important;
        gap: 0.25rem;
    }
}

/* Animation for success states */
@keyframes successPulse {
    0% {
        box-shadow: 0 0 0 0 rgba(40, 167, 69, 0.4);
    }
    70% {
        box-shadow: 0 0 0 10px rgba(40, 167, 69, 0);
    }
    100% {
        box-shadow: 0 0 0 0 rgba(40, 167, 69, 0);
    }
}

.return-success {
    animation: successPulse 2s infinite;
}

/* Enhanced form inputs */
.create-return-content .form-control:focus,
.create-return-content .p-inputnumber-input:focus {
    border-color: #ffc107;
    box-shadow: 0 0 0 0.2rem rgba(255, 193, 7, 0.25);
}

/* Alert enhancements */
.alert-info {
    background: linear-gradient(135deg, #d1ecf1 0%, #bee5eb 100%);
    border: 1px solid #b6d4da;
    border-radius: 8px;
}

/* Empty state enhancements */
.empty-return-state {
    text-align: center;
    padding: 3rem 1rem;
    color: #6c757d;
}

.empty-return-state .pi {
    font-size: 4rem;
    margin-bottom: 1rem;
    opacity: 0.5;
}
.reason-box {
    background: #f8f9fa;
    border: 1px solid #e9ecef;
    border-radius: 6px;
    padding: 0.75rem;
    margin-top: 0.5rem;
    font-size: 0.9rem;
    line-height: 1.4;
    max-height: 80px;
    overflow-y: auto;
}

.reason-section {
    background: #fff8e6;
    border-radius: 6px;
    padding: 0.75rem;
    margin: 0.5rem 0;
    border-left: 4px solid #ffc107;
}

.reason-section-header {
    display: flex;
    align-items: center;
    margin-bottom: 0.5rem;
    font-weight: 600;
    color: #856404;
}

.reason-section-header i {
    margin-right: 0.5rem;
    color: #ffc107;
}

/* Styles cho ảnh minh chứng */
.evidence-section {
    background: #f8f9fa;
    border-radius: 6px;
    padding: 0.75rem;
    margin: 0.5rem 0;
}

.evidence-section-header {
    display: flex;
    align-items: center;
    margin-bottom: 0.5rem;
    font-weight: 600;
    color: #495057;
}

.evidence-section-header i {
    margin-right: 0.5rem;
    color: #007bff;
}

.evidence-image-container {
    position: relative;
    display: inline-block;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
}

.evidence-image-container:hover {
    transform: scale(1.02);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.evidence-image {
    width: 120px;
    height: 90px;
    object-fit: cover;
    cursor: pointer;
    transition: all 0.3s ease;
    border: 2px solid #e9ecef;
}

.evidence-image:hover {
    border-color: #007bff;
}

.image-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
    cursor: pointer;
}

.evidence-image-container:hover .image-overlay {
    opacity: 1;
}

.image-overlay i {
    font-size: 1.5rem;
    color: white;
}

/* Modal xem ảnh */
.image-view-content {
    text-align: center;
}

.full-evidence-image {
    max-width: 100%;
    max-height: 400px;
    object-fit: contain;
    border: 2px solid #e9ecef;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.evidence-thumbnail-small {
    width: 40px;
    height: 30px;
    object-fit: cover;
    cursor: pointer;
    border: 1px solid #dee2e6;
    margin-left: 0.5rem;
}

.evidence-thumbnail-small:hover {
    border-color: #007bff;
    transform: scale(1.1);
}
/* ===== VOUCHER SPECIFIC STYLES ===== */

/* Voucher Item Cards */
.voucher-item {
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
    border: 1px solid #e9ecef;
    border-radius: 12px;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.voucher-item::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: linear-gradient(180deg, #28a745, #20c997);
}

.voucher-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    border-color: #28a745;
}

/* Voucher Details Content */
.voucher-details {
    font-size: 0.9rem;
    line-height: 1.4;
}

.voucher-details .d-flex {
    margin-bottom: 0.5rem;
}

.voucher-details .d-flex:last-child {
    margin-bottom: 0;
}

/* Voucher Summary Section */
.voucher-summary {
    background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(40, 167, 69, 0.3);
    position: relative;
    overflow: hidden;
}

.voucher-summary::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.5), transparent);
}

.voucher-summary h6 {
    color: white;
    font-weight: 600;
    margin-bottom: 1rem;
}

.voucher-summary .h4,
.voucher-summary .h5 {
    color: white;
    font-weight: 700;
    margin-bottom: 0.25rem;
}

.voucher-summary small {
    color: rgba(255, 255, 255, 0.8);
    font-size: 0.75rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

/* Voucher Detail Modal Content */
.voucher-detail-content .card {
    margin-bottom: 1.5rem;
    border: none;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.voucher-detail-content .card-header {
    border-bottom: none;
    font-weight: 600;
}

.voucher-detail-content .card-body {
    padding: 1.5rem;
}

/* Voucher Statistics in DataTable */
.voucher-stats {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
}

.voucher-count {
    display: inline-flex;
    align-items: center;
    background: linear-gradient(135deg, #28a745, #20c997);
    color: white;
    padding: 3px 8px;
    border-radius: 12px;
    font-size: 0.75rem;
    font-weight: 600;
    margin-bottom: 2px;
}

.voucher-count .pi {
    margin-right: 4px;
    font-size: 0.7rem;
}

.voucher-savings {
    color: #28a745;
    font-weight: 600;
    font-size: 0.8rem;
    line-height: 1.2;
}

/* Voucher Type Badges */
.voucher-type-percent {
    background: linear-gradient(135deg, #17a2b8, #138496);
    color: white;
}

.voucher-type-fixed {
    background: linear-gradient(135deg, #28a745, #1e7e34);
    color: white;
}

.voucher-type-amount {
    background: linear-gradient(135deg, #ffc107, #e0a800);
    color: #212529;
}

/* Enhanced Voucher Cards */
.voucher-card {
    border-left: 4px solid #28a745;
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    transition: all 0.3s ease;
    border-radius: 8px;
    overflow: hidden;
}

.voucher-card:hover {
    border-left-color: #20c997;
    box-shadow: 0 4px 15px rgba(40, 167, 69, 0.15);
    transform: translateY(-1px);
}

/* Voucher Badge Animations */
.voucher-badge {
    position: relative;
    overflow: hidden;
}

.voucher-badge::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transition: left 0.6s ease;
}

.voucher-badge:hover::after {
    left: 100%;
}

/* Voucher Info Rows */
.voucher-info .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.75rem 0;
    border-bottom: 1px solid #f1f3f5;
    transition: background-color 0.2s ease;
}

.voucher-info .info-row:last-child {
    border-bottom: none;
}

.voucher-info .info-row:hover {
    background-color: rgba(40, 167, 69, 0.05);
    margin: 0 -1rem;
    padding-left: 1rem;
    padding-right: 1rem;
    border-radius: 6px;
}

/* Voucher Progress Indicators */
.voucher-progress {
    height: 4px;
    background-color: #e9ecef;
    border-radius: 2px;
    overflow: hidden;
    margin-top: 0.5rem;
}

.voucher-progress-bar {
    height: 100%;
    background: linear-gradient(90deg, #28a745, #20c997);
    border-radius: 2px;
    transition: width 0.3s ease;
}

/* Voucher Loading States */
.voucher-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 2rem;
    color: #6c757d;
}

.voucher-loading .pi-spin {
    font-size: 2rem;
    margin-bottom: 1rem;
    color: #28a745;
}

/* Voucher Empty States */
.voucher-empty {
    text-align: center;
    padding: 3rem 1rem;
    color: #6c757d;
}

.voucher-empty .pi {
    font-size: 4rem;
    margin-bottom: 1rem;
    opacity: 0.5;
    color: #28a745;
}

.voucher-empty h6 {
    margin-bottom: 0.5rem;
    color: #495057;
}

.voucher-empty p {
    margin-bottom: 0;
    font-size: 0.9rem;
}

/* Voucher Table Enhancements */
.voucher-table .p-datatable-tbody > tr {
    transition: all 0.3s ease;
}

.voucher-table .p-datatable-tbody > tr:hover {
    background-color: rgba(40, 167, 69, 0.05);
    transform: scale(1.01);
}

.voucher-table .p-datatable-thead > tr > th {
    background: linear-gradient(135deg, #28a745, #20c997);
    color: white;
    border: none;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    font-size: 0.85rem;
}

/* Voucher Action Buttons */
.voucher-btn {
    background: linear-gradient(135deg, #28a745, #20c997);
    border: none;
    color: white;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(40, 167, 69, 0.3);
}

.voucher-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(40, 167, 69, 0.4);
    background: linear-gradient(135deg, #20c997, #28a745);
}

.voucher-btn:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(40, 167, 69, 0.3);
}

/* Voucher Success Animations */
@keyframes voucher-success {
    0% {
        transform: scale(1);
        box-shadow: 0 0 0 0 rgba(40, 167, 69, 0.4);
    }
    70% {
        transform: scale(1.05);
        box-shadow: 0 0 0 10px rgba(40, 167, 69, 0);
    }
    100% {
        transform: scale(1);
        box-shadow: 0 0 0 0 rgba(40, 167, 69, 0);
    }
}

.voucher-success-animation {
    animation: voucher-success 0.6s ease-out;
}

/* Voucher Fade In Animation */
@keyframes voucher-fade-in {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.voucher-fade-in {
    animation: voucher-fade-in 0.5s ease-out;
}

/* Voucher Responsive Design */
@media (max-width: 768px) {
    .voucher-item {
        margin-bottom: 1rem;
    }

    .voucher-summary .col-3,
    .voucher-summary .col-4 {
        margin-bottom: 1rem;
        text-align: center;
    }

    .voucher-stats {
        align-items: center;
        text-align: center;
    }

    .voucher-detail-content .row {
        flex-direction: column;
    }

    .voucher-detail-content .col-md-3 {
        margin-bottom: 0.5rem;
    }

    .voucher-count {
        font-size: 0.7rem;
        padding: 2px 6px;
    }

    .voucher-savings {
        font-size: 0.75rem;
    }
}

@media (max-width: 576px) {
    .voucher-item {
        padding: 1rem;
    }

    .voucher-details {
        font-size: 0.8rem;
    }

    .voucher-summary {
        padding: 1rem;
    }

    .voucher-summary .h4,
    .voucher-summary .h5 {
        font-size: 1.2rem;
    }

    .voucher-detail-content .card-body {
        padding: 1rem;
    }
}

/* Voucher Print Styles */
@media print {
    .voucher-item {
        break-inside: avoid;
        box-shadow: none;
        border: 1px solid #dee2e6;
    }

    .voucher-summary {
        background: #f8f9fa !important;
        color: #212529 !important;
        box-shadow: none;
    }

    .voucher-btn {
        display: none;
    }
}

/* Voucher Accessibility Enhancements */
.voucher-item:focus-within {
    outline: 2px solid #28a745;
    outline-offset: 2px;
}

.voucher-btn:focus {
    outline: 2px solid #ffffff;
    outline-offset: 2px;
    box-shadow: 0 0 0 4px rgba(40, 167, 69, 0.5);
}

/* High Contrast Mode Support */
@media (prefers-contrast: high) {
    .voucher-item {
        border: 2px solid #000000;
        background: #ffffff;
    }

    .voucher-summary {
        background: #000000;
        color: #ffffff;
    }

    .voucher-btn {
        background: #000000;
        color: #ffffff;
        border: 2px solid #ffffff;
    }
}

/* Reduced Motion Support */
@media (prefers-reduced-motion: reduce) {
    .voucher-item,
    .voucher-btn,
    .voucher-stats {
        transition: none;
    }

    .voucher-success-animation,
    .voucher-fade-in {
        animation: none;
    }

    .voucher-item:hover {
        transform: none;
    }
}
</style>
