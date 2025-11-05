package com.novaforum.nova_forum.service.impl;

import com.novaforum.nova_forum.dto.LikeResponse;
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

    public LikeServiceImpl(RedisTemplate<String, Object> redisTemplate, PostMapper postMapper) {
        this.redisTemplate = redisTemplate;
        this.postMapper = postMapper;
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
        return postMapper.selectById(postId).getLikeCount().longValue();
    }

    @Override
    public void syncLikeCountsToDatabase(List<Long> postIds) {
        for (Long postId : postIds) {
            try {
                Long cacheCount = getLikeCount(postId);
                Long dbCount = getLikeCountFromDatabase(postId);
                
                if (!cacheCount.equals(dbCount)) {
                    // 同步到数据库
                    if (cacheCount > dbCount) {
                        // 增加点赞数
                        int diff = (int) (cacheCount - dbCount);
                        for (int i = 0; i < diff; i++) {
                            postMapper.incrementLikeCount(postId);
                        }
                    } else {
                        // 减少点赞数
                        int diff = (int) (dbCount - cacheCount);
                        for (int i = 0; i < diff; i++) {
                            postMapper.decrementLikeCount(postId);
                        }
                    }
                    
                    log.info("同步帖子 {} 的点赞数: 缓存={}, 数据库={}", postId, cacheCount, dbCount);
                }
                
            } catch (Exception e) {
                log.error("同步帖子 {} 点赞数失败", postId, e);
            }
        }
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
