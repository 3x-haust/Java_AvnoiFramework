package io.github._3xhaust.exmaple.orm;

import io.github._3xhaust.annotations.Module;
import io.github._3xhaust.exmaple.orm.post.PostModule;
import io.github._3xhaust.orm.AvnoiOrmModule;

import static io.github._3xhaust.orm.DataSourceOptions.sqlite;

@Module(
        imports = {
                PostModule.class,
                AvnoiOrmModule.class
        }
)
public class AppModule {
    static {
        AvnoiOrmModule.forRoot(
                sqlite("blog.db", true)
        );
    }
}
