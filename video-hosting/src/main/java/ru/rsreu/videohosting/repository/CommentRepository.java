package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.videohosting.entity.Comment;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;

import java.util.List;
import java.util.Map;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByVideo(Video video);
    List<Comment> findByUser(User user);
}
