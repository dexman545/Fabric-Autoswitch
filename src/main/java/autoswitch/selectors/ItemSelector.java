package autoswitch.selectors;

import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Predicate;

public class ItemSelector implements Selector<Item> {

    private final Predicate<Item> predicate;

    public ItemSelector(Identifier id) {
        predicate = makeFutureRegistryEntryPredicate(id);
    }

    public ItemSelector(Item item) {
        this(item::equals);
    }

    public ItemSelector(TagKey<Item> tagKey) {
        this(item -> item.getRegistryEntry().isIn(tagKey));
    }

    public ItemSelector(Predicate<Item> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean matches(Item compare) {
        return predicate.test(compare);
    }

    @Override
    public Registry<Item> getRegistry() {
        return Registry.ITEM;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemSelector that = (ItemSelector) o;

        return predicate.equals(that.predicate);
    }

    @Override
    public int hashCode() {
        return predicate.hashCode();
    }

    @Override
    public String toString() {
        return "ItemSelector{" + "predicate=" + predicate + '}';
    }

}
