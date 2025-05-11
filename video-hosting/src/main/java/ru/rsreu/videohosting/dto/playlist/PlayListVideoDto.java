package ru.rsreu.videohosting.dto.playlist;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayListVideoDto {
    private Long playlistId;
    private Long videoId;
}
