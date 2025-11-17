package com.novaforum.nova_forum.service;

import com.novaforum.nova_forum.entity.Post;

/**
 * 帖子数据同步服务
 * 用于将数据库中的帖子数据同步到Elasticsearch
 */
public interface PostSyncService {

    /**
     * 同步单个帖子到Elasticsearch
     *
     * @param post 帖子实体
     */
    void syncPostToElasticsearch(Post post);

    /**
     * 从Elasticsearch删除帖子索引
     *
     * @param postId 帖子ID
     */
    void deletePostFromElasticsearch(Long postId);

    /**
     * 批量同步所有帖子到Elasticsearch
     */
    void syncAllPostsToElasticsearch();

    /**
     * 增量同步帖子数据（同步最近更新的数据）
     *
     * @param minutes 最近N分钟内更新的数据
     */
    void syncRecentPostsToElasticsearch(int minutes);
}
