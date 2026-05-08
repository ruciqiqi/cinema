<script setup>
import { ref, onMounted, inject } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import api from '../api'
import MemberCard from '../components/MemberCard.vue'

const auth = useAuthStore()
const router = useRouter()
const toast = inject('toast')
const activeTab = ref('info')
const profile = ref(null)
const pointsData = ref(null)
const userCoupons = ref([])
const myReviews = ref([])
const loading = ref(false)

onMounted(async () => {
  if (!auth.isLoggedIn) { router.push('/'); return }
  await Promise.all([loadProfile(), loadPoints()])
})

const tabs = ['info', 'member', 'points', 'coupons', 'reviews', 'security']

async function loadProfile() {
  try {
    const res = await api.get('/user/profile')
    if (res.data.success) profile.value = res.data.data
  } catch (e) { console.error(e) }
}

async function loadPoints() {
  try {
    const res = await api.get('/user/points')
    if (res.data.success) pointsData.value = res.data
  } catch (e) { console.error(e) }
}

async function loadCoupons() {
  try {
    const res = await api.get('/coupons/my')
    if (res.data.success) userCoupons.value = res.data.data || []
  } catch (e) { console.error(e) }
}

async function loadReviews() {
  try {
    const res = await api.get('/reviews/my')
    if (res.data.success) myReviews.value = res.data.data || []
  } catch (e) { console.error(e) }
}

function switchTab(tab) {
  activeTab.value = tab
  if (tab === 'member' || tab === 'points') loadPoints()
  if (tab === 'coupons') loadCoupons()
  if (tab === 'reviews') loadReviews()
  if (tab === 'info' && !profile.value) loadProfile()
}

async function saveProfile() {
  const p = profile.value
  // phone
  if (!p.phone || !p.phone.trim()) { toast('手机号不能为空', 'error'); return }
  if (!/^1[3-9]\d{9}$/.test(p.phone.trim())) { toast('手机号格式不正确（1开头的11位数字）', 'error'); return }
  // email
  if (!p.email || !p.email.trim()) { toast('邮箱不能为空', 'error'); return }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(p.email.trim())) { toast('邮箱格式不正确', 'error'); return }
  // gender
  if (!p.gender) { toast('请选择性别', 'error'); return }
  // birthday
  if (!p.birthday) { toast('生日不能为空', 'error'); return }
  const dateRegex = /^\d{4}-\d{2}-\d{2}$/
  if (!dateRegex.test(p.birthday)) { toast('生日格式不正确（YYYY-MM-DD）', 'error'); return }

  try {
    const res = await api.put('/user/profile', {
      phone: p.phone.trim(),
      email: p.email.trim(),
      gender: p.gender,
      birthday: p.birthday
    })
    if (res.data.success) toast('个人信息已更新', 'success')
    else toast(res.data.message, 'error')
  } catch (e) { toast('更新失败', 'error') }
}

async function doRedeem(pts) {
  try {
    const res = await api.post('/user/redeem-points', { points: pts })
    if (res.data.success) { toast(res.data.message, 'success'); loadPoints(); loadProfile(); }
    else toast(res.data.message, 'error')
  } catch (e) { toast('兑换失败', 'error') }
}

async function receiveCoupon(couponId) {
  try {
    const res = await api.post(`/coupons/receive/${couponId}`)
    if (res.data.success) { toast('领取成功', 'success'); loadCoupons(); }
    else toast(res.data.message, 'error')
  } catch (e) { toast('领取失败', 'error') }
}

async function loadAvailableCoupons() {
  try {
    const res = await api.get('/coupons/available')
    if (res.data.success) {
      return res.data.data || []
    }
  } catch (e) { return [] }
  return []
}

async function changePassword() {
  const oldPwd = document.getElementById('secOldPwd')?.value
  const newPwd = document.getElementById('secNewPwd')?.value
  const confirm = document.getElementById('secConfirmPwd')?.value
  if (!oldPwd || !newPwd) { toast('请填写密码', 'error'); return }
  if (newPwd !== confirm) { toast('两次密码不一致', 'error'); return }
  try {
    const res = await api.post('/user/change-password', { oldPassword: oldPwd, newPassword: newPwd })
    if (res.data.success) toast(res.data.message, 'success')
    else toast(res.data.message, 'error')
  } catch (e) { toast('修改失败', 'error') }
}

async function submitRealName() {
  const name = document.getElementById('authName')?.value?.trim()
  const idCard = document.getElementById('authIdCard')?.value?.trim()
  if (!name || !idCard) { toast('请填写完整', 'error'); return }
  try {
    const res = await api.post('/user/real-name-auth', { realName: name, idCard: idCard })
    if (res.data.success) { toast(res.data.message, 'success'); loadProfile(); }
    else toast(res.data.message, 'error')
  } catch (e) { toast('认证失败', 'error') }
}

const availableCoupons = ref([])
async function showAvailableCoupons() {
  availableCoupons.value = await loadAvailableCoupons()
}
</script>

<template>
  <div class="uc-container">
    <div class="uc-sidebar">
      <div class="uc-avatar">
        <div style="font-size:48px;">{{ profile?.gender === '女' ? '👩' : '👨' }}</div>
        <div style="color:var(--text);font-weight:600;">{{ auth.username }}</div>
      </div>
      <a v-for="t in tabs" :key="t" href="#" :class="['uc-nav', { active: activeTab === t }]" @click.prevent="switchTab(t)">
        {{ {info:'个人信息',member:'会员等级',points:'积分中心',coupons:'优惠券',reviews:'我的评价',security:'安全设置'}[t] }}
      </a>
    </div>
    <div class="uc-content">
      <!-- Info -->
      <template v-if="activeTab === 'info' && profile">
        <h3>个人信息</h3>
        <div class="uc-form">
          <label>用户名 <input :value="profile.username" disabled></label>
          <label><span style="color:var(--danger)">*</span> 手机号 <input v-model="profile.phone" placeholder="请输入11位手机号"></label>
          <label><span style="color:var(--danger)">*</span> 邮箱 <input v-model="profile.email" placeholder="请输入邮箱地址"></label>
          <label><span style="color:var(--danger)">*</span> 性别 <select v-model="profile.gender"><option value="">请选择</option><option value="男">男</option><option value="女">女</option></select></label>
          <label><span style="color:var(--danger)">*</span> 生日 <input type="date" v-model="profile.birthday"></label>
          <button class="btn btn-primary" @click="saveProfile">保存修改</button>
        </div>
      </template>

      <!-- Member -->
      <template v-if="activeTab === 'member' && pointsData">
        <h3>会员等级</h3>
        <MemberCard :memberLevel="pointsData.memberLevel || 0" :totalSpent="pointsData.totalSpent || 0" :points="pointsData.points || 0" />
      </template>

      <!-- Points -->
      <template v-if="activeTab === 'points' && pointsData">
        <h3>积分中心</h3>
        <div class="stat-card"><div class="num">{{ pointsData.points || 0 }}</div><div class="label">可用积分</div></div>
        <p style="font-size:13px;color:var(--text2);margin:12px 0;">每消费1元获得1积分 | 10积分可兑换1元代金券</p>
        <div style="display:flex;gap:8px;flex-wrap:wrap;">
          <button class="btn btn-sm btn-primary" @click="doRedeem(100)">兑换10元 (100积分)</button>
          <button class="btn btn-sm btn-primary" @click="doRedeem(500)">兑换50元 (500积分)</button>
          <button class="btn btn-sm btn-primary" @click="doRedeem(1000)">兑换100元 (1000积分)</button>
        </div>
      </template>

      <!-- Coupons -->
      <template v-if="activeTab === 'coupons'">
        <h3>我的优惠券</h3>
        <div v-if="!userCoupons.length" style="color:var(--text3);padding:20px;">暂无优惠券</div>
        <div v-for="c in userCoupons" :key="c.id" :class="['coupon-card', { used: c.status !== 'unused' }]">
          <div>
            <div style="font-size:18px;font-weight:700;">{{ c.type === 'discount' ? c.value + '%折扣' : '¥' + c.value }}</div>
            <div style="font-size:12px;">{{ c.name }}</div>
            <div style="font-size:11px;">有效期至 {{ c.endDate }}</div>
          </div>
          <div style="font-size:12px;padding:4px 10px;border-radius:12px;background:rgba(255,255,255,.2);">{{ c.status === 'unused' ? '可用' : c.status === 'used' ? '已用' : '过期' }}</div>
        </div>
        <button class="btn btn-sm btn-outline" style="margin-top:12px;" @click="showAvailableCoupons">领取更多优惠券</button>
        <div v-if="availableCoupons.length" style="margin-top:12px;">
          <div v-for="c in availableCoupons" :key="c.id" class="coupon-card" style="cursor:pointer;" @click="receiveCoupon(c.id)">
            <div>
              <div style="font-size:16px;font-weight:700;">{{ c.type === 'discount' ? c.value + '%折扣' : '¥' + c.value }}</div>
              <div style="font-size:12px;">{{ c.name }}</div>
            </div>
            <div style="font-size:12px;">立即领取</div>
          </div>
        </div>
      </template>

      <!-- Reviews -->
      <template v-if="activeTab === 'reviews'">
        <h3>我的评价</h3>
        <div v-if="!myReviews.length" style="color:var(--text3);padding:20px;">暂无评价</div>
        <div v-for="r in myReviews" :key="r.id" class="review-item">
          <div class="review-header"><span>★ {{ r.rating }}/10</span></div>
          <div class="review-content">{{ r.content }}</div>
          <div class="review-time">{{ r.createdAt }}</div>
        </div>
      </template>

      <!-- Security -->
      <template v-if="activeTab === 'security'">
        <h3>安全设置</h3>
        <div class="uc-form">
          <label>原密码 <input type="password" id="secOldPwd"></label>
          <label>新密码 <input type="password" id="secNewPwd"></label>
          <label>确认新密码 <input type="password" id="secConfirmPwd"></label>
          <button class="btn btn-primary" @click="changePassword">修改密码</button>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.uc-container { display: flex; gap: 24px; min-height: 500px; }
.uc-sidebar { width: 200px; background: var(--surface); border-radius: var(--radius); padding: 20px; border: 1px solid var(--border); flex-shrink: 0; }
.uc-avatar { text-align: center; padding: 20px 0; }
.uc-nav { display: block; padding: 8px 14px; border-radius: 6px; color: var(--text2); text-decoration: none; font-size: 14px; margin-bottom: 4px; transition: all .2s; }
.uc-nav:hover, .uc-nav.active { background: var(--surface2); color: var(--primary); }
.uc-content { flex: 1; background: var(--surface); border-radius: var(--radius); padding: 24px; border: 1px solid var(--border); }
.uc-content h3 { color: var(--primary); margin-bottom: 16px; }
.uc-form { display: grid; gap: 12px; max-width: 400px; }
.uc-form label { font-size: 14px; color: var(--text2); }
.uc-form input, .uc-form select { display: block; width: 100%; margin-top: 4px; }
.stat-card { background: var(--surface2); border-radius: 8px; padding: 18px; text-align: center; display: inline-block; }
.stat-card .num { font-size: 32px; font-weight: 700; color: var(--primary); }
.stat-card .label { font-size: 13px; color: var(--text3); margin-top: 4px; }
.coupon-card { background: linear-gradient(135deg, var(--primary2), var(--primary)); border-radius: 8px; padding: 14px 18px; margin-bottom: 10px; color: #fff; display: flex; justify-content: space-between; align-items: center; }
.coupon-card.used { opacity: .5; }
.review-item { background: var(--surface2); border-radius: 8px; padding: 14px; margin-bottom: 10px; }
.review-item .review-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; font-weight: 600; color: var(--gold); }
.review-item .review-content { font-size: 14px; color: var(--text2); line-height: 1.6; }
.review-item .review-time { font-size: 11px; color: var(--text3); margin-top: 6px; }
@media(max-width:900px) { .uc-container { flex-direction: column; } .uc-sidebar { width: 100%; display: flex; gap: 6px; flex-wrap: wrap; padding: 12px; } .uc-nav { padding: 6px 12px; font-size: 12px; } }
</style>
