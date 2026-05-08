<template>
  <canvas ref="canvasRef" width="600" height="200" class="chart-line" />
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  data: {
    type: Object,
    required: true,
  },
  color: {
    type: String,
    default: '#ff6b6b',
  },
})

const canvasRef = ref(null)

function draw() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const W = canvas.width
  const H = canvas.height

  const entries = Object.entries(props.data || {})
    .map(([date, value]) => ({ date, value: Number(value) || 0 }))
    .sort((a, b) => a.date.localeCompare(b.date))

  cancelAnimationFrame(draw._raf || 0)
  draw._raf = requestAnimationFrame(() => {
    ctx.clearRect(0, 0, W, H)

    const textColor = getCssVar('--text', '#eee')
    const text3 = getCssVar('--text3', '#666')
    const border = getCssVar('--border', '#333')

    if (entries.length === 0) {
      ctx.fillStyle = text3
      ctx.font = '13px sans-serif'
      ctx.textAlign = 'center'
      ctx.fillText('暂无数据', W / 2, H / 2)
      return
    }

    const padLeft = 36
    const padRight = 16
    const padTop = 16
    const padBottom = 28
    const chartW = W - padLeft - padRight
    const chartH = H - padTop - padBottom

    const maxVal = Math.max(...entries.map(e => e.value), 1)

    // Grid lines
    const gridLines = 4
    ctx.strokeStyle = border
    ctx.lineWidth = 0.5
    for (let i = 0; i <= gridLines; i++) {
      const y = padTop + (chartH / gridLines) * i
      ctx.beginPath()
      ctx.moveTo(padLeft, y)
      ctx.lineTo(W - padRight, y)
      ctx.stroke()
    }

    if (entries.length === 1) {
      // Single point — draw a dot only
      const x = padLeft + chartW / 2
      const y = padTop + chartH - (entries[0].value / maxVal) * chartH

      ctx.fillStyle = props.color
      ctx.beginPath()
      ctx.arc(x, y, 4, 0, Math.PI * 2)
      ctx.fill()

      ctx.fillStyle = textColor
      ctx.font = '11px sans-serif'
      ctx.textAlign = 'center'
      ctx.fillText(entries[0].value, x, y - 8)

      ctx.fillStyle = text3
      ctx.font = '10px sans-serif'
      ctx.fillText(entries[0].date, x, H - 6)
      return
    }

    // Build point coordinates
    const points = entries.map((entry, i) => ({
      x: padLeft + (i / (entries.length - 1)) * chartW,
      y: padTop + chartH - (entry.value / maxVal) * chartH,
      ...entry,
    }))

    // Area fill
    const areaGrad = ctx.createLinearGradient(0, padTop, 0, padTop + chartH)
    areaGrad.addColorStop(0, props.color + '33')
    areaGrad.addColorStop(1, props.color + '05')
    ctx.fillStyle = areaGrad
    ctx.beginPath()
    ctx.moveTo(points[0].x, padTop + chartH)
    points.forEach(p => ctx.lineTo(p.x, p.y))
    ctx.lineTo(points[points.length - 1].x, padTop + chartH)
    ctx.closePath()
    ctx.fill()

    // Line
    ctx.strokeStyle = props.color
    ctx.lineWidth = 2
    ctx.lineJoin = 'round'
    ctx.lineCap = 'round'
    ctx.beginPath()
    ctx.moveTo(points[0].x, points[0].y)
    points.forEach(p => ctx.lineTo(p.x, p.y))
    ctx.stroke()

    // Dots and values
    points.forEach(p => {
      // Dot
      ctx.fillStyle = props.color
      ctx.beginPath()
      ctx.arc(p.x, p.y, 3.5, 0, Math.PI * 2)
      ctx.fill()

      // White center
      ctx.fillStyle = '#fff'
      ctx.beginPath()
      ctx.arc(p.x, p.y, 1.5, 0, Math.PI * 2)
      ctx.fill()

      // Value label
      ctx.fillStyle = textColor
      ctx.font = '10px sans-serif'
      ctx.textAlign = 'center'
      ctx.fillText(p.value, p.x, p.y - 8)
    })

    // X-axis labels (show at most 8 evenly spaced)
    ctx.fillStyle = text3
    ctx.font = '10px sans-serif'
    ctx.textAlign = 'center'
    const step = Math.max(1, Math.floor((entries.length - 1) / 7))
    for (let i = 0; i < entries.length; i += step) {
      const p = points[i]
      ctx.fillText(p.date, p.x, H - 6)
    }
    // Always show last if not covered by step
    if ((entries.length - 1) % step !== 0 && entries.length > 1) {
      const last = points[points.length - 1]
      ctx.fillText(last.date, last.x, H - 6)
    }
  })
}

function getCssVar(name, fallback) {
  if (typeof window === 'undefined') return fallback
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim() || fallback
}

onMounted(draw)
watch(() => props.data, draw, { deep: true })
watch(() => props.color, draw)
</script>

<style scoped>
.chart-line {
  display: block;
  background: var(--surface);
  border-radius: var(--radius, 8px);
}
</style>
