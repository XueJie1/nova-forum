<template>
  <div>
    <AppHeader />
    <div class="page-container">
      <!-- 帖子内容 -->
      <el-card v-if="post" class="post-detail-card">
        <template #header>
          <div class="post-header">
            <h1 class="post-title">{{ post.title }}</h1>
            <div class="post-actions">
              <el-button
                type="danger"
                size="small"
                v-if="isOwner"
                @click="handleDelete"
              >
                删除
              </el-button>
              <el-button
                size="small"
                v-if="isOwner"
                @click="router.push({ name: 'EditPost', params: { id: post.id } })"
              >
                编辑
              </el-button>
            </div>
          </div>
        </template>

        <div class="post-info">
          <span><el-icon><User /></el-icon> {{ post.username }}</span>
          <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
          <el-button
            :type="liked ? 'primary' : 'default'"
            :icon="Star"
            circle
            size="small"
            @click="handleLike"
          />
          <span class="like-count">{{ post.likeCount }}</span>
        </div>

        <div class="post-body">{{ post.content }}</div>
        <div class="post-time">{{ formatTime(post.createTime) }}</div>
      </el-card>

      <!-- 评论区域 -->
      <el-card class="comment-section">
        <template #header>
          <span>评论 ({{ commentCount }})</span>
        </template>

        <!-- 发表评论 -->
        <div class="comment-input">
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论..."
            @keyup.ctrl.enter="submitComment"
          />
          <el-button
            type="primary"
            :disabled="!newComment.trim() || !userStore.isLoggedIn"
            @click="submitComment"
          >
            发表评论
          </el-button>
        </div>

        <!-- 评论列表 -->
        <div v-if="comments.length === 0" class="empty-comments">暂无评论</div>
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-main">
            <el-avatar :size="28">{{ comment.username[0] }}</el-avatar>
            <div class="comment-body">
              <div class="comment-author">{{ comment.username }}</div>
              <div class="comment-text">{{ comment.content }}</div>
              <div class="comment-footer">
                <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                <el-button
                  text
                  type="primary"
                  size="small"
                  v-if="userStore.isLoggedIn"
                  @click="showReplyInput = comment.id"
                >
                  回复
                </el-button>
              </div>
            </div>
          </div>

          <!-- 回复输入框 -->
          <div class="reply-input" v-if="showReplyInput === comment.id">
            <el-input
              v-model="replyContent"
              size="small"
              placeholder="回复..."
              @keyup.enter="submitReply(comment.id)"
            />
            <el-button size="small" type="primary" @click="submitReply(comment.id)">
              发送
            </el-button>
            <el-button size="small" @click="showReplyInput = null">
              取消
            </el-button>
          </div>

          <!-- 子回复 -->
          <div class="reply-list" v-if="comment.replies?.length">
            <div v-for="reply in comment.replies" :key="reply.id" class="comment-item reply-item">
              <el-avatar :size="22">{{ reply.username[0] }}</el-avatar>
              <div class="comment-body">
                <span class="comment-author">{{ reply.username }}</span>
                <span class="comment-text">{{ reply.content }}</span>
                <span class="comment-time">{{ formatTime(reply.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <el-button class="back-btn" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Star, ArrowLeft } from '@element-plus/icons-vue'
import { postApi } from '@/api/post'
import { commentApi } from '@/api/comment'
import { likeApi } from '@/api/like'
import { useUserStore } from '@/stores/user'
import type { Post, Comment } from '@/types'
import AppHeader from '@/components/AppHeader.vue'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const postId = computed(() => Number(route.params.id))
const post = ref<Post | null>(null)
const comments = ref<Comment[]>([])
const commentCount = ref(0)
const newComment = ref('')
const replyContent = ref('')
const showReplyInput = ref<number | null>(null)
const liked = ref(false)

const isOwner = computed(() => {
  return userStore.profile?.userId === post.value?.userId
})

function formatTime(time: string) {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

async function loadPost() {
  post.value = await postApi.getDetail(postId.value)
  liked.value = false
  try {
    liked.value = await likeApi.getStatus(postId.value)
  } catch {
    // 未登录时不查点赞状态
  }
}

async function loadComments() {
  const data = await commentApi.getListByPost(postId.value)
  comments.value = data.records
  commentCount.value = data.total
}

async function handleLike() {
  if (!userStore.isLoggedIn) return router.push('/login')
  const data = await likeApi.toggle(postId.value)
  liked.value = data.isLiked
  post.value!.likeCount = data.likeCount
}

async function handleDelete() {
  await ElMessageBox.confirm('确定删除此帖子？', '确认', { type: 'warning' })
  await postApi.remove(postId.value)
  router.push('/')
}

async function submitComment() {
  if (!userStore.isLoggedIn) return router.push('/login')
  await commentApi.create({ postId: postId.value, content: newComment.value, parentId: null })
  newComment.value = ''
  loadComments()
}

async function submitReply(parentId: number) {
  if (!replyContent.value.trim()) return
  await commentApi.create({ postId: postId.value, content: replyContent.value, parentId })
  replyContent.value = ''
  showReplyInput.value = null
  loadComments()
}

onMounted(() => {
  loadPost()
  loadComments()
})
</script>

<style scoped lang="scss">
.post-detail-card {
  margin-bottom: 20px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.post-title {
  font-size: 22px;
  font-weight: bold;
  margin: 0;
}

.post-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  font-size: 14px;
  color: #909399;
}

.like-count {
  color: #e6a23c;
}

.post-body {
  padding: 16px 20px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.post-time {
  padding: 8px 20px;
  font-size: 13px;
  color: #c0c4cc;
}

.comment-section {
  margin-top: 20px;
}

.comment-input {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.comment-main {
  display: flex;
  gap: 10px;
}

.comment-body {
  flex: 1;
}

.comment-author {
  font-weight: 600;
  font-size: 14px;
}

.comment-text {
  margin: 4px 0;
  line-height: 1.5;
}

.comment-footer {
  display: flex;
  align-items: center;
  gap: 12px;
}

.comment-time {
  font-size: 12px;
  color: #c0c4cc;
}

.reply-input {
  display: flex;
  gap: 8px;
  margin: 8px 0;
  padding-left: 38px;
}

.reply-item {
  display: flex;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
}

.reply-item .comment-body {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.empty-comments {
  text-align: center;
  color: #c0c4cc;
  padding: 20px;
}

.back-btn {
  margin-top: 16px;
}
</style>
