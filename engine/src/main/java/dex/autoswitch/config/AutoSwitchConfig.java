package dex.autoswitch.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dex.autoswitch.config.data.FallbackSelector;
import dex.autoswitch.config.data.tree.ExpressionTree;
import dex.autoswitch.config.subentries.FeatureConfig;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.SelectionEngine;
import dex.autoswitch.engine.Selector;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SwitchRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;

@ConfigSerializable
public class AutoSwitchConfig {
    public static final Selector ANY_ITEM_SELECTOR = new Selector(0, (i, j, k) -> new Match(true));
    public static final Selector FALLBACK = new Selector(Integer.MIN_VALUE, SwitchRegistry.INSTANCE.nonToolMatcher);

    @Comment("Control all features")
    public FeatureConfig featureConfig = new FeatureConfig();

    @Comment("Control selection when attacking")
    public Set<TargetEntry> attackAction = new HashSet<>();

    @Comment("Control selection when interacting")
    public Set<TargetEntry> interactAction = new HashSet<>();

    @Comment("Control selection when a stat changes")
    public Set<TargetEntry> statChangeAction = new HashSet<>();

    @SuppressWarnings("unused")
    @Comment("Config version to support migration")
    public int configVersion = 1;

    private transient Map<Action, Map<Selector, Set<Selector>>> configuration = null;
    private transient SelectionEngine engine;
    private final transient Set<Object> relevantEvents = new HashSet<>();

    @Unmodifiable
    public Map<Action, @Unmodifiable Map<Selector, @Unmodifiable Set<Selector>>> getConfiguration() {
        if (configuration == null) {
            var configuration = new HashMap<Action, Map<Selector, Set<Selector>>>();
            for (Action action : Action.values()) {
                configuration.put(action, makeMap(action));
            }

            this.configuration = Collections.unmodifiableMap(configuration);
        }

        return configuration;
    }

    public SelectionEngine getEngine() {
        if (engine == null) {
            engine = new SelectionEngine(getConfiguration(), new FallbackSelector(featureConfig.switchAwayFromTools, FALLBACK));
        }

        return engine;
    }

    public boolean isEventRelevant(Object o) {
        return relevantEvents.contains(o);
    }

    public void addEventRelevant(Object o) {
        relevantEvents.add(o);
    }

    /**
     * Resets the configuration map of selectors so that it can be reprocessed
     */
    @PostProcess
    public void resetConfiguration() {
        configuration = null;
        engine = null;
        relevantEvents.clear();
    }

    private Map<Selector, Set<Selector>> makeMap(Action action) {
        var entries = getEntries(action);
        var m = new HashMap<Selector, Set<Selector>>();

        for (TargetEntry targetEntry : entries) {
            if (targetEntry.target == null) {
                continue;
            }

            var tools = getToolSelectors(targetEntry);

            m.put(new Selector(targetEntry.priority, targetEntry.target), Collections.unmodifiableSet(tools));
        }

        // Add non-tool selector
        /*if (featureConfig.switchAwayFromTools) {
            m.put(new Selector(Integer.MIN_VALUE,
                    (i, j, k) -> new Match(j.action() == Action.ATTACK)),
                    Set.of(new Selector(Integer.MIN_VALUE, SwitchRegistry.INSTANCE.nonToolMatcher)));
        }*/

        return Collections.unmodifiableMap(m);
    }

    private @NotNull Set<Selector> getToolSelectors(TargetEntry targetEntry) {
        var tools = new HashSet<Selector>();

        var p = 0;
        for (ExpressionTree tool : targetEntry.tools) {
            if (tool == null) {
                continue;
            }

            tools.add(new Selector(p--, tool));
        }

        // For empty tool selectors just select any item
        // the engine should then prefer to select the current slot
        if (tools.isEmpty()) {
            tools.add(ANY_ITEM_SELECTOR);
        }

        return tools;
    }

    private Set<TargetEntry> getEntries(Action action) {
        return switch (action) {
            case ATTACK -> attackAction;
            case STAT_CHANGE -> statChangeAction;
            case INTERACT -> interactAction;
        };
    }

    @ConfigSerializable
    public static class TargetEntry {
        @Comment("""
                The priority of selecting this target in relation to other valid targets.
                Higher values are preferred.\
                """)
        public int priority = 10;

        public ExpressionTree target;

        public ExpressionTree[] tools = new ExpressionTree[0];

        @Override
        public String toString() {
            return "TargetEntry{" +
                    "priority=" + priority +
                    ", target=" + target +
                    ", tools=" + Arrays.toString(tools) +
                    '}';
        }
    }
}
