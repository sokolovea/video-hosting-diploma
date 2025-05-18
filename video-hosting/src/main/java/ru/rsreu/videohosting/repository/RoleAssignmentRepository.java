package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.videohosting.entity.MultimediaClass;
import ru.rsreu.videohosting.entity.Role;
import ru.rsreu.videohosting.entity.RoleAssignment;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.repository.composite.RoleAssignmentId;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, RoleAssignmentId> {

    RoleAssignment findByReceiverAndMultimediaClass(User receiver, MultimediaClass multimediaClass);
}
