package com.novaforum.nova_forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.novaforum.nova_forum.dto.CommentRequest;
import com.novaforum.nova_forum.dto.CommentResponse;
import com.novaforum.nova_forum.entity.Comment;
import com.novaforum.nova_forum.entity.Post;
import com.novaforum.nova_forum.entity.User;
import com.novaforum.nova_forum.mapper.CommentMapper;
import com.novaforum.nova_forum.mapper.PostMapper;
import com.novaforum.nova_forum.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * CommentServiceImpl 单元测试
 *
 * 测试覆盖：
 * - createComment 帖子/父评论校验、成功
 * - canEditComment 权限判断
 * - updateComment / deleteComment 权限校验与批量删除
 * - buildCommentTree 空值 / 单层 / 多层级 / 时间排序
 * - getCommentsByPostId 树构建与分页
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("评论服务单元测试")
class CommentServiceImplTest {

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment root;
    private Comment child;

    @BeforeEach
    void setUp() {
        root = new Comment();
        root.setId(1L);
        root.setPostId(100L);
        root.setUserId(10L);
        root.setParentId(null);
        root.setContent("root");
        root.setCreateTime(LocalDateTime.of(2025, 1, 1, 0, 0));

        child = new Comment();
        child.setId(2L);
        child.setPostId(100L);
        child.setUserId(20L);
        child.setParentId(1L);
        child.setContent("child");
        child.setCreateTime(LocalDateTime.of(2025, 1, 2, 0, 0));
    }

    // ==================== createComment ====================

    @Test
    @DisplayName("createComment - 帖子不存在抛出异常")
    void testCreateComment_PostNotFound() {
        when(postMapper.selectById(100L)).thenReturn(null);

        assertThatRuntimeException().isThrownBy(() -> commentService.createComment(req(100L, null), 10L))
                .withMessageContaining("帖子不存在");
    }

    @Test
    @DisplayName("createComment - 父评论不存在抛出异常")
    void testCreateComment_ParentNotFound() {
        Post post = new Post();
        post.setId(100L);
        when(postMapper.selectById(100L)).thenReturn(post);
        when(commentMapper.selectById(55L)).thenReturn(null);

        assertThatRuntimeException().isThrownBy(() -> commentService.createComment(req(100L, 55L), 10L))
                .withMessageContaining("父评论不存在");
    }

    @Test
    @DisplayName("createComment - 父评论不属于该帖子抛出异常")
    void testCreateComment_ParentWrongPost() {
        Post post = new Post();
        post.setId(100L);
        when(postMapper.selectById(100L)).thenReturn(post);
        Comment otherParent = new Comment();
        otherParent.setId(9L);
        otherParent.setPostId(999L);
        when(commentMapper.selectById(9L)).thenReturn(otherParent);

        assertThatRuntimeException().isThrownBy(() -> commentService.createComment(req(100L, 9L), 10L))
                .withMessageContaining("父评论不属于该帖子");
    }

    @Test
    @DisplayName("createComment - 成功创建并设置创建时间")
    void testCreateComment_Success() {
        Post post = new Post();
        post.setId(100L);
        when(postMapper.selectById(100L)).thenReturn(post);
        // MyBatis Plus insert 后会自动回填主键，用 doAnswer 模拟回填
        doAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(7L);
            return 1;
        }).when(commentMapper).insert(any(Comment.class));

        Long id = commentService.createComment(req(100L, null), 10L);

        assertThat(id).isEqualTo(7L);
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentMapper).insert(captor.capture());
        Comment saved = captor.getValue();
        assertThat(saved.getPostId()).isEqualTo(100L);
        assertThat(saved.getUserId()).isEqualTo(10L);
        assertThat(saved.getParentId()).isNull();
        assertThat(saved.getCreateTime()).isNotNull();
    }

    // ==================== canEditComment ====================

    @Test
    @DisplayName("canEditComment - 评论不存在返回false")
    void testCanEditComment_NotFound() {
        when(commentMapper.selectById(1L)).thenReturn(null);
        assertThat(commentService.canEditComment(1L, 10L)).isFalse();
    }

    @Test
    @DisplayName("canEditComment - 非作者返回false")
    void testCanEditComment_NotAuthor() {
        Comment c = new Comment();
        c.setId(1L);
        c.setUserId(99L);
        when(commentMapper.selectById(1L)).thenReturn(c);
        assertThat(commentService.canEditComment(1L, 10L)).isFalse();
    }

    @Test
    @DisplayName("canEditComment - 作者返回true")
    void testCanEditAuthor() {
        Comment c = new Comment();
        c.setId(1L);
        c.setUserId(10L);
        when(commentMapper.selectById(1L)).thenReturn(c);
        assertThat(commentService.canEditComment(1L, 10L)).isTrue();
    }

    // ==================== updateComment ====================

    @Test
    @DisplayName("updateComment - 非作者抛出异常")
    void testUpdateComment_NotAuthor() {
        Comment c = new Comment();
        c.setId(1L);
        c.setUserId(99L);
        when(commentMapper.selectById(1L)).thenReturn(c);

        assertThatRuntimeException().isThrownBy(() -> commentService.updateComment(1L, "x", 10L))
                .withMessageContaining("没有权限编辑该评论");
        verify(commentMapper, never()).updateById(any(Comment.class));
    }

    @Test
    @DisplayName("updateComment - 作者更新成功")
    void testUpdateComment_Success() {
        Comment c = new Comment();
        c.setId(1L);
        c.setUserId(10L);
        when(commentMapper.selectById(1L)).thenReturn(c);
        when(commentMapper.updateById(any(Comment.class))).thenReturn(1);

        assertThat(commentService.updateComment(1L, "updated", 10L)).isTrue();
        verify(commentMapper).updateById(any(Comment.class));
    }

    // ==================== deleteComment ====================

    @Test
    @DisplayName("deleteComment - 非作者抛出异常")
    void testDeleteComment_NotAuthor() {
        Comment c = new Comment();
        c.setId(1L);
        c.setUserId(99L);
        when(commentMapper.selectById(1L)).thenReturn(c);

        assertThatRuntimeException().isThrownBy(() -> commentService.deleteComment(1L, 10L))
                .withMessageContaining("没有权限删除该评论");
    }

    @Test
    @DisplayName("deleteComment - 作者删除：连同子评论批量删除")
    void testDeleteComment_RemovesChildren() {
        Comment c = new Comment();
        c.setId(1L);
        c.setUserId(10L);
        when(commentMapper.selectById(1L)).thenReturn(c);
        // 第一次查询(id=1)返回直接子评论child，第二次查询(id=2)返回空，避免无限递归
        when(commentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(child))
                .thenReturn(Collections.emptyList());
        when(commentMapper.deleteCommentsBatch(any(List.class))).thenReturn(2);

        assertThat(commentService.deleteComment(1L, 10L)).isTrue();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(commentMapper).deleteCommentsBatch(captor.capture());
        List<Long> ids = captor.getValue();
        assertThat(ids).containsExactlyInAnyOrder(1L, 2L);
    }

    // ==================== buildCommentTree ====================

    @Test
    @DisplayName("buildCommentTree - 空输入返回空列表")
    void testBuildTree_Empty() {
        assertThat(commentService.buildCommentTree(null)).isEmpty();
        assertThat(commentService.buildCommentTree(Collections.emptyList())).isEmpty();
    }

    @Test
    @DisplayName("buildCommentTree - 单条顶级评论")
    void testBuildTree_SingleRoot() {
        User user = new User();
        user.setUsername("alice");
        when(userMapper.selectById(10L)).thenReturn(user);
        when(commentMapper.selectReplyCountByParentId(1L)).thenReturn(0);

        List<CommentResponse> tree = commentService.buildCommentTree(Collections.singletonList(root));

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getUsername()).isEqualTo("alice");
        // 无子评论的顶级评论 replies 为 null（未初始化）
        assertThat(tree.get(0).getReplies()).isNull();
    }

    @Test
    @DisplayName("buildCommentTree - 多级结构：子评论挂到父评论下")
    void testBuildTree_Nested() {
        User rootUser = new User();
        rootUser.setUsername("alice");
        User childUser = new User();
        childUser.setUsername("bob");
        when(userMapper.selectById(10L)).thenReturn(rootUser);
        when(userMapper.selectById(20L)).thenReturn(childUser);
        when(commentMapper.selectReplyCountByParentId(1L)).thenReturn(1);
        when(commentMapper.selectReplyCountByParentId(2L)).thenReturn(0);

        List<CommentResponse> tree = commentService.buildCommentTree(Arrays.asList(root, child));

        assertThat(tree).hasSize(1);
        CommentResponse rootResp = tree.get(0);
        assertThat(rootResp.getUsername()).isEqualTo("alice");
        assertThat(rootResp.getReplies()).hasSize(1);
        assertThat(rootResp.getReplies().get(0).getUsername()).isEqualTo("bob");
        assertThat(rootResp.getReplies().get(0).getParentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("buildCommentTree - 按创建时间升序排列")
    void testBuildTree_SortedByCreateTime() {
        User user = new User();
        user.setUsername("alice");
        when(userMapper.selectById(10L)).thenReturn(user);
        when(commentMapper.selectReplyCountByParentId(anyLong())).thenReturn(0);

        // earlyRoot(id=3) 创建时间比 root(id=1) 更早，应排在前面
        Comment earlyRoot = new Comment();
        earlyRoot.setId(3L);
        earlyRoot.setPostId(100L);
        earlyRoot.setUserId(10L);
        earlyRoot.setParentId(null);
        earlyRoot.setCreateTime(LocalDateTime.of(2024, 12, 31, 0, 0));

        List<CommentResponse> tree = commentService.buildCommentTree(
                Arrays.asList(root, earlyRoot));

        assertThat(tree).hasSize(2);
        assertThat(tree.get(0).getId()).isEqualTo(3L); // 更早的时间排在前面
        assertThat(tree.get(1).getId()).isEqualTo(1L);
    }

    // ==================== getCommentsByPostId ====================

    @Test
    @DisplayName("getCommentsByPostId - 构建树并分页")
    void testGetCommentsByPostId_Paginates() {
        User user = new User();
        user.setUsername("alice");
        when(userMapper.selectById(10L)).thenReturn(user);
        when(commentMapper.selectCommentsByPostId(100L)).thenReturn(Collections.singletonList(root));
        when(commentMapper.selectReplyCountByParentId(1L)).thenReturn(0);

        var page = commentService.getCommentsByPostId(100L, 1, 10);

        assertThat(page.getRecords()).hasSize(1);
        assertThat(page.getTotal()).isEqualTo(1);
    }

    // ==================== 辅助方法 ====================

    private CommentRequest req(Long postId, Long parentId) {
        CommentRequest request = new CommentRequest();
        request.setPostId(postId);
        request.setParentId(parentId);
        request.setContent("评论内容");
        return request;
    }
}
