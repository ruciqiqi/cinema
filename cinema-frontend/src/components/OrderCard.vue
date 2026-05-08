<template>
  <div class="order-card">
    <div class="order-card__header">
      <span class="order-card__code">{{ booking.bookingCode }}</span>
      <span
        class="order-card__status"
        :class="booking.status === 'confirmed' ? 'order-card__status--confirmed' : 'order-card__status--cancelled'"
      >
        {{ booking.status === 'confirmed' ? '已确认' : '已取消' }}
      </span>
    </div>

    <div class="order-card__body">
      <div class="order-card__movie">
        <span class="order-card__label">影片</span>
        <span class="order-card__value">{{ booking.movieTitle }}</span>
      </div>
      <div class="order-card__info-row">
        <div class="order-card__info-item">
          <span class="order-card__label">影厅</span>
          <span class="order-card__value">{{ booking.hallName }}</span>
        </div>
        <div class="order-card__info-item">
          <span class="order-card__label">日期</span>
          <span class="order-card__value">{{ booking.showDate }}</span>
        </div>
        <div class="order-card__info-item">
          <span class="order-card__label">时间</span>
          <span class="order-card__value">{{ booking.showTime }}</span>
        </div>
      </div>
      <div class="order-card__seats">
        <span class="order-card__label">座位</span>
        <span class="order-card__value">
          <span v-for="(seat, i) in booking.seatLabels" :key="i" class="order-card__seat-tag">{{ seat }}</span>
        </span>
      </div>
      <div v-if="snackText" class="order-card__snacks">
        <span class="order-card__label">小食</span>
        <span class="order-card__value">{{ snackText }}</span>
      </div>
      <div class="order-card__divider" />
      <div class="order-card__amount-row">
        <div class="order-card__amount-item">
          <span class="order-card__label">金额</span>
          <span class="order-card__price">¥{{ booking.totalPrice }}</span>
        </div>
        <div class="order-card__amount-item">
          <span class="order-card__label">实付</span>
          <span class="order-card__price order-card__price--paid">¥{{ booking.actualPaid }}</span>
        </div>
        <div class="order-card__amount-item">
          <span class="order-card__label">支付方式</span>
          <span class="order-card__value">{{ paymentMethodText }}</span>
        </div>
      </div>
    </div>

    <div v-if="booking.status === 'confirmed'" class="order-card__actions">
      <button class="order-card__btn order-card__btn--outline" @click="$emit('refundPreview', booking)">
        退票预览
      </button>
      <button v-if="showCancel" class="order-card__btn order-card__btn--danger" @click="$emit('cancel', booking)">
        取消订单
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  booking: { type: Object, required: true },
  showCancel: { type: Boolean, default: true },
})

defineEmits(['cancel', 'refundPreview'])

const snackText = computed(() => {
  if (!props.booking.snacksJson) return ''
  try {
    const snacks = typeof props.booking.snacksJson === 'string'
      ? JSON.parse(props.booking.snacksJson)
      : props.booking.snacksJson
    if (Array.isArray(snacks)) {
      return snacks.map(s => `${s.name || s.snackName || ''}x${s.qty || s.quantity || 1}`).join('、')
    }
    return ''
  } catch {
    return ''
  }
})

const paymentMethodText = computed(() => {
  const map = {
    wechat: '微信支付',
    alipay: '支付宝',
    card: '银行卡',
    cash: '现金',
    points: '积分兑换',
  }
  return map[props.booking.paymentMethod] || props.booking.paymentMethod || '--'
})
</script>

<style scoped>
.order-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius, 8px);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-card__code {
  font-size: 20px;
  font-weight: 700;
  color: var(--gold);
  letter-spacing: 1px;
}

.order-card__status {
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.order-card__status--confirmed {
  background: rgba(82, 196, 26, 0.12);
  color: var(--success, #52c41a);
}

.order-card__status--cancelled {
  background: rgba(255, 77, 79, 0.12);
  color: var(--danger, #ff4d4f);
}

.order-card__body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.order-card__info-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.order-card__info-item,
.order-card__amount-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.order-card__movie,
.order-card__seats,
.order-card__snacks {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.order-card__label {
  font-size: 12px;
  color: var(--text3);
}

.order-card__value {
  font-size: 14px;
  color: var(--text);
}

.order-card__seat-tag {
  display: inline-block;
  background: var(--surface2);
  border: 1px solid var(--border);
  border-radius: 4px;
  padding: 1px 6px;
  margin-right: 4px;
  margin-bottom: 2px;
  font-size: 12px;
  color: var(--text2);
}

.order-card__divider {
  height: 1px;
  background: var(--border);
  margin: 4px 0;
}

.order-card__amount-row {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.order-card__price {
  font-size: 16px;
  font-weight: 700;
  color: var(--primary);
}

.order-card__price--paid {
  color: var(--gold);
}

.order-card__actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.order-card__btn {
  padding: 6px 16px;
  border-radius: var(--radius, 6px);
  font-size: 13px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: opacity 0.2s;
}

.order-card__btn:hover {
  opacity: 0.85;
}

.order-card__btn--outline {
  background: transparent;
  border-color: var(--primary);
  color: var(--primary);
}

.order-card__btn--danger {
  background: var(--danger, #ff4d4f);
  color: #fff;
}
</style>
