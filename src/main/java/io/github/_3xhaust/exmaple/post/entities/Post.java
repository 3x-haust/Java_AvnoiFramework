package io.github._3xhaust.exmaple.post.entities;

import io.github._3xhaust.annotations.Entity;
import io.github._3xhaust.annotations.orm.Column;
import io.github._3xhaust.annotations.orm.PrimaryGeneratedColumn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Post {
    @PrimaryGeneratedColumn(strategy = "increment")
    private Long post_id;

    @Column(unique = true)
    private String title;

    @Column()
    private String content;
}