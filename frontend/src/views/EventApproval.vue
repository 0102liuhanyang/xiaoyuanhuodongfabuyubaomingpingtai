<template>
  <div class="page">
    <el-card class="card">
      <div class="header">
        <div>
          <h3>待审核活动</h3>
          <p class="sub">审核组织者提交的活动，支持通过/驳回并通知</p>
        </div>
        <el-button type="primary" @click="load">刷新</el-button>
      </div>
      <el-form :inline="true" :model="filters" class="filters" @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="filters.keyword" placeholder="标题关键字" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filters.category" placeholder="全部" clearable style="width: 160px">
            <el-option label="演出" value="演出" />
            <el-option label="培训" value="培训" />
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
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="location" label="地点" min-width="140" />
        <el-table-column prop="creatorId" label="创建者" width="100" />
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="success" @click="approve(scope.row.id)">通过</el-button>
            <el-button size="small" type="danger" @click="reject(scope.row.id)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && records.length === 0" description="暂无待审活动" />
      <el-pagination
        layout="prev, pager, next"
        :current-page="page"
        :page-size="size"
        :total="total"
        @current-change="onPage"
        style="margin-top: 16px; text-align: right"
      />
    </el-card>
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
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const showDetail = ref(false)
const detail = ref(null)
const filters = reactive({
  keyword: '',
  category: '',
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
      start: filters.timeRange?.[0],
      end: filters.timeRange?.[1],
    }
    const data = await request.get('/events/admin/pending', { params })
    records.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const onPage = (p) => {
  page.value = p
  load()
}

const approve = async (id) => {
  await ElMessageBox.confirm('确定审核通过该活动？', '审核通过', { type: 'success' })
  await request.post(`/events/admin/${id}/approve`)
  ElMessage.success('已通过')
  load()
}

const reject = async (id) => {
  const { value } = await ElMessageBox.prompt('请输入驳回原因（可选）', '驳回活动', {
    confirmButtonText: '驳回',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：时间冲突/信息不完整',
    inputPattern: /^.{0,200}$/,
    inputErrorMessage: '原因不超过 200 字',
  })
  await request.post(`/events/admin/${id}/reject`, null, { params: { reason: value || '' } })
  ElMessage.success('已驳回')
  load()
}

const reset = () => {
  filters.keyword = ''
  filters.category = ''
  filters.timeRange = []
  page.value = 1
  load()
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
.detail-title {
  margin: 0 0 8px;
}
.detail-desc {
  margin: 0 0 12px;
  color: #666;
}
</style>
