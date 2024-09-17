package io.github._3xhaust.exmaple.orm.post;

import io.github._3xhaust.exceptions.HttpException;
import io.github._3xhaust.http.HttpStatus;
import io.github._3xhaust.annotations.Inject;
import io.github._3xhaust.annotations.Service;
import io.github._3xhaust.exmaple.orm.post.dto.CreatePostDto;
import io.github._3xhaust.exmaple.orm.post.entities.Post;
import io.github._3xhaust.orm.repository.Repository;
import io.github._3xhaust.orm.repository.RepositoryFactory;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class PostService {
    private final Repository<Post> postRepository;

    @Inject
    public PostService(RepositoryFactory repositoryFactory) {
        this.postRepository = repositoryFactory.getRepository(Post.class);
    }

    public CompletableFuture<Object> create(CreatePostDto createPostDto) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                Post newPost = this.postRepository.create(createPostDto);
                newPost.setTitle(createPostDto.getTitle());
                newPost.setContent(createPostDto.getContent());
                newPost = this.postRepository.save(newPost);

                Thread.sleep(5000);

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
            } finally {
                long endTime = System.currentTimeMillis();
                System.out.println("PostService.create() 메서드 내부 작업 시간: " + (endTime - startTime) + "ms");
            }
        });
    }

    public Object findById(Long id) {
        return this.postRepository.findOne(Map.of("id", id));
    }

    public Object findAll() {
        return this.postRepository.find();
    }
    public Object findByTitle(String title) {
        return this.postRepository.findOne(Map.of("title", title));
    }
}