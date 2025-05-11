package ru.rsreu.videohosting.repository.composite;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlaylistVideoClassId implements Serializable {
    Long playlist;
    Long video;
}
