package ru.rsreu.videohosting.repository.composite;

import lombok.*;

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
