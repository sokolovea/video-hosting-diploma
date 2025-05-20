package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.videohosting.entity.Playlist;
import ru.rsreu.videohosting.entity.PlaylistVideo;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.repository.composite.PlaylistVideoClassId;

import java.util.List;


@Repository
public interface PlaylistVideoRepository extends JpaRepository<PlaylistVideo, PlaylistVideoClassId> {
    boolean existsByPlaylistAndVideo(Playlist playlist, Video video);
    void deleteByPlaylistAndVideo(Playlist playlist, Video video);

    @Query("SELECT plv.video from PlaylistVideo plv where plv.playlist.playlistId = :playlistId")
    List<Video> findByPlaylist(@Param("playlistId") Long playlistId);

    @Query("SELECT plv.playlist FROM PlaylistVideo plv where plv.video.videoId = :videoId and plv.playlist.user = :user")
    List<Playlist> findPlaylistsByVideoIdAndUser(@Param("videoId") Long videoId,
                                                 @Param("user") User user);

}
