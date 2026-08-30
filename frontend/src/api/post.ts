import request from '@/utils/request'
import type { PageResponse, Post, PostRequest } from '@/types'

/**
 * 帖子管理 API
 */
export const postApi = {
  /** 发布帖子 */
  create(params: PostRequest) {
    return request.post<string>('/post/create', params)
  },

  /** 更新帖子 */
  update(id: number, params: PostRequest) {
    return request.put<any>(`/post/${id}`, params)
  },

  /** 删除帖子 */
  remove(id: number) {
    return request.delete<any>(`/post/${id}`)
  },

  /** 获取帖子详情 */
  getDetail(id: number) {
    return request.get<Post>(`/post/${id}`)
  },

  /** 获取帖子列表（分页） */
  getList(params?: { pageNum?: number; pageSize?: number; userId?: number }) {
    return request.get<PageResponse<Post>>('/post/list', { params })
  },
}
