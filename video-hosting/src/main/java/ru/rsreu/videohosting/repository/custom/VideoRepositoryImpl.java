package ru.rsreu.videohosting.repository.custom;


import org.springframework.beans.factory.annotation.Autowired;
import ru.rsreu.videohosting.entity.MultimediaClass;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.repository.MultimediaClassRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VideoRepositoryImpl implements VideoRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MultimediaClassRepository multimediaClassRepository;

    @Override
    public List<Video> findWithFilters(String query, String category, LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder jpql = new StringBuilder("SELECT v FROM Video v WHERE LOWER(v.title) LIKE LOWER(:query)");

        if (category != null && !category.isEmpty()) {
            jpql.append(" AND :category MEMBER OF v.multimediaClasses");
        }
        if (startDate != null) {
            jpql.append(" AND v.createdAt >= :startDate");
        }
        if (endDate != null) {
            jpql.append(" AND v.createdAt <= :endDate");
        }

        TypedQuery<Video> typedQuery = entityManager.createQuery(jpql.toString(), Video.class);
        typedQuery.setParameter("query", "%" + query + "%");

        if (category != null && !category.isEmpty()) {
            MultimediaClass categoryObj = multimediaClassRepository.findByMultimediaClassName(category)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + category));
            typedQuery.setParameter("category", categoryObj);
        }
        if (startDate != null) {
            typedQuery.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            typedQuery.setParameter("endDate", endDate);
        }

        return typedQuery.getResultList();
    }
}

