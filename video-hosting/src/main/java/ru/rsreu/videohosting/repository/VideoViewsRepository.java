package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.videohosting.entity.VideoViews;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface VideoViewsRepository extends JpaRepository<VideoViews, Long> {
    List<VideoViews> findByUserOrderByViewedAtDesc(User user);
    List<VideoViews> findByVideo(Video video);
    Long countByVideo(Video video);
    Long countByUser(User user);
    @Query("SELECT MAX(v.viewedAt) FROM video_views v WHERE v.video.videoId = :videoId AND v.user.userId = :userId")
    LocalDateTime findLastViewTimeByVideoAndUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    @Query("SELECT MAX(v.viewedAt) FROM video_views v WHERE v.video.videoId = :videoId AND v.ipAddress = :ipAddress")
    LocalDateTime findLastViewTimeByVideoAndIpAddress(@Param("videoId") Long videoId, @Param("ipAddress") String ipAddress);


    @Query("SELECT vi, COUNT(v.video) FROM video_views v join" +
            " Video vi on v.video.videoId = vi.videoId WHERE v.video IN :videos " +
            "GROUP BY vi")
    List<Object[]> countViewsByVideos(@Param("videos") List<Video> videos);
}
