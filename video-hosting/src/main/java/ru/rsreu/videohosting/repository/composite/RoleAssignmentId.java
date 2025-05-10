package ru.rsreu.videohosting.repository.composite;

import lombok.*;

import javax.persistence.Column;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoleAssignmentId implements Serializable {
    private Long receiver;
    private Long multimediaClass;
}
