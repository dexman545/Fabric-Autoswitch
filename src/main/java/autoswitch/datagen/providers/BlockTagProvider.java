package autoswitch.datagen.providers;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;

import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public BlockTagProvider(FabricDataOutput output,
                            CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(TagKey.of(RegistryKeys.BLOCK, new Identifier("autoswitch:shears_efficient")))
                .addOptionalTag(FabricMineableTags.SHEARS_MINEABLE)
                //todo which MC will be real?
                .addOptionalTag(TagKey.of(RegistryKeys.BLOCK, new Identifier("minecraft:shears_efficient")))
                .addOptionalTag(TagKey.of(RegistryKeys.BLOCK, new Identifier("minecraft:mineable/shears")))
                // Copied from ShearsItem postMine and getMiningSpeedMultiplier
                .addOptionalTag(BlockTags.LEAVES)
                .addOptionalTag(BlockTags.WOOL)
                .addOptional(Registries.BLOCK.getId(Blocks.COBWEB))
                .addOptional(Registries.BLOCK.getId(Blocks.SHORT_GRASS))
                .addOptional(new Identifier("grass")) // This was renamed to short_grass in ~1.20.4
                .addOptional(Registries.BLOCK.getId(Blocks.FERN))
                .addOptional(Registries.BLOCK.getId(Blocks.DEAD_BUSH))
                .addOptional(Registries.BLOCK.getId(Blocks.HANGING_ROOTS))
                .addOptional(Registries.BLOCK.getId(Blocks.VINE))
                .addOptional(Registries.BLOCK.getId(Blocks.TRIPWIRE))
                .addOptional(Registries.BLOCK.getId(Blocks.GLOW_LICHEN));
        // Exists as bamboo and cobweb isn't in sword_efficient
        getOrCreateTagBuilder(TagKey.of(RegistryKeys.BLOCK, new Identifier("autoswitch:sword_efficient")))
                .addOptionalTag(FabricMineableTags.SWORD_MINEABLE)
                .addOptionalTag(BlockTags.SWORD_EFFICIENT)
                .addOptional(Registries.BLOCK.getId(Blocks.BAMBOO))
                .addOptional(Registries.BLOCK.getId(Blocks.COBWEB))
                .addOptional(Registries.BLOCK.getId(Blocks.BAMBOO_SAPLING));
        getOrCreateTagBuilder(TagKey.of(RegistryKeys.BLOCK, new Identifier("autoswitch:bamboo")))
                .addOptional(Registries.BLOCK.getId(Blocks.BAMBOO))
                .addOptional(Registries.BLOCK.getId(Blocks.BAMBOO_SAPLING));

    }

}
