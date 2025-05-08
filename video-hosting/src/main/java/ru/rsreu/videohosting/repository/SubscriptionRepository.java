package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import ru.rsreu.videohosting.entity.Subscription;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.repository.composite.SubscriptionId;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {
    List<Subscription> findBySubscriberOrderBySubscribedAtDesc(User subscriber);
    Long countByAuthor(User author);
}
