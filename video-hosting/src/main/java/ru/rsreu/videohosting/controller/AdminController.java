package ru.rsreu.videohosting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.videohosting.repository.UserRepository;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;

//    @GetMapping("/dashboard")
//    public String adminPanel(Model model) {
//        List<UserDTO> users = jdbcTemplate.query(
//                "SELECT * FROM GetAllUsers()",
//                (rs, rowNum) -> new UserDTO(
//                        rs.getLong("user_id"),
//                        rs.getString("login"),
//                        rs.getString("email"),
//                        rs.getString("roles"),
//                        rs.getBoolean("is_blocked")
//                )
//        );
//
//        model.addAttribute("users", users);
//
//        Map<String, Object> stats = jdbcTemplate.queryForMap(
//                "SELECT * FROM GetContentStatistics()"
//        );
//
//        model.addAttribute("statistics", Map.of(
//                "totalVideos", stats.get("total_videos"),
//                "totalComments", stats.get("total_comments"),
//                "avgComments", stats.get("avg_comments_per_video"),
//                "blockedUsers", stats.get("total_blocked_users")
//        ));
//        return "admin_dashboard";
//    }
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



