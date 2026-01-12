<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="page">
    <div class="card glass">
      <div class="card-header">
        <div>
          <p class="eyebrow">创建账号</p>
          <h2>注册校园活动平台</h2>
          <p class="sub">注册后默认学生身份，可在登录后完善信息</p>
        </div>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="可选" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="可选" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" block @click="onRegister">注册并登录</el-button>
        </el-form-item>
        <el-button link type="primary" @click="goLogin">已有账号，去登录</el-button>
        <el-alert v-if="hint" :title="hint" type="info" show-icon class="hint" />
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { setName, setRoles, setToken } from '../utils/auth'
import { useAuthStore } from '../store/auth'

const formRef = ref()
const form = reactive({
  username: '',
  name: '',
  password: '',
  email: '',
  phone: '',
})
const hint = ref('')
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur', min: 6 }],
  email: [
    {
      type: 'email',
      message: '请输入正确的邮箱格式',
      trigger: 'blur',
    },
  ],
}

const getDefaultPath = (roles) => {
  if (roles.includes('admin')) return '/admin/approvals'
  if (roles.includes('organizer')) return '/manage/events'
  return '/'
}

const onRegister = async () => {
  const valid = await formRef.value?.validate?.()
  if (!valid) return
  hint.value = ''
  try {
    const payload = {
      username: form.username,
      name: form.name,
      password: form.password,
      email: form.email || null,
      phone: form.phone || null,
    }
    const data = await request.post('/auth/register', payload)
    const roles = (data.roles || '').split(',').filter(Boolean)
    setToken(data.token)
    setRoles(roles)
    if (data.name) setName(data.name)
    authStore.setAuth({ token: data.token, roles, name: data.name })
    ElMessage.success('注册成功')
    const redirect = route.query.redirect
    const target = redirect ? redirect : getDefaultPath(roles)
    router.push(target)
  } catch (e) {
    // error handled by interceptor
  }
}

const goLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.page {
  min-height: 70vh;
  display: grid;
  place-items: center;
}
.card {
  width: 520px;
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
.hint {
  margin-top: 10px;
}
</style>
