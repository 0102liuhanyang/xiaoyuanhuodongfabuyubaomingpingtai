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
          text-color="#cbd2ff"
          active-text-color="#4fd6ff"
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
:global(:root) {
  --tech-bg-1: #0f1024;
  --tech-bg-2: #1a1b3a;
  --tech-bg-3: #2b2051;
  --tech-accent: #4fd6ff;
  --tech-accent-2: #8a5bff;
  --tech-accent-3: #2ee59d;
  --tech-text: #eef0ff;
  --tech-muted: rgba(238, 240, 255, 0.68);
  --tech-border: rgba(120, 132, 255, 0.18);
  --tech-card-bg: rgba(18, 22, 56, 0.72);
  --tech-card-bg-light: rgba(255, 255, 255, 0.06);
  --tech-radius: 16px;
  --tech-shadow: 0 14px 36px rgba(10, 12, 30, 0.55);
  --tech-glow: 0 0 24px rgba(79, 214, 255, 0.18);
}
:global(body) {
  margin: 0;
  position: relative;
  color: var(--tech-text);
  font-family: 'Rajdhani', 'DIN Alternate', 'Microsoft YaHei', 'PingFang SC', sans-serif;
  background:
    radial-gradient(circle at 20% 10%, rgba(79, 214, 255, 0.12), transparent 35%),
    radial-gradient(circle at 80% 20%, rgba(138, 91, 255, 0.14), transparent 40%),
    linear-gradient(160deg, var(--tech-bg-1) 0%, var(--tech-bg-2) 55%, var(--tech-bg-3) 100%);
}
:global(#app) {
  position: relative;
  z-index: 1;
}
:global(.el-card) {
  background: var(--tech-card-bg);
  border: 1px solid var(--tech-border);
  border-radius: var(--tech-radius);
  box-shadow: var(--tech-shadow);
  color: var(--tech-text);
}
:global(.el-card__header) {
  border-bottom: 1px solid var(--tech-border);
}
:global(.el-table) {
  background: transparent;
  color: var(--tech-text);
}
:global(.el-table th.el-table__cell) {
  background: rgba(28, 34, 70, 0.9);
  color: var(--tech-text);
}
:global(.el-table__header-wrapper),
:global(.el-table__header-wrapper table),
:global(.el-table__header-wrapper thead),
:global(.el-table__header-wrapper tr) {
  background: rgba(28, 34, 70, 0.9) !important;
}
:global(.el-table__header-wrapper th.el-table__cell) {
  background: rgba(28, 34, 70, 0.9) !important;
}
:global(.el-table td.el-table__cell) {
  background: rgba(22, 26, 60, 0.86);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
:global(.el-table__body tr.el-table__row) {
  background: rgba(22, 26, 60, 0.86) !important;
}
:global(.el-table__body tr.el-table__row--striped) {
  background: rgba(22, 26, 60, 0.86) !important;
}
:global(.el-table__empty-block) {
  background: rgba(18, 22, 56, 0.72);
}
:global(.el-table__inner-wrapper::before) {
  background-color: rgba(255, 255, 255, 0.08);
}
:global(.el-table__fixed),
:global(.el-table__fixed-right),
:global(.el-table__fixed-left) {
  background: rgba(22, 26, 60, 0.86);
}
:global(.el-table__fixed-header-wrapper),
:global(.el-table__fixed-body-wrapper) {
  background: rgba(22, 26, 60, 0.86);
}
:global(.el-table__fixed-header-wrapper .el-table__header th.el-table__cell) {
  background: rgba(28, 34, 70, 0.9);
}
:global(.el-table__fixed-right .el-table__cell),
:global(.el-table__fixed-left .el-table__cell) {
  background: rgba(22, 26, 60, 0.86) !important;
}
:global(.el-table__fixed-right th.el-table__cell),
:global(.el-table__fixed-left th.el-table__cell) {
  background: rgba(28, 34, 70, 0.9) !important;
}
:global(.el-table__fixed-right-patch) {
  background: rgba(28, 34, 70, 0.9) !important;
}
:global(.el-table__fixed-right .el-table__header-wrapper),
:global(.el-table__fixed-right .el-table__header-wrapper table),
:global(.el-table__fixed-right .el-table__header-wrapper thead),
:global(.el-table__fixed-right .el-table__header-wrapper tr) {
  background: rgba(28, 34, 70, 0.9) !important;
}
:global(.el-table__fixed-left .el-table__header-wrapper),
:global(.el-table__fixed-left .el-table__header-wrapper table),
:global(.el-table__fixed-left .el-table__header-wrapper thead),
:global(.el-table__fixed-left .el-table__header-wrapper tr) {
  background: rgba(28, 34, 70, 0.9) !important;
}
:global(.el-table__body tr:hover > td.el-table__cell) {
  background: rgba(79, 214, 255, 0.12);
}
:global(.el-table__body tr.el-table__row--hover) {
  background: rgba(79, 214, 255, 0.12);
}
:global(.el-pagination) {
  --el-pagination-bg-color: rgba(18, 22, 56, 0.72);
  --el-pagination-button-color: var(--tech-text);
  --el-pagination-hover-color: #4fd6ff;
  --el-pagination-button-bg-color: rgba(18, 22, 56, 0.72);
  --el-pagination-disabled-bg-color: rgba(18, 22, 56, 0.5);
}
:global(.el-pagination .btn-prev),
:global(.el-pagination .btn-next),
:global(.el-pager li) {
  background: rgba(22, 26, 60, 0.86) !important;
  color: var(--tech-text);
  border: 1px solid rgba(255, 255, 255, 0.08);
}
:global(.el-pager li.is-active) {
  background: rgba(79, 214, 255, 0.18) !important;
  color: var(--tech-text);
}
:global(.el-tag) {
  --el-tag-bg-color: rgba(79, 214, 255, 0.12);
  --el-tag-border-color: rgba(79, 214, 255, 0.3);
  --el-tag-text-color: var(--tech-text);
}
:global(.el-textarea__inner),
:global(.el-input__inner) {
  background: rgba(18, 22, 56, 0.72);
}
:global(.el-select-dropdown__item) {
  color: var(--tech-text);
}
:global(.el-select-dropdown__item.is-hovering) {
  background: rgba(79, 214, 255, 0.12);
}
:global(.el-select-dropdown__wrap) {
  background: rgba(18, 22, 56, 0.95);
}
:global(.el-dropdown-menu) {
  background: rgba(18, 22, 56, 0.95);
  border: 1px solid var(--tech-border);
}
:global(.el-button:not(.el-button--primary)) {
  color: var(--tech-text);
  border-color: rgba(79, 214, 255, 0.3);
  background: rgba(18, 22, 56, 0.5);
}
:global(.el-input__wrapper),
:global(.el-select__wrapper),
:global(.el-date-editor) {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: none;
}
:global(.el-input__inner),
:global(.el-select__selected-item),
:global(.el-date-editor .el-range-input) {
  color: var(--tech-text);
}
:global(.el-input__inner::placeholder),
:global(.el-select__placeholder),
:global(.el-range-input::placeholder) {
  color: rgba(203, 210, 255, 0.6);
}
:global(.el-button--primary) {
  background: linear-gradient(135deg, #4fd6ff 0%, #8a5bff 100%);
  border: none;
  box-shadow: var(--tech-glow);
}
:global(.el-button--primary.is-link) {
  background: transparent;
  box-shadow: none;
}
:global(.el-empty__description) {
  color: var(--tech-muted);
}
:global(.el-dialog) {
  background: var(--tech-card-bg);
  border: 1px solid var(--tech-border);
  color: var(--tech-text);
}
:global(.el-dialog__title) {
  color: var(--tech-text);
}
:global(.el-descriptions__label),
:global(.el-descriptions__content) {
  color: var(--tech-text);
}
:global(.page) {
  max-width: 100% !important;
  width: 100%;
  margin: 0 !important;
}
:global(.el-loading-mask) {
  background-color: transparent !important;
  pointer-events: none;
}
:global(.el-overlay) {
  background-color: transparent !important;
  pointer-events: none;
}
.layout {
  min-height: 100vh;
  position: relative;
  z-index: 1;
}
.layout :global(.el-container) {
  background: transparent;
}
.aside {
  background: linear-gradient(180deg, rgba(18, 22, 56, 0.95) 0%, rgba(14, 16, 38, 0.98) 100%);
  color: var(--tech-text);
  border-right: 1px solid var(--tech-border);
  box-shadow: inset -1px 0 0 rgba(255, 255, 255, 0.03);
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
  background: rgba(79, 214, 255, 0.18);
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-weight: 700;
  letter-spacing: 1px;
  box-shadow: var(--tech-glow);
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
  color: var(--tech-text);
}
.side-menu :deep(.el-menu-item.is-active) {
  background: rgba(79, 214, 255, 0.18);
  box-shadow: inset 0 0 0 1px rgba(79, 214, 255, 0.25);
}
.header {
  background: rgba(18, 22, 56, 0.65);
  border-bottom: 1px solid var(--tech-border);
  color: var(--tech-text);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-title {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.4px;
}
.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}
.username {
  color: var(--tech-text);
}
.badge {
  margin-left: 6px;
}
.main {
  padding: 0;
}
</style>
