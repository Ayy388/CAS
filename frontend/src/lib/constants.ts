import {
  House,
  BookOpen,
  GraduationCap,
  Bell,
  LayoutDashboard,
  Calendar,
  BookCheck,
  ClipboardList,
  ScrollText,
  Users,
  type LucideIcon,
} from 'lucide-vue-next'

export interface NavItem {
  to: string
  label: string
  icon: LucideIcon
}

export const STUDENT_NAV_ITEMS: NavItem[] = [
  { to: '/student/dashboard', label: '首页', icon: House },
  { to: '/student/courses', label: '课程大厅', icon: BookOpen },
  { to: '/student/my-courses', label: '我的课程', icon: GraduationCap },
  { to: '/student/notifications', label: '通知中心', icon: Bell },
]

export const TEACHER_NAV_ITEMS: NavItem[] = [
  { to: '/teacher/courses', label: '我的课程', icon: BookCheck },
]

export const ADMIN_NAV_ITEMS: NavItem[] = [
  { to: '/admin/dashboard', label: '数据看板', icon: LayoutDashboard },
  { to: '/admin/semesters', label: '学期管理', icon: Calendar },
  { to: '/admin/courses', label: '课程管理', icon: BookOpen },
  { to: '/admin/offerings', label: '开课管理', icon: ClipboardList },
  { to: '/admin/campaigns', label: '选课活动', icon: ScrollText },
  { to: '/admin/review', label: '开课审核', icon: Users },
]