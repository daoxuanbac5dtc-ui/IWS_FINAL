<template>
  <div class="flex items-center space-x-2">
    <div :class="statusColor" class="w-2 h-2 rounded-full"></div>
    <span class="text-xs text-gray-600">{{ statusText }}</span>
    <WifiIcon v-if="status === 'connected'" class="w-4 h-4 text-green-500" />
    <WifiOffIcon v-else-if="status === 'disconnected'" class="w-4 h-4 text-red-500" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Wifi as WifiIcon, WifiOff as WifiOffIcon } from 'lucide-vue-next'

const props = defineProps({
  status: {
    type: String,
    required: true
  }
})

const statusColor = computed(() => {
  switch (props.status) {
    case 'connected': return 'bg-green-500'
    case 'disconnected': return 'bg-red-500'
    case 'error': return 'bg-yellow-500'
    default: return 'bg-gray-500'
  }
})

const statusText = computed(() => {
  switch (props.status) {
    case 'connected': return 'Kết nối API'
    case 'disconnected': return 'Mất kết nối'
    case 'error': return 'Lỗi API'
    default: return 'Đang kiểm tra...'
  }
})
</script>