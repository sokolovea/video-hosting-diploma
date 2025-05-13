package ru.rsreu.videohosting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.rsreu.videohosting.entity.Video;

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
    private Long views;

    public VideoSearchDto(Video video, Double ratingUsual, Double ratingExperts, long views) {
        this.videoId = video.getVideoId();
        this.videoTitle = video.getTitle();
        this.videoDescription = video.getDescription();
        this.videoThumbnailUrl = video.getImagePath();
        this.videoRatingUsual = ratingUsual;
        this.videoRatingExperts = ratingExperts;
        this.views = views;
    }
}
