<template>
  <div class="page" v-if="event">
    <el-card class="card">
      <div class="header">
        <div>
          <h2>{{ event.title }}</h2>
          <el-tag>{{ event.category }}</el-tag>
        </div>
        <div>
          <el-tooltip v-if="registerDisabledReason" effect="dark" :content="registerDisabledReason" placement="top">
            <el-button type="primary" @click="onRegister" :disabled="myStatus || !canRegister">
              {{ registerLabel }}
            </el-button>
          </el-tooltip>
          <el-button v-else type="primary" @click="onRegister" :disabled="myStatus || !canRegister">
            {{ registerLabel }}
          </el-button>
          <el-button v-if="myStatus" type="danger" @click="onCancel">取消报名</el-button>
          <el-button v-if="isRegistered" @click="showCheckin = true">签到</el-button>
        </div>
      </div>
      <p class="desc">{{ event.description }}</p>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="地点">{{ event.location }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ event.startTime }} - {{ event.endTime }}</el-descriptions-item>
        <el-descriptions-item label="容量">{{ event.capacity }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ event.status }}</el-descriptions-item>
        <el-descriptions-item label="报名时间">
          <span v-if="event.signupStartTime">{{ event.signupStartTime }}</span> -
          <span v-if="event.signupEndTime">{{ event.signupEndTime }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <div class="status" v-if="myStatus">
        <el-tag :type="myStatus === 'waitlisted' ? 'warning' : 'success'">
          {{ myStatus === 'waitlisted' ? '候补中' : '已报名' }}
        </el-tag>
        <el-tag v-if="checkinAt" type="success">已签到 {{ checkinAt }}</el-tag>
      </div>
    </el-card>
    <el-dialog v-model="showCheckin" title="签到">
      <el-input v-model="checkinCode" placeholder="请输入签到码" />
      <template #footer>
        <el-button @click="showCheckin = false">取消</el-button>
        <el-button type="primary" @click="onCheckin">签到</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const route = useRoute()
const event = ref(null)
const myStatus = ref('') // registered | waitlisted | ''
const checkinAt = ref('')
const showCheckin = ref(false)
const checkinCode = ref('')
const canRegister = computed(() => {
  if (!event.value) return false
  if (event.value.status !== 'published') return false
  if (event.value.signupStartTime && new Date() < new Date(event.value.signupStartTime)) return false
  if (event.value.signupEndTime && new Date() > new Date(event.value.signupEndTime)) return false
  return true
})
const isRegistered = computed(() => myStatus.value === 'registered')
const registerLabel = computed(() => {
  if (!event.value) return '报名'
  if (event.value.status !== 'published') return '未开放'
  if (event.value.signupStartTime && new Date() < new Date(event.value.signupStartTime)) return '未开放'
  if (event.value.signupEndTime && new Date() > new Date(event.value.signupEndTime)) return '已结束'
  const regCount = event.value.registeredCount || 0
  if (event.value.capacity && regCount >= event.value.capacity) return '候补报名'
  return '报名'
})
const registerDisabledReason = computed(() => {
  if (!event.value) return ''
  if (event.value.status !== 'published') return '未开放'
  if (event.value.signupStartTime && new Date() < new Date(event.value.signupStartTime)) return '未到报名时间'
  if (event.value.signupEndTime && new Date() > new Date(event.value.signupEndTime)) return '报名已截止'
  return ''
})

const load = async () => {
  const data = await request.get(`/events/${route.params.id}`)
  if (data.event) {
    event.value = { ...data.event, registeredCount: data.registeredCount }
    myStatus.value = data.myStatus || ''
    checkinAt.value = data.checkinAt
  } else {
    event.value = data
  }
}

const onRegister = async () => {
  if (!canRegister.value) return
  const res = await request.post(`/events/${event.value.id}/registrations`)
  myStatus.value = res.status
  if (res.status === 'waitlisted') {
    ElMessage.warning('容量已满，您已进入候补队列')
  } else {
    ElMessage.success('报名成功')
  }
}

const onCancel = async () => {
  await request.delete(`/events/${event.value.id}/registrations/my`)
  myStatus.value = ''
  ElMessage.success('已取消报名')
}

const onCheckin = async () => {
  if (!checkinCode.value) return
  await request.post(`/events/${event.value.id}/checkin`, null, { params: { code: checkinCode.value } })
  ElMessage.success('签到成功')
  showCheckin.value = false
}

onMounted(load)
</script>

<style scoped>
.page {
  max-width: 900px;
  margin: 0 auto;
}
.card {
  background: var(--tech-card-bg);
  border: 1px solid var(--tech-border);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.desc {
  margin: 12px 0;
  color: var(--tech-muted);
}
</style>

