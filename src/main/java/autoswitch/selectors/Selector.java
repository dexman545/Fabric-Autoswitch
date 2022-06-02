package autoswitch.selectors;


import java.util.function.Predicate;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface Selector<T> {
    boolean matches(T compare);

    Registry<T> getRegistry();

    default Predicate<T> makeFutureRegistryEntryPredicate(Identifier id) {
        return entry -> new FutureRegistryEntry<>(getRegistry(), id).matches(entry);
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
