package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.videohosting.entity.VideoClass;
import ru.rsreu.videohosting.repository.composite.VideoClassId;

public interface VideoClassRepository extends JpaRepository<VideoClass, VideoClassId> {

}
