<template>
  <div>
    <AppHeader />
    <div class="page-container">
      <el-card>
        <template #header>
          <span>发布帖子</span>
        </template>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
        >
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入帖子标题" maxlength="200" show-word-limit />
          </el-form-item>

          <el-form-item label="内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="10"
              placeholder="请输入帖子内容"
              maxlength="5000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit">发布</el-button>
            <el-button @click="router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { postApi } from '@/api/post'
import type { PostRequest } from '@/types'
import AppHeader from '@/components/AppHeader.vue'

const router = useRouter()
const formRef = ref<FormInstance>()

const form = ref<PostRequest>({
  title: '',
  content: '',
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 1, max: 200, message: '标题长度在 1-200 个字符', trigger: 'blur' },
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 1, max: 5000, message: '内容长度在 1-5000 个字符', trigger: 'blur' },
  ],
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const result = await postApi.create(form.value)
  // 返回格式: "帖子ID: 123"
  const match = result?.match(/帖子ID:\s*(\d+)/)
  const newId = match ? Number(match[1]) : null

  ElMessage.success('帖子发布成功')
  router.push(newId ? { name: 'PostDetail', params: { id: newId } } : '/')
}
</script>
