package io.github._3xhaust.exmaple.orm.post.entities;

import io.github._3xhaust.annotations.Entity;
import io.github._3xhaust.orm.annotations.Column;
import io.github._3xhaust.orm.annotations.PrimaryGeneratedColumn;
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