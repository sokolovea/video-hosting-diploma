package ru.rsreu.videohosting.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.videohosting.dto.*;
import ru.rsreu.videohosting.dto.playlist.PlayListVideoDto;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.service.CommentService;
import ru.rsreu.videohosting.service.UserService;
import ru.rsreu.videohosting.service.VideoService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminActionRestController {

    private final UserService userService;
    private final VideoService videoService;
    private final CommentService commentService;

    @Autowired
    public AdminActionRestController(
            @Autowired UserService userService,
            @Autowired VideoService videoService,
            @Autowired CommentService commentService) {
        this.userService = userService;
        this.videoService = videoService;
        this.commentService = commentService;
    }

    // === Пользователи ===

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/block")
    public ResponseEntity<Void> blockUser(@RequestBody IdRequestDto request) {
        userService.blockUser(request.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/unblock")
    public ResponseEntity<Void> unblockUser(@RequestBody IdRequestDto request) {
        userService.unblockUser(request.getId());
        return ResponseEntity.ok().build();
    }

    // === Видео ===

    @GetMapping("/videos")
    public ResponseEntity<List<VideoGetAdminDto>> getAllVideos() {
        List<VideoGetAdminDto> videos = videoService.getAllVideos();
        return ResponseEntity.ok(videos);
    }

    @PostMapping("/videos/block")
    public ResponseEntity<Void> blockVideo(@RequestBody IdRequestDto request) {
        videoService.blockVideo(request.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/videos/unblock")
    public ResponseEntity<Void> unblockVideo(@RequestBody IdRequestDto request) {
        videoService.unblockVideo(request.getId());
        return ResponseEntity.ok().build();
    }

    // === Комментарии ===

//    @GetMapping("/comments")
//    public ResponseEntity<List<CommentResponseDTO>> getAllComments() {
//        List<CommentResponseDTO> comments = commentService.getAllComments();
//        return ResponseEntity.ok(comments);
//    }

//    @PostMapping("/comments/block")
//    public ResponseEntity<Void> blockComment(@RequestBody BlockRequest request) {
//        commentService.blockComment(request.getCommentId());
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/comments/unblock")
//    public ResponseEntity<Void> unblockComment(@RequestBody BlockRequest request) {
//        commentService.unblockComment(request.getCommentId());
//        return ResponseEntity.ok().build();
//    }

}
