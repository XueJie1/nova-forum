import request from '@/utils/request'
import type { PageResponse, Comment, CommentRequest } from '@/types'

/**
 * 评论系统 API
 */
export const commentApi = {
  /** 创建评论/回复 */
  create(params: CommentRequest) {
    return request.post<any>('/comment/create', params)
  },

  /** 获取帖子评论列表 */
  getListByPost(postId: number, params?: { pageNum?: number; pageSize?: number }) {
    return request.get<PageResponse<Comment>>(`/comment/list/${postId}`, { params })
  },

  /** 获取评论详情 */
  getDetail(id: number) {
    return request.get<Comment>(`/comment/${id}`)
  },

  /** 更新评论 */
  update(id: number, content: string) {
    return request.put<any>(`/comment/${id}`, null, { params: { content } })
  },

  /** 删除评论 */
  remove(id: number) {
    return request.delete<any>(`/comment/${id}`)
  },

  /** 获取用户评论列表 */
  getByUser(userId: number, params?: { pageNum?: number; pageSize?: number }) {
    return request.get<PageResponse<Comment>>(`/comment/user/${userId}`, { params })
  },

  /** 获取评论回复列表 */
  getReplies(parentId: number, params?: { pageNum?: number; pageSize?: number }) {
    return request.get<PageResponse<Comment>>(`/comment/replies/${parentId}`, { params })
  },

  /** 获取帖子评论数量 */
  getCount(postId: number) {
    return request.get<number>(`/comment/count/${postId}`)
  },
}
