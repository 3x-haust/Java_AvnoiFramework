package io.github._3xhaust.exmaple.orm.post;

import io.github._3xhaust.annotations.Controller;
import io.github._3xhaust.annotations.Inject;
import io.github._3xhaust.annotations.http.Post;
import io.github._3xhaust.annotations.types.Body;
import io.github._3xhaust.annotations.http.Get;
import io.github._3xhaust.annotations.http.Param;
import io.github._3xhaust.exmaple.orm.post.dto.CreatePostDto;

import java.util.concurrent.CompletableFuture;

@Controller("api/posts")
public class PostController {
    private final io.github._3xhaust.exmaple.orm.post.PostService postService;

    @Inject
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Post("create")
    public CompletableFuture<Object> create(@Body CreatePostDto post)  {
        long startTime = System.currentTimeMillis();
        CompletableFuture<Object> result = this.postService.create(post);
        long endTime = System.currentTimeMillis();
        System.out.println("create() 메서드 실행 시간: " + (endTime - startTime) + "ms");
        return result;
    }

    @Get("findById")
    public Object findById(@Param("id") Long id) {
        return postService.findById(id);
    }

    @Get("findAll")
    public Object findAll() {
        return postService.findAll();
    }


    @Get("findByTitle")
    public Object findByTitle(@Param("title") String title) {
        return postService.findByTitle(title);
    }

}
