package ru.rsreu.videohosting.repository.composite;


import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class UserVideoMarkId implements Serializable {
    private Long user;
    private Long video;
}
