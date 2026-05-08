<template>
  <div class="payment-method">
    <div
      v-for="option in options"
      :key="option.value"
      class="payment-option"
      :class="{ 'payment-option--selected': modelValue === option.value }"
      @click="select(option.value)"
    >
      <span class="payment-icon">{{ option.icon }}</span>
      <div class="payment-info">
        <span class="payment-name">{{ option.label }}</span>
        <span class="payment-desc">{{ option.desc }}</span>
      </div>
      <span class="payment-radio" :class="{ 'payment-radio--checked': modelValue === option.value }">
        <span v-if="modelValue === option.value" class="payment-radio-dot"></span>
      </span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: ''
    // 'wechat' | 'alipay' | 'card'
  }
})

const emit = defineEmits(['update:modelValue'])

const options = [
  { value: 'wechat', label: '微信支付', desc: '使用微信完成支付', icon: '💚' },
  { value: 'alipay', label: '支付宝', desc: '使用支付宝完成支付', icon: '💙' },
  { value: 'card', label: '银行卡', desc: '使用银行卡完成支付', icon: '💳' }
]

function select(value) {
  emit('update:modelValue', value)
}
</script>

<style scoped>
.payment-method {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: var(--surface);
  border: 2px solid var(--border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.2s ease;
}

.payment-option:hover {
  border-color: var(--primary2);
  background: var(--surface2);
}

.payment-option--selected {
  border-color: var(--primary);
  background: var(--primary2);
}

.payment-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.payment-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.payment-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}

.payment-desc {
  font-size: 12px;
  color: var(--text3);
}

.payment-radio {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: border-color 0.2s;
}

.payment-radio--checked {
  border-color: var(--primary);
}

.payment-radio-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--primary);
}
</style>
