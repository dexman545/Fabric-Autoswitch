package autoswitch.selectors;


import java.util.function.Predicate;

import autoswitch.selectors.futures.FutureRegistryEntry;
import autoswitch.util.RegistryHelper;

import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface Selector<T> {
    boolean matches(T compare);

    Registry<T> getRegistry();

    default Predicate<T> makeFutureRegistryEntryPredicate(Identifier id, Class<T> clazz) {
        var fre = FutureRegistryEntry.getOrCreateEntry(getRegistry(), id, clazz);
        return fre::matches;
    }

    default Predicate<T> makeIsInTagPredicate(TagKey<T> tagKey) {
        return obj -> RegistryHelper.isInTag(getRegistry(), tagKey, obj);
    }

    default Selector<T> or(Selector<T> other) {
        return new Selector<>() {
            @Override
            public boolean matches(T compare) {
                return Selector.this.matches(compare) || other.matches(compare);
            }

            @Override
            public Registry<T> getRegistry() {
                return Selector.this.getRegistry();
            }
        };
    }


}
