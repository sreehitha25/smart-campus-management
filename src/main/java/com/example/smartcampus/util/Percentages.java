package com.example.smartcampus.util;

public final class Percentages {

    private Percentages() {
    }

    public static double of(long part, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(part * 10000.0 / total) / 100.0;
    }
}
