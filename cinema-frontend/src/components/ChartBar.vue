<template>
  <div class="chart-bar-wrap" ref="wrapRef">
    <canvas ref="canvasRef" class="chart-bar" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'

const props = defineProps({
  data: { type: Object, required: true },
  color: { type: String, default: '#4a90d9' },
})

const canvasRef = ref(null)
const wrapRef = ref(null)

function draw() {
  nextTick(() => {
    const canvas = canvasRef.value
    const wrap = wrapRef.value
    if (!canvas || !wrap) return
    const dpr = window.devicePixelRatio || 1
    const W = wrap.clientWidth
    const H = 240
    canvas.width = W * dpr
    canvas.height = H * dpr
    canvas.style.width = W + 'px'
    canvas.style.height = H + 'px'
    const ctx = canvas.getContext('2d')
    ctx.scale(dpr, dpr)

    const entries = Object.entries(props.data || {})
      .map(([label, value]) => ({ label, value: Number(value) || 0 }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 10)

    ctx.clearRect(0, 0, W, H)

    const textColor = getCss('--text', '#333')
    const text3 = getCss('--text3', '#888')
    const border = getCss('--border', '#ddd')
    const surface = getCss('--surface', '#fff')

    ctx.fillStyle = surface
    ctx.fillRect(0, 0, W, H)

    if (entries.length === 0) {
      ctx.fillStyle = text3; ctx.font = '13px sans-serif'; ctx.textAlign = 'center'
      ctx.fillText('暂无数据', W / 2, H / 2)
      return
    }

    const padL = 8, padR = 8, padT = 16, padB = 28
    const chartW = W - padL - padR
    const chartH = H - padT - padB
    const barGap = 6
    const barW = Math.max(10, (chartW - barGap * (entries.length - 1)) / entries.length)
    const maxVal = Math.max(...entries.map(e => e.value), 1)

    // Grid
    const gridLines = 4
    ctx.strokeStyle = border; ctx.lineWidth = 0.5
    for (let i = 0; i <= gridLines; i++) {
      const y = padT + (chartH / gridLines) * i
      ctx.beginPath(); ctx.moveTo(padL, y); ctx.lineTo(W - padR, y); ctx.stroke()
    }

    // Bars
    entries.forEach((entry, i) => {
      const x = padL + i * (barW + barGap)
      const barH = (entry.value / maxVal) * chartH
      const y = padT + chartH - barH

      ctx.fillStyle = props.color
      const r = Math.min(4, barW / 2)
      ctx.beginPath()
      ctx.moveTo(x + r, y)
      ctx.lineTo(x + barW - r, y)
      ctx.quadraticCurveTo(x + barW, y, x + barW, y + r)
      ctx.lineTo(x + barW, y + barH)
      ctx.lineTo(x, y + barH)
      ctx.lineTo(x, y + r)
      ctx.quadraticCurveTo(x, y, x + r, y)
      ctx.closePath()
      ctx.fill()

      ctx.fillStyle = textColor; ctx.font = '10px sans-serif'; ctx.textAlign = 'center'
      ctx.fillText(formatNum(entry.value), x + barW / 2, y - 4)
    })

    // Labels
    ctx.fillStyle = text3; ctx.font = '10px sans-serif'; ctx.textAlign = 'center'
    entries.forEach((entry, i) => {
      const x = padL + i * (barW + barGap)
      const label = entry.label.length > 8 ? entry.label.slice(0, 8) + '…' : entry.label
      ctx.fillText(label, x + barW / 2, H - 6)
    })
  })
}

function getCss(name, fallback) {
  if (typeof window === 'undefined') return fallback
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim() || fallback
}

function formatNum(n) {
  if (n >= 1e8) return (n / 1e8).toFixed(1) + '亿'
  if (n >= 1e4) return (n / 1e4).toFixed(1) + '万'
  return String(Math.round(n))
}

onMounted(draw)
watch(() => props.data, draw, { deep: true })
</script>

<style scoped>
.chart-bar-wrap {
  width: 100%;
  min-height: 240px;
}
.chart-bar {
  display: block;
  width: 100%;
}
</style>
