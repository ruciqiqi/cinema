<template>
  <div class="chart-pie-wrap" ref="wrapRef">
    <canvas ref="canvasRef" class="chart-pie" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'

const props = defineProps({
  data: { type: Object, required: true },
})

const canvasRef = ref(null)
const wrapRef = ref(null)
const palette = ['#ff5e5e','#4a9eff','#2ecc71','#ffc107','#9b59b6','#e67e22','#1abc9c','#e74c3c','#3498db','#f39c12']

function draw() {
  nextTick(() => {
    const canvas = canvasRef.value
    const wrap = wrapRef.value
    if (!canvas || !wrap) return
    const dpr = window.devicePixelRatio || 1
    const W = wrap.clientWidth
    const H = 260
    canvas.width = W * dpr
    canvas.height = H * dpr
    canvas.style.width = W + 'px'
    canvas.style.height = H + 'px'
    const ctx = canvas.getContext('2d')
    ctx.scale(dpr, dpr)

    const entries = Object.entries(props.data || {})
      .map(([label, value]) => ({ label, value: Number(value) || 0 }))
      .filter(e => e.value > 0)
      .sort((a, b) => b.value - a.value)

    const textColor = getCss('--text', '#333')
    const text3 = getCss('--text3', '#888')
    const surface = getCss('--surface', '#fff')

    ctx.fillStyle = surface
    ctx.fillRect(0, 0, W, H)

    if (entries.length === 0) {
      ctx.fillStyle = text3; ctx.font = '13px sans-serif'; ctx.textAlign = 'center'
      ctx.fillText('暂无数据', W / 2, H / 2)
      return
    }

    const total = entries.reduce((sum, e) => sum + e.value, 0)
    const cx = Math.min(100, W * 0.3)
    const cy = H / 2
    const outerR = Math.min(80, cx - 10)
    const innerR = 32

    // Slices
    let angle = -Math.PI / 2
    entries.forEach((entry, i) => {
      const slice = (entry.value / total) * Math.PI * 2
      ctx.fillStyle = palette[i % palette.length]
      ctx.beginPath()
      ctx.arc(cx, cy, outerR, angle, angle + slice)
      ctx.arc(cx, cy, innerR, angle + slice, angle, true)
      ctx.closePath()
      ctx.fill()
      angle += slice
    })

    // Center text
    const bg = getCss('--surface2', '#f5f5f5')
    ctx.fillStyle = bg; ctx.beginPath(); ctx.arc(cx, cy, innerR, 0, Math.PI * 2); ctx.fill()
    ctx.fillStyle = textColor; ctx.font = 'bold 14px sans-serif'; ctx.textAlign = 'center'; ctx.textBaseline = 'middle'
    ctx.fillText(total, cx, cy - 3)
    ctx.fillStyle = text3; ctx.font = '10px sans-serif'; ctx.textBaseline = 'top'
    ctx.fillText('合计', cx, cy + 4)

    // Legend
    const legendX = cx + outerR + 20
    let ly = Math.max(10, cy - (entries.length * 22) / 2)
    ctx.textBaseline = 'middle'
    entries.forEach((e, i) => {
      const y = ly + i * 22
      if (y + 11 > H) return
      ctx.fillStyle = palette[i % palette.length]
      ctx.fillRect(legendX, y - 5, 12, 12)
      ctx.fillStyle = text3; ctx.font = '11px sans-serif'; ctx.textAlign = 'left'
      const pct = total > 0 ? ((e.value / total) * 100).toFixed(1) + '%' : '0%'
      const label = e.label.length > 6 ? e.label.slice(0, 6) + '…' : e.label
      ctx.fillText(label + ' ' + pct, legendX + 16, y + 1)
    })
  })
}

function getCss(name, fallback) {
  if (typeof window === 'undefined') return fallback
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim() || fallback
}

onMounted(draw)
watch(() => props.data, draw, { deep: true })
</script>

<style scoped>
.chart-pie-wrap {
  width: 100%;
  min-height: 260px;
}
.chart-pie {
  display: block;
  width: 100%;
}
</style>
