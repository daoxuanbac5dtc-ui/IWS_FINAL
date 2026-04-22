<!-- <template>
    <div class="profile-container max-w-7xl mx-auto">
      <!-- Custom Confirmation Modal -->
      <Teleport to="body">
        <Transition name="modal">
          <div v-if="confirmModal.show" class="fixed inset-0 z-50 overflow-y-auto">
            <!-- Backdrop -->
            <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
              <div class="fixed inset-0 transition-opacity" aria-hidden="true" @click="hideConfirmModal">
                <div class="absolute inset-0 bg-gray-900 opacity-75"></div>
              </div>

              <!-- Modal -->
              <div class="inline-block align-bottom bg-white rounded-2xl px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
                <!-- Icon -->
                <div class="mx-auto flex items-center justify-center h-16 w-16 rounded-full mb-4"
                     :class="{
                       'bg-red-100': confirmModal.type === 'danger',
                       'bg-yellow-100': confirmModal.type === 'warning',
                       'bg-blue-100': confirmModal.type === 'info'
                     }">
                  <svg v-if="confirmModal.type === 'danger'" class="h-8 w-8 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                  <svg v-else-if="confirmModal.type === 'warning'" class="h-8 w-8 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.732 15.5c-.77.833.192 2.5 1.732 2.5z" />
                  </svg>
                  <svg v-else class="h-8 w-8 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                </div>

                <!-- Content -->
                <div class="text-center">
                  <h3 class="text-lg leading-6 font-bold text-gray-900 mb-2">
                    {{ confirmModal.title }}
                  </h3>
                  <div class="mt-2">
                    <p class="text-sm text-gray-600" v-html="confirmModal.message"></p>
                  </div>
                </div>

                <!-- Actions -->
                <div class="mt-6 flex flex-col-reverse sm:flex-row sm:justify-center gap-3">
                  <button
                    type="button"
                    class="w-full inline-flex justify-center rounded-xl border border-gray-300 shadow-sm px-4 py-3 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-200 sm:w-auto sm:text-sm transition-all"
                    @click="hideConfirmModal"
                    :disabled="confirmModal.loading"
                  >
                    {{ confirmModal.cancelText }}
                  </button>
                  <button
                    type="button"
                    class="w-full inline-flex justify-center items-center rounded-xl border border-transparent shadow-sm px-4 py-3 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:w-auto sm:text-sm transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    :class="{
                      'bg-red-600 hover:bg-red-700 focus:ring-red-500': confirmModal.type === 'danger',
                      'bg-yellow-600 hover:bg-yellow-700 focus:ring-yellow-500': confirmModal.type === 'warning',
                      'bg-blue-600 hover:bg-blue-700 focus:ring-blue-500': confirmModal.type === 'info'
                    }"
                    @click="handleConfirm"
                    :disabled="confirmModal.loading"
                  >
                    <svg v-if="confirmModal.loading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    {{ confirmModal.loading ? 'Đang xử lý...' : confirmModal.confirmText }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </Transition>
      </Teleport>

      <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <!-- Sidebar -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-lg shadow-sm p-6">
            <!-- User Info -->
            <div class="text-center mb-6">
              <div class="w-20 h-20 bg-orange-500 text-white text-2xl font-bold rounded-full flex items-center justify-center mx-auto mb-3">
                {{ getInitials(userInfo.hoTen) }}
              </div>
              <h5 class="font-semibold text-gray-800">{{ userInfo.hoTen || 'Người dùng' }}</h5>
              <p class="text-sm text-gray-500">Member • {{ userInfo.diemThuong || 0 }} điểm thưởng</p>
            </div>

            <!-- Menu -->
            <nav class="space-y-1">
              <button
                @click="activeTab = 'profile'"
                :class="[
                  'w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200',
                  activeTab === 'profile'
                    ? 'bg-orange-50 text-orange-600 border-l-4 border-orange-500'
                    : 'text-gray-600 hover:bg-gray-50'
                ]"
              >
                <i class="pi pi-user text-lg"></i>
                <span class="font-medium">Thông tin cá nhân</span>
              </button>

              <button
                @click="activeTab = 'orders'"
                :class="[
                  'w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200',
                  activeTab === 'orders'
                    ? 'bg-orange-50 text-orange-600 border-l-4 border-orange-500'
                    : 'text-gray-600 hover:bg-gray-50'
                ]"
              >
                <i class="pi pi-file-text text-lg"></i>
                <span class="font-medium">Hóa đơn</span>
              </button>

              <button
                @click="activeTab = 'addresses'; console.log('Clicked addresses tab, userInfo:', userInfo.value);"
                :class="[
                  'w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200',
                  activeTab === 'addresses'
                    ? 'bg-orange-50 text-orange-600 border-l-4 border-orange-500'
                    : 'text-gray-600 hover:bg-gray-50'
                ]"
              >
                <i class="pi pi-map-marker text-lg"></i>
                <span class="font-medium">Địa chỉ giao hàng</span>
              </button>

              <hr class="my-4">

              <button
                @click="logout"
                class="w-full flex items-center gap-3 px-4 py-3 text-gray-600 hover:bg-gray-50 rounded-lg transition-all duration-200"
              >
                <i class="pi pi-sign-out text-lg"></i>
                <span class="font-medium">Đăng xuất</span>
              </button>
            </nav>
          </div>
        </div>

        <!-- Main Content -->
        <div class="lg:col-span-3">
          <!-- Profile Tab -->
          <div v-if="activeTab === 'profile'" class="bg-white rounded-lg shadow-sm p-6">
            <h4 class="text-xl font-semibold text-gray-800 mb-6">THÔNG TIN CÁ NHÂN</h4>

            <form @submit.prevent="updateProfile" class="space-y-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Họ</label>
                  <input
                    v-model="profileForm.ho"
                    type="text"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition"
                    placeholder="Nhập họ"
                  />
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Tên</label>
                  <input
                    v-model="profileForm.ten"
                    type="text"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition"
                    placeholder="Nhập tên"
                  />
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Giới tính</label>
                  <div class="flex gap-6">
                    <label class="flex items-center cursor-pointer">
                      <input
                        v-model="profileForm.gioiTinh"
                        type="radio"
                        value="Nu"
                        class="mr-2 text-orange-500 focus:ring-orange-500"
                      />
                      <span>Nữ</span>
                    </label>
                    <label class="flex items-center cursor-pointer">
                      <input
                        v-model="profileForm.gioiTinh"
                        type="radio"
                        value="Nam"
                        class="mr-2 text-orange-500 focus:ring-orange-500"
                      />
                      <span>Nam</span>
                    </label>
                  </div>
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Ngày sinh (DD/MM/YYYY)</label>
                  <input
                    v-model="profileForm.ngaySinh"
                    type="date"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition"
                  />
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Số điện thoại</label>
                  <input
                    v-model="profileForm.sdt"
                    type="tel"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition"
                    placeholder="Nhập số điện thoại"
                  />
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
                  <input
                    v-model="profileForm.email"
                    type="email"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50 outline-none"
                    placeholder="Nhập email"
                    readonly
                  />
                </div>
              </div>

              <div class="flex justify-end">
                <button
                  type="submit"
                  class="px-8 py-3 bg-orange-600 text-white font-medium rounded-lg hover:bg-orange-700 transition duration-200"
                >
                  Cập nhật
                </button>
              </div>
            </form>
          </div>

          <!-- Orders Tab (Now Invoices) -->
          <div v-if="activeTab === 'orders'" class="bg-white rounded-lg shadow-sm p-6">
            <h4 class="text-xl font-semibold text-gray-800 mb-6">HÓA ĐƠN CỦA TÔI</h4>

            <div v-if="isLoadingOrders" class="flex justify-center py-12">
              <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500"></div>
            </div>

            <div v-else-if="orders.length === 0" class="text-center py-12">
              <i class="pi pi-file-text text-6xl text-gray-300 mb-4"></i>
              <p class="text-gray-500">Bạn chưa có hóa đơn online nào</p>
            </div>

            <div v-else class="space-y-4">
              <div v-for="order in orders" :key="order.id" class="border border-gray-200 rounded-lg overflow-hidden hover:shadow-md transition-shadow">
                <div class="bg-gray-50 px-6 py-4 flex justify-between items-center">
                  <div>
                    <span class="font-semibold text-gray-800">{{ order.maHoaDon }}</span>
                    <span class="text-sm text-gray-500 ml-3">{{ formatDate(order.ngayTao) }}</span>
                    <span class="inline-flex items-center px-2 py-1 ml-3 rounded-full text-xs font-medium bg-green-100 text-green-800">
                      <i class="pi pi-globe mr-1"></i>
                      Online
                    </span>
                  </div>
                  <span :class="['px-3 py-1 rounded-full text-sm font-medium', getStatusClass(order.trangThaiHoaDon)]">
                    {{ getStatusLabel(order.trangThaiHoaDon) }}
                  </span>
                </div>

                <div class="p-6">
                  <div class="flex justify-between items-start">
                    <div class="space-y-2">
                      <p class="text-sm text-gray-600">
                        <i class="pi pi-map-marker mr-2"></i>
                        {{ order.diaChi }}
                      </p>
                      <p class="text-sm text-gray-600">
                        <i class="pi pi-credit-card mr-2"></i>
                        {{ getPaymentMethod(order.phuongThucThanhToan) }}
                      </p>
                    </div>
                    <div class="text-right">
                      <p class="text-sm text-gray-500">Tổng tiền</p>
                      <p class="text-xl font-semibold text-orange-600">{{ formatCurrency(getTotalAmount(order)) }}</p>
                    </div>
                  </div>

                  <div class="mt-4 pt-4 border-t border-gray-200">
                    <button
                      @click="viewOrderDetail(order.id)"
                      class="text-orange-600 hover:text-orange-700 font-medium text-sm"
                    >
                      Xem chi tiết →
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Addresses Tab -->
          <div v-if="activeTab === 'addresses'" class="bg-white rounded-lg shadow-sm p-6">
            <div class="flex justify-between items-center mb-6">
              <h4 class="text-xl font-semibold text-gray-800">ĐỊA CHỈ GIAO HÀNG</h4>
              <button
                @click="showAddAddressModal = true"
                class="px-4 py-2 bg-orange-600 text-white font-medium rounded-lg hover:bg-orange-700 transition duration-200 flex items-center gap-2"
              >
                <i class="pi pi-plus"></i>
                Thêm địa chỉ mới
              </button>
            </div>

            <div v-if="isLoadingAddresses" class="flex justify-center py-12">
              <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500"></div>
            </div>

            <div v-else-if="addresses.length === 0" class="text-center py-12">
              <i class="pi pi-map-marker text-6xl text-gray-300 mb-4"></i>
              <p class="text-gray-500">Bạn chưa có địa chỉ giao hàng nào</p>
            </div>

            <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div
                v-for="address in addresses"
                :key="address.id"
                class="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
              >
                <div class="flex justify-between items-start mb-3">
                  <div>
                    <h6 class="font-semibold text-gray-800">{{ address.tenNguoiNhan }}</h6>
                    <span v-if="address.isDefault" class="inline-block px-2 py-1 bg-green-100 text-green-800 text-xs rounded-full mt-1">
                      Mặc định
                    </span>
                  </div>
                  <div class="flex gap-2">
                    <button
                      @click="editAddress(address)"
                      class="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition"
                    >
                      <i class="pi pi-pencil"></i>
                    </button>
                    <button
                      v-if="!address.isDefault"
                      @click="deleteAddress(address.id)"
                      class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition"
                    >
                      <i class="pi pi-trash"></i>
                    </button>
                  </div>
                </div>

                <div class="space-y-2 text-sm text-gray-600">
                  <p><i class="pi pi-phone mr-2"></i>{{ address.sdt }}</p>
                  <p><i class="pi pi-map-marker mr-2"></i>{{ formatAddress(address) }}</p>
                </div>

                <div v-if="!address.isDefault" class="mt-3 pt-3 border-t border-gray-200">
                  <button
                    @click="setDefaultAddress(address.id)"
                    class="text-orange-600 hover:text-orange-700 text-sm font-medium"
                  >
                    Đặt làm mặc định
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Add/Edit Address Modal -->
      <Teleport to="body">
        <div v-if="showAddAddressModal" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
          <div class="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div class="p-6 border-b border-gray-200">
              <h5 class="text-lg font-semibold text-gray-800">{{ editingAddress ? 'Sửa' : 'Thêm' }} địa chỉ</h5>
            </div>

            <form @submit.prevent="saveAddress" class="p-6 space-y-4">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Tên người nhận</label>
                  <input
                    v-model="addressForm.tenNguoiNhan"
                    type="text"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition"
                    required
                  />
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Số điện thoại</label>
                  <input
                    v-model="addressForm.sdt"
                    type="tel"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition"
                    required
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Địa chỉ chi tiết</label>
                <input
                  v-model="addressForm.diaChiChiTiet"
                  type="text"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition"
                  required
                />
              </div>

              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Tỉnh/Thành</label>
                  <select
                    v-model="addressForm.maTinh"
                    @change="onProvinceChange"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition"
                    required
                  >
                    <option value="">Chọn tỉnh/thành</option>
                    <option v-for="province in provinces" :key="province.id" :value="province.id">
                      {{ province.name }}
                    </option>
                  </select>
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Quận/Huyện</label>
                  <select
                    v-model="addressForm.maHuyen"
                    @change="onDistrictChange"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition"
                    :disabled="!addressForm.maTinh"
                    required
                  >
                    <option value="">Chọn quận/huyện</option>
                    <option v-for="district in districts" :key="district.id" :value="district.id">
                      {{ district.name }}
                    </option>
                  </select>
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Phường/Xã</label>
                  <select
                    v-model="addressForm.maPhuong"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition"
                    :disabled="!addressForm.maHuyen"
                    required
                  >
                    <option value="">Chọn phường/xã</option>
                    <option v-for="ward in wards" :key="ward.id" :value="ward.id">
                      {{ ward.name }}
                    </option>
                  </select>
                </div>
              </div>

              <div class="flex items-center">
                <input
                  v-model="addressForm.isDefault"
                  type="checkbox"
                  id="setDefault"
                  class="mr-2 text-orange-500 focus:ring-orange-500"
                />
                <label for="setDefault" class="text-sm text-gray-700">
                  Đặt làm địa chỉ mặc định
                </label>
              </div>

              <div class="flex justify-end gap-3 pt-4">
                <button
                  type="button"
                  @click="closeAddressModal"
                  class="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  class="px-6 py-2 bg-orange-600 text-white font-medium rounded-lg hover:bg-orange-700 transition"
                >
                  {{ editingAddress ? 'Cập nhật' : 'Lưu' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </Teleport>
    </div>
  </template>

  <script setup>
  import { ref, onMounted, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import axios from 'axios';
  import { useToast } from 'primevue/usetoast';

  const router = useRouter();
  const toast = useToast();

  // State
  const activeTab = ref('profile');
  const userInfo = ref({});
  const orders = ref([]);
  const addresses = ref([]);
  const isLoadingOrders = ref(false);
  const isLoadingAddresses = ref(false);
  const showAddAddressModal = ref(false);
  const editingAddress = ref(null);

  // Confirmation Modal State
  const confirmModal = ref({
    show: false,
    title: '',
    message: '',
    confirmText: 'Xác nhận',
    cancelText: 'Hủy',
    type: 'danger', // 'danger', 'warning', 'info'
    onConfirm: null,
    loading: false
  });

  // Forms
  const profileForm = ref({
    ho: '',
    ten: '',
    gioiTinh: 'Nu',
    ngaySinh: '',
    sdt: '',
    email: ''
  });

  const addressForm = ref({
    tenNguoiNhan: '',
    sdt: '',
    diaChiChiTiet: '',
    maTinh: '',
    maHuyen: '',
    maPhuong: '',
    tenTinh: '',
    tenHuyen: '',
    tenPhuong: '',
    isDefault: false
  });

  // Location data
  const addressData = ref([]);
  const provinces = ref([]);
  const districts = ref([]);
  const wards = ref([]);

  // API Configuration
  const API_BASE_URL = 'http://localhost:8080';

  // Auth helpers
  const getAuthToken = () => localStorage.getItem('auth_token');

  // Confirmation Modal Functions
  const showConfirmModal = (title, message, onConfirm, options = {}) => {
    confirmModal.value = {
      show: true,
      title,
      message,
      confirmText: options.confirmText || 'Xác nhận',
      cancelText: options.cancelText || 'Hủy',
      type: options.type || 'danger',
      onConfirm,
      loading: false
    };
  };

  const hideConfirmModal = () => {
    confirmModal.value.show = false;
    confirmModal.value.loading = false;
    confirmModal.value.onConfirm = null;
  };

  const handleConfirm = async () => {
    if (confirmModal.value.onConfirm) {
      confirmModal.value.loading = true;
      try {
        await confirmModal.value.onConfirm();
        hideConfirmModal();
      } catch (error) {
        confirmModal.value.loading = false;
        console.error('Error in confirmation action:', error);
      }
    }
  };

  // Load user info
  const loadUserInfo = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/khach-hang/current`, {
        headers: {
          'Authorization': `Bearer ${getAuthToken()}`
        }
      });

      userInfo.value = response.data;

      console.log('User info loaded:', userInfo.value);

      // Split họ tên
      const fullName = response.data.hoTen || '';
      const nameParts = fullName.split(' ');
      profileForm.value = {
        ho: nameParts.slice(0, -1).join(' '),
        ten: nameParts[nameParts.length - 1] || '',
        gioiTinh: response.data.gioiTinh || 'Nu',
        ngaySinh: response.data.ngaySinh ? formatDateForInput(response.data.ngaySinh) : '',
        sdt: response.data.sdt || '',
        email: response.data.taiKhoan?.email || response.data.email || ''
      };
    } catch (error) {
      console.error('Error loading user info:', error);
      toast.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: 'Không thể tải thông tin người dùng',
        life: 3000
      });
    }
  };

  // Load orders - Filter only online orders
  const loadOrders = async () => {
    isLoadingOrders.value = true;
    try {
      const response = await axios.get(`${API_BASE_URL}/hoa-don/my-orders`, {
        headers: {
          'Authorization': `Bearer ${getAuthToken()}`,
          'Content-Type': 'application/json'
        }
      });

      // Lọc chỉ những hóa đơn online
      const allOrders = response.data || [];

      // Debug: Log ra cấu trúc dữ liệu để xem field nào chứa tổng tiền
      if (allOrders.length > 0) {
        console.log('Sample order data:', allOrders[0]);
        console.log('Available amount fields:', {
          tongThanhToan: allOrders[0].tongThanhToan,
          tongTien: allOrders[0].tongTien,
          totalAmount: allOrders[0].totalAmount,
          amount: allOrders[0].amount,
          total: allOrders[0].total,
          thanhTien: allOrders[0].thanhTien,
          soTien: allOrders[0].soTien
        });
      }

      orders.value = allOrders.filter(order => {
        // Có thể là một trong các field này để phân biệt online/offline
        return order.loaiHoaDon === 'ONLINE' ||
               order.phuongThucDatHang === 'ONLINE' ||
               order.isOnline === true ||
               order.type === 'ONLINE' ||
               (order.nguonDonHang && order.nguonDonHang.toUpperCase() === 'ONLINE') ||
               // Nếu không có field cụ thể, có thể dựa vào payment method
               (order.phuongThucThanhToan &&
                order.phuongThucThanhToan !== 'COD' &&
                order.phuongThucThanhToan !== 'CASH');
      });

      console.log(`Loaded ${orders.value.length} online orders from ${allOrders.length} total orders`);

    } catch (error) {
      console.error('Error loading orders:', error);
      if (error.response?.status === 401) {
        toast.add({
          severity: 'error',
          summary: 'Lỗi',
          detail: 'Phiên đăng nhập hết hạn',
          life: 3000
        });
      }
      orders.value = [];
    } finally {
      isLoadingOrders.value = false;
    }
  };

  // Load addresses
  const loadAddresses = async () => {
    isLoadingAddresses.value = true;
    try {
      // Đảm bảo có taiKhoanId - sửa logic lấy ID
      let taiKhoanId = userInfo.value?.idTaiKhoan || userInfo.value?.taiKhoan?.id || userInfo.value?.taiKhoanId;

      console.log('Debug userInfo:', userInfo.value);
      console.log('Debug taiKhoanId found:', taiKhoanId);

      if (!taiKhoanId) {
        console.error('Không tìm thấy ID tài khoản:', userInfo.value);
        // Thử load lại userInfo
        await loadUserInfo();
        taiKhoanId = userInfo.value?.idTaiKhoan || userInfo.value?.taiKhoan?.id || userInfo.value?.taiKhoanId;
        console.log('After reload taiKhoanId:', taiKhoanId);

        if (!taiKhoanId) {
          console.error('Vẫn không tìm thấy ID tài khoản sau khi reload');
          return;
        }
      }

      console.log('Loading addresses for taiKhoanId:', taiKhoanId);
      const url = `${API_BASE_URL}/api/dia-chi/tai-khoan/${taiKhoanId}`;
      console.log('API URL:', url);

      const response = await axios.get(url, {
        headers: {
          'Authorization': `Bearer ${getAuthToken()}`,
          'Content-Type': 'application/json'
        }
      });

      console.log('Addresses response:', response.data);
      console.log('Addresses response type:', typeof response.data);
      console.log('Addresses response length:', response.data?.length);

      // Kiểm tra xem response.data có phải là array không
      const addressData = Array.isArray(response.data) ? response.data : [];

      addresses.value = addressData.map(addr => {
        console.log('Processing address:', addr);
        return {
          ...addr,
          tenNguoiNhan: addr.tenNguoiNhan || addr.hoTen || userInfo.value.hoTen,
          sdt: addr.sdt || userInfo.value.sdt,
          isDefault: addr.isDefault || addr.trangThai === 1
        };
      });

      console.log('Final addresses array:', addresses.value);
      console.log('Addresses count:', addresses.value.length);

    } catch (error) {
      console.error('Error loading addresses:', error);
      console.error('Error response:', error.response?.data);
      console.error('Error status:', error.response?.status);
      addresses.value = [];
    } finally {
      isLoadingAddresses.value = false;
    }
  };

  // Load address data
  const loadAddressData = async () => {
    try {
      const response = await fetch("https://raw.githubusercontent.com/kenzouno1/DiaGioiHanhChinhVN/master/data.json");
      const data = await response.json();
      addressData.value = data;
      provinces.value = data.map(p => ({ id: p.Id, name: p.Name }));
    } catch (error) {
      console.error('Error loading address data:', error);
    }
  };

  // Province change handler
  const onProvinceChange = () => {
    addressForm.value.maHuyen = '';
    addressForm.value.maPhuong = '';
    districts.value = [];
    wards.value = [];

    if (addressForm.value.maTinh) {
      const province = addressData.value.find(p => p.Id === addressForm.value.maTinh);
      if (province) {
        addressForm.value.tenTinh = province.Name;
        districts.value = province.Districts.map(d => ({ id: d.Id, name: d.Name }));
      }
    }
  };

  // District change handler
  const onDistrictChange = () => {
    addressForm.value.maPhuong = '';
    wards.value = [];

    if (addressForm.value.maHuyen) {
      const province = addressData.value.find(p => p.Id === addressForm.value.maTinh);
      const district = province?.Districts.find(d => d.Id === addressForm.value.maHuyen);
      if (district) {
        addressForm.value.tenHuyen = district.Name;
        wards.value = district.Wards.map(w => ({ id: w.Id, name: w.Name }));
      }
    }
  };

  // Update profile
  const updateProfile = async () => {
    try {
      const updateData = {
        hoTen: `${profileForm.value.ho} ${profileForm.value.ten}`.trim(),
        gioiTinh: profileForm.value.gioiTinh,
        ngaySinh: profileForm.value.ngaySinh,
        sdt: profileForm.value.sdt
      };

      await axios.put(`${API_BASE_URL}/api/khach-hang/${userInfo.value.id}`, updateData, {
        headers: {
          'Authorization': `Bearer ${getAuthToken()}`,
          'Content-Type': 'application/json'
        }
      });

      toast.add({
        severity: 'success',
        summary: 'Thành công',
        detail: 'Cập nhật thông tin thành công!',
        life: 3000
      });

      await loadUserInfo();
    } catch (error) {
      console.error('Error updating profile:', error);
      toast.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: 'Không thể cập nhật thông tin!',
        life: 3000
      });
    }
  };

  // Save address
  const saveAddress = async () => {
    try {
      const ward = wards.value.find(w => w.id === addressForm.value.maPhuong);
      addressForm.value.tenPhuong = ward?.name || '';

      // Lấy taiKhoanId với fallback
      const taiKhoanId = userInfo.value.taiKhoan?.id || userInfo.value.taiKhoanId;

      if (!taiKhoanId) {
        toast.add({
          severity: 'error',
          summary: 'Lỗi',
          detail: 'Không tìm thấy thông tin tài khoản!',
          life: 3000
        });
        return;
      }

      const addressData = {
        tenNguoiNhan: addressForm.value.tenNguoiNhan,
        sdt: addressForm.value.sdt,
        diaChiChiTiet: addressForm.value.diaChiChiTiet,
        maTinh: addressForm.value.maTinh,
        maHuyen: addressForm.value.maHuyen,
        maPhuong: addressForm.value.maPhuong,
        tenTinh: addressForm.value.tenTinh,
        tenHuyen: addressForm.value.tenHuyen,
        tenPhuong: addressForm.value.tenPhuong,
        trangThai: addressForm.value.isDefault ? 1 : 0,
        idTaiKhoan: taiKhoanId
      };

      if (editingAddress.value) {
        await axios.put(`${API_BASE_URL}/api/dia-chi/${editingAddress.value.id}`, addressData, {
          headers: {
            'Authorization': `Bearer ${getAuthToken()}`,
            'Content-Type': 'application/json'
          }
        });
      } else {
        await axios.post(`${API_BASE_URL}/api/dia-chi`, addressData, {
          headers: {
            'Authorization': `Bearer ${getAuthToken()}`,
            'Content-Type': 'application/json'
          }
        });
      }

      await loadAddresses();
      closeAddressModal();

      toast.add({
        severity: 'success',
        summary: 'Thành công',
        detail: editingAddress.value ? 'Cập nhật địa chỉ thành công!' : 'Thêm địa chỉ thành công!',
        life: 3000
      });
    } catch (error) {
      console.error('Error saving address:', error);
      toast.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: 'Không thể lưu địa chỉ!',
        life: 3000
      });
    }
  };

  // Edit address
  const editAddress = (address) => {
    editingAddress.value = address;
    addressForm.value = {
      tenNguoiNhan: address.tenNguoiNhan || address.hoTen || userInfo.value.hoTen,
      sdt: address.sdt || userInfo.value.sdt,
      diaChiChiTiet: address.diaChiChiTiet,
      maTinh: address.maTinh,
      maHuyen: address.maHuyen,
      maPhuong: address.maPhuong,
      tenTinh: address.tenTinh,
      tenHuyen: address.tenHuyen,
      tenPhuong: address.tenPhuong,
      isDefault: address.isDefault
    };

    // Load districts and wards
    if (address.maTinh) {
      onProvinceChange();
      setTimeout(() => {
        if (address.maHuyen) {
          addressForm.value.maHuyen = address.maHuyen;
          onDistrictChange();
          setTimeout(() => {
            addressForm.value.maPhuong = address.maPhuong;
          }, 100);
        }
      }, 100);
    }

    showAddAddressModal.value = true;
  };

  // Delete address with custom modal
  const deleteAddress = (id) => {
    const address = addresses.value.find(addr => addr.id === id);

    showConfirmModal(
      '🗑️ Xóa địa chỉ',
      `Bạn có chắc chắn muốn xóa địa chỉ của "<strong>${address?.tenNguoiNhan || 'người dùng này'}</strong>" không?<br><br><span class="text-amber-600">⚠️ Hành động này không thể hoàn tác!</span>`,
      async () => {
        try {
          await axios.delete(`${API_BASE_URL}/api/dia-chi/${id}`, {
            headers: {
              'Authorization': `Bearer ${getAuthToken()}`
            }
          });

          await loadAddresses();
          toast.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Xóa địa chỉ thành công!',
            life: 3000
          });
        } catch (error) {
          console.error('Error deleting address:', error);
          toast.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Không thể xóa địa chỉ!',
            life: 3000
          });
        }
      },
      {
        confirmText: 'Xóa địa chỉ',
        cancelText: 'Hủy bỏ',
        type: 'danger'
      }
    );
  };

  // Set default address
  const setDefaultAddress = async (id) => {
    try {
      await axios.patch(`${API_BASE_URL}/api/dia-chi/${id}/set-default`, {}, {
        headers: {
          'Authorization': `Bearer ${getAuthToken()}`
        }
      });

      await loadAddresses();
      toast.add({
        severity: 'success',
        summary: 'Thành công',
        detail: 'Đã đặt làm địa chỉ mặc định!',
        life: 3000
      });
    } catch (error) {
      console.error('Error setting default address:', error);
      toast.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: 'Không thể cập nhật địa chỉ mặc định!',
        life: 3000
      });
    }
  };

  // Close address modal
  const closeAddressModal = () => {
    showAddAddressModal.value = false;
    editingAddress.value = null;
    addressForm.value = {
      tenNguoiNhan: '',
      sdt: '',
      diaChiChiTiet: '',
      maTinh: '',
      maHuyen: '',
      maPhuong: '',
      tenTinh: '',
      tenHuyen: '',
      tenPhuong: '',
      isDefault: false
    };
    districts.value = [];
    wards.value = [];
  };

  // View order detail
  const viewOrderDetail = (orderId) => {
    router.push(`/order-detail/${orderId}`);
  };

  // Logout with custom modal
  const logout = () => {
    showConfirmModal(
      '👋 Đăng xuất',
      'Bạn có chắc chắn muốn đăng xuất khỏi tài khoản không?<br><br>Bạn sẽ cần đăng nhập lại để tiếp tục sử dụng.',
      async () => {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user_info');
        router.push('/auth/login');
      },
      {
        confirmText: 'Đăng xuất',
        cancelText: 'Ở lại',
        type: 'info'
      }
    );
  };

  // Utility functions
  const getInitials = (name) => {
    if (!name) return 'U';
    const words = name.split(' ');
    return words.map(w => w[0]).join('').toUpperCase().slice(0, 2);
  };

  const formatDate = (date) => {
    if (!date) return '';
    
    try {
      let parsedDate;
      
      if (typeof date === 'string') {
        // Xử lý format dd/mm/yyyy hh:mm:ss
        if (date.includes('/') && date.includes(' ')) {
          const [datePart, timePart] = date.split(' ');
          const [day, month, year] = datePart.split('/');
          const [hour, minute, second] = timePart.split(':');
          
          // Tạo Date object với format yyyy-mm-ddThh:mm:ss và cộng thêm 7 giờ
          const dateString = `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}T${hour.padStart(2, '0')}:${minute.padStart(2, '0')}:${(second || '00').padStart(2, '0')}`;
          parsedDate = new Date(dateString);
          
          // Cộng thêm 7 giờ (7 * 60 * 60 * 1000 milliseconds)
          parsedDate.setTime(parsedDate.getTime() + (7 * 60 * 60 * 1000));
        } else {
          // Thử parse trực tiếp và cộng thêm 7 giờ
          parsedDate = new Date(date);
          parsedDate.setTime(parsedDate.getTime() + (7 * 60 * 60 * 1000));
        }
      } else {
        parsedDate = new Date(date);
        parsedDate.setTime(parsedDate.getTime() + (7 * 60 * 60 * 1000));
      }
      
      if (isNaN(parsedDate.getTime())) {
        return '';
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
      return '';
    }
  };

  const formatDateForInput = (date) => {
    if (!date) return '';
    const d = new Date(date);
    return d.toISOString().split('T')[0];
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount || 0);
  };

  // Hàm lấy tổng tiền từ nhiều field có thể có
  const getTotalAmount = (order) => {
    return order.tongThanhToan ||
           order.tongTien ||
           order.totalAmount ||
           order.amount ||
           order.total ||
           order.thanhTien ||
           order.soTien ||
           0;
  };

  const formatAddress = (address) => {
    return [
      address.diaChiChiTiet,
      address.tenPhuong,
      address.tenHuyen,
      address.tenTinh
    ].filter(Boolean).join(', ');
  };

  const getStatusLabel = (status) => {
    const statusMap = {
      'CHO_XAC_NHAN': 'Chờ xác nhận',
      'DA_XAC_NHAN': 'Đã xác nhận',
      'DANG_GIAO': 'Đang giao',
      'DA_GIAO': 'Đã giao',
      'HOAN_THANH': 'Hoàn thành',
      'DA_HUY': 'Đã hủy',
      'PENDING': 'Chờ xác nhận',
      'CONFIRMED': 'Đã xác nhận',
      'SHIPPING': 'Đang giao',
      'DELIVERED': 'Đã giao',
      'COMPLETED': 'Hoàn thành',
      'CANCELLED': 'Đã hủy'
    };
    return statusMap[status] || status;
  };

  const getStatusClass = (status) => {
    const classMap = {
      'CHO_XAC_NHAN': 'bg-yellow-100 text-yellow-800',
      'DA_XAC_NHAN': 'bg-blue-100 text-blue-800',
      'DANG_GIAO': 'bg-purple-100 text-purple-800',
      'DA_GIAO': 'bg-green-100 text-green-800',
      'HOAN_THANH': 'bg-green-100 text-green-800',
      'DA_HUY': 'bg-red-100 text-red-800',
      'PENDING': 'bg-yellow-100 text-yellow-800',
      'CONFIRMED': 'bg-blue-100 text-blue-800',
      'SHIPPING': 'bg-purple-100 text-purple-800',
      'DELIVERED': 'bg-green-100 text-green-800',
      'COMPLETED': 'bg-green-100 text-green-800',
      'CANCELLED': 'bg-red-100 text-red-800'
    };
    return classMap[status] || 'bg-gray-100 text-gray-800';
  };

  const getPaymentMethod = (method) => {
    const methodMap = {
      'COD': 'Thanh toán khi nhận hàng',
      'BANK_TRANSFER': 'Chuyển khoản ngân hàng',
      'CASH': 'Tiền mặt',
      'CARD': 'Thẻ'
    };
    return methodMap[method] || method;
  };

  // Watch tab changes
  watch(activeTab, async (newTab) => {
    console.log('Tab changed to:', newTab);
    console.log('UserInfo in watch:', userInfo.value);

    if (newTab === 'orders' && orders.value.length === 0 && userInfo.value?.id) {
      await loadOrders();
    } else if (newTab === 'addresses' && addresses.value.length === 0 && userInfo.value?.idTaiKhoan) {
      await loadAddresses();
    }
  });

  // Lifecycle
  onMounted(async () => {
    console.log('Component mounted');
    await loadUserInfo();
    await loadAddressData();

    // Chỉ load data khi đã có userInfo
    console.log('After loadUserInfo, userInfo:', userInfo.value);
    if (userInfo.value?.id) {
      if (activeTab.value === 'orders') {
        await loadOrders();
      } else if (activeTab.value === 'addresses' && userInfo.value?.idTaiKhoan) {
        await loadAddresses();
      }
    }
  });
  </script>

  <style scoped>
  /* Remove default input styles */
  input:focus,
  select:focus,
  textarea:focus {
    outline: none !important;
  }

  /* Modal animations */
  .modal-enter-active, .modal-leave-active {
    transition: opacity 0.3s ease;
  }

  .modal-enter-active .modal-container,
  .modal-leave-active .modal-container {
    transition: all 0.3s ease;
  }

  .modal-enter-from, .modal-leave-to {
    opacity: 0;
  }

  .modal-enter-from .modal-container,
  .modal-leave-to .modal-container {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }

  /* Loading animation */
  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }

  .animate-spin {
    animation: spin 1s linear infinite;
  }

  /* Custom scrollbar for modal */
  .overflow-y-auto::-webkit-scrollbar {
    width: 8px;
  }

  .overflow-y-auto::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 10px;
  }

  .overflow-y-auto::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 10px;
  }

  .overflow-y-auto::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }

  /* Button transitions */
  button {
    transition: all 0.2s ease;
  }

  /* Radio and checkbox custom styles */
  input[type="radio"]:checked,
  input[type="checkbox"]:checked {
    background-color: #ea580c;
    border-color: #ea580c;
  }

  input[type="radio"]:focus,
  input[type="checkbox"]:focus {
    box-shadow: 0 0 0 3px rgba(234, 88, 12, 0.1);
  }

  /* Responsive grid */
  @media (max-width: 1024px) {
    .lg\:grid-cols-4 {
      grid-template-columns: 1fr;
    }

    .lg\:col-span-1,
    .lg\:col-span-3 {
      grid-column: span 1;
    }
  }

  /* Utility classes */
  .max-w-7xl {
    max-width: 80rem;
  }

  .mx-auto {
    margin-left: auto;
    margin-right: auto;
  }

  /* Background colors */
  .bg-orange-50 {
    background-color: #fff7ed;
  }

  .text-orange-600 {
    color: #ea580c;
  }

  .border-orange-500 {
    border-color: #f97316;
  }

  .bg-orange-600 {
    background-color: #ea580c;
  }

  .hover\:bg-orange-700:hover {
    background-color: #c2410c;
  }

  /* Status badge colors */
  .bg-yellow-100 {
    background-color: #fef3c7;
  }

  .text-yellow-800 {
    color: #92400e;
  }

  .bg-blue-100 {
    background-color: #dbeafe;
  }

  .text-blue-800 {
    color: #1e40af;
  }

  .bg-purple-100 {
    background-color: #ede9fe;
  }

  .text-purple-800 {
    color: #5b21b6;
  }

  .bg-green-100 {
    background-color: #d1fae5;
  }

  .text-green-800 {
    color: #065f46;
  }

  .bg-red-100 {
    background-color: #fee2e2;
  }

  .text-red-800 {
    color: #991b1b;
  }

  .bg-gray-100 {
    background-color: #f3f4f6;
  }

  .text-gray-800 {
    color: #1f2937;
  }
  </style> -->
