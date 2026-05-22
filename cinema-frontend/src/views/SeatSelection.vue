<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import api from '../api'
import SeatMap from '../components/SeatMap.vue'
import SnackSelector from '../components/SnackSelector.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const toast = inject('toast')

const showtimeId = ref(route.params.showtimeId)
const hallId = ref(null)
const seats = ref([])

onMounted(async () => {
  try {
    const stRes = await api.get(`/showtimes/${showtimeId.value}`)
    if (stRes.data.success) {
      cart.currentShowtime = stRes.data.data
      hallId.value = stRes.data.data.hallId
    }
    const seatRes = await api.get(`/seats?showtimeId=${showtimeId.value}&hallId=${hallId.value}`)
    if (seatRes.data.seats) seats.value = seatRes.data.seats
  } catch (e) { console.error(e) }
})

function autoSelect() {
  const available = seats.value.filter(s => !s.booked)
  available.sort((a, b) => {
    const aScore = Math.abs(a.seatNum - 7) + Math.abs(a.rowLabel.charCodeAt(0) - 'D'.charCodeAt(0))
    const bScore = Math.abs(b.seatNum - 7) + Math.abs(b.rowLabel.charCodeAt(0) - 'D'.charCodeAt(0))
    return aScore - bScore
  })
  const count = Math.min(2, available.length)
  cart.selectedSeats = available.slice(0, count).map(s => ({
    id: s.id, rowLabel: s.rowLabel, seatNum: s.seatNum, seatType: s.seatType
  }))
  if (count > 0) toast('已为您推荐最佳座位', 'info')
}

function confirmSeats() {
  if (cart.selectedSeats.length === 0) {
    toast('请先选择座位', 'error')
    return
  }
  if (!auth.isLoggedIn) {
    toast('请先登录后再购票', 'error')
    router.push('/')
    return
  }
  router.push('/order/confirm')
}
</script>

<template>
  <div class="seats-container">
    <div class="seats-header">
      <button class="btn btn-outline" @click="$router.back()">&larr; 返回</button>
      <h3>选择座位 - {{ cart.currentShowtime?.hallName }}</h3>
      <div class="seat-timer" v-if="false">剩余 15:00</div>
    </div>
    <SeatMap v-if="seats.length" v-model:selectedSeats="cart.selectedSeats" :seatMap="seats" :showtime="cart.currentShowtime" />
    <div style="text-align:center;margin-bottom:14px;">
      <button class="btn btn-sm btn-outline" @click="autoSelect">一键推荐最佳座位</button>
    </div>
    <SnackSelector v-model:snackCart="cart.snackCart" />
    <div class="seats-footer">
      <div class="selected-info">
        已选: <span>{{ cart.seatCount }}</span> 座 |
        合计: <span>&yen;{{ cart.calcFinalTotal().toFixed(2) }}</span>
      </div>
      <button class="btn btn-primary" @click="confirmSeats">确认选座</button>
    </div>
  </div>
</template>

<style scoped>
.seats-container { max-width: 800px; margin: 0 auto; background: var(--surface); border-radius: var(--radius); padding: 24px; border: 1px solid var(--border); }
.seats-header { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; flex-wrap: wrap; }
.seats-header h3 { font-size: 18px; color: var(--primary); }
.seat-timer { color: var(--danger); font-size: 14px; font-weight: 600; margin-left: auto; }
.seats-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 14px; border-top: 1px solid var(--border); }
.selected-info { font-size: 16px; color: var(--gold); }
.selected-info span { font-weight: 700; font-size: 20px; }
</style>
