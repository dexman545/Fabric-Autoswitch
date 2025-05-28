package dex.autoswitch.config.data.tree;

import dex.autoswitch.engine.Matcher;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;

/**
 * Represents a Tree structure for selectors
 */
public sealed interface ExpressionTree extends Data, Matcher permits IdSelector, DisjunctiveUnion, Intersection, Invert, Union {
    /**
     * @param baseLevel  the level to use when generating ratings levels
     * @param context    the context of this match, e.g. the target block for tool matching
     * @param selectable the object to generate the ratings for
     */
    @Override
    Match matches(int baseLevel, SelectionContext context, Object selectable);
}
