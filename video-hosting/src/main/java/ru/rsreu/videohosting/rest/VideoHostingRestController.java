package ru.rsreu.videohosting.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.videohosting.dto.CommentRequestDTO;
import ru.rsreu.videohosting.dto.MarkStatisticsDTO;
import ru.rsreu.videohosting.dto.ViewRequestDTO;
import ru.rsreu.videohosting.entity.*;
import ru.rsreu.videohosting.repository.*;
import ru.rsreu.videohosting.service.StorageService;
import ru.rsreu.videohosting.service.VideoService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/video", produces = "application/json")
//@CrossOrigin(origins = {"http://localhost:8082"}) // DEBUG
@CrossOrigin(origins = "*")
public class VideoHostingRestController {
    private static final Logger log = LoggerFactory.getLogger(VideoHostingRestController.class);

    private final ClassRepository classRepository;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final MarkRepository markRepository;
    private final UserCommentMarkRepository userCommentMarkRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final VideoService videoService;
    private final UserVideoMarkRepository userVideoMarkRepository;

    public VideoHostingRestController(@Autowired UserRepository userRepository,
                                      @Autowired ClassRepository classRepository,
                                      @Autowired VideoRepository videoRepository,
                                      @Autowired CommentRepository commentRepository,
                                      @Autowired MarkRepository markRepository,
                                      @Autowired UserCommentMarkRepository commentMarkRepository,
                                      @Autowired StorageService storageService,
                                      @Autowired VideoService videoService, UserVideoMarkRepository userVideoMarkRepository) {
        this.userRepository = userRepository;
        this.classRepository = classRepository;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
        this.markRepository = markRepository;
        this.storageService = storageService;
        this.userCommentMarkRepository = commentMarkRepository;
        this.videoService = videoService;
        this.userVideoMarkRepository = userVideoMarkRepository;
    }

    @PostMapping("/{videoId}/mark")
    public ResponseEntity<?> addMark(@PathVariable Long videoId,
                                     @RequestParam("markId") Long markId,
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

        Optional<MarkType> markOptional = markRepository.findById(markId);
        if (markOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mark not found");
        }

        UserVideoMark userVideoMark = new UserVideoMark();
        userVideoMark.setUser(userOptional.get());
        userVideoMark.setVideo(videoOptional.get());
        userVideoMark.setMark(markOptional.get());


        userVideoMarkRepository.save(userVideoMark);
        Long likesCount = userVideoMarkRepository.countByVideoAndMark(videoOptional.get(), markRepository.findByName("LIKE").get());
        Long dislikesCount = userVideoMarkRepository.countByVideoAndMark(videoOptional.get(), markRepository.findByName("DISLIKE").get());
        return ResponseEntity.status(HttpStatus.CREATED).body(new MarkStatisticsDTO(likesCount, dislikesCount, markOptional.get().getMarkId()));
    }


    @PostMapping("/{videoId}/comment")
    public ResponseEntity<?> addComment(@PathVariable Long videoId,
                                        @RequestParam(required = false, name = "parentId") Long parentId,
                                        @RequestBody CommentRequestDTO commentText,
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
        if (parentId != null && commentRepository.existsById(parentId)) {
            comment.setParent(commentRepository.findById(parentId).get());
        }
        comment.setText(commentText.getCommentText());
        comment.setVideo(videoOptional.get());
        comment.setUser(userOptional.get());
        comment.setIsModified(false);

        commentRepository.save(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PostMapping("/{videoId}/comment/{commentId}/like")
    @ResponseBody
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

        Map<String, Object> response = new HashMap<>();
        Long updatedLikes = userCommentMarkRepository.countByCommentAndMark(commentOptional.get(), markRepository.findByName("LIKE").get());
        Long updatedDislikes = userCommentMarkRepository.countByCommentAndMark(commentOptional.get(), markRepository.findByName("DISLIKE").get());
        response.put("likes", updatedLikes);
        response.put("dislikes", updatedDislikes);
        response.put("userMark", markOptional.get().getMarkId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/view")
    public ResponseEntity<?> trackView(
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            HttpServletRequest request,
            @RequestBody ViewRequestDTO viewRequestDTO,
            Principal principal
    ) {
        String ipAddress = Optional.ofNullable(forwardedFor)
                .map(f -> f.split(",")[0])
                .orElse(request.getRemoteAddr());

        if (videoService.canRecordView(viewRequestDTO.getVideoId(), ipAddress)) {
            try {
                videoService.recordView(viewRequestDTO.getVideoId(), principal == null ? null : userRepository.findByLogin(principal.getName()).get().getUserId(), ipAddress);
            } catch (NoSuchElementException e) {
                return ResponseEntity.status(400).body("Video or user not found");
            }
            return ResponseEntity.ok("View recorded");
        }
        return ResponseEntity.ok("View already recorded recently");
    }
}
