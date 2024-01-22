package autoswitch.datagen.providers;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Blocks;

public class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public BlockTagProvider(FabricDataOutput output,
                            CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        tag(TagKey.create(Registries.BLOCK, new ResourceLocation("autoswitch:shears_efficient")))
                .addOptionalTag(FabricMineableTags.SHEARS_MINEABLE.location())
                //todo which MC will be real?
                .addOptionalTag(new ResourceLocation("minecraft:shears_efficient"))
                .addOptionalTag(new ResourceLocation("minecraft:mineable/shears"))
                // Copied from ShearsItem postMine and getMiningSpeedMultiplier
                .addOptionalTag(BlockTags.LEAVES.location())
                .addOptionalTag(BlockTags.WOOL.location())
                .addOptional(BuiltInRegistries.BLOCK.getKey(Blocks.COBWEB))
                .addOptional(BuiltInRegistries.BLOCK.getKey(Blocks.SHORT_GRASS))
                .addOptional(new ResourceLocation("grass")) // This was renamed to short_grass in ~1.20.4
                .addOptional(BuiltInRegistries.BLOCK.getKey(Blocks.FERN))
                .addOptional(BuiltInRegistries.BLOCK.getKey(Blocks.DEAD_BUSH))
                .addOptional(BuiltInRegistries.BLOCK.getKey(Blocks.HANGING_ROOTS))
                .addOptional(BuiltInRegistries.BLOCK.getKey(Blocks.VINE))
                .addOptional(BuiltInRegistries.BLOCK.getKey(Blocks.TRIPWIRE))
                .addOptional(BuiltInRegistries.BLOCK.getKey(Blocks.GLOW_LICHEN));
        // Exists as bamboo and cobweb isn't in sword_efficient
        tag(TagKey.create(Registries.BLOCK, new ResourceLocation("autoswitch:sword_efficient")))
                .addOptionalTag(FabricMineableTags.SWORD_MINEABLE.location())
                .addOptionalTag(BlockTags.SWORD_EFFICIENT.location())
                .addOptional(BuiltInRegistries.BLOCK.getKey(Blocks.BAMBOO))
                .addOptional(BuiltInRegistries.BLOCK.getKey(Blocks.COBWEB))
                .addOptional(BuiltInRegistries.BLOCK.getKey(Blocks.BAMBOO_SAPLING));
        tag(TagKey.create(Registries.BLOCK, new ResourceLocation("autoswitch:bamboo")))
                .addOptional(BuiltInRegistries.BLOCK.getKey(Blocks.BAMBOO))
                .addOptional(BuiltInRegistries.BLOCK.getKey(Blocks.BAMBOO_SAPLING));

    }

}
