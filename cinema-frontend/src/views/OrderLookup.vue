<script setup>
import { ref, inject } from 'vue'
import api from '../api'
import OrderCard from '../components/OrderCard.vue'

const toast = inject('toast')
const code = ref('')
const result = ref(null)
const refundPreview = ref(null)

async function lookup() {
  if (!code.value.trim()) { toast('请输入取票码', 'error'); return }
  try {
    const res = await api.get(`/bookings/query?code=${encodeURIComponent(code.value.trim())}`)
    if (res.data.success) {
      result.value = { booking: res.data.booking, seatLabels: res.data.seatLabels || [] }
    } else {
      result.value = null
      toast(res.data.message || '未找到订单', 'error')
    }
  } catch (e) { toast('查询失败', 'error') }
}

async function showRefundPreview(booking) {
  try {
    const res = await api.get(`/bookings/refund-preview?code=${encodeURIComponent(booking.bookingCode)}`)
    if (res.data.success) {
      refundPreview.value = {
        bookingCode: booking.bookingCode,
        originalAmount: res.data.originalAmount,
        refundAmount: res.data.refundAmount,
        movieTitle: booking.movieTitle,
        showDate: booking.showDate,
        showTime: booking.showTime,
      }
    } else {
      toast(res.data.message || '无法获取退款信息', 'error')
    }
  } catch (e) { toast('获取退款信息失败', 'error') }
}

function closeRefundPreview() { refundPreview.value = null }
</script>

<template>
  <div class="order-lookup-container">
    <h3>订单查询</h3>
    <div class="search-bar">
      <input v-model="code" placeholder="请输入取票码" @keyup.enter="lookup">
      <button class="btn btn-primary" @click="lookup">查询</button>
    </div>
    <div v-if="result">
      <OrderCard
        :booking="result.booking"
        :seatLabels="result.seatLabels"
        :showCancel="false"
        @refundPreview="showRefundPreview"
      />
    </div>
    <p v-else-if="result === null && code" style="color:var(--text3);text-align:center;padding:20px;">未找到订单</p>

    <!-- Refund Preview Modal -->
    <Teleport to="body">
      <div v-if="refundPreview" class="modal-overlay" @click.self="closeRefundPreview">
        <div class="refund-modal">
          <div class="refund-modal__header">
            <h4>退票预览</h4>
            <button class="modal-close" @click="closeRefundPreview">&times;</button>
          </div>
          <div class="refund-modal__body">
            <div class="refund-row"><span class="l">取票码</span><span class="v">{{ refundPreview.bookingCode }}</span></div>
            <div class="refund-row"><span class="l">影片</span><span class="v">{{ refundPreview.movieTitle }}</span></div>
            <div class="refund-row"><span class="l">场次</span><span class="v">{{ refundPreview.showDate }} {{ refundPreview.showTime }}</span></div>
            <div class="refund-divider"></div>
            <div class="refund-row"><span class="l">原支付金额</span><span class="v">&yen;{{ (refundPreview.originalAmount || 0).toFixed(2) }}</span></div>
            <div class="refund-row refund-highlight"><span class="l">预计退款</span><span class="v">&yen;{{ (refundPreview.refundAmount || 0).toFixed(2) }}</span></div>
            <div class="refund-divider"></div>
            <div class="refund-rules">
              <p class="refund-rule-title">退票规则说明：</p>
              <p>• 开场前 24 小时以上 — 全额退款</p>
              <p>• 开场前 12-24 小时 — 退款 80%</p>
              <p>• 开场前 6-12 小时 — 退款 50%</p>
              <p>• 开场前 6 小时内 — 不可退款</p>
            </div>
            <div class="refund-tip">
              <p>如需取消订单，请前往 <strong>"我的订单"</strong> 页面操作。</p>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.order-lookup-container { max-width: 580px; margin: 0 auto; }
.order-lookup-container h3 { color: var(--primary); margin-bottom: 14px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; }
.search-bar input { flex: 1; }

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
.refund-tip { margin-top: 14px; padding: 10px 14px; background: rgba(52,152,219,.1); border: 1px solid rgba(52,152,219,.2); border-radius: 8px; font-size: 13px; color: #3498db; text-align: center; }
</style>
