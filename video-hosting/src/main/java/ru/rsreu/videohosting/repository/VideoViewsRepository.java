package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.videohosting.entity.VideoViews;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;

import java.time.LocalDateTime;
import java.util.List;

public interface VideoViewsRepository extends JpaRepository<VideoViews, Long> {
    List<VideoViews> findByUser(User user);
    List<VideoViews> findByVideo(Video video);
    Long countByVideo(Video video);
    Long countByUser(User user);
    @Query("SELECT MAX(v.viewedAt) FROM VideoViews v WHERE v.video = :video AND v.user = :user")
    LocalDateTime findLastViewTimeByVideoAndUser(@Param("video") Video video, @Param("user") User user);

    @Query("SELECT MAX(v.viewedAt) FROM VideoViews v WHERE v.video.videoId = :videoId AND v.ipAddress = :ipAddress")
    LocalDateTime findLastViewTimeByVideoAndIpAddress(@Param("videoId") Long videoId, @Param("ipAddress") String ipAddress);



}
