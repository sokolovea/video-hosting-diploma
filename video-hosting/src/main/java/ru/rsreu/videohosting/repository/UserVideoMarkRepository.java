package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.videohosting.entity.MarkType;
import ru.rsreu.videohosting.entity.UserVideoMark;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.repository.composite.UserVideoMarkId;

public interface UserVideoMarkRepository extends JpaRepository<UserVideoMark, UserVideoMarkId> {
    Long countByVideoAndMark(Video video, MarkType mark);
}
