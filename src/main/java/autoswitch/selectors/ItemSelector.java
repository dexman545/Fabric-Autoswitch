package autoswitch.selectors;

import java.util.function.Predicate;

import autoswitch.selectors.futures.IdentifiedTag;
import autoswitch.selectors.futures.RegistryType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ItemSelector implements Selector<Item> {

    private final Predicate<Object> predicate;
    private final String entryName;

    public ItemSelector(ResourceLocation id) {
        predicate = makeFutureRegistryEntryPredicate(RegistryType.ITEM, id);
        entryName = id.toString();
    }

    public ItemSelector(TagKey<Item> tagKey) {
        this(IdentifiedTag.makeItemPredicate(tagKey), "item@" + tagKey.location().toString());
    }

    public ItemSelector(Predicate<Object> predicate, String entryName) {
        this.predicate = predicate;
        this.entryName = entryName;
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
        return "ItemSelector{" + configEntry() + '}';
    }

    @Override
    public String configEntry() {
        return entryName;
    }

    @Override
    public String separator() {
        return ";";
    }

}
