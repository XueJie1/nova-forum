package com.novaforum.nova_forum.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 评论请求数据传输对象
 *
 * @author Nova Forum Team
 * @since 2025-11-05
 */
@Data
public class CommentRequest {

    /**
     * 帖子ID
     */
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    /**
     * 父评论ID，null表示顶级评论
     */
    private Long parentId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容长度不能超过2000字符")
    private String content;
}
