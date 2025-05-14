package ru.rsreu.videohosting.util;


import org.springframework.stereotype.Component;

@Component
public class RatingFormatter {
    public static String formatRating(double rating) {
        return String.format("%.1f%%", rating * 100);
    }
}

