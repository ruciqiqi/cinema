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
| Canvas API | 图表绘制（柱状图/折线图/饼图），零依赖 |
| CSS Variables | 暗黑/明亮主题切换 |

## 启动方式（完整流程）

### 1. 环境准备

| 依赖 | 版本要求 | 验证命令 |
|---|---|---|
| JDK | 8+ | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| Node.js | 18+ | `node -v` |
| MySQL | 8.0 | `mysql -uroot -p` |

### 2. 创建数据库

登录 MySQL，执行：

```sql
CREATE DATABASE IF NOT EXISTS cinema
  DEFAULT CHARACTER SET = 'utf8mb4';
```

### 3. 配置数据库连接

编辑 `../src/main/resources/application.yml`，确认 MySQL 用户名和密码与你的本地环境一致：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cinema?useSSL=false&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: root
    password: 你的密码
```

### 4. 启动后端

打开终端，进入项目根目录：

```bash
cd cinema          # 项目根目录
mvn spring-boot:run
```

看到 `Started CinemaApplication` 即启动成功，后端运行在 **`http://localhost:8080`**。

首次启动会自动建表并写入种子数据（19 部影片、9 个影厅、297 个场次、15 种卖品等）。

### 5. 启动前端

打开**第二个终端**：

```bash
cd cinema/cinema-frontend
npm install        # 首次运行需安装依赖
npm run dev
```

看到 `VITE ready` 和 `Local: http://localhost:5173` 即启动成功。

### 6. 访问系统

浏览器打开 **`http://localhost:5173`**

### 7. 登录

| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | `admin` | `123456` |
| 普通用户 | `test` | `123456` |

---

### 常见问题

**Q: 启动前端报 `port 5173 is already in use`**

```bash
# Windows
taskkill /F /IM node.exe
npm run dev
```

**Q: 启动后端报 `port 8080 is already in use`**

```bash
# Windows
taskkill /F /IM java.exe
mvn spring-boot:run
```

**Q: 登录提示"网络错误"**

检查是否访问的地址是 `http://localhost:5173`（不是 8080）。8080 是后端 API，不提供页面。

**Q: 页面图片不显示**

SVG 海报存放在 `public/posters/` 目录。确保 `npm run dev` 是从 `cinema-frontend` 目录启动的。

**Q: 数据库表没创建**

检查 MySQL 是否启动，`application.yml` 中的密码是否正确。确认 `spring.jpa.hibernate.ddl-auto` 配置为 `update`。

---

### 生产构建（可选）

```bash
npm run build
```

产物在 `dist/` 目录，部署到 Nginx 等静态服务器。部署时需配置反向代理将 `/api/*` 转发到后端 `http://localhost:8080`。

## 项目结构

```
src/
├── main.js                  # 入口 — 挂载 Pinia + Router
├── App.vue                  # 根组件 — Header + RouterView + Toast
│
├── api/index.js             # Axios 封装 — 自动 JWT + 错误处理
├── router/index.js          # 路由配置（10条）
│
├── stores/                  # Pinia 状态管理
│   ├── auth.js              # 登录状态、Token、角色
│   └── cart.js              # 选座、卖品、优惠券、支付
│
├── assets/styles/main.css   # 全局样式 — CSS 变量 + 主题
│
├── components/              # 16 个组件
│   ├── AppHeader.vue          # 导航栏 + 通知 + 主题切换
│   ├── AuthModal.vue          # 登录/注册弹窗
│   ├── HeroCarousel.vue       # 首页影片轮播
│   ├── MovieCard.vue          # 影片卡片（毛玻璃 + 渐变）
│   ├── SeatMap.vue            # 座位图（可选/已选/已售/VIP）
│   ├── SnackSelector.vue      # 卖品加购
│   ├── PaymentMethod.vue      # 支付方式选择
│   ├── CouponSelector.vue     # 优惠券选择
│   ├── StarRating.vue         # 10 星评分
│   ├── ReviewSection.vue      # 评价区（观影后可见）
│   ├── OrderCard.vue          # 订单卡片
│   ├── MemberCard.vue         # 会员卡（等级+进度+特权）
│   ├── ChartBar.vue           # Canvas 柱状图
│   ├── ChartLine.vue          # Canvas 折线图
│   ├── ChartPie.vue           # Canvas 饼图
│   ├── PageLoader.vue         # 页面切换进度条
│   └── ToastNotification.vue  # 全局消息提示
│
├── views/                   # 10 个页面
│   ├── HomeView.vue           # 首页（轮播+日期/类型筛选+搜索）
│   ├── MovieDetail.vue        # 影片详情（海报+场次+评价）
│   ├── SeatSelection.vue      # 选座（一键推荐+卖品加购）
│   ├── OrderConfirm.vue       # 确认订单（优惠券+支付）
│   ├── OrderSuccess.vue       # 支付成功（取票码+二维码）
│   ├── OrderLookup.vue        # 订单查询（退票预览弹窗）
│   ├── MyOrders.vue           # 我的订单（退票+取消）
│   ├── UserCenter.vue         # 个人中心（信息/会员/积分/优惠券/安全）
│   ├── AdminDashboard.vue     # 后台管理（统计+CRUD）
│   └── BigScreen.vue          # 数据可视化大屏
│
└── public/posters/          # 19 张 SVG 影片海报
```

## 路由一览

| 路径 | 页面 | 说明 |
|---|---|---|
| `/` | HomeView | 正在热映 + 即将上映 |
| `/movie/:id` | MovieDetail | 详情 + 场次 + 评价 |
| `/seats/:showtimeId` | SeatSelection | 选座 + 卖品 |
| `/order/confirm` | OrderConfirm | 下单 + 支付（需登录） |
| `/order/success` | OrderSuccess | 取票码 + 二维码 |
| `/order/lookup` | OrderLookup | 输入取票码查询 |
| `/my/orders` | MyOrders | 我的订单（需登录） |
| `/user/center` | UserCenter | 个人中心（需登录） |
| `/admin` | AdminDashboard | 后台管理（需管理员） |
| `/admin/bigscreen` | BigScreen | 数据大屏（需管理员） |

## 核心功能

- **首页** — 热门轮播 / 公告跑马灯 / 日期筛选 / 类型筛选 / 搜索 / 正在热映 + 即将上映专区
- **选座** — 可视化座位图 / 一键推荐最佳座位 / 卖品加购 / 优惠券
- **下单** — 三种支付方式 / 亲友代购 / 取票码 + Canvas 二维码
- **订单** — 退票预览弹窗 / 阶梯退款计算 / 我的订单列表
- **评价** — 10 星评分 / 观影后才能评价
- **会员** — 6 级会员 / 积分兑换 / 优惠券中心
- **通知** — 用户独立通知 / 点击跳转 / 全部清除
- **后台** — Canvas 图表 / CRUD 管理 / 数据大屏
- **主题** — 暗黑/明亮切换，localStorage 持久化

## 测试账号

| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | `admin` | `123456` |
| 普通用户 | `test` | `123456` |
