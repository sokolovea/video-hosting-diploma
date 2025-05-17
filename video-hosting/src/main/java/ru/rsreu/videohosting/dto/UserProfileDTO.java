package ru.rsreu.videohosting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String login;
    private String surname;
    private String name;
    private String patronymic;
    private String email;
    private String telephone;
    private String imagePath;
    private LocalDate createdAt;
    private boolean isBlocked;
}
