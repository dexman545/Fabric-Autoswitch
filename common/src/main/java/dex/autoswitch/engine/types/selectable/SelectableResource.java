package dex.autoswitch.engine.types.selectable;

import dex.autoswitch.engine.data.extensible.SelectableType;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

public abstract class SelectableResource<VAL> extends SelectableType<Identifier, Holder<VAL>, TagKey<VAL>> {
    protected SelectableResource(String id) {
        super(id);
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
