package ru.rsreu.videohosting.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.rsreu.videohosting.dto.MutualLikePairWeight;
import ru.rsreu.videohosting.repository.*;
import ru.rsreu.videohosting.service.BoostDetectionService;
import ru.rsreu.videohosting.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    public static final int MIN_CYCLE_LENGTH = 2;
    public static final int MAX_CYCLE_LENGTH = 10;
    private final UserVideoMarkRepository userVideoMarkRepository;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final UserCommentMarkRepository userCommentMarkRepository;
    private final UserRepository userRepository;
    private final BoostDetectionService boostDetectionService;
    private final UserService userService;


    @Autowired
    public AdminController(@Autowired UserRepository userRepository,
                             @Autowired VideoRepository videoRepository,
                             @Autowired CommentRepository commentRepository,
                             @Autowired UserCommentMarkRepository commentMarkRepository,
                             @Autowired UserVideoMarkRepository videoMarkRepository,
                             @Autowired BoostDetectionService boostDetectionService,
                             @Autowired UserService userService) {
        this.userVideoMarkRepository = videoMarkRepository;
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
        this.userCommentMarkRepository = commentMarkRepository;
        this.boostDetectionService = boostDetectionService;
        this.userService = userService;
    }

    @GetMapping
    public String adminMainPage() {
        return "admin_main";
    }

    @GetMapping("/report")
    public String adminReport(Model model,
                              @RequestParam(name = "threshold", defaultValue = "10") Long threshold,
                              @RequestParam(name="dataType", defaultValue = "video") String dataType) {
        // Получаем общую статистику
        long totalUsers = userRepository.count();
        long expertsCount = userRepository.countExperts();
        long totalVideos = videoRepository.count();
        long totalComments = commentRepository.count();

        model.addAttribute("threshold", threshold);
        model.addAttribute("dataType", dataType);

        List<MutualLikePairWeight> directBoosts = Collections.emptyList();
        List<Pair<List<Long>, Long>> cyclicBoosts = Collections.emptyList();
        if (dataType.equals("video")) {
            directBoosts = userVideoMarkRepository.findMutualVideoMark(threshold); // Сервис для расчета прямых накруток
            cyclicBoosts = boostDetectionService.findUserMarkVideoCycles(MIN_CYCLE_LENGTH, MAX_CYCLE_LENGTH);
        }
        else {
            directBoosts = userCommentMarkRepository.findMutualCommentMark(threshold); // Сервис для расчета прямых накруток
            cyclicBoosts = boostDetectionService.findUserMarkCommentCycles(2, 10);
        }

        directBoosts = directBoosts.stream()
                .filter(dto -> (dto.getWeight() >= threshold) && (dto.getUserA() != dto.getUserB()))
                .collect(Collectors.toList());

        cyclicBoosts = cyclicBoosts.stream()
                .filter(pair -> pair.getRight() >= threshold)
                .collect(Collectors.toList());


        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("expertsCount", expertsCount);
        model.addAttribute("totalVideos", totalVideos);
        model.addAttribute("totalComments", totalComments);
        model.addAttribute("directBoosts", directBoosts);
        model.addAttribute("cyclicBoosts", cyclicBoosts);

        return "admin_dashboard";
    }

    @GetMapping("/action")
    public String getActionPge() {
        return "admin_actions";
    }

    @PostMapping("/users/block")
    public String blockOrUnblockUser(@RequestParam("userId") Long userId,
                                     @RequestParam("action") String action,
                                     RedirectAttributes redirectAttributes) {
        try {
            if ("block".equals(action)) {
                userService.blockUser(userId);
                redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно заблокирован.");
            } else if ("unblock".equals(action)) {
                userService.unblockUser(userId);
                redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно разблокирован.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Неизвестное действие.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Произошла ошибка: " + e.getMessage());
        }

        return "redirect:/profile/" + userId;
    }




//
//    @PostMapping("/toggle-block")
//    public String toggleBlock(@RequestParam Long userId) {
//        userRepository.findById(userId).ifPresent(user -> {
//            user.setIsBlocked(!user.getIsBlocked());
//            userRepository.save(user);
//        });
//        return "redirect:/admin";
//    }

}



