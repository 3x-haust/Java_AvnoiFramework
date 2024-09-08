package io.github._3xhaust.exmaple.post.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostDto {
    private String title;
    private String content;

    public CreatePostDto() {}
}
