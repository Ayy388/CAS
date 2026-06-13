# CAS 高校选课管理系统
# 接口文档（V1.0）

> 基于 PRD.md、数据库设计.md、前端技术架构设计.md、后端技术架构设计.md 推导
> 前端 Vue 3 + shadcn-vue ↔ 后端 Spring Boot 3.x 对接契约

---

## 文档信息

| 项目 | 内容 |
|------|------|
| 当前版本 | V1.0 |
| 基础路径 | `http://localhost:8080/api/v1` |
| 请求体格式 | JSON, `Content-Type: application/json` |
| 响应格式 | 统一 `ApiResponse<T>` 包裹 |
| 认证方式 | `Authorization: Bearer <token>` |
| 分页参数 | `?page=1&pageSize=20` |
| 日期时间格式 | `yyyy-MM-dd HH:mm:ss` |
| **纯日期格式** | **`yyyy-MM-dd`**（仅 `semester.startDate` / `endDate` 为 DATE 类型） |
| 字段命名 | 请求/响应一律 camelCase（与前端 TypeScript 类型对齐） |

---

## 一、通用规范

### 1.1 统一响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 200=成功, 4xx=客户端错误, 5xx=服务端错误 |
| `message` | string | 提示信息 |
| `data` | T | 具体业务数据 |

### 1.2 分页响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

> 前端 `PageResponse<T>` 与此结构一致。

### 1.3 全局 HTTP 状态码

| 状态码 | `message` 含义 | 说明 |
|--------|---------------|------|
| 200 | success | 请求成功 |
| 400 | 参数校验失败 | 请求参数格式错误 |
| 401 | 未登录或 Token 已过期 | 需要重新登录 |
| 403 | 无权限访问 | 角色不匹配或非本人数据 |
| 404 | 资源不存在 | 请求的资源未找到 |
| 409 | 业务规则冲突 | 具体错误原因在 message 中 |
| 429 | 操作过于频繁 | 触发 Redis 请求去重（2 秒内重复操作） |
| 500 | 服务器内部错误 | 系统异常 |

### 1.4 枚举值一览

| 枚举字段 | 可选值 | 所属模型 |
|---------|--------|---------|
| `role` | `STUDENT` / `TEACHER` / `ADMIN` | User |
| `courseType` | `REQUIRED` / `ELECTIVE_MAJOR` / `ELECTIVE_GENERAL` | Course |
| `semesterType` | `FIRST` / `SECOND` | Semester |
| `semesterStatus` | `ACTIVE` / `INACTIVE` | Semester |
| `campaignStatus` | `PENDING` / `ACTIVE` / `ENDED` | SelectionCampaign |
| `offeringStatus` | `PENDING` / `APPROVED` / `REJECTED` | CourseOffering |
| `enrollmentStatus` | `ENROLLED` / `APPROVED` / `REJECTED` | Enrollment |
| `notificationType` | `APPROVED` / `REJECTED` / `SYSTEM` | Notification |

### 1.5 前端 service → 后端 API 映射

| 前端 service 文件（`src/services/`） | 对应后端 API |
|------------------------------------|-------------|
| `semester.ts` | `/api/v1/admin/semesters/*` |
| `course.ts` | `/api/v1/admin/courses/*` + `GET /api/v1/courses`（公共课程大厅） |
| `offering.ts` | `/api/v1/admin/offerings/*` |
| `campaign.ts` | `/api/v1/admin/campaigns/*` + `GET /api/v1/campaigns/current` |
| `enrollment.ts` | `/api/v1/enrollments` + `/api/v1/student/enrollments` |
| `review.ts` | `/api/v1/admin/review/*` |
| `notification.ts` | `/api/v1/notifications/*` |
| `dashboard.ts` | `/api/v1/admin/dashboard/*` |

---

## 二、认证模块（Auth）

### 2.1 登录

```
POST /api/v1/auth/login
```

**权限**：无需登录

**Request Body**：

```json
{
  "username": "admin",
  "password": "admin123"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `username` | string | 是 | 用户名（对应 DB `sys_user.username`） |
| `password` | string | 是 | 密码明文，后端 BCrypt 校验 |

**Response `data`**：

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "role": "ADMIN",
    "department": null,
    "major": null,
    "grade": null
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `token` | string | JWT Token，默认有效期 24 小时（86400000ms） |
| `user` | User | 当前登录用户信息（密码/邮箱/电话/头像/状态等字段不暴露） |

**Response 中的 User 字段**：

| 字段 | 类型 | DB 列 | 说明 |
|------|------|-------|------|
| `id` | long | `id` | 主键 |
| `username` | string | `username` | 登录名 |
| `realName` | string | `real_name` | 真实姓名 |
| `role` | string | `role` | 角色枚举 |
| `department` | string / null | `department` | 学院/部门 |
| `major` | string / null | `major` | 专业 |
| `grade` | string / null | `grade` | 年级，如 "2023级" |

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 401 | 用户名或密码错误 | 账号不存在或密码不匹配 |
| 409 | 账号已被禁用 | `sys_user.status = 0` |

---

### 2.2 获取当前用户

```
GET /api/v1/auth/me
```

**权限**：已认证（任何角色）

**Request Headers**：`Authorization: Bearer <token>`

**Response `data`**：同 2.1 登录的 `user` 对象。

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 401 | Token 已失效 | Token 过期或已加入黑名单（登出时 Redis 记录黑名单） |
| 401 | 未登录 | 未携带 Token |

---

## 三、学生端 API

### 3.1 首页

```
GET /api/v1/student/dashboard
```

**权限**：`STUDENT`

**Response `data`**：

```json
{
  "semester": {
    "id": 1,
    "name": "2025-2026 第一学期",
    "academicYear": "2025-2026",
    "semesterType": "FIRST",
    "startDate": "2025-09-01",
    "endDate": "2026-01-15",
    "status": "ACTIVE"
  },
  "currentCampaign": {
    "id": 1,
    "name": "2025-2026第一学期选课",
    "semesterId": 1,
    "semesterName": "2025-2026 第一学期",
    "startTime": "2025-09-01 09:00:00",
    "endTime": "2025-09-03 18:00:00",
    "status": "ACTIVE"
  },
  "myEnrollment": {
    "id": 100,
    "campaignId": 1,
    "offeringId": 1,
    "offeringName": "影视鉴赏",
    "studentId": 2,
    "studentName": "张三",
    "status": "ENROLLED",
    "enrolledAt": "2025-09-01 09:00:05",
    "reviewedAt": null
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `semester` | Semester / null | 当前 ACTIVE 学期的信息（后端查询 `WHERE status='ACTIVE'`）。不存在时为 null |
| `currentCampaign` | SelectionCampaign / null | 当前 ACTIVE 或 PENDING 的活动（缓存 30s）。不存在时为 null |
| `myEnrollment` | Enrollment / null | 当前学生在本学期已选课记录（查询当前活动下该学生的 enrollment）。未选课时为 null |

> **前端使用**：`currentCampaign` 实时显示倒计时（`useCountdown` composable），`myEnrollment` 控制课程大厅抢课按钮是否禁用。

**`semester` DATE 字段说明**：`startDate` / `endDate` 格式为 `yyyy-MM-dd`（DB 字段 `start_date` / `end_date` 为 DATE 类型），其余时间字段为 `yyyy-MM-dd HH:mm:ss`（DATETIME 类型）。

---

### 3.2 当前活动（前端轮询）

```
GET /api/v1/campaigns/current
```

**权限**：`STUDENT`

> 供前端 `useCurrentCampaign()` composable 每 30 秒轮询，驱动倒计时和选课按钮状态。缓存 30s，不产生 DB 压力。

**Response `data`**：

```json
{
  "id": 1,
  "name": "2025-2026第一学期选课",
  "semesterId": 1,
  "semesterName": "2025-2026 第一学期",
  "startTime": "2025-09-01 09:00:00",
  "endTime": "2025-09-03 18:00:00",
  "status": "ACTIVE"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `data` | SelectionCampaign / null | 当前 ACTIVE（或最近 PENDING）的活动，不存在时为 null |

> `semesterName` 通过 JOIN `semester` 表获取。

---

### 3.3 课程列表（课程大厅）

```
GET /api/v1/courses
```

> ⚠️ 区别于 `GET /api/v1/admin/courses`（课程基础信息管理）。此接口返回的是**开课记录（offerings）**，供学生选课使用。

**权限**：`STUDENT`

**Query Parameters**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `keyword` | string | 否 | — | 课程名称模糊搜索（LIKE `%keyword%`） |
| `type` | string | 否 | — | 筛选：`ELECTIVE_GENERAL` / `ELECTIVE_MAJOR`。不传返回全部 |
| `page` | int | 是 | 1 | 当前页码 |
| `pageSize` | int | 是 | 20 | 每页条数 |

**后端过滤逻辑**（隐式，无需前端传入）：
1. 只返回**当前 ACTIVE 学期**下的开课记录
2. 只返回 `course.type IN ('ELECTIVE_GENERAL', 'ELECTIVE_MAJOR')` 的记录（`REQUIRED` 不参与抢课）
3. 只返回 `offering.status IN ('PENDING', 'APPROVED')` 的记录（已驳回的不展示）

**Response `data`**（分页）：

```json
{
  "items": [
    {
      "id": 1,
      "semesterId": 1,
      "courseId": 1,
      "courseName": "影视鉴赏",
      "courseType": "ELECTIVE_GENERAL",
      "teacherId": 3,
      "teacherName": "张老师",
      "maxCapacity": 50,
      "minEnrollment": 20,
      "seatsRemaining": 32,
      "openGrade": "2023级",
      "openMajor": null,
      "location": "教学楼A101",
      "schedule": "周三7-8节",
      "status": "APPROVED"
    }
  ],
  "total": 30,
  "page": 1,
  "pageSize": 20
}
```

**CourseOffering 字段对照**：

| JSON 字段 | 类型 | 来源 | 说明 |
|-----------|------|------|------|
| `id` | long | `course_offering.id` | 开课记录 ID |
| `semesterId` | long | `course_offering.semester_id` | |
| `courseId` | long | `course_offering.course_id` | |
| `courseName` | string | JOIN `course.name` | VO 层组装 |
| `courseType` | string | JOIN `course.type` | VO 层组装 |
| `teacherId` | long | `course_offering.teacher_id` | |
| `teacherName` | string | JOIN `sys_user.real_name` | VO 层组装 |
| `maxCapacity` | int | `course_offering.max_capacity` | |
| `minEnrollment` | int | `course_offering.min_enrollment` | |
| `seatsRemaining` | int | **VO 计算** | `maxCapacity - enrolledCount` |
| `openGrade` | string / null | `course_offering.open_grade` | null 表示全年级 |
| `openMajor` | string / null | `course_offering.open_major` | null 表示全专业 |
| `location` | string / null | `course_offering.location` | |
| `schedule` | string / null | `course_offering.schedule` | |
| `status` | string | `course_offering.status` | PENDING / APPROVED / REJECTED |

> **缓存说明**：列表数据 Redis 缓存 5 分钟（键 `cas:course:list:{type}:{page}`）。实时名额以课程详情接口为准。

---

### 3.3 课程详情

```
GET /api/v1/courses/{id}
```

**权限**：`STUDENT`

**Path Parameters**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | long | 开课记录 ID（`course_offering.id`） |

**Response `data`**：

```json
{
  "id": 1,
  "semesterId": 1,
  "courseId": 1,
  "courseName": "影视鉴赏",
  "courseType": "ELECTIVE_GENERAL",
  "teacherId": 3,
  "teacherName": "张老师",
  "maxCapacity": 50,
  "minEnrollment": 20,
  "seatsRemaining": 32,
  "enrolledCount": 18,
  "openGrade": "2023级",
  "openMajor": null,
  "location": "教学楼A101",
  "schedule": "周三7-8节",
  "status": "APPROVED",
  "description": "本课程通过赏析经典影视作品..."
}
```

> 对比列表接口，详情接口额外返回 `enrolledCount` 和 `description` 字段，供前端展示实时报名人数和课程简介。

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 404 | 课程不存在 | 开课记录 `id` 未找到 |
| 403 | 无权访问 | 开课记录不属于学生当前学期（非必要，视安全策略而定） |

---

### 3.4 抢课

```
POST /api/v1/enrollments
```

> 后端自动获取**当前 ACTIVE 活动**作为 `campaignId`，前端只需传入 `offeringId`。

**权限**：`STUDENT`

**Request Body**：

```json
{
  "offeringId": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `offeringId` | long | 是 | 开课记录 ID（`course_offering.id`） |

**Response `data`**：

```json
{
  "enrollmentId": 100,
  "message": "抢课成功"
}
```

**完整的 11 步校验与执行流程**（后端依次执行）：

| 步骤 | 中间件 | 校验逻辑 | 失败返回 |
|------|--------|---------|---------|
| ① | Redis | 同一学生 2s 内重复点击（`SET NX EX 2`） | 429 "操作太频繁，请稍后再试" |
| ② | DB | 当前无 ACTIVE 状态的活动 | 409 "当前没有进行中的选课活动" |
| ③ | DB | **BR-03**：同活动下是否已选课（查 `enrollment` 表，排除 `REJECTED`） | 409 "您已选课" |
| ④ | DB | **BR-05**：学生年级/专业是否匹配 `open_grade` / `open_major` | 409 "您不符合开放范围" |
| ⑤ | Redis | 容量预检：`GET cas:enrollment:count` >= `maxCapacity` | 409 "已满员" |
| ⑥ | Redis | INCR 临时计数 > `maxCapacity` | 409 "已满员" |
| ⑦ | MySQL | 原子 UPDATE：`SET enrolled_count = enrolled_count + 1 WHERE enrolled_count < max_capacity` | 409 "已满员" |
| ⑧ | MySQL | INSERT enrollment（受 `UNIQUE(campaign_id, student_id)` 约束） | 409 "您已选课"（极低概率竞态） |
| ⑨ | Redis | DECR 临时计数，删除课程列表/详情缓存 | — |
| ⑩ | 事务提交 | | |
| ⑪ | MQ | 事务提交后发送 `enrollment.log` 消息（异步日志） | —（不影响主流程） |

> 插入 enrollment 的 `status` 初始为 `ENROLLED`，审核后才变为 `APPROVED` 或 `REJECTED`。

**错误汇总**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 429 | 操作太频繁，请稍后再试 | 2 秒内重复抢课 |
| 409 | 当前没有进行中的选课活动 | 无 ACTIVE 状态的 campaign |
| 409 | 您已选课 | 同活动下已有 ENROLLED/APPROVED 记录（BR-03） |
| 409 | 您不符合开放范围 | 年级/专业不匹配（BR-05） |
| 409 | 已满员 | 名额已满 |

---

### 3.5 我的选课列表

```
GET /api/v1/student/enrollments
```

**权限**：`STUDENT`

**Query Parameters**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | int | 是 | 1 | 当前页码 |
| `pageSize` | int | 是 | 20 | 每页条数 |

**Response `data`**（分页，按 `enrolled_at` 倒序）：

```json
{
  "items": [
    {
      "id": 100,
      "campaignId": 1,
      "offeringId": 1,
      "offeringName": "影视鉴赏",
      "studentId": 2,
      "studentName": "张三",
      "status": "ENROLLED",
      "enrolledAt": "2025-09-01 09:00:05",
      "reviewedAt": null,
      "teacherName": "张老师",
      "credits": 2.0,
      "hours": 16,
      "location": "教学楼A101",
      "schedule": "周三7-8节"
    }
  ],
  "total": 1,
  "page": 1,
  "pageSize": 20
}
```

> 额外返回 `teacherName`、`credits`、`hours`、`location`、`schedule` 等字段，支持「我的课程」Timeline 页面展示。这些字段通过 JOIN `course_offering` + `course` + `sys_user` 获取。

---

### 3.6 退课

```
DELETE /api/v1/enrollments/{id}
```

**权限**：`STUDENT`

**Path Parameters**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | long | 选课记录 ID（`enrollment.id`） |

**Response `data`**：无（`code: 200`）

**退课规则**：

| 当前 campaign 状态 | 允许退课 |
|--------------------|---------|
| PENDING（未开始） | ✅ 是（理论上无 enrollment，但为数据一致性保留） |
| ACTIVE（进行中） | ✅ 是 |
| ENDED（已结束） | ❌ 否，返回 409 |

**后端执行流程**：
1. 校验 `enrollment.student_id` = 当前用户（防止越权）
2. 校验活动未结束（`campaign.status != 'ENDED'`）
3. 原子 DECREMENT：`UPDATE course_offering SET enrolled_count = enrolled_count - 1 WHERE id = ? AND enrolled_count > 0`
4. `DELETE FROM enrollment WHERE id = ?`
5. 删除 Redis 课程列表/详情缓存（`DEL cas:course:list:*` + `DEL cas:offering:{id}`）

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 403 | 无权操作此记录 | `enrollment.student_id` 与当前用户不匹配 |
| 404 | 选课记录不存在 | enrollment id 未找到 |
| 409 | 已超过退课时间 | 选课活动已结束（`campaign.status = 'ENDED'`） |

---

## 四、教师端 API

### 4.1 我的课程

```
GET /api/v1/teacher/courses
```

**权限**：`TEACHER`

> 后端自动通过 JWT 获取当前教师 `teacherId`，仅返回该教师作为授课教师的开课记录。

**Query Parameters**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `semesterId` | long | 否 | 当前 ACTIVE 学期 | 按学期筛选 |
| `page` | int | 是 | 1 | 当前页码 |
| `pageSize` | int | 是 | 20 | 每页条数 |

**Response `data`**（分页）：

```json
{
  "items": [
    {
      "id": 1,
      "courseName": "影视鉴赏",
      "semesterName": "2025-2026 第一学期",
      "maxCapacity": 50,
      "enrolledCount": 42,
      "status": "APPROVED"
    }
  ],
  "total": 5,
  "page": 1,
  "pageSize": 20
}
```

> 查询条件：`course_offering.teacher_id = 当前用户.id AND semester_id = 指定学期（或当前活跃学期）`。默认按 `status` 排序（PENDING 在前）。

---

### 4.2 学生名单

```
GET /api/v1/teacher/courses/{id}/students
```

**权限**：`TEACHER`

**Path Parameters**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | long | 开课记录 ID（`course_offering.id`） |

**Query Parameters**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | int | 是 | 1 | 当前页码 |
| `pageSize` | int | 是 | 20 | 每页条数 |

**Response `data`**（分页，按 `enrolled_at` 正序）：

```json
{
  "items": [
    {
      "studentId": 2,
      "studentName": "张三",
      "studentNumber": "2023001",
      "department": "计算机学院",
      "major": "计算机科学与技术",
      "enrolledAt": "2025-09-01 09:00:05"
    }
  ],
  "total": 42,
  "page": 1,
  "pageSize": 20
}
```

**字段说明**：

| JSON 字段 | DB 来源 | 说明 |
|-----------|---------|------|
| `studentId` | `sys_user.id` | 用户主键 |
| `studentName` | `sys_user.real_name` | 真实姓名 |
| `studentNumber` | `sys_user.student_id` | 学号（仅 STUDENT 角色有值） |
| `department` | `sys_user.department` | 学院 |
| `major` | `sys_user.major` | 专业 |
| `enrolledAt` | `enrollment.enrolled_at` | 报名时间 |

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 403 | 您不是该课程的授课教师 | 当前教师的 `id` 与 `course_offering.teacher_id` 不匹配 |
| 404 | 课程不存在 | 开课记录 `id` 未找到 |

---

### 4.3 导出学生名单（Excel）

```
GET /api/v1/teacher/courses/{id}/students/export
```

**权限**：`TEACHER`

**Path Parameters**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | long | 开课记录 ID |

**Response**：二进制 Excel 文件流（`Content-Type: application/vnd.ms-excel`），导出内容与 4.2 节一致。

**错误**：同 4.2 节。

---

## 五、管理员端 API

### 5.1 数据看板（Dashboard）

#### 5.1.1 KPI 统计

```
GET /api/v1/admin/dashboard/stats
```

**权限**：`ADMIN`

**Response `data`**：

```json
{
  "totalCourses": 30,
  "totalTeachers": 15,
  "totalStudents": 500,
  "totalEnrollments": 1200
}
```

| 字段 | 类型 | DB 查询 |
|------|------|---------|
| `totalCourses` | int | `SELECT COUNT(*) FROM course` |
| `totalTeachers` | int | `SELECT COUNT(*) FROM sys_user WHERE role = 'TEACHER'` |
| `totalStudents` | int | `SELECT COUNT(*) FROM sys_user WHERE role = 'STUDENT' AND status = 1`（只统计启用状态） |
| `totalEnrollments` | int | `SELECT COUNT(*) FROM enrollment` |

---

#### 5.1.2 热门课程 TOP10

```
GET /api/v1/admin/dashboard/top-courses
```

**权限**：`ADMIN`

**Query Parameters**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `semesterId` | long | 否 | 当前 ACTIVE 学期 | 按学期筛选 |

**Response `data`**：

```json
[
  {
    "courseName": "影视鉴赏",
    "enrolledCount": 50,
    "maxCapacity": 50,
    "filledAt": "2025-09-01 09:00:30"
  },
  {
    "courseName": "创业管理",
    "enrolledCount": 35,
    "maxCapacity": 50,
    "filledAt": null
  }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `courseName` | string | 课程名称 |
| `enrolledCount` | int | 当前报名人数 |
| `maxCapacity` | int | 最大容量 |
| `filledAt` | string / null | 满员时间（取该课程 enrollment 按 `enrolled_at` 升序的第 `maxCapacity` 条记录的时间）。未满员为 null |

> 按 `enrolledCount` 降序排列，取前 10 条。使用 `idx_enrollment_enrolled_at` 索引优化 GROUP BY。

---

#### 5.1.3 抢课趋势

```
GET /api/v1/admin/dashboard/trend
```

**权限**：`ADMIN`

**Query Parameters**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `campaignId` | long | 否 | 最近一场已结束的活动 | 活动 ID |

**Response `data`**：

```json
[
  {
    "time": "09:00",
    "count": 150
  },
  {
    "time": "09:01",
    "count": 80
  }
]
```

> 按分钟统计该活动的抢课人数（`DATE_FORMAT(enrolled_at, '%Y-%m-%d %H:%i')`），供前端 ECharts Line Chart 渲染。

---

### 5.2 学期管理

#### 5.2.1 学期列表

```
GET /api/v1/admin/semesters
```

**权限**：`ADMIN`

**Query Parameters**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | int | 是 | 1 | 当前页码 |
| `pageSize` | int | 是 | 20 | 每页条数 |

**Response `data`**（分页，按 `created_at` 倒序）：

```json
{
  "items": [
    {
      "id": 1,
      "name": "2025-2026 第一学期",
      "academicYear": "2025-2026",
      "semesterType": "FIRST",
      "startDate": "2025-09-01",
      "endDate": "2026-01-15",
      "status": "ACTIVE"
    }
  ],
  "total": 3,
  "page": 1,
  "pageSize": 20
}
```

> `startDate` / `endDate` 格式为 `yyyy-MM-dd`（DB 字段为 DATE 类型）。

---

#### 5.2.2 新增学期

```
POST /api/v1/admin/semesters
```

**权限**：`ADMIN`

**Request Body**：

```json
{
  "name": "2025-2026 第一学期",
  "academicYear": "2025-2026",
  "semesterType": "FIRST",
  "startDate": "2025-09-01",
  "endDate": "2026-01-15"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 是 | 学期名称 |
| `academicYear` | string | 是 | 学年，如 `2025-2026` |
| `semesterType` | string | 是 | `FIRST` / `SECOND` |
| `startDate` | string | 是 | 开始日期，格式 `yyyy-MM-dd` |
| `endDate` | string | 是 | 结束日期，格式 `yyyy-MM-dd`。必须晚于 `startDate` |

**Response `data`**：创建的学期对象（含 `id`、`createdAt`、初始 `status = 'INACTIVE'`）。

---

#### 5.2.3 编辑学期

```
PUT /api/v1/admin/semesters/{id}
```

**权限**：`ADMIN`

**Path Parameters**：`id` — 学期 ID

**Request Body**：同新增学期。

> 编辑不改变 `status`，如需激活/停用请使用 5.2.4。

**Response `data`**：更新后的学期对象。

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 404 | 学期不存在 | 学期 `id` 未找到 |

---

#### 5.2.4 激活/停用学期

```
PATCH /api/v1/admin/semesters/{id}/activate
```

**权限**：`ADMIN`

**Path Parameters**：`id` — 学期 ID

**Response `data`**：更新后的学期对象（`status` 在 `ACTIVE` / `INACTIVE` 间切换）。

**业务规则**：
- 激活一个学期时，其他所有学期自动变为 `INACTIVE`（同一时刻只有一个活跃学期）
- 如果当前学期已经是 ACTIVE，调用此接口将其变为 INACTIVE（停用）
- 学期不允许删除（如误创建可通过停用处理）

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 404 | 学期不存在 | 学期 `id` 未找到 |

---

### 5.3 课程管理

#### 5.3.1 课程列表

```
GET /api/v1/admin/courses
```

> ⚠️ 此接口返回**课程基础信息表（course 表）**，区别于 `GET /api/v1/courses`（学生课程大厅，返回开课记录）。

**权限**：`ADMIN`

**Query Parameters**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `keyword` | string | 否 | — | 课程名称/编号搜索（LIKE） |
| `type` | string | 否 | — | 筛选：`REQUIRED` / `ELECTIVE_MAJOR` / `ELECTIVE_GENERAL` |
| `page` | int | 是 | 1 | 当前页码 |
| `pageSize` | int | 是 | 20 | 每页条数 |

**Response `data`**（分页，按 `created_at` 倒序）：

```json
{
  "items": [
    {
      "id": 1,
      "code": "GE001",
      "name": "影视鉴赏",
      "type": "ELECTIVE_GENERAL",
      "credits": 2.0,
      "hours": 16,
      "description": "本课程通过赏析经典影视作品...",
      "createdAt": "2025-06-01 10:00:00"
    }
  ],
  "total": 30,
  "page": 1,
  "pageSize": 20
}
```

---

#### 5.3.2 新增课程

```
POST /api/v1/admin/courses
```

**权限**：`ADMIN`

**Request Body**：

```json
{
  "code": "GE001",
  "name": "影视鉴赏",
  "type": "ELECTIVE_GENERAL",
  "credits": 2.0,
  "hours": 16,
  "description": "本课程通过赏析经典影视作品，提升审美能力。"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `code` | string | 是 | 课程编号，唯一（如 `GE001`）。DB 有 UNIQUE 约束 |
| `name` | string | 是 | 课程名称 |
| `type` | string | 是 | 课程类型 |
| `credits` | number | 是 | 学分，保留 1 位小数（DB 类型 `DECIMAL(3,1)`）。如 `2.0`、`1.5` |
| `hours` | int | 是 | 总课时 |
| `description` | string | 否 | 课程简介（DB 类型 `TEXT`） |

**Response `data`**：创建的课程对象（含 `id`、`createdAt`）。

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 409 | 课程编号已存在 | `course.code` UNIQUE 约束冲突 |

---

#### 5.3.3 编辑课程

```
PUT /api/v1/admin/courses/{id}
```

**权限**：`ADMIN`

**Path Parameters**：`id` — 课程 ID

**Request Body**：同新增课程（所有字段必填，即使是已存在的值）。

**Response `data`**：更新后的课程对象。

---

#### 5.3.4 删除课程

```
DELETE /api/v1/admin/courses/{id}
```

**权限**：`ADMIN`

**Path Parameters**：`id` — 课程 ID

**Response `data`**：无（`code: 200`）

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 404 | 课程不存在 | course `id` 未找到 |
| 409 | 该课程已有开课记录，无法删除 | `course_offering` 表中存在 `course_id = {id}` 的记录（外键约束保护） |

---

### 5.4 开课管理

#### 5.4.1 开课列表

```
GET /api/v1/admin/offerings
```

**权限**：`ADMIN`

**Query Parameters**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `semesterId` | long | 否 | 当前 ACTIVE 学期 | 按学期筛选 |
| `status` | string | 否 | — | 按状态筛选：`PENDING` / `APPROVED` / `REJECTED` |
| `page` | int | 是 | 1 | 当前页码 |
| `pageSize` | int | 是 | 20 | 每页条数 |

**Response `data`**（分页）：

```json
{
  "items": [
    {
      "id": 1,
      "semesterId": 1,
      "semesterName": "2025-2026 第一学期",
      "courseId": 1,
      "courseName": "影视鉴赏",
      "courseType": "ELECTIVE_GENERAL",
      "teacherId": 3,
      "teacherName": "张老师",
      "maxCapacity": 50,
      "minEnrollment": 20,
      "enrolledCount": 42,
      "openGrade": "2023级",
      "openMajor": null,
      "location": "教学楼A101",
      "schedule": "周三7-8节",
      "status": "APPROVED"
    }
  ],
  "total": 30,
  "page": 1,
  "pageSize": 20
}
```

> 管理员列表包含 `enrolledCount` 字段（学生课程大厅列表不包含此字段，只包含 `seatsRemaining`）。

---

#### 5.4.2 新增开课

```
POST /api/v1/admin/offerings
```

**权限**：`ADMIN`

**Request Body**：

```json
{
  "semesterId": 1,
  "courseId": 1,
  "teacherId": 3,
  "maxCapacity": 50,
  "minEnrollment": 20,
  "openGrade": "2023级",
  "openMajor": null,
  "location": "教学楼A101",
  "schedule": "周三7-8节"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `semesterId` | long | 是 | 所属学期 |
| `courseId` | long | 是 | 关联课程 |
| `teacherId` | long | 是 | 授课教师 ID（须为 `role='TEACHER'` 的用户） |
| `maxCapacity` | int | 是 | 最大容量 |
| `minEnrollment` | int | 是 | 最低开课人数 |
| `openGrade` | string | 否 | 开放年级，null=全年级 |
| `openMajor` | string | 否 | 开放专业，null=全专业 |
| `location` | string | 否 | 上课地点 |
| `schedule` | string | 否 | 上课时间说明，如 "周三7-8节" |

> ⚠️ `enrolledCount` 为**只读字段**，不在请求体中出现。新建时初始为 0，由系统在抢课成功后原子递增。

**Response `data`**：创建的开课对象（初始 `status = 'PENDING'`，`enrolledCount = 0`）。

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 400 | 参数校验失败 | `maxCapacity` < `minEnrollment` 等 |
| 404 | 关联的学期/课程/教师不存在 | 外键引用的资源未找到 |

---

#### 5.4.3 编辑开课

```
PUT /api/v1/admin/offerings/{id}
```

**权限**：`ADMIN`

**Path Parameters**：`id` — 开课记录 ID

**Request Body**：同新增开课。

> 编辑时 `status` 和 `enrolledCount` 不受影响（它们由业务流程控制）。

**Response `data`**：更新后的开课对象。

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 404 | 开课记录不存在 | offering `id` 未找到 |

---

### 5.5 选课活动管理

#### 5.5.1 活动列表

```
GET /api/v1/admin/campaigns
```

**权限**：`ADMIN`

**Query Parameters**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `semesterId` | long | 否 | 当前 ACTIVE 学期 | 按学期筛选 |
| `page` | int | 是 | 1 | 当前页码 |
| `pageSize` | int | 是 | 20 | 每页条数 |

**Response `data`**（分页，按 `created_at` 倒序）：

```json
{
  "items": [
    {
      "id": 1,
      "name": "2025-2026第一学期选课",
      "semesterId": 1,
      "semesterName": "2025-2026 第一学期",
      "startTime": "2025-09-01 09:00:00",
      "endTime": "2025-09-03 18:00:00",
      "status": "ACTIVE"
    }
  ],
  "total": 3,
  "page": 1,
  "pageSize": 20
}
```

---

#### 5.5.2 新增活动

```
POST /api/v1/admin/campaigns
```

**权限**：`ADMIN`

**Request Body**：

```json
{
  "name": "2025-2026第一学期选课",
  "semesterId": 1,
  "startTime": "2025-09-01 09:00:00",
  "endTime": "2025-09-03 18:00:00"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 是 | 活动名称 |
| `semesterId` | long | 是 | 所属学期 |
| `startTime` | string | 是 | 开始时间，格式 `yyyy-MM-dd HH:mm:ss` |
| `endTime` | string | 是 | 结束时间，格式 `yyyy-MM-dd HH:mm:ss`，必须晚于 `startTime` |

**Response `data`**：创建的活动对象（初始 `status = 'PENDING'`）。

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 400 | 结束时间必须晚于开始时间 | 时间校验不通过 |
| 409 | 该学期下已存在 PENDING 或 ACTIVE 的活动 | 一个学期内只允许一个活动 |

---

#### 5.5.3 开启活动

```
PATCH /api/v1/admin/campaigns/{id}/start
```

**权限**：`ADMIN`

**Path Parameters**：`id` — 活动 ID

**Response `data`**：

```json
{
  "id": 1,
  "name": "2025-2026第一学期选课",
  "semesterId": 1,
  "semesterName": "2025-2026 第一学期",
  "startTime": "2025-09-01 09:00:00",
  "endTime": "2025-09-03 18:00:00",
  "status": "ACTIVE"
}
```

**业务规则**：
- 状态转换：`PENDING → ACTIVE`（已结束的活动不可重新开启）
- 开启后自动清除 Redis 缓存 `cas:campaign:current`

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 404 | 活动不存在 | campaign `id` 未找到 |
| 409 | 活动当前状态不允许此操作 | 当前 `status` 不是 `PENDING`（如已 ACTIVE 或 ENDED） |

---

#### 5.5.4 结束活动

```
PATCH /api/v1/admin/campaigns/{id}/end
```

**权限**：`ADMIN`

**Path Parameters**：`id` — 活动 ID

**Response `data`**：活动对象（`status` 变为 `ENDED`）。

**业务规则**：
- 状态转换：`ACTIVE → ENDED`（PENDING 状态的活动不能直接结束）
- 结束后所有学生不能再抢课/退课
- 管理员进入审核阶段

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 404 | 活动不存在 | campaign `id` 未找到 |
| 409 | 活动当前状态不允许此操作 | 当前 `status` 不是 `ACTIVE` |

---

### 5.6 开课审核

#### 5.6.1 待审核列表

```
GET /api/v1/admin/review
```

**权限**：`ADMIN`

**Query Parameters**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `campaignId` | long | 否 | 最近一场已 ENDED 的活动 | 指定活动。后端通过 campaign 关联到 semester，再查出 semester 下所有 PENDING 状态的 offering |
| `page` | int | 是 | 1 | 当前页码 |
| `pageSize` | int | 是 | 20 | 每页条数 |

**后端过滤逻辑**：
1. 查询指定（或最近已结束）的 campaign，获取其 `semesterId`
2. 查询该 semester 下所有 `offering.status = 'PENDING'` 的记录
3. 每条记录计算 `suggestion`：`enrolledCount >= minEnrollment → "建议开课"`，否则 `"建议取消"`

**Response `data`**（分页）：

```json
{
  "items": [
    {
      "offeringId": 1,
      "courseName": "影视鉴赏",
      "teacherName": "张老师",
      "enrolledCount": 42,
      "minEnrollment": 20,
      "suggestion": "建议开课",
      "status": "PENDING"
    },
    {
      "offeringId": 2,
      "courseName": "创业管理",
      "teacherName": "李老师",
      "enrolledCount": 12,
      "minEnrollment": 20,
      "suggestion": "建议取消",
      "status": "PENDING"
    }
  ],
  "total": 30,
  "page": 1,
  "pageSize": 20
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `offeringId` | long | 开课记录 ID |
| `courseName` | string | 课程名称 |
| `teacherName` | string | 授课教师 |
| `enrolledCount` | int | 已报名人数 |
| `minEnrollment` | int | 最低开课人数 |
| `suggestion` | string | `"建议开课"` / `"建议取消"`（仅参考，管理员可自行决定） |
| `status` | string | 固定为 `"PENDING"`（只有 PENDING 状态的 offering 在此列表中） |

---

#### 5.6.2 审核通过

```
POST /api/v1/admin/review/{id}/approve
```

**权限**：`ADMIN`

**Path Parameters**：`id` — 开课记录 ID（`course_offering.id`）

**Response `data`**：

```json
{
  "message": "审核通过，通知正在发送"
}
```

**后端执行链**：

| 步骤 | 操作 |
|------|------|
| ①（同步，事务内） | `UPDATE course_offering SET status = 'APPROVED' WHERE id = ? AND status = 'PENDING'` |
| ②（同步，事务内） | `UPDATE enrollment SET status = 'APPROVED', reviewed_at = NOW() WHERE offering_id = ? AND status = 'ENROLLED'` |
| ③（同步，事务内） | 删除 Redis 相关缓存（`cas:course:list:*`） |
| ④（异步 MQ，事务提交后） | 批量生成 APPROVED 通知（SQL：`INSERT INTO notification SELECT ...`），向所有该课程的学生发送 |

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 404 | 课程不存在 | offering `id` 未找到 |
| 409 | 该课程不是待审核状态 | 当前 `status` 不是 `PENDING`（如已 APPROVED 或 REJECTED） |

---

#### 5.6.3 审核驳回

```
POST /api/v1/admin/review/{id}/reject
```

**权限**：`ADMIN`

**Path Parameters**：`id` — 开课记录 ID（`course_offering.id`）

**Response `data`**：

```json
{
  "message": "已驳回，通知正在发送"
}
```

**后端执行链**：

| 步骤 | 操作 |
|------|------|
| ①（同步，事务内） | `UPDATE course_offering SET status = 'REJECTED' WHERE id = ? AND status = 'PENDING'` |
| ②（同步，事务内） | `UPDATE enrollment SET status = 'REJECTED', reviewed_at = NOW() WHERE offering_id = ? AND status = 'ENROLLED'` |
| ③（同步，事务内） | 删除 Redis 相关缓存 |
| ④（异步 MQ，事务提交后） | 批量生成 REJECTED 通知 |

**错误**：同审核通过。

---

## 六、通知模块

### 6.1 通知列表

```
GET /api/v1/notifications
```

**权限**：已认证（所有角色）

> 后端自动通过 JWT 获取当前用户 `userId`，仅返回该用户的通知。

**Query Parameters**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `type` | string | 否 | — | 筛选：`APPROVED` / `REJECTED` / `SYSTEM` |
| `page` | int | 是 | 1 | 当前页码 |
| `pageSize` | int | 是 | 20 | 每页条数 |

**Response `data`**（分页，按 `created_at` 倒序）：

```json
{
  "items": [
    {
      "id": 1,
      "type": "APPROVED",
      "title": "影视鉴赏审核通过",
      "content": "您报名的《影视鉴赏》已审核通过，将正常开课。",
      "courseName": "影视鉴赏",
      "createdAt": "2025-09-05 10:00:00",
      "read": false
    }
  ],
  "total": 10,
  "page": 1,
  "pageSize": 20
}
```

**字段对照**：

| JSON 字段 | 类型 | DB 列 | 说明 |
|-----------|------|-------|------|
| `id` | long | `id` | |
| `type` | string | `type` | APPROVED / REJECTED / SYSTEM |
| `title` | string | `title` | 通知标题 |
| `content` | string | `content` | 通知正文 |
| `courseName` | string | `course_name` | 相关课程名（冗余存储，防止课程改名后通知内容不一致） |
| `createdAt` | string | `created_at` | |
| `read` | boolean | `is_read` | VO 层转换：`0 → false`, `1 → true` |

---

### 6.2 标记已读

```
PUT /api/v1/notifications/{id}/read
```

**权限**：已认证

**Path Parameters**：`id` — 通知 ID

**Response `data`**：无（`code: 200`）

**错误**：

| HTTP | message | 触发条件 |
|------|---------|---------|
| 403 | 无权操作此通知 | 通知的 `user_id` 与当前用户不匹配 |
| 404 | 通知不存在 | 通知 `id` 未找到 |

---

## 七、API 权限矩阵速查

| 路径 | 方法 | 无需登录 | STUDENT | TEACHER | ADMIN |
|------|------|---------|---------|---------|-------|
| `/auth/login` | POST | ✅ | — | — | — |
| `/auth/me` | GET | — | ✅ | ✅ | ✅ |
| `/student/dashboard` | GET | — | ✅ | — | — |
| `/courses` | GET | — | ✅ | — | — |
| `/courses/{id}` | GET | — | ✅ | — | — |
| `/campaigns/current` | GET | — | ✅ | — | — |
| `/enrollments` | POST | — | ✅ | — | — |
| `/student/enrollments` | GET | — | ✅ | — | — |
| `/enrollments/{id}` | DELETE | — | ✅ | — | — |
| `/teacher/courses` | GET | — | — | ✅ | — |
| `/teacher/courses/{id}/students` | GET | — | — | ✅ | — |
| `/teacher/courses/{id}/students/export` | GET | — | — | ✅ | — |
| `/admin/dashboard/stats` | GET | — | — | — | ✅ |
| `/admin/dashboard/top-courses` | GET | — | — | — | ✅ |
| `/admin/dashboard/trend` | GET | — | — | — | ✅ |
| `/admin/semesters` | GET/POST | — | — | — | ✅ |
| `/admin/semesters/{id}` | PUT | — | — | — | ✅ |
| `/admin/semesters/{id}/activate` | PATCH | — | — | — | ✅ |
| `/admin/courses` | GET/POST | — | — | — | ✅ |
| `/admin/courses/{id}` | PUT/DELETE | — | — | — | ✅ |
| `/admin/offerings` | GET/POST | — | — | — | ✅ |
| `/admin/offerings/{id}` | PUT | — | — | — | ✅ |
| `/admin/campaigns` | GET/POST | — | — | — | ✅ |
| `/admin/campaigns/{id}/start` | PATCH | — | — | — | ✅ |
| `/admin/campaigns/{id}/end` | PATCH | — | — | — | ✅ |
| `/admin/review` | GET | — | — | — | ✅ |
| `/admin/review/{id}/approve` | POST | — | — | — | ✅ |
| `/admin/review/{id}/reject` | POST | — | — | — | ✅ |
| `/notifications` | GET | — | ✅ | ✅ | ✅ |
| `/notifications/{id}/read` | PUT | — | ✅ | ✅ | ✅ |

---

## 八、状态流转图

### 8.1 Campaign（选课活动）状态

```
PENDING ──[start]──→ ACTIVE ──[end]──→ ENDED
                                      ↑ 不可逆
```

- 只有 `PENDING` 才能 start → `ACTIVE`
- 只有 `ACTIVE` 才能 end → `ENDED`
- `ENDED` 不可回到 ACTIVE 或 PENDING

### 8.2 Offering（开课记录）状态

```
PENDING ──[approve]──→ APPROVED
       └──[reject]────→ REJECTED
```

- 管理员审核时才变化
- `APPROVED` / `REJECTED` 不可逆

### 8.3 Enrollment（选课记录）状态

```
ENROLLED ──[approve]──→ APPROVED
        └──[reject]────→ REJECTED
```

- 抢课成功时初始为 `ENROLLED`
- 审核时随 offering 一起批量更新
- 退课时 DELETE 整条记录（不做软删除）

---

## 九、设计与约定

### 9.1 响应字段对齐规则

| 前端字段 | 后端处理 | 说明 |
|---------|---------|------|
| `seatsRemaining` | VO 计算 | `maxCapacity - enrolledCount`，不在 DB 中存储 |
| `read` | VO 转换 | 数据库 `is_read`（TINYINT）→ boolean |
| `courseName` / `teacherName` / `offeringName` / `semesterName` | VO 组装 | 通过 JOIN 从关联表中获取 |

### 9.2 缓存与 TTL 概要

| 缓存键 | 类型 | TTL | 用途 |
|--------|------|-----|------|
| `cas:course:list:{type}:{page}` | String(JSON) | 5min | 课程大厅列表 |
| `cas:campaign:current` | String(JSON) | 30s | 当前活动 |
| `cas:offering:{id}` | String(JSON) | 30s | 课程详情 |
| `cas:enrollment:count:{offeringId}` | String | 10s | 实时名额 |
| `cas:token:blacklist:{jti}` | String | Token 剩余 TTL | 登出黑名单 |

> 所有写操作（增/删/改）都会删除相关缓存，下次读取时从 DB 重新加载。

### 9.3 枚举值一致性

所有枚举值在后端 Java `enum`、数据库 `VARCHAR`、前端 TypeScript `type` 中必须完全一致（大小写敏感）。约定文档见 `数据库设计.md` 第 7 章。

---

> 本文档为 CAS V1.0 前后端对接的唯一契约。所有 API 实现、前端 service 调用、TypeScript 类型定义均以此文档为准。
>
> 相关文档：PRD.md · 数据库设计.md · 前端技术架构设计.md · 后端技术架构设计.md · cas-init.sql