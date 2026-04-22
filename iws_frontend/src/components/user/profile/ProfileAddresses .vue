<template>
    <div class="bg-white rounded-lg shadow-sm p-6">
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
                  :disabled="isSubmitting"
                  class="px-6 py-2 bg-orange-600 text-white font-medium rounded-lg hover:bg-orange-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <span v-if="isSubmitting" class="flex items-center">
                    <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Đang lưu...
                  </span>
                  <span v-else>{{ editingAddress ? 'Cập nhật' : 'Lưu' }}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </Teleport>
    </div>
  </template>

  <script setup>
  import { ref, onMounted } from 'vue';
  import axios from 'axios';
  import { useToast } from 'primevue/usetoast';

  const toast = useToast();

  // State
  const addresses = ref([]);
  const isLoadingAddresses = ref(false);
  const isSubmitting = ref(false);
  const showAddAddressModal = ref(false);
  const editingAddress = ref(null);
  const userInfo = ref({});

  // Form data
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

  // Load user info
  const loadUserInfo = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/khach-hang/current`, {
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`
      }
    });

    // DEBUG: Log để xem API response
    console.log('🔍 USER INFO RESPONSE:', response.data);
    console.log('🔍 USER INFO KEYS:', Object.keys(response.data || {}));

    // FIX: Lấy data từ đúng structure
    const userData = response.data.data || response.data;
    userInfo.value = userData;

    console.log('👤 FINAL USER DATA:', userData);
    console.log('🆔 Available IDs:', {
      id: userData.id,
      idTaiKhoan: userData.idTaiKhoan,
      taiKhoanId: userData.taiKhoanId,
      taiKhoan_id: userData.taiKhoan?.id
    });

  } catch (error) {
    console.error('Error loading user info:', error);
  }
};

  // Load addresses
  const loadAddresses = async () => {
  isLoadingAddresses.value = true;
  try {
    // FIX: Tìm taiKhoanId từ nhiều nguồn
    let taiKhoanId = null;

    if (userInfo.value?.id) {
      taiKhoanId = userInfo.value.id;
      console.log('📋 Using userInfo.id:', taiKhoanId);
    } else if (userInfo.value?.idTaiKhoan) {
      taiKhoanId = userInfo.value.idTaiKhoan;
      console.log('📋 Using userInfo.idTaiKhoan:', taiKhoanId);
    } else if (userInfo.value?.taiKhoan?.id) {
      taiKhoanId = userInfo.value.taiKhoan.id;
      console.log('📋 Using userInfo.taiKhoan.id:', taiKhoanId);
    } else if (userInfo.value?.taiKhoanId) {
      taiKhoanId = userInfo.value.taiKhoanId;
      console.log('📋 Using userInfo.taiKhoanId:', taiKhoanId);
    }

    // Nếu vẫn không có, thử reload user info
    if (!taiKhoanId) {
      console.log('🔄 No taiKhoanId found, reloading user info...');
      await loadUserInfo();

      // Thử lại sau khi reload
      if (userInfo.value?.id) {
        taiKhoanId = userInfo.value.id;
      } else if (userInfo.value?.idTaiKhoan) {
        taiKhoanId = userInfo.value.idTaiKhoan;
      } else if (userInfo.value?.taiKhoan?.id) {
        taiKhoanId = userInfo.value.taiKhoan.id;
      }
    }

    if (!taiKhoanId) {
      console.error('❌ Vẫn không tìm thấy taiKhoanId sau khi reload');
      console.log('❌ UserInfo structure:', userInfo.value);
      addresses.value = [];
      return;
    }

    console.log('🔍 Final taiKhoanId to use:', taiKhoanId);

    const url = `${API_BASE_URL}/api/dia-chi/tai-khoan/${taiKhoanId}`;
    console.log('🌐 API URL:', url);

    const response = await axios.get(url, {
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`,
        'Content-Type': 'application/json'
      }
    });

    // DEBUG: Log response
    console.log('🔍 ADDRESS RESPONSE:', response.data);
    console.log('🔍 IS ARRAY:', Array.isArray(response.data));

    let addressData = [];
    if (Array.isArray(response.data)) {
      addressData = response.data;
    } else if (response.data?.data && Array.isArray(response.data.data)) {
      addressData = response.data.data;
    } else if (response.data?.addresses && Array.isArray(response.data.addresses)) {
      addressData = response.data.addresses;
    }

    console.log('📍 Raw address data:', addressData);
    console.log('📍 Address count:', addressData.length);

    // FIX: Map addresses với fallback values
    addresses.value = addressData
      .filter(addr => addr && addr.id) // Chỉ lấy địa chỉ có ID
      .map(addr => ({
        id: addr.id,
        tenNguoiNhan: addr.tenNguoiNhan ||
                      addr.hoTen ||
                      userInfo.value?.hoTen ||
                      'Khách hàng',
        sdt: addr.sdt ||
             userInfo.value?.sdt ||
             '',
        diaChiChiTiet: addr.diaChiChiTiet || '',
        maTinh: addr.maTinh || '',
        maHuyen: addr.maHuyen || '',
        maPhuong: addr.maPhuong || '',
        tenTinh: addr.tenTinh || '',
        tenHuyen: addr.tenHuyen || '',
        tenPhuong: addr.tenPhuong || '',
        isDefault: Boolean(addr.isDefault || addr.trangThai === 1),
        trangThai: addr.trangThai || 0
      }))
      .sort((a, b) => {
        // Sort: default trước, sau đó theo ID
        if (a.isDefault && !b.isDefault) return -1;
        if (!a.isDefault && b.isDefault) return 1;
        return b.id - a.id;
      });

    console.log('📍 Final mapped addresses:', addresses.value);

  } catch (error) {
    console.error('❌ Error loading addresses:', error);
    console.error('❌ Error details:', {
      status: error.response?.status,
      data: error.response?.data,
      message: error.message
    });

    addresses.value = [];

    if (error.response?.status === 401) {
      toast.add({
        severity: 'error',
        summary: 'Phiên đăng nhập hết hạn',
        detail: 'Vui lòng đăng nhập lại',
        life: 3000
      });
    } else if (error.response?.status === 404) {
      console.log('No addresses found for user');
    }
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

  // Save address
  const saveAddress = async () => {
  isSubmitting.value = true;
  try {
    const ward = wards.value.find(w => w.id === addressForm.value.maPhuong);
    addressForm.value.tenPhuong = ward?.name || '';

    // FIX: Tìm taiKhoanId cho save
    let taiKhoanId = null;

    if (userInfo.value?.id) {
      taiKhoanId = userInfo.value.id;
    } else if (userInfo.value?.idTaiKhoan) {
      taiKhoanId = userInfo.value.idTaiKhoan;
    } else if (userInfo.value?.taiKhoan?.id) {
      taiKhoanId = userInfo.value.taiKhoan.id;
    } else if (userInfo.value?.taiKhoanId) {
      taiKhoanId = userInfo.value.taiKhoanId;
    }

    console.log('💾 Saving with taiKhoanId:', taiKhoanId);

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
      isDefault: addressForm.value.isDefault,
      idTaiKhoan: taiKhoanId
    };

    console.log('💾 Address data to save:', addressData);

    let response;
    if (editingAddress.value) {
      console.log('✏️ Updating address ID:', editingAddress.value.id);
      response = await axios.put(`${API_BASE_URL}/api/dia-chi/${editingAddress.value.id}`, addressData, {
        headers: {
          'Authorization': `Bearer ${getAuthToken()}`,
          'Content-Type': 'application/json'
        }
      });
    } else {
      console.log('➕ Creating new address');
      response = await axios.post(`${API_BASE_URL}/api/dia-chi`, addressData, {
        headers: {
          'Authorization': `Bearer ${getAuthToken()}`,
          'Content-Type': 'application/json'
        }
      });
    }

    console.log('✅ Save response:', response.data);

    await loadAddresses();
    closeAddressModal();

    toast.add({
      severity: 'success',
      summary: 'Thành công',
      detail: editingAddress.value ? 'Cập nhật địa chỉ thành công!' : 'Thêm địa chỉ thành công!',
      life: 3000
    });
  } catch (error) {
    console.error('❌ Error saving address:', error);
    console.error('❌ Save error details:', {
      status: error.response?.status,
      data: error.response?.data,
      message: error.message
    });

    toast.add({
      severity: 'error',
      summary: 'Lỗi',
      detail: error.response?.data?.message || 'Không thể lưu địa chỉ!',
      life: 3000
    });
  } finally {
    isSubmitting.value = false;
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

  // Delete address
  const deleteAddress = async (id) => {
    if (!confirm('Bạn có chắc chắn muốn xóa địa chỉ này?')) return;

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

  // Utility functions
  const formatAddress = (address) => {
    return [
      address.diaChiChiTiet,
      address.tenPhuong,
      address.tenHuyen,
      address.tenTinh
    ].filter(Boolean).join(', ');
  };

  // Lifecycle
  onMounted(async () => {
    await loadUserInfo();
    await loadAddressData();
    await loadAddresses();
  });
  </script>

  <style scoped>
  /* Loading animation */
  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }

  .animate-spin {
    animation: spin 1s linear infinite;
  }

  /* Remove default input styles */
  input:focus,
  select:focus,
  textarea:focus {
    outline: none !important;
  }

  /* Background colors */
  .bg-orange-600 {
    background-color: #ea580c;
  }

  .hover\:bg-orange-700:hover {
    background-color: #c2410c;
  }

  .text-orange-600 {
    color: #ea580c;
  }

  .hover\:text-orange-700:hover {
    color: #c2410c;
  }

  /* Status badge colors */
  .bg-green-100 {
    background-color: #d1fae5;
  }

  .text-green-800 {
    color: #065f46;
  }

  /* Button transitions */
  button {
    transition: all 0.2s ease;
  }

  /* Hover effects */
  .hover\:shadow-md:hover {
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  }

  .transition-shadow {
    transition-property: box-shadow;
    transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    transition-duration: 150ms;
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

  /* Checkbox custom styles */
  input[type="checkbox"]:checked {
    background-color: #ea580c;
    border-color: #ea580c;
  }

  input[type="checkbox"]:focus {
    box-shadow: 0 0 0 3px rgba(234, 88, 12, 0.1);
  }

  /* Border styles */
  .border-gray-200 {
    border-color: #e5e7eb;
  }

  .border-t {
    border-top-width: 1px;
  }
  </style>
