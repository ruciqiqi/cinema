# 影院订票系统 - 代码质量改进建议

> 生成时间: 2026-05-17
> 项目路径: d:\code\gitv1\cinema

---

## 🔴 P0 - 安全性问题（需优先处理）

### 1. 密码存储不够安全
**位置**: `src/main/java/com/cinema/config/DataInitializer.java` L388-398

**问题描述**:
- 当前使用 SHA-256 哈希密码，但没有添加盐值（Salt）
- 容易被彩虹表攻击和暴力破解

**当前代码**:
```java
MessageDigest md = MessageDigest.getInstance("SHA-256");
byte[] hash = md.digest(password.getBytes("UTF-8"));
```

**建议方案**:
- 引入 Spring Security 的 BCryptPasswordEncoder
- 或使用 Argon2 加密

**参考实现**:
```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

---

### 2. JWT密钥硬编码风险
**位置**: `src/main/resources/application.yml` L19

**问题描述**:
```yaml
app:
  jwt-secret: ${JWT_SECRET:cinemabookingsystemsecretkey1234567890abcdef}
```
- 生产环境必须使用强随机密钥
- 不应使用可预测的默认值

**建议方案**:
- 确保生产环境设置复杂的环境变量
- 添加密钥长度验证（至少32字符）
- 在应用启动时检测弱密钥并发出警告

---

### 3. 缺少输入验证
**位置**: 所有 Controller 类

**问题描述**:
- 多个 Controller 直接接收参数没有校验
- 用户名、密码、手机号等缺乏格式验证
- SQL注入和XSS攻击风险

**建议方案**:
- 添加 Jakarta Validation 依赖
- 为请求DTO添加 `@NotBlank`, `@Size`, `@Pattern` 等注解
- 配置全局异常处理器返回友好的错误信息

**参考实现**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## 🟠 P1 - 代码质量问题

### 4. 大量重复的getter/setter
**位置**: `src/main/java/com/cinema/entity/User.java` 及所有 Entity 类

**问题描述**:
- User.java 有23个getter/setter方法
- 手写容易出错且维护成本高
- 代码臃肿，可读性差

**建议方案**:
引入 Lombok 简化代码

**参考实现**:
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

**优化后代码**:
```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    // ... 其他字段
}
```

---

### 5. 数据库查询效率低下
**位置**: `src/main/java/com/cinema/service/BookingService.java`

**问题1 - L167-172**: 按用户ID查询订单
```java
// 当前实现 - 内存过滤
List<Booking> bookings = bookingRepository.findAll();
for (Booking b : bookings) {
    if (b.getUserId() != null && b.getUserId().equals(userId)) {
        list.add(buildBookingMap(b));
    }
}
```

**优化方案**:
```java
// 在 BookingRepository 添加方法
List<Booking> findByUserId(Long userId);

// Service中使用
List<Booking> bookings = bookingRepository.findByUserId(userId);
```

---

**问题2 - L380-387**: 获取已预订座位
```java
// 当前实现 - 内存过滤
List<Booking> bookings = bookingRepository.findAll().stream()
    .filter(b -> b.getShowtimeId().equals(showtimeId) 
              && "confirmed".equals(b.getStatus()))
    .collect(Collectors.toList());
```

**优化方案**:
```java
// 在 BookingRepository 添加方法
@Query("SELECT b FROM Booking b WHERE b.showtimeId = :showtimeId AND b.status = 'confirmed'")
List<Booking> findConfirmedByShowtimeId(@Param("showtimeId") Long showtimeId);
```

---

### 6. DataInitializer代码重复
**位置**: `src/main/java/com/cinema/config/DataInitializer.java`

**问题描述**:
- L96-141 和 L340-363 定义了相同的电影数据数组
- 代码冗余，维护困难

**建议方案**:
- 提取为常量或配置文件
- 或合并两个方法为一个可配置的方法

---

### 7. 违反依赖注入原则
**位置**: `src/main/java/com/cinema/service/BookingService.java` L54

**问题描述**:
```java
SeatService seatService = new SeatService(seatRepository, bookingSeatRepository, bookingRepository);
```
- 手动new对象绕过了Spring的依赖注入
- 难以测试，失去Spring容器的管理能力

**建议方案**:
- 通过构造函数注入 SeatService
- 或将 getBookedSeatIds 方法移到 Repository 层

---

## 🟡 P2 - 开发效率优化

### 8. 统一API响应格式
**位置**: 所有 Controller 类

**问题描述**:
- 当前返回格式不统一
- 有些用 `Map<String, Object>`
- 应统一使用现有的 ApiResponse.java

**建议方案**:
```java
// 统一的响应格式
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    // getters, setters
}

// Controller 使用示例
@GetMapping("/{id}")
public ApiResponse<Movie> detail(@PathVariable Long id) {
    return movieService.getMovieById(id)
        .map(ApiResponse::success)
        .orElse(ApiResponse.error("影片不存在"));
}
```

---

### 9. 缺少全局异常处理
**问题描述**:
- 没有 @ControllerAdvice 统一处理异常
- 异常信息不友好

**建议方案**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ApiResponse.error("验证失败: " + message);
    }
    
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleGeneral(Exception ex) {
        return ApiResponse.error("系统错误: " + ex.getMessage());
    }
}
```

---

### 10. RESTful API 规范改进
**当前**: `GET /api/movies?keyword=xxx`
**建议**: `GET /api/movies/search?keyword=xxx`

**建议方案**:
- 使用 `@GetMapping("/search")` 分离搜索接口
- 使用 `@RequestParam` 明确参数
- 添加分页支持: `GET /api/movies?page=0&size=10`

---

## 🟢 P3 - 生产级优化

### 11. 生产环境配置
**位置**: `src/main/resources/application.yml` L12

**问题描述**:
```yaml
jpa:
  hibernate:
    ddl-auto: update
```
- `update` 不适合生产环境
- 可能导致数据丢失或不一致

**建议方案**:
- 生产环境使用 `ddl-auto: validate`
- 引入 Flyway 或 Liquibase 进行数据库迁移
- 分离 dev 和 prod 配置文件

**文件结构**:
```
resources/
├── application.yml          # 通用配置
├── application-dev.yml      # 开发环境
└── application-prod.yml     # 生产环境
```

---

### 12. 缺少缓存层
**问题描述**:
- 高频查询（电影列表）每次都查数据库
- 增加数据库压力

**建议方案**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

**使用示例**:
```java
@Cacheable(value = "movies", key = "#root.method.name")
public List<Movie> getAllMovies() {
    return movieRepository.findByStatus("showing");
}

@CacheEvict(value = "movies", allEntries = true)
public void addMovie(Movie movie) { ... }
```

---

### 13. 单元测试覆盖不足
**当前测试**:
- `CinemaApplicationTests.java`
- `MovieServiceTests.java`
- `UserServiceTests.java`

**建议补充**:
- Service层业务逻辑测试
- Repository层数据操作测试
- Controller层接口测试（使用 MockMvc）
- 集成测试

**参考工具**:
```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
</dependency>
```

---

## 📋 改进优先级总结

| 优先级 | 问题 | 影响 | 预计工时 |
|--------|------|------|----------|
| P0 | 密码加盐 | 安全风险 | 2h |
| P0 | 输入验证 | 安全风险 | 3h |
| P1 | 查询优化 | 性能 | 2h |
| P1 | 统一响应格式 | 可维护性 | 2h |
| P2 | Lombok | 开发效率 | 1h |
| P2 | 全局异常处理 | 可维护性 | 1h |
| P3 | Redis缓存 | 性能 | 3h |
| P3 | 数据库迁移 | 生产稳定性 | 4h |
| P3 | 测试覆盖 | 代码质量 | 持续 |

---

## 📝 待办清单

- [ ] P0: 引入 BCrypt 密码加密
- [ ] P0: 添加输入验证注解和全局异常处理
- [ ] P1: 优化数据库查询，添加 Repository 方法
- [ ] P1: 统一 API 响应格式
- [ ] P2: 引入 Lombok 简化代码
- [ ] P2: 添加全局异常处理器
- [ ] P3: 引入 Redis 缓存
- [ ] P3: 配置 Flyway 数据库迁移
- [ ] P3: 补充单元测试覆盖
