package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.videohosting.entity.History;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.repository.composite.HistoryId;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History, HistoryId> {
    List<History> findByUser(User user);
    List<History> findByVideo(Video video);
}
