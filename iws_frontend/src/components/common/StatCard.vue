<template>
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between">
            <div class="flex-1">
                <p class="text-sm font-medium text-gray-600 mb-1">{{ title }}</p>
                <p class="text-2xl font-bold text-gray-900">{{ value }}</p>
                <div v-if="change" :class="trendClass" class="flex items-center mt-2 text-sm">
                    <TrendingUpIcon v-if="trend === 'up'" class="w-4 h-4 mr-1" />
                    <TrendingDownIcon v-else class="w-4 h-4 mr-1" />
                    <span>{{ change }}</span>
                </div>
            </div>
            <div :class="iconBackgroundClass" class="p-3 rounded-full">
                <component :is="icon" :class="iconClass" class="w-6 h-6" />
            </div>
        </div>
    </div>
</template>

<script setup>
import { TrendingDown as TrendingDownIcon, TrendingUp as TrendingUpIcon } from 'lucide-vue-next';
import { computed } from 'vue';

const props = defineProps({
    title: String,
    value: String,
    change: String,
    icon: Object,
    trend: String,
    color: {
        type: String,
        default: 'blue'
    }
});

const trendClass = computed(() => {
    return props.trend === 'up' ? 'text-green-600' : 'text-red-600';
});

const iconBackgroundClass = computed(() => {
    return `bg-${props.color}-50`;
});

const iconClass = computed(() => {
    return `text-${props.color}-600`;
});
</script>
