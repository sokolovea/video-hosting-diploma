package ru.rsreu.videohosting.repository.custom;

import ru.rsreu.videohosting.entity.Video;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface VideoRepositoryCustom {
    List<Video> findWithFilters(String query, String category, LocalDateTime startDate, LocalDateTime endDate);
}
