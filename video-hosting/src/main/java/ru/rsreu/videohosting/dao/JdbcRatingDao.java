package ru.rsreu.videohosting.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.rsreu.videohosting.dto.CountSumDto;
import ru.rsreu.videohosting.dto.RatingDto;
import ru.rsreu.videohosting.dto.RelevanceDTO;
import ru.rsreu.videohosting.dto.VideoRatingDto;
import ru.rsreu.videohosting.entity.MultimediaClass;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.repository.*;
import ru.rsreu.videohosting.service.RedisService;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class JdbcRatingDao {
    private final JdbcTemplate jdbcTemplate;
    private final MarkRepository markRepository;
    private final UserVideoMarkRepository videoMarkRepository;
    private final UserCommentMarkRepository commentMarkRepository;
    private final UserRepository userRepository;
    private final UserVideoMarkRepository userVideoMarkRepository;


    private final String likesDislikesUserByVideoCount = """
            SELECT distinct count(*)
            FROM "user_video_mark"
                join "video" on "user_video_mark".video_id = "video".video_id
                join "user" on 	"video".author_id = "user".user_id
                join "mark_type" on "mark_type".mark_id = "user_video_mark".mark
                join "video_class" on "video_class".video_id = "video".video_id
                join "multimedia_class" on "multimedia_class".multimedia_class_id = "video_class".multimedia_class_id
            where "user".user_id = ? and "mark_type"."name" = ? and "multimedia_class".multimedia_class_name = ?
                and "user".user_id != "user_video_mark".user_id""";

    private final String likesDislikesUserByCommentsCount = """
            SELECT count(*)
            FROM "user_comment_mark"
            	join "comment" on "user_comment_mark".comment_id = "comment".comment_id
            	join "user" on 	"comment".user_id = "user".user_id
            	join "mark_type" on "mark_type".mark_id = "user_comment_mark".mark
            	join "video" on "comment".video_id = "video".video_id
            	join "video_class" on "video_class".video_id = "video".video_id
                join "multimedia_class" on "multimedia_class".multimedia_class_id = "video_class".multimedia_class_id
            where "user".user_id = ? and "mark_type"."name" = ? and "multimedia_class".multimedia_class_name = ?
                and "user".user_id != "user_comment_mark".user_id""";

    private final String likesDislikesVideoByUserClasses = """ 
            SELECT distinct "video".video_id, "multimedia_class".multimedia_class_id, "role".role_id, "user_video_mark".mark as mark_id, COUNT(DISTINCT "user_video_mark".user_id) AS count
            FROM "user_video_mark"
                join "video" on "user_video_mark".video_id = "video".video_id
                join "user" on 	"user_video_mark".user_id	= "user".user_id
                join "role_assignment" on "role_assignment".receiver_id = "user".user_id
                join "role" on "role".role_id = "role_assignment".role_id
                join "mark_type" on "mark_type".mark_id = "user_video_mark".mark
                join "video_class" on "video_class".video_id = "video".video_id
                join "multimedia_class" on "multimedia_class".multimedia_class_id = "video_class".multimedia_class_id
            where "video".video_id = ? and "user".user_id != "video".author_id
            group by "video".video_id, "multimedia_class"."multimedia_class_id", "role".role_id, "user_video_mark".mark
            order by "video".video_id asc, "multimedia_class".multimedia_class_id asc, "role".role_id asc, "user_video_mark".mark asc""";

    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final RoleRepository roleRepository;
    private final VideoViewsRepository videoViewsRepository;
    private final RedisService redisService;

    private final long userRoleId;
    private final long expertRoleId;
    private final long likeId;

    public JdbcRatingDao(@Autowired DataSource dataSource,
                         @Autowired MarkRepository markRepository,
                         @Autowired UserVideoMarkRepository videoMarkRepository,
                         @Autowired UserCommentMarkRepository commentMarkRepository,
                         @Autowired UserRepository userRepository,
                         @Autowired UserVideoMarkRepository userVideoMarkRepository,
                         @Autowired VideoRepository videoRepository,
                         @Autowired CommentRepository commentRepository,
                         @Autowired RoleRepository roleRepository,
                         @Autowired VideoViewsRepository videoViewsRepository, RedisService redisService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.markRepository = markRepository;
        this.videoMarkRepository = videoMarkRepository;
        this.commentMarkRepository = commentMarkRepository;
        this.userRepository = userRepository;
        this.userVideoMarkRepository = userVideoMarkRepository;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
        this.roleRepository = roleRepository;
        this.videoViewsRepository = videoViewsRepository;
        this.redisService = redisService;

        this.userRoleId = roleRepository.findByRoleName("USER").get().getRoleId();
        this.expertRoleId = roleRepository.findByRoleName("EXPERT").get().getRoleId();
        this.likeId = markRepository.findByName("LIKE").get().getMarkId();
    }

    public Map<MultimediaClass, Double> getUserRating(User user, List<MultimediaClass> classifications) {
        HashMap<MultimediaClass, Double> userRating = new HashMap<>();
        for (MultimediaClass classification : classifications) {

            Double cachedRating = redisService.getRatingUser(user.getUserId(), classification.getMultimediaClassId());
            if (cachedRating != null) {
                userRating.put(classification, cachedRating);
                continue;
            }

            Object[] paramsLikes = {user.getUserId(), "LIKE", classification.getMultimediaClassName()};
            Object[] paramsDislikes = {user.getUserId(), "DISLIKE", classification.getMultimediaClassName()};

            Long likesOnUserVideos = this.jdbcTemplate.queryForObject(likesDislikesUserByVideoCount,
                    paramsLikes, Long.class);
            Long dislikesOnUserVideos = this.jdbcTemplate.queryForObject(likesDislikesUserByVideoCount,
                    paramsDislikes, Long.class);
            long marksAssignedToUserVideoCount = likesOnUserVideos + dislikesOnUserVideos;
            long videosInClassCount = videoRepository.countAllByAuthorAndHavingClass(user, classification);
            double ratingByVideo = 0;
            if (marksAssignedToUserVideoCount != 0) {
                ratingByVideo = likesOnUserVideos / (double) marksAssignedToUserVideoCount - 0.5;
            }


            Long likesOnUserComments = this.jdbcTemplate.queryForObject(likesDislikesUserByCommentsCount,
                    paramsLikes, Long.class);
            Long dislikesOnUserComments = this.jdbcTemplate.queryForObject(likesDislikesUserByCommentsCount,
                    paramsDislikes, Long.class);
            long marksAssignedToUserByCommentsCount = likesOnUserComments + dislikesOnUserComments;
            long commentsInClassCount = commentRepository.countAllByAuthorAndHavingClass(user, classification);
            double ratingByComments = 0;
            if (marksAssignedToUserByCommentsCount != 0) {
                ratingByComments = likesOnUserComments / (double) marksAssignedToUserByCommentsCount - 0.5;
            }
            double finalRating = ratingByVideo * videosInClassCount * 0.9 + ratingByComments * commentsInClassCount * 0.1;
            userRating.put(classification, finalRating);

            redisService.saveRatingUser(user.getUserId(), classification.getMultimediaClassId(), finalRating);
        }
        return userRating;
    }

    public Map<MultimediaClass, RatingDto> getVideoRating(Video video) {

        Set<MultimediaClass> classifications = video.getMultimediaClasses();
        HashMap<Long, CountSumDto> videoSumMarksByClasses = new HashMap<>();

        boolean isDataCorrect = true;
        for (MultimediaClass classification : classifications) {
            CountSumDto countSumDto = new CountSumDto();
            countSumDto =  redisService.getRatingVideo(video.getVideoId(), classification.getMultimediaClassId());
            if (countSumDto == null) {
                countSumDto = new CountSumDto(0L, 0L, 0L, 0L);
                isDataCorrect = false;
            }
            videoSumMarksByClasses.put(classification.getMultimediaClassId(), countSumDto);
        }

        if (!isDataCorrect) {
            videoSumMarksByClasses.clear();
            for (MultimediaClass classification : classifications) {
                videoSumMarksByClasses.put(classification.getMultimediaClassId(),
                        new CountSumDto(0L, 0L, 0L, 0L));
            }

            Object[] params = {video.getVideoId()};

            List<VideoRatingDto> marksOnVideo = this.jdbcTemplate.query(
                    likesDislikesVideoByUserClasses,
                    params,
                    (rs, rowNum) -> new VideoRatingDto(
                            rs.getLong("video_id"),
                            rs.getLong("multimedia_class_id"),
                            rs.getLong("role_id"),
                            rs.getLong("mark_id"),
                            rs.getLong("count")
                    )
            );


            for (VideoRatingDto videoRatingDto : marksOnVideo) {
                boolean isExpert = Objects.equals(videoRatingDto.getRoleId(), expertRoleId);
                Long positiveMark = Objects.equals(videoRatingDto.getMarkId(), likeId) ? 1L : 0L;
                CountSumDto countSumDto = videoSumMarksByClasses.get(videoRatingDto.getMultimediaClassId());
                if (isExpert) {
                    countSumDto.setCountExpert(countSumDto.getCountExpert() + 1);
                    countSumDto.setSumExpert(countSumDto.getSumExpert() + positiveMark);
                } else {
                    countSumDto.setCountUser(countSumDto.getCountUser() + 1);
                    countSumDto.setSumUser(countSumDto.getSumUser() + positiveMark);
                }
                videoSumMarksByClasses.put(videoRatingDto.getMultimediaClassId(), countSumDto);
            }
            for (MultimediaClass classification : classifications) {
                redisService.saveRatingVideo(video.getVideoId(), classification.getMultimediaClassId(),
                        videoSumMarksByClasses.get(classification.getMultimediaClassId()));
            }
        }

        HashMap<MultimediaClass, RatingDto> videoRatingByClasses = new HashMap<>();
        for (MultimediaClass classification : classifications) {
            videoRatingByClasses.put(classification,
                    videoSumMarksByClasses.get(classification.getMultimediaClassId()).getRatingDto());
        }
        return videoRatingByClasses;
    }

    public Map<MultimediaClass, RelevanceDTO> getVideoRelevance(Video video) {
        Map<MultimediaClass, RelevanceDTO> resultVideoRelevance = new HashMap<>();
        Map<MultimediaClass, RatingDto> videoRatingByClasses = getVideoRating(video);
        double alpha = 0.3;
        double beta = 0.3;
        double gamma = 0.4;

        double lambda = 0.1;

        for (MultimediaClass multimediaClass : videoRatingByClasses.keySet()) {

            String cacheKey = "video:relevance:" + video.getVideoId() + ":" + multimediaClass.getMultimediaClassId();

            // Попытка достать из кеша
            RelevanceDTO cachedRelevance = new RelevanceDTO();
            cachedRelevance.setRelevanceUser(redisService.getRelevanceVideo(video.getVideoId(),
                    multimediaClass.getMultimediaClassId(), userRoleId));
            cachedRelevance.setRelevanceUser(redisService.getRelevanceVideo(video.getVideoId(),
                    multimediaClass.getMultimediaClassId(), expertRoleId));

            if (cachedRelevance.getRelevanceUser() != null && cachedRelevance.getRelevanceExpert() != null) {
                resultVideoRelevance.put(multimediaClass, cachedRelevance);
                continue;
            }

            RatingDto ratingDto = videoRatingByClasses.get(multimediaClass);
            double ratingUser =  ratingDto.getRatingUser() == null ? 0 : ratingDto.getRatingUser();
            double ratingExpert = ratingDto.getRatingExpert() == null ? 0 :ratingDto.getRatingExpert();

            long views = videoViewsRepository.countByVideo(video);
            var uploadDate = video.getCreatedAt();

            double V = Math.log10(views + 1);
            double ageDays = Duration.between(uploadDate, LocalDateTime.now()).toDays();
            double F = Math.exp(-lambda * ageDays);

            double scoreUser = alpha * ratingUser + beta * ratingExpert + gamma * F;
            double scoreExpert = alpha * ratingExpert + beta * V + gamma * F;

            resultVideoRelevance.put(multimediaClass, new RelevanceDTO(scoreUser, scoreExpert));

            redisService.saveRelevanceVideo(video.getVideoId(), multimediaClass.getMultimediaClassId(), scoreUser, userRoleId);
            redisService.saveRelevanceVideo(video.getVideoId(), multimediaClass.getMultimediaClassId(), scoreExpert, expertRoleId);
        }
        return resultVideoRelevance;
    }
}
