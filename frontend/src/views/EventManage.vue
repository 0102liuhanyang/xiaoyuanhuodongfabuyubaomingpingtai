<template>
  <div class="page">
    <el-card class="card">
      <div class="header">
        <h3>活动管理（示例）</h3>
        <el-button type="primary" @click="showEditor = true">新建活动</el-button>
      </div>
      <el-table :data="events" style="width: 100%; margin-top: 12px">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag type="success">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button link type="primary" @click="edit(scope.row)">编辑</el-button>
            <el-button link type="danger">下架</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="showEditor" title="活动编辑" size="50%">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" placeholder="选择分类">
            <el-option label="演出" value="演出" />
            <el-option label="培训" value="培训" />
          </el-select>
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm"
          />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="form.capacity" :min="1" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveEvent">保存（Mock）</el-button>
          <el-button @click="showEditor = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { fetchEvents } from '../mock/api'

const events = ref([])
const showEditor = ref(false)
const form = reactive({
  title: '',
  category: '',
  location: '',
  timeRange: [],
  capacity: 50,
  description: '',
})

const load = async () => {
  const { data } = await fetchEvents()
  events.value = data
}

const edit = (row) => {
  Object.assign(form, {
    title: row.title,
    category: row.category,
    location: row.location,
    timeRange: [row.startTime, row.endTime],
    capacity: row.capacity,
    description: row.description,
  })
  showEditor.value = true
}

const saveEvent = () => {
  showEditor.value = false
}

load()
</script>

<style scoped>
.page {
  max-width: 1100px;
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
</style>
