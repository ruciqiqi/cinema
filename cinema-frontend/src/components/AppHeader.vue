<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AuthModal from './AuthModal.vue'

const auth = useAuthStore()
const router = useRouter()

function navigateOrAuth(path) {
  if (auth.isLoggedIn) {
    router.push(path)
  } else {
    showAuth.value = true
  }
}

/* ------------------- theme ------------------- */
const isLight = ref(localStorage.getItem('cinema_theme') === 'light')

function toggleTheme() {
  isLight.value = !isLight.value
  document.body.classList.toggle('light', isLight.value)
  localStorage.setItem('cinema_theme', isLight.value ? 'light' : 'dark')
}
onMounted(() => {
  if (isLight.value) document.body.classList.add('light')
})

/* ------------------- auth modal ------------------- */
const showAuth = ref(false)
function openAuth() { showAuth.value = true }
function closeAuth() { showAuth.value = false }

/* ------------------- notification dropdown ------------------- */
const showNotif = ref(false)
const notifications = ref([])
const unread = ref(0)
const notifLoaded = ref(false)

async function loadNotifications() {
  if (!auth.isLoggedIn) { notifications.value = []; unread.value = 0; notifLoaded.value = false; return }
  try {
    const res = await fetch('/api/notifications', { headers: { Authorization: 'Bearer ' + auth.token } })
    const data = await res.json()
    if (data.success) {
      notifications.value = data.data.map(n => ({
        id: n.id, title: n.title, text: n.content, isRead: n.isRead,
        time: n.createdAt, action: 'navigate', target: (n.actionUrl && n.actionUrl !== '/user/center') ? n.actionUrl : '/user/center'
      }))
      unread.value = data.unreadCount || 0
      notifLoaded.value = true
    }
  } catch (e) { /* ignore */ }
}

function toggleNotif() {
  showNotif.value = !showNotif.value
  if (showNotif.value && !notifLoaded.value) loadNotifications()
}

async function handleNotif(n) {
  if (!n.isRead) {
    await fetch('/api/notifications/' + n.id + '/read', {
      method: 'PUT', headers: { Authorization: 'Bearer ' + auth.token }
    })
    n.isRead = true
    unread.value = Math.max(0, unread.value - 1)
  }
  showNotif.value = false
  if (n.target) router.push(n.target)
}

async function markAllRead() {
  await fetch('/api/notifications/read-all', {
    method: 'PUT', headers: { Authorization: 'Bearer ' + auth.token }
  })
  notifications.value.forEach(n => { n.isRead = true })
  unread.value = 0
}

// Reload when auth state changes
import { watch } from 'vue'
watch(() => auth.isLoggedIn, () => {
  if (auth.isLoggedIn) loadNotifications()
  else { notifications.value = []; unread.value = 0; notifLoaded.value = false }
})

/* ------------------- user dropdown ------------------- */
const showUser = ref(false)

function handleLogout() {
  auth.logout()
  showUser.value = false
}

/* close dropdowns on outside click */
function docClick(e) {
  if (!e.target.closest('.header-notif')) showNotif.value = false
  if (!e.target.closest('.header-user')) showUser.value = false
}
onMounted(() => document.addEventListener('click', docClick))
</script>

<template>
  <header class="app-header">
    <div class="header-inner">
      <!-- logo -->
      <RouterLink to="/" class="header-logo">&#127756; 星空影院</RouterLink>

      <!-- nav -->
      <nav class="header-nav">
        <RouterLink to="/">首页</RouterLink>
        <a href="#" @click.prevent="navigateOrAuth('/my/orders')">我的订单</a>
        <a href="#" @click.prevent="navigateOrAuth('/user/center')">个人中心</a>
        <RouterLink v-if="auth.isAdmin" to="/admin">后台管理</RouterLink>
        <RouterLink to="/order/lookup">订单查询</RouterLink>
      </nav>

      <!-- right -->
      <div class="header-right">
        <!-- notification -->
        <div class="header-notif" @click.stop="toggleNotif">
          <span class="notif-bell">&#128276;</span>
          <span v-if="unread" class="notif-badge">{{ unread }}</span>
          <Transition name="fade">
            <div v-if="showNotif" class="notif-dropdown">
              <div class="notif-dd-title">
                <span>消息通知</span>
                <button v-if="unread > 0" class="notif-clear-all" @click="markAllRead">一键已读</button>
              </div>
              <div v-if="!notifications.length" class="notif-empty">暂无消息</div>
              <div
                v-for="n in notifications"
                :key="n.id"
                class="notif-dd-item"
                :class="{ 'notif-read': n.isRead }"
                @click="handleNotif(n)"
              >
                <div class="notif-dot" v-if="!n.isRead"></div>
                <div class="notif-dd-text">{{ n.text }}</div>
                <div class="notif-dd-time">{{ n.time }}</div>
              </div>
            </div>
          </Transition>
        </div>

        <!-- theme toggle -->
        <button class="header-theme-btn" @click="toggleTheme" :title="isLight ? '切换暗色' : '切换亮色'">
          {{ isLight ? '&#127769;' : '&#9728;' }}
        </button>

        <!-- user area -->
        <template v-if="auth.isLoggedIn">
          <div class="header-user" @click.stop="showUser = !showUser">
            <span class="user-avatar">{{ auth.username.charAt(0).toUpperCase() }}</span>
            <span class="user-name">{{ auth.username }}</span>
            <Transition name="fade">
              <div v-if="showUser" class="user-dropdown">
                <RouterLink to="/user/center" class="user-dd-item">个人中心</RouterLink>
                <RouterLink to="/my/orders" class="user-dd-item">我的订单</RouterLink>
                <button class="user-dd-item logout-btn" @click="handleLogout">退出登录</button>
              </div>
            </Transition>
          </div>
        </template>
        <template v-else>
          <button class="header-login-btn" @click="openAuth">登录 / 注册</button>
        </template>
      </div>
    </div>
  </header>

  <AuthModal :show="showAuth" @close="closeAuth" />
</template>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 9000;
  background: rgba(10,10,26,.85);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(255,255,255,.06);
  box-shadow: 0 1px 20px rgba(0,0,0,.3);
}

.header-inner {
  max-width: 1300px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 20px;
  gap: 28px;
}

/* ---- logo ---- */
.header-logo {
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--gold), #ff9f43);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-decoration: none;
  white-space: nowrap;
  letter-spacing: 2px;
  flex-shrink: 0;
}
.header-logo:hover { text-decoration: none; opacity: .85; transform: scale(1.03); }

/* ---- nav ---- */
.header-nav {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  justify-content: center;
}
.header-nav a {
  padding: 8px 18px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 500;
  color: var(--text2);
  text-decoration: none;
  transition: all .2s;
  white-space: nowrap;
  position: relative;
}
.header-nav a:hover { color: #fff; background: rgba(255,255,255,.06); }
.header-nav a.router-link-active {
  color: #fff;
  background: linear-gradient(135deg, rgba(255,94,94,.2), rgba(255,94,94,.1));
  box-shadow: 0 2px 12px var(--primary-glow);
}

/* ---- right ---- */
.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
}

/* notif */
.header-notif {
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
}
.notif-bell {
  font-size: 20px;
  color: var(--text2);
  transition: color .2s;
}
.header-notif:hover .notif-bell { color: var(--text); }
.notif-badge {
  position: absolute;
  top: -4px;
  right: -8px;
  background: var(--danger);
  color: #fff;
  font-size: 10px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}
.notif-dropdown {
  position: absolute;
  top: calc(100% + 12px);
  right: -10px;
  width: 300px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: 0 12px 40px rgba(0,0,0,.5);
  overflow: hidden;
  z-index: 9100;
}
.notif-dd-title {
  padding: 14px 18px;
  font-size: 15px;
  font-weight: 600;
  border-bottom: 1px solid var(--border);
  color: var(--text);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.notif-clear-all {
  background: none;
  border: none;
  font-size: 12px;
  color: var(--text3);
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
}
.notif-clear-all:hover { color: var(--primary); background: rgba(255,107,107,.08); }
.notif-empty { padding: 30px 18px; text-align: center; font-size: 13px; color: var(--text3); }
.notif-dd-item {
  padding: 12px 18px 12px 32px;
  border-bottom: 1px solid var(--border);
  transition: background .15s;
  cursor: pointer;
  position: relative;
}
.notif-dd-item:last-child { border-bottom: none; }
.notif-dd-item:hover { background: rgba(255,107,107,.06); }
.notif-dd-item.notif-read { opacity: .55; }
.notif-dd-item.notif-read:hover { opacity: .75; }
.notif-dot {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  width: 8px; height: 8px;
  border-radius: 50%;
  background: var(--primary);
  box-shadow: 0 0 6px var(--primary-glow);
}
.notif-dd-text { font-size: 13px; color: var(--text); margin-bottom: 4px; line-height: 1.4; }
.notif-dd-time { font-size: 11px; color: var(--text3); }

/* theme */
.header-theme-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
  border-radius: 8px;
  line-height: 1;
  transition: background .15s;
}
.header-theme-btn:hover { background: rgba(255,255,255,.08); }

/* user */
.header-user {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 8px;
  transition: background .15s;
}
.header-user:hover { background: rgba(255,255,255,.06); }
.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--primary2));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}
.user-name { font-size: 14px; font-weight: 500; color: var(--text); }
.user-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  min-width: 150px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: 0 12px 40px rgba(0,0,0,.5);
  overflow: hidden;
  z-index: 9100;
}
.user-dd-item {
  display: block;
  width: 100%;
  padding: 12px 18px;
  font-size: 14px;
  color: var(--text);
  text-decoration: none;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  transition: background .15s;
}
.user-dd-item:hover { background: rgba(255,255,255,.04); }
.logout-btn { color: var(--danger); }

/* login btn */
.header-login-btn {
  padding: 7px 20px;
  border-radius: 8px;
  border: 1px solid var(--primary);
  background: transparent;
  color: var(--primary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all .2s;
}
.header-login-btn:hover { background: rgba(255,107,107,.12); }

/* responsive */
@media (max-width: 768px) {
  .header-nav { display: none; }
}
</style>
