<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../api'
import ChartBar from '../components/ChartBar.vue'
import ChartLine from '../components/ChartLine.vue'
import ChartPie from '../components/ChartPie.vue'

const stats = ref(null)
const revenueData = ref({})

function formatRevenue(v) {
  if (v >= 1e8) return (v / 1e8).toFixed(1)
  if (v >= 1e4) return (v / 1e4).toFixed(1)
  return v.toFixed(2)
}

const revenueUnit = computed(() => {
  const v = stats.value?.revenue || 0
  if (v >= 1e8) return '亿'
  if (v >= 1e4) return '万'
  return '元'
})

onMounted(async () => {
  try {
    const [overview, revenue] = await Promise.all([
      api.get('/admin/stats/overview'),
      api.get('/admin/stats/revenue')
    ])
    if (overview.data.success) stats.value = overview.data
    if (revenue.data.success) revenueData.value = revenue.data
  } catch (e) { console.error(e) }
})
</script>

<template>
  <div class="bigscreen-container">
    <div class="bigscreen-header">
      <h2>数据可视化大屏</h2>
      <p>{{ new Date().toLocaleDateString() }} | 实时监控</p>
    </div>
    <div class="bigscreen-grid" v-if="stats">
      <div class="bs-card bs-metric">
        <h4>总营收</h4>
        <div class="bs-value">&yen;{{ formatRevenue(stats.revenue || 0) }}<span class="bs-unit">{{ revenueUnit }}</span></div>
      </div>
      <div class="bs-card bs-metric">
        <h4>订单总数</h4>
        <div class="bs-value">{{ stats.bookingCount || 0 }}<span class="bs-unit">单</span></div>
        <div class="bs-sub">已确认 {{ stats.confirmedBookings || 0 }} 单</div>
      </div>
      <div class="bs-card bs-metric">
        <h4>用户总数</h4>
        <div class="bs-value">{{ stats.userCount || 0 }}<span class="bs-unit">人</span></div>
      </div>
      <div class="bs-card bs-metric">
        <h4>影片 & 场次</h4>
        <div class="bs-value">{{ stats.movieCount || 0 }}<span class="bs-unit">部</span></div>
        <div class="bs-sub">共 {{ stats.showtimeCount || 0 }} 场次</div>
      </div>

      <div class="bs-card bs-chart bs-full">
        <h4>影片票房排行 Top 10</h4>
        <ChartBar :data="stats.movieRevenue || {}" color="#e54847" />
      </div>

      <div class="bs-card bs-chart bs-half">
        <h4>每日营收趋势</h4>
        <ChartLine :data="revenueData.byDate || {}" color="#e54847" />
      </div>

      <div class="bs-card bs-chart bs-half">
        <h4>类型分布</h4>
        <ChartPie :data="stats.genreDistribution || {}" />
      </div>

      <div class="bs-card bs-chart bs-half">
        <h4>热门排行</h4>
        <div class="rank-list">
          <div v-for="([title, count], i) in Object.entries(stats.topMovies||{}).slice(0,6)" :key="title" class="rank-item">
            <span class="rank-num" :class="'rank-' + (i+1)">{{ i+1 }}</span>
            <span class="rank-name">{{ title }}</span>
            <span class="rank-val">{{ count }} 单</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bigscreen-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px 24px;
  color: var(--text, #333);
}

.bigscreen-header {
  text-align: center;
  margin-bottom: 24px;
}

.bigscreen-header h2 {
  font-size: 28px;
  color: var(--primary, #e54847);
  margin-bottom: 4px;
}

.bigscreen-header p {
  font-size: 13px;
  color: var(--text3, #999);
}

.bigscreen-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  max-width: 1400px;
  margin: 0 auto;
}

.bs-card {
  background: #fff;
  border: 1px solid var(--border, #e8e8e8);
  border-radius: 10px;
  padding: 18px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.bs-metric {
  text-align: center;
}

.bs-metric h4 {
  font-size: 13px;
  color: var(--text3, #999);
  margin-bottom: 8px;
}

.bs-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--primary, #e54847);
}

.bs-unit {
  font-size: 14px;
  color: var(--text3, #999);
  margin-left: 4px;
}

.bs-sub {
  font-size: 12px;
  color: var(--text3, #999);
  margin-top: 4px;
}

.bs-chart {
  display: flex;
  flex-direction: column;
}

.bs-chart h4 {
  font-size: 14px;
  color: var(--text, #333);
  margin-bottom: 12px;
}

.bs-full {
  grid-column: span 4;
}

.bs-half {
  grid-column: span 2;
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rank-num {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: #bbb;
}

.rank-1 { background: #e54847; }
.rank-2 { background: #f9b418; }
.rank-3 { background: #1890ff; }

.rank-name {
  flex: 1;
  font-size: 13px;
  color: var(--text, #333);
}

.rank-val {
  font-size: 13px;
  color: var(--primary, #e54847);
  font-weight: 600;
}

@media (max-width: 768px) {
  .bigscreen-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .bs-full { grid-column: span 2; }
  .bs-half { grid-column: span 2; }
}
</style>
