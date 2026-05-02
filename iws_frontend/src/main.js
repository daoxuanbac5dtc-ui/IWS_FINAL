import { createApp } from 'vue';
import App from './App.vue';
import router from './router';

import Aura from '@primeuix/themes/aura';
import PrimeVue from 'primevue/config';
import ConfirmationService from 'primevue/confirmationservice';
import ToastService from 'primevue/toastservice';

import '@/assets/styles.scss';
import 'bootstrap-icons/font/bootstrap-icons.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import './style.css';

import 'bootstrap/dist/js/bootstrap.bundle.min.js';

import { MotionPlugin } from '@vueuse/motion';

function createFallbackAuthPlugin() {
    return {
        install(app) {
            app.config.globalProperties.$auth = {
                isAuthenticated: false,
                user: null,
                login: () => Promise.resolve(false),
                logout: () => Promise.resolve()
            };
        }
    };
}

function createNoopDirective() {
    return {
        beforeMount() {},
        updated() {}
    };
}

async function loadOptionalModules() {
    try {
        await import('./plugins/axios');
        console.log('Axios plugin loaded successfully');
    } catch (error) {
        console.warn('Axios plugin not found or failed to load:', error.message);
    }

    try {
        const authModule = await import('@/composables/useAuthPermissions');
        console.log('Auth module loaded successfully');
        return {
            authPlugin: authModule.AuthPlugin,
            permissionDirective: authModule.permissionDirective,
            roleDirective: authModule.roleDirective
        };
    } catch (error) {
        console.error('Failed to load auth module:', error.message);
        return {
            authPlugin: createFallbackAuthPlugin(),
            permissionDirective: createNoopDirective(),
            roleDirective: createNoopDirective()
        };
    }
}

function renderFatalError(error) {
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
      <h1 style="color: #dc3545; margin-bottom: 1rem;">Ung dung khong the khoi dong</h1>
      <p style="margin-bottom: 1rem;">Da xay ra loi khi khoi dong ung dung:</p>
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
      ">Tai lai trang</button>
    </div>
  `;
}

async function bootstrap() {
    const { authPlugin, permissionDirective, roleDirective } = await loadOptionalModules();
    const app = createApp(App);

    app.use(router);
    app.use(PrimeVue, {
        theme: {
            preset: Aura,
            options: {
                darkModeSelector: '.app-dark'
            }
        }
    });
    app.use(ToastService);
    app.use(ConfirmationService);
    app.use(MotionPlugin);
    app.use(authPlugin);
    app.directive('permission', permissionDirective);
    app.directive('role', roleDirective);
    app.mount('#app');

    if (import.meta.env.DEV) {
        console.log('Running in development mode');
        window.__VUE_APP__ = app;
        window.__VUE_ROUTER__ = router;
    }
}

bootstrap().catch((error) => {
    console.error('App bootstrap failed:', error);
    renderFatalError(error);
});

window.addEventListener('error', (event) => {
    console.error('Global error caught:', event.error);
});

window.addEventListener('unhandledrejection', (event) => {
    console.error('Unhandled promise rejection:', event.reason);
    event.preventDefault();
});

const originalWarn = console.warn;
console.warn = function (...args) {
    if (args[0] && typeof args[0] === 'string' && args[0].includes('Deprecated since v4. Use DatePicker component instead.')) {
        return;
    }
    originalWarn.apply(console, args);
};

