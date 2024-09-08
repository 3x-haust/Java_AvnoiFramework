package io.github._3xhaust.exmaple.post;

import io.github._3xhaust.annotations.Service;
import io.github._3xhaust.exmaple.post.entities.Post;

@Service
public class PostService {
    public Post test() {
        return new Post(1, "Hello, World!", "This is a test post.");
    }
}
