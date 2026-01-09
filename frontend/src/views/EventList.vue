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
        <el-form-item>
          <el-button type="primary" @click="loadEvents">搜索</el-button>
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
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchEvents } from '../mock/api'

const events = ref([])
const filters = reactive({ keyword: '', category: '' })
const router = useRouter()

const loadEvents = async () => {
  const { data } = await fetchEvents()
  events.value = data.filter((e) => {
    const matchKeyword =
      !filters.keyword ||
      e.title.includes(filters.keyword) ||
      (e.tags && e.tags.join(',').includes(filters.keyword))
    const matchCategory = !filters.category || e.category === filters.category
    return matchKeyword && matchCategory
  })
}

const goDetail = (id) => {
  router.push(`/events/${id}`)
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
</style>
