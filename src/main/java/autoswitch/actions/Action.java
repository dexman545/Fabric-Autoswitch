package autoswitch.actions;

import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Supplier;

import autoswitch.AutoSwitch;
import autoswitch.api.AutoSwitchMap;
import autoswitch.selectors.ItemTarget;
import autoswitch.selectors.TargetableGroup;
import autoswitch.selectors.futures.FutureRegistryEntry;
import autoswitch.util.TargetableCache;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Mutable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;

public enum Action {
    ATTACK(true, () -> AutoSwitch.attackActionCfg),
    INTERACT(false, () -> AutoSwitch.useActionCfg),
    EVENT(false, () -> AutoSwitch.eventActionConfig);

    private static final int MAX_CACHE_SIZE = 128;
    /**
     * Map of target object to list of toolSelector IDs for the action.
     */
    private final Object2ObjectOpenCustomHashMap<Object, IntArrayList> target2ToolSelectorsMap;
    /**
     * API Map - this map is passed to interfacing mods for them to add to it.
     * <p>
     * A map of config key -> config value for the action.
     */
    private final AutoSwitchMap<String, String> configMap;

    private final TargetableCache actionCache;
    private final boolean allowNullItemFallback;

    private final Supplier<Accessible> actionConfig;

    Action(boolean allowNullItemFallback, Supplier<Accessible> actionConfig) {
        this.allowNullItemFallback = allowNullItemFallback;
        this.actionConfig = actionConfig;
        target2ToolSelectorsMap = new Object2ObjectOpenCustomHashMap<>(new FutureRegistryEntry.TargetHashingStrategy());
        configMap = new AutoSwitchMap<>();
        actionCache = new TargetableCache(MAX_CACHE_SIZE);
    }

    public Object getTarget(Object protoTarget) {
        return getTarget(target2ToolSelectorsMap, protoTarget);
    }

    /**
     * Extract target from protoTarget, given a map of targets to examine.
     *
     * @param map         map of targets to compare protoTarget to
     * @param protoTarget object to extract target data from
     *
     * @return target
     */
    private static Object getTarget(Map<Object, IntArrayList> map, Object protoTarget) {
        if (protoTarget instanceof ItemTarget) return protoTarget;

        // These methods were moved to AbstractBlockState in 20w12a,
        // so their intermediary name changed breaking compatibility
        if (protoTarget instanceof BlockState state) {
            // Block Override
            Block block = state.getBlock();
            if (map.containsKey(block)) {
                return block;
            }
            return TargetableGroup.maybeGetTarget(protoTarget)
                                  .orElse(TargetableGroup.maybeGetTarget(block).orElse(state.getMaterial()));
        }

        if (protoTarget instanceof Entity e) {
            // Entity Override
            EntityType<?> entityType = e.getType();
            if (map.containsKey(entityType)) {
                return entityType;
            }

            return TargetableGroup.maybeGetTarget(protoTarget)
                                  .orElse(TargetableGroup.maybeGetTarget(entityType)
                                                         .orElse(protoTarget instanceof LivingEntity ?
                                                                 ((LivingEntity) protoTarget).getGroup() :
                                                                 entityType));
        }

        return null;
    }

    public void resetCache() {
        actionCache.clear();
    }

    public void clearSelectors() {
        target2ToolSelectorsMap.clear();
    }

    public static void resetAllActionStates() {
        for (Action action : values()) {
            action.resetCache();
        }
    }

    public OptionalInt getCachedSlot(Object target) {
        return actionCache.containsKey(target) ? OptionalInt.of(actionCache.getInt(target)) : OptionalInt.empty();
    }

    /**
     * Map of target object to list of toolSelector IDs for the action.
     */
    public Object2ObjectOpenCustomHashMap<Object, IntArrayList> getTarget2ToolSelectorsMap() {
        return target2ToolSelectorsMap;
    }

    /**
     * API Map - this map is passed to interfacing mods for them to add to it.
     * <p>
     * A map of config key -> config value for the action.
     */
    public AutoSwitchMap<String, String> getConfigMap() {
        return configMap;
    }

    public TargetableCache getActionCache() {
        return actionCache;
    }

    public boolean allowNullItemFallback() {
        return allowNullItemFallback;
    }

    @SuppressWarnings("unchecked")
    public <T extends Mutable & Accessible> T getActionConfig() {
        return (T) actionConfig.get();
    }
}
