// ========== 统一响应 ==========
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// ========== 分页响应 ==========
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// ========== 用户相关 ==========
export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
  code: string
}

export interface LoginData {
  userId: number
  username: string
  email: string
  token: string
}

export interface UserProfile {
  userId: number
  username: string
  email: string
}

// ========== 帖子相关 ==========
export interface PostRequest {
  title: string
  content: string
}

export interface Post {
  id: number
  userId: number
  username: string | null
  title: string
  content: string
  viewCount: number
  likeCount: number
  createTime: string
  updateTime: string
}

// ========== 评论相关 ==========
export interface CommentRequest {
  postId: number
  content: string
  parentId: number | null
}

export interface Comment {
  id: number
  postId: number
  userId: number
  username: string
  parentId: number | null
  parentContent: string | null
  content: string
  createTime: string
  replyCount: number
  replies?: Comment[]
}

// ========== 点赞相关 ==========
export interface LikeData {
  postId: number
  likeCount: number
  isLiked: boolean
}

// ========== 搜索相关 ==========
export interface SearchResult extends PageResponse<Post> {
  hasNext: boolean
  hasPrevious: boolean
  keyword: string
  took: number
}

// ========== 邮箱验证 ==========
export interface EmailCodeRequest {
  email: string
}

export interface EmailVerifyRequest {
  email: string
  code: string
}

export interface EmailVerifyResult {
  isValid: boolean
  message: string
}
