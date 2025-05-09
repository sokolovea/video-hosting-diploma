package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.videohosting.entity.*;
import ru.rsreu.videohosting.repository.composite.UserVideoMarkId;

import java.util.Set;

public interface UserVideoMarkRepository extends JpaRepository<UserVideoMark, UserVideoMarkId> {
    Long countByVideoAndMarkAndUserRoles(Video video, MarkType mark, Set<Role> user_roles);
    Long countByVideo(Video video);
//    Long countByUserAndMark(User user, MarkType mark);
}
