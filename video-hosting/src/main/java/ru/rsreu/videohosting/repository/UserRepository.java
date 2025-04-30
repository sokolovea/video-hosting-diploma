package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.videohosting.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    Optional<User> findByEmail(String email);
}
