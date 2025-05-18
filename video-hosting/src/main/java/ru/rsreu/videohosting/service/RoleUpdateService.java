package ru.rsreu.videohosting.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.videohosting.dao.JdbcRatingDao;
import ru.rsreu.videohosting.entity.MultimediaClass;
import ru.rsreu.videohosting.entity.Role;
import ru.rsreu.videohosting.entity.RoleAssignment;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.repository.MultimediaClassRepository;
import ru.rsreu.videohosting.repository.RoleAssignmentRepository;
import ru.rsreu.videohosting.repository.RoleRepository;
import ru.rsreu.videohosting.repository.UserRepository;

import java.util.List;
import java.util.Map;

@Service
public class RoleUpdateService {

    private final RoleAssignmentRepository roleAssignmentRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final JdbcRatingDao jdbcRatingDao;
    private final MultimediaClassRepository multimediaClassRepository;

    @Autowired
    public RoleUpdateService(RoleAssignmentRepository roleAssignmentRepository, RoleRepository roleRepository, UserRepository userRepository, JdbcRatingDao jdbcRatingDao, MultimediaClassRepository multimediaClassRepository) {
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.jdbcRatingDao = jdbcRatingDao;
        this.multimediaClassRepository = multimediaClassRepository;
    }

    /**
     * Проверяет рейтинги пользователей каждые 15 минут
     */
    @Scheduled(fixedRate = 5 * 1000)
    @Transactional
    public void updateRolesBasedOnRatings() {

//        System.out.println("Updating roles based on ratings");

        // Получаем все назначения ролей
        List<User> users = userRepository.findAll();
        List<MultimediaClass> multimediaClasses = multimediaClassRepository.findAll();

        for (User user : users) {
            Map<MultimediaClass, Double> ratingsByClasses = jdbcRatingDao.getUserRating(user, multimediaClasses);
            for (MultimediaClass multimediaClass : ratingsByClasses.keySet()) {
                Double userRating = ratingsByClasses.get(multimediaClass);
                RoleAssignment assignment = roleAssignmentRepository.findByReceiverAndMultimediaClass(user, multimediaClass);
                Role newRole = null;
                if (userRating >= 1 && !assignment.getRole().getRoleName().equals("EXPERT")) {
                    // Назначаем новую роль
                    newRole = roleRepository.findByRoleName("EXPERT")
                            .orElseThrow(() -> new IllegalStateException("Роль EXPERT не найдена"));
                    // Логируем изменение
                } else if (userRating < 1 && !assignment.getRole().getRoleName().equals("USER")) {
                    newRole = roleRepository.findByRoleName("USER")
                            .orElseThrow(() -> new IllegalStateException("Роль USER не найдена"));
                } else {
                    continue;
                }
                assignment.setRole(newRole);

                System.out.println("Роль пользователя " + user.getLogin() +
                        " обновлена на " + newRole.getRoleName() +
                        " для класса " + assignment.getMultimediaClass().getMultimediaClassName());

                roleAssignmentRepository.save(assignment);
            }
        }
    }
}

