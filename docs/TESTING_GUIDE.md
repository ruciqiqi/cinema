# 影院订票系统 - 完整测试方案

## 目录

1. [测试概述](#1-测试概述)
2. [测试技术栈](#2-测试技术栈)
3. [测试分层与覆盖范围](#3-测试分层与覆盖范围)
4. [测试用例矩阵](#4-测试用例矩阵)
5. [后端单元测试详解](#5-后端单元测试详解)
6. [后端集成测试详解](#6-后端集成测试详解)
7. [前端组件测试详解](#7-前端组件测试详解)
8. [前端状态管理测试详解](#8-前端状态管理测试详解)
9. [前端 API 与路由测试详解](#9-前端-api-与路由测试详解)
10. [性能与安全测试建议](#10-性能与安全测试建议)
11. [测试执行与持续集成](#11-测试执行与持续集成)
12. [附录：测试命令速查](#12-附录测试命令速查)

***

## 1. 测试概述

### 1.1 测试目标

本项目为前后端分离架构的影院订票系统，测试的核心目标包括：

- **功能正确性**：验证用户注册/登录、电影浏览、场次选择、选座、订票、支付、改签、退票、优惠券等核心业务流程
- **业务逻辑完备性**：覆盖座位冲突检测、退款规则计算、优惠券校验等关键业务逻辑
- **边界与异常处理**：空值、越界、并发、过期等场景的处理是否健壮
- **代码可维护性**：通过测试驱动设计，提升代码质量与可维护性
- **支持持续集成**：自动化测试与 CI 流程对接，防止回归

### 1.2 测试架构图

```
┌────────────────────────────────────────────────────────────┐
│                     E2E / 集成测试层                          │
│   （端到端流程：注册 → 登录 → 选片 → 选座 → 下单 → 支付）      │
├────────────────────────────────────────────────────────────┤
│                   Controller / API 层测试                     │
│   （请求校验、响应格式、权限拦截、HTTP 状态码）                 │
├────────────────────────────────────────────────────────────┤
│                     Service 业务逻辑层测试                     │
│   （订票、退票、优惠券、场次生成、座位管理等核心逻辑）            │
├────────────────────────────────────────────────────────────┤
│               Repository / Entity 数据访问层测试               │
│   （JPQL 查询、关联映射、字段约束）                             │
├────────────────────────────────────────────────────────────┤
│                 前端 Store / Component 测试                    │
│   （Pinia 状态管理、Vue 组件渲染与交互、API 调用封装）          │
└────────────────────────────────────────────────────────────┘
```

### 1.3 测试金字塔比例建议

| 层级           | 比例  | 工具                    | 示例用例数量估算 |
|----------------|-----|-----------------------|----------|
| E2E / 端到端     | 5%  | Playwright / Cypress  | 5 ~ 10   |
| 集成测试（API）    | 15% | MockMvc / RestAssured | 20 ~ 30  |
| 单元测试（Service） | 60% | JUnit 5 + Mockito     | 50 ~ 80  |
| 前端测试         | 20% | Vitest + Vue Test Utils | 30 ~ 50 |

***

## 2. 测试技术栈

### 2.1 后端技术栈

| 框架 / 工具              | 版本     | 用途                     |
|------------------------|--------|------------------------|
| JUnit 5 (Jupiter)      | 5.8.x  | Java 单元测试框架            |
| Mockito                | 4.x    | Mock / Stub 对象模拟         |
| MockMvc                | 内置     | Spring MVC 集成测试         |
| Spring Boot Test       | 2.7.x  | Spring 上下文与自动装配        |
| JaCoCo（可选）          | 0.8.x  | 代码覆盖率报告               |

### 2.2 前端技术栈

| 框架 / 工具              | 版本      | 用途                     |
|------------------------|---------|------------------------|
| Vitest                 | ^1.6.0  | Vite 原生测试框架          |
| @vue/test-utils        | ^2.4.6  | Vue 组件挂载与交互测试        |
| jsdom                  | ^24.1.0 | 浏览器 DOM 环境模拟          |
| Pinia Testing          | 内置     | Pinia Store 单元测试         |
| c8 / istanbul          | 内置     | 前端覆盖率报告               |

***

## 3. 测试分层与覆盖范围

### 3.1 核心业务模块清单

| 模块          | 关键服务类 / 控制器                          | 业务要点                                |
|-------------|---------------------------------------|-------------------------------------|
| 用户认证        | `UserService`、`AuthController`        | 注册、登录、JWT、密码加密、角色权限           |
| 电影管理        | `MovieService`、`MovieController`      | 电影列表、详情、搜索、上映状态                  |
| 场次管理        | `ShowtimeService`                      | 按电影/日期查询场次、3日滚动场次生成、冲突检测       |
| 座位管理        | `SeatService`                          | 影厅座位图、已售座位标记、座位类型定价              |
| 订单管理        | `BookingService`、`BookingController`  | 创建订单、座位冲突检测、支付、取消、改签、退款规则计算    |
| 优惠券         | `CouponService`、`CouponController`    | 优惠券领取、使用、最低消费校验、折扣/立减两种类型        |
| 小食 / 商品      | `SnackService`                         | 小食列表、购物车扩展价格计算                    |
| 评价 / 评论      | `ReviewService`                        | 发布评论、评分聚合                          |
| 公告 / 通知      | `AnnouncementController` / `Notification` | 运营公告、用户通知、已读标记                   |
| 管理员功能       | `AdminController`                      | 数据管理（电影、场次、影厅）                    |

### 3.2 覆盖范围矩阵

| 模块          | Service 单元测试 | Controller 集成测试 | 前端组件测试 | 前端 Store 测试 |
|-------------|---------------|-------------------|----------|---------------|
| 用户认证        | ✅ 核心          | ✅ 核心             | ✅ 登录弹窗   | ✅ auth        |
| 电影管理        | ✅ 完整          | ✅ 核心             | ✅ MovieCard | —             |
| 场次管理        | ✅ 完整          | ✅ 核心             | ✅ 场次列表   | —             |
| 座位管理        | ✅ 完整          | ✅ 核心             | ✅ SeatMap   | ✅ cart        |
| 订单管理        | ✅ 完整          | ✅ 核心             | ✅ 订单确认   | ✅ cart        |
| 优惠券         | ✅ 完整          | ✅ 核心             | ✅ 优惠券选择器 | ✅ cart        |
| 小食 / 商品      | ✅ 核心          | ✅ 核心             | ✅ 小食选择器  | ✅ cart        |
| 评价 / 评论      | ✅ 核心          | —                 | ✅ 评论组件    | —             |
| 公告 / 通知      | ✅ 核心          | —                 | —        | —             |
| 管理员功能       | —            | ✅ 基础             | —        | —             |

***

## 4. 测试用例矩阵

### 4.1 后端 Service 层 - 关键用例总览

#### 4.1.1 MovieService（电影服务）

| 用例ID   | 场景                     | 输入                   | 预期输出                            | 优先级 |
|--------|------------------------|----------------------|---------------------------------|-----|
| MS-01  | 获取正在上映的电影列表          | status="showing"      | 返回所有 showing 电影，数量正确          | 高   |
| MS-02  | 获取全部电影                | -                    | 返回全部电影                          | 中   |
| MS-03  | 根据 ID 获取电影（存在）       | id=1                 | 返回电影对象，标题/时长/评分字段正确         | 高   |
| MS-04  | 根据 ID 获取电影（不存在）      | id=999               | 返回空 Optional                    | 中   |
| MS-05  | 根据关键词搜索电影             | keyword="动作"          | 匹配标题或类型中包含关键词的电影              | 中   |
| MS-06  | 空关键词搜索返回全部电影         | keyword=""            | 返回全部电影                          | 低   |

#### 4.1.2 UserService（用户服务）

| 用例ID    | 场景                 | 输入                           | 预期输出                                | 优先级 |
|---------|--------------------|------------------------------|-------------------------------------|-----|
| US-01   | 新用户注册成功           | 唯一用户名+密码+手机号                   | success=true, 返回 token 和用户信息         | 高   |
| US-02   | 用户名已存在注册失败       | 用户名已存在                       | success=false, message="用户名已存在"        | 高   |
| US-03   | 用户名过短注册失败        | 用户名长度 < 3                    | success=false, message="用户名至少3个字符"   | 高   |
| US-04   | 密码过短注册失败         | 密码长度 < 6                     | success=false, message="密码至少6个字符"    | 高   |
| US-05   | 登录成功               | 正确用户名+正确密码                    | success=true, 返回 token / username / role | 高   |
| US-06   | 登录用户不存在           | 不存在的用户名                       | success=false, message="用户名或密码错误"    | 高   |
| US-07   | 登录密码错误            | 存在用户+错误密码                     | success=false, message="用户名或密码错误"    | 高   |
| US-08   | 登录密码加密一致性校验      | 注册后使用相同密码登录                  | 密码哈希一致，登录成功                      | 高   |
| US-09   | 修改个人资料            | 更新昵称/头像/性别                    | success=true, 字段正确更新                  | 中   |
| US-10   | 实名认证               | 真实姓名+身份证号                     | success=true, 状态变更                       | 中   |
| US-11   | 积分兑换               | 用户存在+足够积分                      | 扣减积分，返回成功                            | 中   |
| US-12   | 会员等级升级（边界值）      | totalSpent 从 499 → 501          | memberLevel 正确更新                        | 中   |

#### 4.1.3 BookingService（订单服务 - 核心）

| 用例ID     | 场景                                | 输入                               | 预期输出                                          | 优先级 |
|----------|-----------------------------------|----------------------------------|-----------------------------------------------|-----|
| BS-01    | 创建订单 - 成功                          | 有效场次ID + 空闲座位 + 姓名+手机号            | success=true, 生成 bookingCode, 返回总价与座位标签   | 高   |
| BS-02    | 创建订单 - 场次不存在                       | showtimeId=999                    | success=false, message="场次不存在"                 | 高   |
| BS-03    | 创建订单 - 座位已被预订                      | 同一 showtime 两次预订相同座位              | 第二次 success=false, message="座位已被预订，请重新选择" | 高   |
| BS-04    | 创建订单 - 场次已过期                       | showtime 时间早于当前时间                 | success=false, message="该场次已过期，无法购买"        | 高   |
| BS-05    | 创建订单 - VIP 座位按 VIP 价格计费             | 选择 seatType=vip 的座位                | totalPrice = sum(priceVip)                      | 高   |
| BS-06    | 创建订单 - 普通座位按标准价格计费                | 选择 seatType=standard 的座位           | totalPrice = sum(priceStandard)                | 高   |
| BS-07    | 创建订单 - 附带小食价格累加                    | snacksJson="1:2,2:1" + 对应 snack 存在 | 小食金额正确累加到总价                              | 中   |
| BS-08    | 创建订单 - 未登录用户订单（无 userId）         | userId=null                        | 订单成功创建，无 userId 字段                       | 中   |
| BS-09    | 查询订单 - 订单号存在                         | 合法 bookingCode                    | success=true, 返回订单详情 + seatLabels              | 高   |
| BS-10    | 查询订单 - 订单号不存在                        | bookingCode="INVALID"              | success=false, message="订单不存在"                | 高   |
| BS-11    | 查询我的订单 - 有数据                          | userId=1 + 多条订单                  | 返回列表，按 createdAt 倒序                         | 中   |
| BS-12    | 查询我的订单 - 无数据                          | userId=999 + 无订单                 | 返回空列表                                          | 低   |
| BS-13    | 取消订单 - 成功                             | 存在+未取消订单                       | success=true, status="cancelled", 返回 refundAmount | 高   |
| BS-14    | 取消订单 - 重复取消                          | 已取消订单再次取消                      | success=false, message="订单已取消，无法重复操作"      | 高   |
| BS-15    | 取消订单 - 订单不存在                         | bookingCode="INVALID"              | success=false, message="订单不存在"                | 高   |
| BS-16    | 取消订单 - 正确扣减用户积分与消费金额                | userId + 原有消费 > 退款金额             | totalSpent 和 points 正确扣减，memberLevel 合理变化    | 中   |
| BS-17    | 退款预览 - ≥24h 前全额退款                     | hoursUntilShow >= 24               | refundRate=1.0, refundAmount=total               | 高   |
| BS-18    | 退款预览 - 2~24h 部分退款                    | 2 <= hours < 24                   | refundRate=0.8, 金额正确                              | 高   |
| BS-19    | 退款预览 - <2h 仅退部分                      | hoursUntilShow < 2                 | refundRate=0.5 或按最低规则                            | 高   |
| BS-20    | 退款预览 - 订单已取消                         | 已取消订单                            | success=false, message="订单已取消"                | 中   |
| BS-21    | 改签 - 成功                              | 有效原订单 + 新场次 + 新空闲座位           | 原订单取消，生成新 bookingCode                       | 高   |
| BS-22    | 改签 - 原订单不存在                         | 不存在的 bookingCode                 | success=false, message="原订单不存在"               | 中   |
| BS-23    | 改签 - 新场次座位被占用                       | 新座位已被其他订单预订                    | success=false, message="新场次座位已被预订"           | 中   |
| BS-24    | 支付成功 - 更新状态与积分                      | 存在未支付订单 + paymentMethod        | paymentStatus="paid", actualPaid 正确，积分与消费累加    | 高   |
| BS-25    | 支付成功 - 使用优惠券并标记已使用                 | 订单关联未使用 userCouponId         | 支付后 coupon 状态变为 used                           | 中   |
| BS-26    | 支付订单不存在                            | bookingCode="INVALID"              | success=false, message="订单不存在"                | 中   |

#### 4.1.4 ShowtimeService（场次服务）

| 用例ID     | 场景                        | 输入                                | 预期输出                                         | 优先级 |
|----------|---------------------------|-----------------------------------|----------------------------------------------|-----|
| SS-01    | 按电影 ID 查询未来场次               | movieId + 今天日期                       | 返回 showDate >= today 的场次列表                      | 高   |
| SS-02    | 按电影 ID + 日期查询场次            | movieId + "2026-06-13"             | 返回该日期场次，字段正确                                | 高   |
| SS-03    | 按日期查询全部场次                  | date="2026-06-13"                 | 返回该日期所有影院/影厅场次                               | 中   |
| SS-04    | 根据 ID 查询场次（存在）             | id=1                              | 返回 showtime 对象，包含价格与时间                      | 高   |
| SS-05    | 根据 ID 查询场次（不存在）            | id=999                            | 返回 null                                        | 中   |
| SS-06    | 场次日期字符串解析正确                | showTime="14:30"                  | parseTime 返回 14*60+30 = 870                    | 中   |
| SS-07    | 3日滚动场次生成 - 有电影有影厅           | 存在 showing 电影 + 存在 hall       | 删除旧场次并生成新场次，数量合理                           | 高   |
| SS-08    | 3日滚动场次生成 - 无电影时仅清理          | movie 表无 showing 电影               | 清除旧场次，不产生新场次                                 | 中   |
| SS-09    | 3日滚动场次生成 - 无影院时仅清理          | hall 表为空                         | 清除旧场次，不产生新场次                                 | 低   |
| SS-10    | 场次时间冲突 - 同一影厅时间窗口不重叠        | 同一 hall 不同时段                     | tryAssign 不产生时间重叠记录                             | 高   |

#### 4.1.5 SeatService（座位服务）

| 用例ID     | 场景                       | 输入                  | 预期输出                                   | 优先级 |
|----------|--------------------------|---------------------|----------------------------------------|-----|
| SEA-01   | 获取影厅所有座位                | hallId               | 返回所有座位，按 rowLabel + seatNum 有序        | 高   |
| SEA-02   | 获取座位图 - 标记已预订座位          | showtimeId + hallId  | 返回 seats 列表，每个 seat 带 booked 标记        | 高   |
| SEA-03   | 获取已预订座位 ID 集合 - 空场次       | 新场次无预订              | 返回空 Set                                   | 中   |
| SEA-04   | 获取已预订座位 ID 集合 - 有预订        | 场次含 2 个 confirmed 订单 | 返回 2 个座位 ID                              | 高   |
| SEA-05   | 已取消订单座位不纳入已售集合           | cancelled 订单         | 已取消订单的座位不出现在结果中                       | 高   |

#### 4.1.6 CouponService（优惠券服务）

| 用例ID     | 场景                       | 输入                                | 预期输出                                          | 优先级 |
|----------|--------------------------|-----------------------------------|-----------------------------------------------|-----|
| CP-01    | 获取可用优惠券 - 日期与剩余数量正确         | 存在 active 优惠券且在日期区间内              | 返回满足条件的优惠券列表                                 | 高   |
| CP-02    | 获取可用优惠券 - 过期过滤                | endDate < today                      | 不包含过期优惠券                                       | 高   |
| CP-03    | 获取可用优惠券 - 已领完过滤              | usedCount >= usageLimit              | 不包含已领完优惠券                                      | 中   |
| CP-04    | 领取优惠券 - 成功                       | userId + 可用 couponId               | success=true, 用户优惠券记录生成，usedCount +1         | 高   |
| CP-05    | 领取优惠券 - 已领取过                   | 同一用户重复领取同一优惠券                   | success=false, message="已领取过此优惠券"                | 高   |
| CP-06    | 领取优惠券 - 优惠券不存在                | couponId=999                       | success=false, message="优惠券不存在"                  | 中   |
| CP-07    | 领取优惠券 - 已领完                    | usedCount == usageLimit             | success=false, message="优惠券已被领完"                 | 中   |
| CP-08    | 查询用户优惠券 - 包含优惠券详情           | userId                              | 返回列表含 code/name/type/value/endDate/status         | 中   |
| CP-09    | 应用折扣券 - 订单金额满足最低消费          | type="discount", value=10, min=50  | discount = orderAmount * 0.10                         | 高   |
| CP-10    | 应用现金券 - 订单金额满足最低消费          | type="cash", value=20, min=50      | discount = 20, 不超过 orderAmount                       | 高   |
| CP-11    | 应用优惠券 - 订单金额不满最低消费          | orderAmount < minAmount             | success=false, message="订单金额不满 xx 元，无法使用"    | 高   |
| CP-12    | 应用优惠券 - 优惠券已使用                | status="used"                       | success=false, message="优惠券已使用或已过期"              | 中   |
| CP-13    | 应用优惠券 - 非本人优惠券                | 他人的 userCouponId                  | success=false, message="优惠券不存在"                    | 中   |
| CP-14    | 现金券 - 优惠金额不超过订单金额            | orderAmount=10, cash value=20      | discount=10（不超过订单金额）                             | 中   |

### 4.2 后端 Controller 层 - 关键用例总览

| 用例ID      | 接口                                  | 场景                    | 输入                                 | 预期响应                                    | 优先级 |
|-----------|-------------------------------------|-----------------------|------------------------------------|-----------------------------------------|-----|
| CTL-01    | POST /api/auth/register             | 注册请求参数完整            | username + password + phone        | 200 OK + success=true                  | 高   |
| CTL-02    | POST /api/auth/register             | 注册请求参数缺失（无密码）       | username + phone                    | 400 Bad Request / 业务错误提示                 | 高   |
| CTL-03    | POST /api/auth/login                | 登录成功                  | 正确用户名+密码                           | 200 OK + token 返回                     | 高   |
| CTL-04    | POST /api/auth/login                | 登录密码错误                | 存在用户+错误密码                           | 200 OK + success=false                  | 高   |
| CTL-05    | POST /api/bookings                  | 创建订单 - 参数完整           | showtimeId + seats + userName + phone | 200 OK + bookingCode 返回                | 高   |
| CTL-06    | POST /api/bookings                  | 创建订单 - 缺座位             | 缺 seatIds                         | 200 OK + success=false + "请选择座位"     | 高   |
| CTL-07    | GET /api/bookings/query             | 查询订单 - 订单号存在           | code="合法订单号"                       | 200 OK + 订单详情返回                      | 高   |
| CTL-08    | GET /api/bookings/my                | 未登录查我的订单             | 无 userId                           | success=false + "请先登录"               | 高   |
| CTL-09    | POST /api/bookings/cancel           | 取消订单成功                | 合法 bookingCode                    | 200 OK + success=true                   | 高   |
| CTL-10    | POST /api/bookings/pay              | 支付成功                  | 合法订单号 + paymentMethod           | 200 OK + actualPaid 返回                 | 高   |
| CTL-11    | GET /api/movies                     | 获取电影列表                | -                                  | 200 OK + Array                          | 高   |
| CTL-12    | GET /api/movies/{id}                | 获取电影详情                | id=1                               | 200 OK + Movie 对象                      | 中   |
| CTL-13    | GET /api/showtimes/by-movie/{movieId}| 按电影查询场次               | movieId + 可选 date                   | 200 OK + Array                          | 高   |
| CTL-14    | GET /api/seats/map                  | 查询座位图                 | showtimeId + hallId                 | 200 OK + seats + bookedSeatIds           | 高   |

### 4.3 前端测试 - 关键用例总览

#### 4.3.1 组件测试

| 用例ID       | 组件                      | 场景                              | 预期结果                              | 优先级 |
|------------|-------------------------|---------------------------------|-----------------------------------|-----|
| FC-01      | MovieCard               | 渲染电影信息                        | 标题/类型/评分/时长正确显示                 | 高   |
| FC-02      | MovieCard               | 点击跳转到电影详情                     | router.push 调用 /movie/:id           | 高   |
| FC-03      | AuthModal               | 登录 Tab 输入用户名密码提交                | 触发登录 API，成功后关闭弹窗                | 高   |
| FC-04      | AuthModal               | 注册 Tab 校验输入                      | 密码长度不足时显示错误提示                    | 高   |
| FC-05      | SeatMap                 | 渲染座位图并标记已售                     | 已售座位不可点击，VIP 座位样式不同              | 高   |
| FC-06      | SeatMap                 | 点击选择/取消座位并触发回调                 | selectedSeats 正确更新，触发父组件事件          | 高   |
| FC-07      | CouponSelector          | 选择优惠券后更新折扣金额                   | appliedCoupon 状态更新，discount 正确          | 中   |
| FC-08      | SnackSelector           | 增加/减少小食数量                       | snackCart 数量与小计更新                     | 中   |
| FC-09      | OrderCard               | 渲染订单信息                         | 电影名/影厅/时间/状态/座位标签正确展示             | 高   |
| FC-10      | ReviewSection           | 已登录用户显示评论输入框                  | 未登录显示"请先登录"提示                      | 中   |
| FC-11      | StarRating              | 点击设置评分并触发回调                    | rating 值正确更新                            | 低   |
| FC-12      | HeroCarousel            | 轮播自动播放与手动切换                    | 可点击箭头切换海报                          | 低   |

#### 4.3.2 状态管理（Store）测试

| 用例ID       | Store    | 场景                                   | 预期结果                              | 优先级 |
|------------|----------|--------------------------------------|-----------------------------------|-----|
| FS-01      | auth     | 初始化状态为空                              | token/username/role 皆为空              | 高   |
| FS-02      | auth     | setSession 更新状态与 localStorage         | state + localStorage 同步更新            | 高   |
| FS-03      | auth     | logout 清空状态                             | state 清空 + localStorage 移除三项           | 高   |
| FS-04      | auth     | isAdmin 角色判断正确                         | role="admin" → true，其他 → false        | 高   |
| FS-05      | cart     | 初始化购物车为空                             | selectedSeats 空数组，appliedCoupon null    | 高   |
| FS-06      | cart     | calcSeatTotal - 标准座位/VIP 价格正确        | 标准座位累加 priceStandard，VIP 累加 priceVip | 高   |
| FS-07      | cart     | calcSnackTotal - 数量乘价格正确              | 小计 = qty * price 循环累加                 | 中   |
| FS-08      | cart     | calcFinalTotal - 扣除优惠券                     | total = seatTotal + snackTotal - coupon     | 高   |
| FS-09      | cart     | reset 清空购物车                             | 所有选择与优惠券状态重置                      | 高   |
| FS-10      | cart     | seatCount 计算属性正确                        | 返回 selectedSeats.length                 | 高   |

#### 4.3.3 API 与路由测试

| 用例ID       | 模块        | 场景                             | 预期结果                                  | 优先级 |
|------------|-----------|--------------------------------|---------------------------------------|-----|
| FA-01      | api/index | axios 实例包含 baseURL + 超时设置     | request 正确附加 cinema_token header      | 高   |
| FA-02      | api/index | 401 响应触发登出逻辑                    | localStorage 清空，跳转到登录页                 | 中   |
| FA-03      | router    | 未登录访问 /my/orders 重定向到首页       | 路由守卫正确拦截                              | 高   |
| FA-04      | router    | 已登录访问 /admin 需 admin 角色         | 非 admin 角色被拦截                            | 中   |

***

## 5. 后端单元测试详解

> 测试文件路径：`src/test/java/com/cinema/service/*.java`

### 5.1 MovieServiceTests

- **Mock 依赖**：`MovieRepository`
- **核心注解**：`@ExtendWith(MockitoExtension.class)`
- **断言重点**：返回列表数量、Optional 状态、字段值一致

### 5.2 UserServiceTests

- **Mock 依赖**：`UserRepository`、`JwtUtil`
- **关键场景**：
  - 密码通过 `MessageDigest` 做 SHA-256，注册与登录必须使用同一套算法
  - 边界：用户名长度、密码长度、手机号格式（可选）

### 5.3 BookingServiceTests（重点）

- **Mock 依赖**：`BookingRepository`、`BookingSeatRepository`、`SeatRepository`、`ShowtimeRepository`、`MovieRepository`、`SnackRepository`、`RefundRuleRepository`、`UserRepository`、`NotificationRepository`、`UserCouponRepository`、`SeatService`
- **关键场景**：
  - **座位冲突**：两次调用 `createBooking` 使用相同的 showtimeId + seatId，第二次必须返回失败
  - **退款规则**：根据 hoursUntilShow 命中不同 RefundRule（测试用例建议固定 now 时间 / 通过依赖注入时间服务）
  - **改签原子性**：在 `@Transactional` 下验证原订单取消与新订单创建一致

### 5.4 ShowtimeServiceTests

- **Mock 依赖**：`ShowtimeRepository`、`MovieRepository`、`HallRepository`
- **关键场景**：
  - `maintainRollingShowtimes()` 需要对时间进行 mock（建议通过 `Clock` bean 或参数化 today）
  - `parseTime("HH:MM")` 边界值测试（"00:00"、"23:59"、无效格式）

### 5.5 SeatServiceTests

- **Mock 依赖**：`SeatRepository`、`BookingSeatRepository`、`BookingRepository`
- **关键场景**：
  - `findConfirmedByShowtimeId` 仅返回 confirmed 状态订单
  - 已取消订单不影响新座位选择

### 5.6 CouponServiceTests

- **Mock 依赖**：`CouponRepository`、`UserCouponRepository`
- **关键场景**：
  - 现金券（cash）discount=min(value, orderAmount)
  - 折扣券（discount）discount=orderAmount * (value/100)
  - `LocalDateTime.now()` 通过 `DateTimeFormatter` 解析，可通过固定日期或参数化方法注入

***

## 6. 后端集成测试详解

> 推荐使用 `@SpringBootTest` + `MockMvc` / `TestRestTemplate`，也可使用 H2 内存数据库进行轻量集成测试。

### 6.1 集成测试配置建议

在 `src/test/resources/application-test.yml` 中：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:cinema_test;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
```

### 6.2 关键集成用例

| 用例ID       | 场景                              | 验证点                                  |
|------------|---------------------------------|--------------------------------------|
| IT-01      | 注册 → 登录 → 获取 token → 调用受保护接口 | token 可正确解析 userId，权限拦截生效           |
| IT-02      | 创建订单 → 支付 → 取消 → 退款全流程        | 订单状态流转：confirmed → paid → cancelled       |
| IT-03      | 同座位并发预订（2 线程）               | 只有一个线程成功，另一个失败，数据最终一致               |
| IT-04      | 领取优惠券 → 应用优惠券 → 支付使用优惠券      | usedCount 正确 +1，userCoupon status 变 used      |

***

## 7. 前端组件测试详解

### 7.1 基础约定

- 每个组件测试文件命名：`src/tests/components/<ComponentName>.test.js`
- 使用 `mount()` 挂载组件；`shallowMount()` 用于仅测试当前组件行为
- 路由通过 `createMemoryHistory` 或 spy on `router.push`
- HTTP 请求通过 `vi.mock('axios')` 模拟

### 7.2 组件测试模式示例

```js
// 1. 基础渲染测试
it('renders required props', () => {
  const wrapper = mount(Component, { props: {...}, global: {...} })
  expect(wrapper.find('[data-testid="xxx"]').exists()).toBe(true)
})

// 2. 交互测试
it('handles user interaction', async () => {
  const wrapper = mount(Component, { ... })
  await wrapper.find('button').trigger('click')
  expect(wrapper.emitted()).toHaveProperty('update')
})

// 3. 响应式状态测试
it('reacts to prop change', async () => {
  const wrapper = mount(Component, { props: { x: 1 } })
  await wrapper.setProps({ x: 2 })
  expect(wrapper.text()).toContain('2')
})
```

***

## 8. 前端状态管理测试详解

### 8.1 Pinia Store 测试模板

```js
import { createPinia, setActivePinia } from 'pinia'
import { useXxxStore } from '@/stores/xxx'
import { beforeEach, it, expect, vi } from 'vitest'

beforeEach(() => {
  setActivePinia(createPinia())
  vi.clearAllMocks()
  localStorage.clear()
})
```

### 8.2 可测性建议

- Store 方法**纯函数化**：尽量减少外部依赖，避免直接硬编码 `new Date()`
- 关键业务计算（如价格汇总、折扣计算）抽取为独立函数，便于单独测试
- `localStorage` 读写集中在单一方法内，便于 mock

***

## 9. 前端 API 与路由测试详解

### 9.1 API 模块测试

- Mock `axios.create` 返回的实例
- 验证 `request` 拦截器正确附加 `cinema_token`
- 验证 `response` 拦截器对 401 的处理（登出 + 跳转登录）

### 9.2 路由守卫测试

- 真实 `createRouter({ history: createMemoryHistory(), routes })`
- 通过 `router.push('/my/orders')` 验证最终 `router.currentRoute.value.path`
- 通过设置/清除 `localStorage.cinema_token` 模拟登录状态

***

## 10. 性能与安全测试建议

### 10.1 性能测试

| 场景            | 工具              | 目标                     |
|---------------|-----------------|------------------------|
| 列表接口响应时间     | JMeter / k6     | P95 < 500ms            |
| 创建订单接口 TPS    | JMeter / ab     | ≥ 100 TPS              |
| 首页首屏加载        | Lighthouse      | LCP < 2.5s             |
| 大影厅座位图渲染      | Vitest + 自定义计时   | 500 座位渲染 < 100ms     |

### 10.2 安全测试

| 场景            | 方式                       | 验证点                         |
|---------------|--------------------------|-----------------------------|
| JWT 有效性校验    | 手工 curl / 自动化             | 过期/伪造 token 返回 401        |
| SQL 注入防护     | OWASP ZAP                | 所有输入无注入                  |
| 越权访问订单       | 手工 + 自动化                  | `userId` 只能查看自己的订单         |
| 密码传输与存储      | 代码审计 / HTTPS 配置检查      | 传输 HTTPS；存储使用强哈希（BCrypt）   |
| 优惠券金额边界     | 单元测试（负数/极大值）             | discount 不超过订单金额              |

***

## 11. 测试执行与持续集成

### 11.1 本地执行命令

```bash
# 后端：运行全部单元测试
mvn test

# 后端：仅运行 Service 层测试
mvn test -Dtest='*ServiceTests'

# 后端：运行单个测试方法
mvn test -Dtest=BookingServiceTests#testCreateBookingSeatConflict

# 后端：生成 JaCoCo 覆盖率报告（如已引入插件）
mvn clean test jacoco:report

# 前端：运行测试（监听模式）
cd cinema-frontend
npm run test

# 前端：运行测试 + 覆盖率报告
npm run test:coverage

# 前端：仅运行组件测试
npx vitest run src/tests/components

# 前端：仅运行 Store 测试
npx vitest run src/tests/stores
```

### 11.2 CI 集成配置（GitHub Actions 示例）

```yaml
name: Cinema CI
on: [push, pull_request]
jobs:
  backend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
      - run: mvn -B test --file pom.xml

  frontend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
      - run: npm ci
        working-directory: cinema-frontend
      - run: npm run test:coverage
        working-directory: cinema-frontend
```

### 11.3 质量门禁建议

| 指标             | 阈值      |
|----------------|---------|
| 后端 Service 层覆盖率  | ≥ 80%   |
| 后端行覆盖率        | ≥ 70%   |
| 前端核心组件覆盖率     | ≥ 70%   |
| 前端 Store 覆盖率   | ≥ 85%   |
| 自动化测试通过数量     | 100%    |
| 阻塞性缺陷数         | 0       |
| API 响应时间 P95    | < 500ms |

***

## 12. 附录：测试命令速查

| 命令                                        | 说明                |
|-------------------------------------------|-------------------|
| `mvn test`                                | 运行后端全部测试         |
| `mvn test -Dtest=<类名>`                    | 运行指定测试类           |
| `mvn test -Dtest=<类名>#<方法名>`             | 运行单个测试方法          |
| `npm run test`（cinema-frontend 目录）    | 前端测试（监听模式）        |
| `npm run test:coverage`（cinema-frontend）| 前端测试 + 覆盖率报告      |
| `npx vitest run src/tests/<目录>`          | 运行指定目录下的前端测试      |

---

**文档版本**：v2.0  
**最后更新**：2026-06-12  
**适用项目**：cinema-booking 影院订票系统
