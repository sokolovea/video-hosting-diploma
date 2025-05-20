package ru.rsreu.videohosting.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.rsreu.videohosting.dao.JdbcRatingDao;
import ru.rsreu.videohosting.dto.IStringBooleanDto;
import ru.rsreu.videohosting.dto.UserProfileDTO;
import ru.rsreu.videohosting.dto.UserProfileEditDto;
import ru.rsreu.videohosting.dto.playlist.RatingUserProfileDto;
import ru.rsreu.videohosting.entity.*;
import ru.rsreu.videohosting.repository.*;
import ru.rsreu.videohosting.service.BoostDetectionService;
import ru.rsreu.videohosting.service.UserService;
import ru.rsreu.videohosting.util.RatingConverter;

import javax.validation.Valid;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class VideoHostingController {

    private final VideoViewsRepository videoViewsRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PlaylistRepository playlistRepository;

    private final JdbcRatingDao jdbcRatingDao;
    private final MultimediaClassRepository multimediaClassRepository;

    private final RoleAssignmentRepository roleAssignmentRepository;

    public VideoHostingController(@Autowired VideoRepository videoRepository,
                                  @Autowired MarkRepository markRepository,
                                  @Autowired VideoViewsRepository videoViewsRepository,
                                  @Autowired UserRepository userRepository,
                                  @Autowired UserService userService,
                                  @Autowired JdbcRatingDao jdbcRatingDao,
                                  @Autowired MultimediaClassRepository multimediaClassRepository,
                                  @Autowired PlaylistRepository playlistRepository,
                                  @Autowired BoostDetectionService boostDetectionService, RoleAssignmentRepository roleAssignmentRepository) {
        this.videoViewsRepository = videoViewsRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.jdbcRatingDao = jdbcRatingDao;
        this.multimediaClassRepository = multimediaClassRepository;
        this.playlistRepository = playlistRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
    }

    @GetMapping("/")
    public String showAllMostRelevantVideos() {
        return "redirect:/search?sortBy=relevance_user";
    }

    @GetMapping("/history_views")
    public String showHistoryView(Model model, Principal principal) {
        List<VideoViews> videoViews = videoViewsRepository.findByUserOrderByViewedAtDesc(userRepository.findByLogin(principal.getName()).get());
        model.addAttribute("videoViews", videoViews);
        return "history_views";
    }

    @GetMapping("/profile/{userId}")
    public String profile(Model model, Principal principal,
                          @PathVariable(required = false) Long userId) {
        String username = principal == null ? null : principal.getName();
        Optional <User> optionalUser = Optional.empty();
        if (username != null) {
            optionalUser = userRepository.findByLogin(username);
        }
        boolean isCurrentUserEquals = optionalUser.isPresent() && Objects.equals(optionalUser.get().getUserId(), userId);
        model.addAttribute("isCurrentUserEquals", isCurrentUserEquals);
        User user = null;
        if (isCurrentUserEquals) {
            user = optionalUser.get();
        } else {
            Optional<User> tempUser = userRepository.findById(userId);
            if (tempUser.isPresent()) {
                user = tempUser.get();
            } else {
                return "redirect:/404";
            }

        }
        Map<MultimediaClass, Double> mapUserRating = jdbcRatingDao.getUserRating(user, multimediaClassRepository.getAllMultimediaClasses());
        UserProfileDTO profileDto = new UserProfileDTO(
                user.getUserId(),
                user.getLogin(),
                user.getSurname(),
                user.getName(),
                user.getPatronymic(),
                user.getEmail(),
                user.getTelephone(),
                user.getImagePath(),
                user.getCreatedAt().toLocalDate(),
                user.getIsBlocked()
        );

        List<RatingUserProfileDto> listUserRatings = RatingConverter.convertAndSort(mapUserRating);

        List<IStringBooleanDto> rawResults = roleAssignmentRepository.findExpertCategoriesByReceiver(user);

        Map<String, Boolean> rolesByClasses =  rawResults.stream()
                .collect(Collectors.toMap(
                        IStringBooleanDto::getString,
                        IStringBooleanDto::getBoolean
                ));

        model.addAttribute("user", profileDto);
        model.addAttribute("ratings", listUserRatings);
        model.addAttribute("rolesByClasses", rolesByClasses);
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String profileEdit(Model model, Principal principal) {
        String username = principal == null ? null : principal.getName();
        User user = userRepository.findByLogin(username).get();
        UserProfileEditDto profileEditDto = new UserProfileEditDto(
                user.getUserId(),
                user.getLogin(),
                "",
                user.getSurname(),
                user.getName(),
                user.getPatronymic(),
                user.getEmail(),
                user.getTelephone(),
                null,
                user.getIsBlocked()
        );

        model.addAttribute("user", profileEditDto);
        return "profile_edit";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        String username = principal == null ? null : principal.getName();
        if (username == null || username.isEmpty()) {
            return "redirect:/404";
        }
        User user = userRepository.findByLogin(username).get();
        return "redirect:/profile/" + user.getUserId();
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
            return "redirect:/profile/" + userProfileEditDto.getUserId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка обновления профиля. Попробуйте позднее");
            return "redirect:/profile/edit";
        }
    }

    @GetMapping("/playlist")
    public String playlist(Model model, Principal principal) {
        String username = principal == null ? null : principal.getName();
        User user = userRepository.findByLogin(username).get();
        UserProfileDTO profileDto = new UserProfileDTO(
                user.getUserId(),
                user.getLogin(),
                user.getSurname(),
                user.getName(),
                user.getPatronymic(),
                user.getEmail(),
                user.getTelephone(),
                user.getImagePath(),
                user.getCreatedAt().toLocalDate(),
                user.getIsBlocked()
        );

        List<Playlist> playlists = playlistRepository.findByUser(user);

        model.addAttribute("playlists", playlists);
        model.addAttribute("user", profileDto);
        model.addAttribute("selectedPlaylist", new Playlist());
        return "playlist";
    }

}
