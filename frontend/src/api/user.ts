import request from '@/utils/request'
import type {
  LoginRequest,
  LoginData,
  RegisterRequest,
  UserProfile,
} from '@/types'

/**
 * 用户认证 API
 */
export const userApi = {
  /** 用户注册 */
  register(params: RegisterRequest) {
    return request.post<any>('/user/register', params)
  },

  /** 用户登录 */
  login(params: LoginRequest) {
    return request.post<LoginData>('/user/login', params)
  },

  /** 获取用户信息 */
  getProfile() {
    return request.get<UserProfile>('/user/profile')
  },
}
