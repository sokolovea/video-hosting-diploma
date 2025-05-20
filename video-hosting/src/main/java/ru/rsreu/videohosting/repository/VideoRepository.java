package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.videohosting.entity.MultimediaClass;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.repository.custom.VideoRepositoryCustom;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long>, VideoRepositoryCustom {
    List<Video> findByAuthor(User author);
    List<Video> findByTitleContaining(String title);

    boolean existsByAuthorAndVideoId(User author, Long videoId);

    @Query("SELECT count(v) FROM Video v WHERE v.author = :author AND :classificator MEMBER OF v.multimediaClasses")
    Long countAllByAuthorAndHavingClass(@Param("author") User author, @Param("classificator") MultimediaClass classificator);

    @Modifying
    @Query("UPDATE Video v SET v.isBlocked = :isBlocked WHERE v.videoId = :videoId")
    void updateBlockedStatus(@Param("videoId") Long videoId, @Param("isBlocked") Boolean isBlocked);

    @Modifying
    @Query("UPDATE Video v SET v.isDeleted = true WHERE v.videoId = :videoId")
    void softDeleteVideo(@Param("videoId") Long videoId);

    @Query("SELECT v FROM Video v where v.isDeleted = false")
    List<Video> findAllNotDeleted();
}
