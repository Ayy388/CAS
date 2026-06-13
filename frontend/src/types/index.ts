// ========================================
// 枚举类型别名
// ========================================
export type UserRole = 'STUDENT' | 'TEACHER' | 'ADMIN'
export type CourseType = 'REQUIRED' | 'ELECTIVE_MAJOR' | 'ELECTIVE_GENERAL'
export type CampaignStatus = 'PENDING' | 'ACTIVE' | 'ENDED'
export type OfferingStatus = 'PENDING' | 'APPROVED' | 'REJECTED'
export type EnrollmentStatus = 'ENROLLED' | 'APPROVED' | 'REJECTED'
export type NotificationType = 'APPROVED' | 'REJECTED' | 'SYSTEM'

// ========================================
// 核心领域模型
// ========================================
export interface User {
  id: number
  username: string
  realName: string
  role: UserRole
  department: string | null
  major: string | null
  grade: string | null
}

export interface Semester {
  id: number
  name: string
  academicYear: string
  semesterType: 'FIRST' | 'SECOND'
  startDate: string
  endDate: string
  status: 'ACTIVE' | 'INACTIVE'
}

export interface Course {
  id: number
  code: string
  name: string
  type: CourseType
  credits: number
  hours: number
  description: string
  createdAt: string
}

export interface CourseOffering {
  id: number
  semesterId: number
  semesterName: string
  courseId: number
  courseName: string
  courseType: CourseType
  credits: number
  hours: number
  teacherId: number
  teacherName: string
  maxCapacity: number
  minEnrollment: number
  enrolledCount: number
  seatsRemaining: number
  openGrade: string
  openMajor: string
  location: string
  schedule: string
  description?: string
  status: OfferingStatus
}

export interface SelectionCampaign {
  id: number
  name: string
  semesterId: number
  semesterName: string
  startTime: string
  endTime: string
  status: CampaignStatus
}

export interface Enrollment {
  id: number
  campaignId: number
  offeringId: number
  offeringName: string
  studentId: number
  studentName: string
  status: EnrollmentStatus
  enrolledAt: string
  reviewedAt?: string
  teacherName?: string
  credits?: number
  hours?: number
  location?: string
  schedule?: string
}

export interface Notification {
  id: number
  type: NotificationType
  title: string
  content: string
  courseName: string
  createdAt: string
  read: boolean
}

// ========================================
// DTO
// ========================================
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResponse<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

// ========================================
// API 响应类型
// ========================================
export interface LoginResponse {
  token: string
  user: User
}

export interface DashboardStats {
  totalCourses: number
  totalTeachers: number
  totalStudents: number
  totalEnrollments: number
}

export interface TopCourse {
  courseName: string
  enrolledCount: number
  maxCapacity: number
  filledAt: string | null
}

export interface TrendPoint {
  time: string
  count: number
}

export interface StudentDashboardData {
  semester: Semester | null
  currentCampaign: SelectionCampaign | null
  myEnrollment: Enrollment | null
}

export interface ReviewItem {
  offeringId: number
  courseName: string
  teacherName: string
  enrolledCount: number
  minEnrollment: number
}

export interface StudentListItem {
  studentId: number
  studentNumber: string
  studentName: string
  department: string
  major: string
  enrolledAt: string
}

export interface TeacherCourseItem {
  id: number
  courseName: string
  semesterName: string
  maxCapacity: number
  enrolledCount: number
  status: OfferingStatus
}

// ========================================
// 状态映射表
// ========================================
export const CAMPAIGN_STATUS_MAP: Record<CampaignStatus, string> = {
  PENDING: '未开始',
  ACTIVE: '进行中',
  ENDED: '已结束',
}

export const OFFERING_STATUS_MAP: Record<OfferingStatus, string> = {
  PENDING: '待审核',
  APPROVED: '审核通过',
  REJECTED: '审核不通过',
}

export const ENROLLMENT_STATUS_MAP: Record<EnrollmentStatus, string> = {
  ENROLLED: '已报名',
  APPROVED: '审核通过',
  REJECTED: '审核不通过',
}

export const COURSE_TYPE_MAP: Record<CourseType, string> = {
  REQUIRED: '必修课',
  ELECTIVE_MAJOR: '专业选修课',
  ELECTIVE_GENERAL: '通识课',
}