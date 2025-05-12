package ru.rsreu.videohosting.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.rsreu.videohosting.dto.RegistrationDTO;
import ru.rsreu.videohosting.dto.VideoSearchDto;
import ru.rsreu.videohosting.dto.VideoUploadDTO;
import ru.rsreu.videohosting.entity.*;
import ru.rsreu.videohosting.entity.MultimediaClass;
import ru.rsreu.videohosting.repository.*;
import ru.rsreu.videohosting.repository.composite.SubscriptionId;
import ru.rsreu.videohosting.service.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;


@SpringBootApplication
@Controller("/")
public class MVCVideoHostingController {

    private static final Logger log = LoggerFactory.getLogger(MVCVideoHostingController.class);
    private final MultimediaClassRepository multimediaClassRepository;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final MarkRepository markRepository;
    private final UserCommentMarkRepository userCommentMarkRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final VideoViewsRepository videoViewsRepository;
    private final UserVideoMarkRepository videoMarkRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final VideoService videoService;
    private final CustomWebClientService customWebClientService;
    private final CommentService commentService;
    private final RoleRepository roleRepository;


    public MVCVideoHostingController(@Autowired UserRepository userRepository,
                                     @Autowired MultimediaClassRepository multimediaClassRepository,
                                     @Autowired VideoRepository videoRepository,
                                     @Autowired CommentRepository commentRepository,
                                     @Autowired MarkRepository markRepository,
                                     @Autowired UserCommentMarkRepository commentMarkRepository,
                                     @Autowired StorageService storageService,
                                     @Autowired VideoViewsRepository videoViewsRepository,
                                     @Autowired UserVideoMarkRepository videoMarkRepository,
                                     @Autowired VideoService videoService,
                                     @Autowired CustomWebClientService customWebClientService,
                                     @Autowired CommentService commentService,
                                     @Autowired SubscriptionRepository subscriptionRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.multimediaClassRepository = multimediaClassRepository;
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
        this.subscriptionRepository = subscriptionRepository;
        this.roleRepository = roleRepository;
    }


    @GetMapping("/upload_video")
    public String uploadVideo(Model model) {
        List<String> multimediaClasses = multimediaClassRepository.getAllMultimediaClassNames();
        model.addAttribute("multimediaClasses", multimediaClasses);
        model.addAttribute("video", new VideoUploadDTO());
        model.addAttribute("formName", "Загрузка видео");
        return "upload_video";
    }

    @GetMapping("/video_edit/{video_id}")
    public String editVideo(@PathVariable(name = "video_id") Long videoId, Model model) {
        Optional<Video> optionalVideo = videoRepository.findById(videoId);
        if (optionalVideo.isEmpty()) {
            return "redirect:/404";
        }
        Video video = optionalVideo.get();
        List<String> multimediaClasses = multimediaClassRepository.getAllMultimediaClassNames();
        model.addAttribute("multimediaClasses", multimediaClasses);
        model.addAttribute("video", new VideoUploadDTO(
                video.getVideoId(),
                video.getTitle(),
                video.getDescription(),
                null,
                null,
                new ArrayList<String>()
        ));
        model.addAttribute("formName", "Редактирование видео");
        return "upload_video";
    }


    @PostMapping("/upload_video")
    public String uploadVideo(@Validated @ModelAttribute("video") VideoUploadDTO videoUploadDTO,
                              BindingResult result,
                              Principal principal,
                              Model model) {

        if (principal == null) {
            return "redirect:/404";
        }

        List<String> multimediaClasses = multimediaClassRepository.getAllMultimediaClassNames();
        model.addAttribute("multimediaClasses", multimediaClasses);

        Optional<User> optionalUser = userRepository.findByLogin(principal.getName());
        Video video = new Video();

        if (videoUploadDTO.getVideoId() == null) {
            model.addAttribute("formName", "Загрузка видео");
            if (videoUploadDTO.getImageFile() == null || videoUploadDTO.getImageFile().isEmpty()) {
                result.rejectValue("imageFile", "error.imageFile",  "Изображение должно быть выбрано!");
            }
            if (videoUploadDTO.getVideoFile() == null || videoUploadDTO.getVideoFile().isEmpty()) {
                result.rejectValue("videoFile", "error.videoFile", "Видеодорожка должна быть выбрана!");
            }
            if (videoUploadDTO.getClassesString() == null || videoUploadDTO.getClassesString().isEmpty()) {
                result.rejectValue("classesString", "error.classesString", "Хотя бы одна категория должна быть выбрана");
            }
            if (result.hasErrors()) {
                return "upload_video";
            }
        } else {
            model.addAttribute("formName", "Редактирование видео");
            if (videoRepository.existsByAuthorAndVideoId(optionalUser.get(), videoUploadDTO.getVideoId())) {
                video = videoRepository.findById(videoUploadDTO.getVideoId()).get();
            } else {
                return "redirect:/upload_video?error"; //DEBUG
            }
        }

        String imagePath = null;
        if (videoUploadDTO.getImageFile() != null && !videoUploadDTO.getImageFile().isEmpty()) {
            imagePath = this.storageService.store(videoUploadDTO.getImageFile(), ContentMultimediaType.VIDEO_IMAGE);
        }
        String videoPath = null;
        if (videoUploadDTO.getVideoFile() != null && !videoUploadDTO.getVideoFile().isEmpty()) {
            videoPath = this.storageService.store(videoUploadDTO.getVideoFile(), ContentMultimediaType.VIDEO);
        }


        video.setTitle(videoUploadDTO.getTitle());
        video.setDescription(videoUploadDTO.getDescription());
        if (videoPath != null) {
            video.setVideoPath(videoPath);
        }
        if (imagePath != null) {
            video.setImagePath(imagePath);
        }

        if (videoUploadDTO.getClassesString() != null && !videoUploadDTO.getClassesString().isEmpty()) {
            Set<MultimediaClass> multimediaClasses = multimediaClassRepository.findAllByMultimediaClassNameIn(videoUploadDTO.getClassesString());
            video.setMultimediaClasses(multimediaClasses);
        }

        if (optionalUser.isPresent()) {
            video.setAuthor(optionalUser.get());
            videoRepository.save(video);
            return String.format("redirect:/video/%d", video.getVideoId());
        }
        return "redirect:/upload_video?error"; //DEBUG
    }


    @PostMapping("/video_edit")
    public String editVideo(@RequestParam("video") VideoUploadDTO videoUploadDTO,
                              BindingResult result,
                              Principal principal) {

        if (principal == null) {
            return "redirect:/404";
        }

        if (result.hasErrors()) {
            return "redirect:/upload_video?error";
        }


        String imagePath = this.storageService.store(videoUploadDTO.getVideoFile(), ContentMultimediaType.VIDEO_IMAGE);
        String videoPath = this.storageService.store(videoUploadDTO.getImageFile(), ContentMultimediaType.VIDEO);


        Video video = new Video();
        video.setTitle(videoUploadDTO.getTitle());
        video.setDescription(videoUploadDTO.getDescription());
        video.setVideoPath(videoPath);
        video.setImagePath(imagePath);

        Set<MultimediaClass> multimediaClasses = multimediaClassRepository.
                findAllByMultimediaClassNameIn(videoUploadDTO.getClassesString());

        if (multimediaClasses.isEmpty()) {
            result.rejectValue("classesString", "Должен быть выбран хотя бы один класс");
            return "redirect:/upload_video?error";
        }

        video.setMultimediaClasses(multimediaClasses);
        Optional<User> optionalUser = userRepository.findByLogin(principal.getName());
        if (optionalUser.isPresent()) {
            video.setAuthor(optionalUser.get());
            videoRepository.save(video);
            return String.format("redirect:/video/%d", video.getVideoId());
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
                        HttpServletRequest request,
                        Principal principal) {
        Optional<Video> optionalVideo = videoRepository.findById(videoId);

        if (optionalVideo.isPresent()) {
            Video video = optionalVideo.get();
            // Получение всех комментариев для данного видео
            List<Comment> allComments = commentRepository.findByVideo(video);

            // Построение дерева комментариев
            Map<Comment, List<Comment>> commentTree = buildCommentTree(allComments);

            MarkType likeMark = markRepository.findByName("LIKE").get();
            MarkType dislikeMark = markRepository.findByName("DISLIKE").get();

            Long currentUserId = null;
            try {
                if(principal != null) {
                    currentUserId = userRepository.findByLogin(principal.getName()).get().getUserId();
                }
            } catch (Exception ignored) {}

            model.addAttribute("currentUserId", currentUserId);
            model.addAttribute("video", video);
            model.addAttribute("commentTree", commentTree);
            model.addAttribute("subscribers", subscriptionRepository.countByAuthor(video.getAuthor()));
            model.addAttribute("videoViews", videoViewsRepository.countByVideo(video));

            HashSet<Role> roles = new HashSet<>();
            roles.add(roleRepository.findByRoleName("USER").get());
            model.addAttribute("videoLikes", videoMarkRepository.countByVideoAndMarkAndUserRoles(video, likeMark, roles));
            model.addAttribute("videoDislikes", videoMarkRepository.countByVideoAndMarkAndUserRoles(video, dislikeMark, roles));
            model.addAttribute("likeId", likeMark.getMarkId());
            model.addAttribute("dislikeId", dislikeMark.getMarkId());

            boolean isSubscribed = false;
            User author = video.getAuthor();
            if (principal != null) {
                Optional<User> currentUser = userRepository.findByLogin(principal.getName());
                if (currentUser.isPresent()) {
                    isSubscribed = subscriptionRepository.existsById(new SubscriptionId(currentUser.get().getUserId(), author.getUserId()));
                }
            }
            model.addAttribute("isSubscribed", isSubscribed);
            model.addAttribute("authorId", author.getUserId());
        } else {
            return "redirect:/404";
        }
        return "video";
    }

    @GetMapping("/search")
    public String search(@RequestParam("query") String query, Model model) {
        List<Video> videos = videoRepository.findByTitleContaining(query);
        List<VideoSearchDto> videosDTO= new ArrayList<VideoSearchDto>();
        for (Video video : videos) {
            videosDTO.add(new VideoSearchDto(
                    video,
                    1, 2, videoViewsRepository.countByVideo(video)
            ));
        }
        model.addAttribute("query", query);
        model.addAttribute("videos", videosDTO);
        return "video_search";
    }


    @GetMapping("/subscriptions")
    public String subscriptions(Model model, Principal principal) {
        User user = userRepository.findByLogin(principal.getName()).get();
        List<Subscription> subscriptions = subscriptionRepository.findBySubscriberOrderBySubscribedAtDesc(user);
        model.addAttribute("subscriptions", subscriptions);
        return "subscriptions";
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
