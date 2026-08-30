import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  // 首页 — 帖子列表
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
  },
  // 帖子详情 + 评论
  {
    path: '/post/:id',
    name: 'PostDetail',
    component: () => import('@/views/PostDetail.vue'),
  },
  // 发布帖子
  {
    path: '/post/create',
    name: 'CreatePost',
    component: () => import('@/views/CreatePost.vue'),
    meta: { requiresAuth: true },
  },
  // 编辑帖子
  {
    path: '/post/:id/edit',
    name: 'EditPost',
    component: () => import('@/views/EditPost.vue'),
    meta: { requiresAuth: true },
  },
  // 登录
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
  },
  // 注册
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
  },
  // 搜索
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/Search.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }
})

export default router
