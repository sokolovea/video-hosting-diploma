package ru.rsreu.videohosting.rest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.rsreu.videohosting.dto.playlist.PlayListDeleteDto;
import ru.rsreu.videohosting.dto.playlist.PlayListCreateDto;
import ru.rsreu.videohosting.dto.playlist.PlayListVideoDto;
import ru.rsreu.videohosting.entity.Playlist;
import ru.rsreu.videohosting.entity.PlaylistVideo;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.repository.*;
import ru.rsreu.videohosting.service.StorageService;
import ru.rsreu.videohosting.service.VideoService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/playlist", produces = "application/json")
//@CrossOrigin(origins = {"http://localhost:8082"}) // DEBUG
@CrossOrigin(origins = "*")
public class PlaylistRestController {
    private static final Logger log = LoggerFactory.getLogger(VideoHostingRestController.class);

    private final MultimediaClassRepository multimediaClassRepository;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final MarkRepository markRepository;
    private final UserCommentMarkRepository userCommentMarkRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final VideoService videoService;
    private final UserVideoMarkRepository userVideoMarkRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistVideoRepository playlistVideoRepository;

    public PlaylistRestController(@Autowired MultimediaClassRepository multimediaClassRepository,
                                  @Autowired VideoRepository videoRepository,
                                  @Autowired CommentRepository commentRepository,
                                  @Autowired MarkRepository markRepository,
                                  @Autowired UserCommentMarkRepository userCommentMarkRepository,
                                  @Autowired UserRepository userRepository,
                                  @Autowired StorageService storageService,
                                  @Autowired VideoService videoService,
                                  @Autowired UserVideoMarkRepository userVideoMarkRepository,
                                  @Autowired SubscriptionRepository subscriptionRepository,
                                  @Autowired PlaylistRepository playlistRepository, PlaylistVideoRepository playlistVideoRepository) {
        this.multimediaClassRepository = multimediaClassRepository;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
        this.markRepository = markRepository;
        this.userCommentMarkRepository = userCommentMarkRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.videoService = videoService;
        this.userVideoMarkRepository = userVideoMarkRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.playlistRepository = playlistRepository;
        this.playlistVideoRepository = playlistVideoRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPlaylist(
            @RequestBody PlayListCreateDto playlistCreateDto,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body("You are not logged in");
        }

        User user = userRepository.findByLogin(principal.getName()).get();
        Long createdPlaylistId = null;
        if (playlistRepository.findPlaylistNamesByUser(user).contains(playlistCreateDto.getTitle())) {
            return ResponseEntity.status(409).body("Playlist name already exists");
        } else {
            createdPlaylistId = playlistRepository.save(new Playlist(null, playlistCreateDto.getTitle(), user, null, LocalDateTime.now())).getPlaylistId();
        }
        return ResponseEntity.ok(Map.of("id", createdPlaylistId));
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deletePlaylist(
            @RequestBody PlayListDeleteDto playlistDeleteDto,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body("You are not logged in");
        }

        User user = userRepository.findByLogin(principal.getName()).get();

        if (!playlistRepository.belongsToUser(user, playlistDeleteDto.getPlaylistId())) {
            return ResponseEntity.status(404).body("playlist do not exist");
        } else {
            Playlist playlist = playlistRepository.findById(playlistDeleteDto.getPlaylistId()).get();
            playlistRepository.delete(playlist);
        }
        return ResponseEntity.ok(Map.of("id", playlistDeleteDto.getPlaylistId()));
    }

    @PostMapping("/add-video")
    public ResponseEntity<?> addVideoToPlaylist(
            @RequestBody PlayListVideoDto playListVideoDto,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in");
        }

        User user = userRepository.findByLogin(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Long playlistId = playListVideoDto.getPlaylistId();
        Long videoId = playListVideoDto.getVideoId();

        if (!playlistRepository.belongsToUser(user, playlistId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Playlist does not belong to user");
        }

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found"));

        if (playlistVideoRepository.existsByPlaylistAndVideo(playlist, video)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Video already in playlist");
        }

        playlistVideoRepository.save(new PlaylistVideo(null, playlist, video));

        return ResponseEntity.ok(Map.of("message", "Video added to playlist"));
    }

    @PostMapping("/remove-video")
    public ResponseEntity<?> removeVideoFromPlaylist(
            @RequestBody PlayListVideoDto playListVideoDto,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in");
        }

        User user = userRepository.findByLogin(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Long playlistId = playListVideoDto.getPlaylistId();
        Long videoId = playListVideoDto.getVideoId();

        if (!playlistRepository.belongsToUser(user, playlistId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Playlist does not belong to user");
        }

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found"));

        if (!playlistVideoRepository.existsByPlaylistAndVideo(playlist, video)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video not found in playlist");
        }

        playlistVideoRepository.deleteByPlaylistAndVideo(playlist, video);

        return ResponseEntity.ok(Map.of("message", "Video removed from playlist"));
    }


}