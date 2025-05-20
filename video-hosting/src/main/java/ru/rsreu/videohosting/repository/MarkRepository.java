package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.videohosting.entity.MarkType;

import java.util.Optional;


public interface MarkRepository extends JpaRepository<MarkType, Long> {
    Optional<MarkType> findByName(String name);
}
