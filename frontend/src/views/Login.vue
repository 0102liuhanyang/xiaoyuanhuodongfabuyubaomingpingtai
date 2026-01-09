<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="page">
    <div class="card glass">
      <div class="card-header">
        <div>
          <p class="eyebrow">欢迎回来</p>
          <h2>登录校园活动平台</h2>
          <p class="sub">使用演示账号：admin / 123456 或 stu1 / 123456</p>
        </div>
      </div>
      <el-form :model="form" label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" block @click="onLogin">登录</el-button>
        </el-form-item>
        <el-alert v-if="hint" :title="hint" type="info" show-icon />
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const form = reactive({ username: '', password: '' })
const hint = ref('')
const router = useRouter()

const onLogin = () => {
  if (!form.username || !form.password) {
    hint.value = '请输入用户名和密码（示例：admin/123456 或 stu1/123456）'
    return
  }
  hint.value = '登录成功，跳转中...'
  setTimeout(() => router.push('/'), 500)
}
</script>

<style scoped>
.page {
  min-height: 70vh;
  display: grid;
  place-items: center;
}
.card {
  width: 480px;
  padding: 28px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}
.glass {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.12), rgba(255, 255, 255, 0.06));
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(10px);
}
.card-header {
  margin-bottom: 16px;
}
.eyebrow {
  letter-spacing: 1px;
  font-size: 13px;
  color: #d7c8ff;
  margin: 0 0 4px;
}
h2 {
  margin: 0 0 6px;
  color: #f7f5fb;
}
.sub {
  margin: 0;
  color: #d7d0e8;
  font-size: 13px;
}
</style>
