package ru.rsreu.videohosting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.repository.VideoRepository;

import java.util.List;

@Controller
public class VideoHostingController {
    private final VideoRepository videoRepository;

    public VideoHostingController(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @GetMapping("/")
    public String showAllMostPopularVideos(Model model) {
        List<Video> videos = videoRepository.findAll();
        model.addAttribute("videos", videos);
        return "index";
    }
}
