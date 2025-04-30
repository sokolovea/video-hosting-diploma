package ru.rsreu.videohosting.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.videohosting.dto.RegistrationDTO;
import ru.rsreu.videohosting.entity.Role;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.repository.RoleRepository;
import ru.rsreu.videohosting.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StorageService storageService;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       StorageService storageService, @Autowired PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.storageService = storageService;
        this.passwordEncoder = passwordEncoder;
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

        if (!dto.getImagePath().isEmpty()) {
            String filename = storageService.store(dto.getImagePath(), ContentMultimediaType.LOGO);
            user.setImagePath(filename);
        }

        Role userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new IllegalStateException("Роль USER не найдена"));
        user.getRoles().add(userRole);

        userRepository.save(user);
    }

//    public User getCurrentUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        Long userId = 0L;
//        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
//            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//            String username = userDetails.getUsername();
//            userId = userRepository.findByLogin(username).get().getUserId();
//        }
//        return this.userRepository.getReferenceById(userId);
//    }
}
