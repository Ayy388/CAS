# CAS 后端 MVP 实施计划（TDD 模式）

> 严格依据：PRD.md · 数据库设计.md · 后端技术架构设计.md · API文档.md · 前端技术架构设计.md · 页面原型说明.md · cas-init.sql

---

## 开发模式：TDD（测试驱动开发）

每个模块的开发循环：

```
① 编写测试（验证契约/业务规则）
    ↓  测试预期失败（RED）
② 编写实现（让测试通过）
    ↓  测试通过（GREEN）
③ 重构（优化代码，保持测试绿色）
```

### TDD 层级

| 层级 | 使用框架 | 覆盖范围 |
|------|---------|---------|
| **Unit** | JUnit 5 + Mockito | Service 层业务规则验证（不加载 Spring 上下文） |
| **Integration** | `@SpringBootTest` + `@AutoConfigureMockMvc` | Controller 层 HTTP 请求/响应契约 |
| **Repository** | `@DataJpaTest` / `@MybatisPlusTest` + H2 | Mapper SQL 映射正确性 |

### 测试命名规范

```
// Unit test
@Test
void should_throw_409_when_duplicate_enrollment_in_same_campaign() { }

// Integration test
@Test
void should_return_200_with_token_when_login_success() { }

// Repository test
@Test
void should_select_student_offerings_with_course_name_joined() { }
```

---

## Phase 1：项目骨架 + Flyway 迁移

**目标**：可启动的 Spring Boot 项目，数据库已初始化。

### 创建文件

| 文件 | 说明 |
|------|------|
| `backend/pom.xml` | 全部依赖：Spring Boot Web/Security/Validation, MyBatis-Plus, Redis, RabbitMQ, JWT, SpringDoc, Flyway, Lombok, HuTool, POI, H2(test), JUnit 5 |
| `backend/src/main/resources/application.yml` | 完整配置（见后端架构设计.md §十一） |
| `backend/src/main/resources/application-test.yml` | H2 内存库测试配置 |
| `backend/sql/V1__init_schema.sql` | cas-init.sql 完整拷贝（7 表 + 索引 + FK） |
| `backend/sql/V2__seed_admin.sql` | admin 用户（BCrypt: admin123） |
| `backend/src/main/java/com/cas/CasApplication.java` | `@SpringBootApplication` + `@MapperScan("com.cas.**.mapper")` + `@EnableScheduling` |
| `backend/src/main/java/com/cas/config/RedisConfig.java` | RedisTemplate<String, Object> bean |
| `backend/src/main/java/com/cas/config/MyBatisPlusConfig.java` | 分页拦截器 |
| `backend/src/main/java/com/cas/config/OpenApiConfig.java` | SpringDoc 配置 |
| `backend/src/main/java/com/cas/config/WebMvcConfig.java` | CORS: allow localhost:5173 |

### TDD 验证

```java
// Phase1ApplicationTest.java
@SpringBootTest
class Phase1ApplicationTest {
    @Test void should_load_application_context() { }
    @Test void should_have_flyway_migrations_applied() {
        // 验证 7 张表存在
    }
    @Test void should_have_admin_user_seeded() {
        // 验证 sys_user 中有 admin 账号
    }
}
```

**验证命令**：`mvn clean test` + `mvn spring-boot:run` 启动无报错

---

## Phase 2：公共层（TDD）

**目标**：响应封装、异常处理、枚举定义。

### TDD 先行 — 先写测试

| 测试 | 验证点 |
|------|--------|
| `ApiResponse.success(data)` → `code=200, data=输入值` | 响应结构 |
| `ApiResponse.error(409, "msg")` → `code=409, message="msg"` | 错误格式 |
| `PageResponse.of(IPage)` → items/total/page/pageSize 正确 | 分页封装 |
| `BusinessException` → `GlobalExceptionHandler` 返回 409 | 异常捕获 |
| 枚举值确认 `UserRole.STUDENT.name()` = "STUDENT" | 枚举一致性 |
| `MethodArgumentNotValidException` → 400 | 参数校验 |

### 创建文件

- `common/result/ApiResponse.java` — 泛型 `ApiResponse<T>`，含 `code`/`message`/`data`
- `common/result/PageResponse.java` — 含 `items`/`total`/`page`/`pageSize`
- `common/exception/BusinessException.java` — 运行时异常，携带 code + message
- `common/exception/GlobalExceptionHandler.java` — `@RestControllerAdvice`，5 个 handler
- `common/enums/` — 6 个枚举（UserRole, CourseType, CampaignStatus, OfferingStatus, EnrollmentStatus, NotificationType）

### 关键约束

枚举字符串必须与 `API文档.md` §1.4 和 `数据库设计.md` §七 完全一致。写个测试遍历验证：

```java
@Test
void enum_values_must_match_api_doc() {
    assertThat(CourseType.ELECTIVE_GENERAL.name()).isEqualTo("ELECTIVE_GENERAL");
    assertThat(CampaignStatus.ACTIVE.name()).isEqualTo("ACTIVE");
    // ... 全部枚举值
}
```

---

## Phase 3：认证模块（TDD）

**目标**：JWT 登录 + 鉴权 + Token 黑名单登出。

### TDD 先行

| 测试 | HTTP | 预期 |
|------|------|------|
| 正确用户名密码登录 | POST /auth/login | 200 + token + user |
| 错误密码 | POST /auth/login | 401 |
| 已禁用用户 | POST /auth/login | 409 |
| 带 Token 访问 /auth/me | GET /auth/me | 200 + user |
| 无 Token 访问 /auth/me | GET /auth/me | 401 |
| 过期/伪造 Token | GET /auth/me | 401 |
| 非 ADMIN 访问 admin 接口 | GET /admin/semesters | 403 |

### 创建文件

- `config/SecurityConfig.java` — 路由权限矩阵
- `auth/JwtTokenProvider.java` — Token 签发/验证（HMAC-SHA256, 24h, jti）
- `auth/JwtAuthenticationFilter.java` — OncePerRequestFilter: 提取 → 校验 → 黑名单检查 → 设上下文
- `auth/AuthController.java` — `/login` + `/me`
- `auth/LoginRequest.java` / `LoginResponse.java`

---

## Phase 4：学期模块（TDD）

**目标**：管理员学期 CRUD + 激活/停用。

### 模块模式（后续模块均遵循）

```
modules/semester/
├── SemesterController.java      # @RestController + @RequestMapping
├── SemesterService.java          # 接口
├── SemesterServiceImpl.java      # @Service 实现
├── entity/Semester.java          # @TableName + @Data
├── mapper/SemesterMapper.java    # extends BaseMapper<Semester>
└── dto/
    ├── SemesterRequest.java      # 请求 DTO（@NotBlank, @NotNull, @AssertTrue）
    └── SemesterVO.java           # 响应 VO（camelCase）
```

### TDD 先行

| 测试 | 验证点 |
|------|--------|
| 创建学期 → 返回 200 + 含 id + status=INACTIVE | 创建 |
| 创建时结束日期早于开始日期 → 400 | 参数校验 |
| 列表查询 → 分页正确 | 分页 |
| 激活学期 → status 变 ACTIVE，其他学期变 INACTIVE | 业务规则 |
| 激活已 ACTIVE 的学期 → 变为 INACTIVE（开关效果） | 业务规则 |
| 编辑学期 → 字段更新但不影响 status | 编辑 |

### 业务规则

- 激活一个学期时，其他学期自动变为 INACTIVE
- 学期不允许删除

---

## Phase 5：课程模块（TDD）

**目标**：管理员课程 CRUD。

### TDD 先行

| 测试 | 验证点 |
|------|--------|
| 创建课程 → 200 + id | 创建 |
| 重复 code → 409 "课程编号已存在" | UNIQUE 约束 |
| 编辑课程 → 字段更新 | 编辑 |
| 列表查询（keyword/type 筛选）→ 分页正确 | 筛选 |
| 删除无开课的课程 → 200 | 删除 |
| 删除有开课的课程 → 409 | 外键保护 |

### 业务规则

- `course.code` UNIQUE
- 删除前检查 `course_offering` 中无引用

---

## Phase 6：活动模块 + 定时任务（TDD）

**目标**：管理员活动 CRUD + 状态机 + 定时轮询 + 学生端当前活动查询（Redis 缓存）。

### TDD 先行

| 测试 | 验证点 |
|------|--------|
| 创建 PENDING 活动 → 200 + status=PENDING | 创建 |
| 同学期重复创建 PENDING/ACTIVE → 409 | 业务规则 |
| start → status=ACTIVE | 状态机 |
| end → status=ENDED | 状态机 |
| ACTIVE 不可再 start → 409 | 状态机 |
| ENDED 不可再 end → 409 | 状态机 |
| 定时任务：超时 PENDING→ACTIVE / ACTIVE→ENDED | 调度 |
| GET /campaigns/current → 返回当前 ACTIVE 活动 | 学生端查询 |
| 缓存命中：两次调用 `/campaigns/current` 验证缓存策略 | 缓存 |

### 缓存

`cas:campaign:current` — Cache-Aside，TTL 30s

### 额外创建

- `scheduler/CampaignScheduler.java` — `@Scheduled(fixedRate = 60000)` 自动切换状态

---

## Phase 7：开课模块 + 课程大厅（TDD）

**目标**：管理员开课 CRUD + 学生端课程列表/详情（Redis 缓存）。

### TDD 先行

| 测试 | 验证点 |
|------|--------|
| 创建开课 → 200 + status=PENDING + enrolledCount=0 | 创建 |
| 关联不存在的 semester/course/teacher → 404 | 外键校验 |
| maxCapacity < minEnrollment → 400 | 参数校验 |
| GET /api/v1/courses → 只返回 ELECTIVE 类型 + 当前学期 | 学生端列表 |
| 列表返回 seatsRemaining(计算字段) | VO |
| keyword 搜索 → 模糊匹配 course.name | 搜索 |
| GET /api/v1/courses/{id} → 详情含 enrolledCount + description | 详情 |
| 缓存验证：首次查 DB，第二次查缓存 | 缓存 |

### 创建文件

- `modules/offering/` 完整模块
- `modules/offering/mapper/OfferingMapper.xml` — 自定义 JOIN SQL

### 缓存键

- `cas:course:list:{type}:{page}` — 5min
- `cas:offering:{id}` — 30s
- 写操作（增/删/改）→ `DEL cas:course:list:*` + `DEL cas:offering:{id}`

### VO 计算

`seatsRemaining = maxCapacity - enrolledCount`（不在 DB 中存储）

---

## Phase 8：选课模块（核心，TDD 重中之重）

**目标**：抢课 11 步流程 + 退课（Redis + MySQL + MQ）。

### TDD 先行

| 测试 | 步骤# | 预期 |
|------|-------|------|
| 抢课成功 → enrollmentId 返回 | 全部 | 200 |
| 2s 内重复抢课 → 429 | ① | Redis 去重 |
| 无 ACTIVE 活动时抢课 → 409 | ② | 活动校验 |
| 同活动已选课后再次抢课 → 409 | ③ | BR-03 |
| 不符合开放范围 → 409 | ④ | BR-05 |
| 满员后抢课 → 409 | ⑤⑥⑦ | 容量校验 |
| 退课成功 → 200 + 名额恢复 | — | 退课 |
| 退课不属于自己的 enrollment → 403 | — | 归属校验 |
| 活动结束后退课 → 409 | — | 时间校验 |
| 退课后可重新选课（名额够） | — | UNIQUE 不阻塞 |

### 11 步流程（严格按 `后端技术架构设计.md` §7.1）

```
① Redis SET NX EX 2 去重 → 429
② DB 查询当前 ACTIVE 活动 → 409
③ DB BR-03 同活动已选课 → 409
④ DB BR-05 开放范围 → 409
⑤ Redis GET 容量预检 → 409
⑥ Redis INCR 临时计数 → 超 cap DECR + 409
⑦ MySQL 原子 UPDATE enrolled_count + 1 → 409
⑧ MySQL INSERT enrollment
⑨ DECR 临时计数 + 清缓存
⑩ 事务提交
⑪ @TransactionalEventListener → MQ enrollment.log
```

### 创建文件

- `modules/enrollment/` 完整模块 + `EnrollmentMapper.xml`（JOIN 查询）
- `mq/event/EnrollEvent.java`

### 关键约束

- 所有 Redis INCR 临时计数，无论成功/失败都必须 DECR
- MQ 消息在 `@TransactionalEventListener(phase = AFTER_COMMIT)` 中发送
- 事务回滚时也需 DECR 临时计数

---

## Phase 9：审核模块 + MQ 异步通知（TDD）

**目标**：审核列表/通过/驳回 + MQ 异步通知生成。

### TDD 先行

| 测试 | 验证点 |
|------|--------|
| 审核列表 → 含 suggestion 计算 | 列表 |
| 通过 → offering.status=APPROVED + enrollment.status=APPROVED | 审核通过 |
| 驳回 → offering.status=REJECTED + enrollment.status=REJECTED | 审核驳回 |
| 非 PENDING 状态的 offering 不可审核 → 409 | 状态校验 |
| MQ 消费者成功生成通知 | 异步通知 |
| MQ 通知 SQL 正确（按 offering_id，不按 status 过滤） | 关键约束 |

### 创建文件

- `modules/review/` — Service + Controller
- `config/RabbitMQConfig.java` — DirectExchange + 2 个队列 + binding
- `mq/event/ReviewEvent.java`
- `mq/consumer/NotificationConsumer.java` — 批量 INSERT notification
- `mq/consumer/EnrollmentLogConsumer.java` — 记录日志（MVP 只需 log）

### 关键约束

MQ 通知 SQL 不能按 `status='ENROLLED'` 过滤。事务已将所有 enrollment 更新为 APPROVED/REJECTED。应按 `offering_id` 查询所有记录。

---

## Phase 10：剩余 API + 最终验证（TDD）

**目标**：通知模块 + 教师端 + 数据看板 + 完整端到端流程。

### TDD 先行

| 测试 | 验证点 |
|------|--------|
| 通知列表 → 仅当前用户的、按时间倒序 | 用户过滤 |
| 通知列表 → type 筛选 | 筛选 |
| 标记已读 → is_read=1 | 已读 |
| 标记别人的通知 → 403 | 归属校验 |
| 教师课程列表 → 仅当前教师的 | 归属过滤 |
| 学生名单 → 含学号/姓名/学院/专业/报名时间 | 列表 |
| 非授课教师查看学生名单 → 403 | 权限 |
| Excel 导出 → 文件流 Content-Disposition 正确 | 导出 |
| KPI 统计 → 4 个数字正确 | 看板 |
| TOP10 → 按报名人数降序 | 看板 |
| 趋势 → 按分钟聚合 | 看板 |

### 创建文件

- `modules/notification/` — Entity/Mapper/Service/Controller/VO
- `modules/course/controller/TeacherController.java` — 我的课程 + 学生名单 + Excel 导出
- `modules/dashboard/` — KPI、TOP10、趋势

---

## 端到端验证流程（手动测试）

启动应用后，按以下步骤手动验证完整业务闭环：

```
1. ADMIN  POST   /auth/login                                    → token
2. ADMIN  POST   /admin/semesters                               → 创建学期
3. ADMIN  GET    /admin/semesters                               → 学期列表
4. ADMIN  PATCH  /admin/semesters/{id}/activate                 → 激活学期
5. ADMIN  POST   /admin/courses                                 → 创建课程
6. ADMIN  POST   /admin/offerings                               → 创建开课
7. ADMIN  POST   /admin/campaigns                               → 创建活动（startTime=稍后）
8. ADMIN  PATCH  /admin/campaigns/{id}/start                    → 开启活动
9. STUDENT POST  /auth/login                                    → student token
10.STUDENT GET    /courses                                      → 课程列表
11.STUDENT POST  /enrollments                                   → 抢课
12.STUDENT GET    /student/enrollments                          → 我的选课
13.STUDENT DELETE /enrollments/{id}                             → 退课
14.STUDENT POST  /enrollments                                   → 重新选课
15.ADMIN  PATCH  /admin/campaigns/{id}/end                      → 结束活动
16.ADMIN  GET    /admin/review                                  → 审核列表
17.ADMIN  POST   /admin/review/{id}/approve                     → 审核通过
18.STUDENT GET    /notifications                                → 查看通知
19.STUDENT PUT    /notifications/{id}/read                      → 标记已读
20.TEACHER POST   /auth/login                                   → teacher token
21.TEACHER GET    /teacher/courses                              → 我的课程
22.TEACHER GET    /teacher/courses/{id}/students                → 学生名单
23.TEACHER GET    /teacher/courses/{id}/students/export         → 导出 Excel
24.ADMIN  GET    /admin/dashboard/stats                         → KPI
25.ADMIN  GET    /admin/dashboard/top-courses                   → TOP10
26.ADMIN  GET    /admin/dashboard/trend                         → 趋势
```

---

## 依赖关系图

```
Phase 1: 骨架 + Flyway
  └── Phase 2: 公共层 ←─ 所有模块依赖
        └── Phase 3: 认证 + Security
              ├── Phase 4: 学期
              │     ├── Phase 5: 课程
              │     │     └── Phase 7: 开课 ←─ 需要 course FK + semester FK
              │     └── Phase 6: 活动 ←─ 需要 semester FK
              │             └── Phase 8: 选课 ←─ 需要 campaign + offering
              │                     └── Phase 9: 审核+MQ ←─ 需要 enrollment
              │                             └── Phase 10: 通知+教师+看板
```

---

## 禁止清单（TDD 中可自动验证）

| 禁止项 | 如何避免 |
|--------|---------|
| SELECT ... FOR UPDATE | 原子 UPDATE + affected_rows 判断 |
| 事务内发 MQ 消息 | 测试验证 `@TransactionalEventListener` 调用 |
| 通知 SQL 按 status 过滤 | 测试验证 SQL 只按 offering_id 查询 |
| 枚举值不一致 | 测试遍历验证枚举 name() |
| 响应字段名与文档不一致 | 集成测试断言 JSON key 名称 |
| 未校验删除关联 | 测试验证删除有开课的课程返回 409 |

---

> 本文档是实施过程的唯一路线图。所有代码必须对应到文档中的具体章节。发现疑义时，先查文档，不猜。