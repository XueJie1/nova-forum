package com.novaforum.nova_forum.service;

import com.novaforum.nova_forum.dto.SearchRequest;
import com.novaforum.nova_forum.dto.SearchResponse;
import com.novaforum.nova_forum.entity.PostDocument;

/**
 * 搜索服务接口
 */
public interface SearchService {

    /**
     * 搜索帖子
     *
     * @param request 搜索请求
     * @return 搜索响应
     */
    SearchResponse searchPosts(SearchRequest request);

    /**
     * 索引单个帖子文档
     *
     * @param postDocument 帖子文档
     */
    void indexPost(PostDocument postDocument);

    /**
     * 更新帖子索引
     *
     * @param postDocument 帖子文档
     */
    void updatePostIndex(PostDocument postDocument);

    /**
     * 删除帖子索引
     *
     * @param postId 帖子ID
     */
    void deletePostIndex(Long postId);

    /**
     * 重建所有帖子索引
     */
    void rebuildAllIndexes();

    /**
     * 获取搜索建议
     *
     * @param keyword 关键词
     * @param size    建议数量
     * @return 搜索建议列表
     */
    java.util.List<String> getSearchSuggestions(String keyword, int size);

    /**
     * 检查索引是否存在
     *
     * @return 是否存在
     */
    boolean indexExists();

    /**
     * 创建索引
     */
    void createIndex();

    /**
     * 删除索引
     */
    void deleteIndex();
}
