package io.github._3xhaust.post;

import io.github._3xhaust.annotations.Module;
import io.github._3xhaust.exmaple.post.PostController;
import io.github._3xhaust.exmaple.post.PostService;

@Module(
        controllers = {PostController.class},
        providers = {PostService.class}
)
public class PostModule {
}
