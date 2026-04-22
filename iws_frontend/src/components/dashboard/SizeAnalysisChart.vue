<template>
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-6">Phân Tích Size Giày</h3>
        <div class="h-64">
            <canvas ref="chartCanvas"></canvas>
        </div>
    </div>
</template>

<script setup>
import { Chart, registerables } from 'chart.js';
import { nextTick, onMounted, ref, watch } from 'vue';

Chart.register(...registerables);

const props = defineProps({
    data: {
        type: Array,
        required: true
    }
});

const chartCanvas = ref(null);
let chartInstance = null;

const createChart = async () => {
    await nextTick();

    if (!chartCanvas.value || !props.data?.length) return;

    const ctx = chartCanvas.value.getContext('2d');

    if (chartInstance) {
        chartInstance.destroy();
    }

    chartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: props.data.map((item) => `Size ${item.size}`),
            datasets: [
                {
                    label: 'Số lượng bán',
                    data: props.data.map((item) => item.count),
                    backgroundColor: '#8B5CF6',
                    borderRadius: 4
                }
            ]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                x: {
                    beginAtZero: true
                }
            }
        }
    });
};

onMounted(() => {
    createChart();
});

watch(
    () => props.data,
    () => {
        createChart();
    },
    { deep: true }
);
</script>
