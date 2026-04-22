<template>
  <div class="bg-white rounded-xl shadow-md border border-gray-200 p-6">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-xl font-semibold text-gray-800">📊 Top sản phẩm bán chạy</h3>
      <select
        v-model="top"
        @change="fetchTopProducts"
        class="px-3 py-1 border rounded-lg text-sm shadow-sm"
      >
        <option value="5">Top 5</option>
        <option value="10">Top 10</option>
        <option value="20">Top 20</option>
      </select>
    </div>

    <!-- Bảng -->
    <div class="overflow-x-auto mb-8">
      <table class="min-w-full border border-gray-200 rounded-lg shadow-sm">
        <thead class="bg-gray-100">
          <tr>
            <th class="px-4 py-2 text-left text-sm font-medium text-gray-600">#</th>
            <th class="px-4 py-2 text-left text-sm font-medium text-gray-600">Tên sản phẩm</th>
            <th class="px-4 py-2 text-left text-sm font-medium text-gray-600">Số lượng bán</th>
            <th class="px-4 py-2 text-left text-sm font-medium text-gray-600">Doanh thu</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(sp, index) in topProducts"
            :key="index"
            class="border-t hover:bg-gray-50 transition"
          >
            <td class="px-4 py-2 text-sm text-gray-700">{{ index + 1 }}</td>
            <td class="px-4 py-2 text-sm font-medium text-gray-800">{{ sp.tenSanPham }}</td>
            <td class="px-4 py-2 text-sm text-gray-700">{{ sp.tongSoLuongBan }}</td>
            <td class="px-4 py-2 text-sm text-gray-700">
              {{ formatCurrency(sp.tongDoanhThu) }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Biểu đồ -->
    <div class="w-full h-[500px] bg-gray-50 rounded-lg p-4 shadow-inner">
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

// ✅ Plugin hiển thị số trên đầu cột
const dataLabelPlugin = {
  id: "dataLabelPlugin",
  afterDatasetsDraw(chart) {
    const { ctx } = chart;
    chart.data.datasets.forEach((dataset, i) => {
      const meta = chart.getDatasetMeta(i);
      meta.data.forEach((bar, index) => {
        const value = dataset.data[index];
        let text = value;

        if (dataset.label.includes("Doanh thu")) {
          text = new Intl.NumberFormat("vi-VN", {
            style: "currency",
            currency: "VND",
            maximumFractionDigits: 0,
          }).format(value);
        }

        ctx.save();
        ctx.fillStyle = "#111827";
        ctx.font = "12px sans-serif";
        ctx.textAlign = "center";
        ctx.fillText(text, bar.x, bar.y - 6);
        ctx.restore();
      });
    });
  },
};

export default {
  name: "TopProductsList",
  components: { BarChart },
  setup() {
    const top = ref(5);
    const topProducts = ref([]);
    const chartData = ref(null);
    const chartOptions = ref({});

    const formatCurrency = (value) =>
      new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
      }).format(value);

    const fetchTopProducts = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/thong-ke/top-ban-chay?top=${top.value}`
        );
        topProducts.value = res.data;

        chartData.value = {
          labels: res.data.map((p) => p.tenSanPham),
          datasets: [
            {
              label: "Số lượng bán",
              data: res.data.map((p) => p.tongSoLuongBan),
              backgroundColor: "#4F46E5",
              yAxisID: "y",
              borderRadius: 5,
              hoverBackgroundColor: "#4338CA",
            },
            {
              label: "Doanh thu (VNĐ)",
              data: res.data.map((p) => p.tongDoanhThu),
              backgroundColor: "#22C55E",
              yAxisID: "y1",
              borderRadius: 5,
              hoverBackgroundColor: "#16A34A",
            },
          ],
        };

        chartOptions.value = {
          responsive: true,
          maintainAspectRatio: false, // 👈 cho phép chart full container
          plugins: {
            legend: { position: "top" },
            tooltip: {
              callbacks: {
                label: function (context) {
                  if (context.dataset.label.includes("Doanh thu")) {
                    return new Intl.NumberFormat("vi-VN", {
                      style: "currency",
                      currency: "VND",
                    }).format(context.raw);
                  }
                  return context.raw;
                },
              },
            },
          },
          scales: {
            y: {
              type: "linear",
              display: true,
              position: "left",
              title: { display: true, text: "Số lượng" , font: { size: 14 , weight: 'bold' } },
            },
            y1: {
              type: "linear",
              display: true,
              position: "right",
              grid: { drawOnChartArea: false },
              title: { display: true, text: "Doanh thu (VNĐ)", font: { size: 14 , weight: 'bold' } },
            },
          },
          plugins: [dataLabelPlugin],
        };
      } catch (err) {
        console.error("Lỗi khi tải dữ liệu top sản phẩm:", err);
      }
    };

    onMounted(fetchTopProducts);

    return {
      top,
      topProducts,
      chartData,
      chartOptions,
      fetchTopProducts,
      formatCurrency,
    };
  },
};
</script>
