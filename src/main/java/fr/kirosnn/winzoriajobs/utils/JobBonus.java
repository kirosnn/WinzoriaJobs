package fr.kirosnn.winzoriajobs.utils;

public class JobBonus {

    public static double getBonusMultiplier(int level) {
        if (level <= 0) {
            return 0;
        }

        if (level >= 1 && level <= 30) {
            double[] bonusTable = {
                    0.0, 0.0025, 0.0035, 0.0045, 0.01, 0.0115, 0.0125, 0.0135, 0.0145, 0.02,
                    0.022, 0.0235, 0.025, 0.0265, 0.035, 0.037, 0.039, 0.041, 0.043, 0.05,
                    0.055, 0.06, 0.065, 0.07, 0.1, 0.11, 0.12, 0.13, 0.14, 0.15
            };

            return bonusTable[level - 1];
        }

        return 0.15;
    }

    public static double applyBonus(double baseValue, int level) {
        double multiplier = getBonusMultiplier(level);
        return baseValue * (1 + multiplier);
    }
}
