package io.github._3xhaust.exmaple;

import io.github._3xhaust.annotations.Module;
import io.github._3xhaust.exmaple.post.PostModule;
import io.github._3xhaust.orm.AvnoiOrmModule;

import static io.github._3xhaust.orm.DataSourceOptions.mysql;
import static io.github._3xhaust.orm.DataSourceOptions.sqlite;

@Module(
        imports = {
                PostModule.class,
                AvnoiOrmModule.class
        },
        controllers = {
                AppController.class
        },
        providers = {
                AppService.class
        }
)
public class AppModule {
         static {
                 AvnoiOrmModule.forRoot(
                        sqlite("blog.db", true)
                );
         }
}
