<template>
  <div class="page">
    <el-card class="card">
      <div class="header">
        <div>
          <h3>统计概览</h3>
          <p class="sub">按组织与时间范围查看汇总统计</p>
        </div>
        <el-button type="primary" @click="load">查询</el-button>
      </div>
      <el-form :inline="true" class="filters" @submit.prevent>
        <el-form-item label="组织">
          <el-select v-model="orgId" placeholder="全部组织" clearable style="width: 220px">
            <el-option v-for="org in orgs" :key="org.id" :label="org.name" :value="org.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      <div v-if="data">
        <el-row :gutter="16" style="margin-top: 12px">
          <el-col :span="6"><el-card shadow="hover" class="stat-card">活动总数：{{ data.events }}</el-card></el-col>
          <el-col :span="6"><el-card shadow="hover" class="stat-card">发布：{{ data.published }}</el-card></el-col>
          <el-col :span="6"><el-card shadow="hover" class="stat-card">报名总数：{{ data.registrations }}</el-card></el-col>
          <el-col :span="6"><el-card shadow="hover" class="stat-card">签到总数：{{ data.checkins }}</el-card></el-col>
        </el-row>
        <div class="charts">
          <el-card class="chart-card">
            <div class="chart-title">报名与签到对比</div>
            <v-chart :option="chartOption" autoresize style="height: 300px" />
          </el-card>
          <el-card class="chart-card">
            <div class="chart-title">发布占比</div>
            <v-chart :option="chartOption2" autoresize style="height: 300px" />
          </el-card>
        </div>
      </div>
      <el-empty v-else description="暂无数据" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import request from '../utils/request'
import VChart from 'vue-echarts'

const orgId = ref('')
const dateRange = ref([])
const data = ref(null)
const orgs = ref([])

const load = async () => {
  const params = {}
  if (orgId.value) params.orgId = orgId.value
  if (dateRange.value && dateRange.value.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  data.value = await request.get('/events/stats/overview', { params })
}

const loadOrgs = async () => {
  const resp = await request.get('/events/stats/orgs')
  orgs.value = resp || []
}

const reset = () => {
  orgId.value = ''
  dateRange.value = []
  load()
}

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 24, right: 24, top: 24, bottom: 24, containLabel: true },
  xAxis: {
    type: 'category',
    data: ['报名', '签到'],
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
      data: data.value ? [data.value.registrations || 0, data.value.checkins || 0] : [0, 0],
      type: 'bar',
      barWidth: 36,
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

const chartOption2 = computed(() => {
  const total = data.value ? data.value.events || 0 : 0
  const published = data.value ? data.value.published || 0 : 0
  const other = Math.max(total - published, 0)
  return {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#cbd2ff' } },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '45%'],
        label: { formatter: '{b}: {d}%', color: '#cbd2ff' },
        data: [
          { value: published, name: '已发布', itemStyle: { color: '#52c41a' } },
          { value: other, name: '未发布', itemStyle: { color: '#faad14' } },
        ],
      },
    ],
  }
})

onMounted(async () => {
  await loadOrgs()
  await load()
})
</script>

<style scoped>
.page {
  max-width: 1200px;
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
.stat-card {
  border: 1px solid rgba(79, 214, 255, 0.16);
  background: linear-gradient(135deg, rgba(79, 214, 255, 0.08), rgba(138, 91, 255, 0.08));
  font-weight: 600;
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
