<template>
  <div class="coupon-selector">
    <label class="coupon-label">优惠券</label>
    <div v-if="loading" class="coupon-loading">加载中...</div>
    <div v-else-if="error" class="coupon-error">{{ error }}</div>
    <select
      v-else
      class="coupon-select"
      :value="modelValue?.userCouponId || ''"
      @change="handleChange"
    >
      <option value="">不使用优惠券</option>
      <option
        v-for="coupon in coupons"
        :key="coupon.userCouponId"
        :value="coupon.userCouponId"
      >
        {{ coupon.name || '优惠券' + coupon.userCouponId }} (满{{ coupon.minAmount || 0 }}减{{ coupon.discount }})
      </option>
    </select>
    <div v-if="modelValue && modelValue.discount" class="coupon-discount">
      已优惠: &yen;{{ modelValue.amount || modelValue.discount }}
    </div>
    <div v-if="validateError" class="coupon-validate-error">{{ validateError }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import api from '../api'

const props = defineProps({
  modelValue: {
    type: Object,
    default: null
    // { userCouponId, discount, couponId }
  },
  orderAmount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:modelValue'])

const coupons = ref([])
const loading = ref(false)
const error = ref('')
const validateError = ref('')

async function fetchCoupons() {
  loading.value = true
  error.value = ''
  try {
    const res = await api.get('/coupons/my', { params: { status: 'unused' } })
    coupons.value = res.data.data || []
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function validateCoupon(userCouponId) {
  if (!userCouponId) return true
  validateError.value = ''
  try {
    const res = await api.post('/coupons/apply', {
      userCouponId, orderAmount: props.orderAmount
    })
    if (!res.data.success) {
      validateError.value = res.data.message || '优惠券不可用'
      return false
    }
    return data
  } catch (e) {
    validateError.value = '验证优惠券失败'
    return false
  }
}

async function handleChange(e) {
  const userCouponId = e.target.value
  if (!userCouponId) {
    emit('update:modelValue', null)
    validateError.value = ''
    return
  }
  const selectedCoupon = coupons.value.find(c => c.userCouponId === userCouponId)
  if (!selectedCoupon) return

  const result = await validateCoupon(userCouponId)
  if (result && result !== true) {
    emit('update:modelValue', {
      userCouponId,
      discount: selectedCoupon.discount,
      couponId: selectedCoupon.couponId,
      amount: result.amount || result.discount || selectedCoupon.discount
    })
  } else if (result === true) {
    emit('update:modelValue', {
      userCouponId,
      discount: selectedCoupon.discount,
      couponId: selectedCoupon.couponId
    })
  } else {
    emit('update:modelValue', null)
  }
}

onMounted(() => {
  fetchCoupons()
})
</script>

<style scoped>
.coupon-selector {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.coupon-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}

.coupon-loading,
.coupon-error {
  font-size: 13px;
  color: var(--text3);
}

.coupon-error {
  color: var(--danger);
}

.coupon-select {
  width: 100%;
  padding: 10px 12px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  color: var(--text);
  font-size: 14px;
  cursor: pointer;
  outline: none;
  transition: border-color 0.2s;
}

.coupon-select:focus {
  border-color: var(--primary);
}

.coupon-discount {
  font-size: 13px;
  color: var(--danger);
  font-weight: 600;
}

.coupon-validate-error {
  font-size: 13px;
  color: var(--danger);
}
</style>
