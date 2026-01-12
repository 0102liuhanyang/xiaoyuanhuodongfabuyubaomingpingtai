<template>
  <div class="page">
    <el-card class="card">
      <div class="header">
        <div>
          <h3>活动统计</h3>
          <p class="sub">报名、候补、签到数据与明细</p>
        </div>
        <el-button type="primary" @click="refresh" :disabled="!eventId">刷新</el-button>
      </div>
      <el-form :inline="true" class="filters" @submit.prevent>
        <el-form-item label="活动">
          <el-select v-model="eventId" placeholder="选择活动" style="width: 260px" @change="refresh">
            <el-option v-for="item in events" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :disabled="!eventId" @click="exportCsv">导出报名</el-button>
        </el-form-item>
      </el-form>
      <div class="stats" v-if="eventId">
        <el-card class="stat-card">
          <div class="stat-label">已报名</div>
          <div class="stat-value">{{ chartData.registered }}</div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-label">候补</div>
          <div class="stat-value">{{ chartData.waitlisted }}</div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-label">已签到</div>
          <div class="stat-value">{{ chartData.checkedIn }}</div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-label">未签到</div>
          <div class="stat-value">{{ chartData.absent }}</div>
        </el-card>
      </div>
      <div v-if="chartData.total > 0" style="margin-top: 16px">
        <v-chart :option="option" autoresize style="height: 340px" />
      </div>
      <el-empty v-else-if="eventId && !loading" description="暂无数据" />
      <el-table v-if="eventId" :data="records" style="width: 100%; margin-top: 12px">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column label="报名状态" width="120">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="报名时间" width="180" />
        <el-table-column prop="checkinAt" label="签到时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import request from '../utils/request'
import VChart from 'vue-echarts'
import { getRoles } from '../utils/auth'

const eventId = ref('')
const events = ref([])
const records = ref([])
const loading = ref(false)
const chartData = ref({ registered: 0, waitlisted: 0, checkedIn: 0, absent: 0, total: 0 })

const loadStats = async () => {
  if (!eventId.value) return
  const data = await request.get(`/events/${eventId.value}/stats`)
  const total = (data.registered || 0) + (data.waitlisted || 0)
  chartData.value = {
    registered: data.registered || 0,
    waitlisted: data.waitlisted || 0,
    checkedIn: data.checkedIn || 0,
    absent: (data.registered || 0) - (data.checkedIn || 0),
    total,
  }
}

const loadRegistrations = async () => {
  if (!eventId.value) return
  const data = await request.get(`/events/${eventId.value}/registrations`, { params: { page: 1, size: 200 } })
  records.value = data.records || []
}

const loadEvents = async () => {
  const roles = getRoles()
  if (roles.includes('admin')) {
    const data = await request.get('/events/admin/all', { params: { page: 1, size: 200 } })
    events.value = data.records || []
  } else {
    const data = await request.get('/events/mine', { params: { page: 1, size: 200 } })
    events.value = data.records || []
  }
  if (!eventId.value && events.value.length) {
    eventId.value = events.value[0].id
  }
}

const refresh = async () => {
  if (!eventId.value) return
  loading.value = true
  try {
    await loadStats()
    await loadRegistrations()
  } finally {
    loading.value = false
  }
}

const exportCsv = async () => {
  if (!eventId.value) return
  const blob = await request.get(`/events/${eventId.value}/registrations/export`, { responseType: 'blob' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `registrations-${eventId.value}.csv`
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

const option = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { top: 'bottom', textStyle: { color: '#cbd2ff' } },
  series: [
    {
      type: 'pie',
      radius: ['35%', '60%'],
      label: { formatter: '{b}: {d}%', color: '#cbd2ff' },
      data: [
        { value: chartData.value.checkedIn, name: '已签到' },
        { value: chartData.value.absent, name: '未签到' },
        { value: chartData.value.waitlisted, name: '候补' },
      ],
    },
  ],
}))

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
  await refresh()
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
}
.sub {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--tech-muted);
}
.filters {
  margin-top: 12px;
}
.stats {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}
.stat-card {
  border: 1px solid rgba(79, 214, 255, 0.16);
  background: linear-gradient(135deg, rgba(79, 214, 255, 0.08), rgba(138, 91, 255, 0.08));
}
.stat-label {
  color: var(--tech-muted);
  font-size: 12px;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  margin-top: 6px;
}
</style>

