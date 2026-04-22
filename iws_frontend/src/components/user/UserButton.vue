<template>
    <button class="flex items-center justify-center gap-2 rounded-full border bg-coral-red px-7 py-4 font-montserrat text-lg font-medium leading-none text-white">
        <!-- Ưu tiên: 1. Slot text, 2. Label prop, 3. Text mặc định -->
        <slot>{{ label || 'Button' }}</slot>
        <img v-if="iconUrl != ''" class="ml-2 h-5 w-5 rounded-full" :src="iconUrl" alt="" />
    </button>
</template>

<script setup>
import { defineProps } from 'vue';

const props = defineProps({
    iconUrl: {
        type: String,
        default: ''
    },
    label: {
        type: String,
        default: ''
    }
});
</script>

<style lang="scss" scoped></style>
