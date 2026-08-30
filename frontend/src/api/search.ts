import request from '@/utils/request'
import type { SearchResult, Post } from '@/types'

/**
 * 搜索 API
 */
export const searchApi = {
  /** 搜索帖子 */
  searchPosts(params?: { keyword?: string; page?: number; size?: number }) {
    return request.get<SearchResult>('/search/posts', { params })
  },

  /** 获取搜索建议 */
  getSuggestions(keyword: string, size?: number) {
    return request.get<string[]>('/search/suggestions', {
      params: { keyword, size },
    })
  },

  /** 创建搜索索引 */
  createIndex() {
    return request.post<any>('/search/index/create')
  },

  /** 重建搜索索引 */
  rebuildIndex() {
    return request.post<any>('/search/index/rebuild')
  },

  /** 删除搜索索引 */
  deleteIndex() {
    return request.delete<any>('/search/index')
  },
}
