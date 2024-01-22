package autoswitch.selectors;


import java.util.function.Predicate;

import autoswitch.config.io.ConfigWritable;
import autoswitch.selectors.futures.FutureRegistryEntry;
import autoswitch.selectors.futures.RegistryType;

import net.minecraft.resources.ResourceLocation;

public interface Selector<T> extends ConfigWritable {
    boolean matches(T compare);

    default Predicate<Object> makeFutureRegistryEntryPredicate(RegistryType type, ResourceLocation id) {
        var fre = FutureRegistryEntry.getOrCreate(type, id);
        return fre::matches;
    }

}
