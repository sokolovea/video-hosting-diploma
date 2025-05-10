package ru.rsreu.videohosting.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.rsreu.videohosting.entity.MultimediaClass;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.repository.*;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                join "multimedia_class" on "multimedia_class".multimedia_class_id = "video_class".class_id
            where "user".user_id = ? and "mark_type"."name" = ? and "multimedia_class".multimedia_class_name = ?""";

    private final String likesDislikesUserByCommentsCount = """
            SELECT count(*)
            FROM "user_comment_mark"
            	join "comment" on "user_comment_mark".comment_id = "comment".comment_id
            	join "user" on 	"comment".user_id = "user".user_id
            	join "mark_type" on "mark_type".mark_id = "user_comment_mark".mark
            	join "video" on "comment".video_id = "video".video_id
            	join "video_class" on "video_class".video_id = "video".video_id
                join "multimedia_class" on "multimedia_class".multimedia_class_id = "video_class".class_id
            where "user".user_id = ? and "mark_type"."name" = ? and "multimedia_class".multimedia_class_name = ?""";
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;

    public JdbcRatingDao(@Autowired DataSource dataSource,
                         @Autowired MarkRepository markRepository,
                         @Autowired UserVideoMarkRepository videoMarkRepository,
                         @Autowired UserCommentMarkRepository commentMarkRepository, UserRepository userRepository, UserVideoMarkRepository userVideoMarkRepository, VideoRepository videoRepository, CommentRepository commentRepository) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.markRepository = markRepository;
        this.videoMarkRepository = videoMarkRepository;
        this.commentMarkRepository = commentMarkRepository;
        this.userRepository = userRepository;
        this.userVideoMarkRepository = userVideoMarkRepository;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
    }

    public Map<MultimediaClass, Double> getUserRating(User user, List<MultimediaClass> classificators) {
        HashMap<MultimediaClass, Double> userRating = new HashMap<>();
        for (MultimediaClass classificator : classificators) {
            Object[] paramsLikes = {user.getUserId(), "LIKE", classificator.getMultimediaClassName()};
            Object[] paramsDislikes = {user.getUserId(), "DISLIKE", classificator.getMultimediaClassName()};
            Long likesOnUserVideos = this.jdbcTemplate.queryForObject(likesDislikesUserByVideoCount,
                    paramsLikes, Long.class);
            Long dislikesOnUserVideos = this.jdbcTemplate.queryForObject(likesDislikesUserByVideoCount,
                    paramsDislikes, Long.class);
            long marksAssignedToUserVideoCount = likesOnUserVideos + dislikesOnUserVideos;
            long videosInClassCount = videoRepository.countAllByAuthorAndHavingClass(user, classificator);
            double ratingByVideo = 0;
            if (marksAssignedToUserVideoCount != 0) {
                ratingByVideo = likesOnUserVideos / (double) marksAssignedToUserVideoCount - 0.5;
            }


            Long likesOnUserComments = this.jdbcTemplate.queryForObject(likesDislikesUserByCommentsCount,
                    paramsLikes, Long.class);
            Long dislikesOnUserComments = this.jdbcTemplate.queryForObject(likesDislikesUserByCommentsCount,
                    paramsDislikes, Long.class);
            long marksAssignedToUserByCommentsCount = likesOnUserComments + dislikesOnUserComments;
            long commentsInClassCount = commentRepository.countAllByAuthorAndHavingClass(user, classificator);
            double ratingByComments = 0;
            if (marksAssignedToUserByCommentsCount != 0) {
                ratingByComments = likesOnUserComments / (double) marksAssignedToUserByCommentsCount - 0.5;
            }
            double finalRating = ratingByVideo * videosInClassCount * 0.95 + ratingByComments * commentsInClassCount * 0.05;
            userRating.put(classificator, finalRating);
        }
        return userRating;
    }
}
