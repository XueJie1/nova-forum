package com.novaforum.nova_forum.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaforum.nova_forum.dto.*;
import com.novaforum.nova_forum.entity.Post;
import com.novaforum.nova_forum.service.PostService;
import com.novaforum.nova_forum.util.JwtUtil;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * 帖子控制器
 */
@RestController
@RequestMapping("/post")
@Validated
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 发布帖子
     */
    @PostMapping("/create")
    public ApiResponse<String> createPost(@Valid @RequestBody PostRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证JWT令牌
            String token = validateToken(authHeader);
            if (token == null) {
                return ApiResponse.error(401, "无效的授权头");
            }

            // 获取用户ID
            Long userId = jwtUtil.extractUserId(token);
            if (userId == null) {
                return ApiResponse.error(401, "无法从令牌中获取用户信息");
            }

            // 转换DTO为实体
            Post post = new Post();
            BeanUtils.copyProperties(request, post);

            // 创建帖子
            Long postId = postService.createPost(post, userId);

            return ApiResponse.success("帖子发布成功", "帖子ID: " + postId);

        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("发布帖子异常：" + e.getMessage());
        }
    }

    /**
     * 更新帖子
     */
    @PutMapping("/{id}")
    public ApiResponse<String> updatePost(@PathVariable @NotNull @Min(1) Long id,
            @Valid @RequestBody PostRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证JWT令牌
            String token = validateToken(authHeader);
            if (token == null) {
                return ApiResponse.error(401, "无效的授权头");
            }

            // 获取用户ID
            Long userId = jwtUtil.extractUserId(token);
            if (userId == null) {
                return ApiResponse.error(401, "无法从令牌中获取用户信息");
            }

            // 转换DTO为实体
            Post post = new Post();
            post.setId(id);
            BeanUtils.copyProperties(request, post);

            // 更新帖子
            boolean success = postService.updatePost(post, userId);

            if (success) {
                return ApiResponse.success("帖子更新成功");
            } else {
                return ApiResponse.error(400, "帖子更新失败");
            }

        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (SecurityException e) {
            return ApiResponse.error(403, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("更新帖子异常：" + e.getMessage());
        }
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deletePost(@PathVariable @NotNull @Min(1) Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证JWT令牌
            String token = validateToken(authHeader);
            if (token == null) {
                return ApiResponse.error(401, "无效的授权头");
            }

            // 获取用户ID
            Long userId = jwtUtil.extractUserId(token);
            if (userId == null) {
                return ApiResponse.error(401, "无法从令牌中获取用户信息");
            }

            // 删除帖子
            boolean success = postService.deletePost(id, userId);

            if (success) {
                return ApiResponse.success("帖子删除成功");
            } else {
                return ApiResponse.error(400, "帖子删除失败");
            }

        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (SecurityException e) {
            return ApiResponse.error(403, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("删除帖子异常：" + e.getMessage());
        }
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{id}")
    public ApiResponse<PostResponse> getPostDetail(@PathVariable @NotNull @Min(1) Long id) {
        try {
            // 获取帖子详情
            Post post = postService.getPostDetail(id);

            // 转换实体为DTO
            PostResponse response = convertToResponse(post);

            return ApiResponse.success(response);

        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取帖子详情异常：" + e.getMessage());
        }
    }

    /**
     * 获取帖子列表（分页）
     */
    @GetMapping("/list")
    public ApiResponse<IPage<PostResponse>> getPostList(
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(1) Integer pageSize,
            @RequestParam(required = false) Long userId) {
        try {
            // 获取帖子列表
            IPage<Post> postPage = postService.getPostList(pageNum, pageSize, userId);

            // 转换分页结果
            IPage<PostResponse> responsePage = convertToResponsePage(postPage);

            return ApiResponse.success(responsePage);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取帖子列表异常：" + e.getMessage());
        }
    }

    /**
     * 验证JWT令牌
     */
    private String validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return null;
        }

        return token;
    }

    /**
     * 将Post实体转换为PostResponse DTO
     */
    private PostResponse convertToResponse(Post post) {
        PostResponse response = new PostResponse();
        BeanUtils.copyProperties(post, response);
        // TODO: 设置作者用户名，需要关联查询
        return response;
    }

    /**
     * 将Post分页结果转换为PostResponse分页结果
     */
    private IPage<PostResponse> convertToResponsePage(IPage<Post> postPage) {
        // 创建新的分页对象
        Page<PostResponse> responsePage = new Page<>(
                postPage.getCurrent(),
                postPage.getSize(),
                postPage.getTotal());
        
        // 创建新的列表来存储转换后的数据
        List<PostResponse> responseList = new ArrayList<>();
        
        // 转换记录列表
        for (Post post : postPage.getRecords()) {
            responseList.add(convertToResponse(post));
        }
        
        // 设置转换后的记录列表
        responsePage.setRecords(responseList);

        return responsePage;
    }
}
