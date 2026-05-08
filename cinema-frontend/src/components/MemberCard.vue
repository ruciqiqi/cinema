<template>
  <div class="member-card" :style="cardGradient">
    <div class="member-card__header">
      <span class="member-card__level-name">{{ levelInfo.name }}</span>
      <span class="member-card__badge">Lv{{ memberLevel }}</span>
    </div>

    <div class="member-card__stats">
      <div class="member-card__stat">
        <span class="member-card__stat-label">累计消费</span>
        <span class="member-card__stat-value">¥{{ totalSpent.toFixed(2) }}</span>
      </div>
      <div class="member-card__stat">
        <span class="member-card__stat-label">积分余额</span>
        <span class="member-card__stat-value">{{ points }}</span>
      </div>
    </div>

    <div v-if="nextLevel" class="member-card__progress-section">
      <div class="member-card__progress-header">
        <span class="member-card__progress-text">距 {{ nextLevel.name }}</span>
        <span class="member-card__progress-amount">还差 ¥{{ remainStr }}</span>
      </div>
      <div class="member-card__progress-bar">
        <div
          class="member-card__progress-fill"
          :style="{ width: progressPercent + '%' }"
        />
      </div>
    </div>

    <div class="member-card__benefits">
      <span class="member-card__benefits-title">等级权益</span>
      <ul class="member-card__benefits-list">
        <li v-for="(b, i) in levelInfo.benefits" :key="i" class="member-card__benefit-item">
          {{ b }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  memberLevel: {
    type: Number,
    default: 0,
    validator: v => v >= 0 && v <= 5,
  },
  totalSpent: {
    type: Number,
    default: 0,
  },
  points: {
    type: Number,
    default: 0,
  },
})

const levels = [
  { name: '普通用户', threshold: 0,    color: '#9e9e9e', benefits: ['畅享在线选座', '每日签到积分'] },
  { name: '青铜会员', threshold: 200,  color: '#cd7f32', benefits: ['购票 98 折', '生日双倍积分', '优先选座'] },
  { name: '白银会员', threshold: 500,  color: '#a0a0b0', benefits: ['购票 95 折', '生日双倍积分', '每月赠饮 x1'] },
  { name: '黄金会员', threshold: 1000, color: '#daa520', benefits: ['购票 9 折', '生日双倍积分', '每月赠饮 x2', '免退票手续费'] },
  { name: '铂金会员', threshold: 2000, color: '#9b59b6', benefits: ['购票 85 折', '生日三倍积分', '每月赠饮 x3', '免退票手续费', '专属客服'] },
  { name: '钻石会员', threshold: 5000, color: '#3498db', benefits: ['购票 8 折', '生日三倍积分', '每月赠饮 x5', '免退票手续费', '专属客服', '包厢升舱'] },
]

const levelInfo = computed(() => levels[props.memberLevel] || levels[0])

const nextLevel = computed(() => {
  const idx = props.memberLevel + 1
  return idx < levels.length ? levels[idx] : null
})

const progressPercent = computed(() => {
  if (!nextLevel.value) return 100
  const prev = levelInfo.value.threshold
  const next = nextLevel.value.threshold
  const range = next - prev
  if (range <= 0) return 100
  return Math.min(100, Math.max(0, ((props.totalSpent - prev) / range) * 100))
})

const remainStr = computed(() => {
  if (!nextLevel.value) return '0'
  const remain = nextLevel.value.threshold - props.totalSpent
  return remain > 0 ? remain.toFixed(2) : '0.00'
})

const cardGradient = computed(() => {
  const c = levelInfo.value.color
  return {
    background: `linear-gradient(135deg, var(--surface) 40%, ${c}33)`,
    borderColor: c,
  }
})
</script>

<style scoped>
.member-card {
  border: 1px solid;
  border-radius: var(--radius, 12px);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  color: var(--text);
}

.member-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.member-card__level-name {
  font-size: 20px;
  font-weight: 700;
}

.member-card__badge {
  background: var(--surface2);
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text2);
}

.member-card__stats {
  display: flex;
  gap: 24px;
}

.member-card__stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.member-card__stat-label {
  font-size: 12px;
  color: var(--text3);
}

.member-card__stat-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary);
}

.member-card__progress-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.member-card__progress-header {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.member-card__progress-text {
  color: var(--text3);
}

.member-card__progress-amount {
  color: var(--gold);
  font-weight: 600;
}

.member-card__progress-bar {
  height: 6px;
  background: var(--surface2);
  border-radius: 999px;
  overflow: hidden;
}

.member-card__progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--gold), var(--primary));
  border-radius: 999px;
  transition: width 0.5s ease;
}

.member-card__benefits {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.member-card__benefits-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text2);
}

.member-card__benefits-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.member-card__benefit-item {
  font-size: 12px;
  color: var(--text2);
  background: var(--surface2);
  padding: 2px 8px;
  border-radius: 4px;
}
</style>
