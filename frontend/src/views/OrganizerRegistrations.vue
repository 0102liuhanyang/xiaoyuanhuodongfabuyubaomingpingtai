<template>
  <div class="page">
    <el-card class="card">
      <div class="header">
        <div>
          <h3>报名管理</h3>
          <p class="sub">查看报名名单、导出 CSV、筛选报名状态</p>
        </div>
        <el-button type="primary" @click="load">刷新</el-button>
      </div>
      <el-form :inline="true" :model="filters" class="filters" @submit.prevent>
        <el-form-item label="活动">
          <el-select v-model="filters.eventId" placeholder="全部" style="width: 220px" @change="onEventChange">
            <el-option label="全部" value="" />
            <el-option v-for="item in events" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="姓名/用户名/手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="已报名" value="registered" />
            <el-option label="候补中" value="waitlisted" />
            <el-option label="已取消" value="canceled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="records" style="width: 100%; margin-top: 12px">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="报名时间" width="180" />
        <el-table-column prop="checkinAt" label="签到时间" width="180" />
      </el-table>
      <el-empty v-if="!loading && records.length === 0" description="暂无报名记录" />
      <div class="footer">
        <el-button :disabled="!filters.eventId" @click="exportCsv">导出报名</el-button>
        <el-pagination
          layout="prev, pager, next"
          :current-page="page"
          :page-size="size"
          :total="total"
          @current-change="onPage"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '../utils/request'

const events = ref([])
const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const filters = reactive({
  eventId: '',
  keyword: '',
  status: '',
})

const loadEvents = async () => {
  const data = await request.get('/events/mine', { params: { page: 1, size: 200 } })
  events.value = data.records || []
}

const load = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      status: filters.status,
      keyword: filters.keyword,
    }
    if (!filters.eventId) {
      const data = await request.get('/events/registrations/organizer', { params })
      records.value = data.records || []
      total.value = data.total || 0
      return
    }
    const data = await request.get(`/events/${filters.eventId}/registrations`, { params })
    records.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const reset = () => {
  filters.keyword = ''
  filters.status = ''
  filters.eventId = ''
  page.value = 1
  load()
}

const onPage = (p) => {
  page.value = p
  load()
}

const onEventChange = () => {
  page.value = 1
  load()
}

const exportCsv = async () => {
  const blob = await request.get(`/events/${filters.eventId}/registrations/export`, { responseType: 'blob' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `registrations-${filters.eventId}.csv`
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

const statusLabel = (status) => {
  if (status === 'registered') return '已报名'
  if (status === 'waitlisted') return '候补中'
  if (status === 'canceled') return '已取消'
  return status || '-'
}

const statusType = (status) => {
  if (status === 'registered') return 'success'
  if (status === 'waitlisted') return 'warning'
  if (status === 'canceled') return 'info'
  return 'info'
}

onMounted(async () => {
  await loadEvents()
  load()
})
</script>

<style scoped>
.page {
  max-width: 1300px;
  margin: 0 auto;
}
.card {
  border: 1px solid var(--tech-border);
  background: var(--tech-card-bg);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.sub {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--tech-muted);
}
.filters {
  margin-top: 12px;
}
.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}
</style>



