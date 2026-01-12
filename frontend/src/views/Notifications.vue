<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="page">
    <el-card class="card">
      <div class="header">
        <div>
          <h3>通知中心</h3>
          <p class="sub">查看系统与活动通知，支持筛选与广播</p>
        </div>
        <el-button type="primary" @click="load">刷新</el-button>
      </div>
      <el-form :inline="true" :model="filters" class="filters" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="filters.read" placeholder="全部" clearable style="width: 140px">
            <el-option label="未读" :value="false" />
            <el-option label="已读" :value="true" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-card v-if="isAdmin" class="broadcast">
        <div class="broadcast-title">通知广播</div>
        <el-form :model="broadcast" label-position="top">
          <el-form-item label="标题">
            <el-input v-model="broadcast.title" placeholder="请输入标题" />
          </el-form-item>
          <el-form-item label="内容">
            <el-input v-model="broadcast.content" type="textarea" rows="3" placeholder="请输入内容" />
          </el-form-item>
          <el-form-item label="角色（可选）">
            <el-select v-model="broadcast.role" placeholder="全部用户" clearable style="width: 200px">
              <el-option label="学生" value="student" />
              <el-option label="组织者" value="organizer" />
              <el-option label="管理员" value="admin" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="sendBroadcast">发送通知</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-scrollbar v-if="list.length" height="500px">
        <div v-for="item in list" :key="item.id" class="item">
          <div class="title">
            <el-tag size="small" :type="item.readFlag ? 'info' : 'warning'">{{ item.type || '通知' }}</el-tag>
            <span :class="['msg', { unread: !item.readFlag }]">{{ item.title }}</span>
          </div>
          <div class="content">{{ item.content }}</div>
          <div class="meta">
            <span>{{ item.createdAt }}</span>
            <el-button v-if="!item.readFlag" link type="primary" @click="markRead(item.id)">标记已读</el-button>
          </div>
          <el-divider />
        </div>
      </el-scrollbar>
      <div v-else style="padding: 16px; text-align: center; color: #888;">暂无通知</div>
      <el-pagination
        layout="prev, pager, next"
        :current-page="page"
        :page-size="size"
        :total="total"
        @current-change="onPage"
        style="margin-top: 16px; text-align: right"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { getRoles, getToken } from '../utils/auth'

const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const filters = reactive({
  read: null,
})
const broadcast = reactive({
  title: '',
  content: '',
  role: '',
})
const isAdmin = ref(false)

const load = async () => {
  if (!getToken()) {
    list.value = []
    total.value = 0
    return
  }
  const params = {
    page: page.value,
    size: size.value,
    read: filters.read,
  }
  try {
    const data = await request.get('/notifications', { params })
    list.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    // 退出后可能出现 403，避免报错打断渲染
    list.value = []
    total.value = 0
  }
}

const markRead = async (id) => {
  await request.post(`/notifications/${id}/read`)
  load()
  window.dispatchEvent(new Event('notifications:refresh'))
}

const onSearch = () => {
  page.value = 1
  load()
}

const reset = () => {
  filters.read = null
  page.value = 1
  load()
}

const onPage = (p) => {
  page.value = p
  load()
}

const sendBroadcast = async () => {
  if (!broadcast.title || !broadcast.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  await request.post('/notifications/broadcast', null, { params: broadcast })
  ElMessage.success('已发送通知')
  broadcast.title = ''
  broadcast.content = ''
  broadcast.role = ''
  window.dispatchEvent(new Event('notifications:refresh'))
}

onMounted(load)
onMounted(() => {
  const roles = getRoles()
  isAdmin.value = roles.includes('admin')
})
</script>

<style scoped>
.page {
  max-width: 1100px;
  margin: 0 auto;
}
.card {
  border: none;
  background: rgba(255, 255, 255, 0.95);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.sub {
  margin: 6px 0 0;
  font-size: 12px;
  color: #7b7b7b;
}
.filters {
  margin-top: 12px;
}
.broadcast {
  margin: 12px 0;
  border: none;
  background: rgba(0, 0, 0, 0.03);
}
.broadcast-title {
  font-weight: 600;
  margin-bottom: 8px;
}
.item {
  padding: 12px 0;
}
.title {
  display: flex;
  align-items: center;
  gap: 8px;
}
.msg.unread {
  font-weight: 600;
}
.content {
  margin: 4px 0;
  color: #666;
}
.meta {
  font-size: 12px;
  color: #999;
  display: flex;
  justify-content: space-between;
}
</style>
