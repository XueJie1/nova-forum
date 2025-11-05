package com.novaforum.nova_forum.service;

import com.novaforum.nova_forum.dto.LikeResponse;

/**
 * 点赞服务接口
 * 使用Redis缓存实现高性能点赞功能
 *
 * @author Nova Forum Team
 * @since 2025-11-05
 */
public interface LikeService {

    /**
     * 点赞/取消点赞帖子
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 点赞结果，包含新的点赞数和点赞状态
     */
    LikeResponse toggleLike(Long postId, Long userId);

    /**
     * 获取帖子点赞数
     * 
     * @param postId 帖子ID
     * @return 点赞数
     */
    Long getLikeCount(Long postId);

    /**
     * 获取用户点赞状态
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    boolean isLiked(Long postId, Long userId);

    /**
     * 获取帖子的点赞数（从数据库获取，用于同步）
     * 
     * @param postId 帖子ID
     * @return 点赞数
     */
    Long getLikeCountFromDatabase(Long postId);

    /**
     * 批量同步Redis缓存中的点赞数到数据库
     * 
     * @param postIds 帖子ID列表
     */
    void syncLikeCountsToDatabase(java.util.List<Long> postIds);
}
