package ru.rsreu.videohosting.dto.playlist;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RatingUserProfileDto {
    private String categoryName;
    private double rating;
}
