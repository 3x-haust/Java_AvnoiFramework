package io.github._3xhaust.exmaple.orm.post;

import io.github._3xhaust.annotations.Module;

@Module(
        controllers = {PostController.class},
        providers = {PostService.class}
)
public class PostModule {
}
