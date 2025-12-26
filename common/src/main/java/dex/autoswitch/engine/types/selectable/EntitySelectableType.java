package dex.autoswitch.engine.types.selectable;

import dex.autoswitch.engine.TargetType;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.futures.FutureSelectable;
import dex.autoswitch.platform.Services;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntitySelectableType extends SelectableResource<EntityType<?>> {
    public static final EntitySelectableType INSTANCE = new EntitySelectableType();

    protected EntitySelectableType() {
        super("entity");
    }

    @Override
    public Holder<EntityType<?>> lookup(ResourceLocation resourceLocation) {
        return BuiltInRegistries.ENTITY_TYPE.get(resourceLocation).orElse(null);
    }

    @Override
    public TagKey<EntityType<?>> lookupGroup(ResourceLocation resourceLocation) {
        return TagKey.create(Registries.ENTITY_TYPE, resourceLocation);
    }

    @Override
    public boolean matches(SelectionContext context, Holder<EntityType<?>> v, Object selectable) {
        var ref = v.value();
        if (selectable instanceof Entity entity) {
            return ref.equals(entity.getType());
        }

        if (selectable instanceof EntityType<?> type) {
            return ref.equals(type);
        }

        return false;
    }

    @Override
    public boolean matchesGroup(SelectionContext context, TagKey<EntityType<?>> tagKey, Object selectable) {
        if (selectable instanceof Entity entity) {
            return Services.PLATFORM.isInTag(tagKey, entity.getType());
        }

        if (selectable instanceof EntityType<?> type) {
            return Services.PLATFORM.isInTag(tagKey, type);
        }

        return false;
    }

    @Override
    public @Nullable TargetType targetType() {
        return TargetType.ENTITIES;
    }

    @Override
    public boolean isOf(Object o) {
        return o instanceof Entity || o instanceof EntityType<?> || (o instanceof Holder<?> h && h.value() instanceof Entity);
    }

    @Override
    public double typeRating(SelectionContext context, FutureSelectable<ResourceLocation, Holder<EntityType<?>>> futureValue, Object selectable) {
        return 0;
    }
}
