package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.rsreu.videohosting.entity.MultimediaClass;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MultimediaClassRepository extends JpaRepository<MultimediaClass, Long> {

    @Query("SELECT c.multimediaClassName FROM MultimediaClass c")
    List<String> getAllMultimediaClassNames();

    @Query("SELECT c FROM MultimediaClass c")
    List<MultimediaClass> getAllMultimediaClasses();

    Optional<MultimediaClass> findByMultimediaClassName(String className);

    Set<MultimediaClass> findAllByMultimediaClassNameIn(List<String> classesString);
}
