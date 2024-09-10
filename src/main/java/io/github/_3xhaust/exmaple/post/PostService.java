package io.github._3xhaust.exmaple.post;

import io.github._3xhaust.annotations.Inject;
import io.github._3xhaust.annotations.Service;
import io.github._3xhaust.exmaple.post.dto.CreatePostDto;
import io.github._3xhaust.exmaple.post.entities.Post;
import io.github._3xhaust.orm.Repository;
import io.github._3xhaust.orm.RepositoryFactory;

import java.util.List;
import java.util.Map;

@Service
public class PostService {
    private final Repository<Post> postRepository;

    @Inject
    public PostService(RepositoryFactory repositoryFactory) {
        this.postRepository = repositoryFactory.getRepository(Post.class);
    }

    public Post create(CreatePostDto createPostDto) {
        Post newPost = this.postRepository.create(createPostDto);
        newPost.setTitle(createPostDto.getTitle());
        newPost.setContent(createPostDto.getContent());
        return this.postRepository.save(newPost);
    }

    public List<Post> findById(Long id) {
        return this.postRepository.findOne(Map.of("id", id));
    }

    public List<Post> findAll() {
        return this.postRepository.find();
    }

    public List<Post> findByTitle(String title) {
        return this.postRepository.findOne(Map.of("title", title));
    }
}