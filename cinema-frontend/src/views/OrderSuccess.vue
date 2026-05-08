<script setup>
import { ref, onMounted } from 'vue'
import { useCartStore } from '../stores/cart'

const cart = useCartStore()
const qrCanvas = ref(null)

onMounted(() => {
  if (qrCanvas.value) drawQR()
})

function drawQR() {
  const c = qrCanvas.value
  if (!c) return
  const ctx = c.getContext('2d'), size = 150, modules = 21, ms = Math.floor(size / modules)
  ctx.fillStyle = '#fff'; ctx.fillRect(0, 0, size, size); ctx.fillStyle = '#000'
  let hash = 0
  const text = cart.currentShowtime?.movieTitle || 'CINEMA'
  for (let i = 0; i < text.length; i++) { hash = ((hash << 5) - hash) + text.charCodeAt(i); hash |= 0 }
  for (let r = 0; r < modules; r++) {
    for (let col = 0; col < modules; col++) {
      if ((hash * (r * 7 + col * 13 + 1)) & 1) ctx.fillRect(col * ms, r * ms, ms, ms)
    }
  }
  // Finder patterns
  function drawFinder(x, y) {
    ctx.fillStyle = '#000'; ctx.fillRect(x, y, 7*ms, 7*ms)
    ctx.fillStyle = '#fff'; ctx.fillRect(x+ms, y+ms, 5*ms, 5*ms)
    ctx.fillStyle = '#000'; ctx.fillRect(x+2*ms, y+2*ms, 3*ms, 3*ms)
  }
  drawFinder(0, 0)
  drawFinder((modules-7)*ms, 0)
  drawFinder(0, (modules-7)*ms)
}

function formatMoney(n) {
  return n ? n.toFixed(2) : '0.00'
}
</script>

<template>
  <div class="success-container">
    <div class="icon">&#9989;</div>
    <h3>支付成功！</h3>
    <p style="color:var(--text3);margin-bottom:12px;">请凭取票码到影院自助取票</p>
    <div class="code">{{ cart.orderTotal || 'CIN000000' }}</div>
    <div class="qr-code"><canvas ref="qrCanvas" width="150" height="150"></canvas></div>
    <div class="info" v-if="cart.currentShowtime">
      <div class="summary-line"><span class="l">影片</span><span>{{ cart.currentShowtime.movieTitle }}</span></div>
      <div class="summary-line"><span class="l">影厅</span><span>{{ cart.currentShowtime.hallName }}</span></div>
      <div class="summary-line"><span class="l">场次</span><span>{{ cart.currentShowtime.showDate }} {{ cart.currentShowtime.showTime }}</span></div>
      <div class="summary-line"><span class="l">座位</span><span>{{ cart.selectedSeats.map(s => s.rowLabel + '排' + s.seatNum + '座').join('、') }}</span></div>
      <div class="summary-line"><span class="l">金额</span><span style="color:var(--gold);font-weight:700;">&yen;{{ formatMoney(cart.calcFinalTotal()) }}</span></div>
    </div>
    <button class="btn btn-primary btn-block" @click="$router.push('/')">返回首页</button>
    <button class="btn btn-outline btn-block" @click="$router.push('/my/orders')">查看我的订单</button>
    <button class="btn btn-outline btn-block" @click="$router.push('/order/lookup')">查询订单</button>
  </div>
</template>

<style scoped>
.success-container { max-width: 500px; margin: 0 auto; background: var(--surface); border-radius: var(--radius); padding: 30px 24px; text-align: center; border: 1px solid var(--border); }
.success-container .icon { font-size: 60px; margin-bottom: 14px; }
.success-container h3 { color: var(--success); font-size: 22px; margin-bottom: 12px; }
.code { font-size: 28px; font-weight: 700; color: var(--gold); letter-spacing: 4px; margin-bottom: 16px; padding: 10px; background: var(--surface2); border-radius: 8px; border: 1px dashed var(--gold); display: inline-block; }
.qr-code { margin: 16px auto; padding: 12px; background: #fff; display: inline-block; border-radius: 8px; }
.info { text-align: left; background: var(--surface2); border-radius: 8px; padding: 14px; margin-bottom: 18px; font-size: 14px; line-height: 2; }
.summary-line { display: flex; justify-content: space-between; }
.summary-line .l { color: var(--text3); }
</style>
