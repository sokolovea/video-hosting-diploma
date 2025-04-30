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
public class RoleAssignment implements Serializable {
    @Id
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Id
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(updatable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();
}

