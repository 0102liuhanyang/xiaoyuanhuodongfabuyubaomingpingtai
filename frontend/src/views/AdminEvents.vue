<template>
  <div class="page">
    <el-card class="card">
      <div class="header">
        <div>
          <h3>活动监管</h3>
          <p class="sub">管理员查看并处理全部活动，支持下架、归档与生成签到码</p>
        </div>
        <el-button type="primary" @click="load">刷新</el-button>
      </div>
      <el-form :inline="true" :model="filters" class="filters" @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="filters.keyword" placeholder="标题关键字" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filters.category" placeholder="全部" clearable style="width: 140px">
            <el-option label="演出" value="演出" />
            <el-option label="培训" value="培训" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="草稿" value="draft" />
            <el-option label="待审核" value="pending_review" />
            <el-option label="已发布" value="published" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="已下架" value="canceled" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.timeRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="records" style="width: 100%; margin-top: 12px">
        <el-table-column prop="title" label="标题" min-width="180">
          <template #default="scope">
            <el-button link type="primary" @click="openDetail(scope.row.id)">{{ scope.row.title }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="location" label="地点" min-width="140" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="scope">
            <el-button link type="primary" v-if="scope.row.status === 'pending_review'" @click="goApproval">
              去审核
            </el-button>
            <el-button link type="warning" v-if="canArchive(scope.row)" @click="archive(scope.row.id)">归档</el-button>
            <el-button link type="danger" v-if="canCancel(scope.row)" @click="cancel(scope.row.id)">下架</el-button>
            <el-button link type="primary" @click="genCheckin(scope.row.id)">签到码</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && records.length === 0" description="暂无活动" />
      <el-pagination
        layout="prev, pager, next"
        :current-page="page"
        :page-size="size"
        :total="total"
        @current-change="onPage"
        style="margin-top: 16px; text-align: right"
      />
    </el-card>
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
    <el-drawer v-model="showDetail" title="活动详情" size="45%">
      <div v-if="detail">
        <h3 class="detail-title">{{ detail.title }}</h3>
        <p class="detail-desc">{{ detail.description }}</p>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="分类">{{ detail.category || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ detail.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ detail.startTime }} - {{ detail.endTime }}</el-descriptions-item>
          <el-descriptions-item label="报名时间">{{ detail.signupStartTime }} - {{ detail.signupEndTime }}</el-descriptions-item>
          <el-descriptions-item label="容量">{{ detail.capacity }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusLabel(detail.status) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import QRCode from 'qrcode'

const router = useRouter()
const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const showCheckinDialog = ref(false)
const checkinInfo = ref({ code: '', validUntil: '' })
const qrDataUrl = ref('')
const showDetail = ref(false)
const detail = ref(null)
const filters = reactive({
  keyword: '',
  category: '',
  status: '',
  timeRange: [],
})

const load = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      keyword: filters.keyword,
      category: filters.category,
      status: filters.status || null,
      start: filters.timeRange?.[0],
      end: filters.timeRange?.[1],
    }
    const data = await request.get('/events/admin/all', { params })
    records.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const reset = () => {
  filters.keyword = ''
  filters.category = ''
  filters.status = ''
  filters.timeRange = []
  page.value = 1
  load()
}

const onPage = (p) => {
  page.value = p
  load()
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

const statusType = (status) => {
  if (status === 'published') return 'success'
  if (status === 'pending_review') return 'warning'
  if (status === 'rejected') return 'danger'
  if (status === 'draft') return 'info'
  if (status === 'canceled') return 'info'
  if (status === 'archived') return 'info'
  return 'info'
}

const canArchive = (row) => row.status === 'published'
const canCancel = (row) => ['published', 'pending_review', 'draft', 'rejected'].includes(row.status)

const goApproval = () => {
  router.push('/admin/approvals')
}

const cancel = async (id) => {
  await ElMessageBox.confirm('确定下架该活动？', '下架活动', { type: 'warning' })
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

const openDetail = async (id) => {
  const data = await request.get(`/events/${id}`)
  detail.value = data?.event || data
  showDetail.value = true
}

onMounted(load)
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
.detail-title {
  margin: 0 0 8px;
}
.detail-desc {
  margin: 0 0 12px;
  color: var(--tech-muted);
}
</style>



