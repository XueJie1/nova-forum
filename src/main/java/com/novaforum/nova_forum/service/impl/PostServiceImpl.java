package com.novaforum.nova_forum.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaforum.nova_forum.entity.Post;
import com.novaforum.nova_forum.mapper.PostMapper;
import com.novaforum.nova_forum.service.PostService;

import java.time.LocalDateTime;

/**
 * 帖子服务实现类
 */
@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private com.novaforum.nova_forum.service.UserService userService;

    @Override
    @Transactional
    public Long createPost(Post post, Long userId) {
        // 参数验证
        if (post == null || !StringUtils.hasText(post.getTitle()) ||
                !StringUtils.hasText(post.getContent()) || userId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        try {
            // 设置帖子信息
            post.setUserId(userId);
            post.setViewCount(0); // 初始化浏览次数为0
            post.setLikeCount(0); // 初始化点赞次数为0
            post.setCreateTime(LocalDateTime.now());
            post.setUpdateTime(LocalDateTime.now());

            // 保存帖子
            int result = postMapper.insert(post);

            if (result > 0) {
                return post.getId(); // 返回创建的帖子ID
            } else {
                throw new RuntimeException("创建帖子失败");
            }

        } catch (Exception e) {
            throw new RuntimeException("创建帖子异常：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean updatePost(Post post, Long userId) {
        // 参数验证
        if (post == null || post.getId() == null || !StringUtils.hasText(post.getTitle()) ||
                !StringUtils.hasText(post.getContent()) || userId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        try {
            // 检查帖子是否存在
            Post existingPost = postMapper.selectById(post.getId());
            if (existingPost == null) {
                throw new IllegalArgumentException("帖子不存在");
            }

            // 检查是否为帖子作者
            if (!existingPost.getUserId().equals(userId)) {
                throw new SecurityException("只能编辑自己的帖子");
            }

            // 更新帖子信息
            post.setUserId(existingPost.getUserId()); // 保持原作者ID
            post.setViewCount(existingPost.getViewCount()); // 保持原浏览次数
            post.setLikeCount(existingPost.getLikeCount()); // 保持原点赞次数
            post.setUpdateTime(LocalDateTime.now());

            int result = postMapper.updateById(post);

            return result > 0;

        } catch (Exception e) {
            throw new RuntimeException("更新帖子异常：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deletePost(Long postId, Long userId) {
        // 参数验证
        if (postId == null || userId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        try {
            // 检查帖子是否存在
            Post existingPost = postMapper.selectById(postId);
            if (existingPost == null) {
                throw new IllegalArgumentException("帖子不存在");
            }

            // 检查是否为帖子作者
            if (!existingPost.getUserId().equals(userId)) {
                throw new SecurityException("只能删除自己的帖子");
            }

            int result = postMapper.deleteById(postId);

            return result > 0;

        } catch (Exception e) {
            throw new RuntimeException("删除帖子异常：" + e.getMessage());
        }
    }

    @Override
    public Post getPostDetail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("帖子ID不能为空");
        }

        try {
            Post post = postMapper.selectPostWithAuthorById(id);
            if (post == null) {
                throw new IllegalArgumentException("帖子不存在");
            }

            // 增加浏览次数
            incrementViewCount(id);

            return post;

        } catch (Exception e) {
            throw new RuntimeException("获取帖子详情异常：" + e.getMessage());
        }
    }

    @Override
    public IPage<Post> getPostList(Integer pageNum, Integer pageSize, Long userId) {
        // 参数验证
        if (pageNum == null || pageNum < 1) {
            pageNum = 1; // 默认第一页
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10; // 默认每页10条，最大100条
        }

        try {
            Page<Post> page = new Page<>(pageNum, pageSize);
            // 按创建时间倒序排列
            page.addOrder(OrderItem.desc("create_time"));

            return postMapper.selectPostsPage(page, userId);

        } catch (Exception e) {
            throw new RuntimeException("获取帖子列表异常：" + e.getMessage());
        }
    }

    @Override
    public void incrementViewCount(Long id) {
        if (id == null) {
            return;
        }
        try {
            postMapper.incrementViewCount(id);
        } catch (Exception e) {
            // 浏览次数更新失败不影响主要功能，记录日志即可
            e.printStackTrace();
        }
    }

    @Override
    public void incrementLikeCount(Long id) {
        if (id == null) {
            return;
        }
        try {
            postMapper.incrementLikeCount(id);
        } catch (Exception e) {
            // 点赞次数更新失败不影响主要功能，记录日志即可
            e.printStackTrace();
        }
    }

    @Override
    public void decrementLikeCount(Long id) {
        if (id == null) {
            return;
        }
        try {
            postMapper.decrementLikeCount(id);
        } catch (Exception e) {
            // 点赞次数更新失败不影响主要功能，记录日志即可
            e.printStackTrace();
        }
    }
}
