package dex.autoswitch.config.data.tree;

import java.util.Set;

public record ValueCondition<T extends Comparable<T>>(Set<Condition<T>> conditions) implements Data {
    public boolean matches(T value) {
        for (Condition<T> condition : conditions) {
            if (!condition.matches(value)) {
                return false;
            }
        }

        return true;
    }

    public String prettyPrint(int level) {
        var sb = new StringBuilder();
        var i = 0;
        for (Condition<T> condition : conditions) {
            if (i > 0) {
                sb.append("\n").append(" ".repeat(level));
            }
            var comparatorSymbol = switch (condition.comparator()) {
                case EQUALS -> "==";
                case NOT_EQUALS -> "!=";
                case LESSER -> "<";
                case GREATER -> ">";
                case LESSER_EQUAL -> "<=";
                case GREATER_EQUAL -> ">=";
                case INVALID -> "INVALID";
            };
            sb.append(comparatorSymbol).append(" ").append(condition.value());
            i++;
        }
        return sb.toString();
    }

    public record Condition<T extends Comparable<T>>(Comparator comparator, T value) {
        public boolean matches(T value) {
            return switch (comparator) {
                case INVALID -> false;
                case EQUALS -> value.compareTo(this.value) == 0;
                case NOT_EQUALS -> value.compareTo(this.value) != 0;
                case LESSER -> value.compareTo(this.value) < 0;
                case GREATER -> value.compareTo(this.value) > 0;
                case LESSER_EQUAL -> value.compareTo(this.value) <= 0;
                case GREATER_EQUAL -> value.compareTo(this.value) >= 0;
            };
        }
    }

    public enum Comparator {
        INVALID,
        EQUALS,
        NOT_EQUALS,
        LESSER,
        GREATER,
        LESSER_EQUAL,
        GREATER_EQUAL,
    }
}
