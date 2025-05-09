package ru.rsreu.videohosting.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RatingDto {
    private double ratingUserPercent;
    private double ratingExpertPercent;
}
