package ru.rsreu.videohosting.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RatingDto {
    private Double ratingUser;
    private Double ratingExpert;
}
