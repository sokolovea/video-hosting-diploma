package ru.rsreu.videohosting.util;

import ru.rsreu.videohosting.dto.playlist.RatingUserProfileDto;
import ru.rsreu.videohosting.entity.MultimediaClass;

import java.util.*;
import java.util.stream.Collectors;

public class RatingConverter {
    public static List<RatingUserProfileDto> convertAndSort(Map<MultimediaClass, Double> mapUserRating) {
        return mapUserRating.entrySet().stream()
                .map(entry -> new RatingUserProfileDto(entry.getKey().getMultimediaClassName(), entry.getValue()))
                .sorted(Comparator.comparing(RatingUserProfileDto::getCategoryName))
                .collect(Collectors.toList());
    }
}
