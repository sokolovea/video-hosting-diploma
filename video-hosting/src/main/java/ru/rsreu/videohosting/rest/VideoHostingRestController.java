package ru.rsreu.videohosting.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.videohosting.dto.CommentRequestDTO;
import ru.rsreu.videohosting.dto.CommentResponseDTO;
import ru.rsreu.videohosting.dto.MarkStatisticsDTO;
import ru.rsreu.videohosting.dto.ViewRequestDTO;
import ru.rsreu.videohosting.entity.*;
import ru.rsreu.videohosting.repository.*;
import ru.rsreu.videohosting.service.StorageService;
import ru.rsreu.videohosting.service.VideoService;
import ru.rsreu.videohosting.util.JacksonUtil;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

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

    @GetMapping("/{videoId}/comments")
    public ResponseEntity<?> getCommentTree(@PathVariable Long videoId) {
        Optional<Video> optionalVideo = videoRepository.findById(videoId);

        if (optionalVideo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Comment> allComments = commentRepository.findByVideo(optionalVideo.get());

        ObjectMapper objectMapper = JacksonUtil.getObjectMapper();

        SortedMap<Comment, List<Comment>> commentTree = buildCommentTree(allComments);

        SortedMap<String, List<Comment>> resultMap = commentTree.entrySet().stream()
            .collect(Collectors.toMap(
                    entry -> {
                        try {
                            return String.valueOf(objectMapper.writeValueAsString(entry.getKey()));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }, // ключ - ID комментария
                    Map.Entry::getValue,                                   // значение - список комментариев
                    (oldValue, newValue) -> oldValue,                      // функция слияния для дубликатов
                    TreeMap::new                                           // конкретная реализация SortedMap
            ));




        String json = null;
        try {
            json = objectMapper.writeValueAsString(resultMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(json);
    }

    private CommentResponseDTO convertToDTO(Comment comment) {
        return new CommentResponseDTO(
            comment
        );
    }


    public SortedMap<Comment, List<Comment>> buildCommentTree(List<Comment> allComments) {

        allComments.sort(new Comparator<Comment>() {
            @Override
            public int compare(Comment o1, Comment o2) {
                return o2.getCommentId().compareTo(o1.getCommentId());
            }
        });
        SortedMap<Comment, List<Comment>> tree = new TreeMap<>();
        Map<Long, Comment> commentMap = allComments.stream()
                .collect(Collectors.toMap(Comment::getCommentId, c -> c, (c1, c2) -> c1)); // На случай дубликатов

        for (Comment comment : allComments) {
            comment.setLikesCount(userCommentMarkRepository.countByCommentAndMark(comment, markRepository.findByName("LIKE").get()));
            comment.setDislikesCount(userCommentMarkRepository.countByCommentAndMark(comment, markRepository.findByName("DISLIKE").get()));

            if (comment.getParent() == null) {
                tree.putIfAbsent(comment, new ArrayList<>());
            }
        }

        for (Comment comment : allComments) {
            if (comment.getParent() != null) {
                Comment tempParent = comment.getParent();
                while (!tree.containsKey(tempParent) && tempParent.getParent() != null) {
                    tempParent = tempParent.getParent();
                }
                Comment parent = commentMap.get(tempParent.getCommentId());
                if (parent != null) {
                    tree.get(parent).add(comment);
                }
            }
        }

        return tree;

//        return tree.entrySet().stream()
//                .collect(Collectors.toMap(
//                        entry -> convertToDTO(entry.getKey()), // Преобразование ключа (Comment -> CommentResponseDTO)
//                        entry -> entry.getValue().stream()     // Преобразование списка значений (List<Comment> -> List<CommentResponseDTO>)
//                                .map(this::convertToDTO)           // Преобразование каждого Comment в CommentResponseDTO
//                                .collect(Collectors.toList()),    // Сбор в List<CommentResponseDTO>
//                        (oldValue, newValue) -> oldValue,     // Слияние дубликатов (не нужно в этом случае)
//                        TreeMap::new                          // Указываем, что результат должен быть TreeMap
//                ));
    }
}
