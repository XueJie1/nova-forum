package com.novaforum.nova_forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.novaforum.nova_forum.entity.Post;
import com.novaforum.nova_forum.entity.PostDocument;
import com.novaforum.nova_forum.mapper.PostMapper;
import com.novaforum.nova_forum.service.PostSyncService;
import com.novaforum.nova_forum.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子数据同步服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostSyncServiceImpl implements PostSyncService {

    private final PostMapper postMapper;
    private final SearchService searchService;

    @Override
    @Transactional
    public void syncPostToElasticsearch(Post post) {
        try {
            // 确保索引存在
            if (!searchService.indexExists()) {
                searchService.createIndex();
            }

            // 转换Post为PostDocument
            PostDocument postDocument = convertToPostDocument(post);

            // 索引到Elasticsearch
            searchService.indexPost(postDocument);

            log.info("成功同步帖子到Elasticsearch: {}", post.getId());
        } catch (Exception e) {
            log.error("同步帖子到Elasticsearch失败: {}", post.getId(), e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    @Override
    public void deletePostFromElasticsearch(Long postId) {
        try {
            searchService.deletePostIndex(postId);
            log.info("成功从Elasticsearch删除帖子索引: {}", postId);
        } catch (Exception e) {
            log.error("从Elasticsearch删除帖子索引失败: {}", postId, e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    @Override
    public void syncAllPostsToElasticsearch() {
        try {
            log.info("开始批量同步所有帖子到Elasticsearch");

            // 确保索引存在
            if (!searchService.indexExists()) {
                searchService.createIndex();
            }

            // 查询所有帖子
            List<Post> posts = postMapper.selectList(null);
            log.info("找到 {} 个帖子需要同步", posts.size());

            // 批量转换和索引
            List<PostDocument> postDocuments = posts.stream()
                    .map(this::convertToPostDocument)
                    .collect(Collectors.toList());

            // 逐个索引（可以优化为批量操作）
            int successCount = 0;
            int failCount = 0;

            for (PostDocument postDocument : postDocuments) {
                try {
                    searchService.indexPost(postDocument);
                    successCount++;
                } catch (Exception e) {
                    log.error("同步帖子失败: {}", postDocument.getId(), e);
                    failCount++;
                }
            }

            log.info("批量同步完成，成功: {}, 失败: {}", successCount, failCount);
        } catch (Exception e) {
            log.error("批量同步帖子到Elasticsearch失败", e);
            throw new RuntimeException("批量同步失败", e);
        }
    }

    @Override
    public void syncRecentPostsToElasticsearch(int minutes) {
        try {
            log.info("开始增量同步最近 {} 分钟更新的帖子", minutes);

            // 计算时间范围
            LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);

            // 查询最近更新的帖子
            QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
            queryWrapper.ge("update_time", since);

            List<Post> posts = postMapper.selectList(queryWrapper);
            log.info("找到 {} 个最近更新的帖子需要同步", posts.size());

            // 同步到Elasticsearch
            for (Post post : posts) {
                syncPostToElasticsearch(post);
            }

            log.info("增量同步完成，共同步 {} 个帖子", posts.size());
        } catch (Exception e) {
            log.error("增量同步帖子到Elasticsearch失败", e);
            throw new RuntimeException("增量同步失败", e);
        }
    }

    /**
     * 将Post实体转换为PostDocument
     *
     * @param post 帖子实体
     * @return 搜索文档
     */
    private PostDocument convertToPostDocument(Post post) {
        PostDocument document = new PostDocument();
        document.setId(post.getId());
        document.setTitle(post.getTitle());
        document.setContent(post.getContent());
        document.setUserId(post.getUserId());

        // 设置默认用户名（后续可以优化为从User表查询）
        document.setUsername("用户" + post.getUserId());

        document.setViewCount(post.getViewCount());
        document.setLikeCount(post.getLikeCount());
        document.setCreateTime(post.getCreateTime());
        document.setUpdateTime(post.getUpdateTime());

        return document;
    }
}
