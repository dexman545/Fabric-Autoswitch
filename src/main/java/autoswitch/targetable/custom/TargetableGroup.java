package autoswitch.targetable.custom;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Create custom targets that capture a group of similar entries.
 * Targets should only be of type {@link net.minecraft.entity.EntityType} or {@link net.minecraft.block.Block}
 */
public final class TargetableGroup<T> {
    /**
     * The map of targets to {@link TargetableGroup}s
     */
    private static final Map<Object, TargetableGroup<?>> ALL_TARGETABLES = new IdentityHashMap<>();
    /**
     * The targets of this target group
     */
    private final Set<T> targetEntries;

    @SafeVarargs
    public TargetableGroup(T... targets) {
        Set<T> set = new HashSet<>(targets.length);

        set.addAll(Arrays.asList(targets));

        this.targetEntries = Collections.unmodifiableSet(set);

        addTargetsToGlobal(this);
    }

    /**
     * @param targetEntry the target that may be in a target group
     *
     * @return the target group if it is present
     */
    // Uses Optional<Object> to make working with it easier, we don't actually need the target group elsewhere
    public static Optional<Object> maybeGetTarget(Object targetEntry) {
        return Optional.ofNullable(ALL_TARGETABLES.get(targetEntry));
    }

    /**
     * Add all targets and the group to the global map, {@link TargetableGroup#ALL_TARGETABLES}
     */
    private static void addTargetsToGlobal(TargetableGroup<?> targetableGroup) {
        for (Object targetEntry : targetableGroup.getTargetEntries()) {
            ALL_TARGETABLES.put(targetEntry, targetableGroup);
        }
    }

    /**
     * @return the set of targets this group targets
     */
    public Set<T> getTargetEntries() {
        return targetEntries;
    }

}
