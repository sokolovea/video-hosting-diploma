package ru.rsreu.videohosting.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.videohosting.dto.IdRequestDto;
import ru.rsreu.videohosting.dto.UserDTO;
import ru.rsreu.videohosting.dto.VideoGetAdminDto;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.repository.UserRepository;
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
    private final SessionRegistry sessionRegistry;
    private final UserRepository userRepository;

    @Autowired
    public AdminActionRestController(
            @Autowired UserService userService,
            @Autowired VideoService videoService,
            @Autowired CommentService commentService,
            @Autowired SessionRegistry sessionRegistry, UserRepository userRepository) {
        this.userService = userService;
        this.videoService = videoService;
        this.commentService = commentService;
        this.sessionRegistry = sessionRegistry;
        this.userRepository = userRepository;
    }


    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/block")
    public ResponseEntity<Void> blockUser(@RequestBody IdRequestDto request) {
        userService.blockUser(request.getId());
        if (userService.isUserExist(request.getId())) {
            User blockedUser = userRepository.findById(request.getId()).get();
            sessionRegistry.getAllPrincipals().stream()
                    .filter(principal -> principal instanceof org.springframework.security.core.userdetails.User)
                    .map(principal -> (org.springframework.security.core.userdetails.User) principal)
                    .filter(user -> user.getUsername().equals(blockedUser.getUsername()))
                    .forEach(user -> {
                        for (SessionInformation sessionInfo : sessionRegistry.getAllSessions(user, false)) {
                            sessionInfo.expireNow();
                        }
                    });
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/unblock")
    public ResponseEntity<Void> unblockUser(@RequestBody IdRequestDto request) {
        userService.unblockUser(request.getId());
        return ResponseEntity.ok().build();
    }


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

//    DEBUG
    @PostMapping("/comments/block")
    public ResponseEntity<Void> blockComment(@RequestBody IdRequestDto request) {
        commentService.blockComment(request.getId());
        return ResponseEntity.ok().build();
    }

//
//    @PostMapping("/comments/unblock")
//    public ResponseEntity<Void> unblockComment(@RequestBody BlockRequest request) {
//        commentService.unblockComment(request.getCommentId());
//        return ResponseEntity.ok().build();
//    }

}
