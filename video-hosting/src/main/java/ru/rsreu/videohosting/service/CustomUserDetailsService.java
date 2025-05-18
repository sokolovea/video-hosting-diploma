package ru.rsreu.videohosting.service;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        Hibernate.initialize(user.getRoleAssignments());

        if (Boolean.TRUE.equals(user.getIsBlocked())) {
            throw new RuntimeException("User is blocked");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(
                        user.getRoleAssignments().stream()
                                .map(roleAssignment -> roleAssignment.getRole().getRoleName())
                                .distinct()
                                .toArray(String[]::new)
                )
                .authorities(user.getIsAdmin() ? new String[]{"ROLE_ADMIN"} : new String[]{}) // Добавляем `ROLE_ADMIN`, если `isAdmin=true`
                .build();
    }
}
