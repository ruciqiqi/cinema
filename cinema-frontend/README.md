# 星空影院 — 前端项目

Vue 3 + Vite 构建的在线影院订票系统前端，与 Spring Boot 后端配合使用。

## 技术栈

| 技术 | 说明 |
|---|---|
| Vue 3 (Composition API) | `<script setup>` 语法，响应式数据绑定 |
| Vite 8 | 开发服务器、HMR 热更新、ESBuild 打包 |
| Vue Router 4 | SPA 路由，组件懒加载 + 首页预加载 |
| Pinia 2 | 状态管理（auth、cart 两个 Store） |
| Axios | HTTP 客户端，拦截器自动附加 JWT Token |
| Canvas API | 图表绘制，零第三方依赖 |
| CSS Variables | 暗黑/明亮主题切换 |

## 快速启动

### 前置条件

- Node.js 18+
- MySQL 8.0 已启动，`cinema` 数据库已创建
- 后端已配置并启动

### 1. 配置环境变量

项目根目录（`cinema/`）下有 `.env.example`，复制为 `.env` 并填入实际值：

```bash
cp ../.env.example ../.env
```

`.env` 内容：

```ini
DB_HOST=localhost
DB_PORT=3306
DB_NAME=cinema
DB_USERNAME=root
DB_PASSWORD=你的数据库密码
JWT_SECRET=你的JWT密钥
```

### 2. 启动后端

```bash
cd ..                        # 回到项目根目录 cinema/
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`，首次启动自动建表并写入种子数据。

### 3. 启动前端

```bash
cd cinema-frontend
npm install
npm run dev
```

看到 `Local: http://localhost:5173` 即成功。

### 4. 访问

浏览器打开 `http://localhost:5173`

### 测试账号

| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | `admin` | `123456` |
| 普通用户 | `test` | `123456` |

## 常见问题

**Q: 启动前端报 `port 5173 is already in use`**

```bash
taskkill /F /IM node.exe
npm run dev
```

**Q: 启动后端报 `port 8080 is already in use`**

```bash
taskkill /F /IM java.exe
mvn spring-boot:run
```

**Q: 登录提示"网络错误"**

检查浏览器地址是 `http://localhost:5173`（不是 8080）。8080 是后端 API，不提供页面。

**Q: API 返回 HTML 而不是 JSON**

Vite 不在 `cinema-frontend` 目录启动，代理配置未加载。确保 `cd cinema-frontend && npm run dev`。

## 项目结构

```
src/
├── main.js                  # 入口 — 挂载 Pinia + Router
├── App.vue                  # 根组件 — Header + RouterView + Toast
├── api/index.js             # Axios — 自动 JWT + 错误处理
├── router/index.js          # 路由（10条）
├── stores/                  # Pinia
│   ├── auth.js              # 登录状态、Token、角色
│   └── cart.js              # 选座、卖品、优惠券
├── assets/styles/main.css   # 全局样式 + 主题
├── components/              # 16个组件
│   ├── AppHeader.vue          # 导航 + 通知 + 主题
│   ├── AuthModal.vue          # 登录/注册
│   ├── HeroCarousel.vue       # 首页轮播
│   ├── MovieCard.vue          # 影片卡片
│   ├── SeatMap.vue            # 座位图
│   ├── SnackSelector.vue      # 卖品
│   ├── PaymentMethod.vue      # 支付方式
│   ├── CouponSelector.vue     # 优惠券
│   ├── StarRating.vue         # 10星评分
│   ├── ReviewSection.vue      # 评价区
│   ├── OrderCard.vue          # 订单卡片
│   ├── MemberCard.vue         # 会员卡
│   ├── ChartBar/Line/Pie.vue  # Canvas图表
│   ├── PageLoader.vue         # 页面进度条
│   └── ToastNotification.vue  # 全局消息
├── views/                   # 10个页面
│   ├── HomeView.vue           # 首页
│   ├── MovieDetail.vue        # 影片详情+场次+评价
│   ├── SeatSelection.vue      # 选座+卖品
│   ├── OrderConfirm.vue       # 下单+支付（需登录）
│   ├── OrderSuccess.vue       # 取票码+二维码
│   ├── OrderLookup.vue        # 订单查询
│   ├── MyOrders.vue           # 我的订单（需登录）
│   ├── UserCenter.vue         # 个人中心（需登录）
│   ├── AdminDashboard.vue     # 后台管理（需管理员）
│   └── BigScreen.vue          # 数据大屏
└── public/posters/          # 19张SVG海报
```

## 路由

| 路径 | 页面 | 权限 |
|---|---|---|
| `/` | 首页 | - |
| `/movie/:id` | 影片详情 | - |
| `/seats/:showtimeId` | 选座 | - |
| `/order/confirm` | 确认订单 | 需登录 |
| `/order/success` | 支付成功 | 需登录 |
| `/order/lookup` | 订单查询 | - |
| `/my/orders` | 我的订单 | 需登录 |
| `/user/center` | 个人中心 | 需登录 |
| `/admin` | 后台管理 | 管理员 |
| `/admin/bigscreen` | 数据大屏 | 管理员 |

## 功能

- 首页 — 轮播 / 公告 / 日期筛选 / 类型筛选 / 搜索 / 正在热映 + 即将上映
- 选座 — 可视化座位图 / 一键推荐 / 卖品 / 优惠券
- 下单 — 三种支付 / 亲友代购 / 取票码 + Canvas 二维码
- 订单 — 退票预览弹窗 / 阶梯退款 / 订单管理
- 评价 — 10星评分 / 观影后才能评价
- 会员 — 6级会员 / 积分兑换 / 优惠券中心
- 通知 — 用户独立通知 / 一键已读 / 点击跳转
- 后台 — Canvas 图表 / CRUD / 数据大屏
- 主题 — 暗黑/明亮切换，localStorage 持久化
