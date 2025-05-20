package ru.rsreu.videohosting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.videohosting.dto.IStringBooleanDto;
import ru.rsreu.videohosting.entity.MultimediaClass;
import ru.rsreu.videohosting.entity.RoleAssignment;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.repository.composite.RoleAssignmentId;

import java.util.List;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, RoleAssignmentId> {

    RoleAssignment findByReceiverAndMultimediaClass(User receiver, MultimediaClass multimediaClass);

    @Query("SELECT m.multimediaClassName as string, true as boolean " +
            "FROM RoleAssignment ra " +
            "JOIN ra.multimediaClass m " +
            "WHERE ra.receiver = :receiver AND ra.role.roleName = 'EXPERT'")
    List<IStringBooleanDto> findExpertCategoriesByReceiver(@Param("receiver") User receiver);
}
