<template>
  <div class="page" v-if="event">
    <el-card class="card">
      <div class="header">
        <div>
          <h2>{{ event.title }}</h2>
          <el-tag>{{ event.category }}</el-tag>
        </div>
        <div>
          <el-button type="primary" @click="onRegister" :disabled="registered">报名</el-button>
          <el-button v-if="registered" type="danger" @click="onCancel">取消报名</el-button>
        </div>
      </div>
      <p class="desc">{{ event.description }}</p>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="地点">{{ event.location }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ event.startTime }} - {{ event.endTime }}</el-descriptions-item>
        <el-descriptions-item label="容量">{{ event.capacity }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ event.status }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchEvent, registerEvent, cancelRegistration } from '../mock/api'

const route = useRoute()
const event = ref(null)
const registered = ref(false)

const load = async () => {
  const { data } = await fetchEvent(route.params.id)
  event.value = data
}

const onRegister = async () => {
  await registerEvent(event.value.id)
  registered.value = true
}

const onCancel = async () => {
  await cancelRegistration(event.value.id)
  registered.value = false
}

onMounted(load)
</script>

<style scoped>
.page {
  max-width: 900px;
  margin: 0 auto;
}
.card {
  background: rgba(255, 255, 255, 0.96);
  border: none;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.desc {
  margin: 12px 0;
  color: #555;
}
</style>
