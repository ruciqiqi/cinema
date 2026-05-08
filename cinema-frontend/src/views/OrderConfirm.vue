<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import api from '../api'
import CouponSelector from '../components/CouponSelector.vue'
import PaymentMethod from '../components/PaymentMethod.vue'

const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const toast = inject('toast')

if (!auth.isLoggedIn) {
  toast('请先登录后再购票', 'error')
  router.replace('/')
}

const userName = ref('')
const userPhone = ref('')
const proxyBuy = ref(false)
const friendPhone = ref('')
const submitting = ref(false)
const lastOrder = ref(null)

const seatTotal = computed(() => cart.calcSeatTotal())
const snackTotal = computed(() => cart.calcSnackTotal())
const finalTotal = computed(() => cart.calcFinalTotal())

function seatLines() {
  return cart.selectedSeats.map(s => {
    const price = s.seatType === 'vip' ? cart.currentShowtime.priceVip : cart.currentShowtime.priceStandard
    return `${s.rowLabel}排${s.seatNum}座 (${s.seatType === 'vip' ? 'VIP' : '标准'} ¥${price})`
  })
}

function snackLines() {
  return Object.values(cart.snackCart).filter(sc => sc.qty > 0).map(sc => `${sc.name} x${sc.qty} (¥${(sc.price * sc.qty).toFixed(1)})`)
}

function snacksJson() {
  return Object.entries(cart.snackCart).filter(([_, v]) => v.qty > 0).map(([k, v]) => `${k}:${v.qty}`).join(',')
}

async function submitOrder() {
  if (!userName.value.trim()) { toast('请输入姓名', 'error'); return }
  if (!/^1\d{10}$/.test(userPhone.value)) { toast('请输入正确的手机号', 'error'); return }
  submitting.value = true

  try {
    const body = {
      showtimeId: cart.currentShowtimeId,
      seatIds: cart.selectedSeats.map(s => s.id),
      userName: userName.value.trim(),
      userPhone: userPhone.value.trim(),
      snacksJson: snacksJson()
    }
    if (proxyBuy.value && friendPhone.value) body.friendPhone = friendPhone.value

    const bookingRes = await api.post('/bookings', body)
    if (!bookingRes.data.success) { toast(bookingRes.data.message || '下单失败', 'error'); submitting.value = false; return }

    // Process payment
    const payRes = await api.post('/bookings/pay', {
      bookingCode: bookingRes.data.bookingCode,
      paymentMethod: cart.paymentMethod
    })
    if (payRes.data.success) {
      lastOrder.value = {
        ...bookingRes.data,
        actualPaid: payRes.data.actualPaid,
        qrCode: payRes.data.qrCode,
        discount: cart.appliedCoupon?.discount || 0
      }
      router.push('/order/success')
    } else {
      toast('支付失败: ' + payRes.data.message, 'error')
    }
  } catch (e) {
    toast('下单失败', 'error')
  }
  submitting.value = false
}

// Pass order to success page
defineExpose({ lastOrder })
</script>

<template>
  <div class="booking-container">
    <h3>确认订单</h3>
    <div class="booking-summary" v-if="cart.currentShowtime">
      <div class="summary-line"><span class="l">影片</span><span>{{ cart.currentShowtime.movieTitle }}</span></div>
      <div class="summary-line"><span class="l">影厅</span><span>{{ cart.currentShowtime.hallName }}</span></div>
      <div class="summary-line"><span class="l">场次</span><span>{{ cart.currentShowtime.showDate }} {{ cart.currentShowtime.showTime }}</span></div>
      <div class="summary-line"><span class="l">座位</span><span>{{ seatLines().join('、') }}</span></div>
      <div v-if="snackLines().length" class="summary-line"><span class="l">卖品</span><span>{{ snackLines().join('、') }}</span></div>
      <div v-if="cart.appliedCoupon" class="summary-line"><span class="l">优惠</span><span style="color:#2ecc71;">-¥{{ cart.appliedCoupon.discount.toFixed(2) }}</span></div>
      <div class="summary-line total"><span class="l">实付</span><span>&yen;{{ finalTotal.toFixed(2) }}</span></div>
    </div>

    <CouponSelector v-if="auth.isLoggedIn" v-model="cart.appliedCoupon" :orderAmount="seatTotal" />
    <PaymentMethod v-model="cart.paymentMethod" />

    <div class="booking-form">
      <label>姓名 <input v-model="userName" placeholder="请输入姓名"></label>
      <label>手机号 <input v-model="userPhone" placeholder="请输入手机号"></label>
      <label class="checkbox-label"><input type="checkbox" v-model="proxyBuy"> 为亲友代购</label>
      <label v-if="proxyBuy">亲友手机号 <input v-model="friendPhone" placeholder="请输入亲友手机号"></label>
      <button class="btn btn-primary btn-block" :disabled="submitting" @click="submitOrder">
        {{ submitting ? '提交中...' : '确认支付 ¥' + finalTotal.toFixed(2) }}
      </button>
      <button class="btn btn-outline btn-block" @click="$router.back()">返回修改</button>
    </div>
  </div>
</template>

<style scoped>
.booking-container { max-width: 520px; margin: 0 auto; background: var(--surface); border-radius: var(--radius); padding: 24px; border: 1px solid var(--border); }
.booking-container h3 { color: var(--primary); margin-bottom: 14px; font-size: 20px; }
.booking-summary { background: var(--surface2); border-radius: 8px; padding: 14px; margin-bottom: 18px; font-size: 14px; line-height: 2; }
.summary-line { display: flex; justify-content: space-between; }
.summary-line .l { color: var(--text3); }
.summary-line.total { border-top: 1px solid var(--border); margin-top: 6px; padding-top: 8px; font-size: 20px; font-weight: 700; color: var(--gold); }
.booking-form label { display: block; margin-bottom: 12px; font-size: 14px; color: var(--text2); }
.booking-form label input { display: block; width: 100%; margin-top: 4px; }
.checkbox-label { display: flex !important; align-items: center; gap: 8px; cursor: pointer; }
.checkbox-label input { width: auto !important; display: inline !important; margin: 0 !important; }
</style>
