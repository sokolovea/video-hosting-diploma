package ru.rsreu.videohosting.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class VideoValidator {

    public static boolean isValidVideo(MultipartFile multipartFile) {
        try {
            // Считываем содержимое MultipartFile в массив байт
            byte[] fileBytes = multipartFile.getBytes();

            // Создаём временный файл в памяти для ffprobe
            File tempFile = File.createTempFile("upload", ".tmp");
            Files.write(tempFile.toPath(), fileBytes);

            // Проверка кодека видео
            ProcessBuilder codecCheck = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries", "stream=codec_name",
                    "-of", "csv=p=0",
                    tempFile.getAbsolutePath()
            );
            Process codecProcess = codecCheck.start();
            String codecOutput = new String(codecProcess.getInputStream().readAllBytes());
            codecProcess.waitFor();

            // Проверка контейнера
            ProcessBuilder containerCheck = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-show_entries", "format=format_name",
                    "-of", "csv=p=0",
                    tempFile.getAbsolutePath()
            );
            Process containerProcess = containerCheck.start();
            String containerOutput = new String(containerProcess.getInputStream().readAllBytes());
            containerProcess.waitFor();

            // Удаляем временный файл
            tempFile.delete();

            // Проверяем кодек и контейнер
            return (codecOutput.contains("vp9") || codecOutput.contains("h264")) &&
                    (containerOutput.contains("mp4") || containerOutput.contains("webm"));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

}


