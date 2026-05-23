<template>
  <div class="chart-line-wrap" ref="wrapRef">
    <canvas ref="canvasRef" class="chart-line" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'

const props = defineProps({
  data: { type: Object, required: true },
  color: { type: String, default: '#ff6b6b' },
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
      .map(([date, value]) => ({ date, value: Number(value) || 0 }))
      .sort((a, b) => a.date.localeCompare(b.date))

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

    const padL = 44, padR = 16, padT = 16, padB = 28
    const chartW = W - padL - padR
    const chartH = H - padT - padB
    const maxVal = Math.max(...entries.map(e => e.value), 1)

    // Grid
    const gridLines = 4
    ctx.strokeStyle = border; ctx.lineWidth = 0.5
    for (let i = 0; i <= gridLines; i++) {
      const y = padT + (chartH / gridLines) * i
      ctx.beginPath(); ctx.moveTo(padL, y); ctx.lineTo(W - padR, y); ctx.stroke()
    }

    if (entries.length === 1) {
      const x = padL + chartW / 2
      const y = padT + chartH - (entries[0].value / maxVal) * chartH
      ctx.fillStyle = props.color; ctx.beginPath(); ctx.arc(x, y, 4, 0, Math.PI * 2); ctx.fill()
      ctx.fillStyle = textColor; ctx.font = '11px sans-serif'; ctx.textAlign = 'center'
      ctx.fillText(formatNum(entries[0].value), x, y - 8)
      ctx.fillStyle = text3; ctx.font = '10px sans-serif'
      ctx.fillText(entries[0].date, x, H - 6)
      return
    }

    const points = entries.map((entry, i) => ({
      x: padL + (i / (entries.length - 1)) * chartW,
      y: padT + chartH - (entry.value / maxVal) * chartH,
      ...entry,
    }))

    // Area fill
    const grad = ctx.createLinearGradient(0, padT, 0, padT + chartH)
    grad.addColorStop(0, props.color + '33')
    grad.addColorStop(1, props.color + '05')
    ctx.fillStyle = grad
    ctx.beginPath()
    ctx.moveTo(points[0].x, padT + chartH)
    points.forEach(p => ctx.lineTo(p.x, p.y))
    ctx.lineTo(points[points.length - 1].x, padT + chartH)
    ctx.closePath(); ctx.fill()

    // Line
    ctx.strokeStyle = props.color; ctx.lineWidth = 2; ctx.lineJoin = 'round'; ctx.lineCap = 'round'
    ctx.beginPath(); ctx.moveTo(points[0].x, points[0].y)
    points.forEach(p => ctx.lineTo(p.x, p.y))
    ctx.stroke()

    // Dots
    points.forEach(p => {
      ctx.fillStyle = props.color; ctx.beginPath(); ctx.arc(p.x, p.y, 3.5, 0, Math.PI * 2); ctx.fill()
      ctx.fillStyle = surface; ctx.beginPath(); ctx.arc(p.x, p.y, 1.5, 0, Math.PI * 2); ctx.fill()
      ctx.fillStyle = textColor; ctx.font = '10px sans-serif'; ctx.textAlign = 'center'
      ctx.fillText(formatNum(p.value), p.x, p.y - 8)
    })

    // X labels
    ctx.fillStyle = text3; ctx.font = '10px sans-serif'; ctx.textAlign = 'center'
    const step = Math.max(1, Math.floor((entries.length - 1) / 6))
    for (let i = 0; i < entries.length; i += step) {
      ctx.fillText(points[i].date, points[i].x, H - 6)
    }
    if ((entries.length - 1) % step !== 0 && entries.length > 1) {
      const last = points[points.length - 1]
      ctx.fillText(last.date, last.x, H - 6)
    }
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
.chart-line-wrap {
  width: 100%;
  min-height: 240px;
}
.chart-line {
  display: block;
  width: 100%;
}
</style>
