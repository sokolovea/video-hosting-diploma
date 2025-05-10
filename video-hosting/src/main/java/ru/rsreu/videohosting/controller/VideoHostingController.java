package ru.rsreu.videohosting.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.rsreu.videohosting.dao.JdbcRatingDao;
import ru.rsreu.videohosting.dto.UserProfileDTO;
import ru.rsreu.videohosting.dto.UserProfileEditDto;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.entity.VideoViews;
import ru.rsreu.videohosting.repository.*;
import ru.rsreu.videohosting.service.UserService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class VideoHostingController {
    private final VideoRepository videoRepository;
    private final MarkRepository markRepository;
    private final VideoViewsRepository videoViewsRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    private final JdbcRatingDao jdbcRatingDao;
    private final MultimediaClassRepository multimediaClassRepository;

    public VideoHostingController(@Autowired VideoRepository videoRepository,
                                  @Autowired MarkRepository markRepository,
                                  @Autowired VideoViewsRepository videoViewsRepository,
                                  @Autowired UserRepository userRepository,
                                  @Autowired UserService userService,
                                  @Autowired JdbcRatingDao jdbcRatingDao, MultimediaClassRepository multimediaClassRepository) {
        this.videoRepository = videoRepository;
        this.markRepository = markRepository;
        this.videoViewsRepository = videoViewsRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.jdbcRatingDao = jdbcRatingDao;
        this.multimediaClassRepository = multimediaClassRepository;
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
        var a = jdbcRatingDao.getUserRating(user, multimediaClassRepository.getAllMultimediaClasses());
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

    @GetMapping("/profile/edit")
    public String profileEdit(Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByLogin(username).get();
        UserProfileEditDto profileEditDto = new UserProfileEditDto(
                user.getLogin(),
                "",
                user.getSurname(),
                user.getName(),
                user.getPatronymic(),
                user.getEmail(),
                user.getTelephone(),
                null
        );

        model.addAttribute("user", profileEditDto);
        return "profile_edit";
    }


    @PostMapping("/profile/edit")
    public String registerUser(
            @ModelAttribute("user") @Valid UserProfileEditDto userProfileEditDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "profile_edit";
        }

        try {
            boolean isEmailUnique = true;
            if (userService.isUserWithCurrentEmailExist(userProfileEditDto.getEmail())) {
                Optional<User> userWithSuchEmail = userRepository.findByEmail(userProfileEditDto.getEmail());
                if (userWithSuchEmail.isPresent() && !userWithSuchEmail.get().getLogin().equals(userProfileEditDto.getLogin())) {
                    isEmailUnique = false;
                }
            }

            if (!isEmailUnique) {
                result.rejectValue("email", "email.exists" );
            }

            if (result.hasErrors()) {
                return "profile_edit";
            }

            userService.updateUser(userProfileEditDto);
            redirectAttributes.addFlashAttribute("success", "Обновление прошло успешно!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка обновления профиля. Попробуйте позднее");
            return "redirect:/profile/edit";
        }
    }
}
