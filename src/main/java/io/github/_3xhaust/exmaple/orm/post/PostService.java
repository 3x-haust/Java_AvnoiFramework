package io.github._3xhaust.post;

import io.github._3xhaust.HttpException;
import io.github._3xhaust.HttpStatus;
import io.github._3xhaust.annotations.Inject;
import io.github._3xhaust.annotations.Service;
import io.github._3xhaust.exmaple.post.dto.CreatePostDto;
import io.github._3xhaust.exmaple.post.entities.Post;
import io.github._3xhaust.orm.Repository;
import io.github._3xhaust.orm.RepositoryFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class PostService {
    private final Repository<Post> postRepository;

    @Inject
    public PostService(RepositoryFactory repositoryFactory) {
        this.postRepository = repositoryFactory.getRepository(Post.class);
    }

    public Map<String, Object> create(CreatePostDto createPostDto) {
        try {
            Post newPost = this.postRepository.create(createPostDto);
            newPost.setTitle(createPostDto.getTitle());
            newPost.setContent(createPostDto.getContent());
            newPost = this.postRepository.save(newPost);

            return Map.of(
                    "status", HttpStatus.CREATED.getCode(),
                    "message", "Post created successfully",
                    "timestamp", new Date().toString(),
                    "data", newPost
            );
        } catch (Exception e) {
            throw new HttpException(
                    Map.of(
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                            "message", "Failed to create post",
                            "timestamp", new Date().toString(),
                            "error", e.getMessage()
                    ),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
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