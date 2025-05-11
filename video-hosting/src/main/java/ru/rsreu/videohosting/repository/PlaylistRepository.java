package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.videohosting.entity.Playlist;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUser(User user);

    @Query("SELECT p.name from Playlist p where p.user = :user")
    List<String> findPlaylistNamesByUser(@Param("user") User user);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Playlist p WHERE p.user = :user AND p.playlistId = :playlistId")
    boolean belongsToUser(@Param("user") User user, @Param("playlistId") Long playlistId);

}
