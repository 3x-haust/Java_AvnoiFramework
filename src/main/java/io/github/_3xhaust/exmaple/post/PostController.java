package io.github._3xhaust.exmaple.post;

import io.github._3xhaust.annotations.Controller;
import io.github._3xhaust.annotations.Inject;
import io.github._3xhaust.annotations.types.Get;
import io.github._3xhaust.exmaple.post.entities.Post;

@Controller("/api/posts")
public class PostController {
    private final PostService postService;

    @Inject
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Get("test")
    public Post test() {
        return postService.test();
    }
}
