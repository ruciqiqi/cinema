<template>
  <div class="snack-selector">
    <h4 class="snack-title">小食饮品</h4>
    <div v-if="loading" class="snack-loading">加载中...</div>
    <div v-else-if="error" class="snack-error">{{ error }}</div>
    <div v-else class="snack-grid">
      <div
        v-for="snack in snacks"
        :key="snack.id"
        class="snack-card"
        :class="{ 'snack-card--active': getQty(snack.id) > 0 }"
        @click="incrementQty(snack)"
      >
        <div class="snack-name">{{ snack.name }}</div>
        <div class="snack-price">&yen;{{ snack.price }}</div>
        <div class="snack-qty" v-if="getQty(snack.id) > 0">
          <span class="snack-qty-num">{{ getQty(snack.id) }}</span>
        </div>
      </div>
    </div>
    <div v-if="cartTotal > 0" class="snack-total">
      小食合计: &yen;{{ cartTotal }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '../api'

const props = defineProps({
  snackCart: {
    type: Object,
    required: true
    // { id: { id, name, price, qty } }
  }
})

const emit = defineEmits(['update:snackCart'])

const snacks = ref([])
const loading = ref(false)
const error = ref('')

function getQty(id) {
  return props.snackCart[id]?.qty || 0
}

const cartTotal = computed(() => {
  return Object.values(props.snackCart).reduce((sum, item) => {
    return sum + item.price * item.qty
  }, 0)
})

function incrementQty(snack) {
  const current = getQty(snack.id)
  const next = current >= 5 ? 0 : current + 1
  const newCart = { ...props.snackCart }
  if (next === 0) {
    delete newCart[snack.id]
  } else {
    newCart[snack.id] = {
      id: snack.id,
      name: snack.name,
      price: snack.price,
      qty: next
    }
  }
  emit('update:snackCart', newCart)
}

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await api.get('/snacks')
    snacks.value = res.data.data || []
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.snack-selector {
  padding: 4px 0;
}

.snack-title {
  margin: 0 0 12px;
  font-size: 15px;
  color: var(--text);
}

.snack-loading,
.snack-error {
  color: var(--text3);
  font-size: 13px;
  padding: 12px 0;
}

.snack-error {
  color: var(--danger);
}

.snack-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
}

.snack-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 12px;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s ease;
  position: relative;
}

.snack-card:hover {
  border-color: var(--primary);
  background: var(--surface2);
}

.snack-card--active {
  border-color: var(--success);
  background: var(--surface2);
}

.snack-name {
  font-size: 13px;
  color: var(--text);
  margin-bottom: 6px;
}

.snack-price {
  font-size: 13px;
  color: var(--danger);
  font-weight: 600;
}

.snack-qty {
  position: absolute;
  top: -6px;
  right: -6px;
  background: var(--success);
  color: #fff;
  border-radius: 50%;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.snack-total {
  margin-top: 14px;
  text-align: right;
  font-size: 14px;
  color: var(--text);
  font-weight: 600;
}
</style>
