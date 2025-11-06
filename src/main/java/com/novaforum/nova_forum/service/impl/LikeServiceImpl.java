package com.novaforum.nova_forum.service.impl;

import com.novaforum.nova_forum.dto.LikeResponse;
import com.novaforum.nova_forum.entity.PostLike;
import com.novaforum.nova_forum.mapper.PostLikeMapper;
import com.novaforum.nova_forum.mapper.PostMapper;
import com.novaforum.nova_forum.service.LikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 点赞服务实现类
 * 使用Redis缓存实现高性能点赞功能
 *
 * @author Nova Forum Team
 * @since 2025-11-05
 */
@Slf4j
@Service
public class LikeServiceImpl implements LikeService {

    private static final String LIKE_SET_PREFIX = "post_like:";
    private static final String LIKE_COUNT_PREFIX = "post_like_count:";
    private static final String USER_LIKES_PREFIX = "user_likes:";
    private static final long CACHE_EXPIRE_TIME = 30 * 24 * 60 * 60; // 30天

    private final RedisTemplate<String, Object> redisTemplate;
    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;

    public LikeServiceImpl(RedisTemplate<String, Object> redisTemplate, PostMapper postMapper,
            PostLikeMapper postLikeMapper) {
        this.redisTemplate = redisTemplate;
        this.postMapper = postMapper;
        this.postLikeMapper = postLikeMapper;
    }

    @Override
    @Transactional
    public LikeResponse toggleLike(Long postId, Long userId) {
        // 验证帖子是否存在
        if (postMapper.selectById(postId) == null) {
            throw new RuntimeException("帖子不存在");
        }

        String likeSetKey = LIKE_SET_PREFIX + postId;
        String likeCountKey = LIKE_COUNT_PREFIX + postId;
        String userLikesKey = USER_LIKES_PREFIX + userId;

        Boolean isMember = redisTemplate.opsForSet().isMember(likeSetKey, userId);

        LikeResponse response = new LikeResponse();
        response.setPostId(postId);

        if (Boolean.TRUE.equals(isMember)) {
            // 取消点赞
            redisTemplate.opsForSet().remove(likeSetKey, userId);
            redisTemplate.opsForSet().remove(userLikesKey, postId);

            // 更新点赞数
            Long newCount = redisTemplate.opsForSet().size(likeSetKey);
            if (newCount != null) {
                redisTemplate.opsForValue().set(likeCountKey, newCount, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                response.setLikeCount(newCount);
            } else {
                response.setLikeCount(0L);
            }
            response.setIsLiked(false);

            log.info("用户 {} 取消点赞帖子 {}", userId, postId);

        } else {
            // 点赞
            redisTemplate.opsForSet().add(likeSetKey, userId);
            redisTemplate.opsForSet().add(userLikesKey, postId);

            // 更新点赞数
            Long newCount = redisTemplate.opsForSet().size(likeSetKey);
            if (newCount != null) {
                redisTemplate.opsForValue().set(likeCountKey, newCount, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                response.setLikeCount(newCount);
            } else {
                response.setLikeCount(1L);
            }
            response.setIsLiked(true);

            log.info("用户 {} 点赞帖子 {}", userId, postId);
        }

        return response;
    }

    @Override
    public Long getLikeCount(Long postId) {
        String likeCountKey = LIKE_COUNT_PREFIX + postId;

        // 先从缓存获取
        Object cachedCount = redisTemplate.opsForValue().get(likeCountKey);
        if (cachedCount != null) {
            return Long.valueOf(cachedCount.toString());
        }

        // 缓存中没有，从Set计算
        String likeSetKey = LIKE_SET_PREFIX + postId;
        Long count = redisTemplate.opsForSet().size(likeSetKey);

        // 缓存结果
        if (count != null) {
            redisTemplate.opsForValue().set(likeCountKey, count, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        }

        return count != null ? count : 0L;
    }

    @Override
    public boolean isLiked(Long postId, Long userId) {
        String likeSetKey = LIKE_SET_PREFIX + postId;
        Boolean isMember = redisTemplate.opsForSet().isMember(likeSetKey, userId);
        return Boolean.TRUE.equals(isMember);
    }

    @Override
    public Long getLikeCountFromDatabase(Long postId) {
        // 从数据库获取真实点赞数
        var post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在: " + postId);
        }
        return post.getLikeCount().longValue();
    }

    @Override
    public void syncLikeCountsToDatabase(List<Long> postIds) {
        int successCount = 0;
        int skipCount = 0;

        for (Long postId : postIds) {
            try {
                // 验证帖子是否存在
                var post = postMapper.selectById(postId);
                if (post == null) {
                    log.warn("跳过不存在的帖子 {} 的同步", postId);
                    skipCount++;
                    continue;
                }

                // 获取Redis中的点赞用户列表
                String likeSetKey = LIKE_SET_PREFIX + postId;
                var userIds = redisTemplate.opsForSet().members(likeSetKey);

                if (userIds != null && !userIds.isEmpty()) {
                    // 删除现有的点赞记录
                    postLikeMapper.deleteByPostId(postId);

                    // 创建新的点赞记录
                    List<PostLike> likeRecords = userIds.stream()
                            .map(userId -> {
                                PostLike like = new PostLike();
                                like.setPostId(postId);
                                like.setUserId(Long.valueOf(userId.toString()));
                                like.setCreateTime(java.time.LocalDateTime.now());
                                return like;
                            })
                            .collect(java.util.stream.Collectors.toList());

                    // 批量插入点赞记录
                    if (!likeRecords.isEmpty()) {
                        postLikeMapper.insertBatch(likeRecords);
                    }

                    log.info("同步帖子 {} 的点赞记录: {} 条", postId, likeRecords.size());
                }

                // 同步点赞数到post表
                Long cacheCount = getLikeCount(postId);
                Long dbCount = post.getLikeCount().longValue();

                if (!cacheCount.equals(dbCount)) {
                    // 更新post表的点赞数
                    post.setLikeCount(cacheCount.intValue());
                    postMapper.updateById(post);

                    log.info("同步帖子 {} 的点赞数: 缓存={}, 数据库={}", postId, cacheCount, dbCount);
                }

                successCount++;

            } catch (Exception e) {
                log.error("同步帖子 {} 点赞数据失败", postId, e);
                throw new RuntimeException("同步帖子 " + postId + " 点赞数据失败: " + e.getMessage(), e);
            }
        }

        log.info("点赞数据同步完成: 成功同步 {} 个帖子，跳过 {} 个不存在的帖子", successCount, skipCount);
    }

    /**
     * 获取Redis缓存键
     */
    private String getLikeSetKey(Long postId) {
        return LIKE_SET_PREFIX + postId;
    }

    private String getLikeCountKey(Long postId) {
        return LIKE_COUNT_PREFIX + postId;
    }

    private String getUserLikesKey(Long userId) {
        return USER_LIKES_PREFIX + userId;
    }
}
