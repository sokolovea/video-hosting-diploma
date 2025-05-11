package ru.rsreu.videohosting.dto.playlist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistRenameDto {
    @NotNull
    private Long playlistId;
    @NotNull
    private String newPlaylistName;
}
