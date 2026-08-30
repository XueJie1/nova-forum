import request from '@/utils/request'
import type { EmailCodeRequest, EmailVerifyRequest, EmailVerifyResult } from '@/types'

/**
 * 邮箱验证 API
 */
export const emailApi = {
  /** 发送验证码 */
  sendCode(params: EmailCodeRequest) {
    return request.post<any>('/email/send-code', params)
  },

  /** 验证邮箱验证码 */
  verify(params: EmailVerifyRequest) {
    return request.post<EmailVerifyResult>('/email/verify', params)
  },

  /** 检查邮箱验证状态 */
  checkVerified(params: EmailCodeRequest) {
    return request.post<boolean>('/email/check-verified', params)
  },
}
