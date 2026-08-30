<template>
  <div class="auth-page">
    <AppHeader />
    <div class="auth-container">
      <h2>用户登录</h2>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>

        <div class="auth-footer">
          还没有账号？
          <el-link type="primary" @click="router.push('/register')">立即注册</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'
import AppHeader from '@/components/AppHeader.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = ref({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await userApi.login(form.value)
    userStore.setLogin(data.token, {
      userId: data.userId,
      username: data.username,
      email: data.email,
    })

    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
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
