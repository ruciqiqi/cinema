<script setup>
import { ref } from 'vue'

let uid = 0
const toasts = ref([])

function addToast(msg, type) {
  uid++
  const id = uid
  toasts.value.push({ id, msg, type })
  setTimeout(() => {
    const idx = toasts.value.findIndex(t => t.id === id)
    if (idx !== -1) {
      toasts.value[idx].leaving = true
      setTimeout(() => {
        toasts.value = toasts.value.filter(t => t.id !== id)
      }, 300)
    }
  }, 3000)
}

defineExpose({ addToast })
</script>

<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div
          v-for="t in toasts"
          :key="t.id"
          class="toast-item"
          :class="[t.type, { 'toast-leaving': t.leaving }]"
        >
          <span class="toast-icon">
            <template v-if="t.type === 'success'">&#10003;</template>
            <template v-else-if="t.type === 'error'">&#10007;</template>
            <template v-else>&#8505;</template>
          </span>
          <span class="toast-msg">{{ t.msg }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-container {
  position: fixed;
  top: 80px;
  right: 24px;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
}

.toast-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  min-width: 280px;
  max-width: 420px;
  background: var(--surface);
  border-radius: var(--radius);
  box-shadow: 0 8px 32px rgba(0, 0, 0, .45);
  border-left: 4px solid transparent;
  backdrop-filter: blur(12px);
  pointer-events: auto;
  animation: toast-in .35s cubic-bezier(.21, 1.02, .73, 1);
}

.toast-item.success { border-left-color: var(--success); }
.toast-item.error   { border-left-color: var(--danger); }
.toast-item.info    { border-left-color: #3498db; }

.toast-icon {
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}
.success .toast-icon { color: var(--success); background: rgba(46,204,113,.15); }
.error   .toast-icon { color: var(--danger);  background: rgba(231,76,60,.15); }
.info    .toast-icon { color: #3498db;        background: rgba(52,152,219,.15); }

.toast-msg {
  font-size: 14px;
  color: var(--text);
  line-height: 1.4;
}

/* TransitionGroup */
.toast-enter-active { animation: toast-in .35s cubic-bezier(.21, 1.02, .73, 1); }
.toast-leave-active { animation: toast-out .3s ease-in; }

.toast-leaving {
  opacity: 0;
  transform: translateX(60px);
  transition: opacity .3s, transform .3s;
}

@keyframes toast-in {
  from { opacity: 0; transform: translateX(80px) scale(.9); }
  to   { opacity: 1; transform: translateX(0) scale(1); }
}
@keyframes toast-out {
  from { opacity: 1; transform: translateX(0) scale(1); }
  to   { opacity: 0; transform: translateX(80px) scale(.85); }
}
</style>
