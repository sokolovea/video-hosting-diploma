package ru.rsreu.videohosting.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.rsreu.videohosting.repository.composite.RoleAssignmentId;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RoleAssignmentId.class)
public class RoleAssignment implements Serializable {
    @Id
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Id
    @ManyToOne
    @JoinColumn(name = "multimedia_class_id")
    private MultimediaClass multimediaClass;

    @Column(updatable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "is_fixed")
    private boolean isFixed = false;
}

