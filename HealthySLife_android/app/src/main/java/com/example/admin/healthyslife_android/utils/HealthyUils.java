package com.example.admin.healthyslife_android.utils;

/**
 * @author wu jingji
 */
public class HealthyUils {
    /**
     * Calculate the calories burned in exercise
     * 跑步热量（kcal）＝ 体重（kg）× 运动时间（小时）× 指数K
     * 指数K＝30 ÷ 速度（分钟400米）
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
