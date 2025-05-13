package ru.rsreu.videohosting.repository.custom;


import ru.rsreu.videohosting.entity.Video;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VideoRepositoryImpl implements VideoRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Video> findWithFilters(String query, String category, LocalDate startDate, LocalDate endDate) {
        StringBuilder jpql = new StringBuilder("SELECT v FROM Video v WHERE v.title LIKE :query");

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
            typedQuery.setParameter("category", category);
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

