package ru.rsreu.videohosting.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription implements Serializable {
    @Id
    @ManyToOne
    @JoinColumn(name = "subscriber_id")
    private User subscriber;

    @Id
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column(updatable = false)
    private LocalDateTime subscribedAt = LocalDateTime.now();
}

