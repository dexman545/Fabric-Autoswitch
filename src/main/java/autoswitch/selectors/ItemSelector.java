package autoswitch.selectors;

import autoswitch.selectors.futures.RegistryType;

import autoswitch.selectors.selectable.Selectables;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.function.Predicate;

public class ItemSelector implements Selector<Object> {

    private final Predicate<Object> predicate;

    public ItemSelector(Identifier id) {
        predicate = makeFutureRegistryEntryPredicate(RegistryType.ITEM, id);
    }

    public ItemSelector(TagKey<Item> tagKey) {
        this(o -> {
            var m = Selectables.getSelectableItem(o);
            return m.filter(selectableItem -> selectableItem.isIn().apply(selectableItem.safety(o), tagKey))
                    .isPresent();
        });
    }

    public ItemSelector(Predicate<Object> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean matches(Object compare) {
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
