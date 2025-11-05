package com.novaforum.nova_forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaforum.nova_forum.dto.CommentRequest;
import com.novaforum.nova_forum.dto.CommentResponse;
import com.novaforum.nova_forum.entity.Comment;
import com.novaforum.nova_forum.entity.Post;
import com.novaforum.nova_forum.entity.User;
import com.novaforum.nova_forum.mapper.CommentMapper;
import com.novaforum.nova_forum.mapper.PostMapper;
import com.novaforum.nova_forum.mapper.UserMapper;
import com.novaforum.nova_forum.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 * 支持多层级评论结构和权限控制
 *
 * @author Nova Forum Team
 * @since 2025-11-05
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    public CommentServiceImpl(CommentMapper commentMapper, PostMapper postMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(CommentRequest request, Long userId) {
        // 验证帖子是否存在
        Post post = postMapper.selectById(request.getPostId());
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 验证父评论（如果是回复）
        if (request.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null) {
                throw new RuntimeException("父评论不存在");
            }
            if (!parentComment.getPostId().equals(request.getPostId())) {
                throw new RuntimeException("父评论不属于该帖子");
            }
        }

        Comment comment = new Comment();
        comment.setPostId(request.getPostId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());

        commentMapper.insert(comment);
        
        log.info("创建评论成功，评论ID: {}, 用户ID: {}", comment.getId(), userId);
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateComment(Long commentId, String content, Long userId) {
        // 验证权限
        if (!canEditComment(commentId, userId)) {
            throw new RuntimeException("没有权限编辑该评论");
        }

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setContent(content);

        int result = commentMapper.updateById(comment);
        if (result > 0) {
            log.info("更新评论成功，评论ID: {}", commentId);
        }
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long commentId, Long userId) {
        // 验证权限
        if (!canEditComment(commentId, userId)) {
            throw new RuntimeException("没有权限删除该评论");
        }

        // 查询所有子评论
        List<Comment> allSubComments = getAllSubComments(commentId);
        List<Long> commentIds = allSubComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        commentIds.add(commentId); // 包含自己

        // 批量删除
        int result = commentMapper.deleteCommentsBatch(commentIds);
        if (result > 0) {
            log.info("删除评论成功，评论ID: {}, 包含子评论总数: {}", commentId, commentIds.size());
        }
        return result > 0;
    }

    @Override
    public Comment getCommentDetail(Long id) {
        return commentMapper.selectCommentWithUserById(id);
    }

    @Override
    public IPage<CommentResponse> getCommentsByPostId(Long postId, Integer pageNum, Integer pageSize) {
        // 获取所有评论
        List<Comment> allComments = commentMapper.selectCommentsByPostId(postId);
        
        // 构建树形结构
        List<CommentResponse> commentTree = buildCommentTree(allComments);
        
        // 分页处理（简化实现，实际应该在前端或服务层分页）
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, commentTree.size());
        
        Page<CommentResponse> page = new Page<>(pageNum, pageSize);
        if (start < commentTree.size()) {
            List<CommentResponse> pageData = commentTree.subList(start, end);
            page.setRecords(pageData);
            page.setTotal(commentTree.size());
        }
        
        return page;
    }

    @Override
    public IPage<Comment> getCommentsByUserId(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getUserId, userId)
                .orderByDesc(Comment::getCreateTime);
        
        return commentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public IPage<CommentResponse> getRepliesByCommentId(Long parentId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, parentId)
                .orderByAsc(Comment::getCreateTime);
        
        IPage<Comment> commentPage = commentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        
        // 转换为Response对象
        Page<CommentResponse> responsePage = new Page<>(pageNum, pageSize, commentPage.getTotal());
        List<CommentResponse> responses = commentPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responses);
        
        return responsePage;
    }

    @Override
    public Integer getCommentCountByPostId(Long postId) {
        return commentMapper.selectCommentCountByPostId(postId);
    }

    @Override
    public Integer getCommentCountByUserId(Long userId) {
        return commentMapper.selectCommentCountByUserId(userId);
    }

    @Override
    public Integer getReplyCountByCommentId(Long commentId) {
        return commentMapper.selectReplyCountByParentId(commentId);
    }

    @Override
    public boolean canEditComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return false;
        }
        return comment.getUserId().equals(userId);
    }

    @Override
    public List<CommentResponse> buildCommentTree(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, CommentResponse> commentMap = new HashMap<>();
        List<CommentResponse> rootComments = new ArrayList<>();

        // 创建所有评论的Response对象
        for (Comment comment : comments) {
            CommentResponse response = convertToResponse(comment);
            commentMap.put(comment.getId(), response);
        }

        // 构建父子关系
        for (Comment comment : comments) {
            CommentResponse response = commentMap.get(comment.getId());
            if (comment.getParentId() == null) {
                // 顶级评论
                rootComments.add(response);
            } else {
                // 子评论
                CommentResponse parentResponse = commentMap.get(comment.getParentId());
                if (parentResponse != null) {
                    if (parentResponse.getReplies() == null) {
                        parentResponse.setReplies(new ArrayList<>());
                    }
                    parentResponse.getReplies().add(response);
                }
            }
        }

        // 按创建时间排序
        rootComments.sort((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()));
        
        // 递归排序子评论
        sortRepliesRecursively(rootComments);

        return rootComments;
    }

    /**
     * 递归排序子评论
     */
    private void sortRepliesRecursively(List<CommentResponse> comments) {
        for (CommentResponse comment : comments) {
            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                comment.getReplies().sort((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()));
                sortRepliesRecursively(comment.getReplies());
            }
        }
    }

    /**
     * 获取所有子评论（包括孙子评论等）
     */
    private List<Comment> getAllSubComments(Long parentId) {
        List<Comment> allSubComments = new ArrayList<>();
        List<Comment> directSubComments = commentMapper.selectList(
            new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, parentId)
        );
        
        for (Comment subComment : directSubComments) {
            allSubComments.add(subComment);
            allSubComments.addAll(getAllSubComments(subComment.getId()));
        }
        
        return allSubComments;
    }

    /**
     * 将Comment转换为CommentResponse
     */
    private CommentResponse convertToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        BeanUtils.copyProperties(comment, response);
        
        // 设置用户名
        if (comment.getUserId() != null) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                response.setUsername(user.getUsername());
            }
        }
        
        // 设置父评论内容
        if (comment.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(comment.getParentId());
            if (parentComment != null) {
                response.setParentContent(parentComment.getContent());
            }
        }
        
        // 设置回复数量
        response.setReplyCount(getReplyCountByCommentId(comment.getId()));
        
        return response;
    }
}
