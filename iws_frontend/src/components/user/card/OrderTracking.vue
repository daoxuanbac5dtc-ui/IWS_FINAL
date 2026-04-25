<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-2xl mx-auto px-4">
      <div class="bg-white rounded-lg shadow-lg overflow-hidden">
        <!-- Header -->
        <div class="bg-gradient-to-r from-green-600 to-blue-600 text-white p-6">
          <h1 class="text-2xl font-bold">Tra cứu đơn hàng</h1>
          <p class="text-green-100 mt-2">Nhập thông tin để xem chi tiết đơn hàng</p>
        </div>

        <div class="p-6">
          <!-- Search Form -->
          <div v-if="!orderInfo" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Email đặt hàng
              </label>
              <input
                v-model="searchForm.email"
                type="email"
                required
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                placeholder="email@example.com"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Mã đơn hàng
              </label>
              <input
                v-model="searchForm.orderCode"
                type="text"
                required
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                placeholder="HD123456789"
              />
            </div>

            <button
              @click="trackOrder"
              :disabled="isLoading"
              class="w-full py-3 px-6 bg-gradient-to-r from-green-600 to-blue-600 text-white rounded-lg hover:from-green-700 hover:to-blue-700 font-medium transition-all disabled:opacity-50"
            >
              <span v-if="isLoading" class="flex items-center justify-center">
                <i class="pi pi-spin pi-spinner mr-2"></i>
                Đang tìm kiếm...
              </span>
              <span v-else>Tra cứu đơn hàng</span>
            </button>
          </div>

          <!-- Order Information -->
          <div v-if="orderInfo" class="space-y-6 fade-in">
            <!-- Order Header -->
            <div class="flex justify-between items-start">
              <div>
                <h2 class="text-xl font-bold text-gray-800">Đơn hàng #{{ orderInfo.orderCode }}</h2>
                <p class="text-sm text-gray-600">Ngày đặt: {{ formatDate(orderInfo.createdDate) }}</p>
              </div>
              <div class="text-right">
                <span :class="getStatusClass(orderInfo.status)" class="px-3 py-1 rounded-full text-sm font-medium">
                  {{ getStatusText(orderInfo.status) }}
                </span>
              </div>
            </div>

            <!-- Cancel Note Display (if order was cancelled) -->
            <div v-if="orderInfo.status === 'DA_HUY' && orderInfo.notes" class="bg-red-50 border-l-4 border-red-400 p-4">
              <div class="flex">
                <div class="flex-shrink-0">
                  <i class="pi pi-times-circle text-red-400 text-xl"></i>
                </div>
                <div class="ml-3">
                  <h3 class="text-sm font-medium text-red-800">
                    Đơn hàng đã bị hủy
                  </h3>
                  <div class="mt-2 text-sm text-red-700">
                    <p><strong>Lý do hủy:</strong> {{ orderInfo.notes }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Cancel Order Button -->
            <div v-if="canCancelOrder" class="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
              <div class="flex items-center justify-between">
                <div>
                  <h3 class="text-sm font-medium text-yellow-800">Có thể hủy đơn hàng</h3>
                  <p class="text-sm text-yellow-700">Đơn hàng chưa được xác nhận, bạn có thể hủy đơn hàng này.</p>
                </div>
                <button
                  @click="showCancelModal = true"
                  class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
                >
                  Hủy đơn hàng
                </button>
              </div>
            </div>

            <!-- Order Status Timeline -->
            <div class="bg-gray-50 rounded-lg p-4">
              <h3 class="font-medium text-gray-800 mb-4">Trạng thái đơn hàng</h3>
              <div class="space-y-3">
                <div v-for="status in orderStatuses" :key="status.key" class="flex items-center">
                  <div :class="[
                    'w-4 h-4 rounded-full mr-3',
                    isStatusCompleted(status.key) ? 'bg-green-500' : 'bg-gray-300'
                  ]"></div>
                  <div class="flex-1">
                    <p :class="[
                      'text-sm font-medium',
                      isStatusCompleted(status.key) ? 'text-green-600' : 'text-gray-500'
                    ]">
                      {{ status.label }}
                    </p>
                    <p v-if="status.date && isStatusCompleted(status.key)" class="text-xs text-gray-500">
                      {{ formatDate(status.date) }}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Customer Information -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="bg-gray-50 rounded-lg p-4">
                <h3 class="font-medium text-gray-800 mb-3">Thông tin người nhận</h3>
                <div class="space-y-2 text-sm">
                  <p><span class="font-medium">Tên:</span> {{ orderInfo.customerName }}</p>
                  <p><span class="font-medium">Email:</span> {{ orderInfo.email }}</p>
                  <p><span class="font-medium">SĐT:</span> {{ orderInfo.phone }}</p>
                  <p><span class="font-medium">Địa chỉ:</span> {{ orderInfo.address }}</p>
                </div>
              </div>

              <div class="bg-gray-50 rounded-lg p-4">
                <h3 class="font-medium text-gray-800 mb-3">Thông tin thanh toán</h3>
                <div class="space-y-2 text-sm">
                  <div class="flex justify-between">
                    <span>Tạm tính:</span>
                    <span>{{ formatCurrency(orderInfo.subtotal) }}đ</span>
                  </div>
                  <div class="flex justify-between">
                    <span>Phí vận chuyển:</span>
                    <span>{{ formatCurrency(orderInfo.shippingFee) }}đ</span>
                  </div>
                  <div class="flex justify-between font-medium text-lg border-t pt-2">
                    <span>Tổng cộng:</span>
                    <span class="text-red-600">{{ formatCurrency(orderInfo.totalAmount) }}đ</span>
                  </div>
                  <p class="text-xs text-gray-500 mt-2">
                    Phương thức: {{ getPaymentMethodText(orderInfo.paymentMethod) }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Order Items -->
            <div class="bg-gray-50 rounded-lg p-4">
              <h3 class="font-medium text-gray-800 mb-4">Sản phẩm đã đặt</h3>
              <div class="space-y-4">
                <div
                  v-for="item in orderInfo.items"
                  :key="item.id"
                  class="flex items-center space-x-4 bg-white rounded-lg p-4"
                >
                  <div class="w-16 h-16 bg-gray-200 rounded-lg flex items-center justify-center overflow-hidden">
                    <img
                      v-if="item.image"
                      :src="item.image"
                      :alt="item.name"
                      class="w-full h-full object-cover"
                    />
                    <div v-else class="text-gray-400 text-xs">No Image</div>
                  </div>

                  <div class="flex-1">
                    <h4 class="font-medium text-gray-800">{{ item.name }}</h4>
                    <p class="text-sm text-gray-600">
                      {{ item.size }} | {{ item.color }} | SL: {{ item.quantity }}
                    </p>
                    <p class="text-sm text-gray-500">Mã: {{ item.code }}</p>
                  </div>

                  <div class="text-right">
                    <p class="font-medium text-gray-800">
                      {{ formatCurrency(item.totalPrice) }}đ
                    </p>
                    <p class="text-sm text-gray-500">
                      {{ formatCurrency(item.unitPrice) }}đ/sp
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex flex-col sm:flex-row gap-4">
              <button
                @click="searchAgain"
                class="flex-1 py-2 px-4 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
              >
                Tra cứu đơn khác
              </button>
              <button
                @click="continueShopping"
                class="flex-1 py-2 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
              >
                Tiếp tục mua sắm
              </button>
            </div>
          </div>

          <!-- Not Found Message -->
          <div v-if="showNotFound" class="text-center py-8 fade-in">
            <i class="pi pi-exclamation-triangle text-6xl text-red-400 mb-4"></i>
            <h3 class="text-lg font-medium text-gray-800 mb-2">Không tìm thấy đơn hàng</h3>
            <p class="text-gray-600 mb-4">
              Vui lòng kiểm tra lại email và mã đơn hàng
            </p>
            <button
              @click="clearSearch"
              class="py-2 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              Thử lại
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Cancel Order Dialog -->
  <Dialog v-model:visible="showCancelModal" modal header="Hủy đơn hàng" :style="{width: '500px'}">
    <div class="space-y-4">
      <div class="flex items-center p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
        <i class="pi pi-exclamation-triangle text-yellow-500 text-2xl mr-3"></i>
        <div>
          <p class="text-sm text-gray-700">
            Bạn có chắc chắn muốn hủy đơn hàng <strong>#{{ orderInfo?.orderCode }}</strong>?
          </p>
          <p class="text-xs text-gray-500 mt-1">Hành động này không thể hoàn tác.</p>
        </div>
      </div>
      
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          Lý do hủy đơn hàng <span class="text-red-500">*</span>
        </label>
        <Dropdown 
          v-model="cancelReason" 
          :options="cancelReasons" 
          option-label="label" 
          option-value="value"
          placeholder="Chọn lý do hủy"
          class="w-full"
        />
      </div>
      
      <div v-if="cancelReason === 'Khác'">
        <label class="block text-sm font-medium text-gray-700 mb-2">
          Lý do chi tiết
        </label>
        <Textarea 
          v-model="customCancelReason"
          placeholder="Vui lòng mô tả lý do hủy đơn hàng..."
          rows="3"
          class="w-full"
        />
      </div>
      
      <div v-if="cancelReason && cancelReason !== 'Khác'">
        <label class="block text-sm font-medium text-gray-700 mb-2">
          Ghi chú thêm (tùy chọn)
        </label>
        <Textarea 
          v-model="additionalNote"
          placeholder="Thêm ghi chú nếu cần..."
          rows="2"
          class="w-full"
        />
      </div>
    </div>
    
    <template #footer>
      <Button 
        label="Hủy bỏ" 
        icon="pi pi-times" 
        @click="closeCancelModal" 
        class="p-button-text" 
        :disabled="isCancelling"
      />
      <Button 
        label="Xác nhận hủy" 
        icon="pi pi-check" 
        @click="confirmCancelOrder"
        class="p-button-danger"
        :loading="isCancelling"
        :disabled="!cancelReason || (cancelReason === 'Khác' && !customCancelReason?.trim())"
      />
    </template>
  </Dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { useToast } from 'primevue/usetoast';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import Textarea from 'primevue/textarea';

const toast = useToast();

// API Configuration
const API_BASE_URL = 'http://localhost:8080';

// Form data
const searchForm = ref({
  email: '',
  orderCode: ''
});

// State
const orderInfo = ref(null);
const isLoading = ref(false);
const showNotFound = ref(false);

// Cancel modal state
const showCancelModal = ref(false);
const cancelReason = ref('');
const customCancelReason = ref('');
const additionalNote = ref('');
const isCancelling = ref(false);

// Cancel reasons
const cancelReasons = ref([
  { label: 'Thay đổi ý định', value: 'Thay đổi ý định' },
  { label: 'Tìm được sản phẩm khác tốt hơn', value: 'Tìm được sản phẩm khác tốt hơn' },
  { label: 'Phí vận chuyển quá cao', value: 'Phí vận chuyển quá cao' },
  { label: 'Thời gian giao hàng quá lâu', value: 'Thời gian giao hàng quá lâu' },
  { label: 'Đặt nhầm sản phẩm', value: 'Đặt nhầm sản phẩm' },
  { label: 'Không còn cần thiết', value: 'Không còn cần thiết' },
  { label: 'Lý do khác', value: 'Khác' }
]);

// Order statuses
const orderStatuses = ref([
  { key: 'CHO_XAC_NHAN', label: 'Chờ xác nhận', date: null },
  { key: 'DA_XAC_NHAN', label: 'Đã xác nhận', date: null },
  { key: 'DANG_CHUAN_BI', label: 'Đang chuẩn bị hàng', date: null },
  { key: 'DANG_GIAO', label: 'Đang giao hàng', date: null },
  { key: 'DA_GIAO', label: 'Đã giao hàng', date: null },
  { key: 'HOAN_THANH', label: 'Hoàn thành', date: null }
]);

// Computed properties
const canCancelOrder = computed(() => {
  if (!orderInfo.value) return false;
  
  const cancellableStatuses = ['CHO_XAC_NHAN'];
  return cancellableStatuses.includes(orderInfo.value.status);
});

// Methods
const formatCurrency = (amount) => {
  if (!amount) return '0';
  return new Intl.NumberFormat('vi-VN').format(amount);
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit'
  });
};

const getStatusClass = (status) => {
  const statusColors = {
    'CHO_XAC_NHAN': 'bg-yellow-100 text-yellow-800',
    'DA_XAC_NHAN': 'bg-blue-100 text-blue-800',
    'DANG_CHUAN_BI': 'bg-purple-100 text-purple-800',
    'DANG_GIAO': 'bg-orange-100 text-orange-800',
    'DA_GIAO': 'bg-green-100 text-green-800',
    'HOAN_THANH': 'bg-green-100 text-green-800',
    'DA_HUY': 'bg-red-100 text-red-800'
  };
  return statusColors[status] || 'bg-gray-100 text-gray-800';
};

const getStatusText = (status) => {
  const statusTexts = {
    'CHO_XAC_NHAN': 'Chờ xác nhận',
    'DA_XAC_NHAN': 'Đã xác nhận',
    'DANG_CHUAN_BI': 'Đang chuẩn bị',
    'DANG_GIAO': 'Đang giao hàng',
    'DA_GIAO': 'Đã giao hàng',
    'HOAN_THANH': 'Hoàn thành',
    'DA_HUY': 'Đã hủy'
  };
  return statusTexts[status] || status;
};

const getPaymentMethodText = (method) => {
  const methods = {
    'COD': 'Thanh toán khi nhận hàng',
    'BANK_TRANSFER': 'Chuyển khoản ngân hàng',
    'MOMO': 'Ví MoMo',
    'VNPAY': 'VNPay'
  };
  return methods[method] || method;
};

const isStatusCompleted = (statusKey) => {
  if (!orderInfo.value) return false;
  
  const statusOrder = ['CHO_XAC_NHAN', 'DA_XAC_NHAN', 'DANG_CHUAN_BI', 'DANG_GIAO', 'DA_GIAO', 'HOAN_THANH'];
  const currentIndex = statusOrder.indexOf(orderInfo.value.status);
  const checkIndex = statusOrder.indexOf(statusKey);
  
  return checkIndex <= currentIndex;
};

// Track order
const trackOrder = async () => {
  if (!searchForm.value.email || !searchForm.value.orderCode) {
    toast.add({
      severity: 'error',
      summary: 'Lỗi',
      detail: 'Vui lòng nhập đầy đủ email và mã đơn hàng',
      life: 3000
    });
    return;
  }
  
  try {
    isLoading.value = true;
    showNotFound.value = false;
    
    console.log('Tracking order:', searchForm.value);
    
    const response = await axios.get(`${API_BASE_URL}/api/orders/track`, {
      params: {
        email: searchForm.value.email,
        orderCode: searchForm.value.orderCode
      }
    });
    
    if (response.data) {
      orderInfo.value = response.data;
      console.log('Order tracking response:', response.data);
    }
    
  } catch (error) {
    console.error('Error tracking order:', error);
    if (error.response?.status === 404) {
      showNotFound.value = true;
    } else {
      toast.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: 'Có lỗi xảy ra khi tra cứu đơn hàng. Vui lòng thử lại!',
        life: 3000
      });
    }
  } finally {
    isLoading.value = false;
  }
};

// Cancel order functions
const closeCancelModal = () => {
  showCancelModal.value = false;
  cancelReason.value = '';
  customCancelReason.value = '';
  additionalNote.value = '';
};

const confirmCancelOrder = async () => {
  if (!cancelReason.value) {
    toast.add({
      severity: 'error',
      summary: 'Lỗi',
      detail: 'Vui lòng chọn lý do hủy đơn hàng',
      life: 3000
    });
    return;
  }
  
  if (cancelReason.value === 'Khác' && !customCancelReason.value?.trim()) {
    toast.add({
      severity: 'error',
      summary: 'Lỗi',
      detail: 'Vui lòng nhập lý do chi tiết',
      life: 3000
    });
    return;
  }
  
  try {
    isCancelling.value = true;
    
    // Prepare cancel reason text
    let finalReason = cancelReason.value;
    if (cancelReason.value === 'Khác') {
      finalReason = customCancelReason.value.trim();
    } else if (additionalNote.value?.trim()) {
      finalReason += ` - ${additionalNote.value.trim()}`;
    }
    
    console.log('Cancelling order:', orderInfo.value.orderId, 'with reason:', finalReason);
    
    const response = await axios.put(
      `${API_BASE_URL}/hoa-don/${orderInfo.value.orderId}/huy`,
      {
        lyDo: finalReason
      }
    );
    
    console.log('Cancel response:', response.data);
    
    if (response.data.success) {
      // Update order info
      orderInfo.value.status = 'DA_HUY';
      orderInfo.value.notes = finalReason;
      
      closeCancelModal();
      
      toast.add({
        severity: 'success',
        summary: 'Thành công',
        detail: 'Đã hủy đơn hàng thành công!',
        life: 3000
      });
    }
    
  } catch (error) {
    console.error('Error cancelling order:', error);
    
    let errorMsg = 'Có lỗi xảy ra khi hủy đơn hàng';
    if (error.response?.data?.message) {
      errorMsg = error.response.data.message;
    } else if (error.response?.status === 400) {
      errorMsg = 'Không thể hủy đơn hàng ở trạng thái này';
    } else if (error.response?.status === 404) {
      errorMsg = 'Không tìm thấy đơn hàng';
    }
    
    toast.add({
      severity: 'error',
      summary: 'Lỗi',
      detail: errorMsg,
      life: 3000
    });
  } finally {
    isCancelling.value = false;
  }
};

// Navigation functions
const searchAgain = () => {
  orderInfo.value = null;
  showNotFound.value = false;
  searchForm.value.email = '';
  searchForm.value.orderCode = '';
};

const clearSearch = () => {
  showNotFound.value = false;
};

const continueShopping = () => {
  // Navigate to products page - adjust according to your routing
  window.location.href = '/products';
};

// Lifecycle
onMounted(() => {
  // Check URL parameters for auto-fill
  const urlParams = new URLSearchParams(window.location.search);
  const email = urlParams.get('email');
  const code = urlParams.get('code');
  
  if (email && code) {
    searchForm.value.email = email;
    searchForm.value.orderCode = code;
    trackOrder();
  }
});
</script>

<style scoped>
.fade-in {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from { 
    opacity: 0; 
    transform: translateY(10px); 
  }
  to { 
    opacity: 1; 
    transform: translateY(0); 
  }
}

/* Custom button styles */
.p-button-danger {
  background-color: #ef4444;
  border-color: #ef4444;
}

.p-button-danger:hover {
  background-color: #dc2626;
  border-color: #dc2626;
}
</style>