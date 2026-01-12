<template>
  <div class="page">
    <el-card class="card">
      <div class="header">
        <div>
          <h3>黑名单管理</h3>
          <p class="sub">限制恶意报名用户，影响报名资格</p>
        </div>
        <el-button type="primary" @click="load">刷新</el-button>
      </div>
      <el-form :model="form" label-width="80px" class="form">
        <el-form-item label="用户ID">
          <el-input v-model="form.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="form.reason" placeholder="可选" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="add">添加</el-button>
          <el-button type="danger" @click="remove">移除</el-button>
        </el-form-item>
      </el-form>
      <el-form :inline="true" :model="filters" class="filters" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="原因关键字" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="records" style="width: 100%; margin-top: 12px">
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="reason" label="原因" min-width="200" />
        <el-table-column prop="createdAt" label="加入时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button link type="danger" @click="remove(scope.row.userId)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && records.length === 0" description="暂无黑名单记录" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const form = reactive({
  userId: '',
  reason: '',
})
const filters = reactive({
  keyword: '',
})
const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

const add = async () => {
  if (!form.userId) {
    ElMessage.warning('请输入用户ID')
    return
  }
  await request.post('/admin/registrations/blacklist', null, {
    params: { userId: form.userId, reason: form.reason || undefined },
  })
  ElMessage.success('已加入黑名单')
  form.userId = ''
  form.reason = ''
  load()
}

const remove = async (userId) => {
  if (!userId) {
    ElMessage.warning('请输入用户ID')
    return
  }
  await ElMessageBox.confirm('确定移除该用户？', '移除黑名单', { type: 'warning' })
  await request.delete(`/admin/registrations/blacklist/${userId}`)
  ElMessage.success('已移除黑名单')
  load()
}

const load = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, keyword: filters.keyword }
    const data = await request.get('/admin/registrations/blacklist', { params })
    if (!data) {
      records.value = []
      total.value = 0
      return
    }
    records.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    const status = error?.response?.status
    if (status === 401 || status === 403) {
      records.value = []
      total.value = 0
      return
    }
    throw error
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  page.value = 1
  load()
}

const reset = () => {
  filters.keyword = ''
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
  max-width: 1100px;
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
.form {
  margin-top: 16px;
}
.filters {
  margin-top: 16px;
}
</style>



