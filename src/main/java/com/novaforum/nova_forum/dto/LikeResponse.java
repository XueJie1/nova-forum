package com.novaforum.nova_forum.dto;

import lombok.Data;

/**
 * 点赞响应数据传输对象
 *
 * @author Nova Forum Team
 * @since 2025-11-05
 */
@Data
public class LikeResponse {

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 点赞数量
     */
    private Long likeCount;

    /**
     * 用户是否已点赞
     */
    private Boolean isLiked;
}
