package ru.rsreu.videohosting.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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
            inverseJoinColumns = @JoinColumn(name = "class_id")
    )
    private List<Class> classes;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getImagePath() {
        return '/' + imagePath;
    }

    public String getVideoPath() {
        return '/' + videoPath;
    }
}

