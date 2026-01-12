<template>
  <div class="page">
    <el-card class="card">
      <div class="header">
        <div>
          <h3>活动统计</h3>
          <p class="sub">选择活动查看报名与签到统计</p>
        </div>
        <el-button type="primary" @click="refresh">刷新</el-button>
      </div>
      <el-form :inline="true" class="filters" @submit.prevent>
        <el-form-item label="活动">
          <el-select v-model="eventId" placeholder="全部" style="width: 260px" @change="refresh">
            <el-option label="全部" value="" />
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
      <div v-if="showCharts" class="charts">
        <el-card class="chart-card">
          <div class="chart-title">签到占比</div>
          <v-chart :option="option" autoresize style="height: 300px" />
        </el-card>
        <el-card class="chart-card">
          <div class="chart-title">报名与签到对比</div>
          <v-chart :option="optionBar" autoresize style="height: 300px" />
        </el-card>
      </div>
      <el-empty v-else-if="eventId && !loading" description="暂无数据" />
      <el-table :data="records" style="width: 100%; margin-top: 12px">
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
      <el-empty v-if="!loading && records.length === 0" description="暂无报名明细" />
      <div class="footer">
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
import { computed, onMounted, ref } from 'vue'
import request from '../utils/request'
import VChart from 'vue-echarts'

const eventId = ref('')
const events = ref([])
const records = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const chartData = ref({ registered: 0, waitlisted: 0, checkedIn: 0, absent: 0, total: 0 })

const loadEvents = async () => {
  const data = await request.get('/events/mine', { params: { page: 1, size: 200 } })
  events.value = data.records || []
}

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
  if (!eventId.value) {
    const data = await request.get('/events/registrations/organizer', {
      params: { page: page.value, size: size.value },
    })
    records.value = data.records || []
    total.value = data.total || 0
    const registered = records.value.filter((item) => item.status === 'registered').length
    const waitlisted = records.value.filter((item) => item.status === 'waitlisted').length
    const checkedIn = records.value.filter((item) => item.checkinAt).length
    chartData.value = {
      registered,
      waitlisted,
      checkedIn,
      absent: Math.max(registered - checkedIn, 0),
      total: registered + waitlisted,
    }
    return
  }
  const data = await request.get(`/events/${eventId.value}/registrations`, {
    params: { page: page.value, size: size.value },
  })
  records.value = data.records || []
  total.value = data.total || 0
}

const refresh = async () => {
  page.value = 1
  loading.value = true
  try {
    await loadStats()
    await loadRegistrations()
  } finally {
    loading.value = false
  }
}

const onPage = (p) => {
  page.value = p
  loadRegistrations()
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
  legend: { bottom: 0, textStyle: { color: '#cbd2ff' } },
  series: [
    {
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      label: { formatter: '{b}: {d}%', color: '#cbd2ff' },
      data: [
        { value: chartData.value.checkedIn, name: '已签到', itemStyle: { color: '#52c41a' } },
        { value: chartData.value.absent, name: '未签到', itemStyle: { color: '#f5222d' } },
        { value: chartData.value.waitlisted, name: '候补', itemStyle: { color: '#faad14' } },
      ],
    },
  ],
}))

const optionBar = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 24, right: 24, top: 24, bottom: 24, containLabel: true },
  xAxis: {
    type: 'category',
    data: ['已报名', '候补', '已签到', '未签到'],
    axisLine: { lineStyle: { color: 'rgba(239, 242, 255, 0.25)' } },
    axisLabel: { color: '#cbd2ff' },
  },
  yAxis: {
    type: 'value',
    axisLine: { show: false },
    splitLine: { lineStyle: { color: 'rgba(239, 242, 255, 0.12)' } },
    axisLabel: { color: '#cbd2ff' },
  },
  series: [
    {
      type: 'bar',
      barWidth: 28,
      data: [
        chartData.value.registered || 0,
        chartData.value.waitlisted || 0,
        chartData.value.checkedIn || 0,
        chartData.value.absent || 0,
      ],
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: '#7c5cf3' },
            { offset: 1, color: '#5a37c4' },
          ],
        },
      },
    },
  ],
}))

const showCharts = computed(() => {
  if (!eventId.value) return records.value.length > 0
  return chartData.value.total > 0
})

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
.footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.charts {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}
.chart-card {
  border: 1px solid rgba(79, 214, 255, 0.14);
  background: rgba(16, 20, 52, 0.7);
}
.chart-title {
  font-weight: 600;
  color: var(--tech-text);
  margin-bottom: 8px;
}
@media (max-width: 1024px) {
  .charts {
    grid-template-columns: 1fr;
  }
}
</style>
