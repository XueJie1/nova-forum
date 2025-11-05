package com.novaforum.nova_forum.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应数据传输对象
 * 支持多层级评论结构
 *
 * @author Nova Forum Team
 * @since 2025-11-05
 */
@Data
public class CommentResponse {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 评论者用户ID
     */
    private Long userId;

    /**
     * 评论者用户名
     */
    private String username;

    /**
     * 父评论ID
     */
    private Long parentId;

    /**
     * 父评论内容（如果是回复）
     */
    private String parentContent;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论创建时间
     */
    private LocalDateTime createTime;

    /**
     * 子评论数量
     */
    private Integer replyCount;

    /**
     * 子评论列表（递归结构）
     */
    private List<CommentResponse> replies;
}
