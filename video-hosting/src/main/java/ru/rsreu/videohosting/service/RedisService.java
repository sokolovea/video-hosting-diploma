package ru.rsreu.videohosting.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.rsreu.videohosting.dto.CountSumDto;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    public static final int TIMEOUT_MINUTES = 5;
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String RATING_USER_KEY_PREFIX = "user:rating:";
    private static final String RATING_VIDEO_KEY_PREFIX = "video:rating:";
    private static final String RELEVANCE_VIDEO_KEY_PREFIX = "video:relevance:";

    public void saveRatingUser(Long userId, Long classId, double rating) {
        String key = RATING_USER_KEY_PREFIX + userId + ":" + classId;
        redisTemplate.opsForValue().set(key, rating, TIMEOUT_MINUTES, TimeUnit.MINUTES); // Храним 5 минут
    }

    public Double getRatingUser(Long userId, Long classId) {
        String key = RATING_USER_KEY_PREFIX + userId + ":" + classId;
        Object rating = redisTemplate.opsForValue().get(key);
        return rating != null ? (Double) rating : null;
    }

    public void saveRatingVideo(Long videoId, Long classId, CountSumDto countSumDto) {
        String key = RATING_VIDEO_KEY_PREFIX + videoId + ":" + classId;
        redisTemplate.opsForValue().set(key, countSumDto, TIMEOUT_MINUTES, TimeUnit.MINUTES); // Храним 5 минут
    }

    public CountSumDto getRatingVideo(Long videoId, Long classId) {
        String key = RATING_VIDEO_KEY_PREFIX + videoId + ":" + classId;
        Object rating = redisTemplate.opsForValue().get(key);
        return rating != null ? (CountSumDto) rating : null;
    }

    public void saveRelevanceVideo(Long videoId, Long classId, double rating, Long userRoleId) {
        String key = RELEVANCE_VIDEO_KEY_PREFIX + videoId + ":" + classId + ":" + userRoleId;
        redisTemplate.opsForValue().set(key, rating, TIMEOUT_MINUTES, TimeUnit.MINUTES); // Храним 5 минут
    }

    public Double getRelevanceVideo(Long videoId, Long classId, Long userRoleId) {
        String key = RELEVANCE_VIDEO_KEY_PREFIX+ videoId + ":" + classId + ":" + userRoleId;
        Object rating = redisTemplate.opsForValue().get(key);
        return rating != null ? (Double) rating : null;
    }
}

