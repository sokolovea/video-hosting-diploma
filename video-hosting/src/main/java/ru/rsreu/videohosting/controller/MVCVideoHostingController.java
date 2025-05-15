package ru.rsreu.videohosting.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.rsreu.videohosting.dao.JdbcRatingDao;
import ru.rsreu.videohosting.dto.*;
import ru.rsreu.videohosting.entity.*;
import ru.rsreu.videohosting.entity.MultimediaClass;
import ru.rsreu.videohosting.repository.*;
import ru.rsreu.videohosting.repository.composite.SubscriptionId;
import ru.rsreu.videohosting.service.*;

import javax.management.DescriptorKey;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private final JdbcRatingDao jdbcRatingDao;


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
                                     @Autowired SubscriptionRepository subscriptionRepository,
                                     @Autowired RoleRepository roleRepository,
                                     @Autowired JdbcRatingDao jdbcRatingDao) {
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
        this.jdbcRatingDao = jdbcRatingDao;
    }


    @GetMapping("/upload_video")
    public String uploadVideo(Model model) {
        List<String> multimediaClasses = multimediaClassRepository.getAllMultimediaClassNames();
        model.addAttribute("multimediaClasses", multimediaClasses);
        model.addAttribute("video", new VideoUploadDTO());
        model.addAttribute("formName", "Загрузка видео");
        model.addAttribute("submitButtonName", "Загрузить");
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
        model.addAttribute("submitButtonName", "Изменить");
        return "upload_video";
    }


    @PostMapping("/upload_video")
    public String uploadVideo(@Validated @ModelAttribute("video") VideoUploadDTO videoUploadDTO,
                              BindingResult result,
                              Principal principal,
                              Model model) {

        if (principal == null) {
            return "redirect:/403";
        }

        List<String> multimediaClasses = multimediaClassRepository.getAllMultimediaClassNames();
        model.addAttribute("multimediaClasses", multimediaClasses);
        model.addAttribute("video", videoUploadDTO);

        Optional<User> optionalUser = userRepository.findByLogin(principal.getName());
        Video video = new Video();

        if (videoUploadDTO.getVideoId() == null) {
            model.addAttribute("formName", "Загрузка видео");
            model.addAttribute("submitButtonName", "Загрузить");
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
            model.addAttribute("submitButtonName", "Изменить");
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
            Set<MultimediaClass> multimediaClassesSet = multimediaClassRepository.findAllByMultimediaClassNameIn(videoUploadDTO.getClassesString());
            video.setMultimediaClasses(multimediaClassesSet);
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

    @Transactional
    @DeleteMapping("/video")
    public String deleteVideo(@RequestParam("videoId") Long videoId,
                            RedirectAttributes redirectAttributes,
                            Principal principal) {

        if (principal == null || videoId == null) {
            return "redirect:/403";
        }
        User user = userRepository.findByLogin(principal.getName()).get();
        Optional<Video> optionalVideo = videoRepository.findById(videoId);
        if (optionalVideo.isEmpty()) {
            return "redirect:/404";
        }
        Video video = optionalVideo.get();
        User author = video.getAuthor();
        if (!Objects.equals(author.getUserId(), user.getUserId())) {
            return "redirect:/403";
        }

        videoMarkRepository.deleteByVideo(video);
        commentRepository.deleteByVideo(video);
        videoViewsRepository.deleteByVideo(video);
        videoRepository.delete(video);
        redirectAttributes.addFlashAttribute("success", "Удаление видео произведено успешно!");
        return "redirect:/profile";
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

            // var a = jdbcRatingDao.getVideoRating(video); //DEBUG
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

//            HashSet<Role> roles = new HashSet<>();
//            roles.add(roleRepository.findByRoleName("USER").get());
            model.addAttribute("videoLikes", videoMarkRepository.countByVideoAndMark(video, likeMark));
            model.addAttribute("videoDislikes", videoMarkRepository.countByVideoAndMark(video, dislikeMark));
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

    public Map<Video, Long> getVideoViewCounts(List<Video> videos) {
        List<Object[]> results = videoViewsRepository.countViewsByVideos(videos);

        // Преобразуем в Map
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Video) result[0], // Ключ — объект Video
                        result -> (Long) result[1]   // Значение — количество просмотров
                ));
    }

    @GetMapping("/search")
    public String search(
            @RequestParam("query") String query,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "sortBy", defaultValue = "rating_expert") String sortBy,
            @RequestParam(value = "reverseOrder", required = false, defaultValue = "false") Boolean reverseOrder,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate endDate,
            @RequestParam(value = "authorId", required = false) Long authorId,
            Model model) {

        model.addAttribute("categories", multimediaClassRepository.findAll());
        model.addAttribute("category", category);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("authorId", authorId);
        model.addAttribute("reverseOrder", reverseOrder);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        model.addAttribute("startDate", startDate == null ? null : startDate.format(formatter));
        model.addAttribute("endDate", endDate == null ? null : endDate.format(formatter));


        // Получение видео по запросу и фильтрам
        List<Video> videos = videoRepository.findWithFilters(query, category, startDate == null ? null : startDate.atStartOfDay(),
                endDate == null ? null : endDate.atTime(23, 59, 59), authorId);

        // Сбор просмотров и рейтингов
        Map<Video, Long> videoViews = getVideoViewCounts(videos);
        Map<Long, Map<MultimediaClass, RatingDto>> allRatings = videos.stream()
                .collect(Collectors.toMap(
                        Video::getVideoId,
                        jdbcRatingDao::getVideoRating
                ));

        Map<Long, Map<MultimediaClass, RelevanceDTO>> allRelevance = videos.stream()
                .collect(Collectors.toMap(
                        Video::getVideoId,
                        jdbcRatingDao::getVideoRelevance
                ));

        MultimediaClass multimediaClass = null;
        if (category != null && !category.trim().isEmpty()) {
            multimediaClass = multimediaClassRepository.findByMultimediaClassName(category)
                    .orElse(null);
        }

        // Подсчет среднего рейтинга или извлечение конкретного
        Map<Video, RatingDto> aggregatedRatings = new HashMap<>();
        Map<Video, RelevanceDTO> aggregatedVideoRelevance = new HashMap<>();
        for (Video video : videos) {
            Map<MultimediaClass, RatingDto> videoRatings = allRatings.get(video.getVideoId());
            Map<MultimediaClass, RelevanceDTO> videoRelevance = allRelevance.get(video.getVideoId());

            Double userRating = null;
            Double expertRating = null;

            Double userRelevance = null;
            Double expertRelevance = null;

            if (multimediaClass == null) {
                // Средний рейтинг по всем категориям
                for (RatingDto ratingDto : videoRatings.values()) {
                    if (ratingDto.getRatingUser() != null) {
                        if (userRating == null) {
                            userRating = 0.0;
                        }
                        userRating += ratingDto.getRatingUser();
                    }
                    if (ratingDto.getRatingExpert() != null) {
                        if (expertRating == null) {
                            expertRating = 0.0;
                        }
                        expertRating += ratingDto.getRatingExpert();
                    }
                }
                if (userRating != null) {
                    userRating /= videoRatings.size();
                }
                if (expertRating != null) {
                    expertRating /= videoRatings.size();
                }

                for (RelevanceDTO relevanceDto : videoRelevance.values()) {
                    if (relevanceDto.getRelevanceUser() != null) {
                        if (userRelevance == null) {
                            userRelevance = 0.0;
                        }
                        userRelevance += relevanceDto.getRelevanceUser();
                    }
                    if (relevanceDto.getRelevanceExpert() != null) {
                        if (expertRelevance == null) {
                            expertRelevance = 0.0;
                        }
                        expertRelevance += relevanceDto.getRelevanceExpert();
                    }
                }
                if (userRelevance != null) {
                    userRelevance /= videoRelevance.size();
                }
                if (expertRelevance != null) {
                    expertRelevance /= videoRelevance.size();
                }
            } else {
                // Релевантность только для указанной категории
                RelevanceDTO relevanceDto = videoRelevance.get(multimediaClass);
                if (relevanceDto != null) {
                    if (relevanceDto.getRelevanceUser() != null) {
                        userRelevance = relevanceDto.getRelevanceUser();
                    }
                    if (relevanceDto.getRelevanceExpert() != null) {
                        expertRelevance = relevanceDto.getRelevanceExpert();
                    }
                }
            }

            aggregatedRatings.put(video, new RatingDto(userRating == null ? -1 : userRating,
                    expertRating == null ? -1 : expertRating));
            aggregatedVideoRelevance.put(video, new RelevanceDTO(userRelevance == null ? -1 : userRelevance,
                    expertRelevance == null ? -1 : expertRelevance));
        }


        // Сбор DTO и сортировка
        List<VideoSearchDto> videosDTO = videos.stream()
                .map(video -> {
                    RatingDto rating = aggregatedRatings.get(video);
                    RelevanceDTO relevance = aggregatedVideoRelevance.get(video);
                    return new VideoSearchDto(
                            video,
                            rating.getRatingUser(),
                            rating.getRatingExpert(),
                            relevance.getRelevanceUser(),
                            relevance.getRelevanceExpert(),
                            video.getCreatedAt(),
                            videoViews.getOrDefault(video, 0L)
                    );
                })
                .sorted(getComparator(sortBy, reverseOrder))
                .toList();

        // Добавление данных в модель
        model.addAttribute("query", query);
        model.addAttribute("videos", videosDTO);
        return "video_search";
    }

    private Comparator<VideoSearchDto> getComparator(String sortBy, boolean isReversedOrder) {
        Comparator<VideoSearchDto> comparator;

        switch (sortBy.toLowerCase()) {
            case "rating_user" ->
                    comparator = Comparator.comparing(VideoSearchDto::getVideoRatingUser);
            case "rating_expert" ->
                    comparator = Comparator.comparingDouble(VideoSearchDto::getVideoRatingExpert);
            case "relevance_user" ->
                    comparator = Comparator.comparing(VideoSearchDto::getVideoRelevanceUser);
            case "relevance_expert" ->
                    comparator = Comparator.comparingDouble(VideoSearchDto::getVideoRelevanceExpert);
            case "newest" ->
                    comparator = Comparator.comparing(VideoSearchDto::getVideoUploadDate);
            case "title" ->
                    comparator = Comparator.comparing(VideoSearchDto::getVideoTitle).reversed();
            // Включая сортировку по кол-ву просмотров
            default ->
                    comparator = Comparator.comparingLong(VideoSearchDto::getViews);
        }
        if (isReversedOrder) {
            comparator = comparator.reversed();
        }
        return comparator.thenComparingLong(VideoSearchDto::getViews).reversed();
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
