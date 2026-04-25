<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-7xl mx-auto">
      <!-- Header -->
      <DashboardHeader 
        :selectedPeriod="selectedPeriod"
        :loading="loading"
        :lastUpdated="lastUpdated"
        :apiStatus="apiStatus"
        @periodChange="handlePeriodChange"
        @refresh="handleRefresh"
      />

      <!-- Error Display -->
      <!-- <ErrorAlert 
        v-if="error && !dashboardData"
        :error="error"
        @retry="handleRefresh"
      /> -->

      <!-- Loading Spinner -->
      <LoadingSpinner v-if="loading && !dashboardData" />

      <!-- Dashboard Content -->
      <div v-else-if="dashboardData" class="space-y-8">

        <!-- KPI Cards -->
        <KPICards :stats="dashboardData.totalStats" />
        
        <!-- Charts Row 1 -->

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <DoanhThuTheoThang :data="dashboardData.doanhThuTheoThang" />

          <OrderStatusList :data="dashboardData.orderStatus" />

          <!-- <DoanhThuTheoThang :data="dashboardData.doanhThuTheoThang" /> -->
        </div>

        <!-- Charts Row 2 -->
         <!-- <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <RevenueChart :data="dashboardData.revenueData" />
          <BrandChart :data="dashboardData.brandAnalysis" />
        </div> -->

        <!-- Bottom Row -->
        <div class="grid grid-cols-1 lg:grid-cols-1 gap-6">
          <TopProductsList 
            :products="dashboardData.topProducts"
            :selectedCategory="selectedCategory"
            @categoryChange="setSelectedCategory"
          />
          
          <!-- <SizeAnalysisChart :data="dashboardData.sizeAnalysis" /> -->
          <!-- <QuickActions /> -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useDashboardAPI } from '@/composables/useDashboardAPI'

// Import components
import DashboardHeader from '@/components/dashboard/DashboardHeader.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import KPICards from '@/components/dashboard/KPICards.vue'
import TopProductsList from '@/components/dashboard/TopProductsList.vue'
import OrderStatusList from '@/components/dashboard/OrderStatusList.vue'
import SizeAnalysisChart from '@/components/dashboard/SizeAnalysisChart.vue'
import DoanhThuTheoThang from '@/components/dashboard/DoanhThuTheoThang.vue'

// Reactive data
const selectedPeriod = ref('30days')
const selectedCategory = ref('all')
const refreshInterval = ref(null)

// Use dashboard API composable
const {
  dashboardData,
  loading,
  error,
  lastUpdated,
  apiStatus,
  fetchDashboardData,
  checkApiHealth
} = useDashboardAPI()

// Methods
const handlePeriodChange = (period) => {
  selectedPeriod.value = period
  fetchDashboardData(period)
}

const handleRefresh = () => {
  fetchDashboardData(selectedPeriod.value)
}

const setSelectedCategory = (category) => {
  selectedCategory.value = category
}

// Lifecycle
onMounted(() => {
  fetchDashboardData(selectedPeriod.value)
  
  // Setup auto-refresh every 5 minutes
  refreshInterval.value = setInterval(() => {
    checkApiHealth()
    if (apiStatus.value === 'connected') {
      fetchDashboardData(selectedPeriod.value)
    }
  }, 300000)
})

onUnmounted(() => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
  }
})
</script>