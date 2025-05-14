package ru.rsreu.videohosting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.rsreu.videohosting.entity.Video;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoSearchDto {
    private Long videoId;
    private String videoTitle;
    private String videoDescription;
    private String videoThumbnailUrl;
    private Double videoRatingUsual;
    private Double videoRatingExperts;
    private LocalDateTime videoUploadDate;
    private Long views;

    public VideoSearchDto(Video video, Double ratingUsual, Double ratingExperts,
                          LocalDateTime videoUploadDate, long views) {
        this.videoId = video.getVideoId();
        this.videoTitle = video.getTitle();
        this.videoDescription = video.getDescription();
        this.videoThumbnailUrl = video.getImagePath();
        this.videoRatingUsual = ratingUsual;
        this.videoRatingExperts = ratingExperts;
        this.videoUploadDate = videoUploadDate;
        this.views = views;
    }
}
