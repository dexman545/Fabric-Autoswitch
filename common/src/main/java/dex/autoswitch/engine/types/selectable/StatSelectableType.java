package dex.autoswitch.engine.types.selectable;

import java.util.Optional;

import dex.autoswitch.engine.TargetType;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.SelectableType;
import dex.autoswitch.futures.FutureSelectable;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class StatSelectableType extends SelectableType<Identifier, Stat<?>, Void> {
    public static final StatSelectableType INSTANCE = new StatSelectableType();

    protected StatSelectableType() {
        super("stat");
    }

    /**
     * Stats have a <a href="https://minecraft.wiki/w/Statistics">weird format</a>.
     * <p>
     * See Command parsing for reference {@link ObjectiveCriteria#byName(String)}
     */
    @Override
    public Stat<?> lookup(Identifier identifier) {
        var statType = Identifier.bySeparator(identifier.getNamespace(), '.');
        var statName = Identifier.bySeparator(identifier.getPath(), '.');

        var stat = BuiltInRegistries.STAT_TYPE
                .getOptional(statType)
                .flatMap(type -> getStat(type, statName));
        return stat.orElse(null);
    }

    private static <T> Optional<Stat<?>> getStat(StatType<T> statType, Identifier identifier) {
        return statType.getRegistry().getOptional(identifier).map(statType::get);
    }

    @Override
    public Void lookupGroup(Identifier identifier) {
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
    public double typeRating(SelectionContext context, FutureSelectable<Identifier, Stat<?>> futureValue, Object selectable) {
        return 0;
    }

    @Override
    public String serializeKey(Identifier identifier) {
        return identifier.toString();
    }

    @Override
    public Identifier deserializeKey(String key) {
        var id = Identifier.tryParse(key);

        if (id != null) {
            return id;
        }

        throw new NullPointerException("Invalid Identifier: " + key);
    }
}
