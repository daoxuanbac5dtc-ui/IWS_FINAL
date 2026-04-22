<template>
    <div class="nike-complete-layout">
      <!-- Navigation Component -->
      <Nav />

      <!-- Breadcrumb -->
      <div class="breadcrumb-section bg-gray-50 py-4">
        <div class="w-full mx-auto px-6">
          <nav class="flex items-center space-x-2 text-sm">
            <router-link to="/" class="text-gray-600 hover:text-orange-500 transition-colors">
              Trang chủ
            </router-link>
            <span class="text-gray-400">/</span>
            <router-link to="/profile/info" class="text-gray-600 hover:text-orange-500 transition-colors">
              Thông tin của tôi
            </router-link>
            <span v-if="getSubPageTitle()" class="text-gray-400">/</span>
            <span v-if="getSubPageTitle()" class="text-gray-900 font-medium">
              {{ getSubPageTitle() }}
            </span>
          </nav>
        </div>
      </div>

      <!-- Main Content -->
      <main class="nike-layout w-full bg-gray-50 py-8">
        <div class="w-full mx-auto px-6">
          <div class="profile-container max-w-7xl mx-auto">
            <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">

              <!-- Sidebar Navigation -->
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
                    <router-link
                      to="/profile/info"
                      active-class="bg-orange-50 text-orange-600 border-l-4 border-orange-500"
                      class="w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 text-gray-600 hover:bg-gray-50"
                    >
                      <i class="pi pi-user text-lg"></i>
                      <span class="font-medium">Thông tin cá nhân</span>
                    </router-link>

                    <router-link
                      to="/profile/orders"
                      active-class="bg-orange-50 text-orange-600 border-l-4 border-orange-500"
                      class="w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 text-gray-600 hover:bg-gray-50"
                    >
                      <i class="pi pi-file-text text-lg"></i>
                      <span class="font-medium">Hóa đơn</span>
                    </router-link>

                    <router-link
                      to="/profile/addresses"
                      active-class="bg-orange-50 text-orange-600 border-l-4 border-orange-500"
                      class="w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 text-gray-600 hover:bg-gray-50"
                    >
                      <i class="pi pi-map-marker text-lg"></i>
                      <span class="font-medium">Địa chỉ giao hàng</span>
                    </router-link>

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

              <!-- Main Content Area - Router View -->
              <div class="lg:col-span-3">
                <slot>
                    <router-view />
                </slot>
              </div>
            </div>
          </div>
        </div>
      </main>

      <!-- Footer Component -->
      <section class="padding-x padding-t bg-black pb-8">
        <Footer />
      </section>

      <ScrollToggler />
    </div>
  </template>

  <script setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import axios from 'axios';
  import { useToast } from 'primevue/usetoast';

  import Nav from '@/components/user/Nav.vue';
  import Footer from '@/views/user/Footer.vue';
  import ScrollToggler from '@/components/user/ScrollToggler.vue';

  const router = useRouter();
  const toast = useToast();

  // State
  const userInfo = ref({});

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

      userInfo.value = response.data;
      console.log('User info loaded:', userInfo.value);

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

  // Utility functions
  const getInitials = (name) => {
    if (!name) return 'U';
    const words = name.split(' ');
    return words.map(w => w[0]).join('').toUpperCase().slice(0, 2);
  };

  const getSubPageTitle = () => {
    const routeTitles = {
      'ProfileOrders': 'Hóa đơn',
      'ProfileAddresses': 'Địa chỉ giao hàng',
      'OrderDetail': 'Chi tiết đơn hàng'
    };

    return routeTitles[router.currentRoute.value.name] || '';
  };

  // Logout
  const logout = () => {
    if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('user_info');
      router.push('/auth/login');
    }
  };

  // Lifecycle
  onMounted(() => {
    loadUserInfo();
  });
  </script>

  <style scoped>
  .nike-complete-layout {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    width: 100%;
  }

  .nike-layout {
    flex: 1;
    width: 100%;
  }

  .w-full {
    width: 100% !important;
  }

  main, .nike-layout {
    max-width: none !important;
  }

  .transition-colors {
    transition-property: color, background-color, border-color;
    transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    transition-duration: 150ms;
  }

  * {
    box-sizing: border-box;
  }

  .padding-x {
    padding-left: 1.5rem;
    padding-right: 1.5rem;
  }

  .padding-t {
    padding-top: 3rem;
  }

  @media (min-width: 640px) {
    .padding-x {
      padding-left: 2rem;
      padding-right: 2rem;
    }
  }

  @media (min-width: 1024px) {
    .padding-x {
      padding-left: 3rem;
      padding-right: 3rem;
    }
  }

  @media (max-width: 1024px) {
    .lg\:grid-cols-4 {
      grid-template-columns: 1fr;
    }

    .lg\:col-span-1,
    .lg\:col-span-3 {
      grid-column: span 1;
    }
  }

  .max-w-7xl {
    max-width: 80rem;
  }

  .mx-auto {
    margin-left: auto;
    margin-right: auto;
  }

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
  </style>
