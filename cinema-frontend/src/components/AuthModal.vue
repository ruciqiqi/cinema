<script setup>
import { ref, inject, watch } from 'vue'
import { useAuthStore } from '../stores/auth'

const props = defineProps({ show: Boolean })
const emit = defineEmits(['close'])

const auth = useAuthStore()
const toast = inject('toast', () => {})

const tab = ref('login')
const username = ref('')
const password = ref('')
const phone = ref('')
const error = ref('')
const loading = ref(false)

watch(() => props.show, (v) => {
  if (v) { error.value = ''; loading.value = false }
})

function close() {
  emit('close')
  error.value = ''
}

async function handleLogin() {
  if (!username.value.trim() || !password.value.trim()) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await auth.login(username.value.trim(), password.value)
    if (res.success) {
      toast('登录成功', 'success')
      close()
    } else {
      error.value = res.message || '登录失败'
    }
  } catch (e) {
    error.value = e?.response?.data?.message || '网络错误，请稍后再试'
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!username.value.trim() || !password.value.trim() || !phone.value.trim()) {
    error.value = '请填写所有字段'
    return
  }
  if (password.value.length < 6) {
    error.value = '密码至少 6 位'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await auth.register(username.value.trim(), password.value, phone.value.trim())
    if (res.success) {
      toast('注册成功', 'success')
      close()
    } else {
      error.value = res.message || '注册失败'
    }
  } catch (e) {
    error.value = e?.response?.data?.message || '网络错误，请稍后再试'
  } finally {
    loading.value = false
  }
}

function switchTab(t) {
  tab.value = t
  error.value = ''
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="show" class="modal-overlay" @click.self="close">
        <div class="modal-panel">
          <button class="modal-close" @click="close">&times;</button>

          <!-- tabs -->
          <div class="modal-tabs">
            <button
              :class="['modal-tab', { active: tab === 'login' }]"
              @click="switchTab('login')"
            >登录</button>
            <button
              :class="['modal-tab', { active: tab === 'register' }]"
              @click="switchTab('register')"
            >注册</button>
          </div>

          <!-- error -->
          <div v-if="error" class="modal-error">{{ error }}</div>

          <!-- login form -->
          <form v-if="tab === 'login'" @submit.prevent="handleLogin" class="modal-form">
            <input v-model="username" type="text" placeholder="用户名" autocomplete="username" />
            <input v-model="password" type="password" placeholder="密码" autocomplete="current-password" />
            <button class="modal-submit" :disabled="loading">
              {{ loading ? '登录中...' : '登 录' }}
            </button>
          </form>

          <!-- register form -->
          <form v-if="tab === 'register'" @submit.prevent="handleRegister" class="modal-form">
            <input v-model="username" type="text" placeholder="用户名" autocomplete="username" />
            <input v-model="password" type="password" placeholder="密码 (至少6位)" autocomplete="new-password" />
            <input v-model="phone" type="text" placeholder="手机号" autocomplete="tel" />
            <button class="modal-submit" :disabled="loading">
              {{ loading ? '注册中...' : '注 册' }}
            </button>
          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 9500;
  background: rgba(0, 0, 0, .65);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal-panel {
  position: relative;
  width: 100%;
  max-width: 400px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: 0 24px 64px rgba(0, 0, 0, .55);
  padding: 36px 30px 30px;
  animation: modal-in .35s cubic-bezier(.21, 1.02, .73, 1);
}

.modal-close {
  position: absolute;
  top: 12px;
  right: 16px;
  background: none;
  border: none;
  font-size: 28px;
  color: var(--text2);
  cursor: pointer;
  line-height: 1;
  transition: color .15s;
}
.modal-close:hover { color: var(--text); }

.modal-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  border-bottom: 2px solid var(--border);
}
.modal-tab {
  flex: 1;
  padding: 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text2);
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  cursor: pointer;
  transition: all .2s;
}
.modal-tab.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
}
.modal-tab:hover { color: var(--text); }

.modal-error {
  padding: 10px 14px;
  margin-bottom: 16px;
  background: rgba(231, 76, 60, .12);
  border: 1px solid rgba(231, 76, 60, .3);
  border-radius: 8px;
  color: var(--danger);
  font-size: 13px;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.modal-form input {
  padding: 12px 16px;
  font-size: 15px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--text);
}

.modal-submit {
  margin-top: 4px;
  padding: 13px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--primary), var(--primary2));
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all .2s;
}
.modal-submit:hover { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(255, 107, 107, .4); }
.modal-submit:disabled { opacity: .6; cursor: not-allowed; transform: none; box-shadow: none; }

/* transition */
.modal-enter-active .modal-panel { animation: modal-in .35s cubic-bezier(.21, 1.02, .73, 1); }
.modal-leave-active .modal-panel { animation: modal-out .25s ease-in forwards; }

.modal-enter-active { animation: overlay-in .25s; }
.modal-leave-active { animation: overlay-out .25s ease-in forwards; }

@keyframes modal-in {
  from { opacity: 0; transform: scale(.9) translateY(20px); }
  to   { opacity: 1; transform: scale(1) translateY(0); }
}
@keyframes modal-out {
  from { opacity: 1; transform: scale(1) translateY(0); }
  to   { opacity: 0; transform: scale(.92) translateY(20px); }
}
@keyframes overlay-in  { from { opacity: 0; } to { opacity: 1; } }
@keyframes overlay-out { from { opacity: 1; } to { opacity: 0; } }
</style>
