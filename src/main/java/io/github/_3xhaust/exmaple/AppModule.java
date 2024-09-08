package io.github._3xhaust.exmaple;

import io.github._3xhaust.annotations.Module;
import io.github._3xhaust.exmaple.post.PostModule;

@Module(
        imports = {PostModule.class}
)
public class AppModule {
}
