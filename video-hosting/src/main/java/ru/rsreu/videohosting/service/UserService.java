package ru.rsreu.videohosting.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.videohosting.dto.RegistrationDTO;
import ru.rsreu.videohosting.dto.UserDTO;
import ru.rsreu.videohosting.dto.UserProfileEditDto;
import ru.rsreu.videohosting.entity.MultimediaClass;
import ru.rsreu.videohosting.entity.Role;
import ru.rsreu.videohosting.entity.RoleAssignment;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.repository.MultimediaClassRepository;
import ru.rsreu.videohosting.repository.RoleRepository;
import ru.rsreu.videohosting.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StorageService storageService;
    private final PasswordEncoder passwordEncoder;
    private final MultimediaClassRepository multimediaClassRepository;


    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       StorageService storageService, @Autowired PasswordEncoder passwordEncoder, MultimediaClassRepository multimediaClassRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.storageService = storageService;
        this.passwordEncoder = passwordEncoder;
        this.multimediaClassRepository = multimediaClassRepository;
    }

    @Transactional
    public void registerNewUser(RegistrationDTO dto) {
        User user = new User();
        user.setLogin(dto.getLogin());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setSurname(dto.getSurname());
        user.setName(dto.getName());
        user.setPatronymic(dto.getPatronymic());
        user.setEmail(dto.getEmail());
        user.setTelephone(dto.getTelephone());
        user.setCreatedAt(LocalDateTime.now());
        user.setImagePath(dto.getImagePath().toString());
        user.setIsAdmin(false);

        if (!dto.getImagePath().isEmpty()) {
            String filename = storageService.store(dto.getImagePath(), ContentMultimediaType.LOGO);
            user.setImagePath(filename);
        }

        Role userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new IllegalStateException("Роль USER не найдена"));
        List<MultimediaClass> multimediaClasses = multimediaClassRepository.getAllMultimediaClasses();
        for (MultimediaClass multimediaClass: multimediaClasses) {
            user.getRoleAssignments().add(
                    new RoleAssignment(user, userRole, multimediaClass, LocalDateTime.now()));
        }

        userRepository.save(user);
    }

    @Transactional
    public void updateUser(UserProfileEditDto dto) {
        User user = userRepository.findByLogin(dto.getLogin()).get();
        if (!dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setSurname(dto.getSurname());
        user.setName(dto.getName());
        user.setPatronymic(dto.getPatronymic());
        user.setEmail(dto.getEmail());
        user.setTelephone(dto.getTelephone());

        if (!dto.getImagePath().isEmpty()) {
            String filename = storageService.store(dto.getImagePath(), ContentMultimediaType.LOGO);
            user.setImagePath(filename);
        }

        userRepository.save(user);
    }

    @Transactional
    public boolean isUserExist(String login) {
        return userRepository.findByLogin(login).isPresent();
    }

    @Transactional
    public boolean isUserExist(long userId) {
        return userRepository.findById(userId).isPresent();
    }

    @Transactional
    public boolean isUserWithCurrentEmailExist(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public void blockUser(Long userId) {
        if (userId != null) {
            userRepository.updateBlockedStatus(userId, true);
        }
    }

    @Transactional
    public void unblockUser(Long userId) {
        if (userId != null) {
            userRepository.updateBlockedStatus(userId, false);
        }
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(
                value -> new UserDTO(
                        value.getUserId(),
                        value.getLogin(),
                        value.getEmail(),
                        value.getRoleAssignments().toString(),
                        value.getIsBlocked(),
                        "/profile/" + String.valueOf(value.getUserId())
                )
        ).toList();
    }
}
