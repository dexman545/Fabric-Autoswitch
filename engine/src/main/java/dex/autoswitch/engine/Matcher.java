package dex.autoswitch.engine;

import java.util.Set;
import java.util.stream.Collectors;

import dex.autoswitch.config.data.tree.Data;
import dex.autoswitch.config.data.tree.DataMap;
import dex.autoswitch.config.data.tree.DisjunctiveUnion;
import dex.autoswitch.config.data.tree.ExpressionTree;
import dex.autoswitch.config.data.tree.IdSelector;
import dex.autoswitch.config.data.tree.Intersection;
import dex.autoswitch.config.data.tree.Invert;
import dex.autoswitch.config.data.tree.TypedData;
import dex.autoswitch.config.data.tree.Union;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.futures.FutureSelectableGroup;
import dex.autoswitch.futures.FutureSelectableValue;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a functional interface that matches objects against defined criteria
 * and returns a {@link Match} result. The criteria for matching depend on the
 * implementation provided for the interface.
 * <p>
 * The {@link #matches} method accepts a base level, the context in which the
 * matching operation occurs, and the object to match. Implementations of this
 * interface should define the logic for determining the match result and
 * calculating associated ratings.
 */
@FunctionalInterface
public interface Matcher {
    /**
     * @param baseLevel  the level to use when generating ratings levels
     * @param context    the context of this match, e.g. the target block for tool matching
     * @param selectable the object to generate the ratings for
     */
    Match matches(int baseLevel, SelectionContext context, Object selectable);

    static String prettyPrint(Matcher matcher) {
        return prettyPrint(matcher, 0);
    }

    static String prettyPrint(Matcher matcher, int level) {
        return switch (matcher) {
            case DisjunctiveUnion(var children) -> prettyPrintChildren("XOR", children, level);
            case Intersection(var children) -> prettyPrintChildren("OR", children, level);
            case Union(var children) -> prettyPrintChildren("AND", children, level);
            case Invert(Matcher child) -> " ".repeat(level) + "NOT[" + prettyPrint(child, level + 1) + "]";
            case IdSelector(var selectable, var data) -> {
                var sb = new StringBuilder(selectable.getSelectorType().id());

                sb.append('[');

                switch (selectable) {
                    case FutureSelectableGroup<?, ?, ?> v -> sb.append('#').append(v.getKey());
                    case FutureSelectableValue<?, ?> v -> sb.append(v.getKey());
                }

                sb.append(']');

                var indent = " ".repeat(level);
                for (TypedData<?> subDatum : data) {
                    sb.append('\n').append(indent);
                    sb.append(prettyPrint(subDatum, level + 1));
                }

                yield indent + sb;
            }
            case TypedData<?> typedData -> " ".repeat(level) +
                    typedData.type().id() + '[' + prettyPrint(typedData.data(), level + 1) + ']';
            default -> " ".repeat(level) + "<Fallback>";
        };
    }

    private static @NotNull String prettyPrintChildren(String type, Set<ExpressionTree> children, int level) {
        var indent = " ".repeat(Math.max(0, level - 1));
        return type + "{\n" +
                children.stream()
                        .map(child -> prettyPrint((Matcher) child, level + 1))
                        .collect(Collectors.joining("\n")) +
                "\n" + indent + "}";
    }

    private static String prettyPrint(Data data, int level) {
        return switch (data) {
            case DataMap.Pair pair -> pair.key() + ": " + prettyPrint(pair.value(), level);
            case DataMap.Value value -> value.value();
            case DataMap.Map map -> map.entries().stream()
                    .map(entry -> prettyPrint(entry, level))
                    .collect(Collectors.joining("\n" + " ".repeat(level)));
            case ExpressionTree tree -> prettyPrint((Matcher) tree, level);
            //noinspection UnnecessaryDefault
            default -> data.toString();
        };
    }
}
