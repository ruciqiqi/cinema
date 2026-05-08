<template>
  <canvas ref="canvasRef" width="360" height="260" class="chart-pie" />
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  data: { type: Object, required: true },
})

const canvasRef = ref(null)
const palette = ['#ff5e5e','#4a9eff','#2ecc71','#ffc107','#9b59b6','#e67e22','#1abc9c','#e74c3c']

function draw() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const W = canvas.width, H = canvas.height

  const entries = Object.entries(props.data || {})
    .map(([label, value]) => ({ label, value: Number(value) || 0 }))
    .filter(e => e.value > 0)

  ctx.clearRect(0, 0, W, H)

  if (entries.length === 0) {
    ctx.fillStyle = '#888'; ctx.font = '13px sans-serif'; ctx.textAlign = 'center'
    ctx.fillText('暂无数据', W / 2, H / 2)
    return
  }

  const total = entries.reduce((sum, e) => sum + e.value, 0)
  const cx = 100, cy = H / 2, outerR = 85, innerR = 35

  // Slices
  let angle = -Math.PI / 2
  entries.forEach((entry, i) => {
    const slice = (entry.value / total) * Math.PI * 2
    const color = palette[i % palette.length]
    // Draw donut slice
    ctx.fillStyle = color
    ctx.beginPath()
    ctx.arc(cx, cy, outerR, angle, angle + slice)
    ctx.arc(cx, cy, innerR, angle + slice, angle, true)
    ctx.closePath()
    ctx.fill()

    angle += slice
  })

  // Center
  ctx.fillStyle = '#12122a'; ctx.beginPath(); ctx.arc(cx, cy, innerR, 0, Math.PI * 2); ctx.fill()
  ctx.fillStyle = '#fff'; ctx.font = 'bold 15px sans-serif'; ctx.textAlign = 'center'; ctx.textBaseline = 'middle'
  ctx.fillText(total, cx, cy - 3)
  ctx.fillStyle = '#888'; ctx.font = '10px sans-serif'; ctx.textBaseline = 'top'
  ctx.fillText('影片数', cx, cy + 6)

  // Legend
  let ly = 14
  ctx.textBaseline = 'middle'
  entries.forEach((e, i) => {
    const y = ly + i * 22
    ctx.fillStyle = palette[i % palette.length]
    ctx.fillRect(230, y - 5, 12, 12)
    ctx.fillStyle = '#aaa'; ctx.font = '11px sans-serif'; ctx.textAlign = 'left'
    const pct = ((e.value/total)*100).toFixed(1) + '%'
    ctx.fillText(e.label + '  ' + pct, 248, y + 1)
  })
}

onMounted(draw)
watch(() => props.data, draw, { deep: true })
</script>

<style scoped>
.chart-pie { display: block; border-radius: 8px; }
</style>
