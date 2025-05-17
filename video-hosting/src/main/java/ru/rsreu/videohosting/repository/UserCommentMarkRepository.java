package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.videohosting.dto.MutualLikePairWeight;
import ru.rsreu.videohosting.entity.Comment;
import ru.rsreu.videohosting.entity.MarkType;
import ru.rsreu.videohosting.entity.UserCommentMark;
import ru.rsreu.videohosting.repository.composite.UserCommentMarkId;

import java.util.List;

public interface UserCommentMarkRepository extends JpaRepository<UserCommentMark, UserCommentMarkId> {
    Long countByCommentAndMark(Comment comment, MarkType mark);

    @Query("""
    SELECT
        u1.user.userId AS userA,
        c1.user.userId AS userB,
        SUM(CASE WHEN mt.name = 'LIKE' THEN 1 WHEN mt.name = 'DISLIKE' THEN (-1) ELSE 0 END) AS weight
    FROM UserCommentMark u1
    JOIN Comment c1 ON u1.comment = c1
    JOIN MarkType mt ON u1.mark = mt
    WHERE u1.user != c1.user
    GROUP BY u1.user.userId, c1.user.userId
    HAVING ABS(SUM(CASE WHEN mt.name = 'LIKE' THEN 1 WHEN mt.name = 'DISLIKE' THEN (-1) ELSE 0 END)) >= :weight
    """)
    List<MutualLikePairWeight> findMutualCommentMark(@Param("weight") long weight);
}
