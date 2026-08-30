<template>
  <div>
    <AppHeader />
    <div class="page-container">
      <el-card v-loading="loading">
        <template #header>
          <span>编辑帖子</span>
        </template>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
        >
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" maxlength="200" show-word-limit />
          </el-form-item>

          <el-form-item label="内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="10"
              maxlength="5000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit">保存</el-button>
            <el-button @click="router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { postApi } from '@/api/post'
import type { PostRequest } from '@/types'
import AppHeader from '@/components/AppHeader.vue'

const route = useRoute()
const router = useRouter()
const postId = Number(route.params.id)
const formRef = ref<FormInstance>()
const loading = ref(true)

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

async function loadPost() {
  const data = await postApi.getDetail(postId)
  form.value = { title: data.title, content: data.content }
  loading.value = false
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  await postApi.update(postId, form.value)
  ElMessage.success('帖子更新成功')
  router.push({ name: 'PostDetail', params: { id: postId } })
}

onMounted(() => {
  loadPost()
})
</script>
