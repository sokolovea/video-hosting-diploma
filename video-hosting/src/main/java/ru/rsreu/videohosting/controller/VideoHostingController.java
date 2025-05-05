package ru.rsreu.videohosting.controller;

import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.rsreu.videohosting.dto.UserProfileDTO;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.entity.VideoViews;
import ru.rsreu.videohosting.repository.MarkRepository;
import ru.rsreu.videohosting.repository.UserRepository;
import ru.rsreu.videohosting.repository.VideoRepository;
import ru.rsreu.videohosting.repository.VideoViewsRepository;

import java.security.Principal;
import java.util.List;

@Controller
public class VideoHostingController {
    private final VideoRepository videoRepository;
    private final MarkRepository markRepository;
    private final VideoViewsRepository videoViewsRepository;
    private final UserRepository userRepository;

    public VideoHostingController(@Autowired VideoRepository videoRepository,
                                  @Autowired MarkRepository markRepository,
                                  @Autowired VideoViewsRepository videoViewsRepository, UserRepository userRepository) {
        this.videoRepository = videoRepository;
        this.markRepository = markRepository;
        this.videoViewsRepository = videoViewsRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String showAllMostPopularVideos(Model model) {
        List<Video> videos = videoRepository.findAll();
        model.addAttribute("videos", videos);
        return "index";
    }

    @GetMapping("/history_views")
    public String showHistoryView(Model model, Principal principal) {
        List<VideoViews> videoViews = videoViewsRepository.findByUserOrderByViewedAtDesc(userRepository.findByLogin(principal.getName()).get());
        model.addAttribute("videoViews", videoViews);
        return "history_views";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByLogin(username).get();
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
}
