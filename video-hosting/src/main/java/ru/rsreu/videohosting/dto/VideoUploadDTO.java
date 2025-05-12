package ru.rsreu.videohosting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoUploadDTO {
    private Long videoId;
    @NotNull(message = "Название видео не может быть пустым!")
    private String title;
    @NotNull(message = "Описание видео не может быть пустым!")
    private String description;
    private MultipartFile videoFile;
    private MultipartFile imageFile;
    private List<String> classesString;
}
