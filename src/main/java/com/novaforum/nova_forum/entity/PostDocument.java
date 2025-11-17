package com.novaforum.nova_forum.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * 帖子搜索文档实体类
 * 用于Elasticsearch索引
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "posts")
public class PostDocument {

    /**
     * 文档ID，与数据库post.id保持一致
     */
    @Id
    private Long id;

    /**
     * 帖子标题
     */
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String title;

    /**
     * 帖子内容
     */
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String content;

    /**
     * 作者ID
     */
    @Field(type = FieldType.Long)
    private Long userId;

    /**
     * 作者用户名
     */
    @Field(type = FieldType.Keyword)
    private String username;

    /**
     * 浏览次数
     */
    @Field(type = FieldType.Integer)
    private Integer viewCount;

    /**
     * 点赞数
     */
    @Field(type = FieldType.Integer)
    private Integer likeCount;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Field(type = FieldType.Date)
    private LocalDateTime updateTime;

    /**
     * 搜索高亮标题（查询时动态设置）
     */
    private String highlightTitle;

    /**
     * 搜索高亮内容（查询时动态设置）
     */
    private String highlightContent;
}
