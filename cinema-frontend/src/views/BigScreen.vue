<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const stats = ref(null)
const revenueData = ref({})

onMounted(async () => {
  try {
    const [overview, revenue] = await Promise.all([
      api.get('/admin/stats/overview'),
      api.get('/admin/stats/revenue')
    ])
    if (overview.data.success) stats.value = overview.data
    if (revenue.data.success) revenueData.value = revenue.data
    setTimeout(() => {
      drawBigBar()
      drawBigLine()
      drawBigPie()
    }, 300)
  } catch (e) { console.error(e) }
})

function drawBigBar() {
  const c = document.getElementById('bsBigBar')
  if (!c) return
  const ctx = c.getContext('2d')
  const w = c.parentElement.clientWidth - 32, h = 400
  c.width = w; c.height = h
  const data = revenueData.value.byMovie || {}
  const entries = Object.entries(data).sort((a, b) => b[1] - a[1]).slice(0, 10)
  if (!entries.length) return
  const max = entries[0][1]
  const barW = Math.max(14, (w - 100) / entries.length - 12)
  const grad = ctx.createLinearGradient(0, h - 40, 0, 0)
  grad.addColorStop(0, 'rgba(229,72,71,0.2)')
  grad.addColorStop(1, 'rgba(229,72,71,0.9)')
  entries.forEach((e, i) => {
    const barH = (e[1] / max) * (h - 60)
    const x = 60 + i * (barW + 12), y = h - 30 - barH
    ctx.fillStyle = 'rgba(229,72,71,0.1)'
    ctx.fillRect(x - 2, y - 2, barW + 4, barH + 4)
    ctx.fillStyle = grad
    ctx.fillRect(x, y, barW, barH)
    ctx.fillStyle = '#1a1a1a'
    ctx.font = 'bold 11px sans-serif'
    ctx.textAlign = 'center'
    const val = e[1] >= 1e8 ? (e[1]/1e8).toFixed(1)+'亿' : (e[1]/1e4).toFixed(0)+'万'
    ctx.fillText(val, x + barW/2, y - 6)
    ctx.fillStyle = '#666'
    ctx.font = '11px sans-serif'
    ctx.save()
    ctx.translate(x + barW/2, h - 10)
    ctx.rotate(-0.5)
    ctx.fillText(e[0].substring(0, 8), 0, 0)
    ctx.restore()
  })
}

function drawBigLine() {
  const c = document.getElementById('bsBigLine')
  if (!c) return
  const ctx = c.getContext('2d')
  const w = c.parentElement.clientWidth - 32, h = 400
  c.width = w; c.height = h
  const data = revenueData.value.byDate || {}
  const entries = Object.entries(data).sort((a, b) => a[0].localeCompare(b[0]))
  if (!entries.length) return
  const vals = entries.map(e => e[1])
  const max = Math.max(...vals), min = Math.min(...vals)
  const range = max - min || 1
  ctx.beginPath()
  entries.forEach((e, i) => {
    const x = 50 + (i / (entries.length - 1 || 1)) * (w - 100)
    const y = h - 30 - ((e[1] - min) / range) * (h - 70)
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  })
  entries.toReversed().forEach((e, i) => {
    const x = 50 + ((entries.length - 1 - i) / (entries.length - 1 || 1)) * (w - 100)
    ctx.lineTo(x, h - 30)
  })
  ctx.closePath()
  const grad = ctx.createLinearGradient(0, 0, 0, h)
  grad.addColorStop(0, 'rgba(229,72,71,0.15)')
  grad.addColorStop(1, 'rgba(229,72,71,0.01)')
  ctx.fillStyle = grad; ctx.fill()
  ctx.beginPath()
  ctx.strokeStyle = '#e54847'; ctx.lineWidth = 3
  entries.forEach((e, i) => {
    const x = 50 + (i / (entries.length - 1 || 1)) * (w - 100)
    const y = h - 30 - ((e[1] - min) / range) * (h - 70)
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  })
  ctx.stroke()
  entries.forEach((e, i) => {
    const x = 50 + (i / (entries.length - 1 || 1)) * (w - 100)
    const y = h - 30 - ((e[1] - min) / range) * (h - 70)
    ctx.fillStyle = '#e54847'; ctx.beginPath(); ctx.arc(x, y, 4, 0, Math.PI * 2); ctx.fill()
    ctx.fillStyle = '#fff'; ctx.beginPath(); ctx.arc(x, y, 2, 0, Math.PI * 2); ctx.fill()
    ctx.fillStyle = '#999'; ctx.font = '10px sans-serif'; ctx.textAlign = 'center'
    const val = e[1] >= 1e4 ? (e[1]/1e4).toFixed(1)+'万' : e[1].toFixed(0)
    ctx.fillText(val, x, y - 10)
    if (i % Math.max(1, Math.floor(entries.length / 8)) === 0) {
      ctx.fillStyle = '#666'; ctx.font = '10px sans-serif'
      ctx.fillText(e[0], x, h - 8)
    }
  })
}

function drawBigPie() {
  const c = document.getElementById('bsBigPie')
  if (!c) return
  const ctx = c.getContext('2d')
  const w = c.parentElement.clientWidth - 32, h = 380
  c.width = w; c.height = h
  const data = stats.value?.genreDistribution || {}
  const entries = Object.entries(data)
  if (!entries.length) return
  const total = entries.reduce((s, e) => s + e[1], 0)
  const colors = ['#e54847','#1890ff','#52c41a','#f9b418','#722ed1','#fa8c16','#13c2c2','#eb2f96']
  const cx = 140, cy = h / 2, r = 110
  let angle = -Math.PI / 2
  entries.forEach((e, i) => {
    const slice = (e[1] / total) * Math.PI * 2
    const mid = angle + slice / 2
    const sx = cx + Math.cos(mid) * 4
    const sy = cy + Math.sin(mid) * 4
    ctx.fillStyle = colors[i % colors.length]
    ctx.beginPath(); ctx.moveTo(sx, sy)
    ctx.arc(sx, sy, r, angle, angle + slice); ctx.closePath(); ctx.fill()
    angle += slice
  })
  ctx.fillStyle = '#fff'; ctx.beginPath(); ctx.arc(cx, cy, 45, 0, Math.PI*2); ctx.fill()
  ctx.fillStyle = '#1a1a1a'; ctx.font = 'bold 17px sans-serif'; ctx.textAlign = 'center'; ctx.textBaseline = 'middle'
  ctx.fillText(total, cx, cy - 3)
  ctx.fillStyle = '#999'; ctx.font = '11px sans-serif'; ctx.textBaseline = 'top'
  ctx.fillText('部影片', cx, cy + 8)
  let legendY = 16
  ctx.textBaseline = 'middle'
  entries.forEach((e, i) => {
    const y = legendY + i * 24
    ctx.fillStyle = colors[i % colors.length]
    ctx.fillRect(290, y - 5, 13, 13)
    ctx.fillStyle = '#666'; ctx.font = '11px sans-serif'; ctx.textAlign = 'left'
    const pct = ((e[1]/total)*100).toFixed(1) + '%'
    ctx.fillText(e[0] + '  ' + pct, 310, y + 1)
  })
}
</script>

<template>
  <div class="bigscreen-container">
    <div class="bigscreen-header">
      <h2>🎬 猫眼电影 数据可视化大屏</h2>
      <p>{{ new Date().toLocaleDateString() }} | 实时监控</p>
    </div>
    <div class="bigscreen-grid" v-if="stats">
      <div class="bs-card bs-metric">
        <h4>总营收</h4>
        <div class="bs-value">&yen;{{ ((stats.revenue || 0)/1e4).toFixed(1) }}<span class="bs-unit">万</span></div>
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
        <div class="bs-sub">{{ stats.cinemaCount || 0 }} 家影院 · {{ stats.showtimeCount || 0 }} 场次</div>
      </div>

      <div class="bs-card bs-chart bs-full">
        <h4>🎬 影片票房排行 Top 10</h4>
        <canvas id="bsBigBar"></canvas>
      </div>

      <div class="bs-card bs-chart" style="grid-column:span 2;">
        <h4>📊 类型分布</h4>
        <canvas id="bsBigPie"></canvas>
      </div>
      <div class="bs-card bs-chart" style="grid-column:span 2;">
        <h4>🏆 影院排行</h4>
        <div class="rank-list">
          <div v-for="(v,k,i) in Object.entries(stats.topMovies||{}).slice(0,6)" :key="k" class="rank-item">
            <span class="rank-num" :class="'rank-' + (i+1)">{{ i+1 }}</span>
            <span class="rank-name">{{ k }}</span>
            <span class="rank-val">{{ v }} 单</span>
          </div>
        </div>
      </div>

      <div class="bs-card bs-chart bs-full">
        <h4>📈 每日营收趋势</h4>
        <canvas id="bsBigLine"></canvas>
      </div>
    </div>
    <button class="btn btn-outline" style="margin-top:24px;" @click="router.push('/admin')">返回后台</button>
  </div>
</template>

<style scoped>
.bigscreen-container {
  background: #f5f5f5;
  min-height: calc(100vh - 120px);
  padding: 36px 44px;
  border-radius: 12px;
}
.bigscreen-header { text-align: center; margin-bottom: 32px; }
.bigscreen-header h2 { font-size: 36px; color: var(--primary); letter-spacing: 4px; font-weight: 700; margin-bottom: 6px; }
.bigscreen-header p { color: var(--text3); font-size: 14px; letter-spacing: 2px; }
.bigscreen-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.bs-card {
  background: #fff;
  border-radius: 12px;
  padding: 26px 30px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.bs-card h4 { color: var(--text2); margin-bottom: 8px; font-size: 14px; font-weight: 600; letter-spacing: 1px; }
.bs-metric .bs-value { font-size: 48px; font-weight: 700; color: var(--primary); line-height: 1.15; margin: 12px 0; }
.bs-unit { font-size: 18px; font-weight: 400; color: var(--text2); margin-left: 4px; }
.bs-metric .bs-sub { font-size: 13px; color: var(--text3); margin-top: 8px; }
.bs-chart { min-height: 420px; }
.bs-chart canvas { width: 100%; }
.bs-full { grid-column: 1 / -1; }
.bs-full canvas { min-height: 380px; }

.rank-list { display: flex; flex-direction: column; gap: 12px; margin-top: 16px; }
.rank-item { display: flex; align-items: center; gap: 14px; padding: 12px 16px; background: #fafafa; border-radius: 8px; transition: all 0.2s; }
.rank-item:hover { background: #f0f0f0; transform: translateX(4px); }
.rank-num { width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 700; color: #fff; background: #d0d0d0; }
.rank-1 { background: linear-gradient(135deg, #ffd700, #ff9f43); }
.rank-2 { background: linear-gradient(135deg, #c0c0c0, #999); }
.rank-3 { background: linear-gradient(135deg, #cd7f32, #a0522d); }
.rank-name { flex: 1; font-size: 14px; color: var(--text); font-weight: 500; }
.rank-val { font-size: 14px; font-weight: 700; color: var(--primary); }

@media(max-width:1100px) { .bigscreen-grid { grid-template-columns: repeat(2, 1fr); } .bigscreen-container { padding: 20px; } }
@media(max-width:600px) { .bigscreen-grid { grid-template-columns: 1fr; } .bs-metric .bs-value { font-size: 40px; } .bigscreen-header h2 { font-size: 24px; } }
</style>
