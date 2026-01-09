<template>
  <div id="app">
    <el-container class="layout">
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
          <el-menu-item index="/">
            <span>活动列表</span>
          </el-menu-item>
          <el-menu-item index="/my/registrations">
            <span>我的报名</span>
          </el-menu-item>
          <el-menu-item index="/manage/events">
            <span>活动管理</span>
          </el-menu-item>
          <el-menu-item index="/login">
            <span>登录</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-title">校园活动发布与报名管理平台</div>
        </el-header>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const activePath = computed(() => route.path)

const onSelect = (path) => {
  router.push(path)
}
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
.main {
  padding: 20px;
}
</style>
