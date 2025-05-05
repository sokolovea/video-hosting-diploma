package ru.rsreu.videohosting.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import ru.rsreu.videohosting.entity.Comment;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Getter
@Setter
public class CommentResponseDTO implements Serializable, Comparable<CommentResponseDTO> {
    private Long commentId;
    private String text;
    private Long parentId;
    private String username;
    private String userImagePath;
    private Long likesCount;
    private Long dislikesCount;
    private LocalDateTime createdAt;
    private List<CommentResponseDTO> replies = new ArrayList<>();

    public CommentResponseDTO(Comment comment) {
        this.commentId = comment.getCommentId();
        this.text = comment.getText();
        this.parentId = comment.getParent() != null ? comment.getParent().getCommentId() : null;
        this.username = comment.getUser().getUsername();
        this.userImagePath = comment.getUser().getImagePath();
        this.likesCount = comment.getLikesCount();
        this.dislikesCount = comment.getDislikesCount();
        this.createdAt = comment.getCreatedAt();
    }

    @Override
    public int compareTo(CommentResponseDTO o) {
        return o.getCommentId().compareTo(this.getCommentId());
    }
}
