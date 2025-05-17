package ru.rsreu.videohosting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoGetAdminDto {
    private Long id;
    private String title;
    private String thumbnailPath;
}
