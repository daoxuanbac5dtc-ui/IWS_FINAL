<script setup>
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

// Router & Toast
const router = useRouter();
const toast = useToast();
const uploadedImages = ref([]);
const productImages = ref({}); // Lưu ảnh theo từng sản phẩm: {productId: [files]}
const maxImages = computed(() => {
    // Tính tổng số lượng sản phẩm đang được chọn để trả
    const totalQuantity = currentTotalReturnQuantity.value;
    return Math.max(1, Math.min(totalQuantity, 10)); // Tối thiểu 1, tối đa 10 ảnh
});
const isUploadingImages = ref(false);
const uploadProgress = ref({});
const currentUploadingFile = ref('');
const fileInput = ref(null);
const currentEditingProduct = ref(null); // Sản phẩm đang chỉnh sửa ảnh

// ===== REACTIVE STATE =====
const orders = ref([]);
const isLoading = ref(false);
const isLoadingProducts = ref(false);
const isLoadingReturns = ref(false);
const isLoadingOrderDetail = ref(false);
const isSubmitting = ref(false);

// Filter states
const searchKeyword = ref('');
const selectedStatus = ref(null);
const selectedDate = ref(null);

// Pagination
const first = ref(0);
const itemsPerPage = ref(6);

// Modal states
const showProductModal = ref(false);
const showReturnForm = ref(false);
const showQuickEditModal = ref(false);
const showReturnHistoryModal = ref(false);
const showOrderDetailModal = ref(false);
const showConfirmSubmitModal = ref(false);
const showEvidenceViewer = ref(false);

// Evidence viewer states
const selectedEvidenceImage = ref(null);
const selectedEvidenceProduct = ref(null);

// Selected order and products
const selectedOrder = ref(null);
const selectedOrderDetail = ref(null);
const orderProducts = ref([]);
const orderDetailProducts = ref([]);
const returnHistory = ref([]);
const existingReturns = ref([]);
const existingReturnsDetail = ref([]);

// Return form
const returnReason = ref('');
const customReason = ref('');
const additionalNote = ref('');
const refundMethod = ref('original');
const editingProduct = ref(null);
const isFromDetailModal = ref(false);

// ===== API CONFIGURATION =====
const API_BASE_URL = 'http://localhost:8080';

const getAuthToken = () =>
    localStorage.getItem('auth_token') ||
    localStorage.getItem('token') ||
    localStorage.getItem('accessToken') ||
    sessionStorage.getItem('auth_token') ||
    sessionStorage.getItem('token');

const getAuthHeaders = () => {
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };
    const token = getAuthToken();
    if (token) {
        const cleanToken = token.replace(/^Bearer\s+/i, '');
        headers.Authorization = `Bearer ${cleanToken}`;
        headers['X-Auth-Token'] = cleanToken;
    }
    return headers;
};

// ===== COMPUTED PROPERTIES =====
const filteredOrders = computed(() => {
    let filtered = [...orders.value];

    if (searchKeyword.value.trim()) {
        const keyword = searchKeyword.value.toLowerCase();
        filtered = filtered.filter((order) => order.maHoaDon.toLowerCase().includes(keyword) || order.tenKhachHang?.toLowerCase().includes(keyword));
    }

    if (selectedStatus.value) {
        filtered = filtered.filter((order) => order.trangThaiHoaDon === selectedStatus.value);
    }

    if (selectedDate.value) {
        const filterDate = new Date(selectedDate.value).toDateString();
        filtered = filtered.filter((order) => {
            const orderDate = new Date(order.ngayTao).toDateString();
            return orderDate === filterDate;
        });
    }

    return filtered.sort((a, b) => new Date(b.ngayTao) - new Date(a.ngayTao));
});

const paginatedOrders = computed(() => {
    const start = first.value;
    const end = start + itemsPerPage.value;
    return filteredOrders.value.slice(start, end);
});

const returnableProducts = computed(() => {
    return orderProducts.value.filter((product) => canReturnProduct(product));
});

const selectedProductsForReturn = computed(() => {
    return orderProducts.value.filter((product) => product.returnQuantity && product.returnQuantity > 0);
});

const selectedProductsCount = computed(() => {
    return selectedProductsForReturn.value.length;
});

const totalReturnAmount = computed(() => {
    return selectedProductsForReturn.value.reduce((total, product) => {
        return total + product.giaBan * product.returnQuantity;
    }, 0);
});

const totalReturnQuantity = computed(() => {
    return selectedProductsForReturn.value.reduce((total, product) => {
        return total + product.returnQuantity;
    }, 0);
});

// For detail modal
const returnableProductsDetail = computed(() => {
    return orderDetailProducts.value.filter((product) => canReturnProductDetail(product));
});

const selectedProductsForReturnDetail = computed(() => {
    return orderDetailProducts.value.filter((product) => product.returnQuantity && product.returnQuantity > 0);
});

const selectedProductsCountDetail = computed(() => {
    return selectedProductsForReturnDetail.value.length;
});

const totalReturnAmountDetail = computed(() => {
    return selectedProductsForReturnDetail.value.reduce((total, product) => {
        return total + product.giaBan * product.returnQuantity;
    }, 0);
});

const totalReturnQuantityDetail = computed(() => {
    return selectedProductsForReturnDetail.value.reduce((total, product) => {
        return total + product.returnQuantity;
    }, 0);
});

// Current selected for form (from detail or selection modal)
const currentSelectedProducts = computed(() => {
    return isFromDetailModal.value ? selectedProductsForReturnDetail.value : selectedProductsForReturn.value;
});

const currentTotalReturnAmount = computed(() => {
    return isFromDetailModal.value ? totalReturnAmountDetail.value : totalReturnAmount.value;
});

const currentTotalReturnQuantity = computed(() => {
    return isFromDetailModal.value ? totalReturnQuantityDetail.value : totalReturnQuantity.value;
});

// Computed cho lịch sử trả hàng đã gộp
const groupedReturnHistory = computed(() => {
    return groupReturnsByProduct(returnHistory.value);
});

const canSubmitReturn = computed(() => {
    const hasProducts = currentSelectedProducts.value.length > 0;
    const hasReason = returnReason.value && (returnReason.value !== 'Khác' || customReason.value.trim());
    return hasProducts && hasReason && !isSubmitting.value;
});

const uploadReturnImages = async (files) => {
    if (!files || files.length === 0) return [];

    isUploadingImages.value = true;
    uploadProgress.value = {};
    const uploadedPaths = [];
    const failedUploads = [];

    try {
        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            const fileKey = `${file.name}_${i}`;

            // Cập nhật tiến trình bắt đầu upload
            currentUploadingFile.value = file.name;
            uploadProgress.value[fileKey] = {
                fileName: file.name,
                status: 'uploading',
                progress: 0,
                error: null
            };

            try {
                // Tạm thời bỏ qua việc upload lên server
                // Chỉ tạo một URL tạm thời cho file
                const tempUrl = URL.createObjectURL(file);

                // Giả lập tiến trình upload
                for (let progress = 0; progress <= 100; progress += 10) {
                    uploadProgress.value[fileKey].progress = progress;
                    await new Promise((resolve) => setTimeout(resolve, 50));
                }

                // Tạo đường dẫn tạm thời
                const tempPath = `temp_${Date.now()}_${file.name}`;
                uploadedPaths.push(tempPath);
                uploadProgress.value[fileKey].status = 'success';
                uploadProgress.value[fileKey].progress = 100;

                console.log(`✅ Upload thành công ảnh ${i + 1}: ${file.name} (tạm thời)`);

                // Cleanup temp URL
                URL.revokeObjectURL(tempUrl);
            } catch (fileError) {
                uploadProgress.value[fileKey].status = 'error';
                uploadProgress.value[fileKey].error = fileError.message;
                console.error(`❌ Lỗi xử lý ảnh ${i + 1}: ${file.name}`, fileError);
                failedUploads.push({
                    fileName: file.name,
                    error: fileError.message || 'Lỗi xử lý file'
                });
            }
        }

        // Hiển thị thông báo kết quả upload
        if (failedUploads.length > 0) {
            const failedNames = failedUploads.map((f) => f.fileName).join(', ');
            toast.add({
                severity: 'warn',
                summary: 'Cảnh báo',
                detail: `Xử lý thất bại ${failedUploads.length} ảnh: ${failedNames}`,
                life: 5000
            });
        }

        if (uploadedPaths.length > 0) {
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã chuẩn bị ${uploadedPaths.length}/${files.length} ảnh minh chứng`,
                life: 3000
            });
        }

        return uploadedPaths;
    } catch (error) {
        console.error('Error processing images:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi nghiêm trọng',
            detail: 'Có lỗi xảy ra khi xử lý ảnh: ' + error.message,
            life: 5000
        });
        throw new Error('Lỗi xử lý ảnh: ' + error.message);
    } finally {
        isUploadingImages.value = false;
        currentUploadingFile.value = '';
        // Giữ progress data trong 5 giây để user có thể xem kết quả
        setTimeout(() => {
            uploadProgress.value = {};
        }, 5000);
    }
};

// ===== API FUNCTIONS =====
async function fetchWithErrorHandling(url, options = {}) {
    try {
        const response = await fetch(url, {
            headers: {
                ...getAuthHeaders(),
                ...options.headers
            },
            ...options
        });

        if (!response.ok) {
            if (response.status === 401) {
                throw new Error('Phiên đăng nhập đã hết hạn');
            }
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || errorData.error || `HTTP ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error(`API Error for ${url}:`, error);
        throw error;
    }
}

const loadOrders = async () => {
    isLoading.value = true;
    try {
        const token = getAuthToken();
        const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}');

        console.log('🔑 Auth token:', token ? 'Present' : 'Missing');
        console.log('👤 User info:', userInfo);
        console.log('🌐 API URL:', `${API_BASE_URL}/hoa-don/my-orders`);

        // Thử các endpoint khác nhau để lấy hóa đơn cá nhân
        let response;
        try {
            // Thử endpoint với user ID nếu có
            if (userInfo.id) {
                console.log('🔄 Trying with user ID:', userInfo.id);
                response = await fetchWithErrorHandling(`${API_BASE_URL}/hoa-don/user/${userInfo.id}`);
            } else {
                throw new Error('No user ID');
            }
        } catch (error) {
            console.log('🔄 Fallback to my-orders endpoint');
            response = await fetchWithErrorHandling(`${API_BASE_URL}/hoa-don/my-orders`);
        }
        console.log('📦 API Response:', response);

        if (Array.isArray(response)) {
            orders.value = response;
            console.log('✅ Orders loaded (array):', response.length);
        } else if (response.data && Array.isArray(response.data)) {
            orders.value = response.data;
            console.log('✅ Orders loaded (data):', response.data.length);
        } else {
            orders.value = [];
            console.log('⚠️ No orders found');
        }

        console.log('📋 Final orders:', orders.value);
    } catch (error) {
        console.error('❌ Error loading orders:', error);
        handleApiError(error);
    } finally {
        isLoading.value = false;
    }
};

const loadOrdersSafe = async () => {
    isLoading.value = true;
    try {
        const token = getAuthToken();
        const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}');

        console.log('đŸ”‘ Auth token:', token ? 'Present' : 'Missing');
        console.log('đŸ‘¤ User info:', userInfo);
        console.log('đŸŒ API URL:', `${API_BASE_URL}/hoa-don/my-orders`);

        const response = await fetchWithErrorHandling(`${API_BASE_URL}/hoa-don/my-orders`);
        console.log('đŸ“¦ API Response:', response);

        if (Array.isArray(response)) {
            orders.value = response;
            console.log('âœ… Orders loaded (array):', response.length);
        } else if (response.data && Array.isArray(response.data)) {
            orders.value = response.data;
            console.log('âœ… Orders loaded (data):', response.data.length);
        } else {
            orders.value = [];
            console.log('â ï¸ No orders found');
        }

        console.log('đŸ“‹ Final orders:', orders.value);
    } catch (error) {
        console.error('âŒ Error loading orders:', error);
        handleApiError(error);
    } finally {
        isLoading.value = false;
    }
};

const loadOrderData = async (orderId, isDetailModal = false) => {
    const loadingState = isDetailModal ? isLoadingOrderDetail : isLoadingProducts;
    loadingState.value = true;
    try {
        const response = await fetchWithErrorHandling(`${API_BASE_URL}/hoa-don/${orderId}/chi-tiet`);

        if (response.success && response.data) {
            if (isDetailModal) {
                selectedOrderDetail.value = response.data.hoaDon || response.data;
                orderDetailProducts.value = response.data.chiTietSanPham
                    ? response.data.chiTietSanPham.map((product) => ({
                          ...product,
                          returnQuantity: 0,
                          hinhAnh: createImageUrl(product.hinhAnh)
                      }))
                    : [];
                await loadExistingReturnsDetail(orderId);
            } else {
                selectedOrder.value = response.data.hoaDon || response.data;
                orderProducts.value = response.data.chiTietSanPham
                    ? response.data.chiTietSanPham.map((product) => ({
                          ...product,
                          returnQuantity: 0,
                          hinhAnh: createImageUrl(product.hinhAnh)
                      }))
                    : [];
                await loadExistingReturns(orderId);
            }
        }
    } catch (error) {
        console.error('Error loading order data:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: `Không thể tải ${isDetailModal ? 'chi tiết đơn hàng' : 'danh sách sản phẩm'}`,
            life: 3000
        });
    } finally {
        loadingState.value = false;
    }
};

const loadOrderDetail = async (orderId) => {
    await loadOrderData(orderId, true);
};

const loadOrderProducts = async (orderId) => {
    await loadOrderData(orderId, false);
};

const loadExistingReturns = async (orderId) => {
    try {
        const response = await fetchWithErrorHandling(`${API_BASE_URL}/api/chi-tiet-tra-hang/by-hoa-don/${orderId}`);

        if (response.success && response.data) {
            // KHÔNG gộp - hiển thị từng bản ghi riêng biệt như trong database
            existingReturns.value = response.data; // Giữ nguyên để tính toán
            
            // Xử lý từng bản ghi riêng biệt, thêm ảnh minh chứng nếu có
            returnHistory.value = response.data.map(returnItem => ({
                ...returnItem,
                // Xử lý ảnh minh chứng - có thể là string hoặc array
                anhMinhChung: returnItem.duongDanAnh ? 
                    (Array.isArray(returnItem.duongDanAnh) ? returnItem.duongDanAnh : [returnItem.duongDanAnh]) : 
                    []
            }));
            
            console.log('📋 Loaded individual return records:', returnHistory.value.length);
        } else {
            existingReturns.value = [];
            returnHistory.value = [];
        }
    } catch (error) {
        console.error('Error loading returns:', error);
        existingReturns.value = [];
        returnHistory.value = [];
    }
};

const loadExistingReturnsDetail = async (orderId) => {
    try {
        const response = await fetchWithErrorHandling(`${API_BASE_URL}/api/chi-tiet-tra-hang/by-hoa-don/${orderId}`);

        if (response.success && response.data) {
            existingReturnsDetail.value = response.data; // Giữ nguyên để tính toán
        } else {
            existingReturnsDetail.value = [];
        }
    } catch (error) {
        console.error('Error loading returns for detail:', error);
        existingReturnsDetail.value = [];
    }
};

// ===== SỬA ĐỔI THEO YÊU CẦU - CHO PHÉP TRẢ 100% VÀ NHIỀU ẢNH =====
const submitReturnRequest = async () => {
    if (!canSubmitReturn.value) return;

    console.log('=== STARTING RETURN REQUEST SUBMISSION ===');

    isSubmitting.value = true;

    try {
        // Làm mới dữ liệu trả hàng hiện có để đảm bảo tính toán maxReturn chính xác
        if (selectedOrder.value?.id) {
            if (isFromDetailModal.value) {
                await loadExistingReturnsDetail(selectedOrder.value.id);
            } else {
                await loadExistingReturns(selectedOrder.value.id);
            }
        }

        const productsToReturn = currentSelectedProducts.value;
        const finalReason = returnReason.value === 'Khác' ? customReason.value : returnReason.value;
        const fullReason = finalReason + (additionalNote.value ? ` - ${additionalNote.value}` : '');

        console.log('Submitting return request:', {
            orderId: selectedOrder.value.id,
            reason: fullReason,
            products: productsToReturn.map((p) => ({
                id: p.chiTietSanPhamId,
                name: p.tenSanPham,
                quantity: p.returnQuantity,
                purchased: p.soLuong
            })),
            totalImages: uploadedImages.value.length
        });

        // Re-validate số lượng theo dữ liệu mới nhất
        for (const product of productsToReturn) {
            const maxQty = isFromDetailModal.value ? getMaxReturnQuantityDetail(product) : getMaxReturnQuantity(product);
            if (product.returnQuantity > maxQty) {
                const purchased = product.soLuong || 0;
                const returned = isFromDetailModal.value ? getReturnedQuantityDetail(product) : getReturnedQuantity(product);
                
                toast.add({
                    severity: 'warn',
                    summary: 'Số lượng vượt quá giới hạn',
                    detail: `Sản phẩm "${product.tenSanPham}": Đã mua ${purchased}, đã trả ${returned}, chỉ có thể trả thêm ${maxQty}. Vui lòng điều chỉnh lại.`,
                    life: 7000
                });
                return; // Dừng submit để người dùng chỉnh lại
            }
        }

        const requests = [];

        for (const product of productsToReturn) {
            const productId = product.chiTietSanPhamId;
            const productImages = getProductImages(productId);
            const productQuantity = product.returnQuantity;

            console.log(`Creating request for product ${productId}:`, {
                name: product.tenSanPham,
                quantity: productQuantity,
                productImages: productImages.length,
                totalSharedImages: uploadedImages.value.length
            });

            // Tạo request riêng cho từng đơn vị sản phẩm
            for (let i = 0; i < productQuantity; i++) {
                const formData = new FormData();
                formData.append('chiTietSanPhamId', productId.toString());
                formData.append('soLuong', '1'); // Mỗi request chỉ trả 1 sản phẩm
                formData.append('lyDo', fullReason);
                formData.append('hoaDonId', selectedOrder.value.id.toString());

                // Thêm ảnh cho sản phẩm này (nếu có)
                if (productImages.length > i) {
                    const imageFile = productImages[i];
                    formData.append('anhMinhChung', imageFile, imageFile.name);
                    console.log(`Added product image ${i + 1}: ${imageFile.name} for product ${productId}`);
                } else if (uploadedImages.value.length > 0) {
                    // Fallback: dùng ảnh chung nếu không có ảnh riêng
                    const sharedImageIndex = i % uploadedImages.value.length;
                    const sharedImage = uploadedImages.value[sharedImageIndex];
                    formData.append('anhMinhChung', sharedImage, sharedImage.name);
                    console.log(`Added shared image ${sharedImageIndex + 1}: ${sharedImage.name} for product ${productId}`);
                }

                const requestPromise = fetch(`${API_BASE_URL}/api/chi-tiet-tra-hang/create-with-image`, {
                    method: 'POST',
                    headers: {
                        Authorization: `Bearer ${getAuthToken()}`
                    },
                    body: formData
                }).then(async (response) => {
                    const text = await response.text();
                    console.log(`Response for product ${productId} unit ${i + 1}:`, {
                        status: response.status,
                        text: text
                    });

                    if (!response.ok) {
                        throw new Error(`HTTP ${response.status}: ${text}`);
                    }

                    try {
                        return JSON.parse(text);
                    } catch (e) {
                        return { success: true, message: text };
                    }
                });

                requests.push(requestPromise);
            }
        }

        const results = await Promise.allSettled(requests);

        console.log('Results:', results);

        const successful = results.filter((r) => r.status === 'fulfilled').length;
        const failed = results.filter((r) => r.status === 'rejected').length;

        const errors = results.filter((r) => r.status === 'rejected').map((r) => r.reason.message || r.reason.toString());

        if (successful > 0) {
            const totalProductImages = getTotalProductImages();
            const totalSharedImages = uploadedImages.value.length;
            const totalImages = totalProductImages + totalSharedImages;
            
            const imageText = totalImages > 0 ? ` với ${totalImages} ảnh minh chứng` : '';
            const productText = productsToReturn.length > 1 ? ` cho ${productsToReturn.length} loại sản phẩm` : '';
            
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã tạo ${successful} yêu cầu trả hàng thành công${productText}${failed > 0 ? `, ${failed} yêu cầu thất bại` : ''}${imageText}`,
                life: 5000
            });

            resetReturnForm();

            if (isFromDetailModal.value) {
                closeOrderDetailModal();
            } else {
                closeProductModal();
            }

            await loadOrdersSafe();
        } else {
            throw new Error('Không thể tạo yêu cầu trả hàng: ' + errors.join(', '));
        }
    } catch (error) {
        console.error('Error submitting return request:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.message || 'Có lỗi xảy ra khi gửi yêu cầu',
            life: 8000
        });
    } finally {
        isSubmitting.value = false;
    }
};

// ===== SỬA ĐỔI CÁC HÀM KIỂM TRA SỐ LƯỢNG =====
const getMaxReturnQuantity = (product) => {
    const purchased = product.soLuong || 0;
    const returned = getReturnedQuantity(product);
    // Backend không cho trả 100%, luôn phải giữ lại ít nhất 1 sản phẩm
    // Trừ khi đã có yêu cầu trả trước đó được duyệt
    const maxReturn = Math.max(0, purchased - returned);

    console.log(`Max return calculation for product ${product.chiTietSanPhamId}:`, {
        purchased,
        returned,
        maxReturn,
        note: 'Backend may restrict 100% returns'
    });

    return maxReturn;
};

const getMaxReturnQuantityDetail = (product) => {
    const purchased = product.soLuong || 0;
    const returned = getReturnedQuantityDetail(product);
    const maxReturn = Math.max(0, purchased - returned);

    console.log(`Max return detail calculation for product ${product.chiTietSanPhamId}:`, {
        purchased,
        returned,
        maxReturn,
        note: 'Backend may restrict 100% returns'
    });

    return maxReturn;
};

// ===== SỬA ĐỔI VALIDATION =====
const updateProductSelection = (product) => {
    const maxReturn = getMaxReturnQuantity(product);

    if (product.returnQuantity < 0) {
        product.returnQuantity = 0;
    }
    if (product.returnQuantity > maxReturn) {
        product.returnQuantity = maxReturn;

        toast.add({
            severity: 'warn',
            summary: 'Giới hạn số lượng',
            detail: `Sản phẩm "${product.tenSanPham}" chỉ có thể trả tối đa ${maxReturn} sản phẩm`,
            life: 3000
        });
    }

    // BỎ HẾT các warning về trả 100%
};

const updateProductSelectionDetail = (product) => {
    const maxReturn = getMaxReturnQuantityDetail(product);

    if (product.returnQuantity < 0) {
        product.returnQuantity = 0;
    }
    if (product.returnQuantity > maxReturn) {
        product.returnQuantity = maxReturn;

        toast.add({
            severity: 'warn',
            summary: 'Giới hạn số lượng',
            detail: `Sản phẩm "${product.tenSanPham}" chỉ có thể trả tối đa ${maxReturn} sản phẩm`,
            life: 3000
        });
    }

    // BỎ HẾT các warning về trả 100%
};

// ===== SỬA ĐỔI CALCULATION FUNCTIONS =====
const getReturnedQuantity = (product) => {
    // Tính tất cả yêu cầu trả hàng không bị từ chối/hủy để khớp logic backend
    const returned = existingReturns.value
        .filter((r) => {
            const sameProduct = r.chiTietSanPhamId === product.chiTietSanPhamId;
            const status = r.trangThaiHoaDon || r.trangThai;
            // Các trạng thái KHÔNG được tính vào tổng (đã bị từ chối/hủy)
            const excludedStatuses = ['REJECTED', 'CANCELLED', 'DENIED', 'DA_HUY', 'HUY'];
            const isCountable = !excludedStatuses.includes(status);

            console.log(`Return check for product ${product.chiTietSanPhamId}:`, {
                returnId: r.id,
                sameProduct,
                status,
                isCountable,
                quantity: r.soLuong
            });

            return sameProduct && isCountable;
        })
        .reduce((sum, r) => sum + (r.soLuong || 0), 0);

    console.log(`Total COUNTABLE returned for product ${product.chiTietSanPhamId}: ${returned}`);
    return returned;
};

const getReturnedQuantityDetail = (product) => {
    // Tính tất cả yêu cầu trả hàng không bị từ chối/hủy để khớp logic backend
    const returned = existingReturnsDetail.value
        .filter((r) => {
            const sameProduct = r.chiTietSanPhamId === product.chiTietSanPhamId;
            const status = r.trangThaiHoaDon || r.trangThai;
            const excludedStatuses = ['REJECTED', 'CANCELLED', 'DENIED', 'DA_HUY', 'HUY'];
            const isCountable = !excludedStatuses.includes(status);
            return sameProduct && isCountable;
        })
        .reduce((sum, r) => sum + (r.soLuong || 0), 0);

    console.log(`Total COUNTABLE returned for product detail ${product.chiTietSanPhamId}: ${returned}`);
    return returned;
};

// ===== ĐƠN GIẢN HÓA BUSINESS LOGIC =====
const canReturnProduct = (product) => {
    const totalReturned = getReturnedQuantity(product);
    return totalReturned < product.soLuong; // Đơn giản: chỉ check không vượt quá số đã mua
};

const canReturnProductDetail = (product) => {
    const totalReturned = getReturnedQuantityDetail(product);
    return totalReturned < product.soLuong; // Đơn giản: chỉ check không vượt quá số đã mua
};

const getProductReturnStatus = (product) => {
    const totalReturned = getReturnedQuantity(product);

    if (totalReturned >= product.soLuong) {
        return {
            status: 'completed',
            message: 'Đã trả hết',
            canReturn: false
        };
    }

    return {
        status: 'available',
        message: 'Có thể trả',
        canReturn: true
    };
};

const getProductReturnStatusDetail = (product) => {
    const totalReturned = getReturnedQuantityDetail(product);

    if (totalReturned >= product.soLuong) {
        return {
            status: 'completed',
            message: 'Đã trả hết',
            canReturn: false
        };
    }

    return {
        status: 'available',
        message: 'Có thể trả',
        canReturn: true
    };
};

const triggerFileSelect = () => {
    if (fileInput.value) {
        fileInput.value.click();
    }
};

const handleImageUpload = (event) => {
    try {
        const files = Array.from(event.target.files || event.files || []);

        if (files.length === 0) return;

        // Kiểm tra số lượng file có vượt quá giới hạn không
        const currentMaxImages = maxImages.value;
        const currentImageCount = uploadedImages.value.length;
        const availableSlots = currentMaxImages - currentImageCount;

        if (files.length > availableSlots) {
            toast.add({
                severity: 'warn',
                summary: 'Cảnh báo',
                detail: `Chỉ có thể thêm ${availableSlots} ảnh nữa. Tổng số lượng sản phẩm trả: ${currentTotalReturnQuantity.value}`,
                life: 4000
            });
            return;
        }

        // Validate và thêm từng file
        const validFiles = [];
        for (const file of files) {
            if (validateImageFile(file)) {
                // Tạo preview URL cho file
                file.preview = URL.createObjectURL(file);
                validFiles.push(file);
            }
        }

        // Thêm các file hợp lệ vào danh sách
        uploadedImages.value.push(...validFiles);

        if (validFiles.length > 0) {
            console.log(`✅ Đã thêm ${validFiles.length} file hợp lệ`);
            toast.add({
                severity: 'success',
                summary: 'Thành công',
                detail: `Đã chọn ${validFiles.length} ảnh minh chứng`,
                life: 2000
            });
        }

        // Reset input để có thể chọn lại file nếu cần
        if (event.target) {
            event.target.value = '';
        }
    } catch (error) {
        console.error('❌ Lỗi xử lý upload ảnh:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi nghiêm trọng',
            detail: 'Có lỗi xảy ra khi xử lý ảnh: ' + error.message,
            life: 5000
        });
    }
};

const validateImageFile = (file) => {
    try {
        // Kiểm tra file có tồn tại không
        if (!file) {
            console.error('❌ File không tồn tại');
            return false;
        }

        // Kiểm tra loại file
        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
        if (!allowedTypes.includes(file.type)) {
            console.error(`❌ Loại file không được hỗ trợ: ${file.type} cho file ${file.name}`);
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: `File "${file.name}" không phải là ảnh hợp lệ. Chỉ chấp nhận JPG, PNG, GIF, WebP`,
                life: 4000
            });
            return false;
        }

        // Kiểm tra kích thước (5MB)
        const maxSize = 5 * 1024 * 1024; // 5MB
        if (file.size > maxSize) {
            const fileSizeMB = (file.size / 1024 / 1024).toFixed(2);
            console.error(`❌ File quá lớn: ${fileSizeMB}MB cho file ${file.name}`);
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: `File "${file.name}" quá lớn (${fileSizeMB}MB). Kích thước tối đa là 5MB`,
                life: 4000
            });
            return false;
        }

        // Kiểm tra file có rỗng không
        if (file.size === 0) {
            console.error(`❌ File rỗng: ${file.name}`);
            toast.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: `File "${file.name}" bị rỗng hoặc hỏng`,
                life: 4000
            });
            return false;
        }

        console.log(`✅ File hợp lệ: ${file.name} (${file.type}, ${(file.size / 1024 / 1024).toFixed(2)}MB)`);
        return true;
    } catch (error) {
        console.error(`❌ Lỗi validate file ${file?.name || 'unknown'}:`, error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: `Lỗi kiểm tra file: ${error.message}`,
            life: 4000
        });
        return false;
    }
};

const removeImage = (index) => {
    const file = uploadedImages.value[index];

    // Cleanup object URL để tránh memory leak
    if (file && file.preview) {
        URL.revokeObjectURL(file.preview);
    }

    uploadedImages.value.splice(index, 1);
    toast.add({
        severity: 'info',
        summary: 'Đã xóa',
        detail: 'Đã xóa ảnh minh chứng',
        life: 2000
    });
};

const getImagePreview = (file) => {
    if (!file) return null;
    // Nếu đã có preview URL, ưu tiên dùng preview
    if (file.preview) {
        return file.preview;
    }
    // Nếu là File, tạo preview nếu chưa có
    if (file instanceof File) {
        if (!file.preview) {
            file.preview = URL.createObjectURL(file);
        }
        return file.preview;
    }
    // Nếu đã có URL sẵn
    if (typeof file === 'string') {
        return file;
    }
    return null;
};

const confirmSubmitReturn = () => {
    showConfirmSubmitModal.value = true;
};

const submitReturnRequestWithConfirm = async () => {
    showConfirmSubmitModal.value = false;
    await submitReturnRequest();
};

// ===== UTILITY FUNCTIONS =====
const createImageUrl = (imagePath) => {
    if (!imagePath) return null;
    if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
        return imagePath;
    }
    return `${API_BASE_URL}/${imagePath.replace(/^\//, '')}`;
};

const getProductImage = (item) => {
    return item.hinhAnh || getPlaceholderImage(item);
};

const getPlaceholderImage = (item) => {
    const productName = item?.tenSanPham || 'Sản phẩm';
    // Escape HTML entities để tránh lỗi trong SVG
    const safeName = productName.substring(0, 10).replace(/[<>&"']/g, '');
    
    const svgContent = `<svg width="120" height="120" xmlns="http://www.w3.org/2000/svg">
<rect width="120" height="120" fill="#f3f4f6"/>
<text x="60" y="55" text-anchor="middle" dominant-baseline="middle" font-family="Arial" font-size="12" fill="#9ca3af">${safeName}</text>
<text x="60" y="75" text-anchor="middle" dominant-baseline="middle" font-family="Arial" font-size="10" fill="#9ca3af">No Image</text>
</svg>`;
    
    try {
        return `data:image/svg+xml;base64,${btoa(unescape(encodeURIComponent(svgContent)))}`;
    } catch (error) {
        console.error('Error creating placeholder image:', error);
        // Fallback to simple data URL
        return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svgContent)}`;
    }
};

const handleImageError = (event) => {
    event.target.src = getPlaceholderImage({ tenSanPham: 'Sản phẩm' });
};

const formatDate = (date) => {
    if (!date) return 'Chưa có';

    try {
        let parsedDate;

        if (typeof date === 'string') {
            if (date.includes('/') && date.includes(' ')) {
                const [datePart, timePart] = date.split(' ');
                const [day, month, year] = datePart.split('/');
                const [hour, minute, second] = timePart.split(':');

                const dateString = `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}T${hour.padStart(2, '0')}:${minute.padStart(2, '0')}:${(second || '00').padStart(2, '0')}`;
                parsedDate = new Date(dateString);
                parsedDate.setTime(parsedDate.getTime() + 7 * 60 * 60 * 1000);
            } else {
                parsedDate = new Date(date);
                parsedDate.setTime(parsedDate.getTime() + 7 * 60 * 60 * 1000);
            }
        } else {
            parsedDate = new Date(date);
            parsedDate.setTime(parsedDate.getTime() + 7 * 60 * 60 * 1000);
        }

        if (isNaN(parsedDate.getTime())) {
            return 'Ngày không hợp lệ';
        }

        return parsedDate.toLocaleDateString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (error) {
        console.error('Error formatting date:', error, 'Input:', date);
        return 'Ngày không hợp lệ';
    }
};

const formatCurrency = (amount) => {
    if (!amount && amount !== 0) return '0₫';
    return new Intl.NumberFormat('vi-VN').format(amount) + '₫';
};

const handleApiError = (error) => {
    if (error.message.includes('hết hạn')) {
        toast.add({
            severity: 'error',
            summary: 'Phiên đăng nhập hết hạn',
            detail: 'Vui lòng đăng nhập lại',
            life: 5000
        });
        router.push('/login');
    } else {
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: error.message || 'Có lỗi xảy ra',
            life: 3000
        });
    }
};

// ===== STATUS FUNCTIONS =====
const getStatusLabel = (status) => {
    const statusMap = {
        CHO_XAC_NHAN: 'Chờ xác nhận',
        PENDING: 'Chờ xác nhận',
        DA_XAC_NHAN: 'Đã xác nhận',
        CONFIRMED: 'Đã xác nhận',
        DANG_GIAO: 'Đang giao',
        SHIPPING: 'Đang giao',
        DA_GIAO: 'Đã giao',
        DELIVERED: 'Đã giao',
        HOAN_THANH: 'Hoàn thành',
        COMPLETED: 'Hoàn thành',
        DA_HUY: 'Đã hủy',
        CANCELLED: 'Đã hủy'
    };
    return statusMap[status] || status;
};

const getStatusSeverity = (status) => {
    const severityMap = {
        CHO_XAC_NHAN: 'warning',
        PENDING: 'warning',
        DA_XAC_NHAN: 'info',
        CONFIRMED: 'info',
        DANG_GIAO: null,
        SHIPPING: null,
        DA_GIAO: 'success',
        DELIVERED: 'success',
        HOAN_THANH: 'success',
        COMPLETED: 'success',
        DA_HUY: 'danger',
        CANCELLED: 'danger'
    };
    return severityMap[status] || 'secondary';
};

const getReturnStatusLabel = (status) => {
    const statusMap = {
        PENDING: 'Chờ xử lý',
        APPROVED: 'Đã chấp nhận',
        REJECTED: 'Đã từ chối'
    };
    return statusMap[status] || status;
};

const getReturnStatusSeverity = (status) => {
    const severityMap = {
        PENDING: 'warning',
        APPROVED: 'success',
        REJECTED: 'danger'
    };
    return severityMap[status] || 'secondary';
};

// ===== BUSINESS LOGIC =====
const canReturnOrder = (order) => {
    if (!order) return false;
    const allowedStatuses = ['DA_GIAO', 'HOAN_THANH', 'COMPLETED', 'DELIVERED'];
    return allowedStatuses.includes(order.trangThaiHoaDon);
};

// Cho phép hủy đơn cho KH: chỉ khi CHO_XAC_NHAN (theo backend /hoa-don/{id}/cancel)
const canCancelOrder = (order) => {
  if (!order) return false;
  const status = (order.trangThaiHoaDon || '').toString().toUpperCase();
  return status === 'CHO_XAC_NHAN';
};

const getReturnNotice = (order) => {
    const statusMessages = {
        CHO_XAC_NHAN: 'Đơn hàng chờ xác nhận',
        PENDING: 'Đơn hàng chờ xác nhận',
        DA_XAC_NHAN: 'Đơn hàng chưa được giao', 
        CONFIRMED: 'Đơn hàng chưa được giao',
        DANG_GIAO: 'Đơn hàng đang được giao',
        SHIPPING: 'Đơn hàng đang được giao',
        DA_HUY: 'Đơn hàng đã bị hủy',
        CANCELLED: 'Đơn hàng đã bị hủy'
    };

    const orderDate = new Date(order.ngayTao);
    const currentDate = new Date();
    const diffDays = (currentDate - orderDate) / (1000 * 60 * 60 * 24);

    if (diffDays > 7 && ['DA_GIAO', 'HOAN_THANH', 'COMPLETED', 'DELIVERED'].includes(order.trangThaiHoaDon)) {
        return 'Đơn hàng đã quá 7 ngày, không thể trả';
    }
    return statusMessages[order.trangThaiHoaDon] || 'Không thể trả hàng';
};

const isProductSelected = (product) => {
    return product.returnQuantity > 0;
};

// ===== PRODUCT MANAGEMENT =====
const increaseQuantity = (product) => {
    if (product.returnQuantity < getMaxReturnQuantity(product)) {
        product.returnQuantity++;
        updateProductSelection(product);
    }
};

const decreaseQuantity = (product) => {
    if (product.returnQuantity > 0) {
        product.returnQuantity--;
        updateProductSelection(product);
    }
};

const increaseQuantityDetail = (product) => {
    if (product.returnQuantity < getMaxReturnQuantityDetail(product)) {
        product.returnQuantity++;
        updateProductSelectionDetail(product);
    }
};

const decreaseQuantityDetail = (product) => {
    if (product.returnQuantity > 0) {
        product.returnQuantity--;
        updateProductSelectionDetail(product);
    }
};

// ===== EVENT HANDLERS =====
const onSearch = () => {
    first.value = 0;
};

const onFilterChange = () => {
    first.value = 0;
};

const clearFilters = () => {
    searchKeyword.value = '';
    selectedStatus.value = null;
    selectedDate.value = null;
    first.value = 0;
};

const onPageChange = (event) => {
    first.value = event.first;
};

const viewOrderDetails = async (order) => {
    selectedOrderDetail.value = order;
    isFromDetailModal.value = true;
    showOrderDetailModal.value = true;
    await loadOrderDetail(order.id);
};

const selectOrderForReturn = async (order) => {
    selectedOrder.value = order;
    isFromDetailModal.value = false;
    showProductModal.value = true;
    await loadOrderProducts(order.id);
};

const proceedToReturnFormFromDetail = async () => {
    if (selectedProductsCountDetail.value === 0) {
        toast.add({
            severity: 'warn',
            summary: 'Cảnh báo',
            detail: 'Vui lòng chọn ít nhất một sản phẩm để trả',
            life: 3000
        });
        return;
    }

    selectedOrder.value = selectedOrderDetail.value;
    showReturnForm.value = true;
    showOrderDetailModal.value = false;
};

const closeProductModal = () => {
    showProductModal.value = false;
    selectedOrder.value = null;
    orderProducts.value = [];
    returnHistory.value = [];
    existingReturns.value = [];
};

const closeOrderDetailModal = () => {
    showOrderDetailModal.value = false;
    selectedOrderDetail.value = null;
    orderDetailProducts.value = [];
    existingReturnsDetail.value = [];
    orderDetailProducts.value.forEach((product) => {
        product.returnQuantity = 0;
    });
};

const proceedToReturnForm = () => {
    if (selectedProductsCount.value === 0) {
        toast.add({
            severity: 'warn',
            summary: 'Cảnh báo',
            detail: 'Vui lòng chọn ít nhất một sản phẩm để trả',
            life: 3000
        });
        return;
    }

    showReturnForm.value = true;
};

const closeReturnForm = () => {
    showReturnForm.value = false;
    resetReturnForm();
};

const goBackToProductSelection = () => {
    showReturnForm.value = false;
    if (isFromDetailModal.value) {
        showOrderDetailModal.value = true;
    } else {
        showProductModal.value = true;
    }
};

const resetReturnForm = () => {
    returnReason.value = '';
    customReason.value = '';
    additionalNote.value = '';
    refundMethod.value = 'original';
    uploadedImages.value = [];
    productImages.value = {};
    currentEditingProduct.value = null;
};

// ===== PRODUCT IMAGE MANAGEMENT =====
const getProductImages = (productId) => {
    return productImages.value[productId] || [];
};

const setProductImages = (productId, images) => {
    productImages.value[productId] = images;
};

const addProductImage = (productId, image) => {
    if (!productImages.value[productId]) {
        productImages.value[productId] = [];
    }
    productImages.value[productId].push(image);
};

const removeProductImage = (productId, imageIndex) => {
    if (productImages.value[productId]) {
        const removedImage = productImages.value[productId][imageIndex];
        if (removedImage && removedImage.preview) {
            URL.revokeObjectURL(removedImage.preview);
        }
        productImages.value[productId].splice(imageIndex, 1);
        
        if (productImages.value[productId].length === 0) {
            delete productImages.value[productId];
        }
    }
};

const getTotalProductImages = () => {
    return Object.values(productImages.value).reduce((total, images) => total + images.length, 0);
};

// ===== GROUP RETURNS BY PRODUCT =====
const groupReturnsByProduct = (returns) => {
    if (!returns || returns.length === 0) return [];

    const grouped = {};
    
    returns.forEach(returnItem => {
        const key = `${returnItem.chiTietSanPhamId}_${returnItem.hoaDonId}`;
        
        if (!grouped[key]) {
            // Tạo item gộp mới
            grouped[key] = {
                ...returnItem,
                soLuong: 0,
                originalReturns: [], // Lưu các yêu cầu gốc
                trangThaiList: [], // Lưu các trạng thái
                ngayTaoList: [], // Lưu các ngày tạo
                lyDoList: [], // Lưu các lý do
                anhMinhChungList: [] // Lưu các ảnh
            };
        }
        
        // Cộng dồn số lượng
        grouped[key].soLuong += returnItem.soLuong || 0;
        
        // Lưu thông tin chi tiết
        grouped[key].originalReturns.push(returnItem);
        grouped[key].trangThaiList.push(returnItem.trangThaiHoaDon);
        grouped[key].ngayTaoList.push(returnItem.ngayTao);
        
        if (returnItem.lyDo && !grouped[key].lyDoList.includes(returnItem.lyDo)) {
            grouped[key].lyDoList.push(returnItem.lyDo);
        }
        
        if (returnItem.duongDanAnh && !grouped[key].anhMinhChungList.includes(returnItem.duongDanAnh)) {
            grouped[key].anhMinhChungList.push(returnItem.duongDanAnh);
        }
        
        // Cập nhật trạng thái tổng hợp (ưu tiên: APPROVED > PENDING > REJECTED)
        const currentStatus = grouped[key].trangThaiHoaDon;
        const newStatus = returnItem.trangThaiHoaDon;
        
        if (!currentStatus || 
            (newStatus === 'APPROVED' && currentStatus !== 'APPROVED') ||
            (newStatus === 'PENDING' && currentStatus === 'REJECTED')) {
            grouped[key].trangThaiHoaDon = newStatus;
        }
        
        // Cập nhật ngày tạo mới nhất
        if (!grouped[key].ngayTao || new Date(returnItem.ngayTao) > new Date(grouped[key].ngayTao)) {
            grouped[key].ngayTao = returnItem.ngayTao;
        }
    });
    
    // Chuyển về array và sắp xếp theo ngày tạo
    return Object.values(grouped).sort((a, b) => new Date(b.ngayTao) - new Date(a.ngayTao));
};

// ===== HELPER FUNCTIONS FOR GROUPED RETURNS =====
const getGroupedStatusLabel = (groupedReturn) => {
    if (!groupedReturn.trangThaiList) return 'Không xác định';
    
    const statusCounts = {};
    groupedReturn.trangThaiList.forEach(status => {
        statusCounts[status] = (statusCounts[status] || 0) + 1;
    });
    
    const statusLabels = [];
    if (statusCounts.APPROVED) statusLabels.push(`${statusCounts.APPROVED} đã duyệt`);
    if (statusCounts.PENDING) statusLabels.push(`${statusCounts.PENDING} chờ xử lý`);
    if (statusCounts.REJECTED) statusLabels.push(`${statusCounts.REJECTED} từ chối`);
    
    return statusLabels.join(', ') || 'Không xác định';
};

const getGroupedStatusSeverity = (groupedReturn) => {
    const status = groupedReturn.trangThaiHoaDon;
    switch (status) {
        case 'APPROVED': return 'success';
        case 'PENDING': return 'warning';
        case 'REJECTED': return 'danger';
        default: return 'info';
    }
};

const getGroupedReasonText = (groupedReturn) => {
    if (!groupedReturn.lyDoList || groupedReturn.lyDoList.length === 0) return 'Không có lý do';
    if (groupedReturn.lyDoList.length === 1) return groupedReturn.lyDoList[0];
    return `${groupedReturn.lyDoList[0]} (+${groupedReturn.lyDoList.length - 1} lý do khác)`;
};

const getGroupedImageCount = (groupedReturn) => {
    return groupedReturn.anhMinhChungList ? groupedReturn.anhMinhChungList.length : 0;
};

const goBack = () => {
    router.back();
};

const viewReturnHistory = async (order) => {
    selectedOrder.value = order;
    isLoadingReturns.value = true;
    showReturnHistoryModal.value = true;

    try {
        await loadExistingReturns(order.id);
    } catch (error) {
        toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể tải lịch sử trả hàng',
            life: 3000
        });
    } finally {
        isLoadingReturns.value = false;
    }
};

const viewReturnHistoryFromDetail = async () => {
    if (selectedOrderDetail.value) {
        closeOrderDetailModal();
        await viewReturnHistory(selectedOrderDetail.value);
    }
};

// ===== CANCEL ORDER (HỦY ĐƠN) =====
const cancelOrder = async (order) => {
    if (!order) return;
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

    let lastError = null;

    // 1) Khách hàng: POST /hoa-don/{id}/cancel (không có body)
    try {
        const resCancel = await fetch(`${API_BASE_URL}/hoa-don/${order.id}/cancel`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        if (resCancel.ok) {
            const text = await resCancel.text();
            let data; try { data = JSON.parse(text); } catch { data = { success: true, data: null }; }

            let updatedOrder = data?.data || null;
            if (!updatedOrder) updatedOrder = { trangThaiHoaDon: 'CANCELLED' };

            const idx = orders.value.findIndex((o) => o.id === order.id);
            if (idx !== -1) orders.value[idx] = { ...orders.value[idx], ...updatedOrder };
            if (selectedOrder.value && selectedOrder.value.id === order.id) {
                selectedOrder.value = { ...selectedOrder.value, ...updatedOrder };
            }
            if (selectedOrderDetail.value && selectedOrderDetail.value.id === order.id) {
                selectedOrderDetail.value = { ...selectedOrderDetail.value, ...updatedOrder };
            }

            toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã hủy đơn hàng thành công', life: 3000 });
            return;
        } else if (resCancel.status === 403) {
            lastError = new Error('Không có quyền truy cập tài nguyên này');
            // Không thử tiếp nếu backend rõ ràng cấm quyền
            console.error('Cancel by customer forbidden (403)');
            // Nhưng vẫn thử admin endpoint như fallback nếu tài khoản là admin đăng nhập ở giao diện KH
        } else {
            const txt = await resCancel.text();
            lastError = new Error(txt || `HTTP ${resCancel.status}`);
        }
    } catch (e) {
        lastError = e;
    }

    // 2) Fallback: PUT /hoa-don/{id}/huy (thường cho admin/nhân viên) với body {lyDo}
    try {
        const resHuy = await fetch(`${API_BASE_URL}/hoa-don/${order.id}/huy`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify({ lyDo: reason })
        });
        const txt = await resHuy.text();
        if (!resHuy.ok) {
            if (resHuy.status === 403) {
                lastError = new Error('Không có quyền truy cập tài nguyên này');
            } else {
                lastError = new Error(txt || `HTTP ${resHuy.status}`);
            }
        } else {
            let data; try { data = JSON.parse(txt); } catch { data = { success: true, data: null }; }
            let updatedOrder = data?.data || null;
            if (!updatedOrder) updatedOrder = { trangThaiHoaDon: 'CANCELLED' };

            const idx = orders.value.findIndex((o) => o.id === order.id);
            if (idx !== -1) orders.value[idx] = { ...orders.value[idx], ...updatedOrder };
            if (selectedOrder.value && selectedOrder.value.id === order.id) {
                selectedOrder.value = { ...selectedOrder.value, ...updatedOrder };
            }
            if (selectedOrderDetail.value && selectedOrderDetail.value.id === order.id) {
                selectedOrderDetail.value = { ...selectedOrderDetail.value, ...updatedOrder };
            }

            toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã hủy đơn hàng thành công', life: 3000 });
            return;
        }
    } catch (e) {
        lastError = e;
    }

    console.error('Error cancelling order:', lastError);
    toast.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: `Không thể hủy đơn hàng: ${lastError?.message || 'Lỗi không xác định'}`,
        life: 4000
    });
};

const closeReturnHistoryModal = () => {
    showReturnHistoryModal.value = false;
    selectedOrder.value = null;
    returnHistory.value = [];
};

// ===== EVIDENCE VIEWER FUNCTIONS =====
const viewEvidenceImage = (imageUrl, returnItem) => {
    selectedEvidenceImage.value = createImageUrl(imageUrl);
    selectedEvidenceProduct.value = returnItem;
    showEvidenceViewer.value = true;
};

const closeEvidenceViewer = () => {
    showEvidenceViewer.value = false;
    selectedEvidenceImage.value = null;
    selectedEvidenceProduct.value = null;
};

// ===== RETURN HISTORY STATISTICS =====
const getTotalRefundAmount = () => {
    return returnHistory.value.reduce((total, item) => total + (item.tienHoan || 0), 0);
};

const getPendingReturnsCount = () => {
    return returnHistory.value.filter(item => item.trangThaiHoaDon === 'PENDING').length;
};

const getApprovedReturnsCount = () => {
    return returnHistory.value.filter(item => item.trangThaiHoaDon === 'APPROVED').length;
};

const getRejectedReturnsCount = () => {
    return returnHistory.value.filter(item => item.trangThaiHoaDon === 'REJECTED').length;
};

// Tổng số lượng sản phẩm trả (từng bản ghi riêng biệt)
const getTotalReturnQuantity = () => {
    return returnHistory.value.reduce((total, item) => total + (item.soLuong || 0), 0);
};

// ===== EXPORT FUNCTION =====
const exportReturnHistory = () => {
    try {
        const headers = ['STT', 'Mã yêu cầu', 'Sản phẩm', 'Số lượng', 'Tiền hoàn', 'Lý do', 'Trạng thái', 'Ngày tạo'];
        const csvData = returnHistory.value.map((item, index) => [
            index + 1,
            item.maChiTietTraHang,
            item.tenSanPham,
            item.soLuong,
            item.tienHoan || 0,
            item.lyDo || 'Không có',
            getReturnStatusLabel(item.trangThaiHoaDon),
            formatDate(item.ngayTaoTraHang)
        ]);

        const csvContent = [
            headers.join(','),
            ...csvData.map(row => row.map(field => `"${field}"`).join(','))
        ].join('\n');

        const BOM = '\uFEFF';
        const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);

        const now = new Date();
        const dateStr = now.toISOString().split('T')[0];
        const filename = `LichSu_TraHang_${selectedOrder.value?.maHoaDon}_${dateStr}.csv`;

        link.setAttribute('href', url);
        link.setAttribute('download', filename);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);

        toast.add({
            severity: 'success',
            summary: 'Xuất báo cáo thành công',
            detail: `Đã xuất lịch sử trả hàng của đơn ${selectedOrder.value?.maHoaDon}`,
            life: 3000
        });
    } catch (error) {
        console.error('Error exporting return history:', error);
        toast.add({
            severity: 'error',
            summary: 'Lỗi xuất báo cáo',
            detail: 'Có lỗi xảy ra khi xuất file',
            life: 3000
        });
    }
};

// ===== DEBUG FUNCTION =====
const debugReturnLogic = (product) => {
    console.group(`🔍 Debug Return Logic: ${product.tenSanPham}`);
    console.log('Product info:', {
        id: product.chiTietSanPhamId,
        purchased: product.soLuong,
        currentReturnQuantity: product.returnQuantity
    });

    const approvedReturned = getReturnedQuantity(product);
    const maxCanReturn = product.soLuong - approvedReturned;

    console.log('Return calculation:', {
        approvedReturned,
        maxCanReturn,
        canReturnAll: maxCanReturn === product.soLuong,
        isValid: product.returnQuantity <= maxCanReturn
    });

    console.groupEnd();
};

// ===== CONSTANTS =====
const statusOptions = [
    { label: 'Tất cả trạng thái', value: null },
    { label: 'Chờ xác nhận', value: 'CHO_XAC_NHAN' },
    { label: 'Đã xác nhận', value: 'DA_XAC_NHAN' },
    { label: 'Đang giao', value: 'DANG_GIAO' },
    { label: 'Đã giao', value: 'DA_GIAO' },
    { label: 'Hoàn thành', value: 'HOAN_THANH' },
    { label: 'Đã hủy', value: 'DA_HUY' }
];

const returnReasons = [
    { label: 'Sản phẩm bị lỗi', value: 'Sản phẩm bị lỗi' },
    { label: 'Không đúng mô tả', value: 'Không đúng mô tả' },
    { label: 'Không vừa size', value: 'Không vừa size' },
    { label: 'Không ưng ý', value: 'Không ưng ý' },
    { label: 'Giao nhầm sản phẩm', value: 'Giao nhầm sản phẩm' },
    { label: 'Sản phẩm bị hư hỏng trong vận chuyển', value: 'Sản phẩm bị hư hỏng trong vận chuyển' },
    { label: 'Thay đổi ý định', value: 'Thay đổi ý định' },
    { label: 'Khác', value: 'Khác' }
];

// ===== LIFECYCLE =====
onMounted(() => {
    loadOrdersSafe();
});
</script>
<template>
    <div class="customer-order-return-page">
        <Toast />

        <!-- Header Section -->
        <div class="page-header">
            <div class="header-content">
                <div>
                    <h1 class="page-title">Đơn hàng của tôi </h1>
                    <p class="page-subtitle">Chọn đơn hàng và sản phẩm bạn muốn xem</p>
                </div>
                <Button @click="goBack" outlined class="back-btn">
                    <i class="pi pi-arrow-left mr-2"></i>
                    Quay lại
                </Button>
            </div>
        </div>

        <!-- Loading State -->
        <div v-if="isLoading" class="loading-container">
            <ProgressSpinner style="width: 50px; height: 50px" />
            <p class="loading-text">Đang tải danh sách đơn hàng...</p>
        </div>

        <!-- Main Content -->
        <div v-else class="main-content">
            <!-- Filter Section -->
            <div class="filter-section">
                <div class="filter-row">
                    <div class="search-box">
                        <span class="p-input-icon-left">
                            <i class="pi pi-search" />
                            <InputText v-model="searchKeyword" placeholder="Tìm kiếm mã đơn hàng, tên khách hàng..." class="search-input" @input="onSearch" />
                        </span>
                    </div>
                    <div class="filter-controls">
                        <Dropdown v-model="selectedStatus" :options="statusOptions" optionLabel="label" optionValue="value" placeholder="Trạng thái" class="status-filter" @change="onFilterChange" />
                        <!-- <Calendar v-model="selectedDate" placeholder="Chọn ngày" dateFormat="dd/mm/yy" class="date-filter" @date-select="onFilterChange" /> -->
                        <Button @click="clearFilters" outlined severity="secondary" size="small">
                            <i class="pi pi-times mr-1"></i>
                            Xóa bộ lọc
                        </Button>
                    </div>
                </div>
            </div>

            <!-- Orders List -->
            <div class="orders-container">
                <!-- Empty State -->
                <div v-if="filteredOrders.length === 0" class="empty-state">
                    <i class="pi pi-inbox empty-icon"></i>
                    <h3 class="empty-title">Không tìm thấy đơn hàng</h3>
                    <p class="empty-text">
                        {{ searchKeyword || selectedStatus || selectedDate ? 'Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm' : 'Bạn chưa có đơn hàng nào có thể trả hàng' }}
                    </p>
                </div>

                <!-- Orders Grid -->
                <div v-else class="orders-grid">
                    <div v-for="order in paginatedOrders" :key="order.id" class="order-card" :class="{ returnable: canReturnOrder(order) }">
                        <!-- Order Header -->
                        <div class="order-header">
                            <div class="order-info">
                                <h3 class="order-code">{{ order.maHoaDon }}</h3>
                                <p class="order-date">{{ formatDate(order.ngayTao) }}</p>
                            </div>
                            <div class="order-status">
                                <Tag :value="getStatusLabel(order.trangThaiHoaDon)" :severity="getStatusSeverity(order.trangThaiHoaDon)" />
                            </div>
                        </div>

                        <!-- Customer Info -->
                        <div v-if="order.tenKhachHang" class="customer-info">
                            <i class="pi pi-user mr-2"></i>
                            <span>{{ order.tenKhachHang }}</span>
                        </div>

                        <!-- Order Summary -->
                        <div class="order-summary">
                            <div class="summary-item">
                                <span class="summary-label">Tổng tiền:</span>
                                <span class="summary-value total-amount">{{ formatCurrency(order.tongTien) }}</span>
                            </div>
                            <div v-if="order.soLuongSanPham" class="summary-item">
                                <span class="summary-label">Số sản phẩm:</span>
                                <span class="summary-value">{{ order.soLuongSanPham }}</span>
                            </div>
                        </div>

                        <!-- Return Status -->
                        <div v-if="!canReturnOrder(order)" class="return-notice warning">
                            <i class="pi pi-info-circle notice-icon"></i>
                            <span class="notice-text">{{ getReturnNotice(order) }}</span>
                        </div>
                        <div v-else class="return-notice success">
                            <i class="pi pi-check-circle notice-icon"></i>
                            <span class="notice-text">Có thể tạo yêu cầu trả hàng</span>
                        </div>

                        <!-- Action Buttons -->
                        <div class="order-actions">
                            <Button @click="viewOrderDetails(order)" outlined size="small" class="view-btn">
                                <i class="pi pi-eye mr-1"></i>
                                Chi tiết
                            </Button>

                            <Button @click="viewReturnHistory(order)" outlined severity="info" size="small" class="history-btn">
                                <i class="pi pi-history mr-1"></i>
                                Lịch sử trả
                            </Button>

                            <Button v-if="canCancelOrder(order)" @click="cancelOrder(order)" outlined severity="danger" size="small" class="cancel-btn">
                                <i class="pi pi-times mr-1"></i>
                                Hủy đơn
                            </Button>

                            <Button v-if="canReturnOrder(order)" @click="selectOrderForReturn(order)" severity="warning" size="small" class="return-btn">
                                <i class="pi pi-undo mr-1"></i>
                                Trả hàng
                            </Button>
                        </div>
                    </div>
                </div>

                <!-- Pagination -->
                <div v-if="filteredOrders.length > itemsPerPage" class="pagination-container">
                    <Paginator
                        :first="first"
                        :rows="itemsPerPage"
                        :totalRecords="filteredOrders.length"
                        :rowsPerPageOptions="[6, 12, 24]"
                        @page="onPageChange"
                        template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
                    />
                </div>
            </div>
        </div>

        <!-- Order Detail Modal -->
        <Dialog v-model:visible="showOrderDetailModal" modal :style="{ width: '95vw', maxWidth: '1000px', height: '90vh' }" :maximizable="true">
            <template #header>
                <div class="modal-header">
                    <i class="pi pi-file-text mr-2 text-primary"></i>
                    <span>Chi tiết đơn hàng - {{ selectedOrderDetail?.maHoaDon }}</span>
                </div>
            </template>

            <div class="order-detail-content">
                <!-- Loading State -->
                <div v-if="isLoadingOrderDetail" class="loading-order-detail">
                    <ProgressSpinner style="width: 40px; height: 40px" />
                    <p class="loading-text">Đang tải chi tiết đơn hàng...</p>
                </div>

                <!-- Order Detail Info -->
                <div v-else-if="selectedOrderDetail" class="order-detail-info">
                    <!-- Order Information -->
                    <div class="order-info-section">
                        <h4 class="section-title">
                            <i class="pi pi-info-circle mr-2"></i>
                            Thông tin đơn hàng
                        </h4>
                        <div class="order-info-grid">
                            <div class="info-card">
                                <span class="info-label">Mã đơn hàng</span>
                                <span class="info-value">{{ selectedOrderDetail.maHoaDon }}</span>
                            </div>
                            <div class="info-card">
                                <span class="info-label">Ngày đặt</span>
                                <span class="info-value">{{ formatDate(selectedOrderDetail.ngayTao) }}</span>
                            </div>
                            <div class="info-card">
                                <span class="info-label">Trạng thái</span>
                                <Tag :value="getStatusLabel(selectedOrderDetail.trangThaiHoaDon)" :severity="getStatusSeverity(selectedOrderDetail.trangThaiHoaDon)" />
                            </div>
                            <div class="info-card">
                                <span class="info-label">Tổng tiền</span>
                                <span class="info-value price-highlight">{{ formatCurrency(selectedOrderDetail.tongTien) }}</span>
                            </div>
                        </div>
                    </div>

                    <!-- Products List -->
                    <div class="products-info-section">
                        <h4 class="section-title">
                            <i class="pi pi-box mr-2"></i>
                            Danh sách sản phẩm ({{ orderDetailProducts.length }})
                            <div v-if="selectedProductsCountDetail > 0" class="selected-count-badge">{{ selectedProductsCountDetail }} sản phẩm đã chọn</div>
                        </h4>

                        <div v-if="orderDetailProducts.length > 0" class="products-detail-list">
                            <div v-for="(product, index) in orderDetailProducts" :key="product.chiTietSanPhamId || product.id" class="product-detail-item" :class="{ 'product-selected': product.returnQuantity > 0 }">
                                <div class="product-index">{{ index + 1 }}</div>

                                <div class="product-image-detail">
                                    <img :src="getProductImage(product)" :alt="product.tenSanPham" @error="handleImageError" />
                                </div>

                                <div class="product-main-info">
                                    <h5 class="product-title">{{ product.tenSanPham }}</h5>
                                    <div class="product-attributes-detail">
                                        <Tag v-if="product.mauSac" :value="product.mauSac" severity="info" size="small" />
                                        <Tag v-if="product.kichThuoc" :value="product.kichThuoc" severity="success" size="small" />
                                    </div>
                                </div>

                                <div class="product-pricing-info">
                                    <div class="pricing-detail-row">
                                        <span class="pricing-detail-label">Đơn giá:</span>
                                        <span class="pricing-detail-value">{{ formatCurrency(product.giaBan) }}</span>
                                    </div>
                                    <div class="pricing-detail-row">
                                        <span class="pricing-detail-label">Số lượng:</span>
                                        <span class="pricing-detail-value quantity-highlight">{{ product.soLuong }}</span>
                                    </div>
                                    <div v-if="getReturnedQuantityDetail(product) > 0" class="pricing-detail-row">
                                        <span class="pricing-detail-label">Đã trả:</span>
                                        <span class="pricing-detail-value returned">{{ getReturnedQuantityDetail(product) }}</span>
                                    </div>
                                    <div class="pricing-detail-row">
                                        <span class="pricing-detail-label">Có thể trả:</span>
                                        <span class="pricing-detail-value available">{{ getMaxReturnQuantityDetail(product) }}</span>
                                    </div>
                                </div>

                                <div class="product-selection-info">
                                    <div v-if="canReturnProductDetail(product)" class="selection-controls-detail">
                                        <label class="control-label-detail">Số lượng trả:</label>
                                        <div class="quantity-input-group-detail">
                                            <Button @click="decreaseQuantityDetail(product)" :disabled="product.returnQuantity <= 0" outlined size="small" severity="secondary" class="quantity-btn-detail">
                                                <i class="pi pi-minus"></i>
                                            </Button>
                                            <InputNumber v-model="product.returnQuantity" :min="0" :max="getMaxReturnQuantityDetail(product)" @input="updateProductSelectionDetail(product)" class="quantity-input-detail" :useGrouping="false" />
                                            <Button @click="increaseQuantityDetail(product)" :disabled="product.returnQuantity >= getMaxReturnQuantityDetail(product)" outlined size="small" severity="secondary" class="quantity-btn-detail">
                                                <i class="pi pi-plus"></i>
                                            </Button>
                                        </div>
                                        <small class="quantity-hint-detail"> Tối đa: {{ getMaxReturnQuantityDetail(product) }} </small>
                                        <div v-if="product.returnQuantity > 0" class="product-subtotal-detail">
                                            <span class="subtotal-label-detail">Tiền hoàn:</span>
                                            <span class="subtotal-value-detail">
                                                {{ formatCurrency(product.giaBan * product.returnQuantity) }}
                                            </span>
                                        </div>
                                    </div>
                                    <div v-else class="status-badge-detail" :class="getProductReturnStatusDetail(product).status">
                                        <i v-if="getProductReturnStatusDetail(product).status === 'pending'" class="pi pi-clock mr-1"></i>
                                        <i v-else-if="getProductReturnStatusDetail(product).status === 'completed'" class="pi pi-times-circle mr-1"></i>
                                        <i v-else class="pi pi-check-circle mr-1"></i>
                                        {{ getProductReturnStatusDetail(product).message }}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Return Summary -->
                    <div v-if="selectedProductsCountDetail > 0" class="return-summary-section">
                        <h4 class="section-title">
                            <i class="pi pi-calculator mr-2"></i>
                            Tổng quan trả hàng
                        </h4>
                        <div class="return-summary-grid">
                            <div class="summary-card highlight">
                                <span class="summary-label">Số sản phẩm trả:</span>
                                <span class="summary-value">{{ selectedProductsCountDetail }}</span>
                            </div>
                            <div class="summary-card highlight">
                                <span class="summary-label">Tổng số lượng:</span>
                                <span class="summary-value">{{ totalReturnQuantityDetail }}</span>
                            </div>
                            <div class="summary-card highlight-main">
                                <span class="summary-label">Tổng tiền hoàn dự kiến:</span>
                                <span class="summary-value">{{ formatCurrency(totalReturnAmountDetail) }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <template #footer>
                <div class="order-detail-footer">
                    <Button @click="closeOrderDetailModal" outlined>
                        <i class="pi pi-times mr-1"></i>
                        Đóng
                    </Button>
                    <div class="detail-actions">
                        <Button @click="viewReturnHistoryFromDetail" severity="info" outlined>
                            <i class="pi pi-history mr-1"></i>
                            Lịch sử trả hàng
                        </Button>

                        <Button v-if="selectedOrderDetail && canReturnOrder(selectedOrderDetail)" @click="proceedToReturnFormFromDetail" severity="warning" :disabled="selectedProductsCountDetail === 0" class="return-action-btn">
                            <i class="pi pi-undo mr-1"></i>
                            {{ selectedProductsCountDetail > 0 ? `Tiếp tục trả hàng (${selectedProductsCountDetail} sản phẩm)` : 'Chọn sản phẩm để trả' }}
                        </Button>

                        <div v-else-if="selectedOrderDetail && !canReturnOrder(selectedOrderDetail)" class="cannot-return-notice">
                            <i class="pi pi-info-circle mr-2"></i>
                            <span>{{ getReturnNotice(selectedOrderDetail) }}</span>
                        </div>
                    </div>
                </div>
            </template>
        </Dialog>

        <!-- Product Selection Modal -->
        <Dialog v-model:visible="showProductModal" modal :style="{ width: '95vw', maxWidth: '1000px', height: '90vh' }" :maximizable="true">
            <template #header>
                <div class="modal-header">
                    <i class="pi pi-shopping-cart mr-2 text-primary"></i>
                    <span>Chọn sản phẩm trả hàng - {{ selectedOrder?.maHoaDon }}</span>
                </div>
            </template>

            <div class="product-selection-content">
                <!-- Loading Products -->
                <div v-if="isLoadingProducts" class="loading-products">
                    <ProgressSpinner style="width: 40px; height: 40px" />
                    <p class="loading-text">Đang tải danh sách sản phẩm...</p>
                </div>

                <!-- Products List -->
                <div v-else-if="orderProducts.length > 0" class="products-list">
                    <h4 class="products-title">Chọn sản phẩm muốn trả ({{ returnableProducts.length }}/{{ orderProducts.length }} có thể trả)</h4>

                    <div class="products-grid">
                        <div
                            v-for="product in orderProducts"
                            :key="product.chiTietSanPhamId || product.id"
                            class="product-item"
                            :class="{
                                returnable: canReturnProduct(product),
                                selected: isProductSelected(product),
                                'not-returnable': !canReturnProduct(product)
                            }"
                        >
                            <div class="product-image">
                                <img :src="getProductImage(product)" :alt="product.tenSanPham" @error="handleImageError" class="product-img" />
                                <div v-if="isProductSelected(product)" class="selection-badge">
                                    <i class="pi pi-check"></i>
                                </div>
                            </div>

                            <div class="product-info">
                                <h5 class="product-name">{{ product.tenSanPham }}</h5>

                                <div class="product-attributes">
                                    <Tag v-if="product.mauSac" :value="product.mauSac" severity="info" size="small" />
                                    <Tag v-if="product.kichThuoc" :value="product.kichThuoc" severity="success" size="small" />
                                </div>

                                <div class="product-pricing">
                                    <div class="price-row main-price">
                                        <span class="price-label">Đơn giá:</span>
                                        <span class="price-value">{{ formatCurrency(product.giaBan) }}</span>
                                    </div>
                                    <div class="price-row">
                                        <span class="price-label">Đã mua:</span>
                                        <span class="quantity-value">{{ product.soLuong }}</span>
                                    </div>
                                    <div class="price-row">
                                        <span class="price-label">Có thể trả:</span>
                                        <span class="available-quantity">{{ getMaxReturnQuantity(product) }}</span>
                                    </div>
                                </div>

                                <div v-if="canReturnProduct(product)" class="selection-controls">
                                    <div class="quantity-control">
                                        <label class="control-label">Số lượng trả:</label>
                                        <div class="quantity-input-group">
                                            <Button @click="decreaseQuantity(product)" :disabled="product.returnQuantity <= 0" outlined size="small" severity="secondary" class="quantity-btn">
                                                <i class="pi pi-minus"></i>
                                            </Button>
                                            <InputNumber v-model="product.returnQuantity" :min="0" :max="getMaxReturnQuantity(product)" @input="updateProductSelection(product)" class="quantity-input" :useGrouping="false" />
                                            <Button @click="increaseQuantity(product)" :disabled="product.returnQuantity >= getMaxReturnQuantity(product)" outlined size="small" severity="secondary" class="quantity-btn">
                                                <i class="pi pi-plus"></i>
                                            </Button>
                                        </div>
                                    </div>

                                    <div v-if="isProductSelected(product)" class="product-subtotal">
                                        <span class="subtotal-label">Tiền hoàn:</span>
                                        <span class="subtotal-value">{{ formatCurrency(product.giaBan * product.returnQuantity) }}</span>
                                    </div>
                                </div>

                                <div v-else class="product-status" :class="getProductReturnStatus(product).status">
                                    <i v-if="getProductReturnStatus(product).status === 'pending'" class="pi pi-clock mr-1"></i>
                                    <i v-else-if="getProductReturnStatus(product).status === 'completed'" class="pi pi-times-circle mr-1"></i>
                                    <i v-else class="pi pi-check-circle mr-1"></i>
                                    {{ getProductReturnStatus(product).message }}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <template #footer>
                <div class="modal-footer">
                    <div class="selection-summary">
                        <div class="summary-stats">
                            <span class="stat-item">
                                <i class="pi pi-shopping-bag mr-1"></i>
                                {{ selectedProductsCount }} sản phẩm
                            </span>
                            <span v-if="totalReturnAmount > 0" class="stat-item total-refund">
                                <i class="pi pi-dollar mr-1"></i>
                                {{ formatCurrency(totalReturnAmount) }}
                            </span>
                        </div>
                    </div>
                    <div class="footer-actions">
                        <Button @click="closeProductModal" outlined>
                            <i class="pi pi-times mr-1"></i>
                            Hủy
                        </Button>
                        <Button @click="proceedToReturnForm" severity="warning" :disabled="selectedProductsCount === 0">
                            <i class="pi pi-arrow-right mr-1"></i>
                            Tiếp tục ({{ selectedProductsCount }})
                        </Button>
                    </div>
                </div>
            </template>
        </Dialog>

        <!-- Return Form Modal -->
        <Dialog v-model:visible="showReturnForm" modal :style="{ width: '900px', maxHeight: '95vh' }" :maximizable="true">
            <template #header>
                <div class="modal-header">
                    <i class="pi pi-undo text-warning mr-2"></i>
                    <span>Tạo yêu cầu trả hàng - {{ selectedOrder?.maHoaDon }}</span>
                </div>
            </template>

            <div class="return-form-content">
                <!-- Selected Products Summary -->
                <div class="selected-products-summary">
                    <h4 class="section-title">
                        <i class="pi pi-shopping-bag mr-2"></i>
                        Sản phẩm trả hàng ({{ currentSelectedProducts.length }})
                    </h4>

                    <div class="products-summary-grid">
                        <div v-for="product in currentSelectedProducts" :key="product.chiTietSanPhamId" class="product-summary-item">
                            <img :src="getProductImage(product)" :alt="product.tenSanPham" class="product-summary-img" @error="handleImageError" />
                            <div class="product-summary-info">
                                <h6 class="product-summary-name">{{ product.tenSanPham }}</h6>
                                <div class="product-summary-attrs">
                                    <Tag v-if="product.mauSac" :value="product.mauSac" severity="info" size="small" />
                                    <Tag v-if="product.kichThuoc" :value="product.kichThuoc" severity="success" size="small" />
                                </div>
                                <div class="product-summary-quantity">
                                    <span class="quantity-label">Số lượng trả:</span>
                                    <span class="quantity-value">{{ product.returnQuantity }}</span>
                                    <span class="price-value">{{ formatCurrency(product.giaBan * product.returnQuantity) }}</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="summary-total">
                        <div class="total-row">
                            <span class="total-label">Tổng tiền hoàn dự kiến:</span>
                            <span class="total-value">{{ formatCurrency(currentTotalReturnAmount) }}</span>
                        </div>
                    </div>
                </div>

                <!-- Return Reason Section -->
                <div class="return-reason-section">
                    <h4 class="section-title required">
                        <i class="pi pi-comment mr-2"></i>
                        Lý do trả hàng
                        <span class="required-mark">*</span>
                    </h4>

                    <div class="reason-selection">
                        <Dropdown v-model="returnReason" :options="returnReasons" optionLabel="label" optionValue="value" placeholder="Chọn lý do trả hàng" class="reason-dropdown" :class="{ 'p-invalid': !returnReason }" />
                    </div>

                    <div v-if="returnReason === 'Khác'" class="custom-reason">
                        <label class="custom-reason-label required">
                            Lý do cụ thể:
                            <span class="required-mark">*</span>
                        </label>
                        <Textarea v-model="customReason" rows="3" placeholder="Vui lòng mô tả chi tiết lý do trả hàng..." class="custom-reason-input" maxlength="500" />
                        <small class="char-counter">{{ customReason.length }}/500 ký tự</small>
                    </div>
                </div>

                <!-- Image Upload Section -->
                <div class="image-upload-section">
                    <h4 class="section-title">
                        <i class="pi pi-camera mr-2"></i>
                        Ảnh minh chứng (tùy chọn)
                        <small class="upload-hint">Tối đa {{ maxImages }} ảnh ({{ currentTotalReturnQuantity }} sản phẩm), mỗi ảnh không quá 5MB</small>
                    </h4>

                    <!-- Main Upload Container -->
                    <div class="main-upload-container">
                        <!-- Upload Area with Preview -->
                        <div class="upload-preview-container" :class="{ 'upload-disabled': uploadedImages.length >= maxImages }">
                            <!-- No Image Selected State -->
                            <div v-if="uploadedImages.length === 0" class="no-image-state">
                                <div class="upload-placeholder">
                                    <i class="pi pi-cloud-upload upload-icon"></i>
                                    <p class="upload-text">Chọn ảnh minh chứng</p>
                                    <p class="upload-subtext">JPG, PNG, GIF, WebP (tối đa 5MB/ảnh)</p>
                                    <p class="upload-subtext">Có thể chọn tối đa {{ maxImages }} ảnh cho {{ currentTotalReturnQuantity }} sản phẩm</p>
                                </div>
                            </div>

                            <!-- Images Preview Grid -->
                            <div v-else class="images-preview-grid">
                                <div v-for="(image, index) in uploadedImages" :key="index" class="preview-card">
                                    <div class="image-container">
                                        <img :src="getImagePreview(image)" :alt="`Ảnh minh chứng ${index + 1}`" class="main-preview-img" />
                                        <button @click="removeImage(index)" class="remove-image-btn" type="button">
                                            <i class="pi pi-times"></i>
                                        </button>
                                    </div>
                                    <div class="image-details">
                                        <span class="image-filename">{{ image.name }}</span>
                                        <span class="image-size">{{ (image.size / 1024 / 1024).toFixed(2) }}MB</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Action Buttons -->
                        <div class="upload-actions">
                            <Button
                                @click="triggerFileSelect"
                                :disabled="uploadedImages.length >= maxImages || isUploadingImages"
                                icon="pi pi-upload"
                                :label="uploadedImages.length === 0 ? 'Chọn ảnh' : `Thêm ảnh (${uploadedImages.length}/${maxImages})`"
                                class="select-file-btn"
                                severity="secondary"
                            />
                            <Button
                                v-if="uploadedImages.length > 0"
                                @click="
                                    uploadedImages.forEach((file, index) => removeImage(index));
                                    uploadedImages = [];
                                "
                                icon="pi pi-trash"
                                label="Xóa tất cả"
                                class="remove-all-btn"
                                severity="danger"
                            />
                        </div>

                        <!-- Hidden File Input -->
                        <input ref="fileInput" type="file" accept="image/*" :multiple="true" @change="handleImageUpload" style="display: none" />
                    </div>

                    <!-- Upload Progress -->
                    <div v-if="isUploadingImages || Object.keys(uploadProgress).length > 0" class="upload-progress">
                        <!-- Current Uploading File -->
                        <div v-if="currentUploadingFile" class="current-upload">
                            <i class="pi pi-cloud-upload mr-2"></i>
                            <span>Đang upload: {{ currentUploadingFile }}</span>
                        </div>

                        <!-- Progress for each file -->
                        <div v-for="(progress, key) in uploadProgress" :key="key" class="file-progress">
                            <div class="file-info">
                                <span class="file-name">{{ progress.fileName }}</span>
                                <span class="file-status" :class="progress.status">
                                    <i v-if="progress.status === 'uploading'" class="pi pi-spin pi-spinner mr-1"></i>
                                    <i v-else-if="progress.status === 'success'" class="pi pi-check mr-1"></i>
                                    <i v-else-if="progress.status === 'error'" class="pi pi-times mr-1"></i>
                                    {{ progress.status === 'uploading' ? 'Đang upload...' : progress.status === 'success' ? 'Thành công' : 'Thất bại' }}
                                </span>
                            </div>

                            <!-- Progress Bar -->
                            <div v-if="progress.status === 'uploading'" class="progress-bar-container">
                                <ProgressBar :value="progress.progress" :showValue="false" style="height: 4px" />
                                <span class="progress-percent">{{ progress.progress }}%</span>
                            </div>

                            <!-- Error Message -->
                            <div v-if="progress.status === 'error' && progress.error" class="error-message">
                                <i class="pi pi-exclamation-triangle mr-1"></i>
                                {{ progress.error }}
                            </div>
                        </div>
                    </div>

                    <!-- Image Count Info -->
                    <div v-if="currentTotalReturnQuantity > 0" class="image-count-info">
                        <div class="info-row">
                            <span class="info-label">Số lượng sản phẩm trả:</span>
                            <span class="info-value">{{ currentTotalReturnQuantity }}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Ảnh minh chứng:</span>
                            <span class="info-value" :class="{ 'text-success': uploadedImages.length > 0, 'text-muted': uploadedImages.length === 0 }"> {{ uploadedImages.length }}/{{ maxImages }} </span>
                        </div>
                        <small class="info-hint">Bạn có thể thêm tối đa {{ maxImages }} ảnh minh chứng</small>
                    </div>
                </div>

                <!-- Additional Information -->
                <div class="additional-info-section">
                    <div class="info-item">
                        <label class="info-label">Ghi chú thêm (tùy chọn):</label>
                        <Textarea v-model="additionalNote" rows="3" placeholder="Thêm ghi chú nếu cần..." class="additional-note-input" maxlength="300" />
                        <small class="char-counter">{{ additionalNote.length }}/300 ký tự</small>
                    </div>
                </div>

                <!-- Return Policy Notice -->
                <div class="return-policy-notice">
                    <div class="notice-header">
                        <i class="pi pi-info-circle notice-icon"></i>
                        <span class="notice-title">Lưu ý quan trọng</span>
                    </div>
                    <ul class="notice-list">
                        <li>Yêu cầu trả hàng sẽ được xem xét trong vòng 1-2 ngày làm việc</li>
                        <li>Sản phẩm cần giữ nguyên tình trạng ban đầu khi trả</li>
                        <li>Ảnh minh chứng giúp xử lý yêu cầu nhanh chóng hơn</li>
                        <li>Bạn sẽ nhận được thông báo qua email khi yêu cầu được duyệt</li>
                    </ul>
                </div>
            </div>

            <template #footer>
                <div class="form-footer">
                    <Button @click="goBackToProductSelection" outlined>
                        <i class="pi pi-arrow-left mr-1"></i>
                        Quay lại
                    </Button>
                    <div class="submit-actions">
                        <Button @click="closeReturnForm" outlined severity="secondary">
                            <i class="pi pi-times mr-1"></i>
                            Hủy
                        </Button>
                        <Button @click="confirmSubmitReturn" severity="warning" :loading="isSubmitting" :disabled="!canSubmitReturn" class="submit-btn">
                            <i class="pi pi-send mr-1" v-if="!isSubmitting"></i>
                            {{ isSubmitting ? 'Đang gửi yêu cầu...' : 'Gửi yêu cầu trả hàng' }}
                        </Button>
                    </div>
                </div>
            </template>
        </Dialog>

        <!-- Confirm Submit Modal -->
        <Dialog v-model:visible="showConfirmSubmitModal" modal :style="{ width: '500px' }">
            <template #header>
                <div class="modal-header">
                    <i class="pi pi-exclamation-triangle text-warning mr-2"></i>
                    <span>Xác nhận gửi yêu cầu trả hàng</span>
                </div>
            </template>

            <div class="confirm-content">
                <div class="confirm-summary">
                    <h5 class="confirm-title">Thông tin yêu cầu trả hàng:</h5>

                    <div class="confirm-item">
                        <span class="confirm-label">Số sản phẩm:</span>
                        <span class="confirm-value">{{ currentSelectedProducts.length }} sản phẩm</span>
                    </div>

                    <div class="confirm-item">
                        <span class="confirm-label">Tổng số lượng:</span>
                        <span class="confirm-value">{{ currentTotalReturnQuantity }} cái</span>
                    </div>

                    <div class="confirm-item">
                        <span class="confirm-label">Tổng tiền hoàn dự kiến:</span>
                        <span class="confirm-value highlight">{{ formatCurrency(currentTotalReturnAmount) }}</span>
                    </div>

                    <div class="confirm-item">
                        <span class="confirm-label">Lý do:</span>
                        <span class="confirm-value">{{ returnReason === 'Khác' ? customReason : returnReason }}</span>
                    </div>

                    <div v-if="uploadedImages.length > 0" class="confirm-item">
                        <span class="confirm-label">Ảnh minh chứng:</span>
                        <span class="confirm-value">{{ uploadedImages.length }} ảnh</span>
                    </div>
                </div>

                <div class="confirm-warning">
                    <i class="pi pi-info-circle mr-2"></i>
                    <span>Sau khi gửi, bạn không thể chỉnh sửa yêu cầu này.</span>
                </div>
            </div>

            <template #footer>
                <div class="confirm-footer">
                    <Button @click="showConfirmSubmitModal = false" outlined>
                        <i class="pi pi-times mr-1"></i>
                        Hủy
                    </Button>
                    <Button @click="submitReturnRequestWithConfirm" severity="warning">
                        <i class="pi pi-check mr-1"></i>
                        Xác nhận gửi yêu cầu
                    </Button>
                </div>
            </template>
        </Dialog>

        <!-- Return History Modal -->
        <Dialog v-model:visible="showReturnHistoryModal" modal :style="{ width: '1000px', maxHeight: '90vh' }">
            <template #header>
                <div class="modal-header">
                    <i class="pi pi-history text-info mr-2"></i>
                    <span>Lịch sử trả hàng - {{ selectedOrder?.maHoaDon }}</span>
                    <Badge v-if="returnHistory.length > 0" :value="returnHistory.length.toString()" class="ml-2" />
                </div>
            </template>

            <div class="return-history-content">
                <div v-if="isLoadingReturns" class="loading-returns">
                    <ProgressSpinner style="width: 50px; height: 50px" strokeWidth="6" />
                    <p class="loading-text">Đang tải lịch sử trả hàng...</p>
                </div>

                <div v-else-if="returnHistory.length === 0" class="no-returns">
                    <i class="pi pi-info-circle text-info"></i>
                    <p>Chưa có yêu cầu trả hàng nào cho đơn hàng này</p>
                </div>

                <div v-else class="returns-list">
                    <!-- Summary Stats -->
                    <div class="return-summary mb-4">
                        <div class="summary-card">
                            <div class="summary-item">
                                <span class="summary-label">Tổng yêu cầu trả:</span>
                                <span class="summary-value">{{ returnHistory.length }} yêu cầu</span>
                            </div>
                            <div class="summary-item">
                                <span class="summary-label">Tổng số lượng:</span>
                                <span class="summary-value">{{ getTotalReturnQuantity() }} sản phẩm</span>
                            </div>
                            <div class="summary-item">
                                <span class="summary-label">Tổng tiền hoàn:</span>
                                <span class="summary-value price">{{ formatCurrency(getTotalRefundAmount()) }}</span>
                            </div>
                            <div class="summary-item">
                                <span class="summary-label">Trạng thái:</span>
                                <div class="status-badges">
                                    <Tag v-if="getPendingReturnsCount() > 0" :value="`${getPendingReturnsCount()} chờ xử lý`" severity="warning" />
                                    <Tag v-if="getApprovedReturnsCount() > 0" :value="`${getApprovedReturnsCount()} đã duyệt`" severity="success" />
                                    <Tag v-if="getRejectedReturnsCount() > 0" :value="`${getRejectedReturnsCount()} từ chối`" severity="danger" />
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Individual Return Items -->
                    <div class="individual-returns">
                        <h6 class="section-title">
                            <i class="pi pi-list mr-2"></i>
                            Chi tiết từng yêu cầu trả hàng ({{ returnHistory.length }} yêu cầu)
                        </h6>
                        
                        <div class="return-items-grid">
                            <div v-for="(returnItem, index) in returnHistory" :key="returnItem.id" class="return-item-card enhanced">
                                <!-- Card Header -->
                                <div class="return-item-header">
                                    <div class="return-info">
                                        <div class="return-number">
                                            <i class="pi pi-hashtag"></i>
                                            <span>{{ index + 1 }}</span>
                                        </div>
                                        <div class="return-meta">
                                            <h6 class="return-code">{{ returnItem.maChiTietTraHang }}</h6>
                                            <p class="return-date">{{ formatDate(returnItem.ngayTaoTraHang) }}</p>
                                        </div>
                                    </div>
                                    <div class="return-status">
                                        <Tag :value="getReturnStatusLabel(returnItem.trangThaiHoaDon)" :severity="getReturnStatusSeverity(returnItem.trangThaiHoaDon)" />
                                    </div>
                                </div>

                                <!-- Product Information -->
                                <div class="product-section">
                                    <div class="product-image-container">
                                        <img 
                                            :src="createImageUrl(returnItem.hinhAnh) || getPlaceholderImage({ tenSanPham: returnItem.tenSanPham })" 
                                            :alt="returnItem.tenSanPham" 
                                            class="product-image" 
                                            @error="handleImageError"
                                        />
                                    </div>
                                    <div class="product-details">
                                        <h6 class="product-name">{{ returnItem.tenSanPham }}</h6>
                                        <div class="product-attributes">
                                            <Tag v-if="returnItem.mauSac" :value="returnItem.mauSac" severity="info" size="small" />
                                            <Tag v-if="returnItem.kichThuoc" :value="returnItem.kichThuoc" severity="success" size="small" />
                                        </div>
                                        <div class="product-code">
                                            <small class="text-muted">Mã SP: {{ returnItem.maSanPham || 'N/A' }}</small>
                                        </div>
                                    </div>
                                </div>

                                <!-- Return Details -->
                                <div class="return-details-section">
                                    <div class="detail-row">
                                        <div class="detail-item">
                                            <i class="pi pi-shopping-cart text-primary"></i>
                                            <span class="detail-label">Số lượng trả:</span>
                                            <span class="detail-value">{{ returnItem.soLuong }}</span>
                                        </div>
                                        <div class="detail-item">
                                            <i class="pi pi-money-bill text-success"></i>
                                            <span class="detail-label">Tiền hoàn:</span>
                                            <span class="detail-value price">{{ formatCurrency(returnItem.tienHoan || 0) }}</span>
                                        </div>
                                    </div>
                                </div>

                                <!-- Evidence Images Section -->
                                <div v-if="returnItem.anhMinhChung && returnItem.anhMinhChung.length > 0" class="evidence-section">
                                    <div class="evidence-header">
                                        <i class="pi pi-camera text-info"></i>
                                        <span class="evidence-title">Ảnh minh chứng ({{ returnItem.anhMinhChung.length }})</span>
                                    </div>
                                    <div class="evidence-gallery">
                                        <div 
                                            v-for="(image, imgIndex) in returnItem.anhMinhChung" 
                                            :key="imgIndex" 
                                            class="evidence-item"
                                            @click="viewEvidenceImage(image, returnItem)"
                                        >
                                            <img 
                                                :src="createImageUrl(image)" 
                                                :alt="`Ảnh minh chứng ${imgIndex + 1}`" 
                                                class="evidence-thumbnail"
                                                @error="handleImageError"
                                            />
                                            <div class="evidence-overlay">
                                                <i class="pi pi-eye"></i>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Reason Section -->
                                <div v-if="returnItem.lyDo" class="reason-section">
                                    <div class="reason-header">
                                        <i class="pi pi-comment text-warning"></i>
                                        <span class="reason-title">Lý do trả hàng</span>
                                    </div>
                                    <div class="reason-content">
                                        <p class="reason-text">{{ returnItem.lyDo }}</p>
                                    </div>
                                </div>

                                <!-- Additional Notes -->
                                <div v-if="returnItem.ghiChu" class="notes-section">
                                    <div class="notes-header">
                                        <i class="pi pi-file-edit text-secondary"></i>
                                        <span class="notes-title">Ghi chú</span>
                                    </div>
                                    <div class="notes-content">
                                        <p class="notes-text">{{ returnItem.ghiChu }}</p>
                                    </div>
                                </div>

                                <!-- Processing Timeline -->
                                <div class="timeline-section">
                                    <div class="timeline-item">
                                        <div class="timeline-marker created"></div>
                                        <div class="timeline-content">
                                            <span class="timeline-label">Tạo yêu cầu</span>
                                            <span class="timeline-date">{{ formatDate(returnItem.ngayTaoTraHang) }}</span>
                                        </div>
                                    </div>
                                    <div v-if="returnItem.ngayXuLy" class="timeline-item">
                                        <div class="timeline-marker processed"></div>
                                        <div class="timeline-content">
                                            <span class="timeline-label">Đã xử lý</span>
                                            <span class="timeline-date">{{ formatDate(returnItem.ngayXuLy) }}</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <template #footer>
                <div class="modal-footer-actions">
                    <Button @click="exportReturnHistory" severity="info" outlined>
                        <i class="pi pi-download mr-1"></i>
                        Xuất báo cáo
                    </Button>
                    <Button @click="closeReturnHistoryModal" outlined>
                        <i class="pi pi-times mr-1"></i>
                        Đóng
                    </Button>
                </div>
            </template>
        </Dialog>

        <!-- Evidence Image Viewer Modal -->
        <Dialog v-model:visible="showEvidenceViewer" modal :style="{ width: '800px' }" header="Xem ảnh minh chứng">
            <div v-if="selectedEvidenceImage" class="evidence-viewer">
                <div class="evidence-info mb-3">
                    <h6>{{ selectedEvidenceProduct?.tenSanPham }}</h6>
                    <p class="text-muted">Mã yêu cầu: {{ selectedEvidenceProduct?.maChiTietTraHang }}</p>
                </div>
                <div class="evidence-image-container">
                    <img 
                        :src="selectedEvidenceImage" 
                        alt="Ảnh minh chứng" 
                        class="evidence-full-image"
                        @error="handleImageError"
                    />
                </div>
            </div>
            <template #footer>
                <Button @click="closeEvidenceViewer" outlined>
                    <i class="pi pi-times mr-1"></i>
                    Đóng
                </Button>
            </template>
        </Dialog>
    </div>
</template>
<style scoped>
/* Base Styles */
.customer-order-return-page {
    min-height: 100vh;
    background: #f8fafc;
    font-family:
        'Inter',
        -apple-system,
        BlinkMacSystemFont,
        sans-serif;
}

/* Header */
.page-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 2rem;
    margin-bottom: 2rem;
}

.header-content {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.page-title {
    font-size: 2.5rem;
    font-weight: 700;
    margin-bottom: 0.5rem;
}

.page-subtitle {
    font-size: 1.1rem;
    opacity: 0.9;
    margin: 0;
}

.back-btn {
    color: white;
    border-color: white;
}

.back-btn:hover {
    background-color: rgba(255, 255, 255, 0.1);
    color: white;
    border-color: white;
}

/* Loading States */
.loading-container,
.loading-products,
.loading-returns,
.loading-order-detail {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 4rem 2rem;
    color: #64748b;
}

.loading-text {
    margin-top: 1rem;
    font-size: 1.1rem;
}

/* Main Content */
.main-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1rem;
}

/* Filter Section */
.filter-section {
    background: white;
    border-radius: 12px;
    padding: 1.5rem;
    margin-bottom: 2rem;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-row {
    display: flex;
    gap: 1rem;
    align-items: center;
    flex-wrap: wrap;
}

.search-box {
    flex: 1;
    min-width: 300px;
}

.search-input {
    width: 100%;
}

.filter-controls {
    display: flex;
    gap: 0.5rem;
    align-items: center;
}

.status-filter,
.date-filter {
    min-width: 200px;
}

.date-filter {
    width: 150px;
}

/* Orders Grid */
.orders-container {
    margin-bottom: 2rem;
}

.orders-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
}

.order-card {
    background: white;
    border-radius: 8px;
    padding: 1.5rem;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition:
        transform 0.2s ease,
        box-shadow 0.2s ease;
}

.order-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.order-card.returnable {
    border-left: 4px solid #22c55e;
}

.order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.order-info {
    flex: 1;
}

.order-code {
    font-size: 1.25rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0;
}

.order-date {
    font-size: 0.9rem;
    color: #64748b;
    margin: 0.25rem 0 0;
}

.order-status {
    min-width: 100px;
    text-align: right;
}

.customer-info {
    display: flex;
    align-items: center;
    font-size: 0.95rem;
    color: #475569;
    margin-bottom: 1rem;
}

.order-summary {
    margin-bottom: 1rem;
}

.summary-item {
    display: flex;
    justify-content: space-between;
    font-size: 0.95rem;
    color: #475569;
    margin-bottom: 0.5rem;
}

.summary-label {
    font-weight: 500;
}

.summary-value {
    font-weight: 600;
}

.total-amount {
    color: #dc2626;
}

.return-notice {
    display: flex;
    align-items: center;
    padding: 0.75rem;
    border-radius: 6px;
    font-size: 0.9rem;
    margin-bottom: 1rem;
}

.return-notice.warning {
    background: #fef2f2;
    color: #dc2626;
}

.return-notice.success {
    background: #f0fdf4;
    color: #22c55e;
}

.notice-icon {
    margin-right: 0.5rem;
}

.order-actions {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.view-btn,
.history-btn,
.return-btn {
    flex: 1;
    min-width: 100px;
}

/* Empty State */
.empty-state {
    text-align: center;
    padding: 4rem 2rem;
    color: #64748b;
}

.empty-icon {
    font-size: 3rem;
    margin-bottom: 1rem;
    color: #94a3b8;
}

.empty-title {
    font-size: 1.5rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
}

.empty-text {
    font-size: 1rem;
}

/* Pagination */
.pagination-container {
    margin-top: 2rem;
    display: flex;
    justify-content: center;
}

/* Modal Header */
.modal-header {
    display: flex;
    align-items: center;
    font-size: 1.25rem;
    font-weight: 600;
    color: #1e293b;
}

/* Order Detail Modal */
.order-detail-content {
    padding: 1rem;
    max-height: 70vh;
    overflow-y: auto;
}

.order-info-section,
.products-info-section,
.return-summary-section {
    margin-bottom: 2rem;
}

.section-title {
    font-size: 1.25rem;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 1rem;
    display: flex;
    align-items: center;
    position: relative;
}

.selected-count-badge {
    background: #f59e0b;
    color: white;
    font-size: 0.75rem;
    padding: 0.25rem 0.5rem;
    border-radius: 12px;
    margin-left: auto;
    font-weight: 500;
}

.order-info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
}

.info-card {
    background: #f8fafc;
    padding: 1rem;
    border-radius: 6px;
    display: flex;
    flex-direction: column;
}

.info-label {
    font-size: 0.9rem;
    color: #64748b;
    font-weight: 500;
    margin-bottom: 0.25rem;
}

.info-value {
    font-size: 1rem;
    color: #1e293b;
    font-weight: 600;
}

.price-highlight {
    color: #dc2626;
}

.products-detail-list {
    max-height: 400px;
    overflow-y: auto;
    padding-right: 0.5rem;
}

.product-detail-item {
    display: grid;
    grid-template-columns: 50px 100px 1fr 200px 250px;
    gap: 1rem;
    padding: 1rem;
    border-bottom: 1px solid #e5e7eb;
    align-items: center;
    transition: background-color 0.2s ease;
}

.product-detail-item.product-selected {
    background: #f0fdf4;
    border-left: 3px solid #22c55e;
}

.product-index {
    font-weight: 600;
    color: #1e293b;
    text-align: center;
}

.product-image-detail {
    width: 100px;
    height: 100px;
    border-radius: 6px;
    overflow: hidden;
}

.product-image-detail img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.product-main-info {
    flex: 1;
}

.product-title {
    font-size: 1.1rem;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 0.5rem;
}

.product-attributes-detail {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.product-pricing-info {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.pricing-detail-row {
    display: flex;
    justify-content: space-between;
    font-size: 0.9rem;
}

.pricing-detail-label {
    color: #64748b;
}

.pricing-detail-value {
    font-weight: 600;
    color: #1e293b;
}

.quantity-highlight {
    color: #3b82f6;
}

.returned {
    color: #ef4444;
}

.available {
    color: #22c55e;
}

.product-selection-info {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
}

.selection-controls-detail {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    width: 100%;
}

.control-label-detail {
    font-size: 0.9rem;
    font-weight: 500;
    color: #1e293b;
}

.quantity-input-group-detail {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    justify-content: center;
}

.quantity-btn-detail {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.quantity-input-detail {
    width: 70px;
    text-align: center;
}

.quantity-input-detail .p-inputnumber-input {
    text-align: center;
}

.quantity-hint-detail {
    font-size: 0.8rem;
    color: #64748b;
}

.product-subtotal-detail {
    display: flex;
    justify-content: space-between;
    font-size: 0.9rem;
    width: 100%;
    margin-top: 0.5rem;
    padding: 0.5rem;
    background: #f0fdf4;
    border-radius: 4px;
}

.subtotal-label-detail {
    color: #64748b;
    font-weight: 500;
}

.subtotal-value-detail {
    font-weight: 600;
    color: #dc2626;
}

.status-badge-detail {
    display: flex;
    align-items: center;
    font-size: 0.9rem;
    padding: 0.5rem;
    border-radius: 4px;
}

.status-badge-detail.not-returnable,
.status-badge-detail.completed {
    background: #fef2f2;
    color: #ef4444;
}

.status-badge-detail.pending {
    background: #fef3c7;
    color: #d97706;
}

.status-badge-detail.available {
    background: #f0fdf4;
    color: #16a34a;
}

.return-summary-section {
    background: #f8fafc;
    padding: 1.5rem;
    border-radius: 8px;
    border: 1px solid #e5e7eb;
}

.return-summary-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
}

.summary-card {
    background: white;
    padding: 1rem;
    border-radius: 6px;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
}

.summary-card.highlight {
    border: 2px solid #3b82f6;
}

.summary-card.highlight-main {
    border: 2px solid #f59e0b;
    background: #fffbeb;
}

.summary-label {
    font-size: 0.9rem;
    color: #64748b;
    margin-bottom: 0.5rem;
}

.summary-value {
    font-size: 1.25rem;
    font-weight: 600;
    color: #1e293b;
}

.order-detail-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.detail-actions {
    display: flex;
    gap: 0.5rem;
    align-items: center;
}

.return-action-btn {
    background: linear-gradient(135deg, #f59e0b, #d97706) !important;
    border: none !important;
    font-weight: 600 !important;
    padding: 0.75rem 1.5rem !important;
    border-radius: 8px !important;
    transition: all 0.3s ease !important;
}

.return-action-btn:hover:not(:disabled) {
    background: linear-gradient(135deg, #d97706, #b45309) !important;
    transform: translateY(-1px) !important;
    box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4) !important;
}

.return-action-btn:disabled {
    background: #9ca3af !important;
    cursor: not-allowed !important;
}

.cannot-return-notice {
    display: flex;
    align-items: center;
    padding: 0.75rem 1rem;
    background: #fef2f2;
    color: #dc2626;
    border-radius: 6px;
    font-size: 0.9rem;
    border: 1px solid #fecaca;
}

/* Product Selection Modal */
.product-selection-content {
    padding: 1rem;
    max-height: 70vh;
    overflow-y: auto;
}

.products-title {
    font-size: 1.25rem;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 1rem;
}

.products-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 1rem;
}

.product-item {
    background: white;
    border-radius: 8px;
    padding: 1rem;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: all 0.2s ease;
    border: 2px solid transparent;
}

.product-item.returnable {
    border-color: #22c55e;
}

.product-item.selected {
    background: #f0fdf4;
    border-color: #22c55e;
}

.product-item.not-returnable {
    background: #f8fafc;
    opacity: 0.7;
    border-color: #e5e7eb;
}

.product-image {
    position: relative;
    width: 100%;
    height: 150px;
    margin-bottom: 1rem;
    border-radius: 6px;
    overflow: hidden;
}

.product-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.selection-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    background: #22c55e;
    color: white;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.product-info {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.product-name {
    font-size: 1.1rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0;
}

.product-attributes {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.product-pricing {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.price-row {
    display: flex;
    justify-content: space-between;
    font-size: 0.9rem;
}

.price-row.main-price {
    font-weight: 600;
}

.price-label {
    color: #64748b;
}

.price-value {
    color: #1e293b;
    font-weight: 600;
}

.quantity-value {
    color: #3b82f6;
}

.available-quantity {
    color: #22c55e;
}

.product-status {
    display: flex;
    align-items: center;
    font-size: 0.9rem;
    padding: 0.5rem;
    border-radius: 4px;
}

.product-status.disabled,
.product-status.completed {
    background: #fef2f2;
    color: #ef4444;
}

.product-status.pending {
    background: #fef3c7;
    color: #d97706;
}

.product-status.available {
    background: #f0fdf4;
    color: #16a34a;
}

.selection-controls {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.quantity-control {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.control-label {
    font-size: 0.9rem;
    font-weight: 500;
    color: #1e293b;
}

.quantity-input-group {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    justify-content: center;
}

.quantity-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.quantity-input {
    width: 60px;
    text-align: center;
}

.quantity-input .p-inputnumber-input {
    text-align: center;
}

.product-subtotal {
    display: flex;
    justify-content: space-between;
    font-size: 0.9rem;
    padding: 0.5rem;
    background: #f0fdf4;
    border-radius: 4px;
}

.subtotal-label {
    color: #64748b;
}

.subtotal-value {
    font-weight: 600;
    color: #dc2626;
}

.modal-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.selection-summary {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.summary-stats {
    display: flex;
    gap: 1rem;
}

.stat-item {
    font-size: 0.9rem;
    color: #1e293b;
    display: flex;
    align-items: center;
}

.total-refund {
    font-weight: 600;
    color: #dc2626;
}

.footer-actions {
    display: flex;
    gap: 0.5rem;
}

/* ===== SELECTED PRODUCTS SUMMARY ===== */
.selected-products-summary {
    background: #f8fafc;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    padding: 1.5rem;
    margin-bottom: 2rem;
}

.products-summary-grid {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    margin-bottom: 1rem;
}

.product-summary-item {
    display: flex;
    gap: 1rem;
    padding: 1rem;
    background: white;
    border-radius: 6px;
    border: 1px solid #e5e7eb;
    transition: box-shadow 0.2s ease;
}

.product-summary-item:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.product-summary-img {
    width: 60px;
    height: 60px;
    object-fit: cover;
    border-radius: 4px;
    flex-shrink: 0;
    border: 1px solid #e5e7eb;
}

.product-summary-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.product-summary-name {
    font-size: 1rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0;
    line-height: 1.4;
}

.product-summary-attrs {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.product-summary-quantity {
    display: flex;
    gap: 1rem;
    align-items: center;
    font-size: 0.9rem;
}

.quantity-label {
    color: #64748b;
    font-weight: 500;
}

.quantity-value {
    font-weight: 600;
    color: #3b82f6;
    background: #eff6ff;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
}

.price-value {
    font-weight: 600;
    color: #dc2626;
    background: #fef2f2;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
}

.summary-total {
    border-top: 2px solid #e5e7eb;
    padding-top: 1rem;
    background: white;
    border-radius: 6px;
    padding: 1rem;
}

.total-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 1.1rem;
    font-weight: 600;
}

.total-label {
    color: #1e293b;
}

.total-value {
    color: #dc2626;
    font-size: 1.25rem;
    background: #fef2f2;
    padding: 0.5rem 1rem;
    border-radius: 6px;
    border: 1px solid #fecaca;
}

/* ===== IMAGE UPLOAD SECTION ===== */
.image-upload-section {
    background: #f8fafc;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    padding: 1.5rem;
    margin-bottom: 2rem;
}

/* Main Upload Container */
.main-upload-container {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.upload-preview-container {
    border: 2px dashed #d1d5db;
    border-radius: 8px;
    padding: 1.5rem;
    background: #ffffff;
    min-height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
}

.upload-preview-container:hover {
    border-color: #3b82f6;
    background: #f8fafc;
}

.upload-preview-container.upload-disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* No Image State */
.no-image-state {
    text-align: center;
    width: 100%;
}

.upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.75rem;
}

.upload-icon {
    font-size: 3rem;
    color: #9ca3af;
}

.upload-text {
    font-size: 1.125rem;
    font-weight: 500;
    color: #374151;
    margin: 0;
}

.upload-subtext {
    font-size: 0.875rem;
    color: #6b7280;
    margin: 0;
}

/* ===== MULTIPLE IMAGES PREVIEW GRID ===== */
.images-preview-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1rem;
    width: 100%;
    max-height: 400px;
    overflow-y: auto;
    padding: 0.5rem;
}

.preview-card {
    background: #ffffff;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    padding: 1rem;
    box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
    transition: all 0.2s ease;
}

.preview-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    border-color: #3b82f6;
}

.image-container {
    position: relative;
    margin-bottom: 0.75rem;
}

.main-preview-img {
    width: 100%;
    height: 150px;
    object-fit: contain;
    border-radius: 4px;
    background: #f9fafb;
}

.remove-image-btn {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 24px;
    height: 24px;
    background: #ef4444;
    color: white;
    border: none;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 2px 4px rgba(239, 68, 68, 0.3);
}

.remove-image-btn:hover {
    background: #dc2626;
    transform: scale(1.1);
}

.image-details {
    text-align: center;
}

.image-filename {
    font-size: 0.875rem;
    color: #374151;
    font-weight: 500;
    word-break: break-all;
    display: block;
    margin-bottom: 0.25rem;
}

.image-size {
    font-size: 0.75rem;
    color: #9ca3af;
}

/* Action Buttons */
.upload-actions {
    display: flex;
    gap: 0.75rem;
    justify-content: center;
}

.select-file-btn {
    min-width: 150px;
}

.remove-all-btn {
    min-width: 120px;
}

/* Upload Progress Styles */
.upload-progress {
    margin-top: 1rem;
    padding: 1rem;
    background: #ffffff;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
}

.current-upload {
    display: flex;
    align-items: center;
    padding: 0.5rem;
    background: #dbeafe;
    border: 1px solid #3b82f6;
    border-radius: 4px;
    margin-bottom: 1rem;
    font-weight: 500;
    color: #1e40af;
}

.file-progress {
    margin-bottom: 0.75rem;
    padding: 0.75rem;
    background: #f9fafb;
    border: 1px solid #e5e7eb;
    border-radius: 4px;
}

.file-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.5rem;
}

.file-name {
    font-weight: 500;
    color: #374151;
    flex: 1;
    margin-right: 1rem;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.file-status {
    display: flex;
    align-items: center;
    font-size: 0.875rem;
    font-weight: 500;
}

.file-status.uploading {
    color: #3b82f6;
}

.file-status.success {
    color: #059669;
}

.file-status.error {
    color: #dc2626;
}

.progress-bar-container {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.progress-percent {
    font-size: 0.75rem;
    font-weight: 500;
    color: #6b7280;
    min-width: 3rem;
    text-align: right;
}

.error-message {
    display: flex;
    align-items: center;
    margin-top: 0.5rem;
    padding: 0.5rem;
    background: #fef2f2;
    border: 1px solid #fecaca;
    border-radius: 4px;
    color: #dc2626;
    font-size: 0.875rem;
}

.upload-hint {
    font-size: 0.8rem;
    color: #64748b;
    font-weight: 400;
    margin-left: 0.5rem;
    font-style: italic;
}

/* Image Count Info */
.image-count-info {
    margin-top: 1rem;
    padding: 1rem;
    background: #f0f9ff;
    border: 1px solid #bfdbfe;
    border-radius: 6px;
}

.info-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.5rem;
    font-size: 0.9rem;
}

.info-label {
    color: #64748b;
    font-weight: 500;
}

.info-value {
    font-weight: 600;
    color: #1e293b;
}

.info-value.text-success {
    color: #16a34a;
}

.info-value.text-muted {
    color: #9ca3af;
}

.info-hint {
    font-size: 0.8rem;
    color: #64748b;
    font-style: italic;
    margin-top: 0.5rem;
    display: block;
}

/* Return Form */
.return-form-content {
    padding: 1rem;
    max-height: 70vh;
    overflow-y: auto;
}

.return-reason-section,
.additional-info-section {
    margin-bottom: 2rem;
}

.required-mark {
    color: #ef4444;
    font-size: 0.9rem;
    margin-left: 0.25rem;
}

.reason-selection {
    margin-bottom: 1rem;
}

.reason-dropdown {
    width: 100%;
}

.custom-reason {
    margin-top: 1rem;
}

.custom-reason-label {
    font-size: 0.9rem;
    font-weight: 500;
    color: #1e293b;
    display: block;
    margin-bottom: 0.5rem;
}

.custom-reason-input {
    width: 100%;
}

.info-item {
    margin-bottom: 1.5rem;
}

.info-label {
    font-size: 0.9rem;
    font-weight: 500;
    color: #1e293b;
    display: block;
    margin-bottom: 0.5rem;
}

.additional-note-input {
    width: 100%;
}

.form-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.submit-actions {
    display: flex;
    gap: 0.5rem;
}

/* Character Counter */
.char-counter {
    display: block;
    text-align: right;
    font-size: 0.75rem;
    color: #64748b;
    margin-top: 0.25rem;
    font-style: italic;
}

.char-counter.warning {
    color: #f59e0b;
}

.char-counter.danger {
    color: #ef4444;
}

/* Return Policy Notice */
.return-policy-notice {
    background: #fffbeb;
    border: 1px solid #fed7aa;
    border-radius: 8px;
    padding: 1.5rem;
    margin-top: 2rem;
    position: relative;
}

.return-policy-notice::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: #f59e0b;
    border-radius: 8px 0 0 8px;
}

.notice-header {
    display: flex;
    align-items: center;
    margin-bottom: 1rem;
}

.notice-icon {
    color: #f59e0b;
    font-size: 1.2rem;
    margin-right: 0.5rem;
}

.notice-title {
    font-size: 1.1rem;
    font-weight: 600;
    color: #92400e;
}

.notice-list {
    margin: 0;
    padding-left: 1.5rem;
    color: #92400e;
}

.notice-list li {
    margin-bottom: 0.5rem;
    font-size: 0.9rem;
    line-height: 1.5;
}

.notice-list li::marker {
    color: #f59e0b;
}

/* Enhanced Submit Button */
.submit-btn {
    background: linear-gradient(135deg, #f59e0b, #d97706) !important;
    border: none !important;
    font-weight: 600 !important;
    padding: 0.75rem 1.5rem !important;
    border-radius: 8px !important;
    transition: all 0.3s ease !important;
    min-width: 200px !important;
    position: relative !important;
    overflow: hidden !important;
}

.submit-btn::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.5s ease;
}

.submit-btn:hover:not(:disabled)::before {
    left: 100%;
}

.submit-btn:hover:not(:disabled) {
    background: linear-gradient(135deg, #d97706, #b45309) !important;
    transform: translateY(-1px) !important;
    box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4) !important;
}

.submit-btn:active:not(:disabled) {
    transform: translateY(0) !important;
}

.submit-btn:disabled {
    background: #9ca3af !important;
    cursor: not-allowed !important;
    transform: none !important;
    box-shadow: none !important;
}

.submit-btn:disabled::before {
    display: none;
}

/* Confirm Modal */
.confirm-content {
    padding: 1rem;
}

.confirm-summary {
    margin-bottom: 1.5rem;
}

.confirm-title {
    font-size: 1.1rem;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 1rem;
    display: flex;
    align-items: center;
}

.confirm-title::before {
    content: '📋';
    margin-right: 0.5rem;
    font-size: 1.2rem;
}

.confirm-item {
    display: flex;
    justify-content: space-between;
    padding: 0.75rem 0;
    border-bottom: 1px solid #f1f5f9;
    font-size: 0.95rem;
}

.confirm-item:last-child {
    border-bottom: none;
}

.confirm-label {
    color: #64748b;
    font-weight: 500;
    flex-shrink: 0;
}

.confirm-value {
    color: #1e293b;
    font-weight: 600;
    text-align: right;
    max-width: 60%;
    word-break: break-word;
}

.confirm-value.highlight {
    color: #dc2626;
    font-size: 1.1rem;
    background: #fef2f2;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
}

.confirm-warning {
    display: flex;
    align-items: center;
    padding: 1rem;
    background: #fef2f2;
    border: 1px solid #fecaca;
    border-radius: 6px;
    color: #dc2626;
    font-size: 0.9rem;
    position: relative;
}

.confirm-warning::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    width: 4px;
    height: 100%;
    background: #ef4444;
    border-radius: 6px 0 0 6px;
}

.confirm-footer {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
}

/* Return History Modal */
.return-history-content {
    padding: 1rem;
    max-height: 60vh;
    overflow-y: auto;
}

.no-returns {
    text-align: center;
    padding: 2rem;
    color: #64748b;
}

.returns-list {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.return-item-card {
    background: #f8fafc;
    padding: 1rem;
    border-radius: 6px;
    border: 1px solid #e5e7eb;
}

.return-item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.return-info {
    flex: 1;
}

.return-code {
    font-size: 1.1rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0 0 0.25rem 0;
}

.return-date {
    font-size: 0.9rem;
    color: #64748b;
    margin: 0;
}

.return-status {
    min-width: 100px;
    text-align: right;
}

.return-item-details {
    display: flex;
    gap: 1rem;
    align-items: center;
}

.product-mini-info {
    display: flex;
    gap: 1rem;
    flex: 1;
}

.product-mini-img {
    width: 60px;
    height: 60px;
    object-fit: cover;
    border-radius: 6px;
    background: #f9fafb;
    border: 1px solid #e5e7eb;
}

.product-mini-details {
    flex: 1;
}

.product-mini-name {
    font-size: 1rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0 0 0.5rem 0;
}

.product-mini-attrs {
    font-size: 0.9rem;
    color: #64748b;
}

.return-quantities {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    min-width: 150px;
}

.quantity-item {
    display: flex;
    justify-content: space-between;
    font-size: 0.9rem;
}

.quantity-label {
    color: #64748b;
}

.quantity-value {
    font-weight: 600;
    color: #1e293b;
}

.quantity-value.price {
    color: #dc2626;
}

/* Fix alignment issues */
.quantity-input-group .p-button,
.quantity-input-group-detail .p-button {
    padding: 0 !important;
    min-width: 32px !important;
    height: 32px !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
}

.quantity-input-group .p-inputnumber,
.quantity-input-group-detail .p-inputnumber {
    width: 100%;
}

.quantity-input-group .p-inputnumber-input,
.quantity-input-group-detail .p-inputnumber-input {
    text-align: center !important;
    padding: 0.5rem !important;
    border-radius: 4px !important;
}

/* Enhanced Return History Styles */
.return-summary {
    background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
    border-radius: 12px;
    padding: 1.5rem;
    border: 1px solid #e2e8f0;
}

.summary-card {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1.5rem;
}

.summary-item {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.summary-label {
    font-size: 0.9rem;
    color: #64748b;
    font-weight: 500;
}

.summary-value {
    font-size: 1.25rem;
    font-weight: 700;
    color: #1e293b;
}

.summary-value.price {
    color: #dc2626;
}

.status-badges {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.section-title {
    font-size: 1.1rem;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 1rem;
    display: flex;
    align-items: center;
}

.return-items-grid {
    display: grid;
    gap: 1.5rem;
}

.return-item-card.enhanced {
    background: white;
    border-radius: 12px;
    padding: 1.5rem;
    border: 1px solid #e2e8f0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.3s ease;
}

.return-item-card.enhanced:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
    border-color: #3b82f6;
}

.return-item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #f1f5f9;
}

.return-info {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.return-number {
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

.return-meta p {
    margin: 0;
    font-size: 0.85rem;
    color: #64748b;
}

.product-section {
    display: flex;
    gap: 1rem;
    margin-bottom: 1rem;
    padding: 1rem;
    background: #f8fafc;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
}

.product-image-container {
    flex-shrink: 0;
}

.product-image {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 8px;
    border: 2px solid #e2e8f0;
    transition: all 0.3s ease;
}

.product-image:hover {
    border-color: #3b82f6;
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

.product-attributes {
    display: flex;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
}

.product-code {
    margin-top: 0.5rem;
}

.return-details-section {
    margin-bottom: 1rem;
}

.detail-row {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
}

.detail-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem;
    background: #f8fafc;
    border-radius: 6px;
    border: 1px solid #e2e8f0;
}

.detail-label {
    font-size: 0.9rem;
    color: #64748b;
}

.detail-value {
    font-weight: 600;
    color: #1e293b;
    margin-left: auto;
}

.detail-value.price {
    color: #dc2626;
}

.evidence-section {
    margin-bottom: 1rem;
    padding: 1rem;
    background: #fefce8;
    border-radius: 8px;
    border: 1px solid #fde047;
}

.evidence-header {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.75rem;
}

.evidence-title {
    font-weight: 600;
    color: #1e293b;
}

.evidence-gallery {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    gap: 0.75rem;
}

.evidence-item {
    position: relative;
    cursor: pointer;
    border-radius: 6px;
    overflow: hidden;
    transition: all 0.3s ease;
}

.evidence-item:hover {
    transform: scale(1.05);
}

.evidence-thumbnail {
    width: 100%;
    height: 80px;
    object-fit: cover;
    border-radius: 6px;
    border: 2px solid #fde047;
    transition: all 0.3s ease;
}

.evidence-overlay {
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
    border-radius: 6px;
}

.evidence-item:hover .evidence-overlay {
    opacity: 1;
}

.evidence-overlay i {
    color: white;
    font-size: 1.25rem;
}

.reason-section, .notes-section {
    margin-bottom: 1rem;
    padding: 1rem;
    background: #fef3c7;
    border-radius: 8px;
    border: 1px solid #fbbf24;
}

.reason-header, .notes-header {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
}

.reason-title, .notes-title {
    font-weight: 600;
    color: #1e293b;
}

.reason-text, .notes-text {
    margin: 0;
    line-height: 1.5;
    color: #374151;
}

.timeline-section {
    display: flex;
    gap: 1rem;
    padding: 1rem;
    background: #f1f5f9;
    border-radius: 8px;
    border: 1px solid #cbd5e1;
}

.timeline-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    flex: 1;
}

.timeline-marker {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    flex-shrink: 0;
}

.timeline-marker.created {
    background: #3b82f6;
}

.timeline-marker.processed {
    background: #10b981;
}

.timeline-content {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
}

.timeline-label {
    font-size: 0.85rem;
    font-weight: 600;
    color: #1e293b;
}

.timeline-date {
    font-size: 0.75rem;
    color: #64748b;
}

.modal-footer-actions {
    display: flex;
    gap: 0.75rem;
    justify-content: flex-end;
}

/* Evidence Viewer Modal */
.evidence-viewer {
    text-align: center;
}

.evidence-info h6 {
    margin: 0 0 0.5rem 0;
    font-size: 1.1rem;
    font-weight: 600;
    color: #1e293b;
}

.evidence-image-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 300px;
    background: #f8fafc;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
}

.evidence-full-image {
    max-width: 100%;
    max-height: 500px;
    object-fit: contain;
    border-radius: 6px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* Responsive Design */
@media (max-width: 768px) {
    .page-title {
        font-size: 2rem;
    }
    
    .summary-card {
        grid-template-columns: 1fr;
    }
    
    .return-items-grid {
        grid-template-columns: 1fr;
    }
    
    .product-section {
        flex-direction: column;
        text-align: center;
    }
    
    .detail-row {
        grid-template-columns: 1fr;
    }
    
    .evidence-gallery {
        grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
    }
    
    .timeline-section {
        flex-direction: column;
    }
    
    .modal-footer-actions {
        flex-direction: column;
    }
    
    .header-content {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
    }

    .filter-row {
        flex-direction: column;
        align-items: stretch;
    }

    .search-box,
    .status-filter,
    .date-filter {
        min-width: 100%;
    }

    .orders-grid {
        grid-template-columns: 1fr;
    }

    .product-detail-item {
        grid-template-columns: 40px 80px 1fr;
        grid-template-areas:
            'index image info'
            'index pricing selection';
    }

    .product-index {
        grid-area: index;
    }

    .product-image-detail {
        grid-area: image;
    }

    .product-main-info {
        grid-area: info;
    }

    .product-pricing-info {
        grid-area: pricing;
    }

    .product-selection-info {
        grid-area: selection;
    }

    .order-info-grid {
        grid-template-columns: 1fr;
    }

    .return-summary-grid {
        grid-template-columns: 1fr;
    }

    .products-grid {
        grid-template-columns: 1fr;
    }

    .images-preview-grid {
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    }

    .selected-products-summary,
    .image-upload-section,
    .return-policy-notice {
        margin-left: -0.5rem;
        margin-right: -0.5rem;
        border-radius: 0;
    }

    .product-summary-item {
        flex-direction: column;
        text-align: center;
        align-items: center;
    }

    .product-summary-img {
        align-self: center;
    }

    .product-summary-quantity {
        justify-content: center;
        flex-wrap: wrap;
        gap: 0.5rem;
    }
}

@media (max-width: 576px) {
    .page-header {
        padding: 1.5rem;
    }

    .order-actions {
        flex-direction: column;
    }

    .view-btn,
    .history-btn,
    .return-btn {
        width: 100%;
    }

    .modal-footer,
    .order-detail-footer,
    .form-footer {
        flex-direction: column;
        gap: 1rem;
    }

    .selection-summary,
    .detail-actions,
    .submit-actions {
        width: 100%;
        justify-content: center;
    }

    .return-item-details {
        flex-direction: column;
        align-items: flex-start;
    }

    .return-quantities {
        width: 100%;
        min-width: unset;
    }

    .images-preview-grid {
        grid-template-columns: repeat(2, 1fr);
    }

    .submit-btn {
        min-width: 100% !important;
    }

    .confirm-footer {
        flex-direction: column;
        gap: 0.5rem;
    }

    .notice-list {
        padding-left: 1rem;
    }

    .product-summary-quantity {
        font-size: 0.8rem;
    }

    .quantity-value,
    .price-value {
        padding: 0.2rem 0.4rem;
        font-size: 0.8rem;
    }

    .confirm-item {
        flex-direction: column;
        gap: 0.5rem;
        text-align: left;
    }

    .confirm-value {
        text-align: left;
        max-width: 100%;
    }

    .total-row {
        flex-direction: column;
        gap: 0.5rem;
        text-align: center;
    }

    .upload-actions {
        flex-direction: column;
        align-items: center;
    }

    .select-file-btn,
    .remove-all-btn {
        width: 100%;
        min-width: unset;
    }
}

/* Loading States */
.upload-button.loading .upload-placeholder {
    opacity: 0.6;
}

.upload-button.loading .upload-icon {
    animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
    0%,
    100% {  
        opacity: 1;
        transform: scale(1);
    }
    50% {
        opacity: 0.7;
        transform: scale(1.05);
    }
}

/* Accessibility */
.upload-button:focus-within {
    outline: 2px solid #3b82f6;
    outline-offset: 2px;
}

.remove-image-btn:focus {
    outline: 2px solid #ef4444;
    outline-offset: 2px;
}

/* Enhanced form elements */
.custom-reason-input,
.additional-note-input {
    transition: all 0.2s ease;
}

.custom-reason-input:focus,
.additional-note-input:focus {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

.reason-dropdown.p-invalid {
    animation: shake 0.5s ease-in-out;
}

@keyframes shake {
    0%,
    100% {
        transform: translateX(0);
    }
    25% {
        transform: translateX(-5px);
    }
    75% {
        transform: translateX(5px);
    }
}
</style>
