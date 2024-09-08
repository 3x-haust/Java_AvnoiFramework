package io.github._3xhaust.exmaple.post.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Post {
    private final int id;
    private final String title;
    private final String content;

    public Post(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

}
