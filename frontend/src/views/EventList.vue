<template>
  <div class="page">
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters" @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="filters.keyword" placeholder="活动标题/标签" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filters.category" placeholder="全部" clearable style="width: 160px">
            <el-option label="演出" value="演出" />
            <el-option label="培训" value="培训" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="filters.tags" placeholder="标签关键字" clearable />
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
        <el-form-item label="排序">
          <el-select v-model="filters.sortBy" placeholder="排序" style="width: 140px">
            <el-option label="最新发布" value="created" />
            <el-option label="按开始时间" value="startTime" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadEvents">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" style="margin-top: 12px">
      <el-col v-for="item in events" :key="item.id" :span="12">
        <el-card class="event-card" shadow="hover" @click="goDetail(item.id)">
          <div class="event-header">
            <h3>{{ item.title }}</h3>
            <el-tag type="success">{{ item.category || '通用' }}</el-tag>
          </div>
          <p class="desc">{{ item.description }}</p>
          <div class="meta">
            <span>地点：{{ item.location }}</span>
            <span>时间：{{ item.startTime }}</span>
            <span>容量：{{ item.capacity }}</span>
          </div>
          <div class="meta tags" v-if="item.tags">
            <el-tag v-for="tag in splitTags(item.tags)" :key="tag" size="small" type="info">
              {{ tag }}
            </el-tag>
          </div>
          <div class="status">
            <el-tag :type="statusType(item)">{{ statusLabel(item) }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && events.length === 0" description="暂无活动" />
    <el-pagination
      layout="prev, pager, next"
      :current-page="filters.page"
      :page-size="filters.size"
      :total="total"
      @current-change="onPage"
      style="margin-top: 16px; text-align: center"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const events = ref([])
const total = ref(0)
const loading = ref(false)
const filters = reactive({
  keyword: '',
  category: '',
  tags: '',
  timeRange: [],
  sortBy: 'created',
  page: 1,
  size: 10,
})
const router = useRouter()

const loadEvents = async () => {
  loading.value = true
  try {
    const params = {
      keyword: filters.keyword,
      category: filters.category,
      tags: filters.tags,
      start: filters.timeRange?.[0],
      end: filters.timeRange?.[1],
      page: filters.page,
      size: filters.size,
      sortBy: filters.sortBy === 'startTime' ? 'startTime' : null,
      sortOrder: filters.sortBy === 'startTime' ? 'asc' : null,
    }
    const data = await request.get('/events', { params })
    events.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const onPage = (p) => {
  filters.page = p
  loadEvents()
}

const goDetail = (id) => {
  router.push(`/events/${id}`)
}

const resetFilters = () => {
  Object.assign(filters, {
    keyword: '',
    category: '',
    tags: '',
    timeRange: [],
    sortBy: 'created',
    page: 1,
    size: 10,
  })
  loadEvents()
}

const splitTags = (tags) => tags.split(',').map((t) => t.trim()).filter(Boolean)

const statusLabel = (item) => {
  if (item.status && item.status !== 'published') return '未开放'
  if (item.signupStartTime && new Date() < new Date(item.signupStartTime)) return '未开始'
  if (item.signupEndTime && new Date() > new Date(item.signupEndTime)) return '已结束'
  return '报名中'
}

const statusType = (item) => {
  const label = statusLabel(item)
  if (label === '报名中') return 'success'
  if (label === '未开始') return 'info'
  if (label === '已结束') return 'warning'
  return 'info'
}

onMounted(loadEvents)
</script>

<style scoped>
.page {
  max-width: 1000px;
  margin: 0 auto;
}
.filter-card {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 12px;
  border: none;
}
.event-card {
  margin-bottom: 16px;
  cursor: pointer;
  border: none;
  background: rgba(255, 255, 255, 0.95);
}
.event-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.desc {
  color: #666;
  margin: 8px 0;
}
.meta {
  font-size: 13px;
  color: #555;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.tags {
  margin-top: 8px;
}
.status {
  margin-top: 10px;
}
</style>
