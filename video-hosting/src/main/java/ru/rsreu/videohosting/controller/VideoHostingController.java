package ru.rsreu.videohosting.controller;

import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.repository.MarkRepository;
import ru.rsreu.videohosting.repository.VideoRepository;

import java.util.List;

@Controller
public class VideoHostingController {
    private final VideoRepository videoRepository;
    private final MarkRepository markRepository;

    public VideoHostingController(@Autowired VideoRepository videoRepository,
                                  @Autowired MarkRepository markRepository) {
        this.videoRepository = videoRepository;
        this.markRepository = markRepository;
    }

    @GetMapping("/")
    public String showAllMostPopularVideos(Model model) {
        List<Video> videos = videoRepository.findAll();
        model.addAttribute("videos", videos);
        return "index";
    }
}
