package dex.autoswitch.engine.data;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleSupplier;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * Represents a match result with an associated set of ratings distributed across multiple levels,
 * that can be combined to form a final rating or merged with other matches.
 */
public record Match(boolean matches, Map<Integer, Set<DoubleSupplier>> ratings) implements Comparable<Match> {
    public Match(boolean matches) {
        this(matches, new HashMap<>());
    }

    public Match {
        if (ratings == null) {
            ratings = new HashMap<>();
        }
    }

    /**
     * Adds a rating to the specified level. If the given rating supplier is not memoized,
     * it will be wrapped in a {@link MemoizedDoubleSupplier} to ensure its value is computed only once.
     * If ratings exist for the specified level, the supplied rating is added to the existing set.
     * Otherwise, a new set is created for the level, and the rating is added to it.
     *
     * @param level the level at which the rating should be added.
     * @param rating a {@link DoubleSupplier} providing the rating value. If not already memoized,
     *               it will be wrapped in a {@link MemoizedDoubleSupplier}.
     */
    public void addRating(int level, DoubleSupplier rating) {
        if (!(rating instanceof MemoizedDoubleSupplier)) {
            rating = new MemoizedDoubleSupplier(rating);
        }

        var l = ratings.get(level);
        if (l != null) {
            l.add(rating);
        } else {
            var s = new HashSet<DoubleSupplier>();
            s.add(rating);
            ratings.put(level, s);
        }
    }

    /**
     * Adds a set of rating suppliers to the specified level. If ratings already exist for
     * the given level, the supplied set of ratings is merged with the existing set. Otherwise,
     * the supplied set is added as a new entry for the specified level.
     *
     * @param level  the level at which the ratings should be added
     * @param rating a set of {@link DoubleSupplier} instances providing rating values
     */
    public void addRating(int level, Set<DoubleSupplier> rating) {
        ratings.merge(level, rating,
                (a, b) -> {
                    a.addAll(b);
                    return a;
                });
    }

    /**
     * Calculates and returns the cumulative rating for the specified level.
     * If no ratings are available for the given level, the method returns {@code 0}.
     *
     * @param level the level for which the cumulative rating should be calculated
     * @return the sum of the ratings at the specified level, or {@code 0} if no ratings exist
     */
    public double getRating(int level) {
        var rating = ratings.get(level);
        if (rating != null && !rating.isEmpty()) {
            return rating.stream().mapToDouble(DoubleSupplier::getAsDouble).sum();// / rating.size();
        }

        return 0;
    }

    /**
     * Retrieves the maximum level present in the ratings map.
     * The levels are derived from the keys of the map. If the map is empty,
     * the method returns {@code 0}.
     *
     * @return the highest key (level) in the ratings map, or {@code 0} if the map is empty
     */
    public int getMaxLevel() {
        return ratings.keySet().stream().max(Comparator.naturalOrder()).orElse(0);
    }

    /**
     * Merges the ratings from the given match object into the current match. If the provided match
     * is valid (its `matches` field is true), the ratings from the given match are added to the
     * ratings of the current match. For each level in the provided ratings map, the associated set
     * of rating suppliers is added to the corresponding level in the current match's ratings. If a
     * level does not already exist in the current match, it is created and initialized with the
     * ratings from the given match.
     *
     * @param match the match object whose ratings are to be merged into the current match. If
     *              the `matches` field of the provided match is false, no merge operation occurs.
     */
    public void merge(Match match) {
        if (match.matches) {
            for (Map.Entry<Integer, Set<DoubleSupplier>> entry : match.ratings.entrySet()) {
                addRating(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public int compareTo(@NonNull Match match) {
        if (matches != match.matches) return matches ? -1 : 1;

        var maxTargetRatingLevel = Math.max(getMaxLevel(), match.getMaxLevel());
        for (int i = 0; i <= maxTargetRatingLevel; i++) {
            var r1 = getRating(i);
            var r2 = match.getRating(i);
            var diff = Double.compare(r1, r2);
            if (diff != 0) return diff;
        }

        return 0;
    }

    @Override
    public @NotNull String toString() {
        var r = new double[getMaxLevel() + 1];
        for (int i = 0; i <= getMaxLevel(); i++) {
            r[i] = getRating(i);
        }
        return "Match{" +
                "matches=" + matches +
                ", ratings=" + Arrays.toString(r) +
                '}';
    }

    private static class MemoizedDoubleSupplier implements DoubleSupplier {
        private final DoubleSupplier supplier;
        private volatile boolean done;
        private double value;

        public MemoizedDoubleSupplier(DoubleSupplier supplier) {
            this.supplier = supplier;
        }

        @Override
        public double getAsDouble() {
            if (!done) {
                synchronized (this) {
                    if (!done) {
                        value = supplier.getAsDouble();
                        done = true;
                    }
                }
            }
            return value;
        }
    }
}
