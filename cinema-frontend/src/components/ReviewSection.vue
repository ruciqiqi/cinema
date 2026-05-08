<template>
  <div class="review-section">
    <!-- Stats -->
    <div class="review-stats">
      <div class="review-stats-header">
        <h4 class="review-title">影评</h4>
        <div class="review-avg" v-if="reviews.length > 0">
          <span class="review-avg-num">{{ avgRating.toFixed(1) }}</span>
          <span class="review-avg-total"> / 10 ({{ reviews.length }}条评价)</span>
        </div>
      </div>
    </div>

    <!-- Review form -->
    <div v-if="showForm" class="review-form">
      <h5 class="review-form-title">写影评</h5>
      <div v-if="!auth.isLoggedIn" class="review-form-hint">
        请先<strong>登录</strong>后写评价
      </div>
      <div v-else-if="checkingWatch" class="review-form-hint">检查观影记录...</div>
      <div v-else-if="!hasWatched" class="review-form-hint">
        请先<strong>购票观影</strong>后再写评价
      </div>
      <template v-else>
        <div class="review-form-rating">
          <label class="review-form-label">评分</label>
          <StarRating v-model="formRating" />
        </div>
        <div class="review-form-content">
          <label class="review-form-label">内容</label>
          <textarea v-model="formContent" class="review-form-textarea" rows="4" placeholder="分享你的观影感受..."></textarea>
        </div>
        <div class="review-form-actions">
          <span v-if="submitMsg" class="review-form-msg" :class="{ 'review-form-msg--error': submitError }">{{ submitMsg }}</span>
          <button class="review-form-btn" :disabled="submitting || !formRating || !formContent.trim()" @click="submitReview">
            {{ submitting ? '提交中...' : '提交影评' }}
          </button>
        </div>
      </template>
    </div>

    <!-- Review list -->
    <div class="review-list">
      <div v-if="loading" class="review-loading">加载中...</div>
      <div v-else-if="loadError" class="review-load-error">{{ loadError }}</div>
      <div v-else-if="reviews.length === 0" class="review-empty">
        {{ showForm ? '暂无评价，来写第一条影评吧！' : '暂无评价' }}
      </div>
      <div
        v-for="review in reviews"
        :key="review.id"
        class="review-item"
      >
        <div class="review-item-header">
          <span class="review-item-user">{{ review.username || review.userName || '匿名用户' }}</span>
          <span class="review-item-rating">
            <span class="review-item-stars">
              <span v-for="s in 10" :key="s" class="review-item-star" :class="{ active: s <= review.rating }">
                {{ s <= review.rating ? '★' : '☆' }}
              </span>
            </span>
            <span class="review-item-rating-num">{{ review.rating }}/10</span>
          </span>
          <span class="review-item-date">{{ formatDate(review.createdAt || review.createTime) }}</span>
        </div>
        <p class="review-item-content">{{ review.content }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '../stores/auth'
import api from '../api'
import StarRating from './StarRating.vue'

const props = defineProps({
  movieId: { type: Number, required: true },
  showForm: { type: Boolean, default: false },
})

const auth = useAuthStore()
const reviews = ref([])
const loading = ref(false)
const loadError = ref('')
const formRating = ref(0)
const formContent = ref('')
const submitting = ref(false)
const submitMsg = ref('')
const submitError = ref(false)
const hasWatched = ref(false)
const checkingWatch = ref(false)

const avgRating = computed(() => {
  if (reviews.value.length === 0) return 0
  const sum = reviews.value.reduce((acc, r) => acc + r.rating, 0)
  return sum / reviews.value.length
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

async function fetchReviews() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await api.get(`/reviews/movie/${props.movieId}`)
    reviews.value = res.data.data || []
  } catch (e) {
    loadError.value = e.message || '获取影评失败'
  } finally {
    loading.value = false
  }
}

async function checkWatched() {
  if (!auth.isLoggedIn) return
  checkingWatch.value = true
  try {
    const res = await api.get('/bookings/my')
    if (res.data.success) {
      const bookings = res.data.data || []
      hasWatched.value = bookings.some(b =>
        b.status === 'confirmed' && b.movieTitle
      )
    }
  } catch (e) { /* ignore */ }
  finally { checkingWatch.value = false }
}

async function submitReview() {
  if (submitting.value || !formRating.value || !formContent.value.trim()) return
  if (!auth.isLoggedIn) {
    submitMsg.value = '请先登录后再写评价'
    submitError.value = true
    return
  }
  if (!hasWatched.value) {
    submitMsg.value = '观影后才能写评价，请先购票观影'
    submitError.value = true
    return
  }
  submitting.value = true
  submitMsg.value = ''
  submitError.value = false
  try {
    const res = await api.post('/reviews', {
      movieId: props.movieId,
      rating: formRating.value,
      content: formContent.value.trim()
    })
    if (res.data.success) {
      submitMsg.value = '影评提交成功！'
      formRating.value = 0
      formContent.value = ''
      await fetchReviews()
    } else {
      throw new Error(res.data.message || '提交失败')
    }
  } catch (e) {
    submitMsg.value = e.response?.data?.message || e.message || '提交失败'
    submitError.value = true
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (props.movieId) {
    fetchReviews()
    checkWatched()
  }
})

watch(() => props.movieId, (newId) => {
  if (newId) {
    fetchReviews()
    checkWatched()
  }
})
</script>

<style scoped>
.review-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-stats-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.review-title {
  margin: 0;
  font-size: 16px;
  color: var(--text);
}

.review-avg {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.review-avg-num {
  font-size: 22px;
  font-weight: 700;
  color: var(--gold);
}

.review-avg-total {
  font-size: 12px;
  color: var(--text3);
}

/* Form */
.review-form { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 16px; display: flex; flex-direction: column; gap: 14px; }
.review-form-hint { padding: 20px; text-align: center; font-size: 14px; color: var(--text3); background: var(--surface2); border-radius: 8px; }
.review-form-hint strong { color: var(--primary); }

.review-form-title {
  margin: 0;
  font-size: 14px;
  color: var(--text);
}

.review-form-label {
  font-size: 13px;
  color: var(--text2);
  display: block;
  margin-bottom: 6px;
}

.review-form-textarea {
  width: 100%;
  padding: 10px 12px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  color: var(--text);
  font-size: 14px;
  resize: vertical;
  outline: none;
  font-family: inherit;
  box-sizing: border-box;
}

.review-form-textarea:focus {
  border-color: var(--primary);
}

.review-form-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.review-form-msg {
  font-size: 13px;
  color: var(--success);
}

.review-form-msg--error {
  color: var(--danger);
}

.review-form-btn {
  padding: 8px 20px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.review-form-btn:hover:not(:disabled) {
  background: var(--primary2);
}

.review-form-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* List */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-loading,
.review-load-error,
.review-empty {
  font-size: 13px;
  color: var(--text3);
  padding: 12px 0;
  text-align: center;
}

.review-load-error {
  color: var(--danger);
}

.review-item {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.review-item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.review-item-user {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}

.review-item-rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.review-item-stars {
  display: flex;
  gap: 0;
}

.review-item-star {
  font-size: 13px;
  color: var(--border);
}

.review-item-star.active {
  color: var(--gold);
}

.review-item-rating-num {
  font-size: 12px;
  color: var(--text3);
}

.review-item-date {
  margin-left: auto;
  font-size: 12px;
  color: var(--text3);
}

.review-item-content {
  margin: 0;
  font-size: 14px;
  color: var(--text2);
  line-height: 1.6;
}
</style>
