package ru.rsreu.videohosting.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CommentRequestDTO {
    private String commentText;
}
