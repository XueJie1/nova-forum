import request from '@/utils/request'
import type { LikeData } from '@/types'

/**
 * 点赞功能 API
 */
export const likeApi = {
  /** 点赞/取消点赞 */
  toggle(postId: number) {
    return request.post<LikeData>(`/like/${postId}`)
  },

  /** 获取帖子点赞数 */
  getCount(postId: number) {
    return request.get<number>(`/like/count/${postId}`)
  },

  /** 获取用户点赞状态 */
  getStatus(postId: number) {
    return request.get<boolean>(`/like/status/${postId}`)
  },

  /** 同步点赞数到数据库（管理员） */
  sync(postIds: number[]) {
    return request.post<any>('/like/sync', null, { params: { postIds: postIds.join(',') } })
  },
}
