# 影院订票系统 - 测试文档

## 目录
1. [测试概述](#1-测试概述)
2. [测试技术栈](#2-测试技术栈)
3. [测试目录结构](#3-测试目录结构)
4. [运行测试](#4-运行测试)
5. [后端测试](#5-后端测试)
   - [测试文件列表](#51-测试文件列表)
   - [测试用例详解](#52-测试用例详解)
6. [前端测试](#6-前端测试)
   - [测试文件列表](#61-测试文件列表)
   - [测试用例详解](#62-测试用例详解)
7. [测试规范](#7-测试规范)
8. [持续集成建议](#8-持续集成建议)

---

## 1. 测试概述

本项目采用**前后端分离**的测试架构，分别为后端和前端建立了独立的测试体系。测试的核心目标是：

- **确保功能正确性**：验证各个模块的核心业务逻辑
- **防止回归问题**：确保代码修改不会破坏原有功能
- **提高代码质量**：通过测试驱动开发，提升代码可维护性
- **支持持续集成**：为自动化测试和持续集成提供基础

---

## 2. 测试技术栈

| 模块 | 框架 | 版本 | 说明 |
|------|------|------|------|
| 后端 | JUnit 5 | 5.8.x | Java 单元测试框架 |
| 后端 | Mockito | 4.x | Mock 对象框架 |
| 后端 | Spring Boot Test | 2.7.x | Spring Boot 测试支持 |
| 前端 | Vitest | 1.6.x | Vue 3 测试框架 |
| 前端 | Vue Test Utils | 2.4.x | Vue 组件测试工具 |
| 前端 | jsdom | 24.x | 浏览器环境模拟 |

---

## 3. 测试目录结构

```
cinema/
├── src/test/java/com/cinema/          # 后端测试
│   ├── CinemaApplicationTests.java    # 应用上下文测试
│   └── service/                       # 服务层测试
│       ├── MovieServiceTests.java     # 电影服务测试
│       └── UserServiceTests.java      # 用户服务测试
└── cinema-frontend/src/tests/         # 前端测试
    ├── components/                    # 组件测试
    │   └── MovieCard.test.js          # 电影卡片组件测试
    └── stores/                        # 状态管理测试
        └── auth.test.js               # 认证状态测试
```

---

## 4. 运行测试

### 4.1 后端测试

```bash
# 进入项目根目录
cd cinema

# 运行所有后端测试
mvn test

# 运行特定测试类
mvn test -Dtest=MovieServiceTests

# 运行特定测试方法
mvn test -Dtest=MovieServiceTests#testGetAllMovies
```

### 4.2 前端测试

```bash
# 进入前端目录
cd cinema-frontend

# 运行前端测试（开发模式，监听文件变化）
npm run test

# 运行测试并生成覆盖率报告
npm run test:coverage

# 运行特定测试文件
npx vitest run src/tests/components/MovieCard.test.js
```

---

## 5. 后端测试

### 5.1 测试文件列表

| 测试文件 | 测试类 | 用例数 | 覆盖模块 |
|---------|--------|--------|----------|
| `CinemaApplicationTests.java` | `CinemaApplicationTests` | 1 | Spring Boot 应用上下文 |
| `MovieServiceTests.java` | `MovieServiceTests` | 5 | 电影服务 |
| `UserServiceTests.java` | `UserServiceTests` | 7 | 用户服务 |

### 5.2 测试用例详解

#### 5.2.1 CinemaApplicationTests

| 测试方法 | 测试目的 | 验证内容 |
|---------|---------|---------|
| `contextLoads()` | 验证 Spring 上下文加载 | 应用能正常启动 |

#### 5.2.2 MovieServiceTests

| 测试方法 | 测试目的 | 验证内容 |
|---------|---------|---------|
| `testGetShowingMovies()` | 获取正在上映的电影 | 返回状态为 showing 的电影列表 |
| `testGetAllMovies()` | 获取所有电影 | 返回所有电影列表 |
| `testGetMovieById()` | 根据ID获取电影 | 返回指定ID的电影 |
| `testGetMovieByIdNotFound()` | 获取不存在的电影 | 返回空 Optional |
| `testSearchMovies()` | 搜索电影 | 根据关键词搜索 |

#### 5.2.3 UserServiceTests

| 测试方法 | 测试目的 | 验证内容 |
|---------|---------|---------|
| `testRegisterUserSuccess()` | 用户注册成功 | 创建新用户并返回成功 |
| `testRegisterUserExists()` | 用户已存在 | 返回用户名已存在错误 |
| `testRegisterUsernameTooShort()` | 用户名太短 | 返回用户名至少3个字符 |
| `testRegisterPasswordTooShort()` | 密码太短 | 返回密码至少6个字符 |
| `testLoginSuccess()` | 用户登录成功 | 返回用户信息和 token |
| `testLoginUserNotFound()` | 用户不存在 | 返回用户名或密码错误 |
| `testLoginWrongPassword()` | 密码错误 | 返回用户名或密码错误 |

---

## 6. 前端测试

### 6.1 测试文件列表

| 测试文件 | 测试模块 | 用例数 | 覆盖范围 |
|---------|---------|--------|----------|
| `MovieCard.test.js` | MovieCard 组件 | 2 | 组件渲染、交互 |
| `auth.test.js` | Auth Store | 4 | 状态管理 |

### 6.2 测试用例详解

#### 6.2.1 MovieCard.test.js

| 测试方法 | 测试目的 | 验证内容 |
|---------|---------|---------|
| `renders movie information correctly` | 组件渲染 | 电影标题、类型、评分、时长正确显示 |
| `triggers navigation when clicked` | 点击交互 | 点击卡片触发路由导航 |

#### 6.2.2 auth.test.js

| 测试方法 | 测试目的 | 验证内容 |
|---------|---------|---------|
| `initializes with empty state` | 初始状态 | 用户名、token、角色为空 |
| `sets session successfully` | 设置会话 | 登录后状态正确更新 |
| `logs out successfully` | 退出登录 | 登出后状态清空 |
| `checks if user is admin` | 管理员判断 | 正确识别管理员角色 |

---

## 7. 测试规范

### 7.1 测试命名规范

- **测试类命名**：`<被测试类名>Tests`
  - 示例：`MovieServiceTests`、`UserServiceTests`

- **测试方法命名**：`test<操作><场景>`
  - 示例：`testLoginSuccess`、`testRegisterUserExists`

### 7.2 测试编写规范

1. **前置条件**：使用 `@BeforeEach` 设置测试环境
2. **后置清理**：使用 `@AfterEach` 清理测试数据
3. **Mock 对象**：使用 Mockito 模拟外部依赖
4. **断言优先**：优先使用 `assertTrue`、`assertEquals` 等断言
5. **测试隔离**：每个测试方法独立，互不影响

### 7.3 测试覆盖标准

| 优先级 | 模块 | 覆盖率要求 |
|--------|------|-----------|
| 高 | Controller | ≥ 80% |
| 高 | Service | ≥ 80% |
| 中 | Repository | ≥ 60% |
| 中 | 核心组件 | ≥ 70% |
| 低 | UI 展示组件 | ≥ 50% |

---

## 8. 持续集成建议

### 8.1 CI/CD 流程

```
代码提交 → 自动构建 → 运行测试 → 代码分析 → 部署
```

### 8.2 Jenkins/GitHub Actions 配置示例

**后端测试步骤**：
```yaml
- name: Run Backend Tests
  run: mvn test
  working-directory: cinema
```

**前端测试步骤**：
```yaml
- name: Install Frontend Dependencies
  run: npm install
  working-directory: cinema-frontend

- name: Run Frontend Tests
  run: npm run test:coverage
  working-directory: cinema-frontend
```

### 8.3 测试结果报告

测试运行完成后会生成以下报告：

| 报告类型 | 位置 | 说明 |
|---------|------|------|
| 后端测试报告 | `target/surefire-reports/` | JUnit 测试报告 |
| 前端测试报告 | `coverage/` | Vitest 覆盖率报告 |

---

## 附录：测试命令速查

| 命令 | 说明 |
|------|------|
| `mvn test` | 运行所有后端测试 |
| `mvn test -Dtest=<类名>` | 运行指定测试类 |
| `npm run test` | 运行前端测试（开发模式） |
| `npm run test:coverage` | 运行前端测试并生成覆盖率 |
| `npx vitest run` | 运行前端测试（单次） |

---

## 文档版本

- 创建日期：2026-05-13
- 版本：v1.0
- 作者：系统管理员