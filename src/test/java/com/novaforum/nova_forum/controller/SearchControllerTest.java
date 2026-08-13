package com.novaforum.nova_forum.controller;

import com.novaforum.nova_forum.dto.SearchRequest;
import com.novaforum.nova_forum.dto.SearchResponse;
import com.novaforum.nova_forum.entity.PostDocument;
import com.novaforum.nova_forum.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SearchController 单元测试
 *
 * 使用 @WebMvcTest 进行 Controller 层测试
 * 测试覆盖：
 * - 搜索帖子接口
 * - 搜索建议接口
 * - 索引管理接口（创建、删除、重建、状态查询）
 * - 参数验证
 * - 异常处理
 *
 * 显式限定测试上下文，避免应用入口的 Mapper 扫描污染 Web MVC 切片。
 */
@WebMvcTest(controllers = {SearchController.class})
@ContextConfiguration(classes = SearchController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("搜索控制器单元测试")
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    private SearchResponse searchResponse;
    private PostDocument testPost;

    @BeforeEach
    void setUp() {
        testPost = new PostDocument();
        testPost.setId(1L);
        testPost.setTitle("Java编程基础");
        testPost.setContent("Java编程入门教程");
        testPost.setUserId(100L);
        testPost.setUsername("testuser");
        testPost.setViewCount(1000);
        testPost.setLikeCount(50);
        testPost.setCreateTime(LocalDateTime.now());
        testPost.setUpdateTime(LocalDateTime.now());

        searchResponse = new SearchResponse();
        searchResponse.setRecords(Arrays.asList(testPost));
        searchResponse.setTotal(1L);
        searchResponse.setPages(1L);
        searchResponse.setCurrent(1);
        searchResponse.setSize(10);
        searchResponse.setHasNext(false);
        searchResponse.setHasPrevious(false);
        searchResponse.setKeyword("Java");
        searchResponse.setTook(50L);
        searchResponse.setSuggestions(Arrays.asList("Java教程", "Java实战"));
    }

    // ==================== 搜索帖子接口测试 ====================

    @Test
    @DisplayName("测试搜索帖子 - 带关键词成功")
    void testSearchPosts_WithKeyword_Success() throws Exception {
        // Arrange
        when(searchService.searchPosts(any(SearchRequest.class))).thenReturn(searchResponse);

        // Act & Assert
        mockMvc.perform(get("/search/posts")
                        .param("keyword", "Java")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.records", hasSize(1)))
                .andExpect(jsonPath("$.data.records[0].title").value("Java编程基础"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.keyword").value("Java"))
                .andExpect(jsonPath("$.data.took").value(50));

        verify(searchService, times(1)).searchPosts(any(SearchRequest.class));
    }

    @Test
    @DisplayName("测试搜索帖子 - 无关键词使用默认参数")
    void testSearchPosts_WithoutKeyword_UsesDefaults() throws Exception {
        // Arrange
        when(searchService.searchPosts(any(SearchRequest.class))).thenReturn(searchResponse);

        // Act & Assert
        mockMvc.perform(get("/search/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(searchService, times(1)).searchPosts(any(SearchRequest.class));
    }

    @Test
    @DisplayName("测试搜索帖子 - 自定义分页参数")
    void testSearchPosts_WithCustomPagination() throws Exception {
        // Arrange
        when(searchService.searchPosts(any(SearchRequest.class))).thenReturn(searchResponse);

        // Act & Assert
        mockMvc.perform(get("/search/posts")
                        .param("keyword", "Spring")
                        .param("page", "2")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(searchService, times(1)).searchPosts(any(SearchRequest.class));
    }

    @Test
    @DisplayName("测试搜索帖子 - 所有可选参数")
    void testSearchPosts_WithAllParameters() throws Exception {
        // Arrange
        when(searchService.searchPosts(any(SearchRequest.class))).thenReturn(searchResponse);

        // Act & Assert
        mockMvc.perform(get("/search/posts")
                        .param("keyword", "Java")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sortBy", "createTime")
                        .param("sortOrder", "desc")
                        .param("author", "testuser")
                        .param("timeRange", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(searchService, times(1)).searchPosts(any(SearchRequest.class));
    }

    @Test
    @DisplayName("测试搜索帖子 - 服务异常返回错误")
    void testSearchPosts_ServiceException_ReturnsError() throws Exception {
        // Arrange
        when(searchService.searchPosts(any(SearchRequest.class)))
                .thenThrow(new RuntimeException("Elasticsearch连接失败"));

        // Act & Assert
        mockMvc.perform(get("/search/posts")
                        .param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("搜索服务暂时不可用，请稍后重试"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(searchService, times(1)).searchPosts(any(SearchRequest.class));
    }

    @Test
    @DisplayName("测试搜索帖子 - 无结果")
    void testSearchPosts_NoResults() throws Exception {
        // Arrange
        SearchResponse emptyResponse = new SearchResponse();
        emptyResponse.setRecords(Collections.emptyList());
        emptyResponse.setTotal(0L);
        emptyResponse.setPages(0L);
        emptyResponse.setCurrent(1);
        emptyResponse.setSize(10);
        emptyResponse.setKeyword("不存在的关键词");

        when(searchService.searchPosts(any(SearchRequest.class))).thenReturn(emptyResponse);

        // Act & Assert
        mockMvc.perform(get("/search/posts")
                        .param("keyword", "不存在的关键词"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records", hasSize(0)))
                .andExpect(jsonPath("$.data.total").value(0));

        verify(searchService, times(1)).searchPosts(any(SearchRequest.class));
    }

    // ==================== 搜索建议接口测试 ====================

    @Test
    @DisplayName("测试获取搜索建议 - 成功")
    void testGetSuggestions_Success() throws Exception {
        // Arrange
        List<String> suggestions = Arrays.asList("Java教程", "Java实战", "Java高级");
        when(searchService.getSearchSuggestions(anyString(), anyInt())).thenReturn(suggestions);

        // Act & Assert
        mockMvc.perform(get("/search/suggestions")
                        .param("keyword", "Java")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0]").value("Java教程"))
                .andExpect(jsonPath("$.data[1]").value("Java实战"))
                .andExpect(jsonPath("$.data[2]").value("Java高级"));

        verify(searchService, times(1)).getSearchSuggestions("Java", 3);
    }

    @Test
    @DisplayName("测试获取搜索建议 - 使用默认size")
    void testGetSuggestions_DefaultSize() throws Exception {
        // Arrange
        List<String> suggestions = Arrays.asList("Spring教程", "Spring实战");
        when(searchService.getSearchSuggestions(anyString(), anyInt())).thenReturn(suggestions);

        // Act & Assert
        mockMvc.perform(get("/search/suggestions")
                        .param("keyword", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)));

        verify(searchService, times(1)).getSearchSuggestions("Spring", 5);
    }

    @Test
    @DisplayName("测试获取搜索建议 - 服务异常")
    void testGetSuggestions_ServiceException() throws Exception {
        // Arrange
        when(searchService.getSearchSuggestions(anyString(), anyInt()))
                .thenThrow(new RuntimeException("服务异常"));

        // Act & Assert
        mockMvc.perform(get("/search/suggestions")
                        .param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("获取搜索建议失败"));

        verify(searchService, times(1)).getSearchSuggestions(anyString(), anyInt());
    }

    // ==================== 索引管理接口测试 ====================

    @Test
    @DisplayName("测试创建索引 - 成功")
    void testCreateIndex_Success() throws Exception {
        // Arrange
        when(searchService.indexExists()).thenReturn(false);
        doNothing().when(searchService).createIndex();

        // Act & Assert
        mockMvc.perform(post("/search/index/create"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("索引创建成功"));

        verify(searchService, times(1)).indexExists();
        verify(searchService, times(1)).createIndex();
    }

    @Test
    @DisplayName("测试创建索引 - 索引已存在")
    void testCreateIndex_AlreadyExists() throws Exception {
        // Arrange
        when(searchService.indexExists()).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/search/index/create"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("索引已存在"));

        verify(searchService, times(1)).indexExists();
        verify(searchService, never()).createIndex();
    }

    @Test
    @DisplayName("测试创建索引 - 创建失败")
    void testCreateIndex_Fails() throws Exception {
        // Arrange
        when(searchService.indexExists()).thenReturn(false);
        doThrow(new RuntimeException("创建索引失败")).when(searchService).createIndex();

        // Act & Assert
        mockMvc.perform(post("/search/index/create"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("创建索引失败")));

        verify(searchService, times(1)).createIndex();
    }

    @Test
    @DisplayName("测试删除索引 - 成功")
    void testDeleteIndex_Success() throws Exception {
        // Arrange
        when(searchService.indexExists()).thenReturn(true);
        doNothing().when(searchService).deleteIndex();

        // Act & Assert
        mockMvc.perform(delete("/search/index"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("索引删除成功"));

        verify(searchService, times(1)).indexExists();
        verify(searchService, times(1)).deleteIndex();
    }

    @Test
    @DisplayName("测试删除索引 - 索引不存在")
    void testDeleteIndex_NotExists() throws Exception {
        // Arrange
        when(searchService.indexExists()).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/search/index"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("索引不存在"));

        verify(searchService, times(1)).indexExists();
        verify(searchService, never()).deleteIndex();
    }

    @Test
    @DisplayName("测试删除索引 - 删除失败")
    void testDeleteIndex_Fails() throws Exception {
        // Arrange
        when(searchService.indexExists()).thenReturn(true);
        doThrow(new RuntimeException("删除索引失败")).when(searchService).deleteIndex();

        // Act & Assert
        mockMvc.perform(delete("/search/index"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("删除索引失败")));

        verify(searchService, times(1)).deleteIndex();
    }

    @Test
    @DisplayName("测试重建索引 - 成功")
    void testRebuildIndex_Success() throws Exception {
        // Arrange
        doNothing().when(searchService).rebuildAllIndexes();

        // Act & Assert
        mockMvc.perform(post("/search/index/rebuild"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("索引重建完成"));

        verify(searchService, times(1)).rebuildAllIndexes();
    }

    @Test
    @DisplayName("测试重建索引 - 失败")
    void testRebuildIndex_Fails() throws Exception {
        // Arrange
        doThrow(new RuntimeException("重建失败")).when(searchService).rebuildAllIndexes();

        // Act & Assert
        mockMvc.perform(post("/search/index/rebuild"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("重建索引失败")));

        verify(searchService, times(1)).rebuildAllIndexes();
    }

    @Test
    @DisplayName("测试获取索引状态 - 索引存在")
    void testGetIndexStatus_Exists() throws Exception {
        // Arrange
        when(searchService.indexExists()).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/search/index/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.indexExists").value(true));

        verify(searchService, times(1)).indexExists();
    }

    @Test
    @DisplayName("测试获取索引状态 - 索引不存在")
    void testGetIndexStatus_NotExists() throws Exception {
        // Arrange
        when(searchService.indexExists()).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/search/index/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.indexExists").value(false));

        verify(searchService, times(1)).indexExists();
    }

    @Test
    @DisplayName("测试获取索引状态 - 异常")
    void testGetIndexStatus_Exception() throws Exception {
        // Arrange
        when(searchService.indexExists()).thenThrow(new RuntimeException("连接失败"));

        // Act & Assert
        mockMvc.perform(get("/search/index/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("获取索引状态失败"));

        verify(searchService, times(1)).indexExists();
    }

}
