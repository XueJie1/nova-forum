<template>
  <div>
    <AppHeader />
    <div class="page-container">
      <el-card>
        <template #header>
          <div class="search-header">
            <el-input
              v-model="keyword"
              placeholder="搜索帖子..."
              size="large"
              clearable
              @input="handleInput"
              @keyup.enter="doSearch"
              @clear="doSearch"
            >
              <template #append>
                <el-button :icon="Search" @click="doSearch">搜索</el-button>
              </template>
            </el-input>

            <!-- 搜索建议 -->
            <el-autocomplete
              v-if="keyword"
              v-model="keyword"
              :fetch-suggestions="fetchSuggestions"
              placeholder="智能推荐"
              @select="handleSelectSuggestion"
              @keyup.enter="doSearch"
            />
          </div>
        </template>

        <!-- 搜索结果统计 -->
        <div v-if="hasSearched" class="search-stats">
          找到 <strong>{{ total }}</strong> 条结果，耗时 {{ took }}ms
        </div>

        <!-- 结果列表 -->
        <div v-for="post in posts" :key="post.id" class="post-card" @click="goToPost(post.id)">
          <el-card :body-style="{ padding: '0' }">
            <template #header>
              <span class="post-title">{{ post.title }}</span>
            </template>
            <div class="post-preview">{{ post.content }}</div>
            <div class="post-meta">
              <span><el-icon><User /></el-icon> {{ post.username }}</span>
              <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
              <span><el-icon><Opportunity /></el-icon> {{ post.likeCount }}</span>
              <span class="time">{{ formatTime(post.createTime) }}</span>
            </div>
          </el-card>
        </div>

        <el-empty v-if="hasSearched && posts.length === 0" description="未找到相关帖子" />

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="pages > 1">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="size"
            :total="total"
            layout="prev, pager, next"
            @current-change="doSearch"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { searchApi } from '@/api/search'
import type { Post } from '@/types'
import AppHeader from '@/components/AppHeader.vue'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const keyword = ref((route.query.keyword as string) || '')
const posts = ref<Post[]>([])
const total = ref(0)
const pages = ref(0)
const currentPage = ref(1)
const size = ref(10)
const took = ref(0)
const hasSearched = ref(false)

async function doSearch() {
  const data = await searchApi.searchPosts({
    keyword: keyword.value,
    page: currentPage.value,
    size: size.value,
  })
  posts.value = data.records
  total.value = data.total
  pages.value = data.pages
  took.value = data.took
  hasSearched.value = true
}

async function fetchSuggestions(queryString: string, callback: (items: any[]) => void) {
  if (!queryString.trim()) {
    callback([])
    return
  }
  try {
    const suggestions = await searchApi.getSuggestions(queryString)
    callback(suggestions.map((s) => ({ value: s })))
  } catch {
    callback([])
  }
}

function handleSelectSuggestion(item: { value: string }) {
  keyword.value = item.value
  doSearch()
}

function goToPost(id: number) {
  router.push({ name: 'PostDetail', params: { id } })
}

function formatTime(time: string) {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  doSearch()
})
</script>

<style scoped lang="scss">
.search-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-stats {
  margin-bottom: 16px;
  color: #909399;
  font-size: 14px;
}

.post-card {
  margin-bottom: 12px;
  cursor: pointer;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }

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
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
