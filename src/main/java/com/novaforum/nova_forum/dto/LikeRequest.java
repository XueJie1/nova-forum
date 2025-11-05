package com.novaforum.nova_forum.dto;

import lombok.Data;

/**
 * 点赞请求数据传输对象
 *
 * @author Nova Forum Team
 * @since 2025-11-05
 */
@Data
public class LikeRequest {

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 用户ID（从Token中获取，可选）
     */
    private Long userId;
}
