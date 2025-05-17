package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.videohosting.dto.MutualLikePairWeight;
import ru.rsreu.videohosting.entity.*;
import ru.rsreu.videohosting.repository.composite.UserVideoMarkId;

import java.util.List;
import java.util.Set;

public interface UserVideoMarkRepository extends JpaRepository<UserVideoMark, UserVideoMarkId> {

    @Query("""
    SELECT COUNT(distinct uvm.user)
    FROM UserVideoMark uvm
    JOIN uvm.user u
    JOIN u.roleAssignments ra
    JOIN uvm.video v
    JOIN VideoClass vc on v.videoId = vc.video.videoId
    JOIN MultimediaClass mc on mc.multimediaClassId = vc.videoMultimediaClass.multimediaClassId
    WHERE uvm.video = :video
      AND uvm.mark = :mark
      AND ra.role IN :roles
      AND ra.multimediaClass.multimediaClassId IN mc.multimediaClassId
""") // DEBUG DEBUG DEBUG!!!
    Long countByVideoAndMarkAndUserRoles(
            @Param("video") Video video,
            @Param("mark") MarkType mark,
            @Param("roles") Set<Role> userRoles
    );
    Long countByVideo(Video video);

    Long countByVideoAndMark(Video video, MarkType mark);
    Long countByUserAndMark(User user, MarkType mark);

    @Query("SELECT CASE WHEN COUNT(uvm) > 0 THEN true ELSE false END FROM UserVideoMark uvm " +
            "WHERE uvm.user = :user and uvm.video = :video")
    boolean existsByUserAndVideo(@Param("user") User user, @Param("video") Video video);

    @Query("SELECT uvm FROM UserVideoMark uvm WHERE uvm.user = :user and uvm.video = :video")
    UserVideoMark findByUserAndVideo(@Param("user") User user, @Param("video") Video video);

    void deleteByVideo(Video video);

    @Query("""
    SELECT
        u1.user.userId AS userA,
        v1.author.userId AS userB,
        SUM(CASE WHEN mt.name = 'LIKE' THEN 1 WHEN mt.name = 'DISLIKE' THEN (-1) ELSE 0 END) AS weight
    FROM UserVideoMark u1
    JOIN Video v1 ON u1.video = v1
    JOIN UserVideoMark u2 ON u2.video = v1
    JOIN MarkType mt on u2.mark = mt.markId
    WHERE u1.user.userId != v1.author.userId
        AND u2.user != v1.author
    GROUP BY u1.user.userId, v1.author.userId
    HAVING ABS(SUM(CASE WHEN mt.name = 'LIKE' THEN 1 WHEN mt.name = 'DISLIKE' THEN (-1) ELSE 0 END)) >= :weight
""")
    List<MutualLikePairWeight> findMutualVideoMark(@Param("weight") long weight);


//    @Query("""
//    SELECT
//        u1.user.userId AS userA,
//        u1.user.userId AS userB,
//        count(*) as count
//    FROM UserVideoMark u1
//    JOIN Video v1 ON u1.video = v1
//    WHERE u1.user.userId != v1.author.userId
//        AND u1.mark = :markCount
//    GROUP BY u1.user.userId
//""")
//    List<MutualLikePairWeight> findNotNormalVideoMarker(@Param("markCount") long markCount);
}
