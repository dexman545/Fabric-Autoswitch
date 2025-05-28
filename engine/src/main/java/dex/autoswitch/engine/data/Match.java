package dex.autoswitch.engine.data;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.DoubleSupplier;

public record Match(boolean matches, Map<Integer, Set<DoubleSupplier>> ratings) {
    public Match(boolean matches) {
        this(matches, new HashMap<>());
    }

    public Match {
        if (ratings == null) {
            ratings = new HashMap<>();
        }
    }

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

    public void addRating(int level, Set<DoubleSupplier> rating) {
        ratings.merge(level, rating,
                (a, b) -> {
                    a.addAll(b);
                    return a;
                });
    }

    public double getRating(int level) {
        var rating = ratings.get(level);
        if (rating != null && !rating.isEmpty()) {
            return rating.stream().mapToDouble(DoubleSupplier::getAsDouble).sum();// / rating.size();
        }

        return 0;
    }

    public int getMaxLevel() {
        return ratings.keySet().stream().max(Comparator.naturalOrder()).orElse(0);
    }

    public void merge(Match match) {
        if (match.matches) {
            for (Map.Entry<Integer, Set<DoubleSupplier>> entry : match.ratings.entrySet()) {
                addRating(entry.getKey(), entry.getValue());
            }
        }
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
