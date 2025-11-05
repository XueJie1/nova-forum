package com.novaforum.nova_forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体类
 * 支持多层级评论结构
 *
 * @author Nova Forum Team
 * @since 2025-11-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 评论者用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 父评论ID，支持回复功能，NULL表示顶级评论
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 评论创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 用户信息（关联查询时使用）
     */
    @TableField(exist = false)
    private String username;

    /**
     * 父评论信息（关联查询时使用）
     */
    @TableField(exist = false)
    private String parentContent;

    /**
     * 子评论数量（统计字段）
     */
    @TableField(exist = false)
    private Integer replyCount;
}
