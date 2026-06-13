# CLAUDE.md

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

终端命令默认使用powerShell

---

## 开发环境准备（必须先读）

### 必需的基础设施服务

运行项目或测试前，**必须确认以下服务在运行**。不确定时先问用户，不要自己猜：

| 服务 | 端口 | 用途 | 验证命令 |
|------|------|------|---------|
| MySQL 8.0+ | 3306 | 主数据库 | `mysql -u root -p -e "SELECT 1"` |
| Redis 7.x | 6379 | 缓存 | `redis-cli ping` → 返回 PONG |
| RabbitMQ | 5672 | 消息队列 | `rabbitmqctl status` |

### 快速启动

```powershell
# 启动 Redis
redis-server

# 启动 RabbitMQ（管理员权限）
rabbitmq-service start

# 启动后端
cd backend
mvn spring-boot:run
```

### 运行测试

测试依赖 MySQL（必须运行），不依赖 Redis/RabbitMQ（代码已做容错处理，不可用时自动降级）。

```powershell
cd backend
mvn test                     # 全部 60 个测试
mvn test -Dtest=Phase8EnrollmentTest  # 单个测试类
```

### 基础设施检查规则

1. **先问不猜**：不确定基础设施状态时，问用户"服务在运行吗？"，不自行推断
2. **先验证再假设**：依赖外部服务前，先用验证命令确认，再用它
3. **测试挂了先查环境**：测试突然失败时，先检查基础设施在不在，不改代码

## CAS 项目专属约束

### 源码文档基准（唯一真理来源）

以下文档是 CAS V1.0 的唯一基准，**严禁自行发明不在这些文档中的内容**：

| 文档 | 用途 | 约束级别 |
|------|------|---------|
| `docs/后端技术架构设计.md` | 架构、Redis/MQ、模块目录、API 列表、11 步抢课流程 | **必须遵守** |
| `docs/API文档.md` | 30+ 端点的请求/响应/错误码契约 | **前后端对接唯一契约** |
| `docs/数据库设计.md` | DDL、7 表定义、索引、BR-01~BR-07 | **表结构不可变更** |
| `docs/cas-init.sql` | 已部署的 DDL 和种子数据 | **与 DB 一致** |
| `docs/前端技术架构设计.md` | 前端类型定义、service 映射 | **VO 字段对齐依据** |
| `docs/PRD.md` | 7 条业务规则（BR-01 ~ BR-07） | **功能范围边界** |
| `docs/页面原型说明.md` | 13 个页面原型 | **API 覆盖验证依据** |

**严格执行规则**：
1. **不添加** 文档未定义的字段、枚举值、API 端点或业务规则
2. **不修改** 数据库表结构（cas-init.sql 已部署）
3. **不猜测** 响应字段名或类型——以 API 文档为准
4. **不越界** V2.0 预留内容（成绩、课表、学籍）不做

### 技术栈（写死在文档中，不用问）

| 层 | 技术 | 说明 |
|----|------|------|
| 框架 | Spring Boot 3.x + Java 17 | |
| ORM | MyBatis-Plus 3.5.x | LambdaQueryWrapper + 分页插件 |
| 数据库 | MySQL 8.0+ | 已部署 7 表 + admin 用户 |
| 缓存 | Redis 7.x | Cache-Aside 模式 |
| 消息队列 | RabbitMQ | Direct Exchange + 手动 ACK |
| 认证 | Spring Security 6.x + JWT | Bearer Token + Redis 黑名单 |
| API 文档 | SpringDoc OpenAPI 2.x | /swagger-ui/index.html |
| 迁移 | Flyway | V1__init_schema.sql + V2__seed_admin.sql |
| 单元测试 | JUnit 5 + Mockito + H2 | 全 Service 层覆盖 |

### 编码规范

**模块模式**（每个业务模块都遵循）：
```
modules/{module}/
├── {Module}Controller.java      # @RestController + @RequestMapping
├── {Module}Service.java          # 接口
├── {Module}ServiceImpl.java      # @Service + @Transactional 实现
├── entity/{Module}.java          # @TableName + @Data
├── mapper/{Module}Mapper.java    # extends BaseMapper
└── dto/
    ├── {Module}Request.java      # 请求 DTO（含 @Valid 校验）
    └── {Module}VO.java           # 响应 VO（camelCase，含 JOIN 组装字段）
```

**响应格式**：统一 `ApiResponse<T>` 包裹，不要使用 `Map`、`ResponseEntity` 或裸对象返回。

**枚举**：Java 类名、DB VARCHAR、前端 TypeScript 字面量三者必须完全一致。

**事务**：
- 抢课/退课/审核心流程用 `@Transactional(rollbackFor = Exception.class)`
- MQ 消息在 `@TransactionalEventListener(phase = AFTER_COMMIT)` 中发送
- 事务内不做远程调用或长时间 I/O

**缓存规则**：
- 读：Redis → DB → 写 Redis（Cache-Aside）
- 写：更新 DB → 删除 Redis 键
- TTL 严格按 `后端技术架构设计.md` §2.2

**抢课约束**（严格按 `后端技术架构设计.md` §7.1 的 11 步）：
- Redis 去重 → 活动校验 → BR-03 → BR-05 → 容量预检 → INCR → MySQL 原子 UPDATE → INSERT → DECR → 清缓存 → MQ
- 步骤顺序不可变更，不可跳过

### 名词对照（防止混淆）

| 文档用语 | 实际含义 | 对应 DB 表 |
|---------|---------|-----------|
| "课程"（学生端） | 开课记录（offering）| `course_offering` |
| "课程"（管理员端） | 课程基础信息（course）| `course` |
| "选课活动" | 抢课时间段 | `selection_campaign` |
| "选课记录" | 学生选课行为 | `enrollment` |
| "开课记录" | 学生实际报名对象 | `course_offering` |

### 禁止清单

- ❌ SELECT ... FOR UPDATE（用原子 UPDATE + affected_rows 替代）
- ❌ MySQL ENUM 类型（用 VARCHAR）
- ❌ 软删除 enrollment（退课用 DELETE）
- ❌ 裸 `ResponseEntity` 返回（用 `ApiResponse<T>`）
- ❌ 直接暴露密码/邮箱/电话到前端 User 响应
- ❌ 事务内发送 MQ 消息（用 AFTER_COMMIT）
- ❌ 猜想或假设未在文档中定义的错误码或响应字段

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

## gstack 角色路由
- 当需要产品决策、范围判断时，使用 /office-hours 或 /plan-ceo-review
- 当需要架构审查时，使用 /plan-eng-review
- 当代码准备合并前，使用 /review 进行代码审查
- 当需要端到端测试时，使用 /qa
- 当准备发布时，使用 /ship