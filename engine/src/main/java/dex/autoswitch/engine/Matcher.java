package dex.autoswitch.engine;

import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;

@FunctionalInterface
public interface Matcher {
    /**
     * @param baseLevel  the level to use when generating ratings levels
     * @param context    the context of this match, e.g. the target block for tool matching
     * @param selectable the object to generate the ratings for
     */
    Match matches(int baseLevel, SelectionContext context, Object selectable);
}
