package autoswitch.selectors;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import autoswitch.AutoSwitch;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

/**
 * Create custom targets that capture a group of similar entries. Favors the narrower entries. Targets should only be of
 * type {@link net.minecraft.world.entity.EntityType} or {@link net.minecraft.world.level.block.Block}
 */
public final class TargetableGroup<T> {
    /**
     * The map of targets to {@link TargetableGroup}s
     */
    private static final Map<Object, TargetableGroup<?>> ALL_TARGETABLES = new IdentityHashMap<>();
    /**
     * The map of {@link TargetPredicate}s to {@link TargetableGroup}s
     */
    private static final Map<TargetPredicate, TargetableGroup<?>> ALL_PREDICATES = new IdentityHashMap<>();
    /**
     * The targets of this target group
     */
    private final Set<T> targetEntries;

    private final String groupName;

    @SafeVarargs
    public TargetableGroup(String groupName, T... targets) {
        this.groupName = groupName;
        Set<T> set = new HashSet<>(targets.length);

        for (T target : targets) {
            if (!set.add(target)) {
                AutoSwitch.logger.error("Attempted to add {} to TargetGroup {}, but it was already in group {} - " +
                                        "ignoring the new addition", getTargetId(target), groupName,
                                        ALL_TARGETABLES.get(target).getGroupName());
            }
        }

        this.targetEntries = Collections.unmodifiableSet(set);

        addTargetsToGlobal(this);
    }

    public TargetableGroup(String groupName, TargetPredicate targetPredicate) {
        this.groupName = groupName;
        ALL_PREDICATES.put(targetPredicate, this);

        this.targetEntries = null;
    }

    /**
     * @param targetEntry the target that may be in a target group
     *
     * @return the target group if it is present
     */
    // Uses Optional<Object> to make working with it easier, we don't actually need the target group elsewhere
    public static Optional<Object> maybeGetTarget(Object targetEntry) {
        return Optional.ofNullable(maybeGetPredicate(targetEntry).orElse(ALL_TARGETABLES.get(targetEntry)));
    }

    //todo test if predicates overlap with each other
    private static Optional<TargetableGroup<?>> maybeGetPredicate(Object targetEntry) {
        for (TargetPredicate targetPredicate : ALL_PREDICATES.keySet()) {
            if (targetPredicate.test(targetEntry)) return Optional.of(ALL_PREDICATES.get(targetPredicate));
        }

        return Optional.empty();
    }

    private static String getTargetId(Object target) {
        if (target instanceof EntityType<?>) {
            return EntityType.getKey((EntityType<?>) target).toString();
        }

        if (target instanceof Block) {
            return BuiltInRegistries.BLOCK.getKey((Block) target).toString();
        }

        return "IdentifierNotFound[" + target + "]";
    }

    /**
     * Add all targets and the group to the global map, {@link TargetableGroup#ALL_TARGETABLES}
     */
    private static void addTargetsToGlobal(TargetableGroup<?> targetableGroup) {
        for (Object targetEntry : targetableGroup.getTargetEntries()) {
            ALL_TARGETABLES.put(targetEntry, targetableGroup);
        }
    }

    public static void validatePredicates() {
        for (TargetPredicate targetPredicate : ALL_PREDICATES.keySet()) {
            Set<Object> overlap = ALL_TARGETABLES.keySet().parallelStream().filter(targetPredicate.predicate)
                                                 .collect(Collectors.toSet());
            if (!overlap.isEmpty()) {
                Set<TargetableGroup<?>> targetableGroups = new HashSet<>();
                overlap.forEach(target -> targetableGroups.add(ALL_TARGETABLES.get(target)));
                AutoSwitch.logger.error("A TargetPredicate for {} overlapped with the narrower target(s) {} of " +
                                        "TargetGroup(s) {}, limiting TargetPredicate to ignore the narrower targets",
                                        targetPredicate, overlap, targetableGroups);

                for (Object o : overlap) {
                    targetPredicate.limitPredicate(o);
                }
            }
        }
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public String toString() {
        return "TargetableGroup{" + "targetEntries=" + targetEntries + ", groupName='" + groupName + '\'' + '}';
    }

    /**
     * @return the set of targets this group targets
     */
    public Set<T> getTargetEntries() {
        return targetEntries;
    }

    /**
     * Stores information on what the predicate should match
     */
    public static class TargetPredicate {
        private final String matches;
        private Predicate<Object> predicate;

        public TargetPredicate(String matches, Predicate<Object> predicate) {
            this.matches = matches;
            this.predicate = predicate;
        }

        public boolean test(Object obj) {
            return predicate.test(obj);
        }

        /**
         * Do not match the predicate for this obj
         */
        void limitPredicate(Object obj) {
            predicate = predicate.and(Predicate.isEqual(obj).negate());
        }

        @Override
        public String toString() {
            return "TargetPredicate{" + "matches='" + matches + '\'' + ", predicate=" + predicate + '}';
        }

    }

}
