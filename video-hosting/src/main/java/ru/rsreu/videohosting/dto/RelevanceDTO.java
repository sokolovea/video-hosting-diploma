package ru.rsreu.videohosting.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RelevanceDTO {
    private Double relevanceUser;
    private Double relevanceExpert;
}
