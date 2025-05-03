package ru.rsreu.videohosting.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.videohosting.dto.RegistrationDTO;
import ru.rsreu.videohosting.entity.Comment;
import ru.rsreu.videohosting.entity.Role;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.entity.Video;
import ru.rsreu.videohosting.repository.CommentRepository;
import ru.rsreu.videohosting.repository.RoleRepository;
import ru.rsreu.videohosting.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(@Autowired CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Map<Comment, List<Comment>> getCommentsWithReplies(Video video) {
        List<Comment> topLevelComments = commentRepository.findTopLevelCommentsByVideo(video);
        Map<Comment, List<Comment>> commentsWithReplies = new LinkedHashMap<>();

        for (Comment comment : topLevelComments) {
            List<Comment> replies = commentRepository.findRepliesByParent(comment);
            commentsWithReplies.put(comment, replies);
        }

        return commentsWithReplies;
    }

}
