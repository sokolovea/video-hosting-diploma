package ru.rsreu.videohosting.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "video")
public class Video implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoId;

    private String title;
    private String description;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;
    private String videoPath;

    private String imagePath;

    @ManyToMany
    @JoinTable(
            name = "video_class",
            joinColumns = @JoinColumn(name = "video_id"),
            inverseJoinColumns = @JoinColumn(name = "multimedia_class_id")
    )
    private Set<MultimediaClass> multimediaClasses;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_blocked")
    private Boolean isBlocked = false;

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    public boolean isStoredOnS3() {
        return videoPath.startsWith("http");
    }

}

