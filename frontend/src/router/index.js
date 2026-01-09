import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import EventList from '../views/EventList.vue'
import EventDetail from '../views/EventDetail.vue'
import MyRegistrations from '../views/MyRegistrations.vue'
import EventManage from '../views/EventManage.vue'

const routes = [
  { path: '/login', name: 'login', component: Login },
  { path: '/', name: 'events', component: EventList },
  { path: '/events/:id', name: 'eventDetail', component: EventDetail, props: true },
  { path: '/my/registrations', name: 'myRegistrations', component: MyRegistrations },
  { path: '/manage/events', name: 'manageEvents', component: EventManage },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
