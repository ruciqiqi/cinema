import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const routes = [
  { path: '/', name: 'Home', component: HomeView },
  { path: '/movie/:id', name: 'MovieDetail', component: () => import('../views/MovieDetail.vue') },
  { path: '/seats/:showtimeId', name: 'SeatSelection', component: () => import('../views/SeatSelection.vue') },
  { path: '/order/confirm', name: 'OrderConfirm', component: () => import('../views/OrderConfirm.vue') },
  { path: '/order/success', name: 'OrderSuccess', component: () => import('../views/OrderSuccess.vue') },
  { path: '/order/lookup', name: 'OrderLookup', component: () => import('../views/OrderLookup.vue') },
  { path: '/my/orders', name: 'MyOrders', component: () => import('../views/MyOrders.vue') },
  { path: '/user/center', name: 'UserCenter', component: () => import('../views/UserCenter.vue') },
  { path: '/admin', name: 'AdminDashboard', component: () => import('../views/AdminDashboard.vue') },
  { path: '/admin/bigscreen', name: 'BigScreen', component: () => import('../views/BigScreen.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
