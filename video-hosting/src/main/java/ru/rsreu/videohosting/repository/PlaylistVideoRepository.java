package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rsreu.videohosting.entity.Playlist;
import ru.rsreu.videohosting.entity.PlaylistVideo;
import ru.rsreu.videohosting.entity.Video;


@Repository
public interface PlaylistVideoRepository extends JpaRepository<PlaylistVideo, Long> {
    boolean existsByPlaylistAndVideo(Playlist playlist, Video video);
    void deleteByPlaylistAndVideo(Playlist playlist, Video video);
}
