package com.dndmaster.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiceRoller {

    private static final Random random = new Random();

    public static int roll(int sides) {
        return random.nextInt(sides) + 1;
    }

    public static int rollMultiple(int count, int sides) {
        int total = 0;
        for (int i = 0; i < count; i++) {
            total += roll(sides);
        }
        return total;
    }

    public static RollResult rollWithDetails(int count, int sides) {
        List<Integer> rolls = new ArrayList<>();
        int total = 0;
        for (int i = 0; i < count; i++) {
            int r = roll(sides);
            rolls.add(r);
            total += r;
        }
        return new RollResult(count, sides, rolls, total, 0, false);
    }

    public static RollResult rollWithModifier(int count, int sides, int modifier) {
        RollResult base = rollWithDetails(count, sides);
        return new RollResult(count, sides, base.getRolls(), base.getTotal() + modifier, modifier, false);
    }

    // Advantage: roll twice, take highest
    public static RollResult rollAdvantage(int sides, int modifier) {
        int r1 = roll(sides);
        int r2 = roll(sides);
        List<Integer> rolls = new ArrayList<>();
        rolls.add(r1);
        rolls.add(r2);
        int best = Math.max(r1, r2);
        return new RollResult(1, sides, rolls, best + modifier, modifier, true);
    }

    // Disadvantage: roll twice, take lowest
    public static RollResult rollDisadvantage(int sides, int modifier) {
        int r1 = roll(sides);
        int r2 = roll(sides);
        List<Integer> rolls = new ArrayList<>();
        rolls.add(r1);
        rolls.add(r2);
        int worst = Math.min(r1, r2);
        return new RollResult(1, sides, rolls, worst + modifier, modifier, false);
    }

    // D20 checks
    public static int d4() { return roll(4); }
    public static int d6() { return roll(6); }
    public static int d8() { return roll(8); }
    public static int d10() { return roll(10); }
    public static int d12() { return roll(12); }
    public static int d20() { return roll(20); }
    public static int d100() { return roll(100); }

    // Ability score generation (4d6 drop lowest)
    public static int rollAbilityScore() {
        int[] rolls = new int[4];
        for (int i = 0; i < 4; i++) rolls[i] = d6();
        int min = rolls[0];
        int total = 0;
        for (int r : rolls) {
            total += r;
            if (r < min) min = r;
        }
        return total - min;
    }

    public static int[] rollAllAbilityScores() {
        int[] scores = new int[6];
        for (int i = 0; i < 6; i++) scores[i] = rollAbilityScore();
        return scores;
    }

    // Parse dice notation like "2d6+3"
    public static RollResult parseDiceNotation(String notation) {
        try {
            notation = notation.trim().toLowerCase();
            int modifier = 0;
            int dIndex = notation.indexOf('d');
            if (dIndex < 0) {
                // It's just a number
                return new RollResult(0, 0, new ArrayList<>(), Integer.parseInt(notation), 0, false);
            }
            int count = dIndex > 0 ? Integer.parseInt(notation.substring(0, dIndex)) : 1;
            String rest = notation.substring(dIndex + 1);
            int plus = rest.indexOf('+');
            int minus = rest.indexOf('-');
            int sides;
            if (plus >= 0) {
                sides = Integer.parseInt(rest.substring(0, plus));
                modifier = Integer.parseInt(rest.substring(plus + 1));
            } else if (minus >= 0) {
                sides = Integer.parseInt(rest.substring(0, minus));
                modifier = -Integer.parseInt(rest.substring(minus + 1));
            } else {
                sides = Integer.parseInt(rest);
            }
            return rollWithModifier(count, sides, modifier);
        } catch (NumberFormatException e) {
            return new RollResult(1, 20, new ArrayList<>(), 0, 0, false);
        }
    }

    public static class RollResult {
        private int count;
        private int sides;
        private List<Integer> rolls;
        private int total;
        private int modifier;
        private boolean advantage;

        public RollResult(int count, int sides, List<Integer> rolls, int total, int modifier, boolean advantage) {
            this.count = count;
            this.sides = sides;
            this.rolls = rolls;
            this.total = total;
            this.modifier = modifier;
            this.advantage = advantage;
        }

        public boolean isCriticalHit() { return sides == 20 && rolls.size() == 1 && rolls.get(0) == 20; }
        public boolean isCriticalFail() { return sides == 20 && rolls.size() == 1 && rolls.get(0) == 1; }

        public String getDescription() {
            StringBuilder sb = new StringBuilder();
            sb.append(count).append("d").append(sides);
            if (modifier > 0) sb.append("+").append(modifier);
            else if (modifier < 0) sb.append(modifier);
            sb.append(" [");
            for (int i = 0; i < rolls.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(rolls.get(i));
            }
            sb.append("] = ").append(total);
            if (isCriticalHit()) sb.append(" ⚡ КРИТИЧЕСКИЙ УДАР!");
            if (isCriticalFail()) sb.append(" 💀 КРИТИЧЕСКИЙ ПРОВАЛ!");
            return sb.toString();
        }

        public int getCount() { return count; }
        public int getSides() { return sides; }
        public List<Integer> getRolls() { return rolls; }
        public int getTotal() { return total; }
        public int getModifier() { return modifier; }
        public boolean isAdvantage() { return advantage; }
    }
}
