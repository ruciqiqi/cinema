<script setup>
import { ref, onMounted, inject } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import api from '../api'
import OrderCard from '../components/OrderCard.vue'

const auth = useAuthStore()
const router = useRouter()
const toast = inject('toast')
const bookings = ref([])
const loading = ref(true)
const refundPreview = ref(null)

onMounted(async () => {
  if (!auth.isLoggedIn) { router.push('/'); return }
  try {
    const res = await api.get('/bookings/my')
    if (res.data.success) bookings.value = res.data.data || []
  } catch (e) { console.error(e) }
  loading.value = false
})

async function doCancel(code) {
  if (!confirm('确定要取消此订单吗？退款金额以实际结算为准。')) return
  try {
    const res = await api.post('/bookings/cancel', { bookingCode: code })
    if (res.data.success) {
      toast(`已取消，退款¥${(res.data.refundAmount || 0).toFixed(2)}`, 'success')
      const idx = bookings.value.findIndex(b => b.bookingCode === code)
      if (idx >= 0) bookings.value[idx].status = 'cancelled'
    } else { toast(res.data.message, 'error') }
  } catch (e) { toast('操作失败', 'error') }
}

async function showRefund(booking) {
  try {
    const res = await api.get(`/bookings/refund-preview?code=${encodeURIComponent(booking.bookingCode)}`)
    if (res.data.success) {
      refundPreview.value = {
        ...res.data,
        movieTitle: booking.movieTitle,
        showDate: booking.showDate,
        showTime: booking.showTime,
      }
    } else {
      toast(res.data.message || '无法获取退款信息', 'error')
    }
  } catch (e) { toast('获取退款信息失败', 'error') }
}

function closeRefund() { refundPreview.value = null }
</script>

<template>
  <div class="my-bookings-container">
    <h3>我的订单</h3>
    <div v-if="loading" style="color:var(--text3);text-align:center;padding:40px;">加载中...</div>
    <template v-else>
      <OrderCard
        v-for="b in bookings" :key="b.id"
        :booking="b" :seatLabels="b.seatLabels || []"
        @cancel="doCancel(b.bookingCode)"
        @refundPreview="showRefund(b)"
      />
      <p v-if="!bookings.length" style="color:var(--text3);text-align:center;padding:40px;">暂无订单记录</p>
    </template>

    <!-- Refund Preview Modal -->
    <Teleport to="body">
      <div v-if="refundPreview" class="modal-overlay" @click.self="closeRefund">
        <div class="refund-modal">
          <div class="refund-modal__header">
            <h4>退票预览</h4>
            <button class="modal-close" @click="closeRefund">&times;</button>
          </div>
          <div class="refund-modal__body">
            <div class="refund-row"><span class="l">取票码</span><span class="v">{{ refundPreview.bookingCode }}</span></div>
            <div class="refund-row"><span class="l">影片</span><span class="v">{{ refundPreview.movieTitle }}</span></div>
            <div class="refund-row"><span class="l">场次</span><span class="v">{{ refundPreview.showDate }} {{ refundPreview.showTime }}</span></div>
            <div class="refund-divider"></div>
            <div class="refund-row"><span class="l">距离开场</span><span class="v">{{ refundPreview.hoursUntilShow != null ? (refundPreview.hoursUntilShow > 0 ? '约 ' + refundPreview.hoursUntilShow + ' 小时' : '已开场') : '--' }}</span></div>
            <div class="refund-row"><span class="l">适用规则</span><span class="v" style="font-size:12px;color:var(--text3);">{{ refundPreview.appliedRule || '--' }}</span></div>
            <div class="refund-divider"></div>
            <div class="refund-row"><span class="l">原支付金额</span><span class="v">&yen;{{ (refundPreview.originalAmount || 0).toFixed(2) }}</span></div>
            <div class="refund-row refund-highlight"><span class="l">预计退款 ({{ refundPreview.refundRate || '0%' }})</span><span class="v">&yen;{{ (refundPreview.refundAmount || 0).toFixed(2) }}</span></div>
            <div class="refund-divider"></div>
            <div class="refund-rules">
              <p class="refund-rule-title">退票规则：</p>
              <p>• 开场前 24 小时以上 — 全额退款</p>
              <p>• 开场前 12-24 小时 — 退款 80%</p>
              <p>• 开场前 6-12 小时 — 退款 50%</p>
              <p>• 开场前 6 小时内 — 不可退款</p>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.my-bookings-container { max-width: 580px; margin: 0 auto; }
.my-bookings-container h3 { color: var(--primary); margin-bottom: 14px; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.6); z-index: 200; display: flex; align-items: center; justify-content: center; }
.refund-modal { background: var(--surface); border-radius: var(--radius); width: 420px; max-width: 90vw; border: 1px solid var(--border); }
.refund-modal__header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid var(--border); }
.refund-modal__header h4 { color: var(--primary); font-size: 18px; margin: 0; }
.modal-close { background: none; border: none; font-size: 24px; color: var(--text3); cursor: pointer; }
.modal-close:hover { color: var(--primary); }
.refund-modal__body { padding: 20px; }
.refund-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; }
.refund-row .l { color: var(--text3); }
.refund-row .v { color: var(--text); font-weight: 500; }
.refund-highlight { background: rgba(255,107,107,.08); border-radius: 6px; padding: 8px 12px; margin: 4px 0; }
.refund-highlight .v { color: var(--primary); font-size: 20px; font-weight: 700; }
.refund-divider { height: 1px; background: var(--border); margin: 10px 0; }
.refund-rules { font-size: 12px; color: var(--text3); line-height: 1.8; }
.refund-rule-title { color: var(--text2); font-weight: 600; margin-bottom: 4px; }
</style>
