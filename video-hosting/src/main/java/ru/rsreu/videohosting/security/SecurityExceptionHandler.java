package ru.rsreu.videohosting.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public String handleAuthException() {
        return "redirect:/login";
    }
}