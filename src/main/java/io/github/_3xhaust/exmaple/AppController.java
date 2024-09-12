package io.github._3xhaust.exmaple;

import io.github._3xhaust.annotations.Controller;
import io.github._3xhaust.annotations.Inject;
import io.github._3xhaust.annotations.types.Get;

@Controller
public class AppController {
    private final AppService appService;

    @Inject
    public AppController(AppService appService) {
        this.appService = appService;
    }

    @Get()
    public String getHello() {
        return appService.getHello();
    }
}
