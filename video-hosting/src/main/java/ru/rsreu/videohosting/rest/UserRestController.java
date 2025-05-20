package ru.rsreu.videohosting.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.videohosting.dto.SubscriptionDTO;
import ru.rsreu.videohosting.entity.Subscription;
import ru.rsreu.videohosting.entity.User;
import ru.rsreu.videohosting.enumeration.ActionSubscribeEnum;
import ru.rsreu.videohosting.repository.*;
import ru.rsreu.videohosting.repository.composite.SubscriptionId;

import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/user", produces = "application/json")
@CrossOrigin(origins = {"http://localhost:8082"})
//@CrossOrigin(origins = "*")
public class UserRestController {
    private static final Logger log = LoggerFactory.getLogger(VideoHostingRestController.class);


    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public UserRestController(@Autowired UserRepository userRepository,
                              @Autowired SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(
            @RequestBody String subscriptionDTOString,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body("You are not logged in");
        }
        SubscriptionDTO subscriptionDTO = null;
        try {
            subscriptionDTO = new ObjectMapper().readValue(subscriptionDTOString, SubscriptionDTO.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(400).body("bad json!");
        }

        Optional<User> subscriber = userRepository.findByLogin(principal.getName());
        Optional<User> author = userRepository.findById(subscriptionDTO.getAuthor());

        if (!subscriber.isPresent() && !author.isPresent()) {
            return ResponseEntity.status(400).body("Can't find such user!");
        }

        if (!Objects.equals(subscriber.get().getUserId(), author.get().getUserId())) {
            if (subscriptionDTO.getActionSubscribeEnum().equals(ActionSubscribeEnum.SUBSCRIBE)) {
                try {
                    subscriptionRepository.save(new Subscription(
                            subscriber.get(),
                            author.get()
                    ));
                } catch (NoSuchElementException e) {
                    return ResponseEntity.status(400).body("User not found");
                }
            } else if (subscriptionDTO.getActionSubscribeEnum().equals(ActionSubscribeEnum.UNSUBSCRIBE)) {
                SubscriptionId subscriptionId = new SubscriptionId(subscriber.get().getUserId(), author.get().getUserId());
                if (subscriptionRepository.existsById(subscriptionId)) {
                    subscriptionRepository.deleteById(subscriptionId);
                }
            }
            return ResponseEntity.ok("{}");
        }
        return ResponseEntity.status(400).body("Subscriber = author: failed");
    }
}
