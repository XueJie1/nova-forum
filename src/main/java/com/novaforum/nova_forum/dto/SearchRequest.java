package com.novaforum.nova_forum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 排序字段
     * 可选值：createTime, viewCount, likeCount, relevance
     */
    private String sortBy = "relevance";

    /**
     * 排序方向
     * 可选值：desc, asc
     */
    private String sortOrder = "desc";

    /**
     * 作者筛选
     */
    private String author;

    /**
     * 时间范围筛选（天数）
     * 例如：1（最近1天）、7（最近7天）、30（最近30天）
     */
    private Integer timeRange;
}
