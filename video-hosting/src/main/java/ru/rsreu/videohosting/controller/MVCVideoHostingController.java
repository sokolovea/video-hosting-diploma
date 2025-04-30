package ru.rsreu.videohosting.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.rsreu.videohosting.dto.UserProfileDTO;
import ru.rsreu.videohosting.entity.*;
import ru.rsreu.videohosting.entity.Class;
import ru.rsreu.videohosting.repository.*;
import ru.rsreu.videohosting.service.ContentMultimediaType;
import ru.rsreu.videohosting.service.StorageService;

import javax.sql.DataSource;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;


@SpringBootApplication
@Controller("/")
public class MVCVideoHostingController {

    private static final Logger log = LoggerFactory.getLogger(MVCVideoHostingController.class);
    private final ClassRepository classRepository;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;


    public MVCVideoHostingController(@Autowired UserRepository userRepository,
                                     @Autowired ClassRepository classRepository,
                                     @Autowired VideoRepository videoRepository,
                                     @Autowired CommentRepository commentRepository,
                                     @Autowired UserCommentMarkRepository commentMarkRepository,
                                     @Autowired StorageService storageService) {
        this.userRepository = userRepository;
        this.classRepository = classRepository;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
        this.storageService = storageService;
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


    @GetMapping("/subscriptions")
    public String sub(Model model) {
        return "subscriptions";
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
                              @RequestParam("categories") List<String> classesString) throws IOException {

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
        video.setVideoPath(videoPath.toString());
        video.setImagePath(imagePath.toString());

        List<Class> classes = classRepository.findAllByClassNameIn(classesString);
        video.setClasses(classes);
        video.setAuthor(userRepository.findById(1L).get());

        videoRepository.save(video);

        return "redirect:/upload_video?success";
    }

    @GetMapping("/video/{videoId}")
    public String video(@PathVariable("videoId") Long videoId, Model model) {
        Optional<Video> optionalVideo = videoRepository.findById(videoId);

        if (optionalVideo.isPresent()) {
            Video video = optionalVideo.get();

            List<Comment> comments = commentRepository.findByVideo(video);

            model.addAttribute("video", video);
            model.addAttribute("comments", comments);
        } else {
            return "redirect:/404";
        }
        return "video";
    }

    @PostMapping("/video/{videoId}/comment")
    public String addComment(@PathVariable Long videoId,
                             @RequestParam("commentText") String commentText,
                             Principal principal) {
        Optional<Video> videoOptional = videoRepository.findById(videoId);
        if (videoOptional.isPresent() && principal != null) {
            Video video = videoOptional.get();
            Optional<User> userOptional = userRepository.findByLogin(principal.getName());
            User user = userOptional.get();

            Comment comment = new Comment();
            comment.setText(commentText);
            comment.setVideo(video);
            comment.setUser(user);
            comment.setIsModified(false);

            commentRepository.save(comment);
        }

        return "redirect:/video/" + videoId;
    }

    @GetMapping("/search")
    public String search() {
        return "video_search";
    }


//    @PostMapping("/video/{videoId}/comment/{commentId}/like")
//    public String likeComment(@PathVariable Long videoId,
//                              @PathVariable Long commentId,
//                              @RequestParam("like") boolean isLike,
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
//                if (isLike) {
//                    userCommentMark.setMark(MarkType.LIKE);
//                } else {
//                    userCommentMark.setMark(MarkType.DISLIKE);
//                }
//                userCommentMarkRepository.save(userCommentMark);
//            }
//        }
//        return "redirect:/video/" + videoId;
//    }

}
