package ru.rsreu.videohosting.entity;

import lombok.*;
import ru.rsreu.videohosting.repository.composite.PlaylistVideoClassId;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PlaylistVideoClassId.class)
public class PlaylistVideo implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @Id
    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;
}
