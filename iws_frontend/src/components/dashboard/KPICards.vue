<template>
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
    <StatCard
      title="Tổng Doanh Thu"
      :value="formatCurrency(stats.totalRevenue)"
      :icon="DollarSignIcon"
      trend="up"
      color="green"
    />
    <StatCard
      title="Đơn Hàng"
      :value="formatNumber(stats.totalOrders)"
      :icon="ShoppingCartIcon"
      trend="up"
      color="blue"
    />
    <StatCard
      title="Khách Hàng"
      :value="formatNumber(stats.totalCustomers)"
      :icon="UsersIcon"
      trend="up"
      color="purple"
    />
    <StatCard
      title="Sản Phẩm"
      :value="formatNumber(stats.totalProducts)"
      :icon="PackageIcon"
      trend="up"
      color="orange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

import { 
  DollarSign as DollarSignIcon, 
  ShoppingCart as ShoppingCartIcon, 
  Users as UsersIcon, 
  Package as PackageIcon 
} from 'lucide-vue-next'

import StatCard from '@/components/common/StatCard.vue'
import { formatCurrency, formatNumber } from '@/utils/formatters'

// reactive state
const stats = ref({
  totalRevenue: 0,
  totalOrders: 0,
  totalCustomers: 0,
  totalProducts: 0
})

// fetch data from backend
const fetchStats = async () => {
  try {
    const res = await axios.get('http://localhost:8080/thong-ke')
    stats.value = res.data
  } catch (error) {
    console.error("Lỗi khi lấy dữ liệu thống kê:", error)
  }
}

onMounted(() => {
  fetchStats()
})
</script>
