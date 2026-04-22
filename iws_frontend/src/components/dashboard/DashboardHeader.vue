<!-- <template>
  <div class="mb-8">
    <div class="flex flex-col md:flex-row md:items-center md:justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900 mb-2">Dashboard Thống Kê Sneaker</h1>
        <div class="flex items-center space-x-4">
          <p class="text-gray-600">Tổng quan kinh doanh và phân tích dữ liệu chi tiết</p>
          <StatusIndicator :status="apiStatus" />
        </div>
        <p v-if="lastUpdated" class="text-sm text-gray-500 mt-1">
          Cập nhật lần cuối: {{ formatDateTime(lastUpdated) }}
        </p>
      </div>
      <div class="flex items-center space-x-4 mt-4 md:mt-0">
        <select 
          :value="selectedPeriod"
          @change="$emit('periodChange', $event.target.value)"
          class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="7days">7 ngày qua</option>
          <option value="30days">30 ngày qua</option>
          <option value="90days">3 tháng qua</option>
          <option value="12months">12 tháng qua</option>
        </select>
        <button 
          @click="$emit('refresh')"
          :disabled="loading"
          class="flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
        >
          <RefreshIcon :class="{ 'animate-spin': loading }" class="w-4 h-4 mr-2" />
          Cập nhật
        </button>
      </div>
    </div>
  </div>
</template> -->
<template>
  <div class="mb-8">
    <div class="flex flex-col md:flex-row md:items-center md:justify-between">
      <div>
          <h1 class="text-3xl font-bold text-gray-900 mb-2">Quản lý thống kê</h1>
      </div>
    </div>
  </div>
</template>

<script setup>
import { RefreshCw as RefreshIcon } from 'lucide-vue-next'
import StatusIndicator from '@/components/common/StatusIndicator.vue'

defineProps({
  selectedPeriod: String,
  loading: Boolean,
  lastUpdated: Date,
  apiStatus: String
})

defineEmits(['periodChange', 'refresh'])

const formatDateTime = (date) => {
  return date.toLocaleString('vi-VN')
}
</script>