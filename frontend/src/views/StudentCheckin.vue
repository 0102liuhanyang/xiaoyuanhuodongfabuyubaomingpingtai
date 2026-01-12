<template>
  <div class="page">
    <el-card class="card">
      <div class="header">
        <div>
          <h3>签到入口</h3>
          <p class="sub">选择已报名活动并输入签到码完成签到</p>
        </div>
        <el-button type="primary" @click="refresh">刷新</el-button>
      </div>
      <el-form :model="form" label-position="top" class="form">
        <el-form-item label="活动">
          <el-select v-model="form.eventId" placeholder="选择已报名活动" @change="onEventChange">
            <el-option
              v-for="item in events"
              :key="item.id"
              :label="`${item.title}（${item.statusLabel}）`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="签到码">
          <el-input v-model="form.code" placeholder="请输入签到码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="!form.eventId || !form.code" @click="onCheckin">签到</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="events" style="width: 100%; margin-top: 12px">
        <el-table-column prop="title" label="活动" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="location" label="地点" />
        <el-table-column label="报名状态" width="120">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ scope.row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkinAt" label="签到时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button link type="primary" @click="selectEvent(scope.row)">签到</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && events.length === 0" description="暂无可签到活动" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const events = ref([])
const loading = ref(false)
const form = reactive({
  eventId: '',
  code: '',
})

const load = async () => {
  loading.value = true
  try {
    const data = await request.get('/registrations/my', { params: { page: 1, size: 50 } })
    const list = data.records || []
    events.value = list.map((item) => ({
      ...item,
      statusLabel: statusLabel(item.status),
    }))
  } finally {
    loading.value = false
  }
}

const onEventChange = () => {
  const item = events.value.find((e) => e.id === form.eventId)
  if (item && item.status === 'waitlisted') {
    ElMessage.warning('候补状态暂不可签到')
  }
}

const selectEvent = (row) => {
  form.eventId = row.id
}

const onCheckin = async () => {
  if (!form.eventId || !form.code) return
  await request.post(`/events/${form.eventId}/checkin`, null, { params: { code: form.code } })
  ElMessage.success('签到成功')
  form.code = ''
  await load()
}

const refresh = async () => {
  await load()
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

onMounted(load)
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
  margin-bottom: 12px;
}
.sub {
  margin: 6px 0 0;
  font-size: 12px;
  color: #7b7b7b;
}
.form {
  margin-top: 12px;
}
</style>
