package dex.autoswitch.engine.types.selectable;

import dex.autoswitch.engine.TargetType;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.SelectableType;
import dex.autoswitch.futures.FutureSelectable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class StatSelectableType extends SelectableType<ResourceLocation, Stat<?>, Void> {
    public static final StatSelectableType INSTANCE = new StatSelectableType();

    protected StatSelectableType() {
        super("stat");
    }

    /**
     * Stat's have a <a href="https://minecraft.wiki/w/Statistics">weird format</a>.
     * <p>
     * See Command parsing for reference {@link ObjectiveCriteria#byName(String)}
     */
    @Override
    public Stat<?> lookup(ResourceLocation resourceLocation) {
        var statType = ResourceLocation.bySeparator(resourceLocation.getNamespace(), '.');
        var statName = ResourceLocation.bySeparator(resourceLocation.getPath(), '.');

        var stat = BuiltInRegistries.STAT_TYPE
                .getOptional(statType)
                .flatMap(type -> getStat(type, statName));
        return stat.orElse(null);
    }

    private static <T> Optional<Stat<?>> getStat(StatType<T> statType, ResourceLocation resourceLocation) {
        return statType.getRegistry().getOptional(resourceLocation).map(statType::get);
    }

    @Override
    public Void lookupGroup(ResourceLocation resourceLocation) {
        return null;
    }

    @Override
    public boolean matches(SelectionContext context, Stat<?> v, Object selectable) {
        if (selectable instanceof Stat<?> stat) {
            return v.equals(stat);
        }
        return false;
    }

    @Override
    public boolean matchesGroup(SelectionContext context, Void statTagKey, Object selectable) {
        return false;
    }

    @Override
    public @Nullable TargetType targetType() {
        return TargetType.EVENTS;
    }

    @Override
    public boolean isOf(Object o) {
        return o instanceof Stat<?>;
    }

    @Override
    public double typeRating(SelectionContext context, FutureSelectable<ResourceLocation, Stat<?>> futureValue, Object selectable) {
        return 0;
    }

    @Override
    public String serializeKey(ResourceLocation resourceLocation) {
        return resourceLocation.toString();
    }

    @Override
    public ResourceLocation deserializeKey(String key) {
        var id = ResourceLocation.tryParse(key);

        if (id != null) {
            return id;
        }

        throw new NullPointerException("Invalid ResourceLocation: " + key);
    }
}
