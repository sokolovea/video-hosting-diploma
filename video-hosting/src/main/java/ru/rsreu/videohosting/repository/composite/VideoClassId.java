package ru.rsreu.videohosting.repository.composite;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class VideoClassId implements Serializable {
    private Long video;
    private Long videoClass;
}
