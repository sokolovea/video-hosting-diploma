package ru.rsreu.videohosting.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CountSumDto {
    private Long sumUser;
    private Long sumExpert;
    private Long countUser;
    private Long countExpert;

    @JsonIgnore
    public RatingDto getRatingDto() {
        RatingDto dto = new RatingDto();
        dto.setRatingUser(countUser == null || countUser == 0 ? null : sumUser / (double) countUser);
        dto.setRatingExpert(countExpert == null || countExpert == 0 ? null : sumExpert / (double) countExpert);
        return dto;
    }
}
