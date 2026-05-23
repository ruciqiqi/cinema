<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import api from '../api'
import ChartBar from '../components/ChartBar.vue'
import ChartLine from '../components/ChartLine.vue'
import ChartPie from '../components/ChartPie.vue'
import OrderCard from '../components/OrderCard.vue'

const auth = useAuthStore()
const router = useRouter()
const toast = inject('toast')

const activeTab = ref('stats')
const stats = ref(null)
const revenueData = ref({})
const movies = ref([])
const showtimes = ref([])
const bookings = ref([])
const snacks = ref([])
const coupons = ref([])
const announcements = ref([])
const users = ref([])
const halls = ref([])

const stFilterMovie = ref('')
const stFilterHall = ref('')
const stFilterDate = ref('')

const filteredShowtimes = computed(() => {
  return showtimes.value.filter(s => {
    if (stFilterMovie.value && String(s.movieId) !== stFilterMovie.value) return false
    if (stFilterHall.value && String(s.hallId) !== stFilterHall.value) return false
    if (stFilterDate.value && s.showDate !== stFilterDate.value) return false
    return true
  })
})

onMounted(async () => {
  if (!auth.isAdmin) { router.push('/'); return }
  await loadTab('stats')
})

async function loadTab(tab) {
  activeTab.value = tab
  switch (tab) {
    case 'stats': await loadStats(); break
    case 'statsfull': router.push('/admin/bigscreen'); break
    case 'movies': await loadMovies(); break
    case 'showtimes': await loadShowtimes(); break
    case 'bookings': await loadBookings(); break
    case 'snacks': await loadSnacks(); break
    case 'coupons': await loadCoupons(); break
    case 'announcements': await loadAnnouncements(); break
    case 'users': await loadUsers(); break
  }
}

async function loadStats() {
  const [overview, revenue] = await Promise.all([
    api.get('/admin/stats/overview'),
    api.get('/admin/stats/revenue')
  ])
  if (overview.data.success) stats.value = overview.data
  if (revenue.data.success) revenueData.value = revenue.data
}

async function loadMovies() {
  const res = await api.get('/admin/movies')
  if (res.data.success) movies.value = res.data.data
}

async function loadShowtimes() {
  const [stRes, mvRes, hlRes] = await Promise.all([
    api.get('/admin/showtimes'),
    api.get('/admin/movies'),
    api.get('/admin/halls')
  ])
  if (stRes.data.success) showtimes.value = stRes.data.data
  if (mvRes.data.success) movies.value = mvRes.data.data
  if (hlRes.data.success) halls.value = hlRes.data.data
}

async function loadBookings() {
  const res = await api.get('/bookings/all')
  if (res.data.success) bookings.value = res.data.data || []
}

async function loadSnacks() {
  const res = await api.get('/admin/snacks')
  if (res.data.success) snacks.value = res.data.data
}

async function loadCoupons() {
  const res = await api.get('/admin/coupons')
  if (res.data.success) coupons.value = res.data.data
}

async function loadAnnouncements() {
  const res = await api.get('/admin/announcements')
  if (res.data.success) announcements.value = res.data.data
}

async function loadUsers() {
  const res = await api.get('/admin/users')
  if (res.data.success) users.value = res.data.data
}

// CRUD operations
async function addMovie() {
  const d = {
    title: document.getElementById('mTitle')?.value?.trim(),
    director: document.getElementById('mDirector')?.value?.trim(),
    cast: document.getElementById('mCast')?.value?.trim(),
    genre: document.getElementById('mGenre')?.value?.trim(),
    duration: parseInt(document.getElementById('mDuration')?.value) || 120,
    language: document.getElementById('mLanguage')?.value?.trim(),
    country: document.getElementById('mCountry')?.value?.trim(),
    releaseDate: document.getElementById('mReleaseDate')?.value?.trim(),
    posterBg: document.getElementById('mPosterBg')?.value?.trim(),
    rating: parseFloat(document.getElementById('mRating')?.value) || 0,
    status: document.getElementById('mStatus')?.value || 'showing',
    isHot: document.getElementById('mIsHot')?.value === 'true'
  }
  if (!d.title) { toast('请输入片名', 'error'); return }
  try {
    const res = await api.post('/admin/movies', d)
    if (res.data.success) { toast('添加成功', 'success'); loadMovies(); }
    else toast(res.data.message, 'error')
  } catch (e) { toast('操作失败', 'error') }
}

async function delMovie(id) {
  if (!confirm('确定删除？')) return
  await api.delete(`/admin/movies/${id}`)
  toast('已删除', 'success')
  loadMovies()
}

async function addShowtime() {
  const d = {
    movieId: parseInt(document.getElementById('stMovieId')?.value),
    hallId: parseInt(document.getElementById('stHallId')?.value),
    showDate: document.getElementById('stDate')?.value?.trim(),
    showTime: document.getElementById('stTime')?.value?.trim(),
    priceStandard: parseFloat(document.getElementById('stPriceStd')?.value) || 39.9,
    priceVip: parseFloat(document.getElementById('stPriceVip')?.value) || 59.9
  }
  if (!d.showDate || !d.showTime) { toast('请填写日期和时间', 'error'); return }
  const res = await api.post('/admin/showtimes', d)
  if (res.data.success) { toast('添加成功', 'success'); loadShowtimes(); }
  else toast(res.data.message, 'error')
}

async function delShowtime(id) {
  if (!confirm('确定删除？')) return
  await api.delete(`/admin/showtimes/${id}`)
  toast('已删除', 'success')
  loadShowtimes()
}

async function refreshShowtimes() {
  try {
    const res = await api.post('/admin/showtimes/refresh')
    if (res.data.success) {
      toast(res.data.message, 'success')
      loadShowtimes()
    } else {
      toast('刷新失败', 'error')
    }
  } catch (e) {
    toast('刷新失败', 'error')
  }
}

async function cancelBooking(id) {
  if (!confirm('确定取消此订单？')) return
  await api.put(`/admin/bookings/${id}/cancel`)
  toast('已取消', 'success')
  loadBookings()
}

async function addSnack() {
  const d = {
    name: document.getElementById('snName')?.value?.trim(),
    category: document.getElementById('snCategory')?.value?.trim(),
    price: parseFloat(document.getElementById('snPrice')?.value) || 0,
    image: document.getElementById('snImage')?.value?.trim(),
    status: 'on'
  }
  if (!d.name) { toast('请输入名称', 'error'); return }
  const res = await api.post('/admin/snacks', d)
  if (res.data.success) { toast('添加成功', 'success'); loadSnacks(); }
}

async function delSnack(id) {
  if (!confirm('确定删除？')) return
  await api.delete(`/admin/snacks/${id}`)
  loadSnacks()
}

async function addCoupon() {
  const d = {
    code: document.getElementById('coCode')?.value?.trim(),
    name: document.getElementById('coName')?.value?.trim(),
    type: document.getElementById('coType')?.value || 'discount',
    value: parseFloat(document.getElementById('coValue')?.value) || 0,
    minAmount: parseFloat(document.getElementById('coMinAmount')?.value) || 0,
    startDate: document.getElementById('coStartDate')?.value?.trim(),
    endDate: document.getElementById('coEndDate')?.value?.trim(),
    usageLimit: parseInt(document.getElementById('coLimit')?.value) || 100,
    status: 'active'
  }
  if (!d.code || !d.name) { toast('请填写券码和名称', 'error'); return }
  const res = await api.post('/admin/coupons', d)
  if (res.data.success) { toast('添加成功', 'success'); loadCoupons(); }
}

async function delCoupon(id) {
  if (!confirm('确定删除？')) return
  await api.delete(`/admin/coupons/${id}`)
  loadCoupons()
}

async function addAnnouncement() {
  const title = document.getElementById('annTitle')?.value?.trim()
  const content = document.getElementById('annContent')?.value?.trim()
  if (!title) { toast('请输入标题', 'error'); return }
  const res = await api.post('/admin/announcements', { title, content })
  if (res.data.success) { toast('发布成功', 'success'); loadAnnouncements(); }
}

async function delAnnouncement(id) {
  if (!confirm('确定删除？')) return
  await api.delete(`/admin/announcements/${id}`)
  loadAnnouncements()
}

</script>

<template>
  <div class="admin-container">
    <div class="admin-sidebar">
      <h3>后台管理</h3>
      <a href="#" :class="['admin-nav', { active: activeTab === 'stats' }]" @click.prevent="loadTab('stats')">数据概览</a>
      <a href="#" :class="['admin-nav', { active: activeTab === 'statsfull' }]" @click.prevent="loadTab('statsfull')">数据大屏</a>
      <a href="#" :class="['admin-nav', { active: activeTab === 'movies' }]" @click.prevent="loadTab('movies')">影片管理</a>
      <a href="#" :class="['admin-nav', { active: activeTab === 'showtimes' }]" @click.prevent="loadTab('showtimes')">场次管理</a>
      <a href="#" :class="['admin-nav', { active: activeTab === 'bookings' }]" @click.prevent="loadTab('bookings')">订单管理</a>
      <a href="#" :class="['admin-nav', { active: activeTab === 'snacks' }]" @click.prevent="loadTab('snacks')">卖品管理</a>
      <a href="#" :class="['admin-nav', { active: activeTab === 'coupons' }]" @click.prevent="loadTab('coupons')">优惠券管理</a>
      <a href="#" :class="['admin-nav', { active: activeTab === 'announcements' }]" @click.prevent="loadTab('announcements')">公告管理</a>
      <a href="#" :class="['admin-nav', { active: activeTab === 'users' }]" @click.prevent="loadTab('users')">用户管理</a>
    </div>
    <div class="admin-content">
      <!-- Stats Overview -->
      <template v-if="activeTab === 'stats' && stats">
        <h3>数据概览</h3>
        <div class="stats-grid">
          <div class="stat-card"><div class="num">{{ stats.movieCount }}</div><div class="label">影片数</div></div>
          <div class="stat-card"><div class="num">{{ stats.showtimeCount }}</div><div class="label">场次数</div></div>
          <div class="stat-card"><div class="num">{{ stats.bookingCount }}</div><div class="label">订单数</div></div>
          <div class="stat-card"><div class="num">{{ stats.userCount }}</div><div class="label">用户数</div></div>
          <div class="stat-card"><div class="num">&yen;{{ (stats.revenue || 0).toFixed(0) }}</div><div class="label">总营收</div></div>
        </div>
        <div class="chart-row">
          <div class="chart-box"><h4>影片票房</h4><ChartBar :data="stats.movieRevenue || {}" /></div>
          <div class="chart-box"><h4>每日营收</h4><ChartLine :data="revenueData.byDate || {}" /></div>
          <div class="chart-box"><h4>类型分布</h4><ChartPie :data="stats.genreDistribution || {}" /></div>
          <div class="chart-box"><h4>热门影片订单</h4><ChartBar :data="stats.topMovies || {}" color="#ff6b6b" /></div>
        </div>
      </template>

      <!-- Movies -->
      <template v-if="activeTab === 'movies'">
        <h3>影片管理</h3>
        <div class="admin-form">
          <input id="mTitle" placeholder="片名"><input id="mDirector" placeholder="导演">
          <input id="mCast" placeholder="主演"><input id="mGenre" placeholder="类型">
          <input id="mDuration" placeholder="时长(分钟)" type="number"><input id="mLanguage" placeholder="语言">
          <input id="mCountry" placeholder="国家"><input id="mReleaseDate" placeholder="上映日期">
          <input id="mPosterBg" placeholder="海报CSS渐变"><input id="mRating" placeholder="评分" step="0.1" type="number">
          <select id="mStatus"><option value="showing">正在热映</option><option value="coming">即将上映</option></select>
          <select id="mIsHot"><option value="false">普通</option><option value="true">热门推荐</option></select>
          <textarea id="mDesc" placeholder="简介" rows="2"></textarea>
          <button class="btn btn-primary" @click="addMovie">添加影片</button>
        </div>
        <table class="admin-table"><tr><th>ID</th><th>片名</th><th>类型</th><th>评分</th><th>操作</th></tr>
          <tr v-for="m in movies" :key="m.id"><td>{{ m.id }}</td><td>{{ m.title }}</td><td>{{ m.genre }}</td><td>{{ (m.rating||0).toFixed(1) }}</td>
            <td><button class="btn btn-sm btn-danger" @click="delMovie(m.id)">删除</button></td></tr>
        </table>
      </template>

      <!-- Showtimes -->
      <template v-if="activeTab === 'showtimes'">
        <h3>场次管理</h3>
        <div class="admin-form">
          <select id="stMovieId"><option v-for="m in movies" :key="m.id" :value="m.id">{{ m.title }}</option></select>
          <select id="stHallId"><option v-for="h in halls" :key="h.id" :value="h.id">{{ h.name }}</option></select>
          <input id="stDate" placeholder="日期 YYYY-MM-DD"><input id="stTime" placeholder="时间 HH:MM">
          <input id="stPriceStd" placeholder="标准票价" type="number" step="0.1" value="39.9">
          <input id="stPriceVip" placeholder="VIP票价" type="number" step="0.1" value="59.9">
          <button class="btn btn-primary" @click="addShowtime">添加场次</button>
          <button class="btn btn-outline" @click="refreshShowtimes">刷新日期</button>
        </div>
        <div class="admin-form" style="margin-top:12px;">
          <select v-model="stFilterMovie">
            <option value="">全部影片</option>
            <option v-for="m in movies" :key="m.id" :value="String(m.id)">{{ m.title }}</option>
          </select>
          <select v-model="stFilterHall">
            <option value="">全部影厅</option>
            <option v-for="h in halls" :key="h.id" :value="String(h.id)">{{ h.name }}</option>
          </select>
          <input v-model="stFilterDate" placeholder="日期 YYYY-MM-DD">
          <button class="btn btn-sm btn-outline" @click="stFilterMovie='';stFilterHall='';stFilterDate=''">清除筛选</button>
        </div>
        <table class="admin-table"><tr><th>ID</th><th>影片</th><th>影厅</th><th>日期</th><th>时间</th><th>操作</th></tr>
          <tr v-for="s in filteredShowtimes" :key="s.id"><td>{{ s.id }}</td><td>{{ s.movieTitle }}</td><td>{{ s.hallName }}</td><td>{{ s.showDate }}</td><td>{{ s.showTime }}</td>
            <td><button class="btn btn-sm btn-danger" @click="delShowtime(s.id)">删除</button></td></tr>
        </table>
      </template>

      <!-- Bookings -->
      <template v-if="activeTab === 'bookings'">
        <h3>订单管理</h3>
        <table class="admin-table"><tr><th>ID</th><th>取票码</th><th>影片</th><th>姓名</th><th>金额</th><th>状态</th><th>操作</th></tr>
          <tr v-for="b in bookings" :key="b.id"><td>{{ b.id }}</td><td>{{ b.bookingCode }}</td><td>{{ b.movieTitle }}</td><td>{{ b.userName }}</td>
            <td>&yen;{{ (b.actualPaid || b.totalPrice || 0).toFixed(2) }}</td><td>{{ b.status }}</td>
            <td><button v-if="b.status==='confirmed'" class="btn btn-sm btn-danger" @click="cancelBooking(b.id)">取消</button></td></tr>
        </table>
      </template>

      <!-- Snacks -->
      <template v-if="activeTab === 'snacks'">
        <h3>卖品管理</h3>
        <div class="admin-form">
          <input id="snName" placeholder="名称"><input id="snCategory" placeholder="分类">
          <input id="snPrice" placeholder="价格" type="number" step="0.1"><input id="snImage" placeholder="图标">
          <button class="btn btn-primary" @click="addSnack">添加卖品</button>
        </div>
        <table class="admin-table"><tr><th>ID</th><th>名称</th><th>分类</th><th>价格</th><th>操作</th></tr>
          <tr v-for="s in snacks" :key="s.id"><td>{{ s.id }}</td><td>{{ s.name }}</td><td>{{ s.category }}</td><td>&yen;{{ s.price.toFixed(1) }}</td>
            <td><button class="btn btn-sm btn-danger" @click="delSnack(s.id)">删除</button></td></tr>
        </table>
      </template>

      <!-- Coupons -->
      <template v-if="activeTab === 'coupons'">
        <h3>优惠券管理</h3>
        <div class="admin-form">
          <input id="coCode" placeholder="券码"><input id="coName" placeholder="名称">
          <select id="coType"><option value="discount">折扣券(%)</option><option value="cash">代金券(元)</option></select>
          <input id="coValue" placeholder="面值" type="number" step="0.1">
          <input id="coMinAmount" placeholder="最低消费" type="number" step="0.1" value="0">
          <input id="coStartDate" placeholder="开始日期"><input id="coEndDate" placeholder="结束日期">
          <input id="coLimit" placeholder="发放数量" type="number" value="100">
          <button class="btn btn-primary" @click="addCoupon">添加优惠券</button>
        </div>
        <table class="admin-table"><tr><th>ID</th><th>券码</th><th>名称</th><th>已领/总量</th><th>操作</th></tr>
          <tr v-for="c in coupons" :key="c.id"><td>{{ c.id }}</td><td>{{ c.code }}</td><td>{{ c.name }}</td><td>{{ c.usedCount }}/{{ c.usageLimit }}</td>
            <td><button class="btn btn-sm btn-danger" @click="delCoupon(c.id)">删除</button></td></tr>
        </table>
      </template>

      <!-- Announcements -->
      <template v-if="activeTab === 'announcements'">
        <h3>公告管理</h3>
        <div class="admin-form">
          <input id="annTitle" placeholder="标题"><textarea id="annContent" placeholder="内容" rows="3"></textarea>
          <button class="btn btn-primary" @click="addAnnouncement">发布公告</button>
        </div>
        <table class="admin-table"><tr><th>ID</th><th>标题</th><th>时间</th><th>操作</th></tr>
          <tr v-for="a in announcements" :key="a.id"><td>{{ a.id }}</td><td>{{ a.title }}</td><td>{{ a.createdAt }}</td>
            <td><button class="btn btn-sm btn-danger" @click="delAnnouncement(a.id)">删除</button></td></tr>
        </table>
      </template>

      <!-- Users -->
      <template v-if="activeTab === 'users'">
        <h3>用户管理</h3>
        <table class="admin-table"><tr><th>ID</th><th>用户名</th><th>角色</th><th>会员等级</th><th>积分</th><th>手机</th></tr>
          <tr v-for="u in users" :key="u.id"><td>{{ u.id }}</td><td>{{ u.username }}</td><td>{{ u.role }}</td><td>Lv.{{ u.memberLevel || 0 }}</td><td>{{ u.points || 0 }}</td><td>{{ u.phone }}</td></tr>
        </table>
      </template>
    </div>
  </div>
</template>

<style scoped>
.admin-container { 
  display: flex; 
  gap: 20px; 
  min-height: 500px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
}
.admin-sidebar { 
  width: 200px; 
  background: #fff; 
  border-radius: 12px; 
  padding: 20px; 
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  flex-shrink: 0; 
}
.admin-sidebar h3 { 
  color: var(--primary); 
  margin-bottom: 14px; 
  font-size: 16px;
  font-weight: 600;
}
.admin-nav { 
  display: block; 
  padding: 10px 14px; 
  border-radius: 8px; 
  color: var(--text2); 
  text-decoration: none; 
  font-size: 14px; 
  margin-bottom: 4px; 
  transition: all 0.2s; 
}
.admin-nav:hover, .admin-nav.active { 
  background: rgba(229, 72, 71, 0.08); 
  color: var(--primary);
  font-weight: 500;
}
.admin-content { 
  flex: 1; 
  background: #fff; 
  border-radius: 12px; 
  padding: 24px; 
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  overflow-x: auto; 
}
.admin-content h3 { 
  color: var(--text); 
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 2px solid var(--primary);
  padding-bottom: 12px;
}
.admin-table { 
  width: 100%; 
  border-collapse: collapse; 
  font-size: 13px; 
  margin-top: 16px;
}
.admin-table th { 
  background: #fafafa; 
  padding: 12px 10px; 
  text-align: left; 
  color: var(--text); 
  border-bottom: 2px solid var(--border);
  font-weight: 600;
}
.admin-table td { 
  padding: 10px 8px; 
  border-bottom: 1px solid var(--border-light); 
  color: var(--text2); 
}
.admin-table tr:hover td { 
  background: #fafafa; 
}
.admin-form { 
  display: grid; 
  gap: 12px; 
  margin-bottom: 20px; 
  grid-template-columns: 1fr 1fr;
  background: #fafafa;
  padding: 20px;
  border-radius: 8px;
}
.admin-form input, .admin-form select, .admin-form textarea { 
  padding: 10px 14px; 
  border: 1px solid var(--border);
  border-radius: 6px;
}
.admin-form textarea { 
  grid-column: 1 / -1; 
  resize: vertical; 
}
.stats-grid { 
  display: grid; 
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); 
  gap: 16px; 
  margin-bottom: 24px; 
}
.stat-card { 
  background: #fafafa; 
  border: 1px solid var(--border-light);
  border-radius: 10px; 
  padding: 20px; 
  text-align: center;
  transition: all 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}
.stat-card .num { 
  font-size: 28px; 
  font-weight: 700; 
  color: var(--primary); 
  margin-bottom: 8px;
}
.stat-card .label { 
  font-size: 13px; 
  color: var(--text3);
}
.chart-row { 
  display: grid; 
  grid-template-columns: 1fr 1fr; 
  gap: 20px; 
  margin-top: 20px;
}
.chart-box { 
  background: #fafafa; 
  border: 1px solid var(--border-light);
  border-radius: 10px; 
  padding: 16px;
}
.chart-box h4 { 
  color: var(--text2); 
  margin-bottom: 12px; 
  font-size: 14px; 
  font-weight: 600;
  text-transform: uppercase; 
  letter-spacing: 1px;
}
@media(max-width:900px) { .admin-container { flex-direction: column; } .admin-sidebar { width: 100%; display: flex; gap: 6px; flex-wrap: wrap; padding: 12px; } .admin-sidebar h3 { display: none; } .admin-nav { padding: 6px 12px; font-size: 12px; } .admin-form { grid-template-columns: 1fr; } .chart-row { grid-template-columns: 1fr; } }
</style>
