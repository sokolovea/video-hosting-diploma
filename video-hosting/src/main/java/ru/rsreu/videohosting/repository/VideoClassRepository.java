package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.videohosting.entity.VideoClass;
import ru.rsreu.videohosting.repository.composite.VideoClassId;

import java.util.Optional;

public interface VideoClassRepository extends JpaRepository<VideoClass, VideoClassId> {
    Optional<VideoClass> findByVideoClassClassName(String className);
}
