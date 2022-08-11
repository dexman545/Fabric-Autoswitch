package autoswitch.selectors;

import java.util.function.Predicate;

import autoswitch.selectors.futures.IdentifiedTag;
import autoswitch.selectors.futures.RegistryType;

import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;

public class ItemSelector implements Selector<Item> {

    private final Predicate<Object> predicate;

    public ItemSelector(Identifier id) {
        predicate = makeFutureRegistryEntryPredicate(RegistryType.ITEM, id);
    }

    public ItemSelector(TagKey<Item> tagKey) {
        this(IdentifiedTag.makeItemPredicate(tagKey));
    }

    public ItemSelector(Predicate<Object> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean matches(Item compare) {
        return predicate.test(compare);
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
