import { createPinia, setActivePinia } from 'pinia'
import { useCartStore } from '@/stores/cart'
import { describe, it, expect, beforeEach } from 'vitest'

describe('Cart Store', () => {
  let cartStore

  beforeEach(() => {
    setActivePinia(createPinia())
    cartStore = useCartStore()
  })

  // ==================== FS-01 初始状态为空 ====================
  it('initializes with empty state', () => {
    expect(cartStore.selectedSeats).toEqual([])
    expect(cartStore.snackCart).toEqual({})
    expect(cartStore.appliedCoupon).toBeNull()
    expect(cartStore.paymentMethod).toBe('wechat')
    expect(cartStore.bookingCode).toBe('')
    expect(cartStore.seatCount).toBe(0)
  })

  // ==================== FS-02 座位计算 - 标准价格 ====================
  it('calculates seat total with standard seats', () => {
    cartStore.currentShowtime = { priceStandard: 39.9, priceVip: 59.9 }
    cartStore.selectedSeats = [
      { id: 1, rowLabel: 'A', seatNum: 1, seatType: 'standard' },
      { id: 2, rowLabel: 'A', seatNum: 2, seatType: 'standard' }
    ]

    expect(cartStore.calcSeatTotal()).toBe(79.8)
  })

  // ==================== FS-03 座位计算 - VIP价格 ====================
  it('calculates seat total with VIP seats', () => {
    cartStore.currentShowtime = { priceStandard: 39.9, priceVip: 59.9 }
    cartStore.selectedSeats = [
      { id: 1, rowLabel: 'V', seatNum: 1, seatType: 'vip' },
      { id: 2, rowLabel: 'V', seatNum: 2, seatType: 'vip' }
    ]

    expect(cartStore.calcSeatTotal()).toBe(119.8)
  })

  // ==================== FS-04 座位计算 - 混合标准/VIP ====================
  it('calculates seat total with mixed seat types', () => {
    cartStore.currentShowtime = { priceStandard: 39.9, priceVip: 59.9 }
    cartStore.selectedSeats = [
      { id: 1, rowLabel: 'A', seatNum: 1, seatType: 'standard' },
      { id: 2, rowLabel: 'V', seatNum: 1, seatType: 'vip' }
    ]

    expect(cartStore.calcSeatTotal()).toBeCloseTo(99.8, 2)
  })

  // ==================== FS-05 座位计算 - 无场次返回0 ====================
  it('returns 0 for seat total when no showtime', () => {
    cartStore.currentShowtime = null
    cartStore.selectedSeats = [
      { id: 1, seatType: 'standard' }
    ]

    expect(cartStore.calcSeatTotal()).toBe(0)
  })

  // ==================== FS-06 小食计算 - 单个小食 ====================
  it('calculates snack total for single snack', () => {
    cartStore.snackCart = {
      1: { id: 1, name: '爆米花', price: 20.0, qty: 2 }
    }

    expect(cartStore.calcSnackTotal()).toBe(40.0)
  })

  // ==================== FS-07 小食计算 - 多个小食 ====================
  it('calculates snack total for multiple snacks', () => {
    cartStore.snackCart = {
      1: { id: 1, name: '爆米花', price: 20.0, qty: 2 },
      2: { id: 2, name: '可乐', price: 10.0, qty: 3 }
    }

    expect(cartStore.calcSnackTotal()).toBe(70.0)
  })

  // ==================== FS-08 小食计算 - 空购物车返回0 ====================
  it('returns 0 for snack total when cart is empty', () => {
    cartStore.snackCart = {}

    expect(cartStore.calcSnackTotal()).toBe(0)
  })

  // ==================== FS-09 最终总价 - 无优惠券 ====================
  it('calculates final total without coupon', () => {
    cartStore.currentShowtime = { priceStandard: 39.9, priceVip: 59.9 }
    cartStore.selectedSeats = [
      { id: 1, rowLabel: 'A', seatNum: 1, seatType: 'standard' }
    ]
    cartStore.snackCart = {
      1: { id: 1, name: '可乐', price: 10.0, qty: 1 }
    }
    cartStore.appliedCoupon = null

    expect(cartStore.calcFinalTotal()).toBeCloseTo(49.9, 2)
  })

  // ==================== FS-10 最终总价 - 有优惠券 ====================
  it('calculates final total with coupon discount', () => {
    cartStore.currentShowtime = { priceStandard: 39.9, priceVip: 59.9 }
    cartStore.selectedSeats = [
      { id: 1, rowLabel: 'A', seatNum: 1, seatType: 'standard' }
    ]
    cartStore.snackCart = {
      1: { id: 1, name: '可乐', price: 10.0, qty: 1 }
    }
    cartStore.appliedCoupon = { discount: 10.0 }

    // 39.9 + 10 - 10 = 39.9
    expect(cartStore.calcFinalTotal()).toBeCloseTo(39.9, 2)
  })

  // ==================== FS-11 最终总价 - 优惠券大于小计返回0 ====================
  it('returns 0 when coupon exceeds total', () => {
    cartStore.currentShowtime = { priceStandard: 10, priceVip: 20 }
    cartStore.selectedSeats = [
      { id: 1, rowLabel: 'A', seatNum: 1, seatType: 'standard' }
    ]
    cartStore.appliedCoupon = { discount: 100.0 }

    expect(cartStore.calcFinalTotal()).toBe(0)
  })

  // ==================== FS-12 重置购物车 ====================
  it('resets cart state correctly', () => {
    cartStore.selectedSeats = [{ id: 1 }]
    cartStore.snackCart = { 1: { qty: 2 } }
    cartStore.appliedCoupon = { discount: 10 }
    cartStore.paymentMethod = 'alipay'
    cartStore.bookingCode = 'BK123'

    cartStore.reset()

    expect(cartStore.selectedSeats).toEqual([])
    expect(cartStore.snackCart).toEqual({})
    expect(cartStore.appliedCoupon).toBeNull()
    expect(cartStore.paymentMethod).toBe('wechat')
    expect(cartStore.bookingCode).toBe('')
  })

  // ==================== FS-13 seatCount计算属性 ====================
  it('computes seat count correctly', () => {
    expect(cartStore.seatCount).toBe(0)

    cartStore.selectedSeats = [{ id: 1 }, { id: 2 }, { id: 3 }]

    expect(cartStore.seatCount).toBe(3)
  })

  // ==================== FS-14 小食保留2位小数 ====================
  it('rounds snack total to 2 decimals', () => {
    cartStore.snackCart = {
      1: { id: 1, name: '商品', price: 10.333, qty: 3 }
    }

    const total = cartStore.calcSnackTotal()
    expect(total).toBe(Math.round(10.333 * 3 * 100) / 100)
  })
})
