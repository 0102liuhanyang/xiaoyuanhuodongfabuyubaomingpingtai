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
          <el-col :span="6"><el-card shadow="hover">活动总数：{{ data.events }}</el-card></el-col>
          <el-col :span="6"><el-card shadow="hover">发布：{{ data.published }}</el-card></el-col>
          <el-col :span="6"><el-card shadow="hover">报名总数：{{ data.registrations }}</el-card></el-col>
          <el-col :span="6"><el-card shadow="hover">签到总数：{{ data.checkins }}</el-card></el-col>
        </el-row>
        <v-chart :option="chartOption" autoresize style="height: 320px; margin-top: 16px" />
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
  xAxis: { type: 'category', data: ['报名', '签到'] },
  yAxis: { type: 'value' },
  series: [
    {
      data: data.value ? [data.value.registrations || 0, data.value.checkins || 0] : [0, 0],
      type: 'bar',
      itemStyle: { color: '#6f42c1' },
    },
  ],
}))

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
</style>
