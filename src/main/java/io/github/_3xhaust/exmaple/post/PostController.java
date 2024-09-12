package io.github._3xhaust.exmaple.post;

import io.github._3xhaust.annotations.Controller;
import io.github._3xhaust.annotations.Inject;
import io.github._3xhaust.annotations.Query;
import io.github._3xhaust.annotations.types.Body;
import io.github._3xhaust.annotations.types.Get;
import io.github._3xhaust.annotations.types.Param;
import io.github._3xhaust.exmaple.post.dto.CreatePostDto;
import io.github._3xhaust.exmaple.post.entities.Post;

import java.util.List;
import java.util.Map;

@Controller("/api/posts")
public class PostController {
    private final PostService postService;

    @Inject
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @io.github._3xhaust.annotations.types.Post("create")
    public Object create(@Body CreatePostDto post) {
        return postService.create(post);
    }

    @Get("findById")
    public Object findById(@Query("id") Long id) {
        return postService.findById(id);
    }

    @Get("findAll")
    public Object findAll() {
        return postService.findAll();
    }

    @Get("findByTitle")
    public Object findByTitle(@Query("title") String title) {
        return postService.findByTitle(title);
    }

}
