package com.novaforum.nova_forum.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 帖子实体类
 */
@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId; // 作者ID

    private String title; // 帖子标题

    private String content; // 帖子内容

    private Integer viewCount; // 浏览次数，默认0

    private Integer likeCount; // 点赞次数，默认0

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
