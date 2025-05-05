package ru.rsreu.videohosting.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.rsreu.videohosting.dto.UserProfileDTO;
import ru.rsreu.videohosting.dto.ViewRequestDTO;
import ru.rsreu.videohosting.entity.*;
import ru.rsreu.videohosting.entity.Class;
import ru.rsreu.videohosting.repository.*;
import ru.rsreu.videohosting.service.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;


@SpringBootApplication
@Controller("/")
public class MVCVideoHostingController {

    private static final Logger log = LoggerFactory.getLogger(MVCVideoHostingController.class);
    private final ClassRepository classRepository;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final MarkRepository markRepository;
    private final UserCommentMarkRepository userCommentMarkRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final VideoViewsRepository videoViewsRepository;
    private final UserVideoMarkRepository videoMarkRepository;
    private final VideoService videoService;
    private final CustomWebClientService customWebClientService;
    private final CommentService commentService;


    public MVCVideoHostingController(@Autowired UserRepository userRepository,
                                     @Autowired ClassRepository classRepository,
                                     @Autowired VideoRepository videoRepository,
                                     @Autowired CommentRepository commentRepository,
                                     @Autowired MarkRepository markRepository,
                                     @Autowired UserCommentMarkRepository commentMarkRepository,
                                     @Autowired StorageService storageService,
                                     @Autowired VideoViewsRepository videoViewsRepository,
                                     @Autowired UserVideoMarkRepository videoMarkRepository,
                                     @Autowired VideoService videoService,
                                     @Autowired CustomWebClientService customWebClientService,
                                     @Autowired CommentService commentService) {
        this.userRepository = userRepository;
        this.classRepository = classRepository;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
        this.markRepository = markRepository;
        this.storageService = storageService;
        this.userCommentMarkRepository = commentMarkRepository;
        this.videoViewsRepository = videoViewsRepository;
        this.videoMarkRepository = videoMarkRepository;
        this.videoService = videoService;
        this.customWebClientService = customWebClientService;
        this.commentService = commentService;
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        UserProfileDTO profileDto = new UserProfileDTO(
                user.getLogin(),
                user.getSurname(),
                user.getName(),
                user.getPatronymic(),
                user.getEmail(),
                user.getTelephone(),
                user.getImagePath(),
                user.getCreatedAt().toLocalDate()
        );

        model.addAttribute("user", profileDto);
        return "profile";
    }


    @GetMapping("/upload_video")
    public String uploadVideo(Model model) {
        List<String> classes = classRepository.getAllClassNames();
        model.addAttribute("classes", classes);
        return "upload_video";
    }

    @PostMapping("/upload_video")
    public String uploadVideo(@RequestParam("title") String title,
                              @RequestParam("description") String description,
                              @RequestParam("videoFile") MultipartFile videoFile,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              @RequestParam("categories") List<String> classesString,
                              Principal principal) {

        if (principal == null) {
            return "redirect:/404";
        }

        if (videoFile.isEmpty()) {
            return "redirect:/upload_video?error";
        }

        if (imageFile.isEmpty()) {
            return "redirect:/upload_video?error";
        }

        String imagePath = this.storageService.store(imageFile, ContentMultimediaType.VIDEO_IMAGE);
        String videoPath = this.storageService.store(videoFile, ContentMultimediaType.VIDEO);


        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoPath(videoPath);
        video.setImagePath(imagePath);

        List<Class> classes = classRepository.findAllByClassNameIn(classesString);
        video.setClasses(classes);
        Optional<User> optionalUser = userRepository.findByLogin(principal.getName());
        if (optionalUser.isPresent()) {
            video.setAuthor(optionalUser.get());
            videoRepository.save(video);
            return "redirect:/upload_video?success";
        }
        return "redirect:/upload_video?error"; //DEBUG
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
    }



    @GetMapping("/video/{videoId}")
    public String video(@PathVariable("videoId") Long videoId, Model model,
                        HttpServletRequest request) {
        Optional<Video> optionalVideo = videoRepository.findById(videoId);

        if (optionalVideo.isPresent()) {
            Video video = optionalVideo.get();
            // Получение всех комментариев для данного видео
            List<Comment> allComments = commentRepository.findByVideo(video);

            // Построение дерева комментариев
            Map<Comment, List<Comment>> commentTree = buildCommentTree(allComments);

            MarkType likeMark = markRepository.findByName("LIKE").get();
            MarkType dislikeMark = markRepository.findByName("DISLIKE").get();

            model.addAttribute("video", video);
            model.addAttribute("commentTree", commentTree);
            model.addAttribute("subscribers", 777);
            model.addAttribute("videoViews", videoViewsRepository.countByVideo(video));
            model.addAttribute("videoLikes", videoMarkRepository.countByVideoAndMark(video, likeMark));
            model.addAttribute("videoDislikes", videoMarkRepository.countByVideoAndMark(video, dislikeMark));
            model.addAttribute("likeId", likeMark.getMarkId());
            model.addAttribute("dislikeId", dislikeMark.getMarkId());
        } else {
            return "redirect:/404";
        }
        return "video";
    }

//    @PostMapping("/video/{videoId}/comment")
//    public String addComment(@PathVariable Long videoId,
//                             @RequestParam("commentText") String commentText,
//                             Principal principal) {
//        Optional<Video> videoOptional = videoRepository.findById(videoId);
//        if (videoOptional.isPresent() && principal != null) {
//            Video video = videoOptional.get();
//            Optional<User> userOptional = userRepository.findByLogin(principal.getName());
//            User user = userOptional.get();
//
//            Comment comment = new Comment();
//            comment.setText(commentText);
//            comment.setVideo(video);
//            comment.setUser(user);
//            comment.setIsModified(false);
//
//            commentRepository.save(comment);
//        }
//
//        return "redirect:/video/" + videoId;
//    }

    @GetMapping("/search")
    public String search() {
        return "video_search";
    }
//
//
//    @PostMapping("/video/{videoId}/comment/{commentId}/like")
//    public String likeComment(@PathVariable Long videoId,
//                              @PathVariable Long commentId,
//                              @RequestParam("markId") Long markId,
//                              Principal principal) {
//        if (principal != null) {
//            Optional<User> userOptional = userRepository.findByLogin(principal.getName());
//            User user = userOptional.get();
//            Optional<Comment> commentOptional = commentRepository.findById(commentId);
//
//            if (commentOptional.isPresent()) {
//                Comment comment = commentOptional.get();
//                UserCommentMark userCommentMark = new UserCommentMark();
//                userCommentMark.setComment(comment);
//                userCommentMark.setUser(user);
//                Optional<MarkType> mark = markRepository.findById(markId);
//                if (mark.isPresent()) {
//                    userCommentMark.setMark(mark.get());
//                    userCommentMarkRepository.save(userCommentMark);
//                }
//            }
//        }
//        return "redirect:/video/" + videoId;
//    }

}
