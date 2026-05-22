<script setup>
import { ref, onMounted, watch } from 'vue'
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

/* ------------------- search ------------------- */
const searchKeyword = ref('')

function handleSearch() {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    router.push({ path: '/', query: { search: keyword } })
  } else {
    router.push('/')
  }
}

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
        time: n.createdAt, action: 'navigate', target: (n.actionUrl && n.actionUrl !== '/user/center' ? n.actionUrl : '/user/center')
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
      <RouterLink to="/" class="header-logo">
        <span class="logo-icon">🎬</span>
        <span class="logo-text">汪眼电影</span>
      </RouterLink>

      <!-- nav -->
      <nav class="header-nav">
        <RouterLink to="/" class="nav-item">电影</RouterLink>
        <a v-if="!auth.isAdmin" href="#" @click.prevent="navigateOrAuth('/my/orders')" class="nav-item">我的订单</a>
        <a href="#" @click.prevent="navigateOrAuth('/user/center')" class="nav-item">个人中心</a>
        <RouterLink v-if="auth.isAdmin" to="/admin" class="nav-item">后台管理</RouterLink>
        <RouterLink to="/order/lookup" class="nav-item">订单查询</RouterLink>
      </nav>

      <!-- search -->
      <form class="header-search" @submit.prevent="handleSearch">
        <input type="text" v-model="searchKeyword" placeholder="找电影/影人" />
        <span class="search-icon" @click="handleSearch">🔍</span>
      </form>

      <!-- right -->
      <div class="header-right">
        <!-- notification -->
        <div class="header-notif" @click.stop="toggleNotif">
          <span class="notif-icon">🔔</span>
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

        <!-- user area -->
        <template v-if="auth.isLoggedIn">
          <div class="header-user" @click.stop="showUser = !showUser">
            <span class="user-avatar">{{ auth.username.charAt(0).toUpperCase() }}</span>
            <span class="user-name">{{ auth.username }}</span>
            <span class="user-arrow">▼</span>
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
          <button class="header-login-btn" @click="openAuth">登录</button>
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
  background: #fff;
  border-bottom: 1px solid var(--border);
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 70px;
  padding: 0 20px;
  gap: 30px;
}

/* ---- logo ---- */
.header-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  flex-shrink: 0;
}
.header-logo:hover { opacity: 0.9; }
.logo-icon { font-size: 26px; }
.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary);
  letter-spacing: 1px;
}

/* ---- nav ---- */
.header-nav {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}
.nav-item {
  padding: 8px 18px;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text2);
  text-decoration: none;
  transition: all 0.2s;
  white-space: nowrap;
}
.nav-item:hover {
  color: var(--primary);
}
.nav-item.router-link-active {
  color: var(--primary);
  font-weight: 600;
}

/* ---- search ---- */
.header-search {
  position: relative;
  flex-shrink: 0;
}
.header-search input {
  width: 260px;
  padding: 9px 40px 9px 16px;
  border: 1px solid var(--border);
  border-radius: 20px;
  background: #f5f5f5;
  font-size: 14px;
  transition: all 0.2s;
}
.header-search input:focus {
  background: #fff;
  border-color: var(--primary);
}
.search-icon {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  color: var(--text3);
}

/* ---- right ---- */
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

/* notif */
.header-notif {
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 6px;
  border-radius: 50%;
  transition: background 0.15s;
}
.header-notif:hover { background: #f5f5f5; }
.notif-icon {
  font-size: 18px;
  color: var(--text2);
}
.notif-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: var(--primary);
  color: #fff;
  font-size: 10px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  border: 2px solid #fff;
}
.notif-dropdown {
  position: absolute;
  top: calc(100% + 12px);
  right: -10px;
  width: 300px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
  overflow: hidden;
  z-index: 9100;
}
.notif-dd-title {
  padding: 14px 18px;
  font-size: 14px;
  font-weight: 600;
  border-bottom: 1px solid var(--border);
  color: var(--text);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
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
.notif-clear-all:hover { color: var(--primary); }
.notif-empty { padding: 30px 18px; text-align: center; font-size: 13px; color: var(--text3); }
.notif-dd-item {
  padding: 12px 18px 12px 32px;
  border-bottom: 1px solid var(--border);
  transition: background 0.15s;
  cursor: pointer;
  position: relative;
}
.notif-dd-item:last-child { border-bottom: none; }
.notif-dd-item:hover { background: #fafafa; }
.notif-dd-item.notif-read { opacity: 0.55; }
.notif-dd-item.notif-read:hover { opacity: 0.75; }
.notif-dot {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--primary);
}
.notif-dd-text { font-size: 13px; color: var(--text); margin-bottom: 4px; line-height: 1.4; }
.notif-dd-time { font-size: 11px; color: var(--text3); }

/* user */
.header-user {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: background 0.15s;
}
.header-user:hover { background: #f5f5f5; }
.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--primary2));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}
.user-name { font-size: 14px; font-weight: 500; color: var(--text); }
.user-arrow { font-size: 10px; color: var(--text3); }
.user-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  min-width: 150px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
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
  transition: background 0.15s;
}
.user-dd-item:hover { background: #fafafa; color: var(--primary); }
.logout-btn { color: var(--text2); }
.logout-btn:hover { color: var(--danger); }

/* login btn */
.header-login-btn {
  padding: 8px 24px;
  border-radius: 20px;
  border: 1px solid var(--primary);
  background: #fff;
  color: var(--primary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.header-login-btn:hover {
  background: var(--primary);
  color: #fff;
}

/* responsive */
@media (max-width: 768px) {
  .header-nav { display: none; }
  .header-search { display: none; }
}
</style>
