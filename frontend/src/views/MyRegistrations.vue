<template>
  <div class="page">
    <el-card class="card">
      <h3>我的报名</h3>
      <el-table :data="list" style="width: 100%; margin-top: 12px">
        <el-table-column prop="title" label="活动" />
        <el-table-column prop="startTime" label="开始时间" />
        <el-table-column prop="location" label="地点" />
        <el-table-column label="操作" width="140">
          <template #default="scope">
            <el-button link type="primary" @click="goDetail(scope.row.id)">详情</el-button>
            <el-button link type="danger" @click="onCancel(scope.row.id)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchMyRegistrations, cancelRegistration } from '../mock/api'

const list = ref([])
const router = useRouter()

const load = async () => {
  const { data } = await fetchMyRegistrations()
  list.value = data
}

const goDetail = (id) => router.push(`/events/${id}`)

const onCancel = async (id) => {
  await cancelRegistration(id)
  load()
}

onMounted(load)
</script>

<style scoped>
.page {
  max-width: 900px;
  margin: 0 auto;
}
.card {
  border: none;
  background: rgba(255, 255, 255, 0.95);
}
</style>
