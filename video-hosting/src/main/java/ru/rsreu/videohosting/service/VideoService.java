package ru.rsreu.videohosting.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.rsreu.videohosting.dto.RatingDto;
import ru.rsreu.videohosting.entity.MarkType;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.entity.VideoViews;
import ru.rsreu.videohosting.repository.*;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VideoService {

    @Autowired
    private VideoViewsRepository videoViewsRepository;
    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Duration VIEW_INTERVAL = Duration.ofHours(1);
    @Autowired
    private UserVideoMarkRepository userVideoMarkRepository;
    @Autowired
    private MarkRepository markRepository;

    public boolean canRecordView(Long videoId, String viewerId) {
        Optional<Video> optionalVideo = videoRepository.findById(videoId);
        if (optionalVideo.isPresent()) {

            LocalDateTime lastViewTime = videoViewsRepository.findLastViewTimeByVideoAndIpAddress(optionalVideo.get().getVideoId(), viewerId);

            return lastViewTime == null || Duration.between(lastViewTime, LocalDateTime.now()).compareTo(VIEW_INTERVAL) > 0;
        }
        return false;
    }

//    @Transactional
    public void recordView(Long videoId, Long userId, String ipAddress) {
        if (videoId == null) {
            return;
        }
        Video video = null;
        User user = null;
        try {
            video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new EntityNotFoundException("Video not found"));
            user = userId == null? null : userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
        }
        catch (EntityNotFoundException e) {
            return;
        }
        VideoViews viewLog = new VideoViews();
        viewLog.setVideo(video);
        viewLog.setUser(user);
        viewLog.setIpAddress(ipAddress);
        viewLog.setViewedAt(LocalDateTime.now());
        videoViewsRepository.save(viewLog);
    }

    public RatingDto getVideoRating(Long videoId) {
        RatingDto ratingDto = new RatingDto();
        Optional<Video> optionalVideo = videoRepository.findById(videoId);
        if (optionalVideo.isPresent()) {
            Video video = optionalVideo.get();
            Long likesCountUser = userVideoMarkRepository.countByVideoAndMark (video, markRepository.findByName("LIKE").get());
            Long marksCountUser = userVideoMarkRepository.countByVideo(video);

            Long likesCountExpert = userVideoMarkRepository.countByVideoAndMark()
            if (marksCount != 0) {
                return likesCount / (double)marksCount * 100;
            }
        }
        return ratingDto;
    }
}

