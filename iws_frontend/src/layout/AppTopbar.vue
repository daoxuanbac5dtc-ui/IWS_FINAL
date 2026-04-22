<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useLayout } from '@/layout/composables/layout';
import AppConfigurator from './AppConfigurator.vue';
import Logo1 from '@/layout/composables/logo.png';

const { toggleMenu, toggleDarkMode, isDarkTheme } = useLayout();
const router = useRouter();

// Reactive data
const user = ref(null);
const isProfileDropdownOpen = ref(false);

// Computed properties
const userName = computed(() => {
  return user.value?.email || 'Guest';
});

// Methods
const loadUserData = () => {
  // Lấy thông tin user từ localStorage (khớp với router guard)
  const userData = localStorage.getItem('user_info'); // Đổi từ 'user' thành 'user_info'
  if (userData) {
    try {
      user.value = JSON.parse(userData);
      console.log('💼 User data loaded:', user.value);
    } catch (e) {
      console.error('❌ Error parsing user data:', e);
      localStorage.removeItem('user_info');
      localStorage.removeItem('auth_token');
    }
  }
};

const toggleProfileDropdown = () => {
  isProfileDropdownOpen.value = !isProfileDropdownOpen.value;
};

const logout = async () => {
  try {
    console.log('🔄 Starting logout process...');

    // Đóng dropdown trước
    isProfileDropdownOpen.value = false;

    // Gọi API logout với POST method
    const token = localStorage.getItem('auth_token'); // Sửa từ 'token' thành 'auth_token'
    if (token) {
      console.log('📤 Calling logout API with POST method...');

      try {
        const response = await fetch('http://localhost:8080/auth/logout', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
          credentials: 'include',
        });

        if (response.ok) {
          console.log('✅ Logout API call successful');
          const result = await response.json();
          console.log('📝 Logout response:', result);
        } else {
          console.log('⚠️ Logout API call failed with status:', response.status);
        }
      } catch (apiError) {
        console.log('⚠️ Logout API call failed:', apiError.message);
        console.log('🔄 Continuing with local cleanup...');
      }
    }

    // Xóa ĐÚNG keys khỏi localStorage
    console.log('🧹 Cleaning local storage...');
    localStorage.removeItem('auth_token');    // Key chính xác
    localStorage.removeItem('user_info');     // Key chính xác
    localStorage.removeItem('rememberMe');    // Thêm key này
    localStorage.removeItem('savedEmail');    // Thêm key này

    // Xóa các keys cũ (nếu có)
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    localStorage.removeItem('authToken');

    // Xóa tất cả storage
    sessionStorage.clear();

    // Reset user state
    user.value = null;

    console.log('✅ Local storage cleaned');
    console.log('🏠 Redirecting to home page...');

    // Force reload trang để clear mọi state
    setTimeout(() => {
      window.location.reload();
      window.location.href = '/';
    }, 100);

  } catch (error) {
    console.error('❌ Logout error:', error);

    // Force cleanup dù có lỗi
    localStorage.clear(); // Clear toàn bộ localStorage
    sessionStorage.clear();
    user.value = null;
    isProfileDropdownOpen.value = false;

    // Force reload và redirect
    setTimeout(() => {
      window.location.reload();
      window.location.href = '/';
    }, 100);
  }
};

// Click outside to close dropdown
const handleClickOutside = (event) => {
  const dropdown = document.querySelector('.profile-dropdown');
  const profileButton = document.querySelector('.profile-button');

  if (dropdown && !dropdown.contains(event.target) && !profileButton.contains(event.target)) {
    isProfileDropdownOpen.value = false;
  }
};

// Lifecycle hooks
onMounted(() => {
  loadUserData();
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<template>
    <div class="layout-topbar">
        <div class="layout-topbar-logo-container">
            <button class="layout-menu-button layout-topbar-action" @click="toggleMenu">
                <i class="pi pi-bars"></i>
            </button>
            <router-link to="/" class="layout-topbar-logo">
                <img :src="Logo1" alt="Logo1" class="img-fluid" style="width: 50px" />
                <span>BEE SHOES</span>
            </router-link>
        </div>

        <div class="layout-topbar-actions">
            <div class="layout-config-menu">
                <button type="button" class="layout-topbar-action" @click="toggleDarkMode">
                    <i :class="['pi', { 'pi-moon': isDarkTheme, 'pi-sun': !isDarkTheme }]"></i>
                </button>
                <div class="relative">
                    <button
                        v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'animate-scalein', leaveToClass: 'hidden', leaveActiveClass: 'animate-fadeout', hideOnOutsideClick: true }"
                        type="button"
                        class="layout-topbar-action layout-topbar-action-highlight"
                    >
                        <i class="pi pi-palette"></i>
                    </button>
                    <AppConfigurator />
                </div>
            </div>

            <button
                class="layout-topbar-menu-button layout-topbar-action"
                v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'animate-scalein', leaveToClass: 'hidden', leaveActiveClass: 'animate-fadeout', hideOnOutsideClick: true }"
            >
                <i class="pi pi-ellipsis-v"></i>
            </button>

            <div class="layout-topbar-menu hidden lg:block">
                <div class="layout-topbar-menu-content">
                    <button type="button" class="layout-topbar-action">
                        <i class="pi pi-calendar"></i>
                        <span>Calendar</span>
                    </button>
                    <button type="button" class="layout-topbar-action">
                        <i class="pi pi-inbox"></i>
                        <span>Messages</span>
                    </button>

                    <!-- Profile Dropdown -->
                    <div class="relative">
                        <button
                            type="button"
                            class="layout-topbar-action profile-button"
                            @click="toggleProfileDropdown"
                        >
                            <i class="pi pi-user"></i>
                            <span>Profile</span>
                        </button>

                        <!-- Profile Dropdown Menu -->
                        <div
                            v-if="isProfileDropdownOpen"
                            class="profile-dropdown"
                            @click.stop
                        >
                            <div class="profile-dropdown-header">
                                <div class="profile-user-info">
                                    <i class="pi pi-user profile-avatar"></i>
                                    <div class="profile-user-details">
                                        <div class="profile-user-name">{{ userName }}</div>
                                        <div class="profile-user-role" v-if="user?.vaiTro">{{ user.vaiTro }}</div>
                                    </div>
                                </div>
                            </div>
                            <div class="profile-dropdown-divider"></div>
                            <div class="profile-dropdown-content">
                                <button
                                    type="button"
                                    class="profile-dropdown-item logout-btn"
                                    @click="logout"
                                >
                                    <i class="pi pi-sign-out"></i>
                                    <span>Đăng xuất</span>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* Click outside to close */
.profile-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  background: var(--surface-card);
  border: 1px solid var(--surface-border);
  border-radius: 6px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  min-width: 200px;
  z-index: 1000;
  margin-top: 0.5rem;
}

.profile-dropdown-header {
    padding: 1rem;
}

.profile-user-info {
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.profile-avatar {
    width: 32px;
    height: 32px;
    background: var(--primary-color);
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1rem;
}

.profile-user-details {
    flex: 1;
}

.profile-user-name {
    font-weight: 600;
    color: var(--text-color);
    font-size: 0.875rem;
}

.profile-user-role {
    color: var(--text-color-secondary);
    font-size: 0.75rem;
    margin-top: 0.25rem;
}

.profile-dropdown-divider {
    height: 1px;
    background: var(--surface-border);
    margin: 0;
}

.profile-dropdown-content {
    padding: 0.5rem 0;
}

.profile-dropdown-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    width: 100%;
    padding: 0.75rem 1rem;
    background: transparent;
    border: none;
    color: var(--text-color);
    font-size: 0.875rem;
    cursor: pointer;
    transition: all 0.2s;
}

.profile-dropdown-item:hover {
    background: var(--surface-hover);
}

.logout-btn {
    color: var(--red-500);
}

.logout-btn:hover {
    background: var(--red-50);
    color: var(--red-600);
}

/* Click outside to close */
.relative {
  position: relative;
}
</style>
