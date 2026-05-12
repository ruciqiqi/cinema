# 影院订票系统 - 问题记录与解决方案

## 目录
1. [环境变量无法加载问题](#1-环境变量无法加载问题)
2. [PowerShell 命令语法问题](#2-powershell-命令语法问题前端启动)
3. [项目结构问题](#3-项目结构问题)
4. [Git 追踪 target 目录问题](#4-git-追踪-target-目录问题)

---

## 1. 环境变量无法加载问题

### 问题描述
使用 `mvn spring-boot:run` 启动后端服务时，Spring Boot 无法正确读取 `.env` 文件中的环境变量，导致数据库连接失败。

### 错误信息
```
Caused by: com.mysql.cj.exceptions.WrongArgumentException: Failed to parse the host:port pair '${DB_HOST}:${DB_PORT}'
Caused by: java.lang.NumberFormatException: For input string: "${DB_PORT}"
```

### 原因分析
Spring Boot 的 Maven 插件 (`spring-boot-maven-plugin`) 在运行时**不会自动加载 `.env` 文件**。环境变量占位符 `${DB_HOST}`, `${DB_PORT}` 等没有被替换为实际值，直接传递给了 MySQL 连接器，导致解析失败。

### 解决方法
在 `src/main/resources/application.yml` 文件中，为所有环境变量添加**默认值**。即使环境变量未设置，应用也能使用默认值启动。

**修改前：**
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?...
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}

app:
  jwt-secret: ${JWT_SECRET}
```

**修改后：**
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:cinema}?...
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}

app:
  jwt-secret: ${JWT_SECRET:cinemabookingsystemsecretkey1234567890abcdef}
```

### 关键配置说明
| 环境变量 | 默认值 | 说明 |
|---------|--------|------|
| `DB_HOST` | `localhost` | 数据库主机地址 |
| `DB_PORT` | `3306` | 数据库端口 |
| `DB_NAME` | `cinema` | 数据库名称 |
| `DB_USERNAME` | `root` | 数据库用户名 |
| `DB_PASSWORD` | `123456` | 数据库密码 |
| `JWT_SECRET` | `cinemabookingsystemsecretkey1234567890abcdef` | JWT 加密密钥 |

### 验证方法
启动服务后，检查日志中是否包含以下成功信息：
```
HikariPool-1 - Start completed.
Tomcat started on port(s): 8080 (http)
Started CinemaApplication in X seconds
```

### 替代方案
如果需要使用 `.env` 文件管理环境变量，可以：
1. 添加 `spring-boot-starter-webflux` 依赖
2. 使用 `@PropertySource` 注解加载 `.env` 文件
3. 或使用第三方库如 `io.github.cdimascio:java-dotenv`

### 修复状态
✅ **已修复**

---

## 2. PowerShell 命令语法问题（前端启动）

### 问题描述
在 PowerShell 中执行 `npm install && npm run dev` 时失败，报错：
```
标记"&&"不是此版本中的有效语句分隔符。
```

### 原因分析
PowerShell 5.x 及以下版本不支持 `&&` 连接符语法。

### 解决方法
分开执行命令：
```powershell
npm install
npm run dev
```

### 修复状态
✅ **已解决**

---

## 3. 项目结构问题

### 3.1 重复的前端目录 ⚠️

#### 问题描述
项目根目录下存在两个功能重复的前端目录：
- `cinema-frontend/` - Vue 3 + Vite 项目（现代技术栈）
- `frontend/` - 纯 HTML/CSS/JS 项目（旧技术栈）

#### 影响
- 维护成本高
- 容易造成混淆
- 代码重复

#### 建议修复方案
删除旧的 `frontend/` 目录，统一使用 `cinema-frontend/`

#### 修复状态
⏳ **待修复**

---

### 3.2 根目录 package.json 配置错误 ⚠️

#### 问题描述
根目录下的 `package.json` 声明了 axios、pinia、vue-router 依赖，但没有任何脚本。这些依赖应该在 `cinema-frontend/` 项目中。

#### 影响
- 依赖声明位置错误
- 容易造成混淆

#### 建议修复方案
删除根目录的 `package.json` 和 `package-lock.json`

#### 修复状态
⏳ **待修复**

---

### 3.3 cinema-frontend 缺少必要依赖 ⚠️

#### 问题描述
`cinema-frontend/package.json` 只声明了 `vue`，但代码中使用了：
- `pinia`（状态管理）- `src/stores/auth.js`
- `vue-router`（路由）- `src/router/index.js`
- `axios`（HTTP请求）- `src/api/index.js`

#### 影响
- 可能导致运行时错误
- 依赖不完整

#### 解决方法
在 `cinema-frontend/package.json` 中添加缺失的依赖：
```json
{
  "dependencies": {
    "vue": "^3.5.32",
    "axios": "^1.16.0",
    "pinia": "^3.0.4",
    "vue-router": "^4.6.4"
  }
}
```

#### 修复状态
⏳ **package.json 已更新，依赖待安装**

---

### 3.4 安全风险：.env 文件 ⚠️

#### 问题描述
`.gitignore` 中已包含 `.env`，但项目根目录下存在 `.env` 文件（包含数据库密码、JWT密钥等敏感信息）。

#### 影响
- 敏感信息可能泄露到版本控制

#### 建议修复方案
确保 `.env` 不被提交到版本控制（.gitignore 已配置）。

#### 修复状态
⏳ **待确认**

---

### 3.5 测试缺失 📝

#### 问题描述
项目缺少测试代码：
- 后端：`src/test/java/` 目录不存在
- 前端：无测试配置

#### 建议修复方案
后续可添加单元测试和集成测试。

#### 修复状态
📝 **待处理（后续优化）**

---

## 项目结构问题汇总

| 序号 | 问题类型 | 位置 | 严重程度 | 修复状态 |
|------|---------|------|---------|---------|
| 1 | 结构重复 | `frontend/` 目录 | 🔴 高 | ⏳ 待修复 |
| 2 | 配置错误 | 根目录 `package.json` | 🟡 中 | ⏳ 待修复 |
| 3 | 依赖缺失 | `cinema-frontend/package.json` | 🔴 高 | ⏳ package.json 更新 |
| 4 | 安全风险 | `.env` | 🟡 中 | ⏳ 待确认 |
| 5 | 测试缺失 | 全局 | 🟢 低 | 📝 待处理 |

---

## 分步修复计划

### 阶段一：确认当前状态 ✅
- [x] 后端服务正常运行 (http://localhost:8080)
- [x] 前端服务正常运行 (http://localhost:5173)

### 阶段二：修复问题 3（依赖缺失）
- [x] 更新 cinema-frontend/package.json 添加依赖
- [ ] 安装依赖并验证前端正常运行

### 阶段三：修复问题 2（根目录 package.json）
- [ ] 删除根目录的 package.json 和 package-lock.json
- [ ] 验证项目仍可正常运行

### 阶段四：修复问题 1（重复前端目录）
- [ ] 删除 frontend/ 目录
- [ ] 验证项目仍可正常运行

---

## 文档版本
- 创建日期：2026-05-12
- 最后更新：2026-05-12
- 版本：v1.2
- 作者：系统管理员
