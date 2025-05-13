package ru.rsreu.videohosting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VideoRatingDto {
    private Long videoId;
    private Long multimediaClassId;
    private Long roleId;
    private Long markId;
    private Long count_mark;
}
