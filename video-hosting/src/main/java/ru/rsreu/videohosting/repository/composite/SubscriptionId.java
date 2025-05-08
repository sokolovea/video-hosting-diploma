package ru.rsreu.videohosting.repository.composite;

import lombok.*;
import ru.rsreu.videohosting.entity.User;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class SubscriptionId implements Serializable {
    private Long subscriber;
    private Long author;
}
