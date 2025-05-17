package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    Optional<User> findByEmail(String email);

    @Query("SELECT u from User u join Video v on u = v.author and v in :videos")
    List<User> findByVideosWhereAuthor(@Param("videos") List<Video> videos);

    @Query("SELECT count(distinct u.userId) from User u join RoleAssignment ra on u = ra.receiver join Role r on r = ra.role where r.roleName = 'EXPERT'")
    Long countExperts();

    @Modifying
    @Query("UPDATE User u SET u.isBlocked = :isBlocked WHERE u.userId = :userId")
    void updateBlockedStatus(Long userId, Boolean isBlocked);
}
