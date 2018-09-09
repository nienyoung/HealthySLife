package com.example.admin.healthyslife_android.utils;

/**
 * @author wu jingji
 */
public class HealthyUils {
    /**
     * Calculate the calories burned in exercise
     *
     * @param weight   weight in kilogram(kg)
     * @param time     exercise time in seconds
     * @param velocity velocity in m/s
     * @return burned calories
     */
    public static float calorieCalculator(float weight, float time, float velocity) {
        float k = 9 / (2 / velocity);
        float hour = time / 3600;
        float kcal = weight * hour * k;
        return kcal * 1000;
    }
}
