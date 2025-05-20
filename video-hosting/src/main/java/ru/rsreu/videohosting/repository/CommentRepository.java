package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.videohosting.entity.MultimediaClass;
import ru.rsreu.videohosting.entity.Comment;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByVideo(Video video);

    @Query("SELECT c FROM Comment c WHERE c.isBlocked = false")
    List<Comment> findByVideoAndNotBlocked(Video video);

    List<Comment> findByUser(User user);

    @Query("SELECT c FROM Comment c WHERE c.video = :video AND c.parent IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findTopLevelCommentsByVideo(@Param("video") Video video);

    @Query("SELECT c FROM Comment c WHERE c.parent = :parent ORDER BY c.createdAt DESC")
    List<Comment> findRepliesByParent(@Param("parent") Comment parent);

    @Query("SELECT count(c) FROM Comment c WHERE c.user = :author AND :classificator MEMBER OF c.video.multimediaClasses")
    Long countAllByAuthorAndHavingClass(@Param("author") User author, @Param("classificator") MultimediaClass classificator);

    void deleteByVideo(Video video);

    @Modifying
    @Query("UPDATE Comment c SET c.isBlocked = true WHERE c.commentId = :commentId OR c.parent = :commentId")
    void blockCommentWithReplies(Long commentId);
}
