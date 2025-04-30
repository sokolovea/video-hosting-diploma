package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.rsreu.videohosting.entity.Class;

import java.util.List;

public interface ClassRepository extends JpaRepository<Class, Long> {

    @Query("SELECT c.className FROM Class c")
    List<String> getAllClassNames();

    List<Class> findByClassName(String className);

    List<Class> findAllByClassNameIn(List<String> classesString);
}
