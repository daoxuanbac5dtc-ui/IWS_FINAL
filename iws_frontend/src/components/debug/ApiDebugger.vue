<template>
    <div class="fixed bottom-4 right-4 bg-white border border-gray-300 rounded-lg p-4 shadow-lg max-w-sm">
        <h3 class="font-bold text-sm mb-2">🔧 API Debug Panel</h3>

        <div class="space-y-2 text-xs">
            <div class="flex justify-between">
                <span>Backend:</span>
                <span :class="backendStatus === 'online' ? 'text-green-600' : 'text-red-600'">
                    {{ backendStatus }}
                </span>
            </div>

            <div class="flex justify-between">
                <span>API Health:</span>
                <span :class="healthStatus ? 'text-green-600' : 'text-red-600'">
                    {{ healthStatus ? 'OK' : 'Failed' }}
                </span>
            </div>

            <div class="flex justify-between">
                <span>Last Check:</span>
                <span class="text-gray-600">{{ lastCheck }}</span>
            </div>
        </div>

        <div class="mt-3 space-y-1">
            <button @click="testConnection" class="w-full bg-blue-500 text-white text-xs py-1 px-2 rounded hover:bg-blue-600" :disabled="testing">
                {{ testing ? 'Testing...' : 'Test Connection' }}
            </button>

            <button @click="runDiagnostics" class="w-full bg-purple-500 text-white text-xs py-1 px-2 rounded hover:bg-purple-600" :disabled="testing">
                {{ testing ? 'Running...' : 'Run Diagnostics' }}
            </button>
        </div>

        <div v-if="diagnostics" class="mt-2 text-xs">
            <div class="font-semibold">Diagnostics:</div>
            <div class="pl-2">
                <div>Health: {{ diagnostics.health ? '✅' : '❌' }}</div>
                <div>Test: {{ diagnostics.test ? '✅' : '❌' }}</div>
                <div>Stats: {{ diagnostics.stats ? '✅' : '❌' }}</div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useDashboardAPI } from '@/composables/useDashboardAPI';
import { dashboardAPI } from '@/services/dashboardAPI';
import { onMounted, ref } from 'vue';

const backendStatus = ref('checking');
const healthStatus = ref(false);
const lastCheck = ref('');
const testing = ref(false);
const diagnostics = ref(null);

const { runDiagnostics: runFullDiagnostics } = useDashboardAPI();

const testConnection = async () => {
    testing.value = true;
    try {
        const isHealthy = await dashboardAPI.healthCheck();
        healthStatus.value = isHealthy;
        backendStatus.value = isHealthy ? 'online' : 'offline';
        lastCheck.value = new Date().toLocaleTimeString();
    } catch (error) {
        healthStatus.value = false;
        backendStatus.value = 'error';
    } finally {
        testing.value = false;
    }
};

const runDiagnostics = async () => {
    testing.value = true;
    try {
        const results = await runFullDiagnostics();
        diagnostics.value = results;
    } finally {
        testing.value = false;
    }
};

onMounted(() => {
    testConnection();
});
</script>
