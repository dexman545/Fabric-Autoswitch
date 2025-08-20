package dex.autoswitch.engine;

import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;

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
}
