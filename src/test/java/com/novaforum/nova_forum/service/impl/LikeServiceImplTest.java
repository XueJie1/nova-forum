package com.novaforum.nova_forum.service.impl;

import com.novaforum.nova_forum.dto.LikeResponse;
import com.novaforum.nova_forum.entity.Post;
import com.novaforum.nova_forum.entity.PostLike;
import com.novaforum.nova_forum.mapper.PostLikeMapper;
import com.novaforum.nova_forum.mapper.PostMapper;
import com.novaforum.nova_forum.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * LikeServiceImpl 单元测试
 *
 * 测试覆盖：
 * - toggleLike 帖子不存在 / 点赞 / 取消点赞 / 计数为 null 的降级
 * - getLikeCount 缓存命中 / 缓存未命中回写 / 全 null
 * - isLiked
 * - getLikeCountFromDatabase 存在 / 不存在
 * - syncLikeCountsToDatabase 跳过不存在 / 同步记录与点赞数
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("点赞服务单元测试")
class LikeServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostLikeMapper postLikeMapper;

    @Mock
    private SetOperations<String, Object> setOps;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private LikeServiceImpl likeService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // ==================== toggleLike ====================

    @Test
    @DisplayName("toggleLike - 帖子不存在抛出异常")
    void testToggleLike_PostNotFound() {
        when(postMapper.selectById(1L)).thenReturn(null);

        assertThatRuntimeException().isThrownBy(() -> likeService.toggleLike(1L, 10L))
                .withMessageContaining("帖子不存在");
        verifyNoInteractions(setOps);
    }

    @Test
    @DisplayName("toggleLike - 点赞：加入集合并记录点赞数")
    void testToggleLike_Like() {
        Post post = new Post();
        post.setId(1L);
        when(postMapper.selectById(1L)).thenReturn(post);
        when(setOps.isMember(anyString(), any(Object.class))).thenReturn(false);
        doReturn(1L).when(setOps).add(anyString(), any());
        doReturn(1L).when(setOps).add(anyString(), any());
        when(setOps.size(anyString())).thenReturn(1L);
        doNothing().when(valueOps).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        LikeResponse resp = likeService.toggleLike(1L, 10L);

        assertThat(resp.getPostId()).isEqualTo(1L);
        assertThat(resp.getIsLiked()).isTrue();
        assertThat(resp.getLikeCount()).isEqualTo(1L);
        verify(setOps, times(2)).add(anyString(), any());
    }

    @Test
    @DisplayName("toggleLike - 取消点赞：从集合移除并记录点赞数")
    void testToggleLike_Unlike() {
        Post post = new Post();
        post.setId(1L);
        when(postMapper.selectById(1L)).thenReturn(post);
        when(setOps.isMember(anyString(), any(Object.class))).thenReturn(true);
        doReturn(1L).when(setOps).remove(anyString(), any());
        when(setOps.size(anyString())).thenReturn(0L);
        doNothing().when(valueOps).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        LikeResponse resp = likeService.toggleLike(1L, 10L);

        assertThat(resp.getIsLiked()).isFalse();
        assertThat(resp.getLikeCount()).isEqualTo(0L);
        verify(setOps, times(2)).remove(anyString(), any());
    }

    @Test
    @DisplayName("toggleLike - 计数为 null 时点赞数降级为 1")
    void testToggleLike_CountNull_Like() {
        Post post = new Post();
        post.setId(1L);
        when(postMapper.selectById(1L)).thenReturn(post);
        when(setOps.isMember(anyString(), any(Object.class))).thenReturn(false);
        doReturn(1L).when(setOps).add(anyString(), any());
        doReturn(1L).when(setOps).add(anyString(), any());
        when(setOps.size(anyString())).thenReturn(null);

        LikeResponse resp = likeService.toggleLike(1L, 10L);

        assertThat(resp.getIsLiked()).isTrue();
        assertThat(resp.getLikeCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("toggleLike - 计数为 null 时取消点赞数降级为 0")
    void testToggleLike_CountNull_Unlike() {
        Post post = new Post();
        post.setId(1L);
        when(postMapper.selectById(1L)).thenReturn(post);
        when(setOps.isMember(anyString(), any(Object.class))).thenReturn(true);
        doReturn(1L).when(setOps).remove(anyString(), any());
        doReturn(1L).when(setOps).remove(anyString(), any());
        when(setOps.size(anyString())).thenReturn(null);

        LikeResponse resp = likeService.toggleLike(1L, 10L);

        assertThat(resp.getIsLiked()).isFalse();
        assertThat(resp.getLikeCount()).isEqualTo(0L);
    }

    // ==================== getLikeCount ====================

    @Test
    @DisplayName("getLikeCount - 缓存命中直接返回")
    void testGetLikeCount_CacheHit() {
        when(valueOps.get(anyString())).thenReturn("42");

        Long count = likeService.getLikeCount(1L);

        assertThat(count).isEqualTo(42L);
        verifyNoInteractions(setOps);
    }

    @Test
    @DisplayName("getLikeCount - 缓存未命中从 Set 计算并回写")
    void testGetLikeCount_CacheMiss() {
        when(valueOps.get(anyString())).thenReturn(null);
        when(setOps.size(anyString())).thenReturn(7L);
        doNothing().when(valueOps).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        Long count = likeService.getLikeCount(1L);

        assertThat(count).isEqualTo(7L);
        verify(valueOps, times(1)).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("getLikeCount - 缓存与 Set 均为 null 返回 0")
    void testGetLikeCount_AllNull() {
        when(valueOps.get(anyString())).thenReturn(null);
        when(setOps.size(anyString())).thenReturn(null);

        Long count = likeService.getLikeCount(1L);

        assertThat(count).isEqualTo(0L);
    }

    // ==================== isLiked ====================

    @Test
    @DisplayName("isLiked - 已点赞")
    void testIsLiked_True() {
        when(setOps.isMember(anyString(), any(Object.class))).thenReturn(true);

        assertThat(likeService.isLiked(1L, 10L)).isTrue();
    }

    @Test
    @DisplayName("isLiked - 未点赞")
    void testIsLiked_False() {
        when(setOps.isMember(anyString(), any(Object.class))).thenReturn(false);

        assertThat(likeService.isLiked(1L, 10L)).isFalse();
    }

    // ==================== getLikeCountFromDatabase ====================

    @Test
    @DisplayName("getLikeCountFromDatabase - 返回数据库点赞数")
    void testGetLikeCountFromDatabase_Found() {
        Post post = new Post();
        post.setId(1L);
        post.setLikeCount(99);
        when(postMapper.selectById(1L)).thenReturn(post);

        assertThat(likeService.getLikeCountFromDatabase(1L)).isEqualTo(99L);
    }

    @Test
    @DisplayName("getLikeCountFromDatabase - 帖子不存在抛出异常")
    void testGetLikeCountFromDatabase_NotFound() {
        when(postMapper.selectById(1L)).thenReturn(null);

        assertThatRuntimeException().isThrownBy(() -> likeService.getLikeCountFromDatabase(1L))
                .withMessageContaining("帖子不存在");
    }

    // ==================== syncLikeCountsToDatabase ====================

    @Test
    @DisplayName("syncLikeCountsToDatabase - 跳过不存在的帖子")
    void testSync_SkipMissing() {
        when(postMapper.selectById(1L)).thenReturn(null);

        likeService.syncLikeCountsToDatabase(Arrays.asList(1L));

        verifyNoInteractions(postLikeMapper);
    }

    @Test
    @DisplayName("syncLikeCountsToDatabase - 同步点赞记录并更新点赞数")
    void testSync_SyncsRecordsAndCount() {
        Post post = new Post();
        post.setId(1L);
        post.setLikeCount(0);
        when(postMapper.selectById(1L)).thenReturn(post);
        when(setOps.members(anyString())).thenReturn(new HashSet<>(Arrays.asList("10", "20")));
        when(postLikeMapper.deleteByPostId(1L)).thenReturn(2);
        doReturn(1).when(postLikeMapper).insertBatch(any(List.class));
        when(setOps.size(anyString())).thenReturn(2L);
        doNothing().when(valueOps).set(anyString(), any(), anyLong(), any(TimeUnit.class));
        when(postMapper.updateById(any(Post.class))).thenReturn(1);

        likeService.syncLikeCountsToDatabase(Arrays.asList(1L));

        verify(postLikeMapper, times(1)).deleteByPostId(1L);
        verify(postLikeMapper, times(1)).insertBatch(any(List.class));
        // 缓存数(2) 与数据库数(0) 不一致，需更新 post 表
        verify(postMapper, times(1)).updateById(any(Post.class));
    }

    @Test
    @DisplayName("syncLikeCountsToDatabase - 无点赞用户时不插入记录")
    void testSync_NoMembers() {
        Post post = new Post();
        post.setId(1L);
        post.setLikeCount(5);
        when(postMapper.selectById(1L)).thenReturn(post);
        when(setOps.members(anyString())).thenReturn(Collections.emptySet());
        when(setOps.size(anyString())).thenReturn(0L);
        doNothing().when(valueOps).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        likeService.syncLikeCountsToDatabase(Arrays.asList(1L));

        verifyNoInteractions(postLikeMapper);
        // 缓存数(0) 与数据库数(5) 不一致，仍更新
        verify(postMapper, times(1)).updateById(any(Post.class));
    }
}
