---
name: project-builder-lite
description: 项目构建精简版 — 极简流程+TDD,省token省时间
metadata:
  type: workflow
---

# /project-builder-lite — 项目构建精简版

从零构建新项目，使用开发模板和skill快速生产


---

## 阶段 1：设计（需求 + 架构 + 计划，一次搞定）

### 1.1 问清楚

3 个问题足矣，不需要 5 轮：

1. 这个项目做什么的？
2. 核心功能有哪些？

### 1.2 直接出方案

脑子过完设计就行，不在文件里写 architecture.md。直接列：

**数据库**（关键字段即可）：

```
数据设计写入入docs/db.md
```

**接口**（核心端点）：
接口设计写入docs/api.md

**前端规范** :

写入docs/frontend.md，需要包括前端的设计风格。

### 1.3 出计划（唯一输出文件）

写入 `.claude/plans/<project>.md`：

```markdown
# <项目> 实施计划

## 技术栈
后端：Spring Boot 3.x + MyBatis-Plus + JWT
前端：Vue 3 + Element Plus
测试：JUnit 5 + Mockito + MockMvc

## Phase 1：认证（模板已有则跳过）
├── 任务 1：登录/注册 API + 测试
│   ├── 测试：重复注册抛异常、密码错误返回401、正常返回token
│   └── 实现：AuthController + UserService
├── 任务 2：个人信息 + 改密码 + 头像 + 测试
│   └── 实现：UserController + FileUpload

## Phase 2：核心业务
├── 任务 3：[功能A] + 测试
│   └── ...

## Phase 3：完善
├── 任务 4：管理员 CRUD + 测试
└── 任务 5：统计看板 + 测试

## 验证方式
每个任务：mvn test（新增测试绿）→ curl 验证 API
全部完成：启动项目走通完整流程
```

### 1.4 确认（第 1 次）

"计划写好了，确认后开始执行"

> 中途发现遗漏 → 直接更新计划文件，不再重复确认。

---

## 阶段 2：执行（TDD 模式）

### 2.1 每个任务的节奏

```
① 写测试文件 → mvn test（红色，预期失败）
② 写实现文件 → mvn test（绿色，通过）
③ curl 验证 API → 返回 200
④ 计划文件打 ✅
```

### 2.2 测试规范（精简版）

**必须测**：Service 核心逻辑 + 主要异常路径 + API 端点

**不用测**：Getter/Setter、Mapper 接口、纯数据转发

**格式**：Given-When-Then

```java
@Test
void register_duplicate_shouldThrow() {
    // Given
    when(mapper.selectOne(any())).thenReturn(existing);
    // When & Then
    assertThrows(BusinessException.class, () -> service.register(req));
}
```

### 2.3 Agent prompt 要素

```
【文件】只改 xxx 目录
【内容】实现 xxx 功能
【TDD】先写 xxxTest.java → 运行(红) → 写实现 → 运行(绿)
【验收】mvn test 通过 + curl 验证
```

### 2.4 确认（第 2 次 — 每个 Phase 结束时）

"Phase N 完成了，`mvn test` 全绿。继续下一 Phase？"

---

## 阶段 3：验证（扫描 + 修复，一次过）

> 不在文件里写审查报告，扫到问题直接改。

### 3.1 运行测试

`mvn test` → 全绿继续。有红 → 分析全部失败模式 → 批量修。

### 3.2 快速扫一遍常见问题

```
□ UpdateWrapper.set 有 null 检查？
□ YAML 没有重复 key？
□ 前端用了 res.data || res？
□ 静态资源路径在 Security 放行了？
□ CORS 放行了 OPTIONS？
```

扫到一个改一个，改完继续。不写审查报告。

### 3.3 启动验证

启动前后端，走通核心流程。后端无报错、浏览器无红色。

### 3.4 确认（第 3 次）

"全部完成，可以看了"

---

## 阶段 4：复盘（一句话总结）

不需要写 review.md。只需要：

1. 记一条 memory（真有价值的经验才记）：

```
---
name: xxx-takeaway
---
做得好的：XXX
踩坑的：XXX → 以后要 YYY
```

2. 向用户汇报（3 句话）：

```
完成：[项目名]
功能：[核心功能列表]
测试：mvn test 全绿，共 N 个测试
踩坑：[一句话]
```

---

## 📌 TDD 精简指南

### Red-Green-Refactor

```
RED：写测试 → 运行 → 失败
GREEN：写实现 → 运行 → 通过
REFACTOR：优化 → 运行 → 仍然通过
```

### Given-When-Then

```java
@Test
void 方法名_场景_预期() {
    // Given 准备
    // When 执行
    // Then 验证
}
```

### 必须测 vs 不用测

| 必须测 | 不用测 |
|--------|--------|
| Service 业务逻辑 | Getter/Setter |
| 异常/边界条件 | Mapper 接口 |
| API 端点 | 纯数据转发 |

---

## 📌 效率陷阱

### Edit 一步到位 → 拆成小段，每次 3-5 行
### 单循环修测试 → 分析全部失败模式 → 按根因批量修
### Agent 放养 → prompt 写验收命令
### 跳过测试写实现 → TDD 纪律，不测试不算写完