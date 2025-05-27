package ru.rsreu.videohosting.util;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ImageValidator {

    private static final String[] ALLOWED_FORMATS = {"JPEG", "JPG", "PNG", "SVG"};

    public static boolean isValidImage(MultipartFile imageFile) {
        try {
            // Считываем содержимое MultipartFile в массив байт
            byte[] fileBytes = imageFile.getBytes();

            // Попытка загрузить изображение
            try (var inputStream = new ByteArrayInputStream(fileBytes)) {
                BufferedImage image = ImageIO.read(inputStream);

                if (image == null) {
                    // Файл не является изображением
                    return false;
                }
            }

            // Определение формата изображения
            String formatName = getImageFormat(fileBytes);
            boolean isFormatAllowed = false;

            for (String allowedFormat : ALLOWED_FORMATS) {
                if (allowedFormat.equalsIgnoreCase(formatName)) {
                    isFormatAllowed = true;
                    break;
                }
            }

            return isFormatAllowed;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getImageFormat(byte[] fileBytes) throws IOException {
        String formatName = null;

        try (var inputStream = new ByteArrayInputStream(fileBytes)) {
            var imageInputStream = ImageIO.createImageInputStream(inputStream);
            var readers = ImageIO.getImageReaders(imageInputStream);

            if (readers.hasNext()) {
                formatName = readers.next().getFormatName();
            }
        }

        return formatName;
    }
}
