package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.videohosting.entity.Class;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByAuthor(User author);
    List<Video> findByTitleContaining(String title);


    @Query("SELECT count(v) FROM Video v WHERE v.author = :author AND :classificator MEMBER OF v.classes")
    Long countAllByAuthorAndHavingClass(@Param("author") User author, @Param("classificator") Class classificator);

}
