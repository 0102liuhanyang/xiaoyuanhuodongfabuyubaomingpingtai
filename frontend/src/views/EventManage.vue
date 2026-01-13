<template>
  <div class="page">
    <el-card class="card">
      <div class="header">
        <div>
          <h3>活动管理</h3>
          <p class="sub">管理自己创建的活动，支持新建、编辑、提交审核、归档与导出名单</p>
        </div>
        <el-button type="primary" @click="newEvent">新建活动</el-button>
      </div>
      <div class="table-wrap">
        <el-table :data="events" style="width: 100%; margin-top: 12px">
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="location" label="地点" min-width="140" />
        <el-table-column prop="signupStartTime" label="报名时间" width="220">
          <template #default="scope">
            <div>{{ scope.row.signupStartTime || '-' }} -</div>
            <div>{{ scope.row.signupEndTime || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="edit(scope.row)" :disabled="!canEdit(scope.row)">编辑</el-button>
            <el-button link type="success" v-if="showSubmit(scope.row)" @click="submit(scope.row.id)">提交审核</el-button>
            <el-button link type="warning" v-if="canArchive(scope.row)" @click="archive(scope.row.id)">归档</el-button>
            <el-button link type="danger" v-if="canCancel(scope.row)" @click="cancel(scope.row.id)">下架</el-button>
            <el-button link type="primary" @click="openStats(scope.row.id)">统计</el-button>
            <el-button link type="primary" @click="exportRegistrations(scope.row.id)">导出报名</el-button>
            <el-button link type="primary" @click="genCheckin(scope.row.id)">签到码</el-button>
          </template>
        </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-drawer v-model="showEditor" title="活动编辑" size="50%" @close="closeEditor">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" placeholder="选择分类">
            <el-option label="演出" value="演出" />
            <el-option label="培训" value="培训" />
            <el-option label="讲座" value="讲座" />
            <el-option label="社团" value="社团" />
            <el-option label="公益" value="公益" />
            <el-option label="竞赛" value="竞赛" />
            <el-option label="招新" value="招新" />
            <el-option label="展览" value="展览" />
            <el-option label="沙龙" value="沙龙" />
            <el-option label="志愿" value="志愿" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="报名时间">
          <el-date-picker
            v-model="form.signupRange"
            type="datetimerange"
            start-placeholder="开始报名"
            end-placeholder="截止报名"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="form.capacity" :min="1" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveEvent">保存</el-button>
          <el-button @click="closeEditor">取消</el-button>
        </el-form-item>
      </el-form>
    </el-drawer>

    <el-dialog v-model="showCheckinDialog" title="签到码">
      <div v-if="checkinInfo.code">
        <p>签到码：<strong>{{ checkinInfo.code }}</strong></p>
        <p>有效期至：{{ checkinInfo.validUntil }}</p>
        <el-image v-if="qrDataUrl" :src="qrDataUrl" style="width: 180px; height: 180px" fit="cover" />
      </div>
      <template #footer>
        <el-button @click="showCheckinDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import QRCode from 'qrcode'

const events = ref([])
const formRef = ref()
const showEditor = ref(false)
const showCheckinDialog = ref(false)
const checkinInfo = ref({ code: '', validUntil: '' })
const qrDataUrl = ref('')
const route = useRoute()
const router = useRouter()
const form = reactive({
  id: null,
  title: '',
  category: '',
  tags: '',
  location: '',
  signupRange: [],
  timeRange: [],
  capacity: 50,
  description: '',
})
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
  timeRange: [
    {
      validator: (_rule, value, callback) => {
        if (!value || value.length !== 2) {
          callback(new Error('请选择开始和结束时间'))
          return
        }
        if (new Date(value[0]) >= new Date(value[1])) {
          callback(new Error('开始时间必须早于结束时间'))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
  signupRange: [
    {
      validator: (_rule, value, callback) => {
        if (value && value.length === 2 && form.timeRange && form.timeRange.length === 2) {
          if (new Date(value[1]) > new Date(form.timeRange[0])) {
            callback(new Error('报名截止必须早于活动开始'))
            return
          }
        }
        callback()
      },
      trigger: 'change',
    },
  ],
  capacity: [{ type: 'number', required: true, message: '请输入容量', trigger: 'blur', min: 1 }],
}

const load = async () => {
  const data = await request.get('/events/mine', { params: { page: 1, size: 100 } })
  events.value = data.records || []
}

const statusType = (status) => {
  if (status === 'published') return 'success'
  if (status === 'pending_review') return 'warning'
  if (status === 'rejected') return 'danger'
  if (status === 'draft') return 'info'
  if (status === 'canceled') return 'info'
  if (status === 'archived') return 'info'
  return 'info'
}

const statusLabel = (status) => {
  if (status === 'published') return '已发布'
  if (status === 'pending_review') return '待审核'
  if (status === 'rejected') return '已驳回'
  if (status === 'draft') return '草稿'
  if (status === 'canceled') return '已下架'
  if (status === 'archived') return '已归档'
  return status || '未知'
}

const canEdit = (row) => !['archived'].includes(row.status)
const canArchive = (row) => ['published'].includes(row.status)
const canCancel = (row) => ['published', 'pending_review', 'draft', 'rejected'].includes(row.status)

const showSubmit = (row) => row.status === 'draft' || row.status === 'rejected'

const openNewEditor = () => {
  Object.assign(form, {
    id: null,
    title: '',
    category: '',
    tags: '',
    location: '',
    signupRange: [],
    timeRange: [],
    capacity: 50,
    description: '',
  })
  showEditor.value = true
}

const openEditEditor = (row) => {
  Object.assign(form, {
    id: row.id,
    title: row.title,
    category: row.category,
    tags: row.tags,
    location: row.location,
    signupRange: [row.signupStartTime, row.signupEndTime],
    timeRange: [row.startTime, row.endTime],
    capacity: row.capacity,
    description: row.description,
  })
  showEditor.value = true
}

const openEditorById = async (id) => {
  const data = await request.get(`/events/${id}`)
  const event = data?.event || data
  if (!event) return
  openEditEditor({
    id: event.id,
    title: event.title,
    category: event.category,
    tags: event.tags,
    location: event.location,
    signupStartTime: event.signupStartTime,
    signupEndTime: event.signupEndTime,
    startTime: event.startTime,
    endTime: event.endTime,
    capacity: event.capacity,
    description: event.description,
  })
}

const newEvent = () => {
  router.push('/manage/events/new')
}

const edit = (row) => {
  router.push(`/manage/events/${row.id}/edit`)
}

const closeEditor = () => {
  showEditor.value = false
  if (route.path !== '/manage/events') {
    router.push('/manage/events')
  }
}

const saveEvent = async () => {
  const valid = await formRef.value?.validate?.()
  if (!valid) return
  const payload = {
    title: form.title,
    category: form.category,
    tags: form.tags,
    location: form.location,
    signupStartTime: form.signupRange?.[0],
    signupEndTime: form.signupRange?.[1],
    startTime: form.timeRange?.[0],
    endTime: form.timeRange?.[1],
    capacity: form.capacity,
    description: form.description,
  }
  if (form.id) {
    await request.put(`/events/${form.id}`, payload)
    ElMessage.success('已保存')
  } else {
    await request.post('/events', payload)
    ElMessage.success('已创建草稿')
  }
  closeEditor()
  load()
}

const submit = async (id) => {
  await ElMessageBox.confirm('提交后将进入审核流程，是否继续？', '提交审核', { type: 'warning' })
  await request.post(`/events/${id}/submit`)
  ElMessage.success('已提交审核')
  load()
}

const cancel = async (id) => {
  await ElMessageBox.confirm('确定下架该活动？报名者将收到通知。', '下架活动', { type: 'warning' })
  await request.delete(`/events/${id}`)
  ElMessage.success('已下架')
  load()
}

const archive = async (id) => {
  await ElMessageBox.confirm('归档后活动不可再报名，是否继续？', '归档活动', { type: 'warning' })
  await request.post(`/events/${id}/archive`)
  ElMessage.success('已归档')
  load()
}

const genCheckin = async (id) => {
  const resp = await request.post(`/events/${id}/checkin-code`)
  checkinInfo.value = resp
  qrDataUrl.value = await QRCode.toDataURL(resp.qrText || resp.code || '')
  showCheckinDialog.value = true
}

const exportRegistrations = async (id) => {
  const blob = await request.get(`/events/${id}/registrations/export`, { responseType: 'blob' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `registrations-${id}.csv`
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

const openStats = (id) => {
  router.push(`/events/${id}/stats`)
}

load()

watch(
  () => [route.name, route.params.id],
  async ([name, id]) => {
    if (name === 'manageEventsNew') {
      openNewEditor()
      return
    }
    if (name === 'manageEventsEdit' && id) {
      await openEditorById(id)
      return
    }
    showEditor.value = false
  },
  { immediate: true }
)
</script>

<style scoped>
.page {
  max-width: 1500px;
  width: 100%;
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
.table-wrap {
  overflow-x: auto;
}
</style>



