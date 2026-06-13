import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
    },
    {
      path: '/student',
      component: () => import('@/layouts/StudentLayout.vue'),
      meta: { role: 'STUDENT' as const },
      children: [
        { path: '', redirect: { name: 'student-dashboard' } },
        {
          path: 'dashboard',
          name: 'student-dashboard',
          component: () => import('@/views/student/dashboard/index.vue'),
        },
        {
          path: 'courses',
          name: 'student-courses',
          component: () => import('@/views/student/courses/index.vue'),
        },
        {
          path: 'courses/:id',
          name: 'student-course-detail',
          component: () => import('@/views/student/courses/[id].vue'),
        },
        {
          path: 'my-courses',
          name: 'student-my-courses',
          component: () => import('@/views/student/my-courses/index.vue'),
        },
        {
          path: 'notifications',
          name: 'student-notifications',
          component: () => import('@/views/student/notifications/index.vue'),
        },
      ],
    },
    {
      path: '/teacher',
      component: () => import('@/layouts/TeacherLayout.vue'),
      meta: { role: 'TEACHER' as const },
      children: [
        { path: '', redirect: { name: 'teacher-courses' } },
        {
          path: 'courses',
          name: 'teacher-courses',
          component: () => import('@/views/teacher/courses/index.vue'),
        },
        {
          path: 'courses/:id/students',
          name: 'teacher-students',
          component: () => import('@/views/teacher/courses/[id]/students.vue'),
        },
      ],
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { role: 'ADMIN' as const },
      children: [
        { path: '', redirect: { name: 'admin-dashboard' } },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('@/views/admin/dashboard/index.vue'),
        },
        {
          path: 'semesters',
          name: 'admin-semesters',
          component: () => import('@/views/admin/semesters/index.vue'),
        },
        {
          path: 'courses',
          name: 'admin-courses',
          component: () => import('@/views/admin/courses/index.vue'),
        },
        {
          path: 'offerings',
          name: 'admin-offerings',
          component: () => import('@/views/admin/offerings/index.vue'),
        },
        {
          path: 'campaigns',
          name: 'admin-campaigns',
          component: () => import('@/views/admin/campaigns/index.vue'),
        },
        {
          path: 'review',
          name: 'admin-review',
          component: () => import('@/views/admin/review/index.vue'),
        },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/login' },
  ],
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()

  if (!auth.isAuthenticated && to.path !== '/login') {
    return next('/login')
  }

  if (to.meta?.role && auth.user?.role !== to.meta.role) {
    return next('/login')
  }

  next()
})

export default router