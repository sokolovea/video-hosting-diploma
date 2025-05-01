package ru.rsreu.videohosting.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.videohosting.entity.*;
import ru.rsreu.videohosting.repository.*;
import ru.rsreu.videohosting.service.StorageService;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/video", produces = "application/json")
@CrossOrigin(origins = {"http://localhost:8082"}) // DEBUG
public class VideoHostingRestController {
    private static final Logger log = LoggerFactory.getLogger(VideoHostingRestController.class);

    private final ClassRepository classRepository;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final MarkRepository markRepository;
    private final UserCommentMarkRepository userCommentMarkRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    public VideoHostingRestController(@Autowired UserRepository userRepository,
                                      @Autowired ClassRepository classRepository,
                                      @Autowired VideoRepository videoRepository,
                                      @Autowired CommentRepository commentRepository,
                                      @Autowired MarkRepository markRepository,
                                      @Autowired UserCommentMarkRepository commentMarkRepository,
                                      @Autowired StorageService storageService) {
        this.userRepository = userRepository;
        this.classRepository = classRepository;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
        this.markRepository = markRepository;
        this.storageService = storageService;
        this.userCommentMarkRepository = commentMarkRepository;
    }

    @PostMapping("/{videoId}/comment")
    public ResponseEntity<?> addComment(@PathVariable Long videoId,
                                        @RequestParam("commentText") String commentText,
                                        Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        Optional<Video> videoOptional = videoRepository.findById(videoId);
        if (videoOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video not found");
        }

        Optional<User> userOptional = userRepository.findByLogin(principal.getName());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        Comment comment = new Comment();
        comment.setText(commentText);
        comment.setVideo(videoOptional.get());
        comment.setUser(userOptional.get());
        comment.setIsModified(false);

        commentRepository.save(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PostMapping("/{videoId}/comment/{commentId}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long videoId,
                                         @PathVariable Long commentId,
                                         @RequestParam("markId") Long markId,
                                         Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        Optional<User> userOptional = userRepository.findByLogin(principal.getName());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (commentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
        }

        Optional<MarkType> markOptional = markRepository.findById(markId);
        if (markOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mark type not found");
        }

        UserCommentMark userCommentMark = new UserCommentMark();
        userCommentMark.setComment(commentOptional.get());
        userCommentMark.setUser(userOptional.get());
        userCommentMark.setMark(markOptional.get());

        userCommentMarkRepository.save(userCommentMark);
        return ResponseEntity.ok("{}"); //DEBUG
    }
}
