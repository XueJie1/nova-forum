package com.novaforum.nova_forum.controller;

import com.novaforum.nova_forum.dto.ApiResponse;
import com.novaforum.nova_forum.dto.SearchRequest;
import com.novaforum.nova_forum.dto.SearchResponse;
import com.novaforum.nova_forum.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索控制器
 */
@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 搜索帖子
     *
     * @param keyword   搜索关键词
     * @param page      页码（从1开始）
     * @param size      每页大小
     * @param sortBy    排序字段
     * @param sortOrder 排序方向
     * @param author    作者筛选
     * @param timeRange 时间范围（天数）
     * @return 搜索结果
     */
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<SearchResponse>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer timeRange) {

        try {
            SearchRequest request = new SearchRequest();
            request.setKeyword(keyword);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortOrder(sortOrder);
            request.setAuthor(author);
            request.setTimeRange(timeRange);

            SearchResponse response = searchService.searchPosts(request);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("搜索帖子失败", e);
            return ResponseEntity.ok(ApiResponse.error("搜索服务暂时不可用，请稍后重试"));
        }
    }

    /**
     * 获取搜索建议
     *
     * @param keyword 关键词
     * @param size    建议数量
     * @return 搜索建议列表
     */
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<java.util.List<String>>> getSuggestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "5") Integer size) {

        try {
            java.util.List<String> suggestions = searchService.getSearchSuggestions(keyword, size);
            return ResponseEntity.ok(ApiResponse.success(suggestions));
        } catch (Exception e) {
            log.error("获取搜索建议失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取搜索建议失败"));
        }
    }

    /**
     * 重建搜索索引
     *
     * @return 操作结果
     */
    @PostMapping("/index/rebuild")
    public ResponseEntity<ApiResponse<String>> rebuildIndex() {
        try {
            searchService.rebuildAllIndexes();
            return ResponseEntity.ok(ApiResponse.success("索引重建完成"));
        } catch (Exception e) {
            log.error("重建索引失败", e);
            return ResponseEntity.ok(ApiResponse.error("重建索引失败: " + e.getMessage()));
        }
    }

    /**
     * 检查索引状态
     *
     * @return 索引状态信息
     */
    @GetMapping("/index/status")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getIndexStatus() {
        try {
            java.util.Map<String, Object> status = new java.util.HashMap<>();
            status.put("indexExists", searchService.indexExists());

            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (Exception e) {
            log.error("获取索引状态失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取索引状态失败"));
        }
    }

    /**
     * 创建索引
     *
     * @return 操作结果
     */
    @PostMapping("/index/create")
    public ResponseEntity<ApiResponse<String>> createIndex() {
        try {
            if (searchService.indexExists()) {
                return ResponseEntity.ok(ApiResponse.error("索引已存在"));
            }

            searchService.createIndex();
            return ResponseEntity.ok(ApiResponse.success("索引创建成功"));
        } catch (Exception e) {
            log.error("创建索引失败", e);
            return ResponseEntity.ok(ApiResponse.error("创建索引失败: " + e.getMessage()));
        }
    }

    /**
     * 删除索引
     *
     * @return 操作结果
     */
    @DeleteMapping("/index")
    public ResponseEntity<ApiResponse<String>> deleteIndex() {
        try {
            if (!searchService.indexExists()) {
                return ResponseEntity.ok(ApiResponse.error("索引不存在"));
            }

            searchService.deleteIndex();
            return ResponseEntity.ok(ApiResponse.success("索引删除成功"));
        } catch (Exception e) {
            log.error("删除索引失败", e);
            return ResponseEntity.ok(ApiResponse.error("删除索引失败: " + e.getMessage()));
        }
    }
}
