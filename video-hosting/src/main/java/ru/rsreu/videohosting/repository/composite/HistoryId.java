package ru.rsreu.videohosting.repository.composite;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class HistoryId implements Serializable {
    private Long video;
    private Long user;
}
