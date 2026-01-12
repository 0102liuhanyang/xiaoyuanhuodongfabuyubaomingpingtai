<template>
  <div class="page">
    <el-card class="card">
      <div class="header">
        <div>
          <h3>我的报名</h3>
          <p class="sub">查看报名状态与签到时间</p>
        </div>
        <el-button type="primary" @click="load">刷新</el-button>
      </div>
      <el-form :inline="true" :model="filters" class="filters" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="已报名" value="registered" />
            <el-option label="候补中" value="waitlisted" />
            <el-option label="已取消" value="canceled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" style="width: 100%; margin-top: 12px">
        <el-table-column prop="title" label="活动" />
        <el-table-column prop="startTime" label="开始时间" />
        <el-table-column prop="location" label="地点" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">
              {{ statusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkinAt" label="签到时间" width="160" />
        <el-table-column label="操作" width="140">
          <template #default="scope">
            <el-button link type="primary" @click="goDetail(scope.row.id)">详情</el-button>
            <el-button link type="danger" :disabled="scope.row.status === 'canceled'" @click="onCancel(scope.row.id)">
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && list.length === 0" description="暂无报名记录" />
      <el-pagination
        layout="prev, pager, next"
        :current-page="page"
        :page-size="size"
        :total="total"
        @current-change="onPage"
        style="margin-top: 16px; text-align: right"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import request from '../utils/request'

const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const router = useRouter()
const filters = reactive({
  status: '',
})

const load = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      status: filters.status,
    }
    const data = await request.get('/registrations/my', { params })
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => router.push(`/events/${id}`)

const onCancel = async (id) => {
  await ElMessageBox.confirm('确定取消报名？', '取消报名', { type: 'warning' })
  await request.delete(`/events/${id}/registrations/my`)
  ElMessage.success('已取消')
  load()
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

const onSearch = () => {
  page.value = 1
  load()
}

const reset = () => {
  filters.status = ''
  page.value = 1
  load()
}

const onPage = (p) => {
  page.value = p
  load()
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
