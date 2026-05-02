<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";
import { Chart, registerables } from "chart.js";
Chart.register(...registerables);

const chartRef = ref(null);
let chartInstance = null;

// Hàm lấy dữ liệu từ API
const fetchData = async () => {
  try {
    const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/thong-ke/trang-thai-don-hang`);
    const apiData = res.data;
    
    // Bước 1: Chuẩn hóa và gộp dữ liệu
    const mergedData = {};
    
    apiData.forEach(item => {
      // Chuẩn hóa trạng thái về tiếng Việt
      let normalizedStatus;
      const status = item.status.toLowerCase().trim();
      const statusKey = status
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/\u0111/g, "d")
        .replace(/[^a-z0-9]+/g, "_")
        .replace(/^_+|_+$/g, "");
      
      switch (statusKey) {
        case "completed":
        case "da_thanh_toan":
        case "hoan_thanh":
        case "hoàn thành":
        case "hoan thanh":
        case "complete":
          normalizedStatus = "Hoàn thành";
          break;
          
        case "shipping":
        case "dang_giao":
        case "dang_giao_hang":
        case "đang giao":
        case "dang giao":
        case "đang giao hàng":
        case "shipping":
          normalizedStatus = "Đang giao";
          break;
          
        case "confirmed":
        case "da_xac_nhan":
        case "xac_nhan":
        case "đã xác nhận":
        case "da xac nhan":
        case "xác nhận":
        case "confirm":
          normalizedStatus = "Đã xác nhận";
          break;
          
        case "cancelled":
        case "canceled":
        case "da_huy":
        case "huy":
        case "đã hủy":
        case "da huy":
        case "hủy":
        case "cancel":
          normalizedStatus = "Đã hủy";
          break;
          
        case "pending":
        case "cho_xu_ly":
        case "cho_xac_nhan":
        case "dang_cho":
        case "chờ xử lý":
        case "cho xu ly":
        case "đang chờ":
          normalizedStatus = "Chờ xử lý";
          break;
          
        case "processing":
        case "dang_xu_ly":
        case "đang xử lý":
        case "dang xu ly":
          normalizedStatus = "Đang xử lý";
          break;
          
        default:
          // Nếu không match case nào, giữ nguyên nhưng viết hoa chữ cái đầu
          normalizedStatus = item.status.charAt(0).toUpperCase() + item.status.slice(1);
      }
      
      // Gộp số lượng của cùng trạng thái
      if (mergedData[normalizedStatus]) {
        mergedData[normalizedStatus] += item.count;
      } else {
        mergedData[normalizedStatus] = item.count;
      }
    });
    
    // Bước 2: Chuyển về format cho Chart.js
    const labels = Object.keys(mergedData);
    const data = Object.values(mergedData);
    
    console.log("Merged data:", mergedData); // Debug log
    
    renderChart(labels, data);
  } catch (e) {
    console.error("Lỗi load thống kê:", e);
  }
};

// Hàm vẽ biểu đồ
const renderChart = (labels, data) => {
  if (chartInstance) {
    chartInstance.destroy();
  }
  
  // Map màu sắc theo trạng thái chuẩn
  const colorMap = {
    "Hoàn thành": "#16a34a",      // xanh lá
    "Đang giao": "#3b82f6",       // xanh dương  
    "Đã xác nhận": "#eab308",     // vàng
    "Đã hủy": "#ef4444",          // đỏ
    "Chờ xử lý": "#6b7280",       // xám
    "Đang xử lý": "#f97316",      // cam
    "Hoàn trả": "#8b5cf6"         // tím
  };
  
  const backgroundColor = labels.map(label => 
    colorMap[label] || `hsl(${Math.random() * 360}, 70%, 60%)`
  );

  chartInstance = new Chart(chartRef.value, {
    type: "pie",
    data: {
      labels,
      datasets: [
        {
          data,
          backgroundColor,
          borderColor: "#fff",
          borderWidth: 2,
          hoverOffset: 12,
          hoverBorderWidth: 3,
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: "bottom",
          labels: {
            font: { size: 14, weight: '500' },
            color: "#374151",
            padding: 15,
            usePointStyle: true
          }
        },
        tooltip: {
          backgroundColor: "#111827",
          titleColor: "#facc15",
          bodyColor: "#f9fafb",
          borderColor: "#6b7280",
          borderWidth: 1,
          cornerRadius: 8,
          padding: 12,
          callbacks: {
            label: (context) => {
              const total = context.dataset.data.reduce((a, b) => a + b, 0);
              const percentage = ((context.raw / total) * 100).toFixed(1);
              return ` ${context.label}: ${context.raw} đơn (${percentage}%)`;
            }
          }
        }
      },
      animation: {
        animateScale: true,
        animateRotate: true,
        duration: 1000
      }
    }
  });
};

onMounted(fetchData);
</script>

<template>
  <div class="bg-white rounded-2xl shadow-lg border border-gray-200 p-6">
    <h3 class="text-lg font-bold text-gray-800 mb-6">📦 Trạng Thái Đơn Hàng</h3>
    <div class="flex justify-center">
      <!-- Khung chứa biểu đồ -->
      <div style="width: 350px; height: 320px;">
        <canvas ref="chartRef"></canvas>
      </div>
    </div>
  </div>
</template>

