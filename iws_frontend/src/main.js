import { createApp } from 'vue';
import App from './App.vue';
import router from './router';

// PrimeVue imports
import Aura from '@primeuix/themes/aura';
import PrimeVue from 'primevue/config';
import ConfirmationService from 'primevue/confirmationservice';
import ToastService from 'primevue/toastservice';

// CSS imports - ĐÃ SỬA THỨ TỰ
import '@/assets/styles.scss';
import 'bootstrap-icons/font/bootstrap-icons.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import './style.css';

// JavaScript imports
import 'bootstrap/dist/js/bootstrap.bundle.min.js';

// Plugins
import { MotionPlugin } from '@vueuse/motion';

// CHỈNH SỬA: Import axios plugin an toàn hơn
try {
    await import('./plugins/axios');
    console.log('Axios plugin loaded successfully');
} catch (error) {
    console.warn('Axios plugin not found or failed to load:', error.message);
}

// CHỈNH SỬA: Import auth composables với error handling
let AuthPlugin, permissionDirective, roleDirective;

try {
    const authModule = await import('@/composables/useAuthPermissions');
    AuthPlugin = authModule.AuthPlugin;
    permissionDirective = authModule.permissionDirective;
    roleDirective = authModule.roleDirective;
    console.log('Auth module loaded successfully');
} catch (error) {
    console.error('Failed to load auth module:', error.message);
    console.log('Running without auth functionality');

    // Fallback: Tạo auth plugin và directives giả
    AuthPlugin = {
        install(app) {
            console.log('Using fallback auth plugin');
            app.config.globalProperties.$auth = {
                isAuthenticated: false,
                user: null,
                login: () => Promise.resolve(false),
                logout: () => Promise.resolve()
            };
        }
    };

    permissionDirective = {
        beforeMount(el, binding) {
            console.log('Permission directive (fallback):', binding.value);
        },
        updated(el, binding) {
            console.log('Permission directive updated (fallback):', binding.value);
        }
    };

    roleDirective = {
        beforeMount(el, binding) {
            console.log('Role directive (fallback):', binding.value);
        },
        updated(el, binding) {
            console.log('Role directive updated (fallback):', binding.value);
        }
    };
}

// Tạo ứng dụng Vue
const app = createApp(App);

// CHỈNH SỬA: Error handling cho router
try {
    app.use(router);
    console.log('Router installed successfully');
} catch (error) {
    console.error('Router installation failed:', error.message);
}

// CHỈNH SỬA: PrimeVue setup với error handling
try {
    app.use(PrimeVue, {
        theme: {
            preset: Aura,
            options: {
                darkModeSelector: '.app-dark'
            }
        }
    });
    console.log('PrimeVue installed successfully');
} catch (error) {
    console.error('PrimeVue installation failed:', error.message);
}

// Services
try {
    app.use(ToastService);
    app.use(ConfirmationService);
    console.log('PrimeVue services installed successfully');
} catch (error) {
    console.error('PrimeVue services installation failed:', error.message);
}

// Motion plugin
try {
    app.use(MotionPlugin);
    console.log('Motion plugin installed successfully');
} catch (error) {
    console.error('Motion plugin installation failed:', error.message);
}

// Auth plugin
try {
    app.use(AuthPlugin);
    console.log('Auth plugin installed successfully');
} catch (error) {
    console.error('Auth plugin installation failed:', error.message);
}

// Global directives
try {
    app.directive('permission', permissionDirective);
    app.directive('role', roleDirective);
    console.log('Auth directives registered successfully');
} catch (error) {
    console.error('Auth directives registration failed:', error.message);
}

// CHỈNH SỬA: Error handling cho app mounting
try {
    app.mount('#app');
    console.log('App mounted successfully');
} catch (error) {
    console.error('App mounting failed:', error.message);

    // Fallback: Hiển thị error message
    document.body.innerHTML = `
    <div style="
      display: flex; 
      justify-content: center; 
      align-items: center; 
      height: 100vh; 
      flex-direction: column;
      font-family: Arial, sans-serif;
      background: #f5f5f5;
      color: #333;
    ">
      <h1 style="color: #dc3545; margin-bottom: 1rem;">Ứng dụng không thể khởi động</h1>
      <p style="margin-bottom: 1rem;">Đã xảy ra lỗi khi khởi động ứng dụng:</p>
      <code style="
        background: #fff; 
        padding: 1rem; 
        border-radius: 4px; 
        border: 1px solid #ddd;
        max-width: 80%;
        word-break: break-word;
      ">${error.message}</code>
      <button onclick="location.reload()" style="
        margin-top: 1rem; 
        padding: 0.5rem 1rem; 
        background: #007bff; 
        color: white; 
        border: none; 
        border-radius: 4px; 
        cursor: pointer;
      ">Tải lại trang</button>
    </div>
  `;
}

// THÊM: Global error handler
window.addEventListener('error', (event) => {
    console.error('Global error caught:', event.error);
});

window.addEventListener('unhandledrejection', (event) => {
    console.error('Unhandled promise rejection:', event.reason);
    event.preventDefault();
});

// THÊM: Dev tools cho development
if (import.meta.env.DEV) {
    console.log('Running in development mode');
    window.__VUE_APP__ = app;
    window.__VUE_ROUTER__ = router;
}

// Suppress PrimeVue Calendar deprecation warning
const originalWarn = console.warn;
console.warn = function (...args) {
    if (args[0] && typeof args[0] === 'string' && args[0].includes('Deprecated since v4. Use DatePicker component instead.')) {
        return; // Suppress this specific warning
    }
    originalWarn.apply(console, args);
};
