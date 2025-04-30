package ru.rsreu.videohosting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDTO {
    @NotBlank private String login;
    @NotBlank private String password;
    @NotBlank private String surname;
    @NotBlank private String name;
    private String patronymic;
    @NotBlank @Email private String email;
    @NotBlank private String telephone;
    private MultipartFile imagePath;
}
