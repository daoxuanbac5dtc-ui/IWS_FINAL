<script setup>
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';

// ===== AUTHENTICATION & API SETUP =====
const API_BASE_URL = 'http://localhost:8080';
const API_ENDPOINTS = {
    chiTietTraHang: `${API_BASE_URL}/api/chi-tiet-tra-hang`,
    hoaDon: `${API_BASE_URL}/hoa-don`
};

function getAuthHeaders() {
    const token = localStorage.getItem('auth_token') || localStorage.getItem('token');
    if (token) {
        return {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
            Accept: 'application/json'
        };
    }
    return {
        'Content-Type': 'application/json',
        Accept: 'application/json'
    };
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
                throw new Error('Authentication failed');
            }
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `HTTP ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error(`API Error for ${url}:`, error);
        throw error;
    }
}

// ===== REACTIVE DATA =====
const toast = useToast();

// Main data
const invoicesWithReturns = ref([]);
const returnedProducts = ref([]);
const isLoading = ref(false);
const loadingMessage = ref('');

// Tab management
const activeTabIndex = ref(0);
const itemsPerPage = ref(10);
const currentPage = ref(0);

// Filters - Tab 1
const searchKeyword = ref('');
const returnStatusFilter = ref('');
const dateFilter = ref(null);
const showAdvancedFilter = ref(false);
const minRefund = ref(null);
const maxRefund = ref(null);
const invoiceTypeFilter = ref('');
const hasImageFilter = ref('');

// Filters - Tab 2
const productSearchKeyword = ref('');
const productStatusFilter = ref('');
const productImageFilter = ref('');

// Modals
const showReturnDetailModal = ref(false);
const showEvidenceModal = ref(false);
const showReasonModal = ref(false);
const showProcessAllModal = ref(false);
const showApprovalModal = ref(false);

// Selected items
const selectedInvoiceForReturn = ref(null);
const selectedInvoiceForProcessAll = ref(null);
const selectedEvidenceItem = ref(null);
const selectedReasonItem = ref(null);
const currentReturnItems = ref([]);
const selectedReturnForApproval = ref(null);
const approvalNote = ref('');
const restoreInventory = ref(true);

// ===== API FUNCTIONS =====

// Hàm refresh nhanh - chỉ reset loading state
function forceStopLoading() {
    console.log('🛑 Force stop loading...');
    isLoading.value = false;
    loadingMessage.value = '';
}


async function fetchAllReturnRequests() {
    try {
        console.log('🔄 Bắt đầu tải dữ liệu trả hàng...');
        
        // Fetch tất cả yêu cầu trả hàng từ tất cả hóa đơn
        const allInvoices = await fetchWithErrorHandling(API_ENDPOINTS.hoaDon);
        console.log('📋 Đã tải danh sách hóa đơn:', allInvoices?.length || 0);
        
        const allReturns = [];
        const invoiceMap = new Map();

        if (allInvoices && Array.isArray(allInvoices)) {
            let processedCount = 0;
            
            for (const invoice of allInvoices) {
                try {
                    // Lấy chi tiết trả hàng cho từng hóa đơn
                    const returnResponse = await fetchWithErrorHandling(`${API_ENDPOINTS.chiTietTraHang}/by-hoa-don/${invoice.id}`);

                    if (returnResponse && returnResponse.success && returnResponse.data && returnResponse.data.length > 0) {
                        // Thêm vào danh sách tổng
                        allReturns.push(...returnResponse.data);

                        // Tạo invoice có trả hàng
                        if (!invoiceMap.has(invoice.id)) {
                            invoiceMap.set(invoice.id, {
                                ...invoice,
                                returnItems: []
                            });
                        }
                        invoiceMap.get(invoice.id).returnItems.push(...returnResponse.data);
                    }
                    
                    processedCount++;
                    if (processedCount % 10 === 0) {
                        console.log(`📊 Đã xử lý ${processedCount}/${allInvoices.length} hóa đơn`);
                    }
                } catch (error) {
                    console.warn(`⚠️ Không thể lấy trả hàng cho hóa đơn ${invoice.id}:`, error.message);
                }
            }
        } else {
            console.warn('⚠️ Không có dữ liệu hóa đơn hoặc format không đúng:', allInvoices);
        }

        returnedProducts.value = allReturns;
        invoicesWithReturns.value = Array.from(invoiceMap.values());

        console.log('✅ Hoàn thành tải dữ liệu:', {
            totalReturns: allReturns.length,
            invoicesWithReturns: invoiceMap.size
        });

        return { returns: allReturns, invoices: Array.from(invoiceMap.values()) };
    } catch (error) {
        console.error('❌ Lỗi khi tải dữ liệu trả hàng:', error);
        
        // Reset dữ liệu khi có lỗi
        returnedProducts.value = [];
        invoicesWithReturns.value = [];
        
        throw error;
    }
}

async function fetchAllData() {
    // Đảm bảo reset loading state trước khi bắt đầu
    isLoading.value = true;
    loadingMessage.value = 'Đang tải dữ liệu trả hàng...';

    try {
        console.log('🚀 Bắt đầu fetchAllData...');
        await fetchAllReturnRequests();

        console.log('✅ fetchAllData hoàn thành thành công');
        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: `Đã tải ${returnedProducts.value.length} yêu cầu trả hàng từ ${invoicesWithReturns.value.length} hóa đơn`,
            life: 3000
        });
    } catch (error) {
        console.error('❌ Lỗi trong fetchAllData:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi tải dữ liệu',
            detail: `Không thể kết nối đến server: ${error.message}`,
            life: 5000
        });

        // Sample data for development - chỉ khi cần thiết
        console.log('🔧 Tạo dữ liệu mẫu để test...');
        createSampleData();
    } finally {
        // Đảm bảo luôn tắt loading
        console.log('🔄 Tắt loading state...');
        isLoading.value = false;
        loadingMessage.value = '';
        
        // Double check sau 100ms để đảm bảo
        setTimeout(() => {
            if (isLoading.value) {
                console.warn('⚠️ Loading vẫn còn bật, force tắt...');
                isLoading.value = false;
                loadingMessage.value = '';
            }
        }, 100);
    }
}

async function approveReturn(returnItem) {
    // Mở modal để chọn cách duyệt (hoàn kho hoặc không hoàn kho)
    openApprovalModal(returnItem);
}

async function rejectReturn(returnItem) {
    const reason = prompt('Nhập lý do từ chối:');
    if (!reason || reason.trim() === '') {
        toast.add({
            severity: 'warn',
            summary: 'Cảnh báo',
            detail: 'Vui lòng nhập lý do từ chối',
            life: 3000
        });
        return;
    }

    try {
        const response = await fetchWithErrorHandling(`${API_ENDPOINTS.chiTietTraHang}/${returnItem.id}/reject`, {
            method: 'PUT',
            body: JSON.stringify({ lyDoTuChoi: reason })
        });

        if (response.success) {
            // Update local data
            const productIndex = returnedProducts.value.findIndex((p) => p.id === returnItem.id);
            if (productIndex !== -1) {
                returnedProducts.value[productIndex].trangThaiHoaDon = 'REJECTED';
            }

            // Update in current modal if open
            if (currentReturnItems.value.length > 0) {
                const modalIndex = currentReturnItems.value.findIndex((p) => p.id === returnItem.id);
                if (modalIndex !== -1) {
                    currentReturnItems.value[modalIndex].trangThaiHoaDon = 'REJECTED';
                }
            }

            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: 'Đã từ chối yêu cầu trả hàng',
                life: 3000
            });

            await refreshAllData();
        }
    } catch (error) {
        console.error('Error rejecting return:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể từ chối yêu cầu trả hàng',
            life: 3000
        });
    }
}

// ===== MODAL FUNCTIONS =====
async function viewReturnDetails(invoice) {
    selectedInvoiceForReturn.value = invoice;
    currentReturnItems.value = invoice.returnItems || [];
    showReturnDetailModal.value = true;
}

function closeReturnDetailModal() {
    showReturnDetailModal.value = false;
    selectedInvoiceForReturn.value = null;
    currentReturnItems.value = [];
}

function showEvidenceImage(item) {
    selectedEvidenceItem.value = item;
    showEvidenceModal.value = true;
}

function closeEvidenceModal() {
    showEvidenceModal.value = false;
    selectedEvidenceItem.value = null;
}

function showFullReason(item) {
    selectedReasonItem.value = item;
    showReasonModal.value = true;
}

function closeReasonModal() {
    showReasonModal.value = false;
    selectedReasonItem.value = null;
}

function processAllReturns(invoice) {
    selectedInvoiceForProcessAll.value = invoice;
    showProcessAllModal.value = true;
}

function closeProcessAllModal() {
    showProcessAllModal.value = false;
    selectedInvoiceForProcessAll.value = null;
}

async function approveAllReturns() {
    if (!selectedInvoiceForProcessAll.value) return;

    const pendingItems = selectedInvoiceForProcessAll.value.returnItems.filter((item) => item.trangThaiHoaDon === 'PENDING');

    try {
        for (const item of pendingItems) {
            await approveReturn(item);
        }

        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: `Đã chấp nhận ${pendingItems.length} yêu cầu trả hàng`,
            life: 3000
        });

        closeProcessAllModal();
    } catch (error) {
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Có lỗi khi xử lý hàng loạt',
            life: 3000
        });
    }
}

async function rejectAllReturns() {
    const reason = prompt('Nhập lý do từ chối tất cả:');
    if (!reason || reason.trim() === '') return;

    if (!selectedInvoiceForProcessAll.value) return;

    const pendingItems = selectedInvoiceForProcessAll.value.returnItems.filter((item) => item.trangThaiHoaDon === 'PENDING');

    try {
        for (const item of pendingItems) {
            const response = await fetchWithErrorHandling(`${API_ENDPOINTS.chiTietTraHang}/${item.id}/reject`, {
                method: 'PUT',
                body: JSON.stringify({ lyDoTuChoi: reason })
            });
        }

        toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: `Đã từ chối ${pendingItems.length} yêu cầu trả hàng`,
            life: 3000
        });

        closeProcessAllModal();
        await refreshAllData();
    } catch (error) {
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Có lỗi khi xử lý hàng loạt',
            life: 3000
        });
    }
}

// ===== APPROVAL WITH INVENTORY OPTIONS =====
function openApprovalModal(returnItem) {
    selectedReturnForApproval.value = returnItem;
    approvalNote.value = '';
    restoreInventory.value = true;
    showApprovalModal.value = true;
}

function closeApprovalModal() {
    showApprovalModal.value = false;
    selectedReturnForApproval.value = null;
    approvalNote.value = '';
    restoreInventory.value = true;
}

async function confirmApproval() {
    if (!selectedReturnForApproval.value) return;

    try {
        let endpoint = restoreInventory.value
            ? `${API_ENDPOINTS.chiTietTraHang}/${selectedReturnForApproval.value.id}/approve-with-inventory`
            : `${API_ENDPOINTS.chiTietTraHang}/${selectedReturnForApproval.value.id}/approve-no-inventory`;

        let response;
        try {
            response = await fetchWithErrorHandling(endpoint, {
                method: 'PUT',
                body: JSON.stringify({
                    ghiChu: approvalNote.value || (restoreInventory.value ? 'Đã chấp nhận trả hàng và hoàn kho' : 'Đã chấp nhận trả hàng không hoàn kho')
                })
            });
        } catch (err) {
            // Fallback to legacy approve endpoint if specialized one not available
            console.warn('Specialized approve endpoint failed, falling back to /approve. Error:', err?.message);
            endpoint = `${API_ENDPOINTS.chiTietTraHang}/${selectedReturnForApproval.value.id}/approve`;
            response = await fetchWithErrorHandling(endpoint, {
                method: 'PUT',
                body: JSON.stringify({
                    ghiChu: approvalNote.value || 'Đã chấp nhận trả hàng'
                })
            });
        }

        if (response.success) {
            // Cập nhật local state
            const id = selectedReturnForApproval.value.id;
            const idx = returnedProducts.value.findIndex((p) => p.id === id);
            if (idx !== -1) returnedProducts.value[idx].trangThaiHoaDon = 'APPROVED';

            if (currentReturnItems.value.length > 0) {
                const midx = currentReturnItems.value.findIndex((p) => p.id === id);
                if (midx !== -1) currentReturnItems.value[midx].trangThaiHoaDon = 'APPROVED';
            }

            const inventoryMsg = restoreInventory.value ? ' và đã hoàn lại kho' : ' nhưng không hoàn kho';
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã chấp nhận yêu cầu trả hàng${inventoryMsg}`,
                life: 3000
            });

            closeApprovalModal();
            await refreshAllData();
        }
    } catch (error) {
        console.error('Error approving return:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: `Không thể chấp nhận yêu cầu trả hàng: ${error?.message || 'Đã xảy ra lỗi'}`,
            life: 3000
        });
    }
}

// ===== UTILITY FUNCTIONS =====
function createImageUrl(imagePath) {
    if (!imagePath) return null;
    if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) return imagePath;
    return API_BASE_URL + '/' + imagePath.replace(/^\//, '');
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

function getCustomerName(invoice) {
    return invoice.tenKhachHang || invoice.tenNguoiDung || 'Khách lẻ';
}

function getReturnStatusLabel(status) {
    const statusMap = {
        PENDING: 'Chờ xử lý',
        APPROVED: 'Đã chấp nhận',
        REJECTED: 'Đã từ chối'
    };
    return statusMap[status] || status;
}

function getReturnStatusSeverity(status) {
    const severityMap = {
        PENDING: 'warning',
        APPROVED: 'success',
        REJECTED: 'danger'
    };
    return severityMap[status] || 'secondary';
}

function formatReasonPreview(reason, maxLength = 50) {
    if (!reason) return 'Không có';
    if (reason.length <= maxLength) return reason;
    return reason.substring(0, maxLength) + '...';
}

function getReturnRequestCount(invoice) {
    return invoice.returnItems ? invoice.returnItems.length : 0;
}

function getTotalRefundAmount(invoice) {
    if (!invoice.returnItems) return 0;
    return invoice.returnItems.reduce((total, item) => total + (item.tienHoan || 0), 0);
}

function getPendingCount(invoice) {
    if (!invoice.returnItems) return 0;
    return invoice.returnItems.filter((item) => item.trangThaiHoaDon === 'PENDING').length;
}

function getProcessedCount(invoice) {
    if (!invoice.returnItems) return 0;
    return invoice.returnItems.filter((item) => ['APPROVED', 'REJECTED'].includes(item.trangThaiHoaDon)).length;
}

function viewProductReturnHistory(product) {
    toast.add({
        severity: 'info',
        summary: 'Thông báo',
        detail: 'Chức năng xem lịch sử sản phẩm đang phát triển',
        life: 3000
    });
}

function getPlaceholderImage() {
    return 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2Y4ZjlmYSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjE0IiBmaWxsPSIjNmM3NTdkIj5Ow6RuZyBjw7MgYW5oPC90ZXh0Pjwvc3ZnPg==';
}

function handleImageError(event) {
    event.target.src = getPlaceholderImage();
}

// ===== FILTER FUNCTIONS =====
function onSearch() {
    // Auto trigger filter
}

function onProductSearch() {
    // Auto trigger filter
}

function applyFilters() {
    // Filters will be applied through computed properties
}

function applyProductFilters() {
    // Filters will be applied through computed properties
}

function clearAllFilters() {
    searchKeyword.value = '';
    returnStatusFilter.value = '';
    dateFilter.value = null;
    minRefund.value = null;
    maxRefund.value = null;
    invoiceTypeFilter.value = '';
    hasImageFilter.value = '';
}

function clearProductFilters() {
    productSearchKeyword.value = '';
    productStatusFilter.value = '';
    productImageFilter.value = '';
}

function onTabChange(event) {
    activeTabIndex.value = event.index;
}

async function refreshAllData() {
    await fetchAllData();
}

function exportData() {
    try {
        const headers = ['Mã HĐ', 'Khách hàng', 'Số yêu cầu', 'Tiền hoàn', 'Trạng thái'];
        const csvData = filteredInvoicesWithReturns.value.map((invoice) => [
            invoice.maHoaDon,
            getCustomerName(invoice),
            getReturnRequestCount(invoice),
            getTotalRefundAmount(invoice),
            `${getPendingCount(invoice)} chờ, ${getProcessedCount(invoice)} xong`
        ]);

        const csvContent = [headers.join(','), ...csvData.map((row) => row.map((field) => `"${field}"`).join(','))].join('\n');

        const BOM = '\uFEFF';
        const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);

        const now = new Date();
        const dateStr = now.toISOString().split('T')[0];
        const filename = `DonHang_TraHang_${dateStr}.csv`;

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
            detail: `Đã xuất ${filteredInvoicesWithReturns.value.length} đơn hàng`,
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

function exportProductData() {
    try {
        const headers = ['Sản phẩm', 'Mã HĐ', 'Khách hàng', 'SL trả', 'Tiền hoàn', 'Lý do', 'Trạng thái'];
        const csvData = filteredReturnedProducts.value.map((product) => [product.tenSanPham, product.maHoaDon, product.tenKhachHang, product.soLuong, product.tienHoan, product.lyDo || 'Không có', getReturnStatusLabel(product.trangThaiHoaDon)]);

        const csvContent = [headers.join(','), ...csvData.map((row) => row.map((field) => `"${field}"`).join(','))].join('\n');

        const BOM = '\uFEFF';
        const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);

        const now = new Date();
        const dateStr = now.toISOString().split('T')[0];
        const filename = `SanPham_TraHang_${dateStr}.csv`;

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
            detail: `Đã xuất ${filteredReturnedProducts.value.length} sản phẩm`,
            life: 3000
        });
    } catch (error) {
        console.error('Error exporting product data:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi xuất dữ liệu',
            detail: 'Có lỗi xảy ra khi xuất file',
            life: 3000
        });
    }
}

// ===== COMPUTED PROPERTIES =====
const filteredInvoicesWithReturns = computed(() => {
    let filtered = [...invoicesWithReturns.value];

    // Search filter
    if (searchKeyword.value.trim()) {
        const keyword = searchKeyword.value.toLowerCase();
        filtered = filtered.filter((invoice) => invoice.maHoaDon.toLowerCase().includes(keyword) || getCustomerName(invoice).toLowerCase().includes(keyword));
    }

    // Status filter
    if (returnStatusFilter.value) {
        filtered = filtered.filter((invoice) => invoice.returnItems.some((item) => item.trangThaiHoaDon === returnStatusFilter.value));
    }

    // Date filter
    if (dateFilter.value) {
        const filterDate = new Date(dateFilter.value).toDateString();
        filtered = filtered.filter((invoice) => {
            const invoiceDate = new Date(invoice.ngayTao).toDateString();
            return invoiceDate === filterDate;
        });
    }

    // Advanced filters
    if (minRefund.value || maxRefund.value) {
        filtered = filtered.filter((invoice) => {
            const refundAmount = getTotalRefundAmount(invoice);
            const min = minRefund.value || 0;
            const max = maxRefund.value || Infinity;
            return refundAmount >= min && refundAmount <= max;
        });
    }

    if (invoiceTypeFilter.value) {
        filtered = filtered.filter((invoice) => invoice.loaiHoaDon === invoiceTypeFilter.value);
    }

    if (hasImageFilter.value) {
        const hasImage = hasImageFilter.value === 'true';
        filtered = filtered.filter((invoice) => invoice.returnItems.some((item) => (hasImage ? item.duongDanAnh : !item.duongDanAnh)));
    }

    return filtered;
});

const filteredReturnedProducts = computed(() => {
    let filtered = [...returnedProducts.value];

    // Search filter
    if (productSearchKeyword.value.trim()) {
        const keyword = productSearchKeyword.value.toLowerCase();
        filtered = filtered.filter((product) => product.tenSanPham.toLowerCase().includes(keyword) || product.maSanPham.toLowerCase().includes(keyword));
    }

    // Status filter
    if (productStatusFilter.value) {
        filtered = filtered.filter((product) => product.trangThaiHoaDon === productStatusFilter.value);
    }

    // Image filter
    if (productImageFilter.value) {
        const hasImage = productImageFilter.value === 'true';
        filtered = filtered.filter((product) => (hasImage ? product.duongDanAnh : !product.duongDanAnh));
    }

    return filtered;
});

const paginatedReturnedProducts = computed(() => {
    const start = currentPage.value * itemsPerPage.value;
    const end = start + itemsPerPage.value;
    return filteredReturnedProducts.value.slice(start, end);
});

// Statistics
const totalReturnRequests = computed(() => returnedProducts.value.length);

const pendingReturns = computed(() => returnedProducts.value.filter((p) => p.trangThaiHoaDon === 'PENDING').length);

const processedReturns = computed(() => returnedProducts.value.filter((p) => ['APPROVED', 'REJECTED'].includes(p.trangThaiHoaDon)).length);

const totalRefundAmount = computed(() => returnedProducts.value.filter((p) => p.trangThaiHoaDon === 'APPROVED').reduce((total, p) => total + (p.tienHoan || 0), 0));

const totalReturnProducts = computed(() => returnedProducts.value.reduce((total, p) => total + (p.soLuong || 0), 0));

const uniqueProductCount = computed(() => {
    const productIds = new Set(returnedProducts.value.map((p) => p.chiTietSanPhamId));
    return productIds.size;
});

const processingRate = computed(() => {
    if (totalReturnRequests.value === 0) return 0;
    return Math.round((processedReturns.value / totalReturnRequests.value) * 100);
});

// ===== SAMPLE DATA FUNCTION =====
function createSampleData() {
    // Sample data for development/testing
    const sampleReturns = [
        {
            id: 1,
            hoaDonId: 1,
            maHoaDon: 'HD001',
            tenKhachHang: 'Nguyễn Văn A',
            sdtKhachHang: '0912345671',
            tenSanPham: 'Giày Nike Air Max',
            maSanPham: 'SP001',
            soLuong: 1,
            tienHoan: 1200000,
            lyDo: 'Sản phẩm không đúng size',
            trangThaiHoaDon: 'PENDING',
            ngayTaoTraHang: new Date(),
            duongDanAnh: null
        }
    ];

    returnedProducts.value = sampleReturns;

    // Group sample data
    const invoiceMap = new Map();
    sampleReturns.forEach((item) => {
        if (!invoiceMap.has(item.hoaDonId)) {
            invoiceMap.set(item.hoaDonId, {
                id: item.hoaDonId,
                maHoaDon: item.maHoaDon,
                tenKhachHang: item.tenKhachHang,
                sdt: item.sdtKhachHang,
                ngayTao: new Date(),
                tongTien: 2400000,
                returnItems: []
            });
        }
        invoiceMap.get(item.hoaDonId).returnItems.push(item);
    });

    invoicesWithReturns.value = Array.from(invoiceMap.values());
}

// ===== LIFECYCLE HOOKS =====
onMounted(() => {
    fetchAllData();
});
</script>

<template>
    <div class="return-management">
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
                    <h2 class="h3 fw-bold mb-2">Quản Lý Trả Hàng</h2>
                    <p class="text-white-50 mb-0">Hệ thống quản lý yêu cầu trả hàng và sản phẩm</p>
                </div>
                <div class="d-flex gap-2">
                    <Button @click="refreshAllData" :disabled="isLoading" class="btn btn-outline-light" size="small">
                        <i :class="isLoading ? 'pi pi-spinner pi-spin' : 'pi pi-refresh'" class="me-1"></i>
                        {{ isLoading ? 'Đang tải...' : 'Làm mới' }}
                    </Button>
                    <Button v-if="isLoading" @click="forceStopLoading" class="btn btn-warning" size="small">
                        <i class="pi pi-stop me-1"></i>
                        Dừng tải
                    </Button>
                    <Button @click="exportData" class="btn btn-success" size="small">
                        <i class="pi pi-download me-1"></i>
                        Xuất dữ liệu
                    </Button>
                </div>
            </div>

            <!-- Tab Navigation -->
            <TabView v-model:activeIndex="activeTabIndex" @tab-change="onTabChange">
                <TabPanel leftIcon="pi pi-shopping-cart">
                    <template #header>
                        <i class="pi pi-shopping-cart me-2"></i>
                        Đơn hàng có trả
                        <Badge :value="invoicesWithReturns.length.toString()" class="ms-2" />
                    </template>
                </TabPanel>
                <TabPanel leftIcon="pi pi-box">
                    <template #header>
                        <i class="pi pi-box me-2"></i>
                        Sản phẩm trả về
                        <Badge :value="totalReturnProducts.toString()" class="ms-2" />
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
                                <h6 class="text-warning mb-1">Tổng yêu cầu</h6>
                                <h3 class="text-warning mb-2">{{ totalReturnRequests }}</h3>
                                <div class="d-flex gap-2">
                                    <span class="badge bg-danger">Chờ: {{ pendingReturns }}</span>
                                    <span class="badge bg-success">Xong: {{ processedReturns }}</span>
                                </div>
                            </div>
                            <i class="pi pi-refresh text-warning" style="font-size: 2.5rem"></i>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6 col-lg-3">
                <div class="stat-card card border-0">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-danger mb-1">Tiền hoàn</h6>
                                <h3 class="text-danger mb-2">{{ formatCurrency(totalRefundAmount) }}</h3>
                                <span class="badge bg-secondary">{{ processedReturns }} đơn</span>
                            </div>
                            <i class="pi pi-money-bill text-danger" style="font-size: 2.5rem"></i>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6 col-lg-3">
                <div class="stat-card card border-0">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-info mb-1">Sản phẩm trả</h6>
                                <h3 class="text-info mb-2">{{ totalReturnProducts }}</h3>
                                <span class="badge bg-primary">{{ uniqueProductCount }} loại</span>
                            </div>
                            <i class="pi pi-box text-info" style="font-size: 2.5rem"></i>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6 col-lg-3">
                <div class="stat-card card border-0">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-success mb-1">Tỷ lệ xử lý</h6>
                                <h3 class="text-success mb-2">{{ processingRate }}%</h3>
                                <span class="badge bg-info">{{ pendingReturns }} chờ</span>
                            </div>
                            <i class="pi pi-chart-pie text-success" style="font-size: 2.5rem"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- TAB 1: Đơn hàng có trả -->
        <div v-if="activeTabIndex === 0">
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
                                <input v-model="searchKeyword" @input="onSearch" class="form-control" placeholder="Tìm kiếm mã hóa đơn, khách hàng..." />
                            </div>
                        </div>

                        <div class="col-md-2">
                            <label class="form-label fw-semibold">Trạng thái trả</label>
                            <select v-model="returnStatusFilter" @change="applyFilters" class="form-select">
                                <option value="">Tất cả</option>
                                <option value="PENDING">Chờ xử lý</option>
                                <option value="APPROVED">Đã chấp nhận</option>
                                <option value="REJECTED">Đã từ chối</option>
                            </select>
                        </div>

                        <div class="col-md-2">
                            <label class="form-label fw-semibold">Ngày tạo</label>
                            <Calendar v-model="dateFilter" @date-select="applyFilters" placeholder="Chọn ngày" dateFormat="dd/mm/yy" class="w-100" />
                        </div>

                        <div class="col-md-2">
                            <Button @click="clearAllFilters" outlined>
                                <i class="pi pi-filter-slash me-1"></i>
                                Xóa lọc
                            </Button>
                        </div>

                        <div class="col-md-2">
                            <Button @click="showAdvancedFilter = !showAdvancedFilter" outlined>
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
                                <label class="form-label">Khoảng tiền hoàn</label>
                                <div class="row g-2">
                                    <div class="col">
                                        <InputNumber v-model="minRefund" placeholder="Từ" mode="currency" currency="VND" locale="vi-VN" />
                                    </div>
                                    <div class="col">
                                        <InputNumber v-model="maxRefund" placeholder="Đến" mode="currency" currency="VND" locale="vi-VN" />
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Loại hóa đơn</label>
                                <select v-model="invoiceTypeFilter" class="form-select">
                                    <option value="">Tất cả</option>
                                    <option value="ONLINE">Online</option>
                                    <option value="OFFLINE">POS</option>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Có ảnh minh chứng</label>
                                <select v-model="hasImageFilter" class="form-select">
                                    <option value="">Tất cả</option>
                                    <option value="true">Có ảnh</option>
                                    <option value="false">Không có ảnh</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Invoices with Returns Table -->
            <div class="table-container">
                <DataTable
                    :value="filteredInvoicesWithReturns"
                    dataKey="id"
                    :paginator="true"
                    :rows="itemsPerPage"
                    :loading="isLoading"
                    paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                    :rowsPerPageOptions="[10, 25, 50, 100]"
                    currentPageReportTemplate="Hiển thị {first} đến {last} trong tổng số {totalRecords} đơn hàng"
                    class="p-datatable-lg"
                    responsiveLayout="scroll"
                >
                    <Column field="maHoaDon" header="Mã HĐ" sortable style="min-width: 10rem">
                        <template #body="slotProps">
                            <Tag :value="slotProps.data.maHoaDon" severity="secondary" />
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

                    <Column field="ngayTao" header="Ngày đặt" sortable style="min-width: 12rem">
                        <template #body="slotProps">
                            <small>{{ formatDate(slotProps.data.ngayTao) }}</small>
                        </template>
                    </Column>

                    <Column field="tongTien" header="Tổng tiền" sortable style="min-width: 12rem">
                        <template #body="slotProps">
                            <span class="fw-bold text-success">{{ formatCurrency(slotProps.data.tongTien) }}</span>
                        </template>
                    </Column>

                    <Column header="Số yêu cầu trả" style="min-width: 10rem">
                        <template #body="slotProps">
                            <Tag :value="getReturnRequestCount(slotProps.data).toString()" severity="warning" icon="pi pi-refresh" />
                        </template>
                    </Column>

                    <Column header="Tiền hoàn" style="min-width: 12rem">
                        <template #body="slotProps">
                            <span class="fw-bold text-danger">{{ formatCurrency(getTotalRefundAmount(slotProps.data)) }}</span>
                        </template>
                    </Column>

                    <Column header="Trạng thái" style="min-width: 12rem">
                        <template #body="slotProps">
                            <div class="d-flex flex-column gap-1">
                                <Tag v-if="getPendingCount(slotProps.data) > 0" :value="`${getPendingCount(slotProps.data)} chờ`" severity="danger" />
                                <Tag v-if="getProcessedCount(slotProps.data) > 0" :value="`${getProcessedCount(slotProps.data)} xong`" severity="success" />
                            </div>
                        </template>
                    </Column>

                    <Column header="Thao tác" style="min-width: 12rem">
                        <template #body="slotProps">
                            <div class="d-flex gap-1">
                                <Button @click="viewReturnDetails(slotProps.data)" size="small" outlined title="Xem chi tiết">
                                    <i class="pi pi-eye"></i>
                                </Button>
                                <Button v-if="getPendingCount(slotProps.data) > 0" @click="processAllReturns(slotProps.data)" size="small" severity="warning" outlined title="Xử lý tất cả">
                                    <i class="pi pi-cog"></i>
                                </Button>
                            </div>
                        </template>
                    </Column>

                    <template #empty>
                        <div class="py-5 text-center">
                            <i class="pi pi-shopping-cart text-muted display-1 mb-3"></i>
                            <h5 class="text-muted mb-2">Không tìm thấy đơn hàng có trả</h5>
                            <p class="text-muted">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm.</p>
                        </div>
                    </template>
                </DataTable>
            </div>
        </div>

        <!-- TAB 2: Sản phẩm trả về -->
        <div v-if="activeTabIndex === 1">
            <!-- Product Filter -->
            <div class="card mb-4 border-0 shadow-sm">
                <div class="card-body">
                    <div class="row g-3 align-items-end">
                        <div class="col-md-4">
                            <label class="form-label fw-semibold">Tìm kiếm sản phẩm</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <i class="pi pi-search"></i>
                                </span>
                                <input v-model="productSearchKeyword" @input="onProductSearch" class="form-control" placeholder="Tìm kiếm tên sản phẩm, mã..." />
                            </div>
                        </div>

                        <div class="col-md-2">
                            <label class="form-label fw-semibold">Trạng thái</label>
                            <select v-model="productStatusFilter" @change="applyProductFilters" class="form-select">
                                <option value="">Tất cả</option>
                                <option value="PENDING">Chờ xử lý</option>
                                <option value="APPROVED">Đã chấp nhận</option>
                                <option value="REJECTED">Đã từ chối</option>
                            </select>
                        </div>

                        <div class="col-md-2">
                            <label class="form-label fw-semibold">Có ảnh</label>
                            <select v-model="productImageFilter" @change="applyProductFilters" class="form-select">
                                <option value="">Tất cả</option>
                                <option value="true">Có ảnh</option>
                                <option value="false">Không có ảnh</option>
                            </select>
                        </div>

                        <div class="col-md-2">
                            <Button @click="clearProductFilters" outlined>
                                <i class="pi pi-filter-slash me-1"></i>
                                Xóa lọc
                            </Button>
                        </div>

                        <div class="col-md-2">
                            <Button @click="exportProductData" severity="info" outlined>
                                <i class="pi pi-download me-1"></i>
                                Xuất SP
                            </Button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Individual Return Items Display -->
            <div class="return-items-container">
                <div class="section-header mb-4">
                    <h5 class="section-title">
                        <i class="pi pi-list me-2"></i>
                        Chi tiết từng yêu cầu trả hàng ({{ filteredReturnedProducts.length }} yêu cầu)
                    </h5>
                    <div class="section-actions">
                        <Button @click="exportProductData" severity="info" outlined size="small">
                            <i class="pi pi-download me-1"></i>
                            Xuất Excel
                        </Button>
                    </div>
                </div>

                <!-- Return Items Grid -->
                <div class="return-items-grid">
                    <div v-for="(returnItem, index) in paginatedReturnedProducts" :key="returnItem.id" class="return-item-card">
                        <!-- Card Header -->
                        <div class="return-card-header">
                            <div class="return-index">
                                <span class="index-number">{{ (currentPage * itemsPerPage) + index + 1 }}</span>
                            </div>
                            <div class="return-meta">
                                <h6 class="return-code">{{ returnItem.maChiTietTraHang || `TH-${returnItem.id}` }}</h6>
                                <div class="return-date">
                                    <i class="pi pi-calendar me-1"></i>
                                    {{ formatDate(returnItem.ngayTaoTraHang) }}
                                </div>
                            </div>
                            <div class="return-status">
                                <Tag :value="getReturnStatusLabel(returnItem.trangThaiHoaDon)" :severity="getReturnStatusSeverity(returnItem.trangThaiHoaDon)" />
                            </div>
                        </div>

                        <!-- Product Information -->
                        <div class="product-info-section">
                            <div class="product-image-wrapper">
                                <img 
                                    :src="createImageUrl(returnItem.hinhAnhSanPham) || getPlaceholderImage()" 
                                    :alt="returnItem.tenSanPham" 
                                    class="product-image"
                                    @error="handleImageError"
                                />
                                <div v-if="returnItem.duongDanAnh" class="evidence-badge" @click="showEvidenceImage(returnItem)">
                                    <i class="pi pi-camera"></i>
                                    <span>Có ảnh</span>
                                </div>
                            </div>
                            <div class="product-details">
                                <h6 class="product-name">{{ returnItem.tenSanPham }}</h6>
                                <div class="product-code">
                                    <i class="pi pi-tag me-1"></i>
                                    {{ returnItem.maSanPham }}
                                </div>
                                <div class="product-attributes">
                                    <Tag v-if="returnItem.mauSac" :value="returnItem.mauSac" severity="info" size="small" />
                                    <Tag v-if="returnItem.kichThuoc" :value="returnItem.kichThuoc" severity="success" size="small" />
                                </div>
                            </div>
                        </div>

                        <!-- Order & Customer Info -->
                        <div class="order-customer-section">
                            <div class="order-info">
                                <div class="info-item">
                                    <i class="pi pi-file text-primary"></i>
                                    <span class="info-label">Đơn hàng:</span>
                                    <Tag :value="returnItem.maHoaDon" severity="secondary" />
                                </div>
                                <div class="info-item">
                                    <i class="pi pi-user text-success"></i>
                                    <span class="info-label">Khách hàng:</span>
                                    <div class="customer-info">
                                        <span class="customer-name">{{ returnItem.tenKhachHang }}</span>
                                        <small v-if="returnItem.sdtKhachHang" class="customer-phone">{{ returnItem.sdtKhachHang }}</small>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Return Details -->
                        <div class="return-details-section">
                            <div class="detail-grid">
                                <div class="detail-item quantity">
                                    <div class="detail-icon">
                                        <i class="pi pi-shopping-cart"></i>
                                    </div>
                                    <div class="detail-content">
                                        <span class="detail-label">Số lượng trả</span>
                                        <span class="detail-value">{{ returnItem.soLuong }}</span>
                                    </div>
                                </div>
                                <div class="detail-item refund">
                                    <div class="detail-icon">
                                        <i class="pi pi-money-bill"></i>
                                    </div>
                                    <div class="detail-content">
                                        <span class="detail-label">Tiền hoàn</span>
                                        <span class="detail-value price">{{ formatCurrency(returnItem.tienHoan) }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Return Reason -->
                        <div v-if="returnItem.lyDo" class="reason-section">
                            <div class="reason-header">
                                <i class="pi pi-comment text-warning"></i>
                                <span class="reason-title">Lý do trả hàng</span>
                            </div>
                            <div class="reason-content">
                                <p class="reason-text">{{ formatReasonPreview(returnItem.lyDo, 100) }}</p>
                                <Button v-if="returnItem.lyDo.length > 100" @click="showFullReason(returnItem)" link size="small" class="p-0">
                                    <small>Xem đầy đủ</small>
                                </Button>
                            </div>
                        </div>

                        <!-- Actions -->
                        <div class="actions-section">
                            <div class="actions-container">
                                <div class="actions-left">
                                    <Button v-if="returnItem.duongDanAnh" @click="showEvidenceImage(returnItem)" size="small" severity="info" outlined title="Xem ảnh minh chứng">
                                        <i class="pi pi-image me-1"></i>
                                        Xem ảnh
                                    </Button>
                                </div>

                                <div class="actions-right action-block">
                                    <div class="action-header">
                                        <i class="pi pi-shopping-bag text-primary me-2"></i>
                                        <span>Kinh doanh tiếp sản phẩm không ?</span>
                                    </div>
                                    <div class="action-body">
                                        <Button v-if="returnItem.trangThaiHoaDon === 'PENDING'" @click="approveReturn(returnItem)" size="small" severity="success" title="Chấp nhận">
                                            <i class="pi pi-check me-1"></i>
                                            Duyệt
                                        </Button>
                                        <Button v-if="returnItem.trangThaiHoaDon === 'PENDING'" @click="rejectReturn(returnItem)" size="small" severity="danger" outlined title="Từ chối">
                                            <i class="pi pi-times me-1"></i>
                                            Từ chối
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Pagination -->
                <div v-if="filteredReturnedProducts.length > itemsPerPage" class="pagination-section">
                    <Paginator 
                        v-model:first="currentPage" 
                        :rows="itemsPerPage" 
                        :totalRecords="filteredReturnedProducts.length"
                        :rowsPerPageOptions="[10, 25, 50, 100]"
                        template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                        currentPageReportTemplate="Hiển thị {first} đến {last} trong tổng số {totalRecords} yêu cầu"
                    />
                </div>

                <!-- Empty State -->
                <div v-if="filteredReturnedProducts.length === 0" class="empty-state">
                    <div class="empty-content">
                        <i class="pi pi-box text-muted"></i>
                        <h5 class="empty-title">Không tìm thấy yêu cầu trả hàng</h5>
                        <p class="empty-description">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm để xem kết quả khác.</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Modal: Return Details -->
        <Dialog v-model:visible="showReturnDetailModal" modal :style="{ width: '90vw' }" :maximizable="true" header="Chi tiết trả hàng">
            <template #header>
                <div class="d-flex align-items-center">
                    <i class="pi pi-list text-warning me-2"></i>
                    <span>Chi tiết trả hàng - {{ selectedInvoiceForReturn?.maHoaDon }}</span>
                </div>
            </template>

            <div v-if="selectedInvoiceForReturn" class="return-detail-content">
                <!-- Invoice Info -->
                <div class="card mb-4 border-0 shadow-sm">
                    <div class="card-header bg-info text-white">
                        <h6 class="mb-0 text-white">
                            <i class="pi pi-file me-2"></i>
                            Thông tin đơn hàng
                        </h6>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-3"><strong>Mã hóa đơn:</strong> {{ selectedInvoiceForReturn.maHoaDon }}</div>
                            <div class="col-md-3"><strong>Khách hàng:</strong> {{ getCustomerName(selectedInvoiceForReturn) }}</div>
                            <div class="col-md-3"><strong>Tổng tiền:</strong> {{ formatCurrency(selectedInvoiceForReturn.tongTien) }}</div>
                            <div class="col-md-3"><strong>Ngày tạo:</strong> {{ formatDate(selectedInvoiceForReturn.ngayTao) }}</div>
                        </div>
                    </div>
                </div>

                <!-- Return Items -->
                <div class="card border-0 shadow-sm">
                    <div class="card-header bg-warning text-dark">
                        <h6 class="mb-0">
                            <i class="pi pi-list me-2"></i>
                            Danh sách sản phẩm trả ({{ currentReturnItems.length }} sản phẩm)
                        </h6>
                    </div>
                    <div class="card-body">
                        <div class="row g-3">
                            <div v-for="item in currentReturnItems" :key="item.id" class="col-md-6">
                                <div class="return-item-card rounded border p-3">
                                    <div class="d-flex justify-content-between align-items-start mb-2">
                                        <div>
                                            <h6 class="mb-1">{{ item.tenSanPham }}</h6>
                                            <small class="text-muted">{{ item.maSanPham }}</small>
                                        </div>
                                        <Tag :value="getReturnStatusLabel(item.trangThaiHoaDon)" :severity="getReturnStatusSeverity(item.trangThaiHoaDon)" />
                                    </div>

                                    <div class="return-item-details mb-3">
                                        <div class="row">
                                            <div class="col-6">
                                                <small class="text-muted">Số lượng:</small>
                                                <div class="fw-bold">{{ item.soLuong }}</div>
                                            </div>
                                            <div class="col-6">
                                                <small class="text-muted">Tiền hoàn:</small>
                                                <div class="fw-bold text-danger">{{ formatCurrency(item.tienHoan) }}</div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Evidence Image -->
                                    <div v-if="item.duongDanAnh" class="evidence-section mb-3">
                                        <small class="text-muted">Ảnh minh chứng:</small>
                                        <div class="evidence-thumbnail mt-1">
                                            <img :src="createImageUrl(item.duongDanAnh)" alt="Evidence" class="evidence-img cursor-pointer rounded" @click="showEvidenceImage(item)" style="width: 80px; height: 60px; object-fit: cover" />
                                        </div>
                                    </div>

                                    <!-- Reason -->
                                    <div v-if="item.lyDo" class="reason-section mb-3">
                                        <small class="text-muted">Lý do trả:</small>
                                        <div class="reason-text mt-1">
                                            <p class="text-muted small mb-0">{{ formatReasonPreview(item.lyDo, 50) }}</p>
                                            <Button v-if="item.lyDo.length > 50" @click="showFullReason(item)" link size="small" class="p-0">
                                                <small>xem đầy đủ</small>
                                            </Button>
                                        </div>
                                    </div>

                                    <!-- Actions -->
                                    <div v-if="item.trangThaiHoaDon === 'PENDING'" class="d-flex gap-2">
                                        <Button @click="approveReturn(item)" severity="success" size="small" class="flex-1">
                                            <i class="pi pi-check me-1"></i>
                                            Chấp nhận
                                        </Button>
                                        <Button @click="rejectReturn(item)" severity="danger" size="small" outlined class="flex-1">
                                            <i class="pi pi-times me-1"></i>
                                            Từ chối
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <template #footer>
                <Button @click="closeReturnDetailModal" outlined>
                    <i class="pi pi-times me-1"></i>
                    Đóng
                </Button>
            </template>
        </Dialog>
        
        <!-- Modal: Approval with Inventory Options -->
        <Dialog v-model:visible="showApprovalModal" modal :style="{ width: '600px' }" header="Xác nhận chấp nhận trả hàng">
            <div v-if="selectedReturnForApproval" class="approval-content">
                <div class="mb-3">
                    <h6 class="mb-2">{{ selectedReturnForApproval.tenSanPham }}</h6>
                    <div class="d-flex justify-content-between text-muted">
                        <span>Mã: {{ selectedReturnForApproval.maChiTietTraHang || ('TH-' + selectedReturnForApproval.id) }}</span>
                        <span>{{ formatDate(selectedReturnForApproval.ngayTaoTraHang) }}</span>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Ghi chú</label>
                    <InputText v-model="approvalNote" type="text" class="w-100" placeholder="Nhập ghi chú (không bắt buộc)" />
                </div>

                <div class="mb-3">
                    <label class="form-label">Hoàn kho</label>
                    <div class="d-flex align-items-center gap-2">
                        <InputSwitch v-model="restoreInventory" />
                        <small class="text-muted">Bật để hoàn lại kho</small>
                    </div>
                </div>

                <div class="d-flex justify-content-center gap-3">
                    <Button @click="confirmApproval" severity="success" size="large" class="px-4">
                        <i class="pi pi-check me-2"></i>
                        Xác nhận
                    </Button>
                    <Button @click="closeApprovalModal" severity="danger" size="large" outlined class="px-4">
                        <i class="pi pi-times me-2"></i>
                        Hủy
                    </Button>
                </div>
            </div>

            <template #footer>
                <Button @click="closeApprovalModal" outlined>
                    <i class="pi pi-times me-1"></i>
                    Đóng
                </Button>
            </template>
        </Dialog>

        <!-- Modal: Evidence Image -->
        <Dialog v-model:visible="showEvidenceModal" modal :style="{ width: '600px' }" header="Ảnh minh chứng">
            <div v-if="selectedEvidenceItem" class="evidence-view-content">
                <div class="mb-3">
                    <h6 class="mb-2">{{ selectedEvidenceItem.tenSanPham }}</h6>
                    <div class="d-flex justify-content-between text-muted">
                        <span>Mã: {{ selectedEvidenceItem.maChiTietTraHang }}</span>
                        <span>{{ formatDate(selectedEvidenceItem.ngayTaoTraHang) }}</span>
                    </div>
                </div>

                <div class="mb-3 text-center">
                    <img :src="createImageUrl(selectedEvidenceItem.duongDanAnh)" :alt="'Ảnh minh chứng cho ' + selectedEvidenceItem.tenSanPham" class="evidence-full-image rounded" style="max-width: 100%; max-height: 400px; object-fit: contain" />
                </div>

                <div v-if="selectedEvidenceItem.lyDo" class="reason-detail">
                    <strong>Lý do trả hàng:</strong>
                    <div class="bg-light mt-1 rounded p-2">
                        {{ selectedEvidenceItem.lyDo }}
                    </div>
                </div>
            </div>

            <template #footer>
                <Button @click="closeEvidenceModal" outlined>
                    <i class="pi pi-times me-1"></i>
                    Đóng
                </Button>
            </template>
        </Dialog>

        <!-- Modal: Full Reason -->
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

        <!-- Modal: Process All Returns -->
        <Dialog v-model:visible="showProcessAllModal" modal :style="{ width: '700px' }" header="Xử lý tất cả yêu cầu trả hàng">
            <div v-if="selectedInvoiceForProcessAll" class="process-all-content">
                <div class="mb-4">
                    <h6 class="mb-2">Đơn hàng: {{ selectedInvoiceForProcessAll.maHoaDon }}</h6>
                    <p class="text-muted">Bạn có {{ getPendingCount(selectedInvoiceForProcessAll) }} yêu cầu trả hàng chờ xử lý</p>
                </div>

                <div class="d-flex justify-content-center gap-3">
                    <Button @click="approveAllReturns" severity="success" size="large" class="px-4">
                        <i class="pi pi-check me-2"></i>
                        Chấp nhận tất cả
                    </Button>
                    <Button @click="rejectAllReturns" severity="danger" size="large" outlined class="px-4">
                        <i class="pi pi-times me-2"></i>
                        Từ chối tất cả
                    </Button>
                </div>

                <div class="mt-3">
                    <small class="text-muted">
                        <i class="pi pi-exclamation-triangle me-1"></i>
                        Lưu ý: Khi chấp nhận, số lượng sản phẩm sẽ được hoàn lại kho
                    </small>
                </div>
            </div>

            <template #footer>
                <Button @click="closeProcessAllModal" outlined>
                    <i class="pi pi-times me-1"></i>
                    Hủy
                </Button>
            </template>
        </Dialog>
    </div>
</template>

<style scoped>
/* Base styles */
.return-management {
    padding: 1rem;
    min-height: 100vh;
    background-color: #f8f9fa;
}

.gradient-header {
    background: linear-gradient(135deg, #dc3545, #fd7e14);
    color: white;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(220, 53, 69, 0.3);
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

/* Table styles */
.table-container {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    background: white;
}

.return-item-card {
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
    border: 1px solid #e9ecef;
    border-radius: 8px;
    transition: all 0.3s ease;
    position: relative;
}

.return-item-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: linear-gradient(180deg, #dc3545, #fd7e14);
}

.return-item-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    border-color: #dc3545;
}

/* Evidence styles */
.evidence-section {
    background: #f8f9fa;
    border-radius: 6px;
    padding: 0.5rem;
}

.evidence-img {
    border: 2px solid #dee2e6;
    transition: all 0.3s ease;
}

.evidence-img:hover {
    border-color: #dc3545;
    transform: scale(1.05);
}

.evidence-full-image {
    border: 2px solid #e9ecef;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.evidence-indicator {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    background: #e3f2fd;
    border-radius: 50%;
    cursor: pointer;
    transition: all 0.3s ease;
}

.evidence-indicator:hover {
    background: #2196f3;
    color: white;
    transform: scale(1.1);
}

/* Reason styles */
.reason-section {
    background: #fff3cd;
    border-radius: 6px;
    padding: 0.5rem;
    border-left: 4px solid #ffc107;
}

.reason-preview {
    max-width: 200px;
}

.reason-text {
    line-height: 1.4;
}

.reason-detail {
    background: #f8f9fa;
    border-radius: 6px;
    padding: 0.75rem;
    border-left: 4px solid #ffc107;
}

.reason-full-text {
    line-height: 1.5;
    word-wrap: break-word;
}

/* Product thumbnail */
.product-thumbnail {
    border: 2px solid #e9ecef;
    transition: all 0.3s ease;
}

.product-thumbnail:hover {
    border-color: #007bff;
    transform: scale(1.05);
}

/* Modal content */
.return-detail-content .card {
    margin-bottom: 1.5rem;
    border: none;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.evidence-view-content {
    text-align: center;
}

.reason-detail-content .reason-full-text {
    max-height: 300px;
    overflow-y: auto;
}

.process-all-content {
    text-align: center;
    padding: 1rem;
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

/* Return Items Grid Layout */
.return-items-container {
    padding: 1rem;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
}

.section-title {
    font-size: 1.25rem;
    font-weight: 600;
    color: #1e293b;
    display: flex;
    align-items: center;
    margin: 0;
}

.section-actions {
    display: flex;
    gap: 0.75rem;
}

.return-items-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
}

.return-item-card {
    background: white;
    border-radius: 12px;
    padding: 1.5rem;
    border: 1px solid #e2e8f0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.3s ease;
    position: relative;
}

.return-item-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
    border-color: #3b82f6;
}

.return-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #f1f5f9;
}

.return-index {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    background: linear-gradient(135deg, #3b82f6, #1d4ed8);
    color: white;
    border-radius: 50%;
    font-weight: 600;
    font-size: 0.9rem;
}

.return-meta h6 {
    margin: 0 0 0.25rem 0;
    font-size: 1rem;
    font-weight: 600;
    color: #1e293b;
}

.return-date {
    font-size: 0.85rem;
    color: #64748b;
    display: flex;
    align-items: center;
}

.product-info-section {
    display: flex;
    gap: 1rem;
    margin-bottom: 1rem;
    padding: 1rem;
    background: #f8fafc;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
}

.product-image-wrapper {
    position: relative;
    flex-shrink: 0;
}

.product-image {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 8px;
    border: 2px solid #e2e8f0;
}

.evidence-badge {
    position: absolute;
    top: -8px;
    right: -8px;
    background: #fbbf24;
    color: white;
    border-radius: 12px;
    padding: 0.25rem 0.5rem;
    font-size: 0.75rem;
    font-weight: 600;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 0.25rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.evidence-badge:hover {
    background: #f59e0b;
    transform: scale(1.05);
}

.product-details {
    flex: 1;
}

.product-name {
    margin: 0 0 0.5rem 0;
    font-size: 1.1rem;
    font-weight: 600;
    color: #1e293b;
}

.product-code {
    font-size: 0.9rem;
    color: #64748b;
    margin-bottom: 0.5rem;
    display: flex;
    align-items: center;
}

.product-attributes {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.order-customer-section {
    margin-bottom: 1rem;
}

.order-info {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.info-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.info-label {
    font-size: 0.9rem;
    color: #64748b;
    min-width: 80px;
}

.customer-info {
    display: flex;
    flex-direction: column;
}

.customer-name {
    font-weight: 600;
    color: #1e293b;
}

.customer-phone {
    color: #64748b;
    font-size: 0.85rem;
}

.return-details-section {
    margin-bottom: 1rem;
}

.detail-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
}

.detail-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem;
    background: #f8fafc;
    border-radius: 6px;
    border: 1px solid #e2e8f0;
}

.detail-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #e2e8f0;
    color: #64748b;
}

.detail-item.quantity .detail-icon {
    background: #dbeafe;
    color: #3b82f6;
}

.detail-item.refund .detail-icon {
    background: #dcfce7;
    color: #16a34a;
}

.detail-content {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.detail-label {
    font-size: 0.85rem;
    color: #64748b;
    margin-bottom: 0.25rem;
}

.detail-value {
    font-weight: 600;
    color: #1e293b;
}

.detail-value.price {
    color: #dc2626;
}

.reason-section {
    margin-bottom: 1rem;
    padding: 1rem;
    background: #fef3c7;
    border-radius: 8px;
    border: 1px solid #fbbf24;
}

.reason-header {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
}

.reason-title {
    font-weight: 600;
    color: #1e293b;
}

.reason-text {
    margin: 0;
    line-height: 1.5;
    color: #374151;
}

.actions-section {
    border-top: 1px solid #f1f5f9;
    padding-top: 1rem;
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

/* New actions layout */
.actions-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
}

.actions-left {
    display: flex;
    gap: 0.5rem;
}

.action-block {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    padding: 0.75rem 1rem;
    box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}

.action-header {
    display: flex;
    align-items: center;
    gap: 0.25rem;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 0.5rem;
}

.action-body {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.pagination-section {
    margin-top: 2rem;
    display: flex;
    justify-content: center;
}

.empty-state {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 300px;
}

.empty-content {
    text-align: center;
    color: #64748b;
}

.empty-content i {
    font-size: 4rem;
    margin-bottom: 1rem;
    opacity: 0.5;
}

.empty-title {
    font-size: 1.25rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
    color: #374151;
}

.empty-description {
    margin: 0;
    font-size: 0.95rem;
}

/* Status indicators */
.status-pending {
    background: linear-gradient(135deg, #fff3cd, #ffeaa7);
    border-left: 4px solid #ffc107;
}

.status-approved {
    background: linear-gradient(135deg, #d4edda, #c3e6cb);
    border-left: 4px solid #28a745;
}

.status-rejected {
    background: linear-gradient(135deg, #f8d7da, #f5c6cb);
    border-left: 4px solid #dc3545;
}

/* Responsive design */
@media (max-width: 768px) {
    .return-items-grid {
        grid-template-columns: 1fr;
    }
    
    .section-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
    }
    
    .product-info-section {
        flex-direction: column;
        text-align: center;
    }
    
    .detail-grid {
        grid-template-columns: 1fr;
    }
    
    .action-buttons {
        justify-content: center;
    }

    .return-item-card {
        margin-bottom: 1rem;
    }

    .return-detail-content .row {
        flex-direction: column;
    }

    .return-detail-content .col-md-3,
    .return-detail-content .col-md-6 {
        margin-bottom: 0.5rem;
    }

    .evidence-img {
        width: 60px;
        height: 45px;
    }

    .process-all-content .d-flex {
        flex-direction: column;
        gap: 1rem;
    }
}

@media (max-width: 576px) {
    .return-management {
        padding: 0.5rem;
    }

    .gradient-header {
        padding: 1rem;
    }

    .stat-card .card-body {
        padding: 1rem;
    }

    .return-item-card {
        padding: 1rem;
    }

    .evidence-full-image {
        max-height: 250px;
    }
}

/* Cursor pointer for clickable elements */
.cursor-pointer {
    cursor: pointer;
}

/* Animation for success states */
@keyframes return-success {
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

.return-success-animation {
    animation: return-success 0.6s ease-out;
}

/* Custom scrollbar for reason text */
.reason-detail-content .reason-full-text::-webkit-scrollbar {
    width: 6px;
}

.reason-detail-content .reason-full-text::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
}

.reason-detail-content .reason-full-text::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
}

.reason-detail-content .reason-full-text::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}
</style>
