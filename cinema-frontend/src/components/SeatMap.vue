<template>
  <div class="seat-map">
    <!-- Screen indicator -->
    <div class="screen-indicator">
      <span class="screen-text">银 幕</span>
    </div>

    <!-- Seat rows -->
    <div class="rows-container">
      <div
        v-for="(row, rowIdx) in seatRows"
        :key="rowIdx"
        class="seat-row"
      >
        <span class="row-label row-label--left">{{ row.label }}</span>
        <div class="seats">
          <div
            v-for="seat in row.seats"
            :key="seat.id"
            class="seat"
            :class="getSeatClass(seat)"
            :title="`${seat.rowLabel}${seat.seatNum} ${seatTypeLabel(seat.seatType)}`"
            @click="toggleSeat(seat)"
          >
            <span class="seat-num">{{ seat.seatNum }}</span>
          </div>
        </div>
        <span class="row-label row-label--right">{{ row.label }}</span>
      </div>
    </div>

    <!-- Legend -->
    <div class="legend">
      <div class="legend-item">
        <span class="legend-box legend-box--available"></span>
        <span>可选</span>
      </div>
      <div class="legend-item">
        <span class="legend-box legend-box--selected"></span>
        <span>已选</span>
      </div>
      <div class="legend-item">
        <span class="legend-box legend-box--booked"></span>
        <span>已售</span>
      </div>
      <div class="legend-item">
        <span class="legend-box legend-box--vip"></span>
        <span>VIP</span>
      </div>
      <div class="legend-item">
        <span class="legend-box legend-box--couple"></span>
        <span>情侣座</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  seatMap: {
    type: Array,
    required: true
    // Array of { id, rowLabel, seatNum, seatType, booked }
  },
  selectedSeats: {
    type: Array,
    required: true
  },
  showtime: {
    type: Object,
    default: () => ({ priceStandard: 0, priceVip: 0 })
  }
})

const emit = defineEmits(['update:selectedSeats'])

const seatRows = computed(() => {
  const grouped = {}
  for (const seat of props.seatMap) {
    if (!grouped[seat.rowLabel]) {
      grouped[seat.rowLabel] = []
    }
    grouped[seat.rowLabel].push(seat)
  }
  // Sort each row by seatNum
  for (const label of Object.keys(grouped)) {
    grouped[label].sort((a, b) => a.seatNum - b.seatNum)
  }
  // Sort row labels alphabetically
  const sortedLabels = Object.keys(grouped).sort()
  return sortedLabels.map(label => ({
    label,
    seats: grouped[label]
  }))
})

function isSelected(seat) {
  return props.selectedSeats.some(s => s.id === seat.id)
}

function getSeatClass(seat) {
  if (seat.booked) return 'seat--booked'
  if (isSelected(seat)) return 'seat--selected'
  if (seat.seatType === 'couple') return 'seat--couple'
  if (seat.seatType === 'vip') return 'seat--vip'
  return 'seat--available'
}

function seatTypeLabel(type) {
  const map = { standard: '普通座', vip: 'VIP座', couple: '情侣座' }
  return map[type] || type
}

function toggleSeat(seat) {
  if (seat.booked) return
  const idx = props.selectedSeats.findIndex(s => s.id === seat.id)
  let newSelected
  if (idx >= 0) {
    newSelected = [...props.selectedSeats]
    newSelected.splice(idx, 1)
  } else {
    newSelected = [...props.selectedSeats, seat]
  }
  emit('update:selectedSeats', newSelected)
}
</script>

<style scoped>
.seat-map {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 16px;
  user-select: none;
}

.screen-indicator {
  width: 70%;
  max-width: 400px;
  height: 32px;
  background: linear-gradient(135deg, var(--surface2), var(--surface));
  border-bottom-left-radius: 50%;
  border-bottom-right-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(255, 255, 255, 0.05);
}

.screen-text {
  color: var(--text3);
  font-size: 14px;
  letter-spacing: 12px;
}

.rows-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.seat-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.row-label {
  width: 24px;
  text-align: center;
  font-size: 13px;
  color: var(--text3);
  font-weight: 600;
  flex-shrink: 0;
}

.seats {
  display: flex;
  gap: 6px;
  flex-wrap: nowrap;
}

.seat {
  width: 32px;
  height: 32px;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 11px;
  transition: all 0.2s ease;
  border: 2px solid transparent;
}

.seat-num {
  color: inherit;
  font-weight: 500;
}

/* Available (blue) */
.seat--available {
  background: var(--primary2);
  border-color: var(--primary);
  color: var(--primary);
}

.seat--available:hover {
  background: var(--primary);
  color: #fff;
}

/* Selected (red) */
.seat--selected {
  background: var(--danger);
  border-color: var(--danger);
  color: #fff;
}

/* Booked (grey) */
.seat--booked {
  background: var(--surface2);
  border-color: var(--border);
  color: var(--text3);
  cursor: not-allowed;
  opacity: 0.6;
}

/* VIP (gold border) */
.seat--vip {
  background: var(--primary2);
  border-color: var(--gold);
  color: var(--gold);
}

.seat--vip:hover {
  background: var(--gold);
  color: #fff;
}

/* Couple (pink) */
.seat--couple {
  background: #fce4ec;
  border-color: #ec407a;
  color: #ec407a;
  border-radius: 16px;
  width: 36px;
}

.seat--couple:hover {
  background: #ec407a;
  color: #fff;
}

/* Legend */
.legend {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text2);
}

.legend-box {
  width: 18px;
  height: 18px;
  border-radius: 4px;
  border: 2px solid transparent;
}

.legend-box--available {
  background: var(--primary2);
  border-color: var(--primary);
}

.legend-box--selected {
  background: var(--danger);
  border-color: var(--danger);
}

.legend-box--booked {
  background: var(--surface2);
  border-color: var(--border);
  opacity: 0.6;
}

.legend-box--vip {
  background: var(--primary2);
  border-color: var(--gold);
}

.legend-box--couple {
  background: #fce4ec;
  border-color: #ec407a;
}
</style>
