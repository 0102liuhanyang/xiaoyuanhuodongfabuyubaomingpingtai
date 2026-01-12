<template>
  <div id="app">
    <router-view v-if="!isLoggedIn" />
    <el-container v-else class="layout">
      <el-aside width="220px" class="aside">
        <div class="brand">
          <div class="brand-logo">校园</div>
          <div class="brand-text">活动发布与报名</div>
        </div>
        <el-menu
          class="side-menu"
          :default-active="activePath"
          background-color="transparent"
          text-color="#f5eaff"
          active-text-color="#ffd26f"
          @select="onSelect"
        >
          <el-menu-item index="/" v-if="isStudent || isOrganizer">
            <span>活动列表</span>
          </el-menu-item>
          <el-menu-item index="/my/registrations" v-if="isStudent">
            <span>我的报名</span>
          </el-menu-item>
          <el-menu-item index="/checkin" v-if="isStudent">
            <span>签到入口</span>
          </el-menu-item>
          <el-menu-item index="/manage/events" v-if="isOrganizer">
            <span>活动管理</span>
          </el-menu-item>
          <el-menu-item index="/manage/registrations" v-if="isOrganizer">
            <span>报名管理</span>
          </el-menu-item>
          <el-menu-item index="/manage/checkin" v-if="isOrganizer">
            <span>签到管理</span>
          </el-menu-item>
          <el-menu-item index="/manage/stats" v-if="isOrganizer">
            <span>活动统计</span>
          </el-menu-item>
          <el-menu-item index="/admin/approvals" v-if="isAdmin">
            <span>活动审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/events" v-if="isAdmin">
            <span>活动监管</span>
          </el-menu-item>
          <el-menu-item index="/admin/stats" v-if="isAdmin">
            <span>汇总统计</span>
          </el-menu-item>
          <el-menu-item index="/admin/blacklist" v-if="isAdmin">
            <span>黑名单管理</span>
          </el-menu-item>
          <el-menu-item index="/notifications" v-if="isLoggedIn">
            <span>
              通知中心
              <el-badge v-if="unread > 0" :value="unread" class="badge" />
            </span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-title">校园活动发布与报名管理平台</div>
          <div class="header-user">
            <el-avatar size="small" class="avatar">{{ nameInitial }}</el-avatar>
            <span class="username">{{ displayName }}</span>
            <el-button link type="primary" @click="logout">退出</el-button>
          </div>
        </el-header>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './store/auth'
import request from './utils/request'

const route = useRoute()
const router = useRouter()
const activePath = computed(() => route.path)

const onSelect = (path) => {
  router.push(path)
}

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn.value)
const isAdmin = computed(() => authStore.isAdmin.value)
const isStudent = computed(() => authStore.state.roles.includes('student'))
const isOrganizer = computed(() => authStore.state.roles.includes('organizer'))
const displayName = computed(() => authStore.state.name || '用户')
const nameInitial = computed(() => displayName.value.slice(0, 1))
const unread = ref(0)

let unreadTimer = null

const logout = () => {
  authStore.reset()
  unread.value = 0
  if (unreadTimer) {
    clearInterval(unreadTimer)
    unreadTimer = null
  }
  router.push('/login')
}

const fetchUnread = async () => {
  if (!isLoggedIn.value) {
    unread.value = 0
    return
  }
  try {
    const data = await request.get('/notifications/unread-count')
    unread.value = data || 0
  } catch (e) {
    unread.value = 0
  }
}

onMounted(() => {
  fetchUnread()
  unreadTimer = setInterval(fetchUnread, 30000)
})

const handleUnreadRefresh = () => {
  fetchUnread()
}

onMounted(() => {
  window.addEventListener('notifications:refresh', handleUnreadRefresh)
})

onUnmounted(() => {
  window.removeEventListener('notifications:refresh', handleUnreadRefresh)
  if (unreadTimer) {
    clearInterval(unreadTimer)
    unreadTimer = null
  }
})
</script>

<style scoped>
:global(body) {
  margin: 0;
  background: linear-gradient(160deg, #2c1e4a 0%, #3f2b63 50%, #523178 100%);
  color: #f7f5fb;
}
.layout {
  min-height: 100vh;
}
.aside {
  background: linear-gradient(180deg, #3d2b63 0%, #2e1f4c 100%);
  color: #f5eaff;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  flex-direction: column;
}
.brand {
  padding: 20px 16px 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.brand-logo {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-weight: 700;
  letter-spacing: 1px;
}
.brand-text {
  font-weight: 600;
  line-height: 1.2;
}
.side-menu {
  border-right: none;
  background: transparent;
  margin-top: 8px;
}
.side-menu :deep(.el-menu-item) {
  border-radius: 8px;
  margin: 4px 8px;
}
.side-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 210, 111, 0.18);
}
.header {
  background: rgba(255, 255, 255, 0.04);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  color: #f7f5fb;
}
.header-title {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.4px;
}
.badge {
  margin-left: 6px;
}
.main {
  padding: 20px;
}
</style>
