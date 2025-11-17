package com.novaforum.nova_forum.dto;

import com.novaforum.nova_forum.entity.PostDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    /**
     * 搜索结果列表
     */
    private List<PostDocument> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 搜索耗时（毫秒）
     */
    private Long took;

    /**
     * 搜索建议
     */
    private List<String> suggestions;
}
