package fr.kirosnn.winzoriajobs.utils;


import java.util.HashMap;
import java.util.Map;

public class LevelCalculator {

    private static final Map<Integer, Integer> levelXPRequirements = new HashMap<>();

    static {
        levelXPRequirements.put(1, 1000);
        levelXPRequirements.put(2, 1800);
        levelXPRequirements.put(3, 2200);
        levelXPRequirements.put(4, 2600);
        levelXPRequirements.put(5, 3000);
        levelXPRequirements.put(6, 3400);
        levelXPRequirements.put(7, 3800);
        levelXPRequirements.put(8, 4200);
        levelXPRequirements.put(9, 4600);
        levelXPRequirements.put(10, 5000);
        levelXPRequirements.put(11, 13800);
        levelXPRequirements.put(12, 14600);
        levelXPRequirements.put(13, 15400);
        levelXPRequirements.put(14, 16200);
        levelXPRequirements.put(15, 17000);
        levelXPRequirements.put(16, 17800);
        levelXPRequirements.put(17, 18600);
        levelXPRequirements.put(18, 19400);
        levelXPRequirements.put(19, 20200);
        levelXPRequirements.put(20, 21000);
        levelXPRequirements.put(21, 71500);
        levelXPRequirements.put(22, 74667);
        levelXPRequirements.put(23, 77833);
        levelXPRequirements.put(24, 81000);
        levelXPRequirements.put(25, 84167);
        levelXPRequirements.put(26, 87333);
        levelXPRequirements.put(27, 90500);
        levelXPRequirements.put(28, 93667);
        levelXPRequirements.put(29, 96834);
        levelXPRequirements.put(30, 100000);
    }

    public static int calculateLevel(int totalXP) {
        int level = 0;

        for (Map.Entry<Integer, Integer> entry : levelXPRequirements.entrySet()) {
            if (totalXP >= entry.getValue()) {
                level = entry.getKey();
            } else {
                break;
            }
        }

        return level;
    }

    public static int getTier(int level) {
        if (level >= 0 && level <= 9) {
            return 1;
        } else if (level >= 10 && level <= 19) {
            return 2;
        } else if (level >= 20 && level <= 29) {
            return 3;
        } else if (level == 30) {
            return 4;
        } else {
            throw new IllegalArgumentException("Le niveau " + level + " est invalide pour déterminer un tiers.");
        }
    }

    public static int getXPForNextLevel(int level) {
        return levelXPRequirements.getOrDefault(level + 1, -1);
    }
}
