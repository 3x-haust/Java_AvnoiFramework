package io.github._3xhaust.exmaple.post;

import io.github._3xhaust.annotations.Controller;
import io.github._3xhaust.annotations.Inject;
import io.github._3xhaust.annotations.types.Body;
import io.github._3xhaust.annotations.types.Get;
import io.github._3xhaust.exmaple.post.dto.CreatePostDto;
import io.github._3xhaust.exmaple.post.entities.Post;

@Controller("/api/posts")
public class PostController {
    private final PostService postService;

    @Inject
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @io.github._3xhaust.annotations.types.Post("test")
    public Post test(@Body CreatePostDto post) {
        return postService.test(post);
    }
}
