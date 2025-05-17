package ru.rsreu.videohosting.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.session.SessionRegistry;
import java.util.List;

@Controller
public class SessionStatisticsController {

    @Autowired
    private SessionRegistry sessionRegistry;


    public long getActiveSessionsCount() {
        return sessionRegistry.getAllPrincipals().stream()
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .filter(session -> !session.isExpired())
                .count();
    }
}
