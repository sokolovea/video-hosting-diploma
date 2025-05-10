package ru.rsreu.videohosting.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.rsreu.videohosting.repository.composite.VideoClassId;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(VideoClassId.class)
public class VideoClass implements Serializable {
    @Id
    @ManyToOne
    @JoinColumn(name = "video_id")
    private Video video;

    @Id
    @ManyToOne
    @JoinColumn(name = "multimedia_class_id")
    private MultimediaClass videoMultimediaClass;
}

