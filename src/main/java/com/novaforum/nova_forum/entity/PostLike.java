package com.novaforum.nova_forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子点赞记录实体类
 * 记录用户对帖子的点赞关系
 *
 * @author Nova Forum Team
 * @since 2025-11-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("post_like")
public class PostLike implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 点赞记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 点赞时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
