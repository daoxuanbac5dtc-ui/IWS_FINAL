<template>
  <div class="bg-white rounded-2xl shadow-lg border border-gray-200 p-8">
    <h3 class="text-xl font-bold text-gray-800 mb-6">📊 Doanh thu theo tháng</h3>

    <!-- Biểu đồ rộng và cao hơn -->
    <div class="h-[550px] w-full">
      <BarChart v-if="chartData" :data="chartData" :options="chartOptions" />
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { ref, onMounted } from "vue";
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
} from "chart.js";
import { Bar as BarChart } from "vue-chartjs";

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale);

export default {
  name: "DoanhThuThang",
  components: { BarChart },
  setup() {
    const chartData = ref(null);
    const chartOptions = ref({});

    const fetchDoanhThu = async () => {
      try {
        const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/thong-ke/doanh-thu-thang`);
        const data = res.data;

        chartData.value = {
          labels: data.map((d) => `Tháng ${d.thang}`),
          datasets: [
            {
              label: "Doanh thu (VNĐ)",
              data: data.map((d) => d.tongDoanhThu),
              backgroundColor: "#ef4444", // 🌟 đỏ nổi bật
              hoverBackgroundColor: "#dc2626", // đỏ đậm khi hover
              borderRadius: 5,
            },
          ],
        };

        chartOptions.value = {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { 
              position: "top",
              labels: { font: { size: 14 },
            }
              
            },
            tooltip: {
              backgroundColor: "#111827",
              titleColor: "#fff",
              bodyColor: "#facc15",
              padding: 12,
              callbacks: {
                label: function (context) {
                  return (
                    " " +
                    new Intl.NumberFormat("vi-VN", {
                      style: "currency",
                      currency: "VND",
                      maximumFractionDigits: 0,
                    }).format(context.raw)
                  );
                },
              },
            },
          },
          scales: {
            x: {
              grid: { color: "rgba(0,0,0,0.05)" },
              ticks: { font: { size: 11 } },
            },
            y: {
              beginAtZero: true,
              ticks: { 
                font: { size: 13 },
                callback: (value) =>
                  new Intl.NumberFormat("vi-VN", {
                    style: "currency",
                    currency: "VND",
                    maximumFractionDigits: 0,
                  }).format(value),
              },
              title: {
                display: true,
                text: "Doanh thu (VNĐ)",
                font: { size: 15, weight: "bold" },
              },
              grid: { color: "rgba(0,0,0,0.08)" },
            },
          },
        };
      } catch (err) {
        console.error("Lỗi khi tải doanh thu tháng:", err);
      }
    };

    onMounted(fetchDoanhThu);

    return { chartData, chartOptions };
  },
};
</script>

