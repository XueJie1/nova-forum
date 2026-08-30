<template>
  <div>
    <AppHeader />
    <div class="page-container">
      <el-row :gutter="16">
        <!-- 帖子列表 -->
        <el-col :span="16">
          <div v-for="post in posts" :key="post.id" class="post-card" @click="goToPost(post.id)">
            <el-card :body-style="{ padding: '0' }">
              <template #header>
                <div class="card-header">
                  <span class="post-title">{{ post.title }}</span>
                </div>
              </template>
              <div class="post-preview">{{ post.content }}</div>
              <div class="post-meta">
                <span><el-icon><User /></el-icon> {{ post.username || '未知用户' }}</span>
                <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
                <span><el-icon><Opportunity /></el-icon> {{ post.likeCount }}</span>
                <span class="time">{{ formatTime(post.createTime) }}</span>
              </div>
            </el-card>
          </div>

          <!-- 分页 -->
          <div class="pagination-wrapper" v-if="totalPages > 1">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="loadPosts"
            />
          </div>

          <!-- 空状态 -->
          <el-empty v-if="posts.length === 0 && !loading" description="暂无帖子" />
        </el-col>

        <!-- 侧边栏 -->
        <el-col :span="8">
          <el-card class="sidebar-card">
            <template #header>公告</template>
            <p style="color: #909399; font-size: 14px;">欢迎来到 Nova Forum！</p>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { postApi } from '@/api/post'
import type { Post } from '@/types'
import AppHeader from '@/components/AppHeader.vue'
import dayjs from 'dayjs'

const router = useRouter()
const posts = ref<Post[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(0)
const loading = ref(false)

async function loadPosts() {
  loading.value = true
  try {
    const data = await postApi.getList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
    })
    posts.value = data.records
    total.value = data.total
    totalPages.value = data.pages
  } finally {
    loading.value = false
  }
}

function goToPost(id: number) {
  router.push({ name: 'PostDetail', params: { id } })
}

function formatTime(time: string) {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped lang="scss">
.post-title {
  font-size: 16px;
  font-weight: 600;
}

.post-preview {
  padding: 0 20px;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 20px;
  font-size: 13px;
  color: #909399;
}

.time {
  margin-left: auto;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.sidebar-card {
  position: sticky;
  top: 80px;
}
</style>
