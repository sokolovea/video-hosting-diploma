package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.videohosting.entity.Comment;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;

import java.util.List;
import java.util.Map;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByVideo(Video video);
    List<Comment> findByUser(User user);

    @Query("SELECT c FROM Comment c WHERE c.video = :video AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findTopLevelCommentsByVideo(@Param("video") Video video);

    @Query("SELECT c FROM Comment c WHERE c.parent = :parent ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParent(@Param("parent") Comment parent);
}
