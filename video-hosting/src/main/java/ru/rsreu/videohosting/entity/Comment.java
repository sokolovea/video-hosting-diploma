package ru.rsreu.videohosting.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment implements Serializable, Comparable<Comment> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Video video;

    private String text;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    private Boolean isModified;

    @Transient
    private Long likesCount;

    @Transient
    private Long dislikesCount;

    @Transient
    private List<Comment> replies = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Override
    public int compareTo(Comment o) {
        return o.getCommentId().compareTo(this.getCommentId());
    }
}

