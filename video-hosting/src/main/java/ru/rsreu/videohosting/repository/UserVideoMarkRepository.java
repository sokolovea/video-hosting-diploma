package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.videohosting.entity.*;
import ru.rsreu.videohosting.repository.composite.UserVideoMarkId;

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
}
