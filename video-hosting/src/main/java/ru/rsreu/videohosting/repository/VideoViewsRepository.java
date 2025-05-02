package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.videohosting.entity.VideoViews;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;

import java.util.List;

public interface VideoViewsRepository extends JpaRepository<VideoViews, Long> {
    List<VideoViews> findByUser(User user);
    List<VideoViews> findByVideo(Video video);
    Long countByVideo(Video video);
    Long countByUser(User user);
}
