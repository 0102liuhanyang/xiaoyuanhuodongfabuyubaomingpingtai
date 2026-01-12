import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import EventList from '../views/EventList.vue'
import EventDetail from '../views/EventDetail.vue'
import MyRegistrations from '../views/MyRegistrations.vue'
import Register from '../views/Register.vue'
import EventManage from '../views/EventManage.vue'
import EventApproval from '../views/EventApproval.vue'
import Forbidden from '../views/Forbidden.vue'
import NotFound from '../views/NotFound.vue'
import Notifications from '../views/Notifications.vue'
import EventStats from '../views/EventStats.vue'
import StatsOverview from '../views/StatsOverview.vue'
import BlacklistManage from '../views/BlacklistManage.vue'
import AdminEvents from '../views/AdminEvents.vue'
import OrganizerRegistrations from '../views/OrganizerRegistrations.vue'
import OrganizerCheckin from '../views/OrganizerCheckin.vue'
import OrganizerStats from '../views/OrganizerStats.vue'
import StudentCheckin from '../views/StudentCheckin.vue'
import request from '../utils/request'
import { clearAuth, getRoles, getToken, setRoles } from '../utils/auth'
import { useAuthStore } from '../store/auth'

const routes = [
  { path: '/login', name: 'login', component: Login },
  { path: '/register', name: 'register', component: Register },
  { path: '/', name: 'events', component: EventList },
  { path: '/events/:id', name: 'eventDetail', component: EventDetail, props: true },
  { path: '/my/registrations', name: 'myRegistrations', component: MyRegistrations, meta: { requiresAuth: true, roles: ['student'] } },
  { path: '/checkin', name: 'studentCheckin', component: StudentCheckin, meta: { requiresAuth: true, roles: ['student'] } },
  { path: '/manage/events', name: 'manageEvents', component: EventManage, meta: { requiresAuth: true, roles: ['organizer'] } },
  { path: '/manage/events/new', name: 'manageEventsNew', component: EventManage, meta: { requiresAuth: true, roles: ['organizer'] } },
  { path: '/manage/events/:id/edit', name: 'manageEventsEdit', component: EventManage, meta: { requiresAuth: true, roles: ['organizer'] } },
  { path: '/manage/registrations', name: 'manageRegistrations', component: OrganizerRegistrations, meta: { requiresAuth: true, roles: ['organizer'] } },
  { path: '/manage/checkin', name: 'manageCheckin', component: OrganizerCheckin, meta: { requiresAuth: true, roles: ['organizer'] } },
  { path: '/manage/stats', name: 'manageStats', component: OrganizerStats, meta: { requiresAuth: true, roles: ['organizer'] } },
  { path: '/admin/approvals', name: 'adminApprovals', component: EventApproval, meta: { requiresAuth: true, roles: ['admin'] } },
  { path: '/admin/events', name: 'adminEvents', component: AdminEvents, meta: { requiresAuth: true, roles: ['admin'] } },
  { path: '/events/:id/stats', name: 'eventStats', component: EventStats, meta: { requiresAuth: true, roles: ['admin', 'organizer'] } },
  { path: '/admin/stats', name: 'statsOverview', component: StatsOverview, meta: { requiresAuth: true, roles: ['admin'] } },
  { path: '/admin/blacklist', name: 'blacklistManage', component: BlacklistManage, meta: { requiresAuth: true, roles: ['admin'] } },
  { path: '/notifications', name: 'notifications', component: Notifications, meta: { requiresAuth: true } },
  { path: '/403', name: 'forbidden', component: Forbidden },
  { path: '/:pathMatch(.*)*', name: 'notfound', component: NotFound },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

let userLoaded = false
const getDefaultPath = (roles) => {
  if (roles.includes('admin')) return '/admin/approvals'
  if (roles.includes('organizer')) return '/manage/events'
  return '/'
}

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  const token = getToken()
  if (!token && to.path !== '/login' && to.path !== '/register') {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  if (token && !userLoaded) {
    try {
      const profile = await request.get('/auth/profile')
      if (profile?.roles) {
        setRoles(profile.roles)
        authStore.setAuth({ token, roles: profile.roles, name: profile.name })
      }
    } catch (e) {
      clearAuth()
      return next('/login')
    } finally {
      userLoaded = true
    }
  }
  if (token && from.path === '/login' && to.path === '/') {
    const target = getDefaultPath(getRoles())
    if (target !== '/') {
      return next(target)
    }
  }
  const routeRoles = to.meta?.roles
  if (routeRoles && routeRoles.length) {
    const userRoles = getRoles()
    const allowed = routeRoles.some((r) => userRoles.includes(r))
    if (!allowed) {
      return next('/403')
    }
  }
  next()
})

export default router
