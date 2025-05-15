package ru.rsreu.videohosting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.rsreu.videohosting.entity.User;
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
    private Double videoRatingUser;
    private Double videoRatingExpert;
    private Double videoRelevanceUser;
    private Double videoRelevanceExpert;
    private LocalDateTime videoUploadDate;
    private Long views;
    private User author;

    public VideoSearchDto(Video video, Double ratingUser, Double ratingExpert,
                          Double relevanceUser, Double relevanceExpert,
                          LocalDateTime videoUploadDate, long views) {
        this.videoId = video.getVideoId();
        this.videoTitle = video.getTitle();
        this.videoDescription = video.getDescription();
        this.videoThumbnailUrl = video.getImagePath();
        this.videoRatingUser = ratingUser;
        this.videoRatingExpert = ratingExpert;
        this.videoRelevanceUser = relevanceUser;
        this.videoRelevanceExpert = relevanceExpert;
        this.videoUploadDate = videoUploadDate;
        this.views = views;
        this.author = video.getAuthor();
    }
}
