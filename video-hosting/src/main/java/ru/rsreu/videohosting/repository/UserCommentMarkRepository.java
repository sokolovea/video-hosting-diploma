package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.videohosting.entity.Comment;
import ru.rsreu.videohosting.entity.MarkType;
import ru.rsreu.videohosting.entity.UserCommentMark;
import ru.rsreu.videohosting.repository.composite.UserCommentMarkId;

public interface UserCommentMarkRepository extends JpaRepository<UserCommentMark, UserCommentMarkId> {
    Long countByCommentAndMark(Comment comment, MarkType mark);
}
