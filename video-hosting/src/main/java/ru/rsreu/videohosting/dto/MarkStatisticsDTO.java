package ru.rsreu.videohosting.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MarkStatisticsDTO {
    private Long likesCount = 0L;
    private Long dislikesCount = 0L;
    private Long userMark = 0L;
}
