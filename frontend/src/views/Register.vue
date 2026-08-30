<template>
  <div class="auth-page">
    <AppHeader />
    <div class="auth-container">
      <h2>用户注册</h2>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
      >
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名 (3-50字符)" :prefix-icon="User" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码 (至少6位)"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱地址" :prefix-icon="Message" />
        </el-form-item>

        <el-form-item prop="code">
          <div style="display: flex; gap: 8px; width: 100%">
            <el-input v-model="form.code" placeholder="验证码" :prefix-icon="Key" />
            <el-button
              :disabled="countdown > 0"
              @click="sendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="handleRegister">
            注册
          </el-button>
        </el-form-item>

        <div class="auth-footer">
          已有账号？
          <el-link type="primary" @click="router.push('/login')">返回登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Key } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { userApi } from '@/api/user'
import { emailApi } from '@/api/email'
import AppHeader from '@/components/AppHeader.vue'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const countdown = ref(0)

const form = ref({
  username: '',
  password: '',
  email: '',
  code: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为 3-50 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度为 6-100 个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
  ],
}

async function sendCode() {
  if (!form.value.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  await emailApi.sendCode({ email: form.value.email })
  ElMessage.success('验证码已发送')

  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userApi.register(form.value)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.auth-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
}
</style>
