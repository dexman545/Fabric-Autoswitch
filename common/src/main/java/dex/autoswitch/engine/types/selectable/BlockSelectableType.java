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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockSelectableType extends SelectableResource<Block> {
    public static final BlockSelectableType INSTANCE = new BlockSelectableType();

    private BlockSelectableType() {
        super("block");
    }

    @Override
    public Holder<Block> lookup(ResourceLocation resourceLocation) {
        return BuiltInRegistries.BLOCK.get(resourceLocation).orElse(null);
    }

    @Override
    public TagKey<Block> lookupGroup(ResourceLocation resourceLocation) {
        return TagKey.create(Registries.BLOCK, resourceLocation);
    }

    @Override
    public boolean matches(SelectionContext context, Holder<Block> v, Object selectable) {
        var refBlock = v.value();
        if (selectable instanceof BlockState state) {
            return refBlock.equals(state.getBlock());
        }

        if (selectable instanceof Block block) {
            return refBlock.equals(block);
        }

        return false;
    }

    @Override
    public boolean matchesGroup(SelectionContext context, TagKey<Block> blockTagKey, Object selectable) {
        if (selectable instanceof BlockState state) {
            return Services.PLATFORM.isInTag(blockTagKey, state.getBlock());
        }

        if (selectable instanceof Block block) {
            return Services.PLATFORM.isInTag(blockTagKey, block);
        }

        return false;
    }

    @Override
    public @Nullable TargetType targetType() {
        return TargetType.BLOCKS;
    }

    @Override
    public boolean isOf(Object o) {
        return o instanceof BlockState || o instanceof Block || (o instanceof Holder<?> h && h.value() instanceof Block);
    }

    @Override
    public double typeRating(SelectionContext context, FutureSelectable<ResourceLocation, Holder<Block>> futureValue, Object selectable) {
        return 0;
    }
}
