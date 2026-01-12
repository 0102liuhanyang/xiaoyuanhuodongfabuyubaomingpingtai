<template>
  <div class="page">
    <el-card class="card">
      <div class="header">
        <div>
          <h3>签到管理</h3>
          <p class="sub">为活动生成签到码并查看签到情况</p>
        </div>
        <el-button type="primary" @click="refresh">刷新</el-button>
      </div>
      <el-form :inline="true" :model="filters" class="filters" @submit.prevent>
        <el-form-item label="活动">
          <el-select v-model="filters.eventId" placeholder="选择活动" style="width: 240px" @change="onEventChange">
            <el-option v-for="item in events" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="签到状态">
          <el-select v-model="filters.checkin" placeholder="全部" clearable style="width: 140px">
            <el-option label="已签到" value="checked" />
            <el-option label="未签到" value="unchecked" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="!filters.eventId" @click="genCheckin">生成签到码</el-button>
        </el-form-item>
      </el-form>
      <div class="code-card" v-if="filters.eventId">
        <div class="code-info">
          <div class="code-line">
            <span class="label">签到码</span>
            <span class="value">{{ checkinInfo.code || '-' }}</span>
          </div>
          <div class="code-line">
            <span class="label">有效期</span>
            <span class="value">{{ checkinInfo.validUntil || '-' }}</span>
          </div>
        </div>
        <div v-if="qrDataUrl" class="qr">
          <el-image :src="qrDataUrl" style="width: 140px; height: 140px" fit="cover" />
        </div>
      </div>
      <el-table :data="filteredRecords" style="width: 100%; margin-top: 12px">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="status" label="报名状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'waitlisted' ? 'warning' : 'success'">
              {{ scope.row.status === 'waitlisted' ? '候补中' : '已报名' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkinAt" label="签到时间" width="180" />
      </el-table>
      <el-empty v-if="!loading && filteredRecords.length === 0" description="暂无签到记录" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import QRCode from 'qrcode'

const events = ref([])
const records = ref([])
const loading = ref(false)
const qrDataUrl = ref('')
const checkinInfo = ref({ code: '', validUntil: '' })
const filters = reactive({
  eventId: '',
  checkin: '',
})

const loadEvents = async () => {
  const data = await request.get('/events/mine', { params: { page: 1, size: 200 } })
  events.value = data.records || []
  if (!filters.eventId && events.value.length) {
    filters.eventId = events.value[0].id
  }
}

const loadRegistrations = async () => {
  if (!filters.eventId) {
    records.value = []
    return
  }
  loading.value = true
  try {
    const data = await request.get(`/events/${filters.eventId}/registrations`, { params: { page: 1, size: 200 } })
    records.value = data.records || []
  } finally {
    loading.value = false
  }
}

const loadEventDetail = async () => {
  if (!filters.eventId) return
  const data = await request.get(`/events/${filters.eventId}`)
  const event = data?.event || data
  checkinInfo.value = { code: event?.checkinCode || '', validUntil: event?.checkinValidUntil || '' }
  qrDataUrl.value = event?.checkinCode ? await QRCode.toDataURL(event.checkinCode) : ''
}

const genCheckin = async () => {
  if (!filters.eventId) return
  const resp = await request.post(`/events/${filters.eventId}/checkin-code`)
  checkinInfo.value = resp
  qrDataUrl.value = await QRCode.toDataURL(resp.qrText || resp.code || '')
  ElMessage.success('签到码已生成')
}

const onEventChange = async () => {
  await loadEventDetail()
  await loadRegistrations()
}

const refresh = async () => {
  await loadEventDetail()
  await loadRegistrations()
}

const filteredRecords = computed(() => {
  if (!filters.checkin) return records.value
  return records.value.filter((r) => (filters.checkin === 'checked' ? r.checkinAt : !r.checkinAt))
})

onMounted(async () => {
  await loadEvents()
  await onEventChange()
})
</script>

<style scoped>
.page {
  max-width: 1300px;
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
.filters {
  margin-top: 12px;
}
.code-card {
  margin-top: 12px;
  padding: 12px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.02);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.code-info {
  display: grid;
  gap: 6px;
}
.code-line {
  display: flex;
  gap: 12px;
  align-items: center;
}
.label {
  color: #666;
  font-size: 13px;
}
.value {
  font-weight: 600;
}
</style>
