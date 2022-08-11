package autoswitch.selectors;


import java.util.function.Predicate;

import autoswitch.selectors.futures.FutureRegistryEntry;
import autoswitch.selectors.futures.RegistryType;

import net.minecraft.util.Identifier;

public interface Selector<T> {
    boolean matches(T compare);

    default Predicate<Object> makeFutureRegistryEntryPredicate(RegistryType type, Identifier id) {
        var fre = FutureRegistryEntry.getOrCreate(type, id);
        return fre::matches;
    }

}
