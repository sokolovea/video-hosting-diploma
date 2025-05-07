package ru.rsreu.videohosting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileEditDto {
    private String login;
    @NotBlank
    private String password;
    @NotBlank
    private String surname;
    @NotBlank
    private String name;
    private String patronymic;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String telephone;
    private String imagePath;
}
