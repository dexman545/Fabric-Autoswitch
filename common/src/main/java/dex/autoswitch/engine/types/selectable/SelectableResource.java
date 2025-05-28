package dex.autoswitch.engine.types.selectable;

import dex.autoswitch.engine.data.extensible.SelectableType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public abstract class SelectableResource<VAL> extends SelectableType<ResourceLocation, Holder<VAL>, TagKey<VAL>> {
    protected SelectableResource(String id) {
        super(id);
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
