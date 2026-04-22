<template>
  <div class="bg-red-50 border border-red-200 rounded-lg p-6 m-4">
    <div class="flex items-center">
      <AlertCircleIcon class="w-5 h-5 text-red-400 mr-3 flex-shrink-0" />
      <div class="flex-1">
        <h3 class="text-sm font-medium text-red-800">Có lỗi xảy ra</h3>
        <p class="mt-1 text-sm text-red-700">{{ error }}</p>
        <button
          @click="$emit('retry')"
          class="mt-2 bg-red-100 px-3 py-1 rounded text-sm font-medium text-red-800 hover:bg-red-200 transition-colors"
        >
          Thử lại
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { AlertCircle as AlertCircleIcon } from 'lucide-vue-next'

defineProps({
  error: {
    type: String,
    required: true
  }
})

defineEmits(['retry'])
</script>