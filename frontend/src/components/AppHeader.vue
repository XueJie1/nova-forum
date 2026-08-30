<template>
  <el-header class="app-header">
    <div class="header-content">
      <!-- Logo -->
      <div class="logo" @click="router.push('/')">
        <el-icon :size="24"><Monitor /></el-icon>
        <span>Nova Forum</span>
      </div>

      <!-- 搜索栏 -->
      <el-input
        v-model="searchKeyword"
        placeholder="搜索帖子..."
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <!-- 导航 -->
      <div class="header-actions">
        <template v-if="userStore.isLoggedIn">
          <el-button type="primary" @click="router.push('/post/create')">
            <el-icon><Plus /></el-icon>
            发帖子
          </el-button>

          <el-dropdown @command="handleCommand">
            <el-avatar :size="32" :src="userStore.profile?.email">
              {{ userStore.profile?.username?.[0]?.toUpperCase() }}
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button @click="router.push('/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value.trim() } })
  }
}

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/')
  }
}
</script>

<style scoped lang="scss">
.app-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  height: 60px;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  gap: 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
  white-space: nowrap;
}

.search-input {
  flex: 1;
  max-width: 400px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  white-space: nowrap;
}
</style>
