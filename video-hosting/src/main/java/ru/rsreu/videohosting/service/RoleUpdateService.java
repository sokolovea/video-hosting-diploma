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

    public static final int MIN_RATING_TO_BE_EXPERT = 1;
    public static final double COEFFICIENT_RATING_DOWN = 0.9;

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
     * Проверяет рейтинги пользователей каждые 5 минут
     */
    @Scheduled(fixedRate = 5 * 1000)
    @Transactional
    public void updateRolesBasedOnRatings() {

        List<User> users = userRepository.findAll();
        List<MultimediaClass> multimediaClasses = multimediaClassRepository.findAll();

        for (User user : users) {
            Map<MultimediaClass, Double> ratingsByClasses = jdbcRatingDao.getUserRating(user, multimediaClasses);
            for (MultimediaClass multimediaClass : ratingsByClasses.keySet()) {
                Double userRating = ratingsByClasses.get(multimediaClass);
                RoleAssignment assignment = roleAssignmentRepository.findByReceiverAndMultimediaClass(user, multimediaClass);
                Role newRole = null;
                if (userRating >= MIN_RATING_TO_BE_EXPERT && !assignment.getRole().getRoleName().equals("EXPERT")) {
                    newRole = roleRepository.findByRoleName("EXPERT")
                            .orElseThrow(() -> new IllegalStateException("Роль EXPERT не найдена"));
                } else if (userRating < (MIN_RATING_TO_BE_EXPERT * COEFFICIENT_RATING_DOWN) && !assignment.getRole().getRoleName().equals("USER")) {
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

