package ru.rsreu.videohosting.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.rsreu.videohosting.dto.CountSumDto;
import ru.rsreu.videohosting.dto.RatingDto;
import ru.rsreu.videohosting.dto.VideoRatingDto;
import ru.rsreu.videohosting.entity.MultimediaClass;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.repository.*;

import javax.sql.DataSource;
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

    public JdbcRatingDao(@Autowired DataSource dataSource,
                         @Autowired MarkRepository markRepository,
                         @Autowired UserVideoMarkRepository videoMarkRepository,
                         @Autowired UserCommentMarkRepository commentMarkRepository, UserRepository userRepository, UserVideoMarkRepository userVideoMarkRepository, VideoRepository videoRepository, CommentRepository commentRepository, RoleRepository roleRepository) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.markRepository = markRepository;
        this.videoMarkRepository = videoMarkRepository;
        this.commentMarkRepository = commentMarkRepository;
        this.userRepository = userRepository;
        this.userVideoMarkRepository = userVideoMarkRepository;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
        this.roleRepository = roleRepository;
    }

    public Map<MultimediaClass, Double> getUserRating(User user, List<MultimediaClass> classifications) {
        HashMap<MultimediaClass, Double> userRating = new HashMap<>();
        for (MultimediaClass classification : classifications) {
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
        }
        return userRating;
    }

    public Map<MultimediaClass, RatingDto> getVideoRating(Video video) {

        Long likeId = markRepository.findByName("LIKE").get().getMarkId();

        Long expertRoleId = roleRepository.findByRoleName("EXPERT").get().getRoleId();

        Set<MultimediaClass> classifications = video.getMultimediaClasses();
        HashMap<Long, CountSumDto> videoSumMarksByClasses = new HashMap<>();

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

        HashMap<MultimediaClass, RatingDto> videoRatingByClasses = new HashMap<>();
        for (MultimediaClass classification : classifications) {
            videoRatingByClasses.put(classification,
                    videoSumMarksByClasses.get(classification.getMultimediaClassId()).getRatingDto());
        }
        return videoRatingByClasses;
    }
}
