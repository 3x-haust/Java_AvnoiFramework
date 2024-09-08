package io.github._3xhaust.exmaple.post;

import io.github._3xhaust.annotations.Service;
import io.github._3xhaust.exmaple.post.dto.CreatePostDto;
import io.github._3xhaust.exmaple.post.entities.Post;

@Service
public class PostService {
    public Post test(CreatePostDto post) {
        return new Post(1, post.getTitle(), post.getContent());
    }
}
