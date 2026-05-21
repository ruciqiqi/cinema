import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const currentMovieId = ref(null)
  const currentShowtimeId = ref(null)
  const currentHallId = ref(null)
  const currentShowtime = ref(null)
  const seatMap = ref([])
  const selectedSeats = ref([])
  const snackCart = ref({})
  const appliedCoupon = ref(null)
  const paymentMethod = ref('wechat')
  const orderTotal = ref(0)
  const bookingCode = ref('')

  const seatCount = computed(() => selectedSeats.value.length)

  function calcSeatTotal() {
    let total = 0
    if (currentShowtime.value) {
      selectedSeats.value.forEach(s => {
        total += s.seatType === 'vip' ? currentShowtime.value.priceVip : currentShowtime.value.priceStandard
      })
    }
    return Math.round(total * 100) / 100
  }

  function calcSnackTotal() {
    let total = 0
    Object.values(snackCart.value).forEach(sc => { total += sc.price * sc.qty })
    return Math.round(total * 100) / 100
  }

  function calcFinalTotal() {
    let total = calcSeatTotal() + calcSnackTotal()
    if (appliedCoupon.value) {
      total = Math.max(0, total - appliedCoupon.value.discount)
    }
    return Math.round(total * 100) / 100
  }

  function reset() {
    selectedSeats.value = []
    snackCart.value = {}
    appliedCoupon.value = null
    paymentMethod.value = 'wechat'
    bookingCode.value = ''
  }

  return {
    currentMovieId, currentShowtimeId, currentHallId, currentShowtime,
    seatMap, selectedSeats, snackCart, appliedCoupon, paymentMethod, orderTotal, bookingCode,
    seatCount, calcSeatTotal, calcSnackTotal, calcFinalTotal, reset
  }
})
