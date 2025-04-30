package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByAuthor(User author);
    List<Video> findByTitleContaining(String title);
}
