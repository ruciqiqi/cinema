<template>
  <canvas ref="canvasRef" width="300" height="200" class="chart-bar" />
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
    default: '#4a90d9',
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
    .map(([label, value]) => ({ label, value: Number(value) || 0 }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 8)

  cancelAnimationFrame(draw._raf || 0)
  draw._raf = requestAnimationFrame(() => {
    ctx.clearRect(0, 0, W, H)

    // Use CSS variable values — read from document styles as fallback
    const bg = getCssVar('--bg', '#1a1a2e')
    const textColor = getCssVar('--text', '#eee')
    const text3 = getCssVar('--text3', '#666')
    const border = getCssVar('--border', '#333')

    ctx.fillStyle = bg

    if (entries.length === 0) {
      ctx.fillStyle = text3
      ctx.font = '13px sans-serif'
      ctx.textAlign = 'center'
      ctx.fillText('暂无数据', W / 2, H / 2)
      return
    }

    const maxVal = Math.max(...entries.map(e => e.value), 1)
    const padLeft = 8
    const padRight = 8
    const padTop = 12
    const padBottom = 30
    const chartW = W - padLeft - padRight
    const chartH = H - padTop - padBottom
    const barGap = 6
    const barW = (chartW - barGap * (entries.length - 1)) / entries.length

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

    // Bars
    entries.forEach((entry, i) => {
      const x = padLeft + i * (barW + barGap)
      const barH = (entry.value / maxVal) * chartH
      const y = padTop + chartH - barH

      const gradient = ctx.createLinearGradient(x, y, x, padTop + chartH)
      gradient.addColorStop(0, props.color)
      gradient.addColorStop(1, props.color + '44')
      ctx.fillStyle = gradient
      ctx.fillRect(x, y, barW, barH)

      // Value on top
      ctx.fillStyle = textColor
      ctx.font = '10px sans-serif'
      ctx.textAlign = 'center'
      ctx.fillText(entry.value, x + barW / 2, y - 4)
    })

    // X-axis labels
    ctx.fillStyle = text3
    ctx.font = '10px sans-serif'
    ctx.textAlign = 'center'
    entries.forEach((entry, i) => {
      const x = padLeft + i * (barW + barGap)
      const label = entry.label.length > 6 ? entry.label.slice(0, 6) + '…' : entry.label
      ctx.fillText(label, x + barW / 2, H - 6)
    })
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
.chart-bar {
  display: block;
  background: var(--surface);
  border-radius: var(--radius, 8px);
}
</style>
